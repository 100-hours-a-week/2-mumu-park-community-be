package com.kaboot.community.board.service;

import com.kaboot.community.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.board.dto.request.LikeRequest;
import com.kaboot.community.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.board.dto.response.BoardDetailResponse;
import com.kaboot.community.board.dto.response.BoardsResponse;
import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.entity.Comment;
import com.kaboot.community.board.entity.Likes;
import com.kaboot.community.board.mapper.BoardMapper;
import com.kaboot.community.board.mapper.CommentMapper;
import com.kaboot.community.board.mapper.LikesMapper;
import com.kaboot.community.board.repository.CommentRepository;
import com.kaboot.community.board.repository.LikesRepository;
import com.kaboot.community.board.repository.board.BoardRepository;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.member.entity.Member;
import com.kaboot.community.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static com.kaboot.community.board.dto.response.BoardsResponse.*;

@Service
@RequiredArgsConstructor
public class BoardService {
    private static final Integer DEFAULT_PAGE_SIZE = 3;

    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public BoardsResponse getBoards(Long cursor) {
        List<BoardSimpleInfo> boardSimpleInfos = boardRepository.getBoardSimpleInfo(cursor, DEFAULT_PAGE_SIZE);

        Long nextCursor = boardSimpleInfos.isEmpty()
                ? null
                : boardSimpleInfos.getLast().boardId();

        return new BoardsResponse(boardSimpleInfos, nextCursor);
    }

    @Transactional(readOnly = true)
    public BoardDetailResponse getBoardDetail(Long boardId) {
        Board board = getBoardById(boardId);

        return boardRepository.getBoardDetailInfoById(board.getId());
    }

    @Transactional
    public void postBoard(String username, PostOrModifyRequest postRequest) {
        Member member = getMemberByUsername(username);
        Board board = BoardMapper.toBoardFromPostRequest(postRequest, member.getId());

        boardRepository.save(board);
    }

    @Transactional
    public void modifyBoard(String username, PostOrModifyRequest modifyRequest, Long boardId) {
        Member member = getMemberByUsername(username);
        Board board = getBoardById(boardId);

        if (isNotSameMember(board.getMemberId(), member.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        board.update(modifyRequest);
    }

    @Transactional
    public void deleteBoard(String username,  Long boardId) {
        Member member = getMemberByUsername(username);
        Board board = getBoardById(boardId);

        if (isNotSameMember(board.getMemberId(), member.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        boardRepository.delete(board);
    }

    @Transactional
    public void toggleLike(String username, Long boardId, LikeRequest likeRequest) {
        Member member = getMemberByUsername(username);
        Board board = getBoardById(boardId);

        Long existBoardID = board.getId();
        Long memberId = member.getId();

        if (!likeRequest.isLikeCancel()) {
            likesRepository.save(LikesMapper.toLikes(board.getId(), member.getId()));
            return;
        }

        Likes likes = likesRepository.findByBoardIdAndMemberId(existBoardID, memberId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.LIKES_NOT_EXIST));

        likesRepository.delete(likes);
    }

    @Transactional
    public void postComment(String username, Long boardId, CommentPostOrModifyRequest commentPostRequest) {
        Member member = getMemberByUsername(username);
        Board board = getBoardById(boardId);

        commentRepository.save(CommentMapper.toEntity(board.getId(), member.getId(), commentPostRequest));
    }

    @Transactional
    public void modifyComment(String username, Long commentId, CommentPostOrModifyRequest commentModifyRequest) {
        Member member = getMemberByUsername(username);
        Comment comment = getCommentById(commentId);

        if (!comment.isSameMember(member.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        comment.updateComment(commentModifyRequest);
    }

    @Transactional
    public void deleteComment(String username, Long commentId) {
        Member member = getMemberByUsername(username);
        Comment comment = getCommentById(commentId);

        if (!comment.isSameMember(member.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        comment.delete(LocalDateTime.now());
    }

    private Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));
    }

    private Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.BOARD_NOT_EXIST));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.COMMENT_NOT_EXIST));
    }

    private boolean isNotSameMember(Long boardWriterId, Long accessMemberId) {
        return !Objects.equals(boardWriterId, accessMemberId);
    }
}
