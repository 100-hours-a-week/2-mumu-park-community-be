package com.kaboot.community.domain.board.repository.board.custom;

import static com.kaboot.community.domain.board.dto.response.BoardDetailResponse.BoardDetail;
import static com.kaboot.community.domain.board.dto.response.BoardDetailResponse.Comments;
import static com.kaboot.community.domain.board.dto.response.BoardDetailResponse.builder;
import static com.kaboot.community.domain.board.dto.response.BoardsResponse.BoardSimpleInfo;
import static com.kaboot.community.domain.board.entity.QBoard.board;
import static com.kaboot.community.domain.board.entity.QComment.comment;
import static com.kaboot.community.domain.board.entity.QLikes.likes;
import static com.kaboot.community.domain.member.entity.QMember.member;

import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardCustomRepositoryImpl implements BoardCustomRepository {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public BoardDetailResponse getBoardDetailInfoById(Long boardId) {
    List<Comments> comments = fetchComments(boardId);
    BoardDetail boardDetail = fetchBoardDetailInfo(boardId);

    return builder()
        .boardDetail(boardDetail)
        .comments(comments)
        .build();
  }

  @Override
  public List<BoardSimpleInfo> getBoardSimpleInfo(Long cursor, int pageSize) {
    return jpaQueryFactory
        .select(Projections.constructor(
            BoardSimpleInfo.class,
            board.id,
            board.title,
            board.createdAt,
            JPAExpressions.select(likes.id.count().intValue())
                .from(likes)
                .where(likes.board.id.eq(board.id)),
            JPAExpressions.select(comment.id.count().intValue())
                .from(comment)
                .where(comment.board.id.eq(board.id)),
            board.viewCount,
            member.nickname,
            member.profileImgUrl
        ))
        .from(board)
        .leftJoin(member).on(board.member.id.eq(member.id))
        .leftJoin(likes).on(board.id.eq(likes.board.id))
        .leftJoin(comment).on(board.id.eq(comment.board.id))
        .groupBy(board.id, member.nickname, member.profileImgUrl)
        .where(cursor == null ? null : board.id.lt(cursor))
        .orderBy(board.id.desc())
        .limit(pageSize)
        .fetch();
  }

  private List<Comments> fetchComments(Long boardId) {
    return jpaQueryFactory
        .select(Projections.constructor(
            Comments.class,
            comment.member.id,
            member.profileImgUrl,
            member.nickname,
            comment.modifiedAt,
            comment.content
        ))
        .from(comment)
        .leftJoin(member).on(comment.member.id.eq(member.id))
        .where(comment.board.id.eq(boardId))
        .orderBy(comment.id.asc())
        .fetch();
  }

  private BoardDetail fetchBoardDetailInfo(Long boardId) {
    return jpaQueryFactory
        .select(Projections.constructor(
            BoardDetail.class,
            board.id,
            board.title,
            board.content,
            board.imageOriginalName,
            board.imgUrl,
            member.profileImgUrl,
            board.createdAt,
            JPAExpressions.select(likes.id.count().intValue())
                .from(likes)
                .where(likes.board.id.eq(boardId)),
            JPAExpressions.select(comment.count().intValue())
                .from(comment)
                .where(comment.board.id.eq(boardId)),
            board.viewCount
        ))
        .from(board)
        .leftJoin(member).on(board.member.id.eq(member.id))
        .where(board.id.eq(boardId))
        .fetchOne();
  }
}
