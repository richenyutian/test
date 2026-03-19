package com.acme.support.ticket.system.service;

import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.system.dto.SystemManageRequests;
import com.acme.support.ticket.system.dto.SystemManageResponses;
import com.acme.support.ticket.system.entity.SysRoleEntity;
import com.acme.support.ticket.system.entity.SysRoleMenuEntity;
import com.acme.support.ticket.system.mapper.SysRoleMapper;
import com.acme.support.ticket.system.mapper.SysRoleMenuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理服务。
 */
@Service
public class SystemRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    public SystemRoleService(SysRoleMapper sysRoleMapper, SysRoleMenuMapper sysRoleMenuMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
    }

    public List<SystemManageResponses.RoleResponse> listRoles() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()
                        .eq(SysRoleEntity::getDeletedFlag, 0)
                        .orderByAsc(SysRoleEntity::getRoleId))
                .stream()
                .map(this::toRoleResponse)
                .toList();
    }

    @Transactional
    public Long createRole(SystemManageRequests.RoleSaveRequest request) {
        checkRoleCodeUnique(request.roleCode(), null);

        SysRoleEntity entity = new SysRoleEntity();
        entity.setRoleCode(request.roleCode());
        entity.setRoleName(request.roleName());
        entity.setDataScope(request.dataScope());
        entity.setStatus(request.status());
        entity.setRemark(request.remark());
        entity.setDeletedFlag(0);
        sysRoleMapper.insert(entity);
        saveRoleMenus(entity.getRoleId(), request.menuIds());
        return entity.getRoleId();
    }

    @Transactional
    public void updateRole(Long roleId, SystemManageRequests.RoleSaveRequest request) {
        SysRoleEntity entity = sysRoleMapper.selectById(roleId);
        if (entity == null) {
            throw new BusinessException("角色不存在");
        }

        checkRoleCodeUnique(request.roleCode(), roleId);
        entity.setRoleCode(request.roleCode());
        entity.setRoleName(request.roleName());
        entity.setDataScope(request.dataScope());
        entity.setStatus(request.status());
        entity.setRemark(request.remark());
        sysRoleMapper.updateById(entity);

        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenuEntity>().eq(SysRoleMenuEntity::getRoleId, roleId));
        saveRoleMenus(roleId, request.menuIds());
    }

    public List<SystemManageResponses.OptionResponse> listRoleOptions() {
        return listRoles().stream()
                .map(item -> new SystemManageResponses.OptionResponse(item.roleId(), item.roleName()))
                .toList();
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            throw new BusinessException("角色至少需要关联一个菜单或按钮权限");
        }

        for (Long menuId : menuIds) {
            SysRoleMenuEntity relation = new SysRoleMenuEntity();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            sysRoleMenuMapper.insert(relation);
        }
    }

    private void checkRoleCodeUnique(String roleCode, Long roleId) {
        SysRoleEntity duplicate = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getRoleCode, roleCode)
                .eq(SysRoleEntity::getDeletedFlag, 0)
                .last("limit 1"));

        if (duplicate != null && (roleId == null || !duplicate.getRoleId().equals(roleId))) {
            throw new BusinessException("角色编码已存在");
        }
    }

    private SystemManageResponses.RoleResponse toRoleResponse(SysRoleEntity entity) {
        List<Long> menuIds = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenuEntity>()
                        .eq(SysRoleMenuEntity::getRoleId, entity.getRoleId()))
                .stream()
                .map(SysRoleMenuEntity::getMenuId)
                .toList();

        return new SystemManageResponses.RoleResponse(
                entity.getRoleId(),
                entity.getRoleCode(),
                entity.getRoleName(),
                entity.getDataScope(),
                entity.getStatus(),
                entity.getRemark(),
                menuIds
        );
    }
}
