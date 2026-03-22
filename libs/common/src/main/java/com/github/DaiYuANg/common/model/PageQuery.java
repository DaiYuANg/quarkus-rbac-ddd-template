package com.github.DaiYuANg.common.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.QueryParam;
import lombok.Setter;

/**
 * Pagination/query contract shared by admin list endpoints.
 *
 * <p>Supports both the legacy Spring-style {@code pageNum/pageSize}
 * parameters and the common zero-based {@code page/size} alias so the
 * migrated Quarkus endpoints can keep serving the existing frontend while
 * still offering Panache-friendly accessors.</p>
 */
public class PageQuery {
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

    public int getPageNum() {
        if (pageNum != null) {
            return Math.max(pageNum, 1);
        }
        if (page != null) {
            return page + 1;
        }
        return 1;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = Math.max(pageNum, 1);
    }

    public int getPageSize() {
        if (pageSize != null) {
            return clampSize(pageSize);
        }
        if (size != null) {
            return clampSize(size);
        }
        return 10;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = clampSize(pageSize);
    }

    /**
     * Zero-based page index for Panache / JPA paging APIs.
     */
    public int getPage() {
        if (page != null) {
            return Math.max(page, 0);
        }
        return Math.max(getPageNum() - 1, 0);
    }

    public void setPage(int page) {
        this.page = Math.max(page, 0);
    }

    public int getSize() {
        return getPageSize();
    }

    public void setSize(int size) {
        this.size = clampSize(size);
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

  public int offset() {
        return getPage() * getPageSize();
    }

    private int clampSize(int candidate) {
        return Math.clamp(candidate, 1, 200);
    }
}
