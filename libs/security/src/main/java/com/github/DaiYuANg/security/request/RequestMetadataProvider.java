package com.github.DaiYuANg.security.request;

import java.util.Optional;

public interface RequestMetadataProvider {
  Optional<RequestMetadata> currentRequest();
}
