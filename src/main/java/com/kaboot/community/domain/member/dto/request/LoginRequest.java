package com.kaboot.community.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotEmpty
        @Email
        String username,

        @NotEmpty
        String password
) {
}
