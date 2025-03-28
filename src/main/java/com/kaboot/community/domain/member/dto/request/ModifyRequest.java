package com.kaboot.community.domain.member.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record ModifyRequest(
    @NotEmpty
    String nickname,

    String profileImg
) {

}
