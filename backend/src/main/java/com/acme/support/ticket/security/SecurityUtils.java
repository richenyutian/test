package com.acme.support.ticket.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUserPrincipal getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUserPrincipal principal)) {
            return null;
        }

        return principal;
    }
}
