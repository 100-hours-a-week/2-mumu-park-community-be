package com.kaboot.community.board.repository.board.custom;

import com.kaboot.community.board.dto.response.BoardDetailResponse;
import com.kaboot.community.board.dto.response.BoardsResponse.BoardSimpleInfo;

import java.util.List;

public interface BoardCustomRepository {
    BoardDetailResponse getBoardDetailInfoById(Long boardId);

    List<BoardSimpleInfo> getBoardSimpleInfo(Long cursor, int pageSize);
}
