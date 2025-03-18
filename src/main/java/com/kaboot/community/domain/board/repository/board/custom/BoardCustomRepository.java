package com.kaboot.community.domain.board.repository.board.custom;

import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.kaboot.community.domain.board.dto.response.BoardsResponse;

import java.util.List;

public interface BoardCustomRepository {
    BoardDetailResponse getBoardDetailInfoById(Long boardId);

    List<BoardsResponse.BoardSimpleInfo> getBoardSimpleInfo(Long cursor, int pageSize);
}
