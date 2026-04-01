package com.github.DaiYuANg.modules.accesscontrol.query;

import com.github.DaiYuANg.common.model.PageReq;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

@Getter
@Setter
abstract class AbstractAdminPageQueryParams<Q extends PageReq> {
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

  @QueryParam("keyword")
  private String keyword;

  @QueryParam("sortBy")
  private String sortBy;

  @QueryParam("sortDirection")
  private String sortDirection;

  protected Q applyTo(@NonNull Q query) {
    query.setPage(pageNum);
    query.setSize(pageSize);
    query.setPage(page);
    query.setSize(size);
    query.setKeyword(keyword);
    query.setSortBy(sortBy);
    query.setSortDirection(sortDirection);
    return query;
  }
}
