package com.kaboot.community.board.controller;

import com.kaboot.community.board.dto.PostRequest;
import com.kaboot.community.board.entity.Board;
import com.kaboot.community.board.service.BoardService;
import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.util.SessionUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable Long id) {
        Board board = boardService.getBoardById(id);

        return ResponseEntity.ok().body(board);
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<?>> post(
            HttpServletRequest request,
            @RequestBody PostRequest postRequest
    ) {
        String loggedInUserEmail = SessionUtil.getLoggedInUsername(request);
        boardService.post(loggedInUserEmail, postRequest);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }
}
