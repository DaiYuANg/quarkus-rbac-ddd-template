package com.github.DaiYuANg.api.dto.response;

import com.github.DaiYuANg.identity.constant.UserStatus;
import java.time.Instant;
import java.util.Set;

public record UserVO(Long id, String username, String identifier, String mobilePhone, String nickname, String email, Instant latestSignIn, Instant createAt, Instant updateAt, UserStatus userStatus, Set<RoleVO> roles) {}
