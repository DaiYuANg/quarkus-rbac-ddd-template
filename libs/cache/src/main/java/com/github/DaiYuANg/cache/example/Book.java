package com.github.DaiYuANg.cache.example;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Text;
import org.infinispan.protostream.annotations.Proto;

/**
 * Example indexed entity for Infinispan Ickle queries.
 * Used to demonstrate Qute + Infinispan dynamic query.
 */
@Proto
@Indexed
public record Book(
    @Basic(projectable = true, sortable = true) String isbn,
    @Text(projectable = true) String title,
    @Text(projectable = true) String description,
    @Basic(projectable = true, sortable = true) String author,
    @Basic(projectable = true, sortable = true) Integer publicationYear,
    @Basic(projectable = true, sortable = true) String status
) {}
