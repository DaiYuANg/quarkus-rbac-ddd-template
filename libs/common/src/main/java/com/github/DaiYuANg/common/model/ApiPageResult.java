package com.github.DaiYuANg.common.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import org.toolkit4j.data.model.page.PageResult;

public class ApiPageResult<T> extends PageResult<T> {
  public ApiPageResult() {
    super();
  }

  public ApiPageResult(long total, int pageNum, int pageSize, List<T> records) {
    super(records, pageNum, pageSize, total, totalPages(total, pageSize));
  }

  public static <T> ApiPageResult<T> of(long total, int pageNum, int pageSize, List<T> records) {
    return new ApiPageResult<>(total, pageNum, pageSize, records);
  }

  @JsonProperty("total")
  public long total() {
    return super.getTotalElements() == null ? 0L : super.getTotalElements();
  }

  @JsonProperty("pageNum")
  public int pageNum() {
    return super.getPage() == null ? 1 : super.getPage();
  }

  @JsonProperty("pageSize")
  public int pageSize() {
    return super.getSize() == null ? 0 : super.getSize();
  }

  @JsonProperty("records")
  public List<T> records() {
    var content = super.getContent();
    if (content == null || content.isEmpty()) {
      return List.of();
    }
    return List.copyOf(content);
  }

  @JsonProperty("items")
  public List<T> items() {
    return records();
  }

  @JsonProperty("page")
  public int page() {
    return pageNum();
  }

  public int pageSizeAlias() {
    return pageSize();
  }

  @Override
  @JsonIgnore
  public Collection<T> getContent() {
    return super.getContent();
  }

  @Override
  @JsonIgnore
  public Integer getPage() {
    return super.getPage();
  }

  @Override
  @JsonIgnore
  public Integer getSize() {
    return super.getSize();
  }

  @Override
  @JsonIgnore
  public Long getTotalElements() {
    return super.getTotalElements();
  }

  @Override
  @JsonIgnore
  public Long getTotalPages() {
    return super.getTotalPages();
  }

  private static long totalPages(long total, int pageSize) {
    if (pageSize <= 0 || total <= 0) {
      return 0L;
    }
    return (total + pageSize - 1) / pageSize;
  }
}
