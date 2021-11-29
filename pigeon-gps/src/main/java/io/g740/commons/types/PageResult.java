package io.g740.commons.types;

import java.util.List;

/**
 * @author bbottong
 *
 * 分页查询结果
 */
public class PageResult<T> {
	/**
	 * 请求的页序号
	 */
	private int pageNumber;

	/**
	 * 请求的页大小
	 */
	private int pageSize;

	/**
	 * 响应的本页元素数量
	 */
	private int numberOfElements;

	/**
	 * 响应的本页元素列表
	 */
	private List<T> content;

	/**
	 * 响应的总页数
	 */
	private int totalPages;

	/**
	 * 响应的总元素数量
	 */
	private Long totalElements;

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getNumberOfElements() {
		return numberOfElements;
	}

	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	public List<T> getContent() {
		return content;
	}

	public void setContent(List<T> content) {
		this.content = content;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public Long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(Long totalElements) {
		this.totalElements = totalElements;
	}

	public static PageResult<? extends Object> buildEmpty(int pageNumber, int pageSize) {
		PageResult pageResult = new PageResult<>();
		pageResult.setContent(null);
		pageResult.setPageNumber(pageNumber);
		pageResult.setPageSize(pageSize);
		pageResult.setTotalPages(0);
		pageResult.setTotalElements(0L);
		pageResult.setNumberOfElements(0);
		return pageResult;
	}
}
