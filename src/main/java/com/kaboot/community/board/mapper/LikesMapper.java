package com.kaboot.community.board.mapper;

import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.entity.Likes;
import com.kaboot.community.member.entity.Member;

public class LikesMapper {

    public static Likes toLikes(Board board, Member member) {
        return Likes.builder()
                .board(board)
                .member(member)
                .build();
    }
}
