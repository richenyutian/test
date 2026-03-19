package com.acme.support.ticket.security;

import java.io.Serializable;
import java.util.List;

/**
 * 登录用户主体对象。
 */
public record LoginUserPrincipal(
        Long userId,
        String username,
        List<String> roles,
        List<String> permissions
) implements Serializable {
}
