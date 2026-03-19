package com.acme.support.ticket.ticket.mapper;

import com.acme.support.ticket.ticket.entity.TicketFlowRecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工单流转记录 Mapper。
 */
@Mapper
public interface TicketFlowRecordMapper extends BaseMapper<TicketFlowRecordEntity> {
}
