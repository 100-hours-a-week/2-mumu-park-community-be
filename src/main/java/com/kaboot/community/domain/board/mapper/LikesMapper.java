package com.kaboot.community.domain.board.mapper;

import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Likes;
import com.kaboot.community.domain.member.entity.Member;

public class LikesMapper {

    public static Likes toLikes(Board board, Member member) {
        return Likes.builder()
                .board(board)
                .member(member)
                .build();
    }
}
