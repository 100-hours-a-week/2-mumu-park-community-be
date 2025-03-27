package com.kaboot.community.domain.board.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.domain.board.dto.request.LikeRequest;
import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;
import com.kaboot.community.domain.board.entity.Likes;
import com.kaboot.community.domain.board.repository.board.BoardRepository;
import com.kaboot.community.domain.board.repository.comment.CommentRepository;
import com.kaboot.community.domain.board.repository.likes.LikesRepository;
import com.kaboot.community.domain.board.service.BoardQueryService;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;

@Profile("test")
@ExtendWith(MockitoExtension.class)
class BoardCommandServiceImplTest {

  @Mock
  private BoardQueryService boardQueryService;

  @Mock
  private MemberQueryService memberQueryService;

  @Mock
  private BoardRepository boardRepository;

  @Mock
  private LikesRepository likesRepository;

  @Mock
  private CommentRepository commentRepository;

  @InjectMocks
  private BoardCommandServiceImpl boardCommandService;

  @Test
  @DisplayName("유효한 게시글 등록 요청의 경우 게시글 등록 성공")
  void postBoardSuccess() {
    //given
    Member member = createMember();
    String authUsername = "username";
    PostOrModifyRequest validPostRequest = new PostOrModifyRequest("title", "content", "img",
        "url");

    when(memberQueryService.getMemberByUsername(authUsername)).thenReturn(member);

    //when
    boardCommandService.postBoard(authUsername, validPostRequest);

    //then
    verify(boardRepository, times(1)).save(any(Board.class));
  }

  @Test
  @DisplayName("멤버가 존재하지 않는다면 게시글 등록 실패")
  void postBoardFailNotExistMember() {
    //given
    String invalidUsername = "s";
    PostOrModifyRequest validPostRequest = new PostOrModifyRequest("title", "content", "img",
        "url");
    when(memberQueryService.getMemberByUsername(invalidUsername)).thenThrow(
        new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

    //when, then
    assertThatThrownBy(() -> boardCommandService.postBoard(invalidUsername, validPostRequest))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage());
  }

  @Test
  @DisplayName("게시글 생성자가 수정요청을 하는 경우 게시글 수정 성공")
  void modifyPostSuccessValidMember() {
    //given
    String authUsername = "member@test.com";
    Long boardId = 1L;
    Member validMember = createMember();
    Board board = createBoard(validMember);
    PostOrModifyRequest validModifyRequest = new PostOrModifyRequest("modify title",
        "modify content", "modify img", "modify url");

    when(boardQueryService.getBoardById(anyLong())).thenReturn(board);

    //when
    boardCommandService.modifyBoard(authUsername, validModifyRequest, boardId);

    //then
    assertThat(board.getTitle()).isEqualTo(validModifyRequest.title());
    assertThat(board.getContent()).isEqualTo(validModifyRequest.content());
    assertThat(board.getImageOriginalName()).isEqualTo(validModifyRequest.imageOriginalName());
    assertThat(board.getImgUrl()).isEqualTo(validModifyRequest.imageUrl());
  }

  @Test
  @DisplayName("제 3자가 수정요청을 하는 경우 게시글 수정 실패")
  void modifyPostFailNotAuthMember() {
    //given
    String invalidUsername = "different@test.com";
    Long boardId = 1L;
    Member validMember = createMember();
    Board board = createBoard(validMember);
    PostOrModifyRequest validModifyRequest = new PostOrModifyRequest("modify title",
        "modify content", "modify img", "modify url");

    when(boardQueryService.getBoardById(anyLong())).thenReturn(board);

    //when
    assertThatThrownBy(
        () -> boardCommandService.modifyBoard(invalidUsername, validModifyRequest, boardId))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.ACCESS_DENIED.getMessage());
  }

  @Test
  @DisplayName("게시글 생성자가 삭제요청을 하는 경우 게시글 삭제 성공")
  void deletePostSuccessValidMember() {
    //given
    Long boardId = 1L;
    Member validMember = createMember();
    String authUsername = validMember.getUsername();
    Board board = createBoard(validMember);

    when(boardQueryService.getBoardById(anyLong())).thenReturn(board);

    //when
    boardCommandService.deleteBoard(authUsername, boardId, LocalDateTime.now());

    //then
    assertThat(board.getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("좋아요를 누른 경우 좋아요 반영")
  void postLikeSuccess() {
    //given
    String authUsername = "authUser";
    Long boardId = 1L;
    Member member = createMember();
    Board board = createBoard(member);

    LikeRequest postLikeRequest = new LikeRequest(false);

    when(memberQueryService.getMemberByUsername(authUsername)).thenReturn(member);
    when(boardQueryService.getBoardById(boardId)).thenReturn(board);

    //when
    boardCommandService.toggleLike(authUsername, boardId, postLikeRequest);

    //then
    verify(likesRepository, times(1)).save(any(Likes.class));
    verify(likesRepository, never()).findByBoardIdAndMemberId(boardId, member.getId());
    verify(likesRepository, never()).delete(any(Likes.class));
  }

  @Test
  @DisplayName("좋아요를 취소한 경우 좋아요 취소")
  void cancelLikeSuccess() {
    //given
    String authUsername = "authUser";
    Long boardId = 1L;
    Member member = createMember();
    Board board = createBoard(member);
    Likes likes = createLikes(board, member);

    LikeRequest postLikeRequest = new LikeRequest(true);

    when(memberQueryService.getMemberByUsername(authUsername)).thenReturn(member);
    when(boardQueryService.getBoardById(boardId)).thenReturn(board);
    when(likesRepository.findByBoardIdAndMemberId(boardId, member.getId())).thenReturn(
        Optional.ofNullable(likes));

    //when
    boardCommandService.toggleLike(authUsername, boardId, postLikeRequest);

    //then
    verify(likesRepository, times(1)).findByBoardIdAndMemberId(boardId, member.getId());
    verify(likesRepository, never()).save(likes);
  }

  @Test
  @DisplayName("멤버가 존재하지 않는 경우 좋아요 토글 실패")
  void toggleLikeFailNotExistMember() {
    //given
    String invalidUsername = "nonexistent@test.com";
    Long boardId = 1L;
    LikeRequest likeRequest = new LikeRequest(false);

    when(memberQueryService.getMemberByUsername(invalidUsername))
        .thenThrow(new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

    //when, then
    assertThatThrownBy(() -> boardCommandService.toggleLike(invalidUsername, boardId, likeRequest))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage());

    verify(likesRepository, never()).save(any(Likes.class));
    verify(likesRepository, never()).findByBoardIdAndMemberId(anyLong(), anyLong());
    verify(likesRepository, never()).delete(any(Likes.class));
  }

  @Test
  @DisplayName("제 3자가 삭제요청을 하는 경우 게시글 삭제 실패")
  void deletePostFailNotAuthMember() {
    //given
    String invalidUsername = "different@test.com";
    Long boardId = 1L;
    Member validMember = createMember();
    Board board = createBoard(validMember);

    when(boardQueryService.getBoardById(anyLong())).thenReturn(board);

    //when
    assertThatThrownBy(
        () -> boardCommandService.deleteBoard(invalidUsername, boardId, LocalDateTime.now()))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.ACCESS_DENIED.getMessage());
  }

  @Test
  @DisplayName("게시글이 존재하지 않는 경우 좋아요 토글 실패")
  void toggleLikeFailNotExistBoard() {
    //given
    String authUsername = "member@test.com";
    Long invalidBoardId = 999L;
    Member member = createMember();
    LikeRequest likeRequest = new LikeRequest(false);

    when(memberQueryService.getMemberByUsername(authUsername)).thenReturn(member);
    when(boardQueryService.getBoardById(invalidBoardId))
        .thenThrow(new CustomException(CustomResponseStatus.BOARD_NOT_EXIST));

    //when, then
    assertThatThrownBy(
        () -> boardCommandService.toggleLike(authUsername, invalidBoardId, likeRequest))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.BOARD_NOT_EXIST.getMessage());

    verify(likesRepository, never()).save(any(Likes.class));
    verify(likesRepository, never()).findByBoardIdAndMemberId(anyLong(), anyLong());
    verify(likesRepository, never()).delete(any(Likes.class));
  }

  @Test
  @DisplayName("좋아요 취소 시 해당 좋아요가 존재하지 않는 경우 실패")
  void toggleLikeCancelFailLikesNotExist() {
    //given
    String authUsername = "member@test.com";
    Long boardId = 1L;
    Member member = createMember();
    Board board = createBoard(member);
    LikeRequest cancelLikeRequest = new LikeRequest(true); // 좋아요 취소 요청

    when(memberQueryService.getMemberByUsername(authUsername)).thenReturn(member);
    when(boardQueryService.getBoardById(boardId)).thenReturn(board);
    when(likesRepository.findByBoardIdAndMemberId(board.getId(), member.getId()))
        .thenReturn(Optional.empty());

    //when, then
    assertThatThrownBy(
        () -> boardCommandService.toggleLike(authUsername, boardId, cancelLikeRequest))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.LIKES_NOT_EXIST.getMessage());

    verify(likesRepository, times(1)).findByBoardIdAndMemberId(board.getId(), member.getId());
    verify(likesRepository, never()).save(any(Likes.class));
    verify(likesRepository, never()).delete(any(Likes.class));
  }

  @Test
  @DisplayName("유효한 사용자 및 게시글에 대해 댓글 등록 요청을 한 경우 댓글 등록 성공")
  void postCommentSuccess() {
    //given
    Member member = createMember();
    Board validBoard = createBoard(member);
    CommentPostOrModifyRequest postCommentRequest = new CommentPostOrModifyRequest("new content");

    when(memberQueryService.getMemberByUsername(anyString())).thenReturn(member);
    when(boardQueryService.getBoardById(anyLong())).thenReturn(validBoard);

    //when
    boardCommandService.postComment("authUsername", 1L, postCommentRequest);

    //then
    verify(commentRepository, times(1)).save(any(Comment.class));
  }

  @Test
  @DisplayName("멤버가 존재하지 않는다면 댓글 등록 실패")
  void postCommentFailNotExistMember() {
    //given
    String invalidUsername = "s";
    CommentPostOrModifyRequest postCommentRequest = new CommentPostOrModifyRequest("new content");
    when(memberQueryService.getMemberByUsername(invalidUsername)).thenThrow(
        new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

    //when, then
    assertThatThrownBy(
        () -> boardCommandService.postComment(invalidUsername, 1L, postCommentRequest))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage());
  }

  @Test
  @DisplayName("게시글이 존재하지 않는다면 댓글 등록 실패")
  void postCommentFailNotExistBoard() {
    //given
    String authUsername = "test@test.com";
    Member member = createMember();
    CommentPostOrModifyRequest postCommentRequest = new CommentPostOrModifyRequest("new content");
    when(memberQueryService.getMemberByUsername(authUsername)).thenReturn(member);
    when(boardQueryService.getBoardById(anyLong())).thenThrow(
        new CustomException(CustomResponseStatus.BOARD_NOT_EXIST));

    //when, then
    assertThatThrownBy(() -> boardCommandService.postComment(authUsername, 1L, postCommentRequest))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.BOARD_NOT_EXIST.getMessage());
  }

  @Test
  @DisplayName("댓글 생성자가 수정요청을 하는 경우 댓글 수정 성공")
  void modifyCommentSuccess() {
    //given
    Member member = createMember();
    Board board = createBoard(member);
    Comment comment = createComment(board, member);

    CommentPostOrModifyRequest modifyCommentRequest = new CommentPostOrModifyRequest(
        "modify content");

    when(boardQueryService.getCommentById(anyLong())).thenReturn(comment);

    //when
    boardCommandService.modifyComment("member@test.com", 1L, modifyCommentRequest);

    //then
    assertThat(comment.getContent()).isEqualTo(modifyCommentRequest.content());
  }

  @Test
  @DisplayName("제3자가 댓글 수정 요청하는 경우 댓글 수정 실패")
  void modifyCommentFailNotAuthMember() {
    //given
    String invalidUsername = "different@test.com";

    Member member = createMember();
    Board board = createBoard(member);
    Comment comment = createComment(board, member);

    CommentPostOrModifyRequest modifyRequest = new CommentPostOrModifyRequest("new content");

    when(boardQueryService.getCommentById(anyLong())).thenReturn(comment);

    //when, then
    assertThatThrownBy(() -> boardCommandService.modifyComment(invalidUsername, 1L, modifyRequest))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.ACCESS_DENIED.getMessage());
  }

  @Test
  @DisplayName("댓글 생성자가 삭제요청을 하는 경우 댓글 삭제 성공")
  void deleteCommentSuccess() {
    //given
    Member member = createMember();
    Board board = createBoard(member);
    Comment comment = createComment(board, member);

    when(boardQueryService.getCommentById(anyLong())).thenReturn(comment);

    //when
    boardCommandService.deleteComment("member@test.com", 1L);

    //then
    assertThat(comment.getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("제3자가 댓글 삭제 요청하는 경우 댓글 삭제 실패")
  void deleteCommentFailNotAuthMember() {
    //given
    String differentUsername = "different@test.com";

    Member member = createMember();
    Board board = createBoard(member);
    Comment comment = createComment(board, member);

    when(boardQueryService.getCommentById(anyLong())).thenReturn(comment);

    //when, then
    assertThatThrownBy(() -> boardCommandService.deleteComment(differentUsername, 1L))
        .isInstanceOf(CustomException.class)
        .hasMessage(CustomResponseStatus.ACCESS_DENIED.getMessage());
  }

  private Member createMember() {
    return Member.builder()
        .id(1L)
        .username("member@test.com")
        .password("Test1!")
        .nickname("test")
        .profileImgUrl("test.jpeg")
        .role(RoleType.ROLE_MEMBER)
        .build();
  }

  private Board createBoard(Member member) {
    return Board.builder()
        .id(1L)
        .title("title")
        .content("content")
        .imageOriginalName("name")
        .imgUrl("url")
        .viewCount(0)
        .member(member)
        .build();
  }

  private Comment createComment(Board board, Member member) {
    return Comment.builder()
        .board(board)
        .member(member)
        .content("content")
        .build();
  }

  private Likes createLikes(Board board, Member member) {
    return Likes.builder()
        .board(board)
        .member(member)
        .build();
  }
}