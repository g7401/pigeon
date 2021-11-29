package io.g740.pigeon.biz.object.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
@JsonIgnoreProperties(value = {"uid_code", "parent_uid_code", "dictionary_structure_uid_code", "dictionary_category_uid_code"})
public class DictionaryContentDto {
    /**
     * 直接使用Long型uid，再服务输出转换成json时存在混乱uid的bug，因此改成String型uid
     */
    private String uid;
    private Long uidCode;
    private String value;
    private String label;

    /**
     * 内容树中上一级节点的UID
     */
    private String parentUid;
    private Long parentUidCode;

    /**
     * 指定字典结构的UID
     */
    private String dictionaryStructureUid;
    private Long dictionaryStructureUidCode;

    /**
     * 指定字典类目的UID
     */
    private String dictionaryCategoryUid;
    private Long dictionaryCategoryUidCode;

    public void setUidCode(Long uidCode) {
        this.uidCode = uidCode;
        this.uid = String.valueOf(uidCode);
    }

    public void setUid(String uid) {
        this.uid = uid;
        this.uidCode = Long.valueOf(uid);
    }

    public void setParentUidCode(Long parentUidCode) {
        this.parentUidCode = parentUidCode;
        this.parentUid = String.valueOf(parentUidCode);
    }

    public void setParentUid(String parentUid) {
        this.parentUid = parentUid;
        this.parentUidCode = Long.valueOf(parentUid);
    }

    public void setDictionaryStructureUidCode(Long dictionaryStructureUidCode) {
        this.dictionaryStructureUidCode = dictionaryStructureUidCode;
        this.dictionaryStructureUid = String.valueOf(dictionaryStructureUidCode);
    }

    public void setDictionaryStructureUid(String dictionaryStructureUid) {
        this.dictionaryStructureUid = dictionaryStructureUid;
        this.dictionaryStructureUidCode = Long.valueOf(dictionaryStructureUid);
    }

    public void setDictionaryCategoryUidCode(Long dictionaryCategoryUidCode) {
        this.dictionaryCategoryUidCode = dictionaryCategoryUidCode;
        this.dictionaryCategoryUid = String.valueOf(dictionaryCategoryUidCode);
    }

    public void setDictionaryCategoryUid(String dictionaryCategoryUid) {
        this.dictionaryCategoryUid = dictionaryCategoryUid;
        this.dictionaryCategoryUidCode = Long.valueOf(dictionaryCategoryUid);
    }
}
