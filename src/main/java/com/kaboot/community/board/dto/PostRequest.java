package com.kaboot.community.board.dto;

public record PostRequest(
        String title,
        String content,
        String imageOriginalName,
        String imageUrl
) {
}
