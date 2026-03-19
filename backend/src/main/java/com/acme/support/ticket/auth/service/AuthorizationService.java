package com.acme.support.ticket.auth.service;

import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.system.entity.HandleGroupMemberEntity;
import com.acme.support.ticket.system.entity.SysMenuEntity;
import com.acme.support.ticket.system.entity.SysRoleEntity;
import com.acme.support.ticket.system.entity.SysUserEntity;
import com.acme.support.ticket.system.mapper.HandleGroupMemberMapper;
import com.acme.support.ticket.system.mapper.SysMenuMapper;
import com.acme.support.ticket.system.mapper.SysRoleMapper;
import com.acme.support.ticket.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证授权聚合服务，负责加载用户、角色、菜单与权限信息。
 */
@Service
public class AuthorizationService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final HandleGroupMemberMapper handleGroupMemberMapper;

    public AuthorizationService(
            SysUserMapper sysUserMapper,
            SysRoleMapper sysRoleMapper,
            SysMenuMapper sysMenuMapper,
            HandleGroupMemberMapper handleGroupMemberMapper
    ) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.handleGroupMemberMapper = handleGroupMemberMapper;
    }

    public SysUserEntity loadEnabledUserByUsername(String username) {
        SysUserEntity user = sysUserMapper.findByUsername(username)
                .orElseThrow(() -> new BusinessException("账号不存在"));

        if (!"ENABLED".equals(user.getStatus())) {
            throw new BusinessException("账号已禁用");
        }

        return user;
    }

    public List<SysRoleEntity> listRoles(Long userId) {
        return sysRoleMapper.findRolesByUserId(userId);
    }

    public List<SysMenuEntity> listMenus(Long userId) {
        return sysMenuMapper.findMenusByUserId(userId);
    }

    public List<HandleGroupMemberEntity> listGroupMembers(Long userId) {
        return handleGroupMemberMapper.findByUserId(userId);
    }
}
