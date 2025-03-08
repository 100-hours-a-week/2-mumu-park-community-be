package com.kaboot.community.board.repository.board.custom;

import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.entity.QBoard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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
}
