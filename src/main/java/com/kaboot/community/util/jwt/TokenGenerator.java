package com.kaboot.community.util.jwt;

import com.kaboot.community.config.jwt.dto.AuthTokens;
import com.kaboot.community.config.jwt.enums.TokenType;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenGenerator {
    private final JwtUtil jwtUtil;

    public AuthTokens generateTokenWithoutRFToken(Long id, RoleType roleType) {
        String accessToken = jwtUtil.createToken(id, TokenType.ACCESS_TOKEN, roleType);
        String refreshToken = jwtUtil.createToken(id, TokenType.REFRESH_TOKEN, roleType);

        return AuthTokens.of(accessToken, refreshToken);
    }

    public AuthTokens generateTokenWithRFToken(Long id, String refreshToken, RoleType roleType) {

        String accessToken = jwtUtil.createToken(id, TokenType.ACCESS_TOKEN, roleType);
        return AuthTokens.of(accessToken, refreshToken);
    }
}
