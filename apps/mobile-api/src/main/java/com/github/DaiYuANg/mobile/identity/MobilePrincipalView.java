package com.github.DaiYuANg.mobile.identity;

import java.util.List;

/** 示例：从 JWT 读出 userType，演示多类主体（MEMBER / MERCHANT / 数据库用户等）。 */
public record MobilePrincipalView(String username, String userType, List<String> roles) {}
