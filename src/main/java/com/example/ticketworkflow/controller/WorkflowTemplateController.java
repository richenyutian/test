package com.example.ticketworkflow.controller;

import com.example.ticketworkflow.api.ApiModels;
import com.example.ticketworkflow.common.ApiResponse;
import com.example.ticketworkflow.service.WorkflowTemplateService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class WorkflowTemplateController {

    private final WorkflowTemplateService workflowTemplateService;

    public WorkflowTemplateController(WorkflowTemplateService workflowTemplateService) {
        this.workflowTemplateService = workflowTemplateService;
    }

    @GetMapping
    public ApiResponse<List<ApiModels.WorkflowTemplateView>> list() {
        return ApiResponse.ok(workflowTemplateService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<ApiModels.WorkflowTemplateView> detail(@PathVariable Long id) {
        return ApiResponse.ok(workflowTemplateService.getById(id));
    }

    @PostMapping
    public ApiResponse<ApiModels.WorkflowTemplateView> save(@Valid @RequestBody ApiModels.TemplateSaveRequest request) {
        return ApiResponse.ok(workflowTemplateService.save(request));
    }
}
