package com.kaboot.community.domain.board.repository.board.custom;

import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.kaboot.community.domain.board.dto.response.BoardsResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.kaboot.community.domain.board.entity.QBoard.board;
import static com.kaboot.community.domain.board.entity.QComment.comment;
import static com.kaboot.community.domain.board.entity.QLikes.likes;
import static com.kaboot.community.domain.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class BoardCustomRepositoryImpl implements BoardCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public BoardDetailResponse getBoardDetailInfoById(Long boardId) {
        List<BoardDetailResponse.Comments> comments = fetchComments(boardId);
        BoardDetailResponse.BoardDetail boardDetail = fetchBoardDetailInfo(boardId);

        return BoardDetailResponse.builder()
                .boardDetail(boardDetail)
                .comments(comments)
                .build();
    }

    @Override
    public List<BoardsResponse.BoardSimpleInfo> getBoardSimpleInfo(Long cursor, int pageSize) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        BoardsResponse.BoardSimpleInfo.class,
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

    private List<BoardDetailResponse.Comments> fetchComments(Long boardId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        BoardDetailResponse.Comments.class,
                        comment.member.id.as("authorId"),
                        member.profileImgUrl.as("profileImg"),
                        member.nickname,
                        comment.modifiedAt.as("updatedAt"),
                        comment.content
                ))
                .from(comment)
                .leftJoin(member).on(comment.member.id.eq(member.id))
                .where(comment.board.id.eq(boardId))
                .fetch();
    }

    private BoardDetailResponse.BoardDetail fetchBoardDetailInfo(Long boardId) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        BoardDetailResponse.BoardDetail.class,
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
