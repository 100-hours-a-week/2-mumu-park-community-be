package com.kaboot.community.board.service;

import com.kaboot.community.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.board.dto.request.LikeRequest;
import com.kaboot.community.board.dto.request.PostOrModifyRequest;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void post(String username, PostOrModifyRequest postRequest) {
        Member member = findMemberByUsername(username);
        Board board = BoardMapper.toBoardFromPostRequest(postRequest, member.getId());

        boardRepository.save(board);
    }

    @Transactional
    public void modifyBoard(String username, PostOrModifyRequest modifyRequest, Long boardId) {
        Member member = findMemberByUsername(username);
        Board board = getBoardById(boardId);

        if (isNotSameMember(board.getMemberId(), member.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        board.update(modifyRequest);
    }

    @Transactional
    public void deleteBoard(String username,  Long boardId) {
        Member member = findMemberByUsername(username);
        Board board = getBoardById(boardId);

        if (isNotSameMember(board.getMemberId(), member.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        boardRepository.delete(board);
    }

    @Transactional
    public void toggleLike(String username, Long boardId, LikeRequest likeRequest) {
        Member member = findMemberByUsername(username);
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
        Member member = findMemberByUsername(username);
        Board board = getBoardById(boardId);

        commentRepository.save(CommentMapper.toEntity(board.getId(), member.getId(), commentPostRequest));
    }

    @Transactional
    public void modifyComment(String username, Long commentId, CommentPostOrModifyRequest commentModifyRequest) {
        Member member = findMemberByUsername(username);
        Comment comment = getCommentById(commentId);

        if (!comment.isSameMember(member.getId())) {
            throw new CustomException(CustomResponseStatus.UNAUTHORIZED_REQUEST);
        }

        comment.updateComment(commentModifyRequest);
    }

    private Member findMemberByUsername(String username) {
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
