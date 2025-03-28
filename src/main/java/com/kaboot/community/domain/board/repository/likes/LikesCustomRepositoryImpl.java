package com.kaboot.community.domain.board.repository.likes;

import static com.kaboot.community.domain.board.entity.QLikes.likes;

import com.kaboot.community.domain.board.dto.response.BoardLikeResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LikesCustomRepositoryImpl implements LikesCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;


  @Override
  public BoardLikeResponse isBoardLikedByUser(Long memberId, Long boardId) {
    boolean isLike = jpaQueryFactory
        .selectOne()
        .from(likes)
        .where(likes.member.id.eq(memberId),
            likes.board.id.eq(boardId))
        .fetchFirst() != null;

    return new BoardLikeResponse(isLike);
  }
}
