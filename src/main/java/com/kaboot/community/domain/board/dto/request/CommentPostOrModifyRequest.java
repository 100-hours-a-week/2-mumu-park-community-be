package com.kaboot.community.domain.board.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record CommentPostOrModifyRequest(
        @NotEmpty
        String content
) {
}
