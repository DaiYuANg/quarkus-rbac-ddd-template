package com.github.DaiYuANg.cache.example;

import io.quarkus.infinispan.client.Remote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.api.query.Query;
import org.infinispan.commons.api.query.QueryResult;

/**
 * Example service demonstrating Qute-templated dynamic Ickle queries against Infinispan.
 * Similar to MyBatis dynamic SQL: the Qute template conditionally builds WHERE clauses
 * based on which search parameters are present.
 */
@ApplicationScoped
public class BookSearchService {

    private static final String CACHE_NAME = "example-books";
    private static final String TEMPLATE_NAME = "queries/book-search.txt";

    private final io.quarkus.qute.Template searchTemplate;
    private final RemoteCache<String, Book> bookCache;

    @Inject
    public BookSearchService(
            io.quarkus.qute.Engine quteEngine,
            @Remote(CACHE_NAME) RemoteCache<String, Book> bookCache) {
        this.searchTemplate = quteEngine.getTemplate(TEMPLATE_NAME);
        this.bookCache = bookCache;
    }

    /**
     * Execute a dynamic search based on the provided parameters.
     * The Qute template renders the Ickle query with only the WHERE conditions
     * for non-null/non-empty parameters.
     */
    public List<Book> search(BookSearchParams params) {
        String ickle = renderQuery(params);
        Query<Book> query = bookCache.query(ickle);

        bindParameters(query, params);
        query.startOffset(params.offset());
        query.maxResults(params.limit());

        QueryResult<Book> result = query.execute();
        return result.list();
    }

    /**
     * Renders the Ickle query string from the Qute template.
     * Useful for testing and debugging.
     */
    public String renderQuery(BookSearchParams params) {
        return searchTemplate
                .data("keyword", params.keyword())
                .data("author", params.author())
                .data("status", params.status())
                .data("minYear", params.minYear())
                .data("maxYear", params.maxYear())
                .render();
    }

    private void bindParameters(Query<Book> query, BookSearchParams params) {
        if (params.keyword() != null && !params.keyword().isBlank()) {
            query.setParameter("keyword", "%" + params.keyword().trim() + "%");
        }
        if (params.author() != null && !params.author().isBlank()) {
            query.setParameter("author", params.author().trim());
        }
        if (params.status() != null && !params.status().isBlank()) {
            query.setParameter("status", params.status().trim());
        }
        if (params.minYear() != null) {
            query.setParameter("minYear", params.minYear());
        }
        if (params.maxYear() != null) {
            query.setParameter("maxYear", params.maxYear());
        }
    }

    /** Store a book in the cache (for example/bootstrap). */
    public void put(String isbn, Book book) {
        bookCache.put(isbn, book);
    }

    /** Get a book by ISBN. */
    public Book get(String isbn) {
        return bookCache.get(isbn);
    }
}
