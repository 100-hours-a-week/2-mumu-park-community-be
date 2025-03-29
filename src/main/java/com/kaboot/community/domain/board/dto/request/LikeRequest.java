package com.kaboot.community.domain.board.dto.request;

import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Likes;
import com.kaboot.community.domain.member.entity.Member;

public record LikeRequest(
    boolean isLikeCancel
) {

  public Likes toEntity(Board board, Member member) {
    return Likes.builder()
        .board(board)
        .member(member)
        .build();
  }
}
