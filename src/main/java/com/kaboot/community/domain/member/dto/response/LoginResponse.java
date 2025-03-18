package com.kaboot.community.domain.member.dto.response;

public record LoginResponse(
        String accessToken
) {

    public static LoginResponse from(String accessToken) {
        return new LoginResponse(accessToken);
    }
}
