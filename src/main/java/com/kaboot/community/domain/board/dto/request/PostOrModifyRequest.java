package com.kaboot.community.domain.board.dto.request;

public record PostOrModifyRequest(
        String title,
        String content,
        String imageOriginalName,
        String imageUrl
) {
}
