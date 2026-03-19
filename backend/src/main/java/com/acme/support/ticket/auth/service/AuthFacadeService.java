package com.acme.support.ticket.auth.service;

import com.acme.support.ticket.auth.dto.AuthResponses;
import com.acme.support.ticket.mock.MockTicketDataService;
import com.acme.support.ticket.security.JwtTokenProvider;
import org.springframework.stereotype.Service;

/**
 * 登录认证门面服务。
 */
@Service
public class AuthFacadeService {

    private final MockTicketDataService mockTicketDataService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthFacadeService(MockTicketDataService mockTicketDataService, JwtTokenProvider jwtTokenProvider) {
        this.mockTicketDataService = mockTicketDataService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponses.LoginResponse login(String username, String password) {
        MockTicketDataService.MockUserAccount account = mockTicketDataService.findUser(username, password)
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

        AuthResponses.UserProfileResponse profile = mockTicketDataService.buildUserProfile(account);
        String token = jwtTokenProvider.createToken(account.userId(), account.username(), account.roles());

        return new AuthResponses.LoginResponse(token, "Bearer", profile);
    }
}
