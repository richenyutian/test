package com.acme.support.ticket.sla.service;

import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.notification.service.NoticeService;
import com.acme.support.ticket.sla.dto.SlaRequests;
import com.acme.support.ticket.sla.dto.SlaResponses;
import com.acme.support.ticket.sla.entity.SlaRuleEntity;
import com.acme.support.ticket.sla.mapper.SlaRuleMapper;
import com.acme.support.ticket.ticket.entity.TicketEntity;
import com.acme.support.ticket.ticket.mapper.TicketMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SLA 管理服务。
 */
@Service
public class SlaRuleService {

    private final SlaRuleMapper slaRuleMapper;
    private final TicketMapper ticketMapper;
    private final NoticeService noticeService;

    @Value("${ticket.sla.response-warning-minutes:30}")
    private int responseWarningMinutes;

    @Value("${ticket.sla.resolve-warning-minutes:60}")
    private int resolveWarningMinutes;

    public SlaRuleService(
            SlaRuleMapper slaRuleMapper,
            TicketMapper ticketMapper,
            NoticeService noticeService
    ) {
        this.slaRuleMapper = slaRuleMapper;
        this.ticketMapper = ticketMapper;
        this.noticeService = noticeService;
    }

    public SlaResponses.SlaOverviewResponse getOverview() {
        List<SlaResponses.SlaRuleResponse> rules = slaRuleMapper.selectList(
                        new LambdaQueryWrapper<SlaRuleEntity>()
                                .eq(SlaRuleEntity::getDeletedFlag, 0)
                                .orderByAsc(SlaRuleEntity::getSlaRuleId))
                .stream()
                .map(item -> new SlaResponses.SlaRuleResponse(
                        item.getSlaRuleId(),
                        item.getPriorityCode(),
                        item.getResponseLimitMinutes(),
                        item.getResolveLimitMinutes(),
                        item.getEnabledFlag(),
                        item.getRemark()
                ))
                .toList();

        List<SlaResponses.SlaAlertResponse> alerts = ticketMapper.selectList(new LambdaQueryWrapper<TicketEntity>()
                        .eq(TicketEntity::getDeletedFlag, 0)
                        .in(TicketEntity::getCurrentStatus, List.of("PENDING_ACCEPT", "ACCEPTED", "PENDING_ASSIGN", "PROCESSING", "SUSPENDED"))
                        .and(wrapper -> wrapper.eq(TicketEntity::getTimeoutFlag, 1)
                                .or()
                                .eq(TicketEntity::getResponseWarnNotifiedFlag, 1)
                                .or()
                                .eq(TicketEntity::getResolveWarnNotifiedFlag, 1))
                        .orderByDesc(TicketEntity::getUpdatedAt)
                        .last("limit 20"))
                .stream()
                .map(item -> new SlaResponses.SlaAlertResponse(
                        item.getTicketId(),
                        item.getTicketNo(),
                        item.getTitle(),
                        buildAlertType(item),
                        String.valueOf(item.getCurrentHandlerUserId()),
                        String.valueOf(item.getCurrentHandleGroupId()),
                        buildRemainingTime(item)
                ))
                .toList();

        return new SlaResponses.SlaOverviewResponse(rules, alerts);
    }

    @Transactional
    public Long createRule(SlaRequests.SlaRuleSaveRequest request) {
        SlaRuleEntity duplicate = findByPriorityCode(request.priorityCode());
        if (duplicate != null) {
            throw new BusinessException("该优先级已存在 SLA 规则");
        }

        SlaRuleEntity entity = new SlaRuleEntity();
        fillEntity(entity, request);
        entity.setDeletedFlag(0);
        slaRuleMapper.insert(entity);
        return entity.getSlaRuleId();
    }

    @Transactional
    public void updateRule(Long slaRuleId, SlaRequests.SlaRuleSaveRequest request) {
        SlaRuleEntity entity = slaRuleMapper.selectById(slaRuleId);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeletedFlag())) {
            throw new BusinessException("SLA 规则不存在");
        }

        SlaRuleEntity duplicate = findByPriorityCode(request.priorityCode());
        if (duplicate != null && !duplicate.getSlaRuleId().equals(slaRuleId)) {
            throw new BusinessException("该优先级已存在 SLA 规则");
        }

        fillEntity(entity, request);
        slaRuleMapper.updateById(entity);
    }

    @Transactional
    public SlaResponses.SlaScanResultResponse scanTimeouts() {
        List<TicketEntity> activeTickets = ticketMapper.selectList(new LambdaQueryWrapper<TicketEntity>()
                .eq(TicketEntity::getDeletedFlag, 0)
                .in(TicketEntity::getCurrentStatus, List.of("PENDING_ACCEPT", "ACCEPTED", "PENDING_ASSIGN", "PROCESSING", "SUSPENDED")));

        int responseTimeoutCount = 0;
        int resolveTimeoutCount = 0;
        int noticeCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (TicketEntity ticket : activeTickets) {
            boolean changed = false;

            if (ticket.getResponseFinishedAt() == null) {
                if (ticket.getResponseDeadline().minusMinutes(responseWarningMinutes).isBefore(now)
                        && Integer.valueOf(0).equals(ticket.getResponseWarnNotifiedFlag())) {
                    notifyHandler(ticket, "工单响应即将超时", "工单 " + ticket.getTicketNo() + " 即将触发响应超时。");
                    ticket.setResponseWarnNotifiedFlag(1);
                    noticeCount++;
                    changed = true;
                }
                if (ticket.getResponseDeadline().isBefore(now) && Integer.valueOf(0).equals(ticket.getResponseTimeoutFlag())) {
                    ticket.setResponseTimeoutFlag(1);
                    ticket.setTimeoutFlag(1);
                    responseTimeoutCount++;
                    notifyHandler(ticket, "工单响应已超时", "工单 " + ticket.getTicketNo() + " 已发生响应超时。");
                    noticeCount++;
                    changed = true;
                }
            }

            if (ticket.getResolveFinishedAt() == null) {
                if (ticket.getResolveDeadline().minusMinutes(resolveWarningMinutes).isBefore(now)
                        && Integer.valueOf(0).equals(ticket.getResolveWarnNotifiedFlag())) {
                    notifyHandler(ticket, "工单解决即将超时", "工单 " + ticket.getTicketNo() + " 即将触发解决超时。");
                    ticket.setResolveWarnNotifiedFlag(1);
                    noticeCount++;
                    changed = true;
                }
                if (ticket.getResolveDeadline().isBefore(now) && Integer.valueOf(0).equals(ticket.getResolveTimeoutFlag())) {
                    ticket.setResolveTimeoutFlag(1);
                    ticket.setTimeoutFlag(1);
                    resolveTimeoutCount++;
                    notifyHandler(ticket, "工单解决已超时", "工单 " + ticket.getTicketNo() + " 已发生解决超时。");
                    noticeCount++;
                    changed = true;
                }
            }

            if (changed) {
                ticketMapper.updateById(ticket);
            }
        }

        return new SlaResponses.SlaScanResultResponse(responseTimeoutCount, resolveTimeoutCount, noticeCount);
    }

    private void fillEntity(SlaRuleEntity entity, SlaRequests.SlaRuleSaveRequest request) {
        entity.setPriorityCode(request.priorityCode());
        entity.setResponseLimitMinutes(request.responseLimitMinutes());
        entity.setResolveLimitMinutes(request.resolveLimitMinutes());
        entity.setEnabledFlag(request.enabledFlag());
        entity.setRemark(request.remark());
    }

    private SlaRuleEntity findByPriorityCode(String priorityCode) {
        return slaRuleMapper.selectOne(new LambdaQueryWrapper<SlaRuleEntity>()
                .eq(SlaRuleEntity::getPriorityCode, priorityCode)
                .eq(SlaRuleEntity::getDeletedFlag, 0)
                .last("limit 1"));
    }

    private void notifyHandler(TicketEntity ticket, String title, String content) {
        if (ticket.getCurrentHandlerUserId() != null) {
            noticeService.createNotice(ticket.getCurrentHandlerUserId(), title, content, "SLA", "TICKET", ticket.getTicketId());
        }
    }

    private String buildAlertType(TicketEntity item) {
        if (Integer.valueOf(1).equals(item.getResolveTimeoutFlag()) || Integer.valueOf(1).equals(item.getResponseTimeoutFlag())) {
            return "已超时";
        }
        if (Integer.valueOf(1).equals(item.getResolveWarnNotifiedFlag()) || Integer.valueOf(1).equals(item.getResponseWarnNotifiedFlag())) {
            return "即将超时";
        }
        return "正常";
    }

    private String buildRemainingTime(TicketEntity item) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, item.getResolveDeadline());
        long minutes = duration.toMinutes();
        return minutes >= 0 ? minutes + " 分钟" : "已超时 " + Math.abs(minutes) + " 分钟";
    }
}
