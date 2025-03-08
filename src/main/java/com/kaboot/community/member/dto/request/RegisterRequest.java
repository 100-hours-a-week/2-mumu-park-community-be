package com.kaboot.community.member.dto.request;

public record RegisterRequest(
        String email,
        String password,
        String nickname,
        String profileImage
) {
}
