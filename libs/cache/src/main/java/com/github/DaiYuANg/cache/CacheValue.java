package com.github.DaiYuANg.cache;

import org.infinispan.protostream.annotations.Proto;

@Proto
public record CacheValue(String data) {}
