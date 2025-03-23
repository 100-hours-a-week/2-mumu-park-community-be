package com.kaboot.community.domain.board.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record PostOrModifyRequest(
        @NotEmpty
        String title,
        @NotEmpty
        String content,
        String imageOriginalName,
        String imageUrl
) {
}
