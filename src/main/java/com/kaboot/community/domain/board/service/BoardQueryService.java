package com.kaboot.community.domain.board.service;

import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.kaboot.community.domain.board.dto.response.BoardsResponse;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;

public interface BoardQueryService {

    BoardsResponse getBoards(Long cursor);
    BoardDetailResponse getBoardDetail(Long boardId);
    Board getBoardById(Long boardId);
    Comment getCommentById(Long commentId);
}
