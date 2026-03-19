package com.acme.support.ticket.notification.dto;

import java.time.LocalDateTime;
import java.util.List;

public final class NotificationResponses {

    private NotificationResponses() {
    }

    public record NotificationMessageResponse(
            String messageId,
            String title,
            String channel,
            String receiverName,
            String content,
            boolean read,
            LocalDateTime sentAt
    ) {
    }

    public record NotificationCenterResponse(
            long unreadCount,
            List<NotificationMessageResponse> messages
    ) {
    }
}
