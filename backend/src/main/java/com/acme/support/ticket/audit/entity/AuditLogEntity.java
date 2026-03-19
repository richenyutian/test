package com.acme.support.ticket.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审计日志实体。
 */
@Data
@TableName("sys_audit_log")
public class AuditLogEntity {

    @TableId(type = IdType.AUTO)
    private Long auditLogId;
    private Long operatorUserId;
    private String operatorName;
    private String moduleName;
    private String operationType;
    private Long businessId;
    private String operationDescription;
    private String requestPath;
    private String requestIp;
    private LocalDateTime createdAt;
}
