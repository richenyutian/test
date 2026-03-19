package com.acme.support.ticket.report.service;

import com.acme.support.ticket.report.dto.ReportResponses;
import com.acme.support.ticket.system.mapper.HandleGroupMapper;
import com.acme.support.ticket.system.mapper.SysUserMapper;
import com.acme.support.ticket.ticket.entity.TicketEntity;
import com.acme.support.ticket.ticket.mapper.TicketMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * 报表服务。
 */
@Service
public class ReportService {

    private final TicketMapper ticketMapper;
    private final SysUserMapper sysUserMapper;
    private final HandleGroupMapper handleGroupMapper;

    public ReportService(
            TicketMapper ticketMapper,
            SysUserMapper sysUserMapper,
            HandleGroupMapper handleGroupMapper
    ) {
        this.ticketMapper = ticketMapper;
        this.sysUserMapper = sysUserMapper;
        this.handleGroupMapper = handleGroupMapper;
    }

    public ReportResponses.ReportOverviewResponse getOverview() {
        List<TicketEntity> tickets = ticketMapper.selectList(new LambdaQueryWrapper<TicketEntity>()
                .eq(TicketEntity::getDeletedFlag, 0));

        return new ReportResponses.ReportOverviewResponse(
                buildTrend(tickets),
                aggregate(tickets, TicketEntity::getCurrentStatus),
                aggregate(tickets, TicketEntity::getPriorityCode),
                aggregate(tickets, ticket -> {
                    if (ticket.getCurrentHandleGroupId() == null) {
                        return "未分组";
                    }
                    var group = handleGroupMapper.selectById(ticket.getCurrentHandleGroupId());
                    return group == null ? "未知处理组" : group.getGroupName();
                }),
                aggregate(tickets, ticket -> {
                    if (ticket.getCurrentHandlerUserId() == null) {
                        return "未分派";
                    }
                    var user = sysUserMapper.selectById(ticket.getCurrentHandlerUserId());
                    return user == null ? "未知处理人" : user.getDisplayName();
                }),
                buildSlaStatistics(tickets),
                buildTimeoutStatistics(tickets)
        );
    }

    private List<ReportResponses.MetricItem> buildTrend(List<TicketEntity> tickets) {
        Map<String, Long> dailyCountMap = new TreeMap<>();
        for (int index = 6; index >= 0; index--) {
            dailyCountMap.put(LocalDate.now().minusDays(index).toString(), 0L);
        }

        for (TicketEntity ticket : tickets) {
            String key = ticket.getCreatedAt().toLocalDate().toString();
            if (dailyCountMap.containsKey(key)) {
                dailyCountMap.put(key, dailyCountMap.get(key) + 1);
            }
        }

        return dailyCountMap.entrySet().stream()
                .map(entry -> new ReportResponses.MetricItem(entry.getKey(), entry.getValue(), "工单数量"))
                .toList();
    }

    private List<ReportResponses.MetricItem> buildSlaStatistics(List<TicketEntity> tickets) {
        long total = tickets.size();
        long reach = tickets.stream()
                .filter(ticket -> Integer.valueOf(0).equals(ticket.getTimeoutFlag()))
                .count();
        long responseTimeout = tickets.stream().filter(ticket -> Integer.valueOf(1).equals(ticket.getResponseTimeoutFlag())).count();
        long resolveTimeout = tickets.stream().filter(ticket -> Integer.valueOf(1).equals(ticket.getResolveTimeoutFlag())).count();

        List<ReportResponses.MetricItem> result = new ArrayList<>();
        result.add(new ReportResponses.MetricItem("SLA 达成工单", reach, total == 0 ? "0%" : String.format("%.2f%%", (reach * 100.0) / total)));
        result.add(new ReportResponses.MetricItem("响应超时工单", responseTimeout, ""));
        result.add(new ReportResponses.MetricItem("解决超时工单", resolveTimeout, ""));
        return result;
    }

    private List<ReportResponses.MetricItem> buildTimeoutStatistics(List<TicketEntity> tickets) {
        long totalTimeout = tickets.stream().filter(ticket -> Integer.valueOf(1).equals(ticket.getTimeoutFlag())).count();
        long escalated = tickets.stream().filter(ticket -> Integer.valueOf(1).equals(ticket.getEscalatedFlag())).count();
        long reopen = tickets.stream().filter(ticket -> ticket.getReopenCount() != null && ticket.getReopenCount() > 0).count();

        return List.of(
                new ReportResponses.MetricItem("超时工单", totalTimeout, ""),
                new ReportResponses.MetricItem("升级工单", escalated, ""),
                new ReportResponses.MetricItem("重开工单", reopen, "")
        );
    }

    private List<ReportResponses.MetricItem> aggregate(List<TicketEntity> tickets, Function<TicketEntity, String> classifier) {
        return tickets.stream()
                .map(classifier)
                .filter(item -> item != null && !item.isBlank())
                .collect(java.util.stream.Collectors.groupingBy(Function.identity(), java.util.stream.Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new ReportResponses.MetricItem(entry.getKey(), entry.getValue(), ""))
                .sorted((left, right) -> Long.compare(right.value(), left.value()))
                .toList();
    }
}
