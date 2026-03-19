package com.acme.support.ticket.audit.service;

import com.acme.support.ticket.audit.dto.AuditResponses;
import com.acme.support.ticket.audit.entity.AuditLogEntity;
import com.acme.support.ticket.audit.mapper.AuditLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 审计日志服务。
 */
@Service
public class AuditLogService {

    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void save(AuditLogEntity entity) {
        entity.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(entity);
    }

    public AuditResponses.AuditLogPageResponse page(long current, long size) {
        Page<AuditLogEntity> page = auditLogMapper.selectPage(
                new Page<>(current, size),
                new LambdaQueryWrapper<AuditLogEntity>().orderByDesc(AuditLogEntity::getAuditLogId)
        );

        return new AuditResponses.AuditLogPageResponse(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                page.getRecords().stream().map(record -> new AuditResponses.AuditLogResponse(
                        record.getAuditLogId(),
                        record.getOperatorUserId(),
                        record.getModuleName(),
                        record.getOperationType(),
                        record.getOperatorName(),
                        record.getBusinessId(),
                        record.getOperationDescription(),
                        record.getRequestPath(),
                        record.getRequestIp(),
                        record.getCreatedAt()
                )).toList()
        );
    }
}
