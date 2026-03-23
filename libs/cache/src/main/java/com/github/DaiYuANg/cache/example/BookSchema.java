package com.github.DaiYuANg.cache.example;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(
    includeClasses = Book.class,
    schemaPackageName = "example.query",
    schemaFileName = "book.proto")
public interface BookSchema extends GeneratedSchema {}
