package com.github.DaiYuANg.modules.accesscontrol.query;

import com.github.DaiYuANg.common.model.PageReq;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
abstract class AbstractAdminPageQueryParams<Q extends PageReq> {
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
    if (page != null) {
      query.setPage(page + 1);
    }
    if (size != null) {
      query.setSize(size);
    }
    query.setKeyword(normalize(keyword));
    query.setSortBy(normalize(sortBy));
    query.setSortDirection(normalize(sortDirection));
    return query;
  }

  private String normalize(String value) {
    return StringUtils.trimToNull(value);
  }
}
