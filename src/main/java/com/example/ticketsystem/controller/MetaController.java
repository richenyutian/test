package com.example.ticketsystem.controller;

import com.example.ticketsystem.common.ApiResponse;
import com.example.ticketsystem.config.DemoUserProperties;
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
            "maven", "3.9+",
            "springBoot", "3.4.4",
            "mybatis", "3.0.5",
            "postgresql", "16",
            "frontend", "Vue3 + Element Plus"
        ));
    }
}
