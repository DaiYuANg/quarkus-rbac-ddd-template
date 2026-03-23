package com.github.DaiYuANg.cache.example;

import java.util.Objects;

/**
 * Search parameters for the dynamic Book query.
 * Used as data object for Qute template rendering.
 */
public record BookSearchParams(
    String keyword,
    String author,
    String status,
    Integer minYear,
    Integer maxYear,
    int offset,
    int limit
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String keyword;
        private String author;
        private String status;
        private Integer minYear;
        private Integer maxYear;
        private int offset;
        private int limit = 20;

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder minYear(Integer minYear) {
            this.minYear = minYear;
            return this;
        }

        public Builder maxYear(Integer maxYear) {
            this.maxYear = maxYear;
            return this;
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public BookSearchParams build() {
            return new BookSearchParams(keyword, author, status, minYear, maxYear, offset, limit);
        }
    }

    public String keywordLike() {
        return keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookSearchParams that = (BookSearchParams) o;
        return offset == that.offset
            && limit == that.limit
            && Objects.equals(keyword, that.keyword)
            && Objects.equals(author, that.author)
            && Objects.equals(status, that.status)
            && Objects.equals(minYear, that.minYear)
            && Objects.equals(maxYear, that.maxYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword, author, status, minYear, maxYear, offset, limit);
    }
}
