package com.kaboot.community.board.service;

import com.kaboot.community.board.dto.response.BoardDetailResponse;
import com.kaboot.community.board.dto.response.BoardsResponse;
import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.entity.Comment;

public interface BoardQueryService {

    BoardsResponse getBoards(Long cursor);
    BoardDetailResponse getBoardDetail(Long boardId);
    Board getBoardById(Long boardId);
    Comment getCommentById(Long commentId);
}
