package com.kaboot.community.domain.member.service.auth;

import com.kaboot.community.config.jwt.dto.AuthTokens;
import com.kaboot.community.domain.member.dto.request.LoginRequest;
import com.kaboot.community.domain.member.dto.request.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest registerRequest);
    AuthTokens loginV2(LoginRequest loginRequest);

    AuthTokens reissue(String refreshToken);
    void logout(String accessToken);

}
