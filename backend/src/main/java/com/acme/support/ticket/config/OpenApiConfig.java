package com.acme.support.ticket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置，方便前后端联调。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ticketOpenApi() {
        return new OpenAPI().info(
                new Info()
                        .title("工单处理系统 API")
                        .version("1.0.0")
                        .description("用于企业级工单受理、分派、处理、SLA、报表与审计的演示接口集合")
        );
    }
}
