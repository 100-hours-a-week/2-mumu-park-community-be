package com.kaboot.community.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record ModifyRequest(
        @NotEmpty
        @Email
        String nickname,

        String profileImg
) {
}
