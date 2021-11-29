package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class CreateOrReplaceDfConfBasicDto {
    private List<CreateOrReplaceDfConfDataFieldDto> fields;
}
