package com.acme.support.ticket.system.service;

import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.system.dto.SystemManageRequests;
import com.acme.support.ticket.system.dto.SystemManageResponses;
import com.acme.support.ticket.system.entity.SysMenuEntity;
import com.acme.support.ticket.system.mapper.SysMenuMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单管理服务。
 */
@Service
public class SystemMenuService {

    private final SysMenuMapper sysMenuMapper;

    public SystemMenuService(SysMenuMapper sysMenuMapper) {
        this.sysMenuMapper = sysMenuMapper;
    }

    public List<SystemManageResponses.MenuResponse> listMenuTree() {
        List<SysMenuEntity> menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
                .eq(SysMenuEntity::getDeletedFlag, 0)
                .orderByAsc(SysMenuEntity::getSortOrder)
                .orderByAsc(SysMenuEntity::getMenuId));

        return buildChildren(0L, menus);
    }

    public List<SystemManageResponses.OptionResponse> listMenuOptions() {
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
                        .eq(SysMenuEntity::getDeletedFlag, 0)
                        .orderByAsc(SysMenuEntity::getSortOrder))
                .stream()
                .map(item -> new SystemManageResponses.OptionResponse(item.getMenuId(), item.getMenuName()))
                .toList();
    }

    public Long createMenu(SystemManageRequests.MenuSaveRequest request) {
        checkPermissionCodeUnique(request.permissionCode(), null);

        SysMenuEntity entity = new SysMenuEntity();
        fillMenu(entity, request);
        entity.setDeletedFlag(0);
        sysMenuMapper.insert(entity);
        return entity.getMenuId();
    }

    public void updateMenu(Long menuId, SystemManageRequests.MenuSaveRequest request) {
        SysMenuEntity entity = sysMenuMapper.selectById(menuId);
        if (entity == null) {
            throw new BusinessException("菜单不存在");
        }

        checkPermissionCodeUnique(request.permissionCode(), menuId);
        fillMenu(entity, request);
        sysMenuMapper.updateById(entity);
    }

    private void fillMenu(SysMenuEntity entity, SystemManageRequests.MenuSaveRequest request) {
        entity.setParentId(request.parentId());
        entity.setMenuName(request.menuName());
        entity.setMenuType(request.menuType());
        entity.setRoutePath(request.routePath());
        entity.setComponentPath(request.componentPath());
        entity.setPermissionCode(request.permissionCode());
        entity.setIcon(request.icon());
        entity.setSortOrder(request.sortOrder());
        entity.setVisibleFlag(request.visibleFlag());
        entity.setEnabledFlag(request.enabledFlag());
        entity.setKeepAliveFlag(request.keepAliveFlag());
        entity.setRemark(request.remark());
    }

    private void checkPermissionCodeUnique(String permissionCode, Long menuId) {
        if (permissionCode == null || permissionCode.isBlank()) {
            return;
        }

        SysMenuEntity duplicate = sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenuEntity>()
                .eq(SysMenuEntity::getPermissionCode, permissionCode)
                .eq(SysMenuEntity::getDeletedFlag, 0)
                .last("limit 1"));

        if (duplicate != null && (menuId == null || !duplicate.getMenuId().equals(menuId))) {
            throw new BusinessException("权限标识已存在");
        }
    }

    private List<SystemManageResponses.MenuResponse> buildChildren(Long parentId, List<SysMenuEntity> allMenus) {
        List<SystemManageResponses.MenuResponse> results = new ArrayList<>();

        for (SysMenuEntity menu : allMenus) {
            if (!parentId.equals(menu.getParentId())) {
                continue;
            }

            results.add(new SystemManageResponses.MenuResponse(
                    menu.getMenuId(),
                    menu.getParentId(),
                    menu.getMenuName(),
                    menu.getMenuType(),
                    menu.getRoutePath(),
                    menu.getComponentPath(),
                    menu.getPermissionCode(),
                    menu.getIcon(),
                    menu.getSortOrder(),
                    menu.getVisibleFlag(),
                    menu.getEnabledFlag(),
                    menu.getKeepAliveFlag(),
                    menu.getRemark(),
                    buildChildren(menu.getMenuId(), allMenus)
            ));
        }

        return results;
    }
}
