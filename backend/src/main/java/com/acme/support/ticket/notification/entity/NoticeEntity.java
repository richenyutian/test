package com.acme.support.ticket.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内消息实体。
 */
@Data
@TableName("sys_notice")
public class NoticeEntity {

    @TableId(type = IdType.AUTO)
    private Long noticeId;
    private Long receiverUserId;
    private String noticeTitle;
    private String noticeContent;
    private String noticeType;
    private String businessType;
    private Long businessId;
    private Integer readFlag;
    private LocalDateTime readTime;
    private LocalDateTime createdAt;
    private Integer deletedFlag;
}
