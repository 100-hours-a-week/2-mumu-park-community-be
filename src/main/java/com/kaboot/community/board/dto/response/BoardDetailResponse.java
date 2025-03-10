package com.kaboot.community.board.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BoardDetailResponse(
        BoardDetail boardDetail,
        List<Comments> comments
) {
    @Builder
    public record BoardDetail(
            Long boardId,
            String title,
            String content,
            String imgFileName,
            String contentImg,
            String authorProfileImg,
            LocalDateTime createdAt,
            Integer likeCnt,
            Integer commentCnt,
            Integer viewCount
    ) {}

    @Builder
    public record Comments(
            Long authorId,
            String profileImg,
            String nickname,
            LocalDateTime updatedAt,
            String content
    ) {}
}
