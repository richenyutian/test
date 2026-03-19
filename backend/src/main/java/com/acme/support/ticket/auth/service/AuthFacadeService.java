package com.acme.support.ticket.auth.service;

import com.acme.support.ticket.auth.dto.AuthResponses;
import com.acme.support.ticket.common.exception.BusinessException;
import com.acme.support.ticket.security.JwtTokenProvider;
import com.acme.support.ticket.security.LoginUserPrincipal;
import com.acme.support.ticket.system.entity.SysMenuEntity;
import com.acme.support.ticket.system.entity.SysRoleEntity;
import com.acme.support.ticket.system.entity.SysUserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登录认证门面服务。
 */
@Service
public class AuthFacadeService {

    private final AuthorizationService authorizationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthFacadeService(
            AuthorizationService authorizationService,
            JwtTokenProvider jwtTokenProvider,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.authorizationService = authorizationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public AuthResponses.LoginResponse login(String username) {
        SysUserEntity user = authorizationService.loadEnabledUserByUsername(username);
        List<SysRoleEntity> roles = authorizationService.listRoles(user.getUserId());
        List<SysMenuEntity> menus = authorizationService.listMenus(user.getUserId());

        if (roles.isEmpty()) {
            throw new BusinessException("当前账号未分配角色，无法登录");
        }

        List<String> roleCodes = roles.stream().map(SysRoleEntity::getRoleCode).toList();
        List<String> permissionCodes = menus.stream()
                .map(SysMenuEntity::getPermissionCode)
                .filter(permissionCode -> permissionCode != null && !permissionCode.isBlank())
                .distinct()
                .toList();

        AuthResponses.UserProfileResponse profile = buildProfile(user, roles, menus);
        String token = jwtTokenProvider.createToken(user.getUserId(), user.getUsername(), roleCodes, permissionCodes);
        return new AuthResponses.LoginResponse(token, "Bearer", profile);
    }

    public void logout(String token) {
        tokenBlacklistService.blacklist(token, jwtTokenProvider.getRemainingTtl(token));
    }

    public AuthResponses.UserProfileResponse getProfile(LoginUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException("未获取到当前登录用户");
        }

        SysUserEntity user = authorizationService.loadEnabledUserByUsername(principal.username());
        return buildProfile(
                user,
                authorizationService.listRoles(user.getUserId()),
                authorizationService.listMenus(user.getUserId())
        );
    }

    private AuthResponses.UserProfileResponse buildProfile(
            SysUserEntity user,
            List<SysRoleEntity> roles,
            List<SysMenuEntity> menus
    ) {
        List<String> menuPermissions = menus.stream()
                .filter(menu -> "MENU".equals(menu.getMenuType()))
                .map(SysMenuEntity::getPermissionCode)
                .filter(permissionCode -> permissionCode != null && !permissionCode.isBlank())
                .distinct()
                .toList();

        List<String> buttonPermissions = menus.stream()
                .filter(menu -> "BUTTON".equals(menu.getMenuType()))
                .map(SysMenuEntity::getPermissionCode)
                .filter(permissionCode -> permissionCode != null && !permissionCode.isBlank())
                .distinct()
                .toList();

        String dataScope = roles.stream()
                .map(SysRoleEntity::getDataScope)
                .distinct()
                .findFirst()
                .orElse("SELF_CREATED");

        return new AuthResponses.UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getUserType(),
                roles.stream().map(SysRoleEntity::getRoleCode).distinct().toList(),
                menuPermissions,
                buttonPermissions,
                dataScope
        );
    }
}
