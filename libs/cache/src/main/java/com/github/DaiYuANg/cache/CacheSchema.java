package com.github.DaiYuANg.cache;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(includeClasses = CacheValue.class)
public interface CacheSchema extends GeneratedSchema {}
