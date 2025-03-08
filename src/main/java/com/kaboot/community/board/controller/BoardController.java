package com.kaboot.community.board.controller;

import com.kaboot.community.board.dto.PostOrModifyRequest;
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

    @PostMapping()
    public ResponseEntity<ApiResponse<Void>> post(
            HttpServletRequest request,
            @RequestBody PostOrModifyRequest postRequest
    ) {
        String loggedInUserEmail = SessionUtil.getLoggedInUsername(request);
        boardService.post(loggedInUserEmail, postRequest);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> modifyBoard(
            HttpServletRequest request,
            @RequestBody PostOrModifyRequest modifyRequest,
            @PathVariable Long boardId
    ) {
        String loggedInUserEmail = SessionUtil.getLoggedInUsername(request);
        boardService.modifyBoard(loggedInUserEmail, modifyRequest, boardId);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> modifyBoard(
            HttpServletRequest request,
            @PathVariable Long boardId
    ) {
        String loggedInUserEmail = SessionUtil.getLoggedInUsername(request);
        boardService.deleteBoard(loggedInUserEmail, boardId);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }
}
