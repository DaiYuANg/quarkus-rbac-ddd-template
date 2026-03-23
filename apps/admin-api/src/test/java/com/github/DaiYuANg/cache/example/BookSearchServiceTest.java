package com.github.DaiYuANg.cache.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

/**
 * Integration test for the Qute + Infinispan dynamic query example (from libs/cache).
 * Requires Infinispan (Dev Services starts it automatically when Docker is available).
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookSearchServiceTest {

    @BeforeAll
    void bootstrap() {
        given().post("/example/books/bootstrap").then().statusCode(200);
    }

    @Test
    @Order(1)
    void renderQuery_withNoParams_returnsBaseQuery() {
        String query = given()
                .queryParam("keyword", "")
                .get("/example/books/search/render")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        Assertions.assertTrue(query.contains("FROM example.query.Book"));
        Assertions.assertTrue(query.contains("WHERE 1=1"));
        Assertions.assertTrue(query.contains("ORDER BY"));
        Assertions.assertFalse(query.contains("AND b.title LIKE"));
    }

    @Test
    @Order(2)
    void renderQuery_withKeyword_addsLikeClause() {
        String query = given()
                .queryParam("keyword", "java")
                .get("/example/books/search/render")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        Assertions.assertTrue(query.contains("b.title LIKE :keyword"));
        Assertions.assertTrue(query.contains("b.description LIKE :keyword"));
    }

    @Test
    @Order(3)
    void renderQuery_withAuthor_addsAuthorClause() {
        String query = given()
                .queryParam("author", "Bloch")
                .get("/example/books/search/render")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        Assertions.assertTrue(query.contains("b.author = :author"));
    }

    @Test
    @Order(4)
    void renderQuery_withYearRange_addsYearClauses() {
        String query = given()
                .queryParam("minYear", 2015)
                .queryParam("maxYear", 2025)
                .get("/example/books/search/render")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        Assertions.assertTrue(query.contains("b.publicationYear >= :minYear"));
        Assertions.assertTrue(query.contains("b.publicationYear <= :maxYear"));
    }

    @Test
    @Order(5)
    void search_withoutParams_returnsAllBooks() {
        given()
                .get("/example/books/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(3))
                .body("isbn", hasItems("978-1617292545", "978-1617295942", "978-0321356680"));
    }

    @Test
    @Order(6)
    void search_withKeyword_filtersResults() {
        given()
                .queryParam("keyword", "Java")
                .get("/example/books/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(7)
    void search_withAuthor_returnsMatchingBooks() {
        given()
                .queryParam("author", "Joshua Bloch")
                .get("/example/books/search")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("author", everyItem(equalTo("Joshua Bloch")))
                .body("title", hasItem("Effective Java"));
    }

    @Test
    @Order(8)
    void search_withYearRange_filtersByPublicationYear() {
        given()
                .queryParam("minYear", 2020)
                .queryParam("maxYear", 2025)
                .get("/example/books/search")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("publicationYear", everyItem(greaterThanOrEqualTo(2020)))
                .body("publicationYear", everyItem(lessThanOrEqualTo(2025)));
    }
}
