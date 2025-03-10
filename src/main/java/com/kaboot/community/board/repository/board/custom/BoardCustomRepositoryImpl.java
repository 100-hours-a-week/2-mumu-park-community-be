package com.kaboot.community.board.repository.board.custom;

import com.kaboot.community.board.dto.response.BoardsResponse;
import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.entity.QBoard;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.kaboot.community.board.entity.QBoard.board;
import static com.kaboot.community.board.entity.QComment.comment;
import static com.kaboot.community.board.entity.QLikes.likes;
import static com.kaboot.community.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class BoardCustomRepositoryImpl implements BoardCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Board> getBoardById(Long id) {
        Board board = jpaQueryFactory.selectFrom(QBoard.board)
                .where(QBoard.board.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(board);
    }

    @Override
    public List<BoardsResponse.BoardSimpleInfo> getBoardSimpleInfo(Long cursor, int pageSize) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        BoardsResponse.BoardSimpleInfo.class,
                        board.id.as("boardId"),
                        board.title,
                        board.createdAt,
                        JPAExpressions.select(likes.id.count().intValue())
                                .from(likes)
                                .where(likes.boardId.eq(board.id)),
                        JPAExpressions.select(comment.id.count().intValue())
                                .from(comment)
                                .where(comment.boardId.eq(board.id)),
                        board.viewCount,
                        member.nickname.as("authorNickname"),
                        member.profileImgUrl.as("authorProfileImg")
                ))
                .from(board)
                .leftJoin(member).on(board.memberId.eq(member.id))
                .leftJoin(likes).on(board.id.eq(likes.boardId))
                .leftJoin(comment).on(board.id.eq(comment.boardId))
                .groupBy(board.id, member.nickname, member.profileImgUrl)
                .where(cursor == null ? null : board.id.lt(cursor))
                .orderBy(board.id.desc())
                .limit(pageSize)
                .fetch();
    }
}
