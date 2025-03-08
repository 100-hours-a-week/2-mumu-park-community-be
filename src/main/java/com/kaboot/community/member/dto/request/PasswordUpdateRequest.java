package com.kaboot.community.member.dto.request;

public record PasswordUpdateRequest(
        String prevPassword,
        String newPassword
) {
}
