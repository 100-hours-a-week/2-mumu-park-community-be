package com.kaboot.community.board.repository.board;

import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.repository.board.custom.BoardCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardCustomRepository {
}
