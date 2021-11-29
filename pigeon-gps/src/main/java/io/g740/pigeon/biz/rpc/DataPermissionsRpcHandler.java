package io.g740.pigeon.biz.rpc;

import io.g740.commons.api.Response;
import io.g740.commons.constants.NetworkingConstants;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author bbottong
 */
@Handler
public class DataPermissionsRpcHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataPermissionsRpcHandler.class);

    /**
     * TODO 为了pigeon产品化，不应默认有个cuckoo(用户中心）服务来提供针对表的字段的数据权限。而应该是配置产生。当然，可以制定提供基于表的数据权限的接口定义。
     */
    private String rpcCuckooUrlListAllResourceStructures = "TODO for pigeon 产品化";

    /**
     * TODO 为了pigeon产品化，不应默认有个cuckoo(用户中心）服务来提供针对表的字段的数据权限。而应该是配置产生。当然，可以制定提供基于表的数据权限的接口定义。
     */
    private String rpcCuckooUrlGetResourceContent = "TODO for pigeon 产品化";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 定义类，以便处理RestTemplate接受返回对象的泛型问题
     */
    public static class ResponseSimpleTreeNode extends Response<SimpleTreeNode> {

    }

    /**
     * 列出所有资源类目(resource categories)及每个资源类目(resource category)的资源结构Hierarchy(resource structure hierarchy)
     *
     * @return
     * @throws Exception
     */
    public SimpleTreeNode listAllResourceStructures(UserInfo operationUserInfo) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(NetworkingConstants.HEADER_USERNAME, operationUserInfo.getUsername());
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity httpEntity = new HttpEntity(httpHeaders);

        ResponseEntity<ResponseSimpleTreeNode> responseEntity;

        try {
            responseEntity = this.restTemplate.exchange(this.rpcCuckooUrlListAllResourceStructures, HttpMethod.GET, httpEntity, ResponseSimpleTreeNode.class);
        } catch (Exception e) {
            LOGGER.info(String.format("Failed invoking %s", this.rpcCuckooUrlListAllResourceStructures)
                    + ". More info:" + e.getMessage());
            throw e;
        }

        Response response = responseEntity.getBody();
        HttpStatus statusCode = responseEntity.getStatusCode();
        if (HttpStatus.OK.equals(statusCode)) {
            LOGGER.info(String.format("Done invoking %s, response:", this.rpcCuckooUrlListAllResourceStructures,
                    response.toString()));
        } else {
            String errorMessage = String.format("Failed invoking %s. More info: statusCode %s",
                    this.rpcCuckooUrlListAllResourceStructures,
                    responseEntity.getStatusCode());
            throw new Exception(errorMessage);
        }

        Integer errCode = response.getErrCode();
        if (errCode == null) {
            Object object = response.getObject();
            if (object != null) {
                if (object instanceof SimpleTreeNode) {
                    SimpleTreeNode result = (SimpleTreeNode) object;
                    return result;
                } else {
                    String errorMessage =
                            "Unknown object instance, should be type: SimpleTreeNode, but got " + object.getClass().getName();
                    throw new Exception(errorMessage);
                }
            } else {
                return null;
            }
        } else {
            throw new Exception(response.toString());
        }
    }

    /**
     * 列出指定用户在指定资源类目(resource category)领域所授权的内容Hierarchy(resource content hierarchy)
     *
     * @param username                  指定用户
     * @param listOfResourceCategoryUid 指定资源类目(resource category)
     * @return
     * @throws Exception
     */
    public SimpleTreeNode getResourceContent(
            String username, List<Long> listOfResourceCategoryUid, UserInfo operationUserInfo) throws Exception {
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append(this.rpcCuckooUrlGetResourceContent);
        urlStringBuilder.append("?");
        urlStringBuilder.append("username=" + username);
        if (CollectionUtils.isNotEmpty(listOfResourceCategoryUid)) {
            for (Long resourceCategoryUid : listOfResourceCategoryUid) {
                urlStringBuilder.append("&resource_category_uid=" + resourceCategoryUid);
            }
        }
        String url = urlStringBuilder.toString();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(NetworkingConstants.HEADER_USERNAME, operationUserInfo.getUsername());
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity httpEntity = new HttpEntity(httpHeaders);


        ResponseEntity<ResponseSimpleTreeNode> responseEntity;
        try {
            responseEntity = this.restTemplate.exchange(
                    url, HttpMethod.GET, httpEntity, ResponseSimpleTreeNode.class);
        } catch (Exception e) {
            LOGGER.info(String.format("Failed invoking %s", url) + ". More info:" + e.getMessage());
            throw e;
        }

        Response response = responseEntity.getBody();
        HttpStatus statusCode = responseEntity.getStatusCode();
        if (HttpStatus.OK.equals(statusCode)) {
            LOGGER.info(String.format("Done invoking %s, response: %s", url, response.toString()));
        } else {
            String errorMessage = String.format("Failed invoking %s. More info: statusCode %s", url,
                    responseEntity.getStatusCode());
            throw new Exception(errorMessage);
        }
        Integer errCode = response.getErrCode();
        if (errCode == null) {
            Object object = response.getObject();
            if (object != null) {
                if (object instanceof SimpleTreeNode) {
                    SimpleTreeNode result = (SimpleTreeNode) object;
                    return result;
                } else {
                    String errorMessage =
                            "Unknown object instance, should be type: SimpleTreeNode, but got " + object.getClass().getName();
                    throw new Exception(errorMessage);
                }
            } else {
                return null;
            }
        } else {
            throw new Exception(response.toString());
        }
    }

}
