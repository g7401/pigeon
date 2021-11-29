package io.g740.pigeon.biz.share.constants;

/**
 * @author bbottong
 * <p>
 * 表单元素的类型
 */

public enum FormElementTypeEnum {
    /**
     * 全模糊文本
     */
    FULL_FUZZY_TEXT,
    /**
     * 左模糊文本
     */
    LEFT_FUZZY_TEXT,
    /**
     * 右模糊文本
     */
    RIGHT_FUZZY_TEXT,
    /**
     * 精确文本
     */
    EXACT_TEXT,
    /**
     * 数字范围
     */
    NUMBER_RANGE,
    /**
     * 单选下拉列表
     */
    DROP_DOWN_LIST_SINGLE,
    /**
     * 多选下拉列表
     */
    DROP_DOWN_LIST_MULTIPLE,
    /**
     * 单选联想输入
     */
    ASSOCIATING_SINGLE,
    /**
     * 多选联想输入
     */
    ASSOCIATING_MULTIPLE,
    /**
     * 单日期 (yyyy-MM-dd)
     */
    DATE_SINGLE,
    /**
     * 日期范围 (yyyy-MM-dd to yyyy-MM-dd)
     */
    DATE_RANGE,
    /**
     * 单时间 (HH:mm:ss)
     */
    TIME_SINGLE,
    /**
     * 时间范围 (HH:mm:ss to HH:mm:ss)
     */
    TIME_RANGE,
    /**
     * 单时间戳 (yyyy-MM-dd HH:mm:ss)
     */
    TIMESTAMP_SINGLE,
    /**
     * 时间戳范围 (yyyy-MM-dd HH:mm:ss to yyyy-MM-dd HH:mm:ss)
     */
    TIMESTAMP_RANGE,
    /**
     * 级联 (单选)
     */
    CASCADER,
    /**
     * 级联 (多选）
     */
    CASCADER_MULTIPLE
}
