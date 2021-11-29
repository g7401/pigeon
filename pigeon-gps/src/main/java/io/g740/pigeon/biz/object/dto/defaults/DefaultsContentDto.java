package io.g740.pigeon.biz.object.dto.defaults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
@JsonIgnoreProperties(value = {"uid_code", "defaults_category_uid_code"})
public class DefaultsContentDto {
    /**
     * 直接使用Long型uid，再服务输出转换成json时存在混乱uid的bug，因此改成String型uid
     */
    private String uid;
    private Long uidCode;
    private String value;
    private String label;

    /**
     * 指定默认值类目的UID
     */
    private String defaultsCategoryUid;
    private Long defaultsCategoryUidCode;

    public void setUidCode(Long uidCode) {
        this.uidCode = uidCode;
        this.uid = String.valueOf(uidCode);
    }

    public void setUid(String uid) {
        this.uid = uid;
        this.uidCode = Long.valueOf(uid);
    }

    public void setDefaultsCategoryUidCode(Long defaultsCategoryUidCode) {
        this.defaultsCategoryUidCode = defaultsCategoryUidCode;
        this.defaultsCategoryUid = String.valueOf(defaultsCategoryUidCode);
    }

    public void setDefaultsCategoryUid(String defaultsCategoryUid) {
        this.defaultsCategoryUid = defaultsCategoryUid;
        this.defaultsCategoryUidCode = Long.valueOf(defaultsCategoryUid);
    }
}
