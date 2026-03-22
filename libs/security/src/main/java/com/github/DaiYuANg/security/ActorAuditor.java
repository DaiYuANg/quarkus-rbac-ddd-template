package com.github.DaiYuANg.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ActorAuditor {
    private final CurrentUserAccess currentUserAccess;

    public String currentActorKey() {
        return currentUserAccess.currentUser()
            .map(user -> user.userType() + ":" + user.username())
            .orElse("SYSTEM:anonymous");
    }
}
