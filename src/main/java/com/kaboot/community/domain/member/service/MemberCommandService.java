package com.kaboot.community.domain.member.service;

import com.kaboot.community.config.jwt.dto.AuthTokens;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.dto.request.LoginRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;

public interface MemberCommandService {
    void register(RegisterRequest registerRequest);

    void login(LoginRequest loginRequest);
    AuthTokens loginV2(LoginRequest loginRequest);

    void update(String userEmail, ModifyRequest modifyRequest);

    void updatePassword(String userEmail, PasswordUpdateRequest passwordUpdateRequest);

    void withdrawal(String loggedInUsername);

    AuthTokens reissue(String refreshToken);

    void logout(String accessToken);
}
