package com.acme.support.ticket.notification.controller;

import com.acme.support.ticket.common.api.ApiResponse;
import com.acme.support.ticket.notification.dto.NotificationResponses;
import com.acme.support.ticket.notification.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 通知中心接口。
 */
@Tag(name = "通知中心")
@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final NoticeService noticeService;

    public NotificationController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Operation(summary = "获取通知中心数据")
    @GetMapping
    @PreAuthorize("hasAuthority('notice:view')")
    public ApiResponse<NotificationResponses.NotificationCenterResponse> getNotifications() {
        return ApiResponse.success(noticeService.listMine());
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/{noticeId}/read")
    @PreAuthorize("hasAuthority('notice:view')")
    public ApiResponse<Void> markRead(@PathVariable Long noticeId) {
        noticeService.markRead(noticeId);
        return ApiResponse.success("已读成功", null);
    }
}
