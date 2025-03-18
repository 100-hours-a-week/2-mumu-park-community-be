package com.kaboot.community.domain.board.repository.board;

import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.repository.board.custom.BoardCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardCustomRepository {
}
