package com.kaboot.community.board.repository.board.custom;

import com.kaboot.community.board.dto.response.BoardsResponse;
import com.kaboot.community.board.dto.response.BoardsResponse.BoardSimpleInfo;
import com.kaboot.community.board.entity.Board;

import java.util.List;
import java.util.Optional;

public interface BoardCustomRepository {

    Optional<Board> getBoardById(Long id);
    List<BoardSimpleInfo> getBoardSimpleInfo(Long cursor, int pageSize);
}
