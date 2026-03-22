package com.liangdian.security;

import java.util.Optional;

public interface RequestMetadataProvider {
    Optional<RequestMetadata> currentRequest();
}
