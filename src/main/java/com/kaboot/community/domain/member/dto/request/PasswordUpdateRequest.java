package com.kaboot.community.domain.member.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record PasswordUpdateRequest(
        @NotEmpty
        String newPassword
) {
}
