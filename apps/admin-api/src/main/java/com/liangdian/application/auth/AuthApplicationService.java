package com.liangdian.application.auth;

import com.liangdian.api.dto.request.LoginRequest;
import com.liangdian.api.dto.response.SystemAuthenticationToken;
import com.liangdian.api.dto.response.UserDetailVo;
import com.liangdian.application.audit.LoginLogService;
import com.liangdian.application.converter.ViewMapper;
import com.liangdian.common.constant.ResultCode;
import com.liangdian.common.exception.BizException;
import com.liangdian.identity.repository.UserRepository;
import com.liangdian.redis.AuthorityVersionStore;
import com.liangdian.redis.LoginAttemptStore;
import com.liangdian.security.AdminAuthenticationLifecycle;
import com.liangdian.security.AdminTokenIssuer;
import com.liangdian.security.AuthSecurityConfig;
import com.liangdian.security.LoginAuthenticationManager;
import com.liangdian.security.RefreshTokenAuthenticationRequest;
import com.liangdian.security.UsernamePasswordAuthenticationRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.LinkedHashSet;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthApplicationService {
    private final UserRepository userRepository;
    private final LoginAuthenticationManager authenticationManager;
    private final AdminTokenIssuer tokenIssuer;
    private final LoginAttemptStore loginAttemptStore;
    private final AuthorityVersionStore authorityVersionStore;
    private final ViewMapper viewMapper;
    private final LoginLogService loginLogService;
    private final AuthSecurityConfig authSecurityConfig;
    private final AdminAuthenticationLifecycle authenticationLifecycle;

    @Transactional
    public SystemAuthenticationToken login(LoginRequest req) {
        var username = req.username() == null ? "" : req.username().trim();
        if (loginAttemptStore.isLocked(username)) {
            loginLogService.recordFailure(username, ResultCode.ACCOUNT_TEMPORARILY_LOCKED.message());
            throw new BizException(ResultCode.ACCOUNT_TEMPORARILY_LOCKED);
        }
        try {
            var result = authenticationManager.authenticate(new UsernamePasswordAuthenticationRequest(username, req.password()));
            authenticationLifecycle.publishSnapshot(result.user());
            loginAttemptStore.clear(username);
            loginLogService.recordSuccess(username);
            return tokenIssuer.issue(result.user());
        } catch (BizException ex) {
            onLoginFailure(username, ex.getMessage());
            throw ex;
        }
    }

    public UserDetailVo profile(String username) {
        var dbUser = userRepository.findByUsername(username).orElse(null);
        if (dbUser != null) {
            var detail = viewMapper.toUserDetail(dbUser);
            return new UserDetailVo(detail.userid(), detail.username(), detail.nickname(), detail.permissions(), detail.roleCodes(), composeAuthorityVersion(detail.permissions(), detail.roleCodes()));
        }
        return new UserDetailVo(null, username, username, new LinkedHashSet<>(), new LinkedHashSet<>(), composeAuthorityVersion(new LinkedHashSet<>(), new LinkedHashSet<>()));
    }

    public void logout(String refreshToken) {
        authenticationLifecycle.revokeRefreshToken(refreshToken);
    }

    public SystemAuthenticationToken refreshToken(String refreshToken) {
        var result = authenticationManager.authenticate(new RefreshTokenAuthenticationRequest(refreshToken));
        authenticationLifecycle.publishSnapshot(result.user());
        return tokenIssuer.issue(result.user());
    }

    public String checkAuthorityVersion(String username) {
        var user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return authorityVersionStore.currentVersion() + ":config";
        }
        return composeAuthorityVersion(viewMapper.permissionIdentifiers(user), viewMapper.roleCodes(user));
    }

    private void onLoginFailure(String username, String reason) {
        var attempts = loginAttemptStore.incrementFailure(username, Duration.ofSeconds(authSecurityConfig.loginFailureLockSeconds()));
        if (attempts >= authSecurityConfig.loginFailureMaxAttempts()) {
            loginAttemptStore.lock(username, Duration.ofSeconds(authSecurityConfig.loginFailureLockSeconds()));
        }
        loginLogService.recordFailure(username, reason);
    }

    private String composeAuthorityVersion(java.util.Set<String> permissions, java.util.Set<String> roleCodes) {
        return authorityVersionStore.currentVersion() + ":" + UserDetailVo.encodeAuthorityKey(permissions, roleCodes);
    }
}
