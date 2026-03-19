package com.acme.support.ticket.ticket.service;

import com.acme.support.ticket.common.enums.TicketActionType;
import com.acme.support.ticket.common.enums.TicketStatus;
import com.acme.support.ticket.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 工单状态机服务。
 */
@Service
public class TicketLifecycleService {

    private static final Map<TicketActionType, List<String>> ACTION_ROLE_MAP = Map.ofEntries(
            Map.entry(TicketActionType.ACCEPT, List.of("OPS", "SUPERVISOR", "ADMIN")),
            Map.entry(TicketActionType.ASSIGN, List.of("OPS", "SUPERVISOR", "ADMIN")),
            Map.entry(TicketActionType.PROCESS, List.of("OPS", "RD", "SUPERVISOR", "ADMIN")),
            Map.entry(TicketActionType.TRANSFER, List.of("OPS", "RD", "SUPERVISOR", "ADMIN")),
            Map.entry(TicketActionType.SUSPEND, List.of("OPS", "RD", "SUPERVISOR", "ADMIN")),
            Map.entry(TicketActionType.RESUME, List.of("OPS", "RD", "SUPERVISOR", "ADMIN")),
            Map.entry(TicketActionType.SUBMIT_SOLUTION, List.of("OPS", "RD", "SUPERVISOR", "ADMIN")),
            Map.entry(TicketActionType.CONFIRM, List.of("REQUESTER", "ADMIN")),
            Map.entry(TicketActionType.CLOSE, List.of("SUPERVISOR", "ADMIN")),
            Map.entry(TicketActionType.REOPEN, List.of("REQUESTER", "SUPERVISOR", "ADMIN")),
            Map.entry(TicketActionType.ESCALATE, List.of("SUPERVISOR", "ADMIN"))
    );

    public TicketStatus getTargetStatus(TicketStatus currentStatus, TicketActionType actionType) {
        return switch (actionType) {
            case ACCEPT -> require(currentStatus, TicketStatus.PENDING_ACCEPT, TicketStatus.ACCEPTED);
            case ASSIGN -> {
                requireOneOf(currentStatus, List.of(TicketStatus.ACCEPTED, TicketStatus.PENDING_ASSIGN, TicketStatus.REOPENED));
                yield TicketStatus.PENDING_ASSIGN;
            }
            case PROCESS -> {
                requireOneOf(currentStatus, List.of(TicketStatus.PENDING_ASSIGN, TicketStatus.SUSPENDED));
                yield TicketStatus.PROCESSING;
            }
            case TRANSFER -> {
                requireOneOf(currentStatus, List.of(TicketStatus.PENDING_ASSIGN, TicketStatus.PROCESSING, TicketStatus.SUSPENDED));
                yield TicketStatus.PENDING_ASSIGN;
            }
            case SUSPEND -> require(currentStatus, TicketStatus.PROCESSING, TicketStatus.SUSPENDED);
            case RESUME -> require(currentStatus, TicketStatus.SUSPENDED, TicketStatus.PROCESSING);
            case SUBMIT_SOLUTION -> require(currentStatus, TicketStatus.PROCESSING, TicketStatus.PENDING_CONFIRM);
            case CONFIRM -> require(currentStatus, TicketStatus.PENDING_CONFIRM, TicketStatus.COMPLETED);
            case CLOSE -> require(currentStatus, TicketStatus.COMPLETED, TicketStatus.CLOSED);
            case REOPEN -> {
                requireOneOf(currentStatus, List.of(TicketStatus.PENDING_CONFIRM, TicketStatus.COMPLETED, TicketStatus.CLOSED));
                yield TicketStatus.REOPENED;
            }
            case ESCALATE -> currentStatus;
            default -> throw new BusinessException("不支持的工单动作");
        };
    }

    public void validateRole(TicketActionType actionType, List<String> roles) {
        List<String> allowedRoles = ACTION_ROLE_MAP.get(actionType);
        if (allowedRoles == null) {
            return;
        }

        boolean matched = roles.stream().anyMatch(allowedRoles::contains);
        if (!matched) {
            throw new BusinessException("当前角色无权执行该操作");
        }
    }

    public List<String> listAllowedActions(TicketStatus status, List<String> roles) {
        List<String> actions = new ArrayList<>();

        for (TicketActionType actionType : ACTION_ROLE_MAP.keySet()) {
            try {
                validateRole(actionType, roles);
                getTargetStatus(status, actionType);
                actions.add(actionType.name());
            } catch (BusinessException ignored) {
                // 跳过不允许的动作
            }
        }

        return actions;
    }

    private TicketStatus require(TicketStatus currentStatus, TicketStatus expected, TicketStatus target) {
        if (currentStatus != expected) {
            throw new BusinessException("当前状态不允许执行该动作");
        }
        return target;
    }

    private void requireOneOf(TicketStatus currentStatus, List<TicketStatus> expectedStatuses) {
        if (!expectedStatuses.contains(currentStatus)) {
            throw new BusinessException("当前状态不允许执行该动作");
        }
    }
}
