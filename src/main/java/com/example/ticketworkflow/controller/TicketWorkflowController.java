package com.example.ticketworkflow.controller;

import com.example.ticketworkflow.api.ApiModels;
import com.example.ticketworkflow.common.ApiResponse;
import com.example.ticketworkflow.service.TicketWorkflowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketWorkflowController {

    private final TicketWorkflowService ticketWorkflowService;

    public TicketWorkflowController(TicketWorkflowService ticketWorkflowService) {
        this.ticketWorkflowService = ticketWorkflowService;
    }

    @PostMapping("/instances/start")
    public ApiResponse<ApiModels.TicketInstanceDetailView> start(@Valid @RequestBody ApiModels.StartProcessRequest request) {
        return ApiResponse.ok(ticketWorkflowService.start(request));
    }

    @GetMapping("/instances")
    public ApiResponse<List<ApiModels.TicketInstanceView>> instances(@RequestParam(required = false) String keyword,
                                                                    @RequestParam(required = false) String initiator,
                                                                    @RequestParam(required = false) String status,
                                                                    @RequestParam(required = false) String fieldKey,
                                                                    @RequestParam(required = false) String fieldValue) {
        return ApiResponse.ok(ticketWorkflowService.searchInstances(keyword, initiator, status, fieldKey, fieldValue));
    }

    @GetMapping("/instances/{id}")
    public ApiResponse<ApiModels.TicketInstanceDetailView> instanceDetail(@PathVariable Long id) {
        return ApiResponse.ok(ticketWorkflowService.getInstanceDetail(id));
    }

    @GetMapping("/tasks")
    public ApiResponse<List<ApiModels.TaskView>> tasks(@RequestParam String assignee) {
        return ApiResponse.ok(ticketWorkflowService.listTasks(assignee));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ApiResponse<ApiModels.TicketInstanceDetailView> complete(@PathVariable String taskId,
                                                                    @Valid @RequestBody ApiModels.CompleteTaskRequest request) {
        return ApiResponse.ok(ticketWorkflowService.completeTask(taskId, request));
    }
}
