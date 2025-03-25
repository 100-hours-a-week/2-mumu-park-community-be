package com.kaboot.community.domain.board.service.impl;

import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.kaboot.community.domain.board.dto.response.BoardsResponse;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;
import com.kaboot.community.domain.board.repository.comment.CommentRepository;
import com.kaboot.community.domain.board.repository.board.BoardRepository;
import com.kaboot.community.domain.board.service.BoardQueryService;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.kaboot.community.domain.board.dto.response.BoardsResponse.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardQueryServiceImpl implements BoardQueryService {
    private static final Integer DEFAULT_PAGE_SIZE = 3;

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    @Override
    public BoardsResponse getBoards(Long cursor) {
        List<BoardSimpleInfo> boardSimpleInfos = boardRepository.getBoardSimpleInfo(cursor, DEFAULT_PAGE_SIZE);

        Long nextCursor = boardSimpleInfos.isEmpty()
                ? null
                : boardSimpleInfos.getLast().boardId();

        return new BoardsResponse(boardSimpleInfos, nextCursor);
    }

    @Override
    public BoardDetailResponse getBoardDetail(Long boardId) {
        Board board = getBoardById(boardId);

        board.increaseViewCount();
        return boardRepository.getBoardDetailInfoById(board.getId());
    }

    @Override
    public Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.BOARD_NOT_EXIST));
    }

    @Override
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.COMMENT_NOT_EXIST));
    }
}
