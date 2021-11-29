package io.g740.pigeon.biz.object.dto.df;

import lombok.Data;

import java.util.List;

/**
 * @author bbottong
 */
@Data
public class CreateOrReplaceDfDto {
    /**
     * df uid
     */
    private Long uid;

    /**
     * df key
     */
    private String key;

    /**
     * df name
     */
    private String name;

    /**
     * df description
     */
    private String description;

    /**
     * 给待创建DF绑定零个或一个或多个标签，取标签名称
     */
    private List<String> tags;

    /**
     * DF所依存的主要的一个Data Object
     */
    private Long primaryDataObjectUid;

    /**
     * DF所依存的次要的零个或一个或多个Data Object(s)
     */
    private List<Long> secondaryDataObjectUids;

    /**
     * 如果DF依存单个表/视图（只有primary data object），则可以选择是否需要处理（就是transformation）。
     * 或者，
     * 如果DF依存同属一个DS的多个表/视图 (除了primary data object，还有secondary data objects)，
     * 则一定需要处理（也是transformation）。
     */
    private Boolean processingNeeded;

    /**
     * 如果processingNeeded为true，则表示需要对DF所依存的表/视图执行处理（就是transformation）。
     * 处理逻辑当前只支持SQL语句，执行一段SQL语句，以便经过SQL处理得到转换后的数据格式和内容。
     */
    private String processingLogic;
}
