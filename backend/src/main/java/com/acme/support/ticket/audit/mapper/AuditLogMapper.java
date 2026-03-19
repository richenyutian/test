package com.acme.support.ticket.audit.mapper;

import com.acme.support.ticket.audit.entity.AuditLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Mapper。
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntity> {
}
