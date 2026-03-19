package com.acme.support.ticket;

import com.acme.support.ticket.config.AppProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 工单处理系统后端启动类。
 * <p>
 * 当前工程采用单体模块化设计，便于在真实项目中逐步拆分：
 * 认证与权限、工单管理、分派中心、SLA、通知、报表、审计均以领域包组织。
 * </p>
 */
@SpringBootApplication
@MapperScan("com.acme.support.ticket.**.mapper")
@EnableConfigurationProperties(AppProperties.class)
@EnableScheduling
public class TicketServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketServiceApplication.class, args);
	}

}
