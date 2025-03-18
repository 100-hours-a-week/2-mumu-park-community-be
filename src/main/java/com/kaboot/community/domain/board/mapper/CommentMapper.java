package com.kaboot.community.domain.board.mapper;

import com.kaboot.community.domain.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;
import com.kaboot.community.domain.member.entity.Member;

public class CommentMapper {

    public static Comment toEntity(Board board, Member member, CommentPostOrModifyRequest commentPostOrModifyRequest) {
        return Comment.builder()
                .board(board)
                .member(member)
                .content(commentPostOrModifyRequest.content())
                .build();
    }
}
