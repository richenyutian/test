package com.example.ticketsystem.controller;

import com.example.ticketsystem.api.ApiModels;
import com.example.ticketsystem.common.ApiResponse;
import com.example.ticketsystem.service.TicketRuntimeService;
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
public class TicketController {

    private final TicketRuntimeService ticketRuntimeService;

    public TicketController(TicketRuntimeService ticketRuntimeService) {
        this.ticketRuntimeService = ticketRuntimeService;
    }

    @PostMapping("/tickets/start")
    public ApiResponse<ApiModels.TicketDetailView> start(@Valid @RequestBody ApiModels.StartTicketRequest request) {
        return ApiResponse.ok(ticketRuntimeService.startTicket(request));
    }

    @GetMapping("/tickets")
    public ApiResponse<List<ApiModels.TicketView>> listTickets(@RequestParam(required = false) String keyword,
                                                               @RequestParam(required = false) String status) {
        return ApiResponse.ok(ticketRuntimeService.listTickets(keyword, status));
    }

    @GetMapping("/tickets/{id}")
    public ApiResponse<ApiModels.TicketDetailView> detail(@PathVariable Long id) {
        return ApiResponse.ok(ticketRuntimeService.getTicketDetail(id));
    }

    @GetMapping("/tasks")
    public ApiResponse<List<ApiModels.TaskView>> tasks(@RequestParam String assignee) {
        return ApiResponse.ok(ticketRuntimeService.listTasks(assignee));
    }

    @PostMapping("/tasks/{taskId}/complete")
    public ApiResponse<ApiModels.TicketDetailView> complete(@PathVariable Long taskId,
                                                            @Valid @RequestBody ApiModels.CompleteTaskRequest request) {
        return ApiResponse.ok(ticketRuntimeService.completeTask(taskId, request));
    }
}
