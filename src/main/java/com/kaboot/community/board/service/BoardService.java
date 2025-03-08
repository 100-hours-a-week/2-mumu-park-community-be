package com.kaboot.community.board.service;

import com.kaboot.community.board.dto.PostRequest;
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

@Service
@RequiredArgsConstructor
public class BoardService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    public Board getBoardById(Long id) {
        return boardRepository.getBoardById(id)
                .orElseThrow(() -> new IllegalArgumentException("Board not exist"));
    }

    @Transactional
    public void post(String username, PostRequest postRequest) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        Board board = BoardMapper.toBoardFromPostRequest(postRequest, member.getId());

        boardRepository.save(board);
    }
}
