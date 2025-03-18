package com.kaboot.community.domain.member.dto.request;

public record PasswordUpdateRequest(
        String prevPassword,
        String newPassword
) {
}
