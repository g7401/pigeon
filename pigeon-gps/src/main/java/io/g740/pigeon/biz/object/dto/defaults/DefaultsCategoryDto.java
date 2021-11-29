package io.g740.pigeon.biz.object.dto.defaults;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author bbottong
 */
@Data
@JsonIgnoreProperties(value = {"uid_code"})
public class DefaultsCategoryDto {
    /**
     * 直接使用Long型uid，再服务输出转换成json时存在混乱uid的bug，因此改成String型uid
     */
    private String uid;
    private Long uidCode;
    private String name;
    private String description;

    public void setUidCode(Long uidCode) {
        this.uidCode = uidCode;
        this.uid = String.valueOf(uidCode);
    }

    public void setUid(String uid) {
        this.uid = uid;
        this.uidCode = Long.valueOf(uid);
    }
}
