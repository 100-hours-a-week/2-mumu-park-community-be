package com.kaboot.community.board.service;

import com.kaboot.community.board.dto.PostOrModifyRequest;
import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.mapper.BoardMapper;
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
    
    private Member findMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));
    }

    private Board getBoardById(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));
    }

    private boolean isNotSameMember(Long boardWriterId, Long accessMemberId) {
        return !Objects.equals(boardWriterId, accessMemberId);
    }
}
