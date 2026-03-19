package com.acme.support.ticket.sla.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SLA 规则实体。
 */
@Data
@TableName("ticket_sla_rule")
public class SlaRuleEntity {

    @TableId(type = IdType.AUTO)
    private Long slaRuleId;
    private String priorityCode;
    private Integer responseLimitMinutes;
    private Integer resolveLimitMinutes;
    private Integer enabledFlag;
    private String remark;
    private Integer deletedFlag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
