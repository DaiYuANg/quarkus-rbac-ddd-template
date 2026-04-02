package com.github.DaiYuANg.persistence.query;

import com.blazebit.persistence.PagedList;
import java.util.List;
import java.util.function.Function;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.toolkit4j.data.model.page.PageRequest;
import org.toolkit4j.data.model.page.PageResult;

/**
 * Shared adapters for converting read-side pagination results into toolkit4j {@link PageResult}.
 *
 * <p>Keeps Blaze/Panache/custom paging details out of repositories and application services.
 */
public final class PageResults {

  private PageResults() {}

  @Contract("_, _ -> new")
  public static <T> @NonNull PageResult<T> from(
      @NonNull PagedList<T> page, @NonNull PageRequest request) {
    return new PageResult<>(
        page,
        request.getPage(),
        request.getSize(),
        page.getTotalSize(),
        (long) page.getTotalPages());
  }

  @Contract("_, _, _ -> new")
  public static <T> @NonNull PageResult<T> from(
      @NonNull List<T> content, @NonNull PageRequest request, long totalElements) {
    return new PageResult<>(
        content,
        request.getPage(),
        request.getSize(),
        totalElements,
        totalPages(totalElements, request.getSize()));
  }

  @Contract("_, _ -> new")
  public static <S, T> @NonNull PageResult<T> map(
      @NonNull PageResult<S> source, @NonNull Function<S, T> mapper) {
    return source.mapContent(mapper);
  }

  private static long totalPages(long totalElements, @Nullable Integer size) {
    return size == null || size <= 0 ? 0L : (totalElements + size - 1L) / size;
  }
}
