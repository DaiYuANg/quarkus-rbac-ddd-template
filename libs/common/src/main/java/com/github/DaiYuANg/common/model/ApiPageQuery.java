package com.github.DaiYuANg.common.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.QueryParam;
import lombok.Setter;
import org.toolkit4j.data.model.page.PageRequest;

/**
 * Pagination/query contract shared by admin list endpoints.
 *
 * <p>Supports both the legacy Spring-style {@code pageNum/pageSize} parameters and the common
 * zero-based {@code page/size} alias so the migrated Quarkus endpoints can keep serving the
 * existing frontend while still offering Panache-friendly accessors.
 */
public class ApiPageQuery extends PageRequest {
  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 10;
  private static final int MAX_SIZE = 200;

  @QueryParam("pageNum")
  @Min(1)
  private Integer pageNum;

  @QueryParam("pageSize")
  @Min(1)
  @Max(200)
  private Integer pageSize;

  @QueryParam("page")
  @Min(0)
  private Integer page;

  @QueryParam("size")
  @Min(1)
  @Max(200)
  private Integer size;

  @Setter
  @QueryParam("keyword")
  private String keyword;

  @Setter
  @QueryParam("sortBy")
  private String sortBy;

  @Setter
  @QueryParam("sortDirection")
  private String sortDirection;

  public Integer getPageNum() {
    if (pageNum != null) {
      return Math.max(pageNum, 1);
    }
    if (page != null) {
      return page + 1;
    }
    var inherited = super.getPage();
    if (inherited != null) {
      return Math.max(inherited, DEFAULT_PAGE);
    }
    return DEFAULT_PAGE;
  }

  public void setPageNum(Integer pageNum) {
    this.pageNum = pageNum == null ? null : Math.max(pageNum, DEFAULT_PAGE);
    super.setPage(this.pageNum);
  }

  public Integer getPageSize() {
    if (pageSize != null) {
      return clampSize(pageSize);
    }
    if (size != null) {
      return clampSize(size);
    }
    var inherited = super.getSize();
    return clampSize(inherited == null ? DEFAULT_SIZE : inherited);
  }

  public void setPageSize(Integer pageSize) {
    this.pageSize = pageSize == null ? null : clampSize(pageSize);
    super.setSize(this.pageSize);
  }

  @Override
  public Integer getPage() {
    if (page != null) {
      return Math.max(page, 0) + 1;
    }
    return getPageNum();
  }

  @Override
  public void setPage(Integer page) {
    this.page = page == null ? null : Math.max(page, 0);
    super.setPage(this.page == null ? null : this.page + 1);
  }

  @Override
  public Integer getSize() {
    return getPageSize();
  }

  @Override
  public void setSize(Integer size) {
    this.size = size == null ? null : clampSize(size);
    super.setSize(this.size);
  }

  public String getKeyword() {
    if (keyword == null) {
      return null;
    }
    var trimmed = keyword.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  public String getSortBy() {
    if (sortBy == null) {
      return null;
    }
    var trimmed = sortBy.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  public String getSortDirection() {
    if (sortDirection == null) {
      return null;
    }
    var trimmed = sortDirection.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  @Override
  public Integer getOffset() {
    return (getPageNum() - 1) * getPageSize();
  }

  public int offset() {
    return getOffset();
  }

  /** Zero-based page index for repositories that still expect it explicitly. */
  public int pageIndex() {
    return Math.max(getPageNum() - 1, 0);
  }

  private int clampSize(int candidate) {
    return Math.clamp(candidate, 1, MAX_SIZE);
  }
}
