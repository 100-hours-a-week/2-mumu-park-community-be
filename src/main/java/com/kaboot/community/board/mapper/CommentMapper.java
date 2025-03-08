package com.kaboot.community.board.mapper;

import com.kaboot.community.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.board.entity.Comment;

public class CommentMapper {

    public static Comment toEntity(Long boardId, Long memberId, CommentPostOrModifyRequest commentPostOrModifyRequest) {
        return Comment.builder()
                .boardId(boardId)
                .memberId(memberId)
                .content(commentPostOrModifyRequest.content())
                .build();
    }
}
