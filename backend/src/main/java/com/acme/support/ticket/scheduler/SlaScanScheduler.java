package com.acme.support.ticket.scheduler;

import com.acme.support.ticket.sla.service.SlaRuleService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SLA 扫描任务。
 */
@Component
public class SlaScanScheduler {

    private final SlaRuleService slaRuleService;

    public SlaScanScheduler(SlaRuleService slaRuleService) {
        this.slaRuleService = slaRuleService;
    }

    @Scheduled(fixedDelayString = "300000")
    public void scanTimeoutTickets() {
        slaRuleService.scanTimeouts();
    }
}
