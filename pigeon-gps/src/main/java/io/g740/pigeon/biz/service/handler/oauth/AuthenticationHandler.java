package io.g740.pigeon.biz.service.handler.oauth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.g740.commons.constants.InfrastructureConstants;
import io.g740.commons.exception.impl.*;
import io.g740.commons.types.BaseDo;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;

import io.g740.components.uid.tinyid.IdHelper;
import io.g740.pigeon.biz.object.dto.oauth.*;
import io.g740.pigeon.biz.object.entity.account.CredentialDo;
import io.g740.pigeon.biz.object.entity.account.MembershipDo;
import io.g740.pigeon.biz.object.entity.account.UserDo;
import io.g740.pigeon.biz.object.entity.app.AppDo;
import io.g740.pigeon.biz.object.entity.oauth.AppAuthenticationDo;
import io.g740.pigeon.biz.object.entity.oauth.UserAuthenticationDo;
import io.g740.pigeon.biz.persistence.jpa.account.CredentialRepository;
import io.g740.pigeon.biz.persistence.jpa.account.MembershipRepository;
import io.g740.pigeon.biz.persistence.jpa.account.UserRepository;
import io.g740.pigeon.biz.persistence.jpa.app.AppRepository;
import io.g740.pigeon.biz.persistence.jpa.oauth.AppAuthenticationRepository;
import io.g740.pigeon.biz.persistence.jpa.oauth.UserAuthenticationRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author bbottong
 */
@Handler
public class AuthenticationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationHandler.class);

    @Autowired
    private IdHelper idHelper;

    @Autowired
    private AppRepository appRepository;

    @Autowired
    private AppAuthenticationRepository appAuthenticationRepository;

    /**
     * Application Name
     */
    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * JWT的Secret
     */
    @Value("${application.oauth2.jwt.secret}")
    private String jwtSecret;

    /**
     * Access Token的过期时长（按秒计量）
     */
    @Value("${application.oauth2.jwt.expires-in-seconds}")
    private String expiresInSeconds;

    private static final String BEARER_TOKEN_TYPE = "bearer";
    private static final String REFRESH_TOKEN_TYPE = "refresh_token";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private UserAuthenticationRepository userAuthenticationRepository;

    public AccessTokenDto createAccessToken(
            CreateAccessTokenDto createAccessTokenDto) throws ServiceException {

        // Step 1, 检查资源存在
        String appKey = createAccessTokenDto.getClientId();
        AppDo appDo = this.appRepository.findByAppKey(appKey);
        if (appDo == null) {
            throw new AuthAppKeyNotFoundException(appKey);
        }

        // Step 2, 验证签名
        verifySignature(appDo, createAccessTokenDto.getSignature());


        // Step 3, 将该app key名下还未过期的access token删除
        List<AppAuthenticationDo> listOfAppAuthenticationDo = this.appAuthenticationRepository.findUnexpiredByAppKey(appKey);
        if (CollectionUtils.isNotEmpty(listOfAppAuthenticationDo)) {
            listOfAppAuthenticationDo.forEach(appAuthenticationDo -> {
                appAuthenticationDo.setDeleted(true);
                BaseDo.update(appAuthenticationDo, InfrastructureConstants.ROOT_USERNAME, new Date());
            });
            this.appAuthenticationRepository.saveAll(listOfAppAuthenticationDo);
        }

        // Step 4, 利用JWT生成Token作为Access Token
        String accessToken;
        DateTime nowDateTime = new DateTime(new Date());
        DateTime expiresAtDateTime = nowDateTime.plusSeconds(Integer.parseInt(this.expiresInSeconds));
        try {
            accessToken = encodeTokenByJwt(appKey, nowDateTime.toDate(), expiresAtDateTime.toDate());
        } catch (Exception e) {
            throw new ServiceException("failed to generate access token for app " + appKey, e);
        }

        // Step 5, 创建access token
        AppAuthenticationDo appAuthenticationDo = new AppAuthenticationDo();
        appAuthenticationDo.setUid(this.idHelper.getNextId(AppAuthenticationDo.RESOURCE_NAME));
        appAuthenticationDo.setExpired(false);
        appAuthenticationDo.setAppKey(appKey);
        appAuthenticationDo.setTokenType(BEARER_TOKEN_TYPE);
        appAuthenticationDo.setExpiresIn(Integer.parseInt(this.expiresInSeconds));
        appAuthenticationDo.setExpiresAtTimestamp(expiresAtDateTime.toDate());
        appAuthenticationDo.setAccessToken(accessToken);
        String refreshToken = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        appAuthenticationDo.setRefreshToken(refreshToken);
        BaseDo.create(appAuthenticationDo, InfrastructureConstants.ROOT_USERNAME, new Date());
        this.appAuthenticationRepository.save(appAuthenticationDo);

        // Step 6, 构建返回对象
        AccessTokenDto accessTokenDto = new AccessTokenDto();
        BeanUtils.copyProperties(appAuthenticationDo, accessTokenDto);

        return accessTokenDto;
    }

    public RefreshedAccessTokenDto refreshAccessToken(
            RefreshAccessTokenDto refreshAccessTokenDto) throws ServiceException {
        String appKey = refreshAccessTokenDto.getClientId();
        AppDo appDo = this.appRepository.findByAppKey(appKey);
        if (appDo == null) {
            throw new AuthAppKeyNotFoundException(appKey);
        }

        // Step 1, 验证签名
        verifySignature(appDo, refreshAccessTokenDto.getSignature());

        // Step 2, 找到access token
        AppAuthenticationDo appAuthenticationDo = this.appAuthenticationRepository.findByAppKeyAndRefreshToken(
                appKey, refreshAccessTokenDto.getRefreshToken());
        if (appAuthenticationDo == null) {
            throw new AuthRefreshTokenException(AppAuthenticationDo.RESOURCE_NAME + ":" +
                    appKey + "," + refreshAccessTokenDto.getRefreshToken());
        }

        if (Boolean.TRUE.equals(appAuthenticationDo.getExpired())) {
            throw new AuthRefreshTokenException(AppAuthenticationDo.RESOURCE_NAME + ":" +
                    appKey + "," + refreshAccessTokenDto.getRefreshToken());
        }

        // Step 3, 利用JWT生成Token作为Access Token
        String accessToken;
        DateTime nowDateTime = new DateTime(new Date());
        DateTime expiresAtDateTime = nowDateTime.plusSeconds(Integer.parseInt(this.expiresInSeconds));
        try {
            accessToken = encodeTokenByJwt(appKey, nowDateTime.toDate(), expiresAtDateTime.toDate());
        } catch (Exception e) {
            throw new ServiceException("failed to generate access token for app " + appKey, e);
        }

        // Step 4, 更新access token
        appAuthenticationDo.setAccessToken(accessToken);
        appAuthenticationDo.setExpiresAtTimestamp(expiresAtDateTime.toDate());
        appAuthenticationDo.setExpired(false);
        appAuthenticationDo.setExpiresIn(Integer.parseInt(this.expiresInSeconds));
        BaseDo.update(appAuthenticationDo, InfrastructureConstants.ROOT_USERNAME, new Date());
        this.appAuthenticationRepository.save(appAuthenticationDo);

        // Step 5, 构建返回对象
        RefreshedAccessTokenDto refreshedAccessTokenDto = new RefreshedAccessTokenDto();
        refreshedAccessTokenDto.setAccessToken(accessToken);
        refreshedAccessTokenDto.setExpiresIn(appAuthenticationDo.getExpiresIn());

        return refreshedAccessTokenDto;
    }

    public void authenticateApp(String appKey, String accessToken) throws ServiceException {
        DecodedJWT decodedJwt;
        try {
            decodedJwt = decodeTokenByJwt(accessToken);
        } catch (Exception e) {
            LOGGER.error("failed to decode token by JWT for " + accessToken, e);
            throw new AuthAuthenticationFailedException(appKey + "," + accessToken);
        }
        if (!appKey.equalsIgnoreCase(decodedJwt.getSubject())) {
            LOGGER.error("the decoded token by JWT for " + accessToken + " is NOT for app key " + appKey);
            throw new AuthAuthenticationFailedException(appKey + "," + accessToken);
        }
    }

    private String encodeTokenByJwt(String subject, Date issuedAt, Date expiresAt) throws Exception {
        Algorithm algorithm = Algorithm.HMAC256(this.jwtSecret);

        try {
            String token = JWT.create().
                    withIssuer(this.applicationName).
                    withExpiresAt(expiresAt).
                    withSubject(subject).
                    withIssuedAt(issuedAt).
                    sign(algorithm);
            return token;
        } catch (JWTCreationException e) {
            throw new Exception("failed to encode JWT token", e);
        }
    }

    private DecodedJWT decodeTokenByJwt(String token) throws Exception {
        Algorithm algorithm = Algorithm.HMAC256(this.jwtSecret);

        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(this.applicationName).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new Exception("failed to decode JWT token", e);
        }
    }

    private void verifySignature(AppDo appDo, String signature) throws ServiceException {
        String toHashText = appDo.getAppKey() + appDo.getAppSecret();
        String hashedText = org.apache.commons.codec.digest.DigestUtils.sha256Hex(toHashText);
        if (!signature.equals(hashedText)) {
            throw new AuthSignatureException(signature);
        }
    }

    public SignedInDto signIn(SignInDto signInDto) throws ServiceException {
        String username = signInDto.getUsername();

        // Step 1, 验证用户凭据
        UserDo userDo = this.userRepository.findByUsername(username);
        if (userDo == null) {
            throw new AuthAccountNotFoundException(signInDto.getUsername());
        }
        String password = signInDto.getPassword();
        String hashedPassword = DigestUtils.sha256Hex(password);
        CredentialDo credentialDo = this.credentialRepository.findByUsername(username);
        if (credentialDo == null) {
            throw new AuthAuthenticationFailedException(username);
        }
        if (!hashedPassword.equals(credentialDo.getCredential())) {
            throw new AuthAuthenticationFailedException(username);
        }

        // Step 2, 将该username名下还未过期的access token删除
        List<UserAuthenticationDo> listOfUserAuthenticationDo =
                this.userAuthenticationRepository.findUnexpiredByUsername(username);
        if (CollectionUtils.isNotEmpty(listOfUserAuthenticationDo)) {
            listOfUserAuthenticationDo.forEach(userAuthenticationDo -> {
                userAuthenticationDo.setDeleted(true);
                BaseDo.update(userAuthenticationDo, InfrastructureConstants.ROOT_USERNAME, new Date());
            });
            this.userAuthenticationRepository.saveAll(listOfUserAuthenticationDo);
        }

        // Step 3, 生成Access Token
        String accessToken;
        DateTime nowDateTime = new DateTime(new Date());
        DateTime expiresAtDateTime = nowDateTime.plusSeconds(Integer.parseInt(this.expiresInSeconds));
        try {
            accessToken = encodeTokenByJwt(username, nowDateTime.toDate(), expiresAtDateTime.toDate());
        } catch (Exception e) {
            throw new ServiceException("failed to generate access token for username " + username, e);
        }

        // Step 4, 创建新的user鉴权记录
        UserAuthenticationDo userAuthenticationDo = new UserAuthenticationDo();
        userAuthenticationDo.setAccessToken(accessToken);
        userAuthenticationDo.setExpired(false);
        userAuthenticationDo.setExpiresAtTimestamp(expiresAtDateTime.toDate());
        userAuthenticationDo.setExpiresIn(Integer.parseInt(this.expiresInSeconds));
        String refreshToken = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        userAuthenticationDo.setRefreshToken(refreshToken);
        userAuthenticationDo.setTokenType(BEARER_TOKEN_TYPE);
        userAuthenticationDo.setUid(this.idHelper.getNextId(UserAuthenticationDo.RESOURCE_NAME));
        userAuthenticationDo.setUsername(username);
        BaseDo.create(userAuthenticationDo, InfrastructureConstants.ROOT_USERNAME, new Date());
        this.userAuthenticationRepository.save(userAuthenticationDo);

        // Step 5, 构建返回对象
        SignedInDto signedInDto = new SignedInDto();
        signedInDto.setUsername(username);
        signedInDto.setAccessToken(accessToken);
        signedInDto.setExpiresIn(userAuthenticationDo.getExpiresIn());
        signedInDto.setExpiresAtTimestamp(userAuthenticationDo.getExpiresAtTimestamp());
        signedInDto.setRefreshToken(refreshToken);
        signedInDto.setTokenType(BEARER_TOKEN_TYPE);

        return signedInDto;
    }

    public void signOut(String username) throws ServiceException {
        // Step 1, 找到username名下还未过期的access token，逻辑删除
        List<UserAuthenticationDo> listOfUserAuthenticationDo =
                this.userAuthenticationRepository.findUnexpiredByUsername(username);
        if (CollectionUtils.isNotEmpty(listOfUserAuthenticationDo)) {
            listOfUserAuthenticationDo.forEach(userAuthenticationDo -> {
                userAuthenticationDo.setDeleted(true);
                BaseDo.update(userAuthenticationDo, InfrastructureConstants.ROOT_USERNAME, new Date());
            });
            this.userAuthenticationRepository.saveAll(listOfUserAuthenticationDo);
        }
    }

    public void authenticateUser(String username, String accessToken) throws ServiceException {
        DecodedJWT decodedJwt;
        try {
            decodedJwt = decodeTokenByJwt(accessToken);
        } catch (Exception e) {
            LOGGER.error("failed to decode token by JWT for " + accessToken, e);
            throw new AuthAuthenticationFailedException(username + "," + accessToken);
        }
        if (!username.equalsIgnoreCase(decodedJwt.getSubject())) {
            LOGGER.error("the decoded token by JWT for " + accessToken + " is NOT for user " + username);
            throw new AuthAuthenticationFailedException(username + "," + accessToken);
        }
    }

    public String validateAccessTokenAndExtractUsername(String accessToken) throws ServiceException {
        DecodedJWT decodedJwt;
        try {
            decodedJwt = decodeTokenByJwt(accessToken);
        } catch (Exception e) {
            LOGGER.error("failed to decode token by JWT for " + accessToken, e);
            throw new AuthAuthenticationFailedException(accessToken);
        }

        return decodedJwt.getSubject();
    }

    public UserInfo validateAccessTokenAndRetrieveoperationUserInfo(String accessToken) throws ServiceException {
        DecodedJWT decodedJwt;
        try {
            decodedJwt = decodeTokenByJwt(accessToken);
        } catch (Exception e) {
            LOGGER.error("failed to decode token by JWT for " + accessToken, e);
            throw new AuthAuthenticationFailedException(accessToken);
        }

        String username = decodedJwt.getSubject();

        UserInfo operationUserInfo = new UserInfo();
        operationUserInfo.setUsername(username);

        MembershipDo membershipDo = this.membershipRepository.findByUsername(username);
        if (membershipDo != null) {
            operationUserInfo.setRoleName(membershipDo.getRole());
        }

        return operationUserInfo;
    }

    public static void main(String[] args) {
        generateSignature("39ee9e6210bd54aeddcebc494f87055b", "6038b1ea5d1df521206a6ed1ab07f97c");
    }

    public static void generateSignature(String appKey, String appSecret) throws ServiceException {
        String toHashText = appKey + appSecret;
        String hashedText = org.apache.commons.codec.digest.DigestUtils.sha256Hex(toHashText);
        System.out.println(hashedText);
    }
}
