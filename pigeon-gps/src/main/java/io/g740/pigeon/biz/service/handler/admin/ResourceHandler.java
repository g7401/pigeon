package io.g740.pigeon.biz.service.handler.admin;

import io.g740.commons.exception.impl.ServiceException;
import io.g740.commons.types.Handler;
import io.g740.commons.types.UserInfo;
import io.g740.pigeon.biz.rpc.DataPermissionsRpcHandler;
import io.g740.pigeon.biz.share.types.SimpleTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author bbottong
 */
@Handler
public class ResourceHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHandler.class);

    @Autowired
    private DataPermissionsRpcHandler cuckooRpcHandler;

    public SimpleTreeNode listAllResourceStructures(UserInfo operationUserInfo) throws ServiceException {
        try {
            return this.cuckooRpcHandler.listAllResourceStructures(operationUserInfo);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }

//        // TODO 联系用户中心获取所有resource hierarchy的structures(结构)
//        // TODO 先造一份数据
//        SimpleTreeNode rootTreeNode = SimpleTreeNode.buildRootTreeNode();
//        SimpleTreeNode marsGeographyCategoryTreeNode = new SimpleTreeNode();
//        marsGeographyCategoryTreeNode.setUid(100L);
//        marsGeographyCategoryTreeNode.setName("玛氏地理");
//        marsGeographyCategoryTreeNode.setDescription("玛氏地理Hierarchy结构");
//        marsGeographyCategoryTreeNode.setType("category");
//        marsGeographyCategoryTreeNode.setLevel(1);
//        marsGeographyCategoryTreeNode.setChildren(new ArrayList<>());
//        rootTreeNode.getChildren().add(marsGeographyCategoryTreeNode);
//
//        // 大区
//        SimpleTreeNode marsGeographyRegionTreeNode = new SimpleTreeNode();
//        marsGeographyRegionTreeNode.setUid(10001L);
//        marsGeographyRegionTreeNode.setName("大区");
//        marsGeographyRegionTreeNode.setDescription("大区");
//        marsGeographyRegionTreeNode.setType("region");
//        marsGeographyRegionTreeNode.setLevel(2);
//        marsGeographyRegionTreeNode.setChildren(new ArrayList<>());
//        marsGeographyCategoryTreeNode.getChildren().add(marsGeographyRegionTreeNode);
//
//        // 省份
//        SimpleTreeNode marsGeographyProvinceTreeNode = new SimpleTreeNode();
//        marsGeographyProvinceTreeNode.setUid(10002L);
//        marsGeographyProvinceTreeNode.setName("省份");
//        marsGeographyProvinceTreeNode.setDescription("省份");
//        marsGeographyProvinceTreeNode.setType("province");
//        marsGeographyProvinceTreeNode.setLevel(3);
//        marsGeographyProvinceTreeNode.setChildren(new ArrayList<>());
//        marsGeographyRegionTreeNode.getChildren().add(marsGeographyProvinceTreeNode);
//
//        // 城市群
//        SimpleTreeNode marsGeographyCityGroupTreeNode = new SimpleTreeNode();
//        marsGeographyCityGroupTreeNode.setUid(10003L);
//        marsGeographyCityGroupTreeNode.setName("城市群");
//        marsGeographyCityGroupTreeNode.setDescription("城市群");
//        marsGeographyCityGroupTreeNode.setType("cityGroup");
//        marsGeographyCityGroupTreeNode.setLevel(4);
//        marsGeographyCityGroupTreeNode.setChildren(new ArrayList<>());
//        marsGeographyProvinceTreeNode.getChildren().add(marsGeographyCityGroupTreeNode);
//
//        // 城市
//        SimpleTreeNode marsGeographyCityTreeNode = new SimpleTreeNode();
//        marsGeographyCityTreeNode.setUid(10004L);
//        marsGeographyCityTreeNode.setName("城市");
//        marsGeographyCityTreeNode.setDescription("城市");
//        marsGeographyCityTreeNode.setType("city");
//        marsGeographyCityTreeNode.setLevel(5);
//        marsGeographyCityTreeNode.setChildren(null);
//        marsGeographyCityGroupTreeNode.getChildren().add(marsGeographyCityTreeNode);
//
//        return rootTreeNode;
    }
}
