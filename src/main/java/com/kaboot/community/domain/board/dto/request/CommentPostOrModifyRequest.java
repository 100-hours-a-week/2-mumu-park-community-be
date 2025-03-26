package com.kaboot.community.domain.board.dto.request;

import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;
import com.kaboot.community.domain.member.entity.Member;
import jakarta.validation.constraints.NotEmpty;

public record CommentPostOrModifyRequest(
        @NotEmpty
        String content
) {
        public Comment toEntity(Board board, Member member) {
                return Comment.builder()
                    .board(board)
                    .member(member)
                    .content(content)
                    .build();
        }
}
