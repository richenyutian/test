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
                new DashboardResponses.MetricCard("超时工单", String.valueOf(tickets.stream().filter(TicketResponses.TicketListItemResponse::timeoutFlag).count()), "-3%"),
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
                            (long) index,
                            String.format("WO20260319%04d", index),
                            "核心业务系统异常处理 - " + index,
                            "提交人" + ((index % 8) + 1),
                            index % 2 == 0 ? "USER_SUBMIT" : "SERVICE_ENTRY",
                            TICKET_TYPES.get(index % TICKET_TYPES.size()),
                            CATEGORY_NAMES.get(index % CATEGORY_NAMES.size()).toUpperCase().replace(" ", "_"),
                            priority.name(),
                            status.name(),
                            (long) (index % ASSIGNEES.size() + 1),
                            ASSIGNEES.get(index % ASSIGNEES.size()),
                            (long) (index % GROUPS.size() + 1),
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
        return null;
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
                .filter(ticket -> ticket.currentStatus().equals(TicketStatus.PENDING_ASSIGN.name()) || ticket.currentStatus().equals(TicketStatus.REOPENED.name()))
                .limit(8)
                .map(ticket -> new DispatchResponses.DispatchTicketResponse(
                        ticket.ticketNo(),
                        ticket.title(),
                        ticket.categoryCode(),
                        ticket.priorityCode(),
                        ticket.categoryCode().contains("SYSTEM") ? "运维保障组" : "应用支持组",
                        ticket.categoryCode().contains("API") ? "周工" : "陈工",
                        ticket.createdAt()
                ))
                .toList();

        return new DispatchResponses.DispatchBoardResponse(
                dispatchTickets.size(),
                listTickets().stream().filter(TicketResponses.TicketListItemResponse::escalatedFlag).count(),
                dispatchTickets
        );
    }

    public SlaResponses.SlaOverviewResponse getSlaOverview() {
        List<SlaResponses.SlaRuleResponse> rules = List.of(
                new SlaResponses.SlaRuleResponse(1L, "P1", 15, 120, 1, "mock"),
                new SlaResponses.SlaRuleResponse(2L, "P2", 30, 240, 1, "mock"),
                new SlaResponses.SlaRuleResponse(3L, "P3", 60, 480, 1, "mock")
        );

        List<SlaResponses.SlaAlertResponse> alerts = List.of(
                new SlaResponses.SlaAlertResponse(1L, "WO202603190012", "支付接口超时", "即将响应超时", "王客服", "客服台", "15 分钟"),
                new SlaResponses.SlaAlertResponse(2L, "WO202603190018", "业务报表生成缓慢", "即将解决超时", "陈工", "应用支持组", "45 分钟"),
                new SlaResponses.SlaAlertResponse(3L, "WO202603190027", "生产数据库连接告警", "已超时并升级", "赵主管", "运维保障组", "已超时 20 分钟")
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
                tickets.stream().collect(java.util.stream.Collectors.groupingBy(TicketResponses.TicketListItemResponse::currentStatus, java.util.stream.Collectors.counting()))
        );
        List<ReportResponses.MetricItem> byPriority = groupMetric(
                tickets.stream().collect(java.util.stream.Collectors.groupingBy(TicketResponses.TicketListItemResponse::priorityCode, java.util.stream.Collectors.counting()))
        );
        List<ReportResponses.MetricItem> byGroup = groupMetric(
                tickets.stream().collect(java.util.stream.Collectors.groupingBy(TicketResponses.TicketListItemResponse::currentHandleGroupName, java.util.stream.Collectors.counting()))
        );
        List<ReportResponses.MetricItem> byAssignee = groupMetric(
                tickets.stream().collect(java.util.stream.Collectors.groupingBy(TicketResponses.TicketListItemResponse::currentHandlerName, java.util.stream.Collectors.counting()))
        );
        List<ReportResponses.MetricItem> slaStats = List.of(
                new ReportResponses.MetricItem("SLA 达成工单", 42, "91.4%"),
                new ReportResponses.MetricItem("响应超时工单", 3, "平均响应 38 分钟"),
                new ReportResponses.MetricItem("解决超时工单", 4, "平均解决 5.2 小时")
        );
        List<ReportResponses.MetricItem> timeout = List.of(
                new ReportResponses.MetricItem("超时工单", 7, ""),
                new ReportResponses.MetricItem("升级工单", 4, ""),
                new ReportResponses.MetricItem("重开工单", 2, "")
        );

        return new ReportResponses.ReportOverviewResponse(
                trend,
                byStatus,
                byPriority,
                byGroup,
                byAssignee,
                slaStats,
                timeout
        );
    }

    public NotificationResponses.NotificationCenterResponse getNotificationCenter() {
        List<NotificationResponses.NotificationMessageResponse> messages = List.of(
                new NotificationResponses.NotificationMessageResponse(1L, "新工单待受理", "工单 WO202603190012 待受理。", "INTERNAL", "TICKET", 1L, false, null, LocalDateTime.now().minusMinutes(12)),
                new NotificationResponses.NotificationMessageResponse(2L, "SLA 预警", "工单 WO202603190018 即将解决超时。", "INTERNAL", "TICKET", 2L, false, null, LocalDateTime.now().minusMinutes(25)),
                new NotificationResponses.NotificationMessageResponse(3L, "邮件通知已发送", "您的工单已进入待确认状态。", "EMAIL", "TICKET", 3L, true, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(2))
        );
        return new NotificationResponses.NotificationCenterResponse(2, messages);
    }

    public AuditResponses.AuditLogPageResponse getAuditLogPage() {
        List<AuditResponses.AuditLogResponse> records = List.of(
                new AuditResponses.AuditLogResponse(1L, 1L, "工单管理", "创建工单", "李晓明", 1L, "创建成功", "/api/v1/tickets", "127.0.0.1", LocalDateTime.now().minusHours(8)),
                new AuditResponses.AuditLogResponse(2L, 2L, "分派中心", "分派工单", "王客服", 2L, "分派成功", "/api/v1/dispatch/assign", "127.0.0.1", LocalDateTime.now().minusHours(6)),
                new AuditResponses.AuditLogResponse(3L, 3L, "SLA 管理", "升级工单", "赵主管", 3L, "升级成功", "/api/v1/sla/escalate", "127.0.0.1", LocalDateTime.now().minusHours(3)),
                new AuditResponses.AuditLogResponse(4L, 4L, "系统配置", "修改角色权限", "系统管理员", 4L, "修改成功", "/api/v1/system/roles", "127.0.0.1", LocalDateTime.now().minusHours(1))
        );
        return new AuditResponses.AuditLogPageResponse(1, 10, records.size(), records);
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
        return tickets.stream().filter(ticket -> ticket.currentStatus().equals(status.name())).count();
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
