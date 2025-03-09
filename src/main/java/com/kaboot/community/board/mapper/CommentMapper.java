package com.kaboot.community.board.mapper;

import com.kaboot.community.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.board.dto.response.BoardDetailResponse;
import com.kaboot.community.board.entity.Comment;
import com.kaboot.community.member.entity.Member;

public class CommentMapper {

    public static Comment toEntity(Long boardId, Long memberId, CommentPostOrModifyRequest commentPostOrModifyRequest) {
        return Comment.builder()
                .boardId(boardId)
                .memberId(memberId)
                .content(commentPostOrModifyRequest.content())
                .build();
    }

    public static BoardDetailResponse.Comments toCommentsDto(Member writer, Comment comment) {
        return BoardDetailResponse.Comments.builder()
                .authorId(writer.getId())
                .profileImg(writer.getProfileImgUrl())
                .nickname(writer.getNickname())
                .updatedAt(comment.getModifiedAt())
                .content(comment.getContent())
                .build();
    }
}
