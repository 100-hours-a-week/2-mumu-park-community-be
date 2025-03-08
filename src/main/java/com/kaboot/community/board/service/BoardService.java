package com.kaboot.community.board.service;

import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.repository.board.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    public Board getBoardById(Long id) {
        return boardRepository.getBoardById(id)
                .orElseThrow(() -> new IllegalArgumentException("Board not exist"));
    }
}
