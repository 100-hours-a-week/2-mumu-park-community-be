package com.kaboot.community.domain.board.service.impl;

import static com.kaboot.community.domain.board.dto.response.BoardsResponse.BoardSimpleInfo;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.kaboot.community.domain.board.dto.response.BoardLikeResponse;
import com.kaboot.community.domain.board.dto.response.BoardsResponse;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;
import com.kaboot.community.domain.board.repository.board.BoardRepository;
import com.kaboot.community.domain.board.repository.comment.CommentRepository;
import com.kaboot.community.domain.board.repository.likes.LikesRepository;
import com.kaboot.community.domain.board.service.BoardQueryService;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardQueryServiceImpl implements BoardQueryService {

  private static final Integer DEFAULT_PAGE_SIZE = 3;

  private final MemberQueryService memberQueryService;

  private final BoardRepository boardRepository;
  private final LikesRepository likesRepository;
  private final CommentRepository commentRepository;

  @Override
  public BoardsResponse getBoards(Long cursor) {
    List<BoardSimpleInfo> boardSimpleInfos = boardRepository.getBoardSimpleInfo(cursor,
        DEFAULT_PAGE_SIZE);

    Long nextCursor = boardSimpleInfos.isEmpty()
        ? null
        : boardSimpleInfos.getLast().boardId();

    return new BoardsResponse(boardSimpleInfos, nextCursor);
  }

  @Override
  @Transactional
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

  @Override
  public BoardLikeResponse isBoardLikeByUser(String authUsername, Long boardId) {
    Member memberByUsername = memberQueryService.getMemberByUsername(authUsername);

    return likesRepository.isBoardLikedByUser(memberByUsername.getId(), boardId);
  }
}
