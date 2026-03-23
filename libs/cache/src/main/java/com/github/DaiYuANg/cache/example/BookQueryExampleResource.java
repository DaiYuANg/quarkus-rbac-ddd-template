package com.github.DaiYuANg.cache.example;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Example REST resource for the Qute + Infinispan dynamic query feature.
 * Demonstrates how to use the template-based search.
 */
@Path("/example/books")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
public class BookQueryExampleResource {

    private final BookSearchService bookSearchService;

    @Inject
    public BookQueryExampleResource(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }

    /**
     * Dynamic search - all query params are optional.
     * Example: GET /example/books/search?keyword=java&minYear=2020
     */
    @GET
    @Path("/search")
    public List<Book> search(
            @QueryParam("keyword") String keyword,
            @QueryParam("author") String author,
            @QueryParam("status") String status,
            @QueryParam("minYear") Integer minYear,
            @QueryParam("maxYear") Integer maxYear,
            @QueryParam("offset") int offset,
            @QueryParam("limit") int limit) {
        var params = BookSearchParams.builder()
                .keyword(keyword)
                .author(author)
                .status(status)
                .minYear(minYear)
                .maxYear(maxYear)
                .offset(Math.max(0, offset))
                .limit(limit <= 0 ? 20 : Math.min(limit, 100))
                .build();
        return bookSearchService.search(params);
    }

    /**
     * Debug endpoint: render the Ickle query that would be executed.
     */
    @GET
    @Path("/search/render")
    @Produces(MediaType.TEXT_PLAIN)
    public String renderQuery(
            @QueryParam("keyword") String keyword,
            @QueryParam("author") String author,
            @QueryParam("status") String status,
            @QueryParam("minYear") Integer minYear,
            @QueryParam("maxYear") Integer maxYear) {
        var params = BookSearchParams.builder()
                .keyword(keyword)
                .author(author)
                .status(status)
                .minYear(minYear)
                .maxYear(maxYear)
                .offset(0)
                .limit(20)
                .build();
        return bookSearchService.renderQuery(params);
    }

    /**
     * Bootstrap: add sample books for testing.
     */
    @POST
    @Path("/bootstrap")
    public String bootstrap() {
        bookSearchService.put("978-1617292545", new Book(
                "978-1617292545",
                "Quarkus in Action",
                "Build cloud-native Java applications",
                "Katia Pujol",
                2024,
                "PUBLISHED"));
        bookSearchService.put("978-1617295942", new Book(
                "978-1617295942",
                "Infinispan Data Grid Platform Definitive Guide",
                "Distributed caching and data grid",
                "Francisco Fernandes",
                2020,
                "PUBLISHED"));
        bookSearchService.put("978-0321356680", new Book(
                "978-0321356680",
                "Effective Java",
                "Best practices for the Java platform",
                "Joshua Bloch",
                2017,
                "PUBLISHED"));
        return "Bootstrap completed. 3 sample books added.";
    }
}
