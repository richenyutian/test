package com.acme.support.ticket.notification.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.mock.MockTicketDataService;
import com.acme.support.ticket.notification.dto.NotificationResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知中心接口。
 */
@Tag(name = "通知中心")
@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final MockTicketDataService mockTicketDataService;

    public NotificationController(MockTicketDataService mockTicketDataService) {
        this.mockTicketDataService = mockTicketDataService;
    }

    @Operation(summary = "获取通知中心数据")
    @GetMapping
    public ApiResponse<NotificationResponses.NotificationCenterResponse> getNotifications() {
        return ApiResponse.success(mockTicketDataService.getNotificationCenter());
    }
}
