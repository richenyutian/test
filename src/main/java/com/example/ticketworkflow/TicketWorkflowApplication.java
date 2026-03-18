package com.example.ticketworkflow;

import com.example.ticketworkflow.config.DemoUserProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@MapperScan("com.example.ticketworkflow.mapper")
@EnableConfigurationProperties(DemoUserProperties.class)
@SpringBootApplication
public class TicketWorkflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketWorkflowApplication.class, args);
	}

}
