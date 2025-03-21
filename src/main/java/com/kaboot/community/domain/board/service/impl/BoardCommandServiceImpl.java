package com.kaboot.community.domain.board.service.impl;

import com.kaboot.community.domain.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.domain.board.dto.request.LikeRequest;
import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;
import com.kaboot.community.domain.board.entity.Likes;
import com.kaboot.community.domain.board.mapper.BoardMapper;
import com.kaboot.community.domain.board.mapper.CommentMapper;
import com.kaboot.community.domain.board.mapper.LikesMapper;
import com.kaboot.community.domain.board.repository.comment.CommentRepository;
import com.kaboot.community.domain.board.repository.likes.LikesRepository;
import com.kaboot.community.domain.board.repository.board.BoardRepository;
import com.kaboot.community.domain.board.service.BoardCommandService;
import com.kaboot.community.domain.board.service.BoardQueryService;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardCommandServiceImpl implements BoardCommandService {
    private final BoardQueryService boardQueryService;
    private final MemberQueryService memberQueryService;

    private final BoardRepository boardRepository;
    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;

    public void postBoard(String authUsername, PostOrModifyRequest postRequest) {
        Member validMember = getMemberByUsername(authUsername);
        Board board = BoardMapper.toBoardFromPostRequest(postRequest, validMember);

        boardRepository.save(board);
    }

    public void modifyBoard(String authUsername, PostOrModifyRequest modifyRequest, Long boardId) {
        Member validMember = getMemberByUsername(authUsername);
        Board validBoard = getBoardById(boardId);

        validBoard.validateSameMember(validMember.getId());
        validBoard.update(modifyRequest);
    }

    public void deleteBoard(String authUsername,  Long boardId) {
        Member validMember = getMemberByUsername(authUsername);
        Board validBoard = getBoardById(boardId);

        validBoard.validateSameMember(validMember.getId());
        boardRepository.delete(validBoard);
    }

    public void toggleLike(String authUsername, Long boardId, LikeRequest likeRequest) {
        Member validMember = getMemberByUsername(authUsername);
        Board validBoard = getBoardById(boardId);

        Long validBoardId = validBoard.getId();
        Long memberId = validMember.getId();

        if (!likeRequest.isLikeCancel()) {
            likesRepository.save(LikesMapper.toLikes(validBoard, validMember));
            return;
        }

        Likes likes = likesRepository.findByBoardIdAndMemberId(validBoardId, memberId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.LIKES_NOT_EXIST));

        likesRepository.delete(likes);
    }

    public void postComment(String authUsername, Long boardId, CommentPostOrModifyRequest commentPostRequest) {
        Member validMember = getMemberByUsername(authUsername);
        Board validBoard = getBoardById(boardId);

        commentRepository.save(CommentMapper.toEntity(validBoard, validMember, commentPostRequest));
    }

    public void modifyComment(String authUsername, Long commentId, CommentPostOrModifyRequest commentModifyRequest) {
        Member validMember = getMemberByUsername(authUsername);
        Comment validComment = getCommentById(commentId);

        validComment.validateSameMember(validMember.getId());
        validComment.updateComment(commentModifyRequest);
    }

    public void deleteComment(String authUsername, Long commentId) {
        Member validMember = getMemberByUsername(authUsername);
        Comment validComment = getCommentById(commentId);

        validComment.validateSameMember(validMember.getId());
        validComment.delete(LocalDateTime.now());
    }

    private Member getMemberByUsername(String username) {
        return memberQueryService.getMemberByUsername(username);
    }

    private Board getBoardById(Long boardId) {
        return boardQueryService.getBoardById(boardId);
    }

    private Comment getCommentById(Long commentId) {
        return boardQueryService.getCommentById(commentId);
    }
}
