package com.kaboot.community.config.jwt.dto;

import lombok.Builder;

@Builder
public record AuthTokens (
        String accessToken,
        String refreshToken
) {

    public static AuthTokens of(String accessToken, String refreshToken) {
        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}