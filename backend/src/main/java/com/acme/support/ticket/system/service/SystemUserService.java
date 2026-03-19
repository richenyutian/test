package com.acme.support.ticket.system.service;

import com.acme.support.ticket.common.api.PageResponse;
import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.system.dto.SystemManageRequests;
import com.acme.support.ticket.system.dto.SystemManageResponses;
import com.acme.support.ticket.system.entity.HandleGroupEntity;
import com.acme.support.ticket.system.entity.HandleGroupMemberEntity;
import com.acme.support.ticket.system.entity.SysRoleEntity;
import com.acme.support.ticket.system.entity.SysUserEntity;
import com.acme.support.ticket.system.entity.SysUserRoleEntity;
import com.acme.support.ticket.system.mapper.HandleGroupMapper;
import com.acme.support.ticket.system.mapper.HandleGroupMemberMapper;
import com.acme.support.ticket.system.mapper.SysRoleMapper;
import com.acme.support.ticket.system.mapper.SysUserMapper;
import com.acme.support.ticket.system.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理服务。
 */
@Service
public class SystemUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final HandleGroupMapper handleGroupMapper;
    private final HandleGroupMemberMapper handleGroupMemberMapper;

    public SystemUserService(
            SysUserMapper sysUserMapper,
            SysRoleMapper sysRoleMapper,
            SysUserRoleMapper sysUserRoleMapper,
            HandleGroupMapper handleGroupMapper,
            HandleGroupMemberMapper handleGroupMemberMapper
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.handleGroupMapper = handleGroupMapper;
        this.handleGroupMemberMapper = handleGroupMemberMapper;
    }

    public PageResponse<SystemManageResponses.UserPageItemResponse> pageUsers(long current, long size, String keyword) {
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getDeletedFlag, 0)
                .orderByDesc(SysUserEntity::getUserId);

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(query -> query
                    .like(SysUserEntity::getUsername, keyword)
                    .or()
                    .like(SysUserEntity::getDisplayName, keyword));
        }

        Page<SysUserEntity> page = sysUserMapper.selectPage(new Page<>(current, size), wrapper);
        List<SystemManageResponses.UserPageItemResponse> records = page.getRecords().stream()
                .map(this::toUserPageItem)
                .toList();

        return new PageResponse<>(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Transactional
    public Long createUser(SystemManageRequests.UserSaveRequest request) {
        sysUserMapper.findByUsername(request.username()).ifPresent(user -> {
            throw new BusinessException("用户名已存在");
        });

        SysUserEntity entity = new SysUserEntity();
        entity.setUsername(request.username());
        entity.setDisplayName(request.displayName());
        entity.setUserType(request.userType());
        entity.setExternalUserFlag(request.externalUserFlag());
        entity.setPhone(request.phone());
        entity.setEmail(request.email());
        entity.setStatus(request.status());
        entity.setSsoSubject(request.username());
        entity.setRemark(request.remark());
        entity.setDeletedFlag(0);
        sysUserMapper.insert(entity);

        saveUserRoles(entity.getUserId(), request.roleIds());
        savePrimaryGroup(entity.getUserId(), request.primaryHandleGroupId());
        return entity.getUserId();
    }

    @Transactional
    public void updateUser(Long userId, SystemManageRequests.UserSaveRequest request) {
        SysUserEntity existing = sysUserMapper.selectById(userId);

        if (existing == null || Integer.valueOf(1).equals(existing.getDeletedFlag())) {
            throw new BusinessException("用户不存在");
        }

        SysUserEntity duplicate = sysUserMapper.findByUsername(request.username()).orElse(null);
        if (duplicate != null && !duplicate.getUserId().equals(userId)) {
            throw new BusinessException("用户名已存在");
        }

        existing.setUsername(request.username());
        existing.setDisplayName(request.displayName());
        existing.setUserType(request.userType());
        existing.setExternalUserFlag(request.externalUserFlag());
        existing.setPhone(request.phone());
        existing.setEmail(request.email());
        existing.setStatus(request.status());
        existing.setSsoSubject(request.username());
        existing.setRemark(request.remark());
        sysUserMapper.updateById(existing);

        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getUserId, userId));
        saveUserRoles(userId, request.roleIds());

        handleGroupMemberMapper.delete(new LambdaQueryWrapper<HandleGroupMemberEntity>().eq(HandleGroupMemberEntity::getUserId, userId));
        savePrimaryGroup(userId, request.primaryHandleGroupId());
    }

    public List<SystemManageResponses.OptionResponse> listUserOptions() {
        return sysUserMapper.selectList(new LambdaQueryWrapper<SysUserEntity>()
                        .eq(SysUserEntity::getDeletedFlag, 0)
                        .eq(SysUserEntity::getStatus, "ENABLED")
                        .orderByAsc(SysUserEntity::getUserId))
                .stream()
                .map(item -> new SystemManageResponses.OptionResponse(item.getUserId(), item.getDisplayName()))
                .toList();
    }

    private SystemManageResponses.UserPageItemResponse toUserPageItem(SysUserEntity entity) {
        List<SysRoleEntity> roles = sysRoleMapper.findRolesByUserId(entity.getUserId());
        List<HandleGroupMemberEntity> groupMembers = handleGroupMemberMapper.findByUserId(entity.getUserId());

        String primaryGroupName = groupMembers.stream()
                .filter(item -> Integer.valueOf(1).equals(item.getPrimaryGroupFlag()))
                .findFirst()
                .map(HandleGroupMemberEntity::getHandleGroupId)
                .map(handleGroupMapper::selectById)
                .map(HandleGroupEntity::getGroupName)
                .orElse(null);

        return new SystemManageResponses.UserPageItemResponse(
                entity.getUserId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getUserType(),
                entity.getExternalUserFlag(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getStatus(),
                entity.getRemark(),
                roles.stream().map(SysRoleEntity::getRoleName).toList(),
                primaryGroupName,
                entity.getCreatedAt()
        );
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BusinessException("用户至少需要关联一个角色");
        }

        List<SysUserRoleEntity> relations = new ArrayList<>();
        for (Long roleId : roleIds) {
            SysUserRoleEntity relation = new SysUserRoleEntity();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relations.add(relation);
        }

        for (SysUserRoleEntity relation : relations) {
            sysUserRoleMapper.insert(relation);
        }
    }

    private void savePrimaryGroup(Long userId, Long primaryHandleGroupId) {
        if (primaryHandleGroupId == null) {
            return;
        }

        HandleGroupEntity handleGroup = handleGroupMapper.selectById(primaryHandleGroupId);
        if (handleGroup == null) {
            throw new BusinessException("处理组不存在");
        }

        HandleGroupMemberEntity member = new HandleGroupMemberEntity();
        member.setHandleGroupId(primaryHandleGroupId);
        member.setUserId(userId);
        member.setLeaderFlag(0);
        member.setPrimaryGroupFlag(1);
        handleGroupMemberMapper.insert(member);
    }
}
