package com.kaboot.community.domain.member.dto.request;

public record LoginRequest(
        String username,
        String password
) {
}
