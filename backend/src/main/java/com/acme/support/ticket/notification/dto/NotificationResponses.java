package com.acme.support.ticket.notification.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class NotificationResponses {

    private NotificationResponses() {
    }

    public record NotificationMessageResponse(
            Long noticeId,
            String noticeTitle,
            String noticeContent,
            String noticeType,
            String businessType,
            Long businessId,
            boolean readFlag,
            LocalDateTime readTime,
            LocalDateTime createdAt
    ) {
    }

    public record NotificationCenterResponse(
            long unreadCount,
            List<NotificationMessageResponse> messages
    ) {
    }
}
