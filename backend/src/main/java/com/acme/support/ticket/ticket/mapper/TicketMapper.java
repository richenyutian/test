package com.acme.support.ticket.ticket.mapper;

import com.acme.support.ticket.ticket.entity.TicketEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单主表 Mapper。
 */
@Mapper
public interface TicketMapper extends BaseMapper<TicketEntity> {
}
