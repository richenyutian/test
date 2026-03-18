package com.example.ticketworkflow.controller;

import com.example.ticketworkflow.common.ApiResponse;
import com.example.ticketworkflow.config.DemoUserProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final DemoUserProperties demoUserProperties;

    public MetaController(DemoUserProperties demoUserProperties) {
        this.demoUserProperties = demoUserProperties;
    }

    @GetMapping("/users")
    public ApiResponse<List<String>> users() {
        return ApiResponse.ok(demoUserProperties.getDemoUsers());
    }

    @GetMapping("/version")
    public ApiResponse<Map<String, String>> version() {
        return ApiResponse.ok(Map.of(
            "java", "17",
            "springBoot", "3.4.4",
            "mybatis", "3.0.5",
            "flowable", "7.2.0",
            "database", "PostgreSQL/H2(local)"
        ));
    }
}
