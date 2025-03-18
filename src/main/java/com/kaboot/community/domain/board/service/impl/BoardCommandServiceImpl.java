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
import com.kaboot.community.domain.member.service.MemberQueryService;
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

    public void postBoard(String username, PostOrModifyRequest postRequest) {
        Member member = getMemberByUsername(username);
        Board board = BoardMapper.toBoardFromPostRequest(postRequest, member);

        boardRepository.save(board);
    }

    public void modifyBoard(String username, PostOrModifyRequest modifyRequest, Long boardId) {
        Member member = getMemberByUsername(username);
        Board validBoard = getBoardById(boardId);

        validBoard.validateSameMember(member.getId());
        validBoard.update(modifyRequest);
    }

    public void deleteBoard(String username,  Long boardId) {
        Member member = getMemberByUsername(username);
        Board validBoard = getBoardById(boardId);

        validBoard.validateSameMember(member.getId());
        boardRepository.delete(validBoard);
    }

    public void toggleLike(String username, Long boardId, LikeRequest likeRequest) {
        Member member = getMemberByUsername(username);
        Board validBoard = getBoardById(boardId);

        Long validBoardId = validBoard.getId();
        Long memberId = member.getId();

        if (!likeRequest.isLikeCancel()) {
            likesRepository.save(LikesMapper.toLikes(validBoard, member));
            return;
        }

        Likes likes = likesRepository.findByBoardIdAndMemberId(validBoardId, memberId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.LIKES_NOT_EXIST));

        likesRepository.delete(likes);
    }

    public void postComment(String username, Long boardId, CommentPostOrModifyRequest commentPostRequest) {
        Member member = getMemberByUsername(username);
        Board validBoard = getBoardById(boardId);

        commentRepository.save(CommentMapper.toEntity(validBoard, member, commentPostRequest));
    }

    public void modifyComment(String username, Long commentId, CommentPostOrModifyRequest commentModifyRequest) {
        Member member = getMemberByUsername(username);
        Comment validComment = getCommentById(commentId);

        validComment.validateSameMember(member.getId());
        validComment.updateComment(commentModifyRequest);
    }

    public void deleteComment(String username, Long commentId) {
        Member member = getMemberByUsername(username);
        Comment validComment = getCommentById(commentId);

        validComment.validateSameMember(member.getId());
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
