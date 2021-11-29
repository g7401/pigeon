package io.g740.commons.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

/**
 * @author bbottong
 */
@Data
@JsonIgnoreProperties(value = {"id", "deleted"})
public abstract class BaseDto {
    private Long id;
    private Boolean deleted;
    private Date createTimestamp;
    private String createBy;
    private Date lastUpdateTimestamp;
    private String lastUpdateBy;
    private String owner;
}
