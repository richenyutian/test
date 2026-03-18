package com.example.ticketsystem.controller;

import com.example.ticketsystem.api.ApiModels;
import com.example.ticketsystem.common.ApiResponse;
import com.example.ticketsystem.service.DefinitionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    private final DefinitionService definitionService;

    public NodeController(DefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @GetMapping
    public ApiResponse<List<ApiModels.NodeView>> list() {
        return ApiResponse.ok(definitionService.listNodes());
    }

    @PostMapping
    public ApiResponse<ApiModels.NodeView> save(@Valid @RequestBody ApiModels.NodeSaveRequest request) {
        return ApiResponse.ok(definitionService.saveNode(request));
    }
}
