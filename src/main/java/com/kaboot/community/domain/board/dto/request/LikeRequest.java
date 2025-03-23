package com.kaboot.community.domain.board.dto.request;

import jakarta.validation.constraints.NotEmpty;

public record LikeRequest(
        @NotEmpty
        boolean isLikeCancel
) {
}
