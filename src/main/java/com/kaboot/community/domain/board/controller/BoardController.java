package com.kaboot.community.domain.board.controller;

import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.config.security.member.PrincipalDetails;
import com.kaboot.community.domain.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.domain.board.dto.request.LikeRequest;
import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.kaboot.community.domain.board.dto.response.BoardsResponse;
import com.kaboot.community.domain.board.service.BoardCommandService;
import com.kaboot.community.domain.board.service.BoardQueryService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    return ResponseEntity.ok().body(ApiResponse.createSuccess(
        response,
        CustomResponseStatus.SUCCESS.withMessage("게시글 조회에 성공하였습니다."))
    );
  }

  @GetMapping("/{boardId}")
  public ResponseEntity<ApiResponse<BoardDetailResponse>> getBoardDetail(
      @PathVariable Long boardId
  ) {
    BoardDetailResponse response = boardQueryService.getBoardDetail(boardId);

    return ResponseEntity.ok().body(ApiResponse.createSuccess(
        response,
        CustomResponseStatus.SUCCESS.withMessage("게시글 상세조회에 성공하였습니다."))
    );
  }

  @PostMapping()
  public ResponseEntity<ApiResponse<Void>> post(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestBody @Valid PostOrModifyRequest postRequest
  ) {
    boardCommandService.postBoard(
        principalDetails.getUsername(),
        postRequest
    );

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("게시글 등록에 성공하였습니다."))
    );
  }

  @PatchMapping("/{boardId}")
  public ResponseEntity<ApiResponse<Void>> modifyBoard(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestBody @Valid PostOrModifyRequest modifyRequest,
      @PathVariable Long boardId
  ) {
    boardCommandService.modifyBoard(
        principalDetails.getUsername(),
        modifyRequest,
        boardId
    );

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("게시글 수정에 성공하였습니다."))
    );
  }

  @DeleteMapping("/{boardId}")
  public ResponseEntity<ApiResponse<Void>> deleteBoard(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @PathVariable Long boardId
  ) {
    boardCommandService.deleteBoard(
        principalDetails.getUsername(),
        boardId,
        LocalDateTime.now()
    );

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("게시글 삭제에 성공하였습니다."))
    );
  }

  @PatchMapping("/{boardId}/likes")
  public ResponseEntity<ApiResponse<Void>> toggleLike(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @PathVariable Long boardId,
      @RequestBody @Valid LikeRequest likeRequest
  ) {
    boardCommandService.toggleLike(
        principalDetails.getUsername(),
        boardId, likeRequest
    );

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("좋아요 처리에 성공하였습니다."))
    );
  }

  @PostMapping("/{boardId}/comments")
  public ResponseEntity<ApiResponse<Void>> postComment(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @PathVariable Long boardId,
      @RequestBody @Valid CommentPostOrModifyRequest commentPostRequest
  ) {
    boardCommandService.postComment(
        principalDetails.getUsername(),
        boardId,
        commentPostRequest
    );

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("댓글 작성에 성공하였습니다."))
    );
  }

  @PatchMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> modifyComment(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @PathVariable Long commentId,
      @RequestBody @Valid CommentPostOrModifyRequest commentModifyRequest
  ) {
    boardCommandService.modifyComment(
        principalDetails.getUsername(),
        commentId,
        commentModifyRequest
    );

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("댓글 수정에 성공하였습니다."))
    );
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

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("댓글 삭제에 성공하였습니다."))
    );
  }
}
