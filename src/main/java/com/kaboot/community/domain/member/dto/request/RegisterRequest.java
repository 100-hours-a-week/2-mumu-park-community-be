package com.kaboot.community.domain.member.dto.request;

public record RegisterRequest(
        String email,
        String password,
        String nickname,
        String profileImage
) {
}
