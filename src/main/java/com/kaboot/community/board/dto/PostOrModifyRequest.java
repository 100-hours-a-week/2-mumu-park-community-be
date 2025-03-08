package com.kaboot.community.board.dto;

public record PostOrModifyRequest(
        String title,
        String content,
        String imageOriginalName,
        String imageUrl
) {
}
