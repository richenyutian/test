package com.acme.support.ticket.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工单附件实体。
 */
@Data
@TableName("ticket_attachment")
public class TicketAttachmentEntity {

    @TableId(type = IdType.AUTO)
    private Long attachmentId;
    private Long ticketId;
    private String originalFileName;
    private String storageFileName;
    private String storagePath;
    private String fileExtension;
    private Long fileSize;
    private Long uploaderUserId;
    private String uploaderName;
    private LocalDateTime createdAt;
    private Integer deletedFlag;
}
