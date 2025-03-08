package com.kaboot.community.board.mapper;

import com.kaboot.community.board.entity.Likes;

public class LikesMapper {

    public static Likes toLikes(Long boardId, Long memberId) {
        return Likes.builder()
                .boardId(boardId)
                .memberId(memberId)
                .build();
    }
}
