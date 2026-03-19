package com.acme.support.ticket.auth.controller;

import com.acme.support.ticket.auth.dto.AuthResponses;
import com.acme.support.ticket.auth.dto.LoginRequest;
import com.acme.support.ticket.auth.service.AuthFacadeService;
import com.acme.support.ticket.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录与认证控制器。
 */
@Tag(name = "认证中心")
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthFacadeService authFacadeService;

    public AuthController(AuthFacadeService authFacadeService) {
        this.authFacadeService = authFacadeService;
    }

    @Operation(summary = "账号登录")
    @PostMapping("/login")
    public ApiResponse<AuthResponses.LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authFacadeService.login(request.username(), request.password()));
    }
}
