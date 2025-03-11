package com.kaboot.community.board.mapper;

import com.kaboot.community.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.entity.Comment;
import com.kaboot.community.member.entity.Member;

public class CommentMapper {

    public static Comment toEntity(Board board, Member member, CommentPostOrModifyRequest commentPostOrModifyRequest) {
        return Comment.builder()
                .board(board)
                .member(member)
                .content(commentPostOrModifyRequest.content())
                .build();
    }
}
