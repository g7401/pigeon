package io.g740.commons.constants;

/**
 * 异步工作状态
 *
 * @author bbottong
 */
public enum AsyncJobStatusEnum {
    /**
     * 未开始：已创建工作，未开始执行
     */
    WAITING,
    /**
     * 进行中：正在进行工作
     */
    RUNNING,
    /**
     * 已完成
     */
    DONE,
    /**
     * 已失败，可以查看失败原因
     */
    FAILED,
    /**
     * 已取消：在WAITING状态可以取消
     */
    CANCELED,
    /**
     * 已暂停：在RUNNING状态可以暂停，在"已暂停"状态可以通过resume操作恢复到WAITING状态
     */
    SUSPENDED
}
