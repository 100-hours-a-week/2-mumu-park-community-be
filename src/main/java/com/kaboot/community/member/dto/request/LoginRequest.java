package com.kaboot.community.member.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}
