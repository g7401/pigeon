package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author bbottong
 */
@Data
public class CascadeQueryParameterDto {
    /**
     * 级联字段组合名称
     */
    private String cascadeCollectionName;
    /**
     * N个组合中每一个组合的各字段取值
     */
    private List<Map<String, String>> collectionValues;
}
