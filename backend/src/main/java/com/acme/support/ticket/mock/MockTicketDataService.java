package com.acme.support.ticket.mock;

import com.acme.support.ticket.audit.dto.AuditResponses;
import com.acme.support.ticket.auth.dto.AuthResponses;
import com.acme.support.ticket.common.enums.TicketActionType;
import com.acme.support.ticket.common.enums.TicketPriority;
import com.acme.support.ticket.common.enums.TicketStatus;
import com.acme.support.ticket.dashboard.dto.DashboardResponses;
import com.acme.support.ticket.dispatch.dto.DispatchResponses;
import com.acme.support.ticket.notification.dto.NotificationResponses;
import com.acme.support.ticket.report.dto.ReportResponses;
import com.acme.support.ticket.sla.dto.SlaResponses;
import com.acme.support.ticket.system.dto.SystemResponses;
import com.acme.support.ticket.ticket.dto.TicketResponses;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * mock 数据服务。
 * <p>
 * 该类用于原型阶段快速串起全部模块，包括：
 * 登录、工单列表/详情、分派、SLA、通知、报表、审计和系统配置。
 * 后续替换为真实数据库实现时，可逐个方法迁移至 Repository + Service 层。
 * </p>
 */
@Service
public class MockTicketDataService {

    private static final List<MockUserAccount> USER_ACCOUNTS = List.of(
            new MockUserAccount("U1001", "requester.lee", "ChangeMe123!", "李晓明", "客户成功部", "客户一组",
                    List.of("REQUESTER"),
                    List.of("dashboard:view", "ticket:list", "ticket:detail", "ticket:create", "ticket:urge", "ticket:confirm"),
                    List.of("ticket:create", "ticket:urge", "ticket:confirm", "ticket:rate"),
                    "SELF_CREATED"),
            new MockUserAccount("U2001", "service.desk", "ChangeMe123!", "王客服", "服务运营部", "客服台",
                    List.of("CUSTOMER_SERVICE"),
                    List.of("dashboard:view", "ticket:list", "ticket:detail", "dispatch:center", "notification:center"),
                    List.of("ticket:accept", "ticket:assign", "ticket:cancel", "ticket:close"),
                    "TEAM"),
            new MockUserAccount("U3001", "tech.chen", "ChangeMe123!", "陈工", "技术支持部", "应用支持组",
                    List.of("TECHNICIAN"),
                    List.of("dashboard:view", "ticket:list", "ticket:detail", "notification:center"),
                    List.of("ticket:process", "ticket:transfer", "ticket:suspend", "ticket:complete"),
                    "SELF_ASSIGNED"),
            new MockUserAccount("U4001", "supervisor.zhao", "ChangeMe123!", "赵主管", "技术支持部", "应用支持组",
                    List.of("SUPERVISOR"),
                    List.of("dashboard:view", "ticket:list", "ticket:detail", "dispatch:center", "sla:manage", "report:center"),
                    List.of("ticket:assign", "ticket:escalate", "ticket:resume", "ticket:intervene"),
                    "TEAM"),
            new MockUserAccount("U9001", "admin.root", "ChangeMe123!", "系统管理员", "信息化部", "平台管理组",
                    List.of("ADMIN"),
                    List.of("dashboard:view", "ticket:list", "ticket:detail", "dispatch:center", "sla:manage", "report:center", "system:config", "audit:log"),
                    List.of("ticket:*", "system:*", "audit:view"),
                    "ALL")
    );

    private static final List<String> CATEGORY_NAMES = List.of("账号权限", "系统故障", "接口异常", "性能问题", "报表问题", "环境部署");
    private static final List<String> TICKET_TYPES = List.of("咨询", "故障", "需求", "变更");
    private static final List<String> ASSIGNEES = List.of("陈工", "周工", "吴工", "何工", "黄工");
    private static final List<String> GROUPS = List.of("应用支持组", "运维保障组", "研发协同组");
    private static final List<String> DEPARTMENTS = List.of("客户成功部", "财务部", "供应链部", "营销中心", "信息化部");
    private static final List<String> TAG_POOL = List.of("VIP客户", "核心业务", "高峰期", "需回访", "自动升级", "外部依赖");

    public Optional<MockUserAccount> findUser(String username, String password) {
        return USER_ACCOUNTS.stream()
                .filter(user -> user.username().equals(username) && user.password().equals(password))
                .findFirst();
    }

    public AuthResponses.UserProfileResponse buildUserProfile(MockUserAccount account) {
        return new AuthResponses.UserProfileResponse(
                0L,
                account.username(),
                account.displayName(),
                "INTERNAL",
                account.roles(),
                account.menuPermissions(),
                account.buttonPermissions(),
                account.dataScope()
        );
    }

    public DashboardResponses.WorkbenchOverviewResponse getWorkbenchOverview() {
        List<TicketResponses.TicketListItemResponse> tickets = listTickets();

        List<DashboardResponses.MetricCard> metrics = List.of(
                new DashboardResponses.MetricCard("工单总量", String.valueOf(tickets.size()), "+12%"),
                new DashboardResponses.MetricCard("处理中工单", String.valueOf(countByStatus(tickets, TicketStatus.PROCESSING)), "+4%"),
                new DashboardResponses.MetricCard("超时工单", String.valueOf(tickets.stream().filter(TicketResponses.TicketListItemResponse::timeout).count()), "-3%"),
                new DashboardResponses.MetricCard("SLA 达成率", "91.4%", "+1.6%")
        );

        List<DashboardResponses.TrendPoint> trend = IntStream.rangeClosed(0, 6)
                .mapToObj(index -> new DashboardResponses.TrendPoint(
                        LocalDate.now().minusDays(6L - index).toString(),
                        8L + index * 2L
                ))
                .toList();

        List<DashboardResponses.MetricCard> statusDistribution = List.of(
                new DashboardResponses.MetricCard("待受理", String.valueOf(countByStatus(tickets, TicketStatus.PENDING_ACCEPT)), ""),
                new DashboardResponses.MetricCard("待分派", String.valueOf(countByStatus(tickets, TicketStatus.PENDING_ASSIGN)), ""),
                new DashboardResponses.MetricCard("处理中", String.valueOf(countByStatus(tickets, TicketStatus.PROCESSING)), ""),
                new DashboardResponses.MetricCard("待确认", String.valueOf(countByStatus(tickets, TicketStatus.PENDING_CONFIRM)), "")
        );

        List<DashboardResponses.MetricCard> personalTasks = List.of(
                new DashboardResponses.MetricCard("我创建的工单", "14", ""),
                new DashboardResponses.MetricCard("我待处理的工单", "7", ""),
                new DashboardResponses.MetricCard("我未读通知", "5", ""),
                new DashboardResponses.MetricCard("本周满意度", "4.7 / 5", "")
        );

        return new DashboardResponses.WorkbenchOverviewResponse(metrics, trend, statusDistribution, personalTasks);
    }

    public List<TicketResponses.TicketListItemResponse> listTickets() {
        List<TicketStatus> statuses = List.of(
                TicketStatus.PENDING_ACCEPT,
                TicketStatus.ACCEPTED,
                TicketStatus.PENDING_ASSIGN,
                TicketStatus.PROCESSING,
                TicketStatus.PENDING_CONFIRM,
                TicketStatus.COMPLETED,
                TicketStatus.CLOSED,
                TicketStatus.CANCELLED,
                TicketStatus.SUSPENDED,
                TicketStatus.REOPENED
        );

        return IntStream.rangeClosed(1, 48)
                .mapToObj(index -> {
                    TicketStatus status = statuses.get(index % statuses.size());
                    TicketPriority priority = TicketPriority.values()[index % TicketPriority.values().length];
                    LocalDateTime createdAt = LocalDateTime.now().minusDays(index % 20L).minusHours(index * 2L);

                    return new TicketResponses.TicketListItemResponse(
                            "TID-" + index,
                            String.format("WO20260319%04d", index),
                            "核心业务系统异常处理 - " + index,
                            "提交人" + ((index % 8) + 1),
                            index % 2 == 0 ? "用户手工提交" : "客服代录入",
                            TICKET_TYPES.get(index % TICKET_TYPES.size()),
                            CATEGORY_NAMES.get(index % CATEGORY_NAMES.size()),
                            priority.name(),
                            index % 4 == 0 ? "CRITICAL" : index % 3 == 0 ? "HIGH" : "MEDIUM",
                            status.name(),
                            ASSIGNEES.get(index % ASSIGNEES.size()),
                            GROUPS.get(index % GROUPS.size()),
                            index % 6 == 0,
                            index % 7 == 0 || status == TicketStatus.SUSPENDED,
                            createdAt,
                            createdAt.plusHours(index % 24L)
                    );
                })
                .sorted(Comparator.comparing(TicketResponses.TicketListItemResponse::createdAt).reversed())
                .toList();
    }

    public TicketResponses.TicketDetailResponse getTicketDetail(String ticketId) {
        TicketResponses.TicketListItemResponse summary = listTickets().stream()
                .filter(item -> item.ticketId().equals(ticketId))
                .findFirst()
                .orElse(listTickets().get(0));

        LocalDateTime createdAt = summary.createdAt();
        LocalDateTime acceptedAt = createdAt.plusMinutes(20);
        LocalDateTime assignedAt = acceptedAt.plusMinutes(15);
        LocalDateTime completedAt = summary.status().equals(TicketStatus.PENDING_CONFIRM.name())
                || summary.status().equals(TicketStatus.COMPLETED.name())
                || summary.status().equals(TicketStatus.CLOSED.name())
                ? assignedAt.plusHours(4)
                : null;

        List<TicketResponses.TicketFlowRecordResponse> flowRecords = new ArrayList<>();
        flowRecords.add(new TicketResponses.TicketFlowRecordResponse(
                TicketActionType.CREATE.name(),
                summary.requesterName(),
                null,
                TicketStatus.PENDING_ACCEPT.name(),
                "提交工单并上传错误截图",
                createdAt
        ));
        flowRecords.add(new TicketResponses.TicketFlowRecordResponse(
                TicketActionType.ACCEPT.name(),
                "王客服",
                TicketStatus.PENDING_ACCEPT.name(),
                TicketStatus.ACCEPTED.name(),
                "工单有效，进入分类流程",
                acceptedAt
        ));
        flowRecords.add(new TicketResponses.TicketFlowRecordResponse(
                TicketActionType.ASSIGN.name(),
                "王客服",
                TicketStatus.ACCEPTED.name(),
                TicketStatus.PROCESSING.name(),
                "分派至 " + summary.currentGroupName() + " / " + summary.currentAssigneeName(),
                assignedAt
        ));
        if (completedAt != null) {
            flowRecords.add(new TicketResponses.TicketFlowRecordResponse(
                    TicketActionType.SUBMIT_SOLUTION.name(),
                    summary.currentAssigneeName(),
                    TicketStatus.PROCESSING.name(),
                    TicketStatus.PENDING_CONFIRM.name(),
                    "已提交解决方案，等待用户确认",
                    completedAt
            ));
        }

        List<TicketResponses.TicketCommentResponse> comments = List.of(
                new TicketResponses.TicketCommentResponse("CMT-1", summary.requesterName(), "REQUESTER", "问题在高峰期频繁出现，请尽快处理。", createdAt.plusMinutes(5)),
                new TicketResponses.TicketCommentResponse("CMT-2", "王客服", "CUSTOMER_SERVICE", "已完成工单受理，正在安排处理人。", acceptedAt),
                new TicketResponses.TicketCommentResponse("CMT-3", summary.currentAssigneeName(), "TECHNICIAN", "已定位为接口网关超时，正在执行优化。", assignedAt.plusMinutes(30))
        );

        return new TicketResponses.TicketDetailResponse(
                summary.ticketId(),
                summary.ticketNo(),
                summary.title(),
                "客户反馈核心业务流程在高峰期出现异常，请求技术支持快速定位并恢复，涉及接口超时和页面卡顿。",
                summary.requesterName(),
                "13800001234",
                "华东大客户中心",
                DEPARTMENTS.get(0),
                summary.source(),
                summary.ticketType(),
                summary.categoryName(),
                summary.priority(),
                summary.urgencyLevel(),
                summary.status(),
                summary.currentAssigneeName(),
                summary.currentGroupName(),
                createdAt.plusHours(2),
                createdAt.plusHours(8),
                createdAt,
                acceptedAt,
                assignedAt,
                completedAt,
                summary.status().equals(TicketStatus.CLOSED.name()) ? completedAt.plusHours(2) : null,
                "满意",
                "处理及时，沟通顺畅。",
                TAG_POOL.subList(0, 3),
                flowRecords,
                comments
        );
    }

    public List<TicketResponses.TicketLifecycleRuleResponse> getLifecycleRules() {
        return List.of(
                new TicketResponses.TicketLifecycleRuleResponse("PENDING_ACCEPT", "受理", "ACCEPTED", List.of("CUSTOMER_SERVICE")),
                new TicketResponses.TicketLifecycleRuleResponse("ACCEPTED", "提交分派", "PENDING_ASSIGN", List.of("CUSTOMER_SERVICE")),
                new TicketResponses.TicketLifecycleRuleResponse("PENDING_ASSIGN", "正式分派", "PROCESSING", List.of("CUSTOMER_SERVICE", "SUPERVISOR")),
                new TicketResponses.TicketLifecycleRuleResponse("PROCESSING", "挂起", "SUSPENDED", List.of("TECHNICIAN", "SUPERVISOR")),
                new TicketResponses.TicketLifecycleRuleResponse("PROCESSING", "提交解决方案", "PENDING_CONFIRM", List.of("TECHNICIAN")),
                new TicketResponses.TicketLifecycleRuleResponse("PENDING_CONFIRM", "确认解决", "COMPLETED", List.of("REQUESTER")),
                new TicketResponses.TicketLifecycleRuleResponse("PENDING_CONFIRM", "确认未解决", "REOPENED", List.of("REQUESTER")),
                new TicketResponses.TicketLifecycleRuleResponse("COMPLETED", "关闭", "CLOSED", List.of("CUSTOMER_SERVICE", "ADMIN"))
        );
    }

    public DispatchResponses.DispatchBoardResponse getDispatchBoard() {
        List<DispatchResponses.DispatchTicketResponse> dispatchTickets = listTickets().stream()
                .filter(ticket -> ticket.status().equals(TicketStatus.PENDING_ASSIGN.name()) || ticket.status().equals(TicketStatus.REOPENED.name()))
                .limit(8)
                .map(ticket -> new DispatchResponses.DispatchTicketResponse(
                        ticket.ticketNo(),
                        ticket.title(),
                        ticket.categoryName(),
                        ticket.priority(),
                        ticket.categoryName().contains("系统") ? "运维保障组" : "应用支持组",
                        ticket.categoryName().contains("接口") ? "周工" : "陈工",
                        ticket.createdAt()
                ))
                .toList();

        return new DispatchResponses.DispatchBoardResponse(
                dispatchTickets.size(),
                listTickets().stream().filter(TicketResponses.TicketListItemResponse::escalated).count(),
                dispatchTickets
        );
    }

    public SlaResponses.SlaOverviewResponse getSlaOverview() {
        List<SlaResponses.SlaRuleResponse> rules = List.of(
                new SlaResponses.SlaRuleResponse("SLA-INC-P1", "P1 故障响应规则", "系统故障", 15, 120, 5, 20, true),
                new SlaResponses.SlaRuleResponse("SLA-INC-P2", "P2 故障响应规则", "接口异常", 30, 240, 10, 30, true),
                new SlaResponses.SlaRuleResponse("SLA-SRV-P3", "一般服务请求规则", "账号权限", 60, 480, 20, 60, false)
        );

        List<SlaResponses.SlaAlertResponse> alerts = List.of(
                new SlaResponses.SlaAlertResponse("WO202603190012", "支付接口超时", "即将响应超时", "王客服", "客服台", "15 分钟"),
                new SlaResponses.SlaAlertResponse("WO202603190018", "业务报表生成缓慢", "即将解决超时", "陈工", "应用支持组", "45 分钟"),
                new SlaResponses.SlaAlertResponse("WO202603190027", "生产数据库连接告警", "已超时并升级", "赵主管", "运维保障组", "已超时 20 分钟")
        );

        return new SlaResponses.SlaOverviewResponse(rules, alerts);
    }

    public ReportResponses.ReportOverviewResponse getReportOverview() {
        List<TicketResponses.TicketListItemResponse> tickets = listTickets();

        List<ReportResponses.MetricItem> trend = IntStream.rangeClosed(0, 6)
                .mapToObj(index -> new ReportResponses.MetricItem(
                        LocalDate.now().minusDays(6L - index).toString(),
                        10L + index * 3L,
                        "日工单量"
                ))
                .toList();

        List<ReportResponses.MetricItem> byStatus = groupMetric(
                tickets.stream().collect(java.util.stream.Collectors.groupingBy(TicketResponses.TicketListItemResponse::status, java.util.stream.Collectors.counting()))
        );
        List<ReportResponses.MetricItem> byCategory = groupMetric(
                tickets.stream().collect(java.util.stream.Collectors.groupingBy(TicketResponses.TicketListItemResponse::categoryName, java.util.stream.Collectors.counting()))
        );
        List<ReportResponses.MetricItem> byAssignee = groupMetric(
                tickets.stream().collect(java.util.stream.Collectors.groupingBy(TicketResponses.TicketListItemResponse::currentAssigneeName, java.util.stream.Collectors.counting()))
        );
        List<ReportResponses.MetricItem> byDepartment = groupMetric(
                Map.of("客户成功部", 11L, "财务部", 7L, "供应链部", 9L, "营销中心", 10L, "信息化部", 11L)
        );
        List<ReportResponses.MetricItem> slaStats = List.of(
                new ReportResponses.MetricItem("SLA 达成工单", 42, "91.4%"),
                new ReportResponses.MetricItem("响应超时工单", 3, "平均响应 38 分钟"),
                new ReportResponses.MetricItem("解决超时工单", 4, "平均解决 5.2 小时")
        );
        List<ReportResponses.MetricItem> satisfaction = List.of(
                new ReportResponses.MetricItem("满意", 24, "5分"),
                new ReportResponses.MetricItem("一般", 6, "3-4分"),
                new ReportResponses.MetricItem("不满意", 2, "1-2分")
        );

        return new ReportResponses.ReportOverviewResponse(
                trend,
                byStatus,
                byCategory,
                byAssignee,
                byDepartment,
                slaStats,
                satisfaction
        );
    }

    public NotificationResponses.NotificationCenterResponse getNotificationCenter() {
        List<NotificationResponses.NotificationMessageResponse> messages = List.of(
                new NotificationResponses.NotificationMessageResponse("MSG-1", "新工单待受理", "INTERNAL", "王客服", "工单 WO202603190012 待受理。", false, LocalDateTime.now().minusMinutes(12)),
                new NotificationResponses.NotificationMessageResponse("MSG-2", "SLA 预警", "INTERNAL", "陈工", "工单 WO202603190018 即将解决超时。", false, LocalDateTime.now().minusMinutes(25)),
                new NotificationResponses.NotificationMessageResponse("MSG-3", "邮件通知已发送", "EMAIL", "李晓明", "您的工单已进入待确认状态。", true, LocalDateTime.now().minusHours(2))
        );
        return new NotificationResponses.NotificationCenterResponse(2, messages);
    }

    public AuditResponses.AuditLogPageResponse getAuditLogPage() {
        List<AuditResponses.AuditLogResponse> records = List.of(
                new AuditResponses.AuditLogResponse("AUD-1", "工单管理", "创建工单", "李晓明", "/api/v1/tickets", "SUCCESS", LocalDateTime.now().minusHours(8)),
                new AuditResponses.AuditLogResponse("AUD-2", "分派中心", "分派工单", "王客服", "/api/v1/dispatch/assign", "SUCCESS", LocalDateTime.now().minusHours(6)),
                new AuditResponses.AuditLogResponse("AUD-3", "SLA 管理", "升级工单", "赵主管", "/api/v1/sla/escalate", "SUCCESS", LocalDateTime.now().minusHours(3)),
                new AuditResponses.AuditLogResponse("AUD-4", "系统配置", "修改角色权限", "系统管理员", "/api/v1/system/roles", "SUCCESS", LocalDateTime.now().minusHours(1))
        );
        return new AuditResponses.AuditLogPageResponse(records.size(), records);
    }

    public SystemResponses.SystemConfigOverviewResponse getSystemConfigOverview() {
        List<SystemResponses.DictionaryItemResponse> categories = CATEGORY_NAMES.stream()
                .map(name -> new SystemResponses.DictionaryItemResponse(name.toUpperCase().replace(" ", "_"), name, "工单分类配置"))
                .toList();

        List<SystemResponses.DictionaryItemResponse> priorities = List.of(
                new SystemResponses.DictionaryItemResponse("P1", "极高", "核心业务中断"),
                new SystemResponses.DictionaryItemResponse("P2", "高", "重要功能不可用"),
                new SystemResponses.DictionaryItemResponse("P3", "中", "一般业务受影响"),
                new SystemResponses.DictionaryItemResponse("P4", "低", "低影响需求或咨询")
        );

        List<SystemResponses.RolePermissionResponse> rolePermissions = USER_ACCOUNTS.stream()
                .map(account -> new SystemResponses.RolePermissionResponse(
                        account.roles().get(0),
                        account.displayName() + "角色样例",
                        account.menuPermissions(),
                        account.buttonPermissions(),
                        account.dataScope()
                ))
                .toList();

        return new SystemResponses.SystemConfigOverviewResponse(categories, priorities, rolePermissions);
    }

    private long countByStatus(List<TicketResponses.TicketListItemResponse> tickets, TicketStatus status) {
        return tickets.stream().filter(ticket -> ticket.status().equals(status.name())).count();
    }

    private List<ReportResponses.MetricItem> groupMetric(Map<String, Long> groups) {
        return groups.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new ReportResponses.MetricItem(entry.getKey(), entry.getValue(), ""))
                .toList();
    }

    public record MockUserAccount(
            String userId,
            String username,
            String password,
            String displayName,
            String departmentName,
            String teamName,
            List<String> roles,
            List<String> menuPermissions,
            List<String> buttonPermissions,
            String dataScope
    ) {
    }
}
