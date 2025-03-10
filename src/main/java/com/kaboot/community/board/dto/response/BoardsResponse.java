package com.kaboot.community.board.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record BoardsResponse(
    List<BoardSimpleInfo> boardSimpleInfos,
    Long nextCursor
) {

    @Builder
    public record BoardSimpleInfo(
            Long boardId,
            String title,
            LocalDateTime createdAt,
            Integer likeCnt,
            Integer commentCnt,
            Integer viewCount,
            String authorNickname,
            String authorProfileImg
    ) {}
}
