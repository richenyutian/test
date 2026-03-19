package com.acme.support.ticket.ticket.service;

import com.acme.support.ticket.auth.service.AuthorizationService;
import com.acme.support.ticket.common.api.PageResponse;
import com.acme.support.ticket.common.enums.TicketActionType;
import com.acme.support.ticket.common.enums.TicketStatus;
import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.notification.service.NoticeService;
import com.acme.support.ticket.security.LoginUserPrincipal;
import com.acme.support.ticket.security.SecurityUtils;
import com.acme.support.ticket.sla.entity.SlaRuleEntity;
import com.acme.support.ticket.sla.mapper.SlaRuleMapper;
import com.acme.support.ticket.system.entity.HandleGroupEntity;
import com.acme.support.ticket.system.entity.HandleGroupMemberEntity;
import com.acme.support.ticket.system.entity.SysUserEntity;
import com.acme.support.ticket.system.mapper.HandleGroupMapper;
import com.acme.support.ticket.system.mapper.SysUserMapper;
import com.acme.support.ticket.ticket.dto.TicketRequests;
import com.acme.support.ticket.ticket.dto.TicketResponses;
import com.acme.support.ticket.ticket.entity.TicketAttachmentEntity;
import com.acme.support.ticket.ticket.entity.TicketEntity;
import com.acme.support.ticket.ticket.entity.TicketFlowRecordEntity;
import com.acme.support.ticket.ticket.mapper.TicketAttachmentMapper;
import com.acme.support.ticket.ticket.mapper.TicketFlowRecordMapper;
import com.acme.support.ticket.ticket.mapper.TicketMapper;
import com.acme.support.ticket.ticket.query.TicketQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 工单业务服务。
 */
@Service
public class TicketService {

    private final TicketMapper ticketMapper;
    private final TicketFlowRecordMapper ticketFlowRecordMapper;
    private final TicketAttachmentMapper ticketAttachmentMapper;
    private final SysUserMapper sysUserMapper;
    private final HandleGroupMapper handleGroupMapper;
    private final SlaRuleMapper slaRuleMapper;
    private final AuthorizationService authorizationService;
    private final TicketLifecycleService ticketLifecycleService;
    private final TicketNoGeneratorService ticketNoGeneratorService;
    private final NoticeService noticeService;
    private final FileStorageService fileStorageService;

    public TicketService(
            TicketMapper ticketMapper,
            TicketFlowRecordMapper ticketFlowRecordMapper,
            TicketAttachmentMapper ticketAttachmentMapper,
            SysUserMapper sysUserMapper,
            HandleGroupMapper handleGroupMapper,
            SlaRuleMapper slaRuleMapper,
            AuthorizationService authorizationService,
            TicketLifecycleService ticketLifecycleService,
            TicketNoGeneratorService ticketNoGeneratorService,
            NoticeService noticeService,
            FileStorageService fileStorageService
    ) {
        this.ticketMapper = ticketMapper;
        this.ticketFlowRecordMapper = ticketFlowRecordMapper;
        this.ticketAttachmentMapper = ticketAttachmentMapper;
        this.sysUserMapper = sysUserMapper;
        this.handleGroupMapper = handleGroupMapper;
        this.slaRuleMapper = slaRuleMapper;
        this.authorizationService = authorizationService;
        this.ticketLifecycleService = ticketLifecycleService;
        this.ticketNoGeneratorService = ticketNoGeneratorService;
        this.noticeService = noticeService;
        this.fileStorageService = fileStorageService;
    }

    public PageResponse<TicketResponses.TicketListItemResponse> pageTickets(long current, long size, TicketQuery query) {
        LoginUserPrincipal principal = requireLoginUser();

        LambdaQueryWrapper<TicketEntity> wrapper = new LambdaQueryWrapper<TicketEntity>()
                .eq(TicketEntity::getDeletedFlag, 0)
                .orderByDesc(TicketEntity::getCreatedAt);

        if (query.keyword() != null && !query.keyword().isBlank()) {
            wrapper.and(w -> w.like(TicketEntity::getTicketNo, query.keyword())
                    .or()
                    .like(TicketEntity::getTitle, query.keyword()));
        }
        if (query.currentStatus() != null && !query.currentStatus().isBlank()) {
            wrapper.eq(TicketEntity::getCurrentStatus, query.currentStatus());
        }
        if (query.priorityCode() != null && !query.priorityCode().isBlank()) {
            wrapper.eq(TicketEntity::getPriorityCode, query.priorityCode());
        }
        if (query.handleGroupId() != null) {
            wrapper.eq(TicketEntity::getCurrentHandleGroupId, query.handleGroupId());
        }
        if (query.handlerUserId() != null) {
            wrapper.eq(TicketEntity::getCurrentHandlerUserId, query.handlerUserId());
        }

        applyDataScope(wrapper, principal);

        Page<TicketEntity> page = ticketMapper.selectPage(new Page<>(current, size), wrapper);
        List<TicketResponses.TicketListItemResponse> records = page.getRecords().stream()
                .map(this::toListItem)
                .toList();

        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    public TicketResponses.TicketDetailResponse getTicketDetail(Long ticketId) {
        LoginUserPrincipal principal = requireLoginUser();
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        return toDetail(entity, principal.roles());
    }

    @Transactional
    public Long createTicket(TicketRequests.TicketCreateRequest request) {
        LoginUserPrincipal principal = requireLoginUser();
        SysUserEntity requester = loadUser(principal.userId());
        SlaRuleEntity slaRule = loadEnabledSlaRule(request.priorityCode());
        LocalDateTime now = LocalDateTime.now();

        TicketEntity entity = new TicketEntity();
        entity.setTicketNo(ticketNoGeneratorService.nextTicketNo());
        entity.setTitle(request.title());
        entity.setDescription(request.description());
        entity.setRequesterUserId(requester.getUserId());
        entity.setRequesterName(requester.getDisplayName());
        entity.setContactPhone(request.contactPhone());
        entity.setExternalUserFlag(request.externalUserFlag());
        entity.setSourceCode(request.sourceCode());
        entity.setTicketTypeCode(request.ticketTypeCode());
        entity.setCategoryCode(request.categoryCode());
        entity.setPriorityCode(request.priorityCode());
        entity.setCurrentStatus(TicketStatus.PENDING_ACCEPT.name());
        entity.setEscalatedFlag(0);
        entity.setTimeoutFlag(0);
        entity.setResponseTimeoutFlag(0);
        entity.setResolveTimeoutFlag(0);
        entity.setResponseWarnNotifiedFlag(0);
        entity.setResolveWarnNotifiedFlag(0);
        entity.setResponseSlaMinutes(slaRule.getResponseLimitMinutes());
        entity.setResolveSlaMinutes(slaRule.getResolveLimitMinutes());
        entity.setResponseDeadline(now.plusMinutes(slaRule.getResponseLimitMinutes()));
        entity.setResolveDeadline(now.plusMinutes(slaRule.getResolveLimitMinutes()));
        entity.setReopenCount(0);
        entity.setVersion(0);
        entity.setDeletedFlag(0);
        ticketMapper.insert(entity);

        saveFlow(entity.getTicketId(), TicketActionType.CREATE, null, TicketStatus.PENDING_ACCEPT.name(), "创建工单");
        return entity.getTicketId();
    }

    @Transactional
    public void acceptTicket(Long ticketId, TicketRequests.TicketAcceptRequest request) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.ACCEPT, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(TicketStatus.valueOf(entity.getCurrentStatus()), TicketActionType.ACCEPT);
        SlaRuleEntity slaRule = loadEnabledSlaRule(request.priorityCode());

        entity.setPriorityCode(request.priorityCode());
        entity.setCategoryCode(request.categoryCode());
        entity.setCurrentStatus(targetStatus.name());
        entity.setAcceptedAt(LocalDateTime.now());
        entity.setResponseFinishedAt(LocalDateTime.now());
        entity.setResponseSlaMinutes(slaRule.getResponseLimitMinutes());
        entity.setResolveSlaMinutes(slaRule.getResolveLimitMinutes());
        entity.setResponseDeadline(entity.getCreatedAt().plusMinutes(slaRule.getResponseLimitMinutes()));
        entity.setResolveDeadline(entity.getCreatedAt().plusMinutes(slaRule.getResolveLimitMinutes()));
        bumpVersion(entity);
        ticketMapper.updateById(entity);

        saveFlow(ticketId, TicketActionType.ACCEPT, TicketStatus.PENDING_ACCEPT.name(), targetStatus.name(), "工单受理完成");
    }

    @Transactional
    public void assignTicket(Long ticketId, TicketRequests.TicketAssignRequest request) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.ASSIGN, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus currentStatus = TicketStatus.valueOf(entity.getCurrentStatus());
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(currentStatus, TicketActionType.ASSIGN);

        validateHandleGroup(request.handleGroupId());
        String handlerName = null;
        if (request.handlerUserId() != null) {
            handlerName = loadUser(request.handlerUserId()).getDisplayName();
        }

        entity.setCurrentHandleGroupId(request.handleGroupId());
        entity.setCurrentHandlerUserId(request.handlerUserId());
        entity.setCurrentStatus(targetStatus.name());
        entity.setAssignedAt(LocalDateTime.now());
        bumpVersion(entity);
        ticketMapper.updateById(entity);

        saveFlow(ticketId, TicketActionType.ASSIGN, currentStatus.name(), targetStatus.name(),
                request.operateDescription() == null || request.operateDescription().isBlank() ? "工单已分派" : request.operateDescription());

        if (request.handlerUserId() != null) {
            noticeService.createNotice(
                    request.handlerUserId(),
                    "工单已分派",
                    "工单 " + entity.getTicketNo() + " 已分派给你，请及时接单处理。",
                    "ASSIGN",
                    "TICKET",
                    ticketId
            );
        }
    }

    @Transactional
    public void claimTicket(Long ticketId) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.PROCESS, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus currentStatus = TicketStatus.valueOf(entity.getCurrentStatus());
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(currentStatus, TicketActionType.PROCESS);

        entity.setCurrentHandlerUserId(principal.userId());
        if (entity.getCurrentHandleGroupId() == null) {
            List<HandleGroupMemberEntity> groups = authorizationService.listGroupMembers(principal.userId());
            if (!groups.isEmpty()) {
                entity.setCurrentHandleGroupId(groups.get(0).getHandleGroupId());
            }
        }
        entity.setCurrentStatus(targetStatus.name());
        bumpVersion(entity);
        ticketMapper.updateById(entity);

        saveFlow(ticketId, TicketActionType.PROCESS, currentStatus.name(), targetStatus.name(), "处理人接单开始处理");
    }

    @Transactional
    public void transferTicket(Long ticketId, TicketRequests.TicketTransferRequest request) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.TRANSFER, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus currentStatus = TicketStatus.valueOf(entity.getCurrentStatus());
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(currentStatus, TicketActionType.TRANSFER);

        validateHandleGroup(request.handleGroupId());
        if (request.handlerUserId() != null) {
            loadUser(request.handlerUserId());
        }

        entity.setCurrentHandleGroupId(request.handleGroupId());
        entity.setCurrentHandlerUserId(request.handlerUserId());
        entity.setCurrentStatus(targetStatus.name());
        bumpVersion(entity);
        ticketMapper.updateById(entity);

        saveFlow(ticketId, TicketActionType.TRANSFER, currentStatus.name(), targetStatus.name(), request.operateDescription());

        if (request.handlerUserId() != null) {
            noticeService.createNotice(
                    request.handlerUserId(),
                    "工单已转派",
                    "工单 " + entity.getTicketNo() + " 已转派给你，请及时处理。",
                    "TRANSFER",
                    "TICKET",
                    ticketId
            );
        }
    }

    @Transactional
    public void suspendTicket(Long ticketId, TicketRequests.TicketSuspendRequest request) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.SUSPEND, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus currentStatus = TicketStatus.valueOf(entity.getCurrentStatus());
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(currentStatus, TicketActionType.SUSPEND);

        entity.setCurrentStatus(targetStatus.name());
        entity.setSuspendReason(request.suspendReason());
        bumpVersion(entity);
        ticketMapper.updateById(entity);
        saveFlow(ticketId, TicketActionType.SUSPEND, currentStatus.name(), targetStatus.name(), request.suspendReason());
    }

    @Transactional
    public void resumeTicket(Long ticketId) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.RESUME, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus currentStatus = TicketStatus.valueOf(entity.getCurrentStatus());
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(currentStatus, TicketActionType.RESUME);

        entity.setCurrentStatus(targetStatus.name());
        bumpVersion(entity);
        ticketMapper.updateById(entity);
        saveFlow(ticketId, TicketActionType.RESUME, currentStatus.name(), targetStatus.name(), "工单恢复处理");
    }

    @Transactional
    public void completeTicket(Long ticketId, TicketRequests.TicketCompleteRequest request) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.SUBMIT_SOLUTION, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus currentStatus = TicketStatus.valueOf(entity.getCurrentStatus());
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(currentStatus, TicketActionType.SUBMIT_SOLUTION);

        entity.setCurrentStatus(targetStatus.name());
        entity.setProcessedAt(LocalDateTime.now());
        entity.setResolveFinishedAt(LocalDateTime.now());
        entity.setResolutionSummary(request.resolutionSummary());
        bumpVersion(entity);
        ticketMapper.updateById(entity);

        saveFlow(ticketId, TicketActionType.SUBMIT_SOLUTION, currentStatus.name(), targetStatus.name(), request.resolutionSummary());
        noticeService.createNotice(
                entity.getRequesterUserId(),
                "工单待确认",
                "工单 " + entity.getTicketNo() + " 已处理完成，请确认结果。",
                "CONFIRM",
                "TICKET",
                ticketId
        );
    }

    @Transactional
    public void confirmTicket(Long ticketId) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.CONFIRM, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus currentStatus = TicketStatus.valueOf(entity.getCurrentStatus());
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(currentStatus, TicketActionType.CONFIRM);

        entity.setCurrentStatus(targetStatus.name());
        entity.setUserConfirmedAt(LocalDateTime.now());
        bumpVersion(entity);
        ticketMapper.updateById(entity);
        saveFlow(ticketId, TicketActionType.CONFIRM, currentStatus.name(), targetStatus.name(), "提单人确认工单已解决");
    }

    @Transactional
    public void closeTicket(Long ticketId, TicketRequests.TicketCloseRequest request) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.CLOSE, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus currentStatus = TicketStatus.valueOf(entity.getCurrentStatus());
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(currentStatus, TicketActionType.CLOSE);

        entity.setCurrentStatus(targetStatus.name());
        entity.setCloseReason(request.closeReason());
        entity.setClosedAt(LocalDateTime.now());
        if (entity.getResolveFinishedAt() == null) {
            entity.setResolveFinishedAt(LocalDateTime.now());
        }
        bumpVersion(entity);
        ticketMapper.updateById(entity);
        saveFlow(ticketId, TicketActionType.CLOSE, currentStatus.name(), targetStatus.name(), request.closeReason());
    }

    @Transactional
    public void reopenTicket(Long ticketId, TicketRequests.TicketReopenRequest request) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.REOPEN, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        TicketStatus currentStatus = TicketStatus.valueOf(entity.getCurrentStatus());
        TicketStatus targetStatus = ticketLifecycleService.getTargetStatus(currentStatus, TicketActionType.REOPEN);

        entity.setCurrentStatus(targetStatus.name());
        entity.setReopenReason(request.reopenReason());
        entity.setReopenCount(entity.getReopenCount() == null ? 1 : entity.getReopenCount() + 1);
        entity.setProcessedAt(null);
        entity.setUserConfirmedAt(null);
        entity.setClosedAt(null);
        entity.setResolveFinishedAt(null);
        entity.setTimeoutFlag(0);
        entity.setResolveTimeoutFlag(0);
        entity.setResolveWarnNotifiedFlag(0);
        entity.setResolveDeadline(LocalDateTime.now().plusMinutes(entity.getResolveSlaMinutes()));
        bumpVersion(entity);
        ticketMapper.updateById(entity);

        saveFlow(ticketId, TicketActionType.REOPEN, currentStatus.name(), targetStatus.name(), request.reopenReason());

        if (entity.getCurrentHandlerUserId() != null) {
            noticeService.createNotice(
                    entity.getCurrentHandlerUserId(),
                    "工单已重开",
                    "工单 " + entity.getTicketNo() + " 被重新打开，请重新处理。",
                    "REOPEN",
                    "TICKET",
                    ticketId
            );
        }
    }

    @Transactional
    public void escalateTicket(Long ticketId, TicketRequests.TicketEscalateRequest request) {
        LoginUserPrincipal principal = requireLoginUser();
        ticketLifecycleService.validateRole(TicketActionType.ESCALATE, principal.roles());
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);

        entity.setEscalatedFlag(1);
        entity.setEscalateReason(request.escalateReason());
        entity.setEscalateTime(LocalDateTime.now());
        entity.setEscalateOperatorId(principal.userId());
        bumpVersion(entity);
        ticketMapper.updateById(entity);

        saveFlow(ticketId, TicketActionType.ESCALATE, entity.getCurrentStatus(), entity.getCurrentStatus(), request.escalateReason());
        authorizationService.listUsersByRoleCodes(List.of("SUPERVISOR", "ADMIN"))
                .forEach(user -> noticeService.createNotice(
                        user.getUserId(),
                        "工单已升级",
                        "工单 " + entity.getTicketNo() + " 已被升级，请关注处理。",
                        "ESCALATE",
                        "TICKET",
                        ticketId
                ));
    }

    @Transactional
    public TicketResponses.TicketAttachmentResponse uploadAttachment(Long ticketId, MultipartFile file) {
        LoginUserPrincipal principal = requireLoginUser();
        TicketEntity entity = loadTicketWithPermission(ticketId, principal);
        FileStorageService.StoredFile storedFile = fileStorageService.store(file);

        TicketAttachmentEntity attachment = new TicketAttachmentEntity();
        attachment.setTicketId(ticketId);
        attachment.setOriginalFileName(storedFile.originalFileName());
        attachment.setStorageFileName(storedFile.storageFileName());
        attachment.setStoragePath(storedFile.storagePath());
        attachment.setFileExtension(storedFile.extension());
        attachment.setFileSize(storedFile.fileSize());
        attachment.setUploaderUserId(principal.userId());
        attachment.setUploaderName(principal.username());
        attachment.setCreatedAt(LocalDateTime.now());
        attachment.setDeletedFlag(0);
        ticketAttachmentMapper.insert(attachment);

        saveFlow(ticketId, TicketActionType.COMMENT, entity.getCurrentStatus(), entity.getCurrentStatus(), "上传附件：" + storedFile.originalFileName());

        return new TicketResponses.TicketAttachmentResponse(
                attachment.getAttachmentId(),
                attachment.getOriginalFileName(),
                attachment.getStorageFileName(),
                attachment.getFileExtension(),
                attachment.getFileSize(),
                attachment.getUploaderName(),
                attachment.getCreatedAt()
        );
    }

    public TicketAttachmentEntity getAttachment(Long attachmentId) {
        TicketAttachmentEntity attachment = ticketAttachmentMapper.selectById(attachmentId);
        if (attachment == null || Integer.valueOf(1).equals(attachment.getDeletedFlag())) {
            throw new BusinessException("附件不存在");
        }
        loadTicketWithPermission(attachment.getTicketId(), requireLoginUser());
        return attachment;
    }

    public List<TicketResponses.TicketLifecycleRuleResponse> listLifecycleRules() {
        return List.of(
                new TicketResponses.TicketLifecycleRuleResponse("PENDING_ACCEPT", "受理", "ACCEPTED", List.of("OPS", "SUPERVISOR", "ADMIN")),
                new TicketResponses.TicketLifecycleRuleResponse("ACCEPTED", "分派", "PENDING_ASSIGN", List.of("OPS", "SUPERVISOR", "ADMIN")),
                new TicketResponses.TicketLifecycleRuleResponse("PENDING_ASSIGN", "接单", "PROCESSING", List.of("OPS", "RD", "SUPERVISOR", "ADMIN")),
                new TicketResponses.TicketLifecycleRuleResponse("PROCESSING", "挂起", "SUSPENDED", List.of("OPS", "RD", "SUPERVISOR", "ADMIN")),
                new TicketResponses.TicketLifecycleRuleResponse("SUSPENDED", "恢复", "PROCESSING", List.of("OPS", "RD", "SUPERVISOR", "ADMIN")),
                new TicketResponses.TicketLifecycleRuleResponse("PROCESSING", "提交处理完成", "PENDING_CONFIRM", List.of("OPS", "RD", "SUPERVISOR", "ADMIN")),
                new TicketResponses.TicketLifecycleRuleResponse("PENDING_CONFIRM", "确认解决", "COMPLETED", List.of("REQUESTER", "ADMIN")),
                new TicketResponses.TicketLifecycleRuleResponse("COMPLETED", "关闭", "CLOSED", List.of("SUPERVISOR", "ADMIN")),
                new TicketResponses.TicketLifecycleRuleResponse("PENDING_CONFIRM/COMPLETED/CLOSED", "重开", "REOPENED", List.of("REQUESTER", "SUPERVISOR", "ADMIN"))
        );
    }

    private void applyDataScope(LambdaQueryWrapper<TicketEntity> wrapper, LoginUserPrincipal principal) {
        List<String> roles = principal.roles();
        if (roles.contains("ADMIN")) {
            return;
        }

        if (roles.contains("REQUESTER")) {
            wrapper.eq(TicketEntity::getRequesterUserId, principal.userId());
            return;
        }

        List<Long> groupIds = authorizationService.listGroupMembers(principal.userId()).stream()
                .map(HandleGroupMemberEntity::getHandleGroupId)
                .toList();

        if (roles.contains("OPS") || roles.contains("SUPERVISOR")) {
            wrapper.and(w -> w.in(!groupIds.isEmpty(), TicketEntity::getCurrentHandleGroupId, groupIds)
                    .or()
                    .eq(TicketEntity::getCurrentStatus, TicketStatus.PENDING_ACCEPT.name())
                    .or()
                    .eq(TicketEntity::getCurrentStatus, TicketStatus.ACCEPTED.name()));
            return;
        }

        if (roles.contains("RD")) {
            wrapper.in(groupIds != null && !groupIds.isEmpty(), TicketEntity::getCurrentHandleGroupId, groupIds);
        }
    }

    private TicketEntity loadTicketWithPermission(Long ticketId, LoginUserPrincipal principal) {
        TicketEntity entity = ticketMapper.selectById(ticketId);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeletedFlag())) {
            throw new BusinessException("工单不存在");
        }

        if (!canAccess(entity, principal)) {
            throw new BusinessException("无权访问该工单");
        }
        return entity;
    }

    private boolean canAccess(TicketEntity entity, LoginUserPrincipal principal) {
        if (principal.roles().contains("ADMIN")) {
            return true;
        }
        if (principal.roles().contains("REQUESTER")) {
            return Objects.equals(entity.getRequesterUserId(), principal.userId());
        }

        List<Long> groupIds = authorizationService.listGroupMembers(principal.userId()).stream()
                .map(HandleGroupMemberEntity::getHandleGroupId)
                .toList();

        if (principal.roles().contains("OPS") || principal.roles().contains("SUPERVISOR")) {
            return Objects.equals(entity.getCurrentStatus(), TicketStatus.PENDING_ACCEPT.name())
                    || Objects.equals(entity.getCurrentStatus(), TicketStatus.ACCEPTED.name())
                    || (entity.getCurrentHandleGroupId() != null && groupIds.contains(entity.getCurrentHandleGroupId()));
        }

        if (principal.roles().contains("RD")) {
            return entity.getCurrentHandleGroupId() != null && groupIds.contains(entity.getCurrentHandleGroupId());
        }

        return false;
    }

    private TicketResponses.TicketListItemResponse toListItem(TicketEntity entity) {
        SysUserEntity handler = entity.getCurrentHandlerUserId() == null ? null : sysUserMapper.selectById(entity.getCurrentHandlerUserId());
        HandleGroupEntity group = entity.getCurrentHandleGroupId() == null ? null : handleGroupMapper.selectById(entity.getCurrentHandleGroupId());

        return new TicketResponses.TicketListItemResponse(
                entity.getTicketId(),
                entity.getTicketNo(),
                entity.getTitle(),
                entity.getRequesterName(),
                entity.getSourceCode(),
                entity.getTicketTypeCode(),
                entity.getCategoryCode(),
                entity.getPriorityCode(),
                entity.getCurrentStatus(),
                entity.getCurrentHandlerUserId(),
                handler == null ? null : handler.getDisplayName(),
                entity.getCurrentHandleGroupId(),
                group == null ? null : group.getGroupName(),
                Integer.valueOf(1).equals(entity.getEscalatedFlag()),
                Integer.valueOf(1).equals(entity.getTimeoutFlag()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private TicketResponses.TicketDetailResponse toDetail(TicketEntity entity, List<String> roles) {
        SysUserEntity handler = entity.getCurrentHandlerUserId() == null ? null : sysUserMapper.selectById(entity.getCurrentHandlerUserId());
        HandleGroupEntity group = entity.getCurrentHandleGroupId() == null ? null : handleGroupMapper.selectById(entity.getCurrentHandleGroupId());
        List<TicketResponses.TicketAttachmentResponse> attachments = ticketAttachmentMapper.selectList(
                        new LambdaQueryWrapper<TicketAttachmentEntity>()
                                .eq(TicketAttachmentEntity::getTicketId, entity.getTicketId())
                                .eq(TicketAttachmentEntity::getDeletedFlag, 0)
                                .orderByDesc(TicketAttachmentEntity::getAttachmentId))
                .stream()
                .map(item -> new TicketResponses.TicketAttachmentResponse(
                        item.getAttachmentId(),
                        item.getOriginalFileName(),
                        item.getStorageFileName(),
                        item.getFileExtension(),
                        item.getFileSize(),
                        item.getUploaderName(),
                        item.getCreatedAt()
                ))
                .toList();

        List<TicketResponses.TicketFlowRecordResponse> flowRecords = ticketFlowRecordMapper.selectList(
                        new LambdaQueryWrapper<TicketFlowRecordEntity>()
                                .eq(TicketFlowRecordEntity::getTicketId, entity.getTicketId())
                                .eq(TicketFlowRecordEntity::getDeletedFlag, 0)
                                .orderByDesc(TicketFlowRecordEntity::getFlowRecordId))
                .stream()
                .sorted(Comparator.comparing(TicketFlowRecordEntity::getFlowRecordId))
                .map(item -> new TicketResponses.TicketFlowRecordResponse(
                        item.getFlowRecordId(),
                        item.getActionCode(),
                        item.getOperatorName(),
                        item.getFromStatus(),
                        item.getToStatus(),
                        item.getOperateDescription(),
                        item.getOperateTime()
                ))
                .toList();

        return new TicketResponses.TicketDetailResponse(
                entity.getTicketId(),
                entity.getTicketNo(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getRequesterUserId(),
                entity.getRequesterName(),
                entity.getContactPhone(),
                entity.getExternalUserFlag(),
                entity.getSourceCode(),
                entity.getTicketTypeCode(),
                entity.getCategoryCode(),
                entity.getPriorityCode(),
                entity.getCurrentStatus(),
                entity.getCurrentHandlerUserId(),
                handler == null ? null : handler.getDisplayName(),
                entity.getCurrentHandleGroupId(),
                group == null ? null : group.getGroupName(),
                entity.getEscalatedFlag(),
                entity.getEscalateReason(),
                entity.getEscalateTime(),
                entity.getTimeoutFlag(),
                entity.getResponseDeadline(),
                entity.getResolveDeadline(),
                entity.getCreatedAt(),
                entity.getAcceptedAt(),
                entity.getAssignedAt(),
                entity.getProcessedAt(),
                entity.getUserConfirmedAt(),
                entity.getClosedAt(),
                entity.getSuspendReason(),
                entity.getCloseReason(),
                entity.getReopenReason(),
                entity.getResolutionSummary(),
                attachments,
                flowRecords,
                ticketLifecycleService.listAllowedActions(TicketStatus.valueOf(entity.getCurrentStatus()), roles)
        );
    }

    private void saveFlow(Long ticketId, TicketActionType actionType, String fromStatus, String toStatus, String description) {
        LoginUserPrincipal principal = requireLoginUser();
        TicketFlowRecordEntity record = new TicketFlowRecordEntity();
        record.setTicketId(ticketId);
        record.setActionCode(actionType.name());
        record.setFromStatus(fromStatus);
        record.setToStatus(toStatus);
        record.setOperatorUserId(principal.userId());
        record.setOperatorName(principal.username());
        record.setOperateDescription(description);
        record.setOperateTime(LocalDateTime.now());
        record.setRequestPath(currentRequestPath());
        record.setDeletedFlag(0);
        ticketFlowRecordMapper.insert(record);
    }

    private String currentRequestPath() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        return request.getRequestURI();
    }

    private LoginUserPrincipal requireLoginUser() {
        LoginUserPrincipal principal = SecurityUtils.getLoginUser();
        if (principal == null) {
            throw new BusinessException("未获取到当前登录用户");
        }
        return principal;
    }

    private SysUserEntity loadUser(Long userId) {
        SysUserEntity entity = sysUserMapper.selectById(userId);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeletedFlag())) {
            throw new BusinessException("用户不存在");
        }
        return entity;
    }

    private HandleGroupEntity validateHandleGroup(Long handleGroupId) {
        HandleGroupEntity entity = handleGroupMapper.selectById(handleGroupId);
        if (entity == null || Integer.valueOf(1).equals(entity.getDeletedFlag())) {
            throw new BusinessException("处理组不存在");
        }
        return entity;
    }

    private SlaRuleEntity loadEnabledSlaRule(String priorityCode) {
        SlaRuleEntity entity = slaRuleMapper.selectOne(new LambdaQueryWrapper<SlaRuleEntity>()
                .eq(SlaRuleEntity::getPriorityCode, priorityCode)
                .eq(SlaRuleEntity::getDeletedFlag, 0)
                .eq(SlaRuleEntity::getEnabledFlag, 1)
                .last("limit 1"));
        if (entity == null) {
            throw new BusinessException("未找到可用的 SLA 规则");
        }
        return entity;
    }

    private void bumpVersion(TicketEntity entity) {
        entity.setVersion(entity.getVersion() == null ? 1 : entity.getVersion() + 1);
    }
}
