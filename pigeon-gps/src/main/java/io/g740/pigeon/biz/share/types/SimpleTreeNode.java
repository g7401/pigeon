package io.g740.pigeon.biz.share.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bbottong
 */
@Data
@JsonIgnoreProperties(value = {"uid_code"})
public class SimpleTreeNode {
    public static final String ROOT = "ROOT";
    public static final String ROOT_TYPE = "ROOT";
    public static final Long ROOT_UID = -1L;
    public static final Integer ROOT_LEVEL = 0;

    /**
     * 直接使用Long型uid，再服务输出转换成json时存在混乱uid的bug，因此改成String型uid
     */
    private String uid;
    private Long uidCode;
    private String name;
    private String description;
    private String type;
    private Integer level;
    private List<SimpleTreeNode> children;

    public static SimpleTreeNode buildRootTreeNode() {
        SimpleTreeNode rootTreeNode = new SimpleTreeNode();
        rootTreeNode.setUidCode(ROOT_UID);
        rootTreeNode.setName(ROOT);
        rootTreeNode.setDescription(ROOT);
        rootTreeNode.setType(ROOT_TYPE);
        rootTreeNode.setLevel(ROOT_LEVEL);
        rootTreeNode.setChildren(new ArrayList<>());

        return rootTreeNode;
    }

    public void setUidCode(Long uidCode) {
        this.uidCode = uidCode;
        this.uid = String.valueOf(uidCode);
    }

    public void setUid(String uid) {
        this.uid = uid;
        this.uidCode = Long.valueOf(uid);
    }
}




