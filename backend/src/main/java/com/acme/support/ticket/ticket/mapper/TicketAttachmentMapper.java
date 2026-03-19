package com.acme.support.ticket.ticket.mapper;

import com.acme.support.ticket.ticket.entity.TicketAttachmentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单附件 Mapper。
 */
@Mapper
public interface TicketAttachmentMapper extends BaseMapper<TicketAttachmentEntity> {
}
