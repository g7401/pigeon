package io.g740.commons.utils;

import com.alibaba.fastjson.JSONObject;
import io.g740.commons.exception.impl.AuthAuthenticationFailedException;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author bbottong
 */
public class ApiUtils {
    public static final String BEARER_TOKEN_TYPE = "Bearer";

    public static final String OAUTH_GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    public static final String OAUTH_GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    public static final String PAGE_PARAMETER_NAME = "page";
    public static final String SIZE_PARAMETER_NAME = "size";
    public static final String SORT_PARAMETER_NAME = "sort";
    public static final String ACCESS_TOKEN_PARAMETER_NAME = "access_token";
    public static final String CLIENT_ID_PARAMETER_NAME = "client_id";
    public static final String USERNAME_PARAMETER_NAME = "username";

    /**
     * 拷贝一个Map<String, String[]>集合
     * <p>
     * 在处理HttpServletRequest时，如果直接改动parameterMap，则会报错
     * "No modifications are allowed to a locked ParameterMap"
     * 则需要复制一个new parameter map，然后再在这个new parameter map上面进行改动
     *
     * @param parameterMap
     * @return
     */
    public static Map<String, String[]> copy(Map<String, String[]> parameterMap) {
        Map<String, String[]> newParameterMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            newParameterMap.put(entry.getKey(), entry.getValue());
        }
        return newParameterMap;
    }


    /**
     * 从authorization header中按照指定token type提取token
     *
     * @param authorization
     * @return
     * @throws Exception
     */
    public static String extractAccessTokenFromAuthorizationHeader(String tokenType, String authorization) throws Exception {
        String tokenTypeWithSpace = tokenType + " ";
        authorization = authorization.trim();
        if (authorization.length() <= tokenTypeWithSpace.length()) {
            throw new AuthAuthenticationFailedException("illegal authorization header");
        }
        String accessTokenType = authorization.substring(0, tokenTypeWithSpace.length());
        if (!accessTokenType.equals(tokenTypeWithSpace)) {
            throw new AuthAuthenticationFailedException("illegal authorization header");
        }
        String accessToken = authorization.substring(tokenTypeWithSpace.length()).trim();

        return accessToken;
    }

    public static String encodeToString(HttpServletRequest httpServletRequest,
                                        Object requestBodyObject) {
        Map<String, Object> map = new HashMap<>();

        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headerMap.put(headerName, httpServletRequest.getHeader(headerName));
        }
        map.put("headers", headerMap);
        map.put("parameters", httpServletRequest.getParameterMap());
        map.put("contextPath", httpServletRequest.getContextPath());
        map.put("requestUri", httpServletRequest.getRequestURI());
        map.put("cookies", httpServletRequest.getCookies());
        if (requestBodyObject != null) {
            map.put("requestBody", requestBodyObject);
        }

        return JSONObject.toJSONString(map);
    }
}
