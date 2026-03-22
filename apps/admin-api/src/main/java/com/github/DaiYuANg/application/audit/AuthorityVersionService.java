package com.github.DaiYuANg.application.audit;

import com.github.DaiYuANg.redis.AuthorityVersionStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthorityVersionService {
    private final AuthorityVersionStore authorityVersionStore;

    public String bumpGlobalVersion() {
        return authorityVersionStore.bumpGlobalVersion();
    }
}
