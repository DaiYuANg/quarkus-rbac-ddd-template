package com.github.DaiYuANg.cache.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates the example-books cache with indexing at application startup.
 * Required for Ickle queries to work on the Book entity.
 */
@ApplicationScoped
public class ExampleBooksCacheInitializer {

    private static final Logger log = LoggerFactory.getLogger(ExampleBooksCacheInitializer.class);
    private static final String CACHE_NAME = "example-books";

    @Inject
    RemoteCacheManager cacheManager;

    void onStart(@Observes jakarta.enterprise.inject.spi.AfterDeploymentValidation event) {
        try {
            String xml =
                    """
                    <distributed-cache name="%s">
                      <encoding media-type="application/x-protostream"/>
                      <indexing storage="local-heap">
                        <indexed-entities>
                          <indexed-entity>example.query.Book</indexed-entity>
                        </indexed-entities>
                      </indexing>
                    </distributed-cache>
                    """.formatted(CACHE_NAME);
            cacheManager.administration().createCache(CACHE_NAME, xml);
            log.info("Created indexed cache '{}' for Book queries", CACHE_NAME);
        } catch (Exception e) {
            log.debug("example-books cache creation skipped (may already exist): {}", e.getMessage());
        }
    }
}
