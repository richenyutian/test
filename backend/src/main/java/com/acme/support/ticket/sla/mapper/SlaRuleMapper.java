package com.acme.support.ticket.sla.mapper;

import com.acme.support.ticket.sla.entity.SlaRuleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * SLA 规则 Mapper。
 */
@Mapper
public interface SlaRuleMapper extends BaseMapper<SlaRuleEntity> {
}
