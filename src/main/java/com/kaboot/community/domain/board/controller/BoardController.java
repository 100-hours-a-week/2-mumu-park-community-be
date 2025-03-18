package com.kaboot.community.domain.board.controller;

import com.kaboot.community.config.security.member.PrincipalDetails;
import com.kaboot.community.domain.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.domain.board.dto.request.LikeRequest;
import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.kaboot.community.domain.board.dto.response.BoardsResponse;
import com.kaboot.community.domain.board.service.BoardCommandService;
import com.kaboot.community.domain.board.service.BoardQueryService;
import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardQueryService boardQueryService;
    private final BoardCommandService boardCommandService;

    @GetMapping()
    public ResponseEntity<ApiResponse<BoardsResponse>> getBoards(
            @RequestParam(value = "cursor", required = false) Long cursor
    ) {
        BoardsResponse response = boardQueryService.getBoards(cursor);

        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardDetailResponse>> getBoardDetail(
            @PathVariable Long boardId
    ) {
        BoardDetailResponse response = boardQueryService.getBoardDetail(boardId);

        return ResponseEntity.ok().body(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<Void>> post(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody PostOrModifyRequest postRequest
    ) {
        boardCommandService.postBoard(
                principalDetails.getUsername(),
                postRequest
        );

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> modifyBoard(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody PostOrModifyRequest modifyRequest,
            @PathVariable Long boardId
    ) {
        boardCommandService.modifyBoard(
                principalDetails.getUsername(),
                modifyRequest,
                boardId
        );

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long boardId
    ) {
        boardCommandService.deleteBoard(
                principalDetails.getUsername(),
                boardId
        );

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PatchMapping("/{boardId}/likes")
    public ResponseEntity<ApiResponse<Void>> toggleLike(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long boardId,
            @RequestBody LikeRequest likeRequest
    ) {
        boardCommandService.toggleLike(
                principalDetails.getUsername(),
                boardId, likeRequest
        );

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PostMapping("/{boardId}/comments")
    public ResponseEntity<ApiResponse<Void>> postComment(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long boardId,
            @RequestBody CommentPostOrModifyRequest commentPostRequest
    ) {
        boardCommandService.postComment(
                principalDetails.getUsername(),
                boardId,
                commentPostRequest
        );

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> modifyComment(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long commentId,
            @RequestBody CommentPostOrModifyRequest commentModifyRequest
    ) {
        boardCommandService.modifyComment(
                principalDetails.getUsername(),
                commentId,
                commentModifyRequest
        );

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long commentId
    ) {
        boardCommandService.deleteComment(
                principalDetails.getUsername(),
                commentId
        );

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }
}
