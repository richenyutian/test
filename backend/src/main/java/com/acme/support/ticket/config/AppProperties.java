package com.acme.support.ticket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用扩展配置。
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Auth auth,
        File file
) {
    public record Auth(boolean ssoEnabled) {
    }

    public record File(String uploadPath) {
    }
}
