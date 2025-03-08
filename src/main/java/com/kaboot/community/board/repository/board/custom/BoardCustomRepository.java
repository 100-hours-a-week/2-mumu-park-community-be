package com.kaboot.community.board.repository.board.custom;

import com.kaboot.community.board.entity.Board;

import java.util.Optional;

public interface BoardCustomRepository {

    Optional<Board> getBoardById(Long id);
}
