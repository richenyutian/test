package com.acme.support.ticket.system.service;

import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.system.dto.SystemManageRequests;
import com.acme.support.ticket.system.dto.SystemManageResponses;
import com.acme.support.ticket.system.entity.HandleGroupEntity;
import com.acme.support.ticket.system.mapper.HandleGroupMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 处理组管理服务。
 */
@Service
public class HandleGroupService {

    private final HandleGroupMapper handleGroupMapper;

    public HandleGroupService(HandleGroupMapper handleGroupMapper) {
        this.handleGroupMapper = handleGroupMapper;
    }

    public List<SystemManageResponses.HandleGroupResponse> listGroups() {
        return handleGroupMapper.selectList(new LambdaQueryWrapper<HandleGroupEntity>()
                        .eq(HandleGroupEntity::getDeletedFlag, 0)
                        .orderByAsc(HandleGroupEntity::getHandleGroupId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Long createGroup(SystemManageRequests.HandleGroupSaveRequest request) {
        checkGroupCodeUnique(request.groupCode(), null);

        HandleGroupEntity entity = new HandleGroupEntity();
        fillGroup(entity, request);
        entity.setDeletedFlag(0);
        handleGroupMapper.insert(entity);
        return entity.getHandleGroupId();
    }

    public void updateGroup(Long handleGroupId, SystemManageRequests.HandleGroupSaveRequest request) {
        HandleGroupEntity entity = handleGroupMapper.selectById(handleGroupId);
        if (entity == null) {
            throw new BusinessException("处理组不存在");
        }

        checkGroupCodeUnique(request.groupCode(), handleGroupId);
        fillGroup(entity, request);
        handleGroupMapper.updateById(entity);
    }

    public List<SystemManageResponses.OptionResponse> listGroupOptions() {
        return listGroups().stream()
                .map(item -> new SystemManageResponses.OptionResponse(item.handleGroupId(), item.groupName()))
                .toList();
    }

    private void fillGroup(HandleGroupEntity entity, SystemManageRequests.HandleGroupSaveRequest request) {
        entity.setGroupCode(request.groupCode());
        entity.setGroupName(request.groupName());
        entity.setGroupType(request.groupType());
        entity.setLeaderUserId(request.leaderUserId());
        entity.setLeaderName(request.leaderName());
        entity.setStatus(request.status());
        entity.setRemark(request.remark());
    }

    private void checkGroupCodeUnique(String groupCode, Long handleGroupId) {
        HandleGroupEntity duplicate = handleGroupMapper.selectOne(new LambdaQueryWrapper<HandleGroupEntity>()
                .eq(HandleGroupEntity::getGroupCode, groupCode)
                .eq(HandleGroupEntity::getDeletedFlag, 0)
                .last("limit 1"));

        if (duplicate != null && (handleGroupId == null || !duplicate.getHandleGroupId().equals(handleGroupId))) {
            throw new BusinessException("处理组编码已存在");
        }
    }

    private SystemManageResponses.HandleGroupResponse toResponse(HandleGroupEntity entity) {
        return new SystemManageResponses.HandleGroupResponse(
                entity.getHandleGroupId(),
                entity.getGroupCode(),
                entity.getGroupName(),
                entity.getGroupType(),
                entity.getLeaderUserId(),
                entity.getLeaderName(),
                entity.getStatus(),
                entity.getRemark()
        );
    }
}
