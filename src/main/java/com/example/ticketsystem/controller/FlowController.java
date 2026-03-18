package com.example.ticketsystem.controller;

import com.example.ticketsystem.api.ApiModels;
import com.example.ticketsystem.common.ApiResponse;
import com.example.ticketsystem.service.DefinitionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flows")
public class FlowController {

    private final DefinitionService definitionService;

    public FlowController(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @GetMapping
    public ApiResponse<List<ApiModels.FlowView>> list() {
        return ApiResponse.ok(definitionService.listFlows());
    }

    @GetMapping("/{id}")
    public ApiResponse<ApiModels.FlowView> detail(@PathVariable Long id) {
        return ApiResponse.ok(definitionService.getFlow(id));
    }

    @PostMapping
    public ApiResponse<ApiModels.FlowView> save(@Valid @RequestBody ApiModels.FlowSaveRequest request) {
        return ApiResponse.ok(definitionService.saveFlow(request));
    }
}
