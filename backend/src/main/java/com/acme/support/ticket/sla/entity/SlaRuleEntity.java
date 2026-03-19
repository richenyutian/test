package com.acme.support.ticket.sla.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * SLA 规则实体。
 */
@Data
@TableName("sla_rule")
public class SlaRuleEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String ruleId;
    private String ruleCode;
    private String ruleName;
    private String categoryCode;
    private String categoryName;
    private Integer responseMinutes;
    private Integer resolveMinutes;
    private Integer responseWarningMinutes;
    private Integer resolveWarningMinutes;
    private Boolean autoEscalate;
    private Integer deleted;
}
