package com.kaboot.community.domain.board.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.config.jwt.enums.TokenType;
import com.kaboot.community.domain.board.dto.request.CommentPostOrModifyRequest;
import com.kaboot.community.domain.board.dto.request.LikeRequest;
import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;
import com.kaboot.community.domain.board.entity.Likes;
import com.kaboot.community.domain.board.repository.board.BoardRepository;
import com.kaboot.community.domain.board.repository.comment.CommentRepository;
import com.kaboot.community.domain.board.repository.likes.LikesRepository;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.util.jwt.JwtUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BoardControllerTest {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private BoardRepository boardRepository;

  @Autowired
  private LikesRepository likesRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private String accessToken;
  private Member testMember;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();

    objectMapper = new ObjectMapper();

    testMember = Member.builder()
        .username("test@example.com")
        .password("Test1!")
        .nickname("tester")
        .profileImgUrl("test.jpeg")
        .role(RoleType.ROLE_MEMBER)
        .build();

    Member savedMember = memberRepository.save(testMember);

    accessToken = "Bearer " + jwtUtil.createToken(savedMember.getId(), TokenType.ACCESS_TOKEN,
        savedMember.getRole());
  }

  @Test
  @DisplayName("게시글 조회 성공시 200 응답 반환")
  void getBoardsSuccess() throws Exception {
    // given
    Member member1 = createMember("test1@test.com", "test1", "test.jpeg");
    Member member2 = createMember("test2@test.com", "test2", "test2.jpeg");

    Board board1 = createBoard("title1", "test1", member1);
    Board board2 = createBoard("title2", "test2", member2);

    memberRepository.saveAllAndFlush(List.of(member1, member2));
    boardRepository.saveAllAndFlush(List.of(board1, board2));

    // when, then
    mockMvc.perform(get("/boards"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.boardSimpleInfos").isArray())
        .andExpect(jsonPath("$.data.boardSimpleInfos.length()").value(2))
        .andExpect(jsonPath("$.data.boardSimpleInfos[0].title").value("title2"))
        .andExpect(jsonPath("$.data.boardSimpleInfos[0].authorNickname").value("test2"))
        .andExpect(jsonPath("$.data.boardSimpleInfos[1].title").value("title1"))
        .andExpect(jsonPath("$.data.boardSimpleInfos[1].authorNickname").value("test1"))
        .andExpect(jsonPath("$.message").value("게시글 조회에 성공하였습니다."));
  }

  @Test
  @DisplayName("게시글 상세조회 성공시 200응답 반환")
  void getBoardDetailSuccess() throws Exception {
    // given
    Member member1 = createMember("test1@test.com", "test1", "test.jpeg");

    Board board1 = createBoard("title1", "test1", member1);

    Member savedMember = memberRepository.saveAndFlush(member1);
    Board savedBoard = boardRepository.saveAndFlush(board1);

    // when, then
    mockMvc.perform(get("/boards/{boardId}", board1.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.boardDetail").exists())
        .andExpect(jsonPath("$.data.boardDetail.title").value(savedBoard.getTitle()))
        .andExpect(jsonPath("$.data.boardDetail.content").value(savedBoard.getContent()))
        .andExpect(
            jsonPath("$.data.boardDetail.imgFileName").value(savedBoard.getImageOriginalName()))
        .andExpect(jsonPath("$.data.boardDetail.contentImg").value(savedBoard.getImgUrl()))
        .andExpect(
            jsonPath("$.data.boardDetail.authorProfileImg").value(savedMember.getProfileImgUrl()))
        .andExpect(jsonPath("$.data.boardDetail.createdAt").isString())
        .andExpect(jsonPath("$.message").value("게시글 상세조회에 성공하였습니다."));
  }

  @Test
  @DisplayName("존재하지 않는 게시글 상세조회 요청시 404응답 반환")
  void getBoardDetailFailNotExist() throws Exception {
    // given
    Member member1 = createMember("test1@test.com", "test1", "test.jpeg");

    Board board1 = createBoard("title1", "test1", member1);

    memberRepository.saveAndFlush(member1);
    boardRepository.saveAndFlush(board1);

    // when, then
    mockMvc.perform(get("/boards/{boardId}", 999))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value("존재하지 게시글입니다."));
  }

  @Test
  @DisplayName("유효한 게시글 등록 DTO로 등록 요청시 게시글 등록 성공")
  void postBoardSuccess() throws Exception {
    // given
    PostOrModifyRequest validPostDto = new PostOrModifyRequest("test title", "test content", "name",
        "url");

    // when, then
    mockMvc.perform(
            post("/boards")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validPostDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value("게시글 등록에 성공하였습니다."));
  }

  static Stream<Object[]> invalidPostBoardData() {
    return Stream.of(
        new Object[]{"", "test content", "testName", "image.jpeg", "title", "must not be empty"},
        new Object[]{"test title", "", "testName", "image.jpeg", "content", "must not be empty"}
    );
  }

  @ParameterizedTest
  @MethodSource("invalidPostBoardData")
  @DisplayName("글 등록시 필수 필드가 비어있다면 400 오류 발생")
  void postBoardFailWithInvalidDto(String title, String content, String imgName, String imgUrl,
      String fieldName, String errorMessage)
      throws Exception {
    // given
    PostOrModifyRequest invalidPostDto = new PostOrModifyRequest(title, content, imgName,
        imgUrl);

    mockMvc.perform(
            post("/boards")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPostDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("유효하지 않은 데이터입니다."))
        .andExpect(jsonPath("$.data." + fieldName).exists())
        .andExpect(jsonPath("$.data." + fieldName).value(errorMessage));
  }

  @Test
  @DisplayName("유효한 게시글 수정 DTO로 수정 요청시 게시글 수정 성공")
  void modifyBoardSuccess() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("old title", "old content", testMember));

    PostOrModifyRequest modifyDto = new PostOrModifyRequest("new title", "new content", "imgName",
        "imgUrl");

    // when, then
    mockMvc.perform(
            patch("/boards/{boardId}", board.getId())
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value("게시글 수정에 성공하였습니다."));

    assertThat(board.getTitle()).isEqualTo(modifyDto.title());
    assertThat(board.getContent()).isEqualTo(modifyDto.content());
  }

  @ParameterizedTest
  @MethodSource("invalidPostBoardData")
  @DisplayName("게시글 수정 시 필수 필드가 비어있다면 400 오류 발생")
  void modifyBoardFailWithInvalidDto(String title, String content, String imgName, String imgUrl,
      String fieldName, String errorMessage) throws Exception {
    // given
    Board board = boardRepository.save(
        createBoard("original title", "original content", testMember));

    PostOrModifyRequest invalidDto = new PostOrModifyRequest(title, content, imgName, imgUrl);

    // when, then
    mockMvc.perform(
            patch("/boards/{boardId}", board.getId())
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("유효하지 않은 데이터입니다."))
        .andExpect(jsonPath("$.data." + fieldName).exists())
        .andExpect(jsonPath("$.data." + fieldName).value(errorMessage));
  }

  @Test
  @DisplayName("수정 권한이 없는 경우 수정 실패")
  void modifyBoardFailNoAuth() throws Exception {
    // given
    Member member = memberRepository.save(createMember("test2@test.com", "test", "profile.jpeg"));
    Board board = boardRepository.save(createBoard("old title", "old content", testMember));

    String diffAT =
        "Bearer " + jwtUtil.createToken(member.getId(), TokenType.ACCESS_TOKEN, member.getRole());
    PostOrModifyRequest validModifyDto = new PostOrModifyRequest("new title", "new content",
        "imgName", "imgUrl");

    // when, then
    mockMvc.perform(
            patch("/boards/{boardId}", board.getId())
                .header("Authorization", diffAT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validModifyDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(
            jsonPath("$.message").value(CustomResponseStatus.ACCESS_DENIED.getMessage()));
  }

  @Test
  @DisplayName("게시글 삭제 성공 시 200 응답과 성공 메시지 반환")
  void deleteBoardSuccess() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));

    // when, then
    mockMvc.perform(delete("/boards/{boardId}", board.getId())
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value("게시글 삭제에 성공하였습니다."));

    assertThat(board.getDeletedAt()).isNotNull();
  }

  @Test
  @DisplayName("존재하지 않는 게시글 삭제 요청시 오류 발생")
  void deleteBoardFailNotFound() throws Exception {
    // given
    Long invalidId = 9999L;

    // when, then
    mockMvc.perform(delete("/boards/{boardId}", invalidId)
            .header("Authorization", accessToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value(CustomResponseStatus.BOARD_NOT_EXIST.getMessage()));
  }

  @Test
  @DisplayName("삭제 권한이 없는 경우 게시글 삭제 실패")
  void deleteBoardFailNoAuth() throws Exception {
    // given
    Member otherMember = memberRepository.save(
        createMember("other@test.com", "other", "profile.jpeg"));
    Board board = boardRepository.save(createBoard("title", "content", testMember));

    String otherAccessToken = "Bearer " + jwtUtil.createToken(
        otherMember.getId(), TokenType.ACCESS_TOKEN, otherMember.getRole());

    // when, then
    mockMvc.perform(delete("/boards/{boardId}", board.getId())
            .header("Authorization", otherAccessToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value(CustomResponseStatus.ACCESS_DENIED.getMessage()));

    assertThat(board.getDeletedAt()).isNull();
  }

  @Test
  @DisplayName("게시글 좋아요 여부 조회 - 좋아요 누른 경우 true 반환")
  void checkBoardLikeByUserLiked() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));
    Likes like = likesRepository.save(createLikes(testMember, board));

    // when, then
    mockMvc.perform(get("/boards/{boardId}/likes", board.getId())
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isLike").value(true))
        .andExpect(jsonPath("$.message").value("게시글 좋아요 여부조회에 성공하였습니다."));
  }

  @Test
  @DisplayName("게시글 좋아요 여부 조회 - 좋아요 안 누른 경우 false 반환")
  void checkBoardLikeByUserNotLiked() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));
    // Likes 저장하지 않음

    // when, then
    mockMvc.perform(get("/boards/{boardId}/likes", board.getId())
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isLike").value(false))
        .andExpect(jsonPath("$.message").value("게시글 좋아요 여부조회에 성공하였습니다."));
  }

  @Test
  @DisplayName("존재하지 않는 게시글 좋아요 여부 조회 시 isLike: false 리턴")
  void checkBoardLikeByUserBoardNotFound() throws Exception {
    // given
    Long invalidBoardId = 9999L;

    // when, then
    mockMvc.perform(get("/boards/{boardId}/likes", invalidBoardId)
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isLike").value(false))
        .andExpect(jsonPath("$.message").value("게시글 좋아요 여부조회에 성공하였습니다."));
  }

  @Test
  @DisplayName("좋아요 등록 성공 시 200 응답과 성공 메시지 반환")
  void toggleLike_addSuccess() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));
    LikeRequest likeRequest = new LikeRequest(false);

    // when, then
    mockMvc.perform(patch("/boards/{boardId}/likes", board.getId())
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(likeRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value("좋아요 처리에 성공하였습니다."));
  }

  @Test
  @DisplayName("좋아요 취소 성공 시 200 응답과 성공 메시지 반환")
  void toggleLike_removeSuccess() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));
    likesRepository.save(createLikes(testMember, board));

    LikeRequest unlikeRequest = new LikeRequest(true);

    // when, then
    mockMvc.perform(patch("/boards/{boardId}/likes", board.getId())
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(unlikeRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value("좋아요 처리에 성공하였습니다."));
  }

  @Test
  @DisplayName("댓글 작성 성공 시 200 응답과 성공 메시지 반환")
  void postCommentSuccess() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));
    CommentPostOrModifyRequest request = new CommentPostOrModifyRequest("댓글 내용입니다.");

    // when, then
    mockMvc.perform(post("/boards/{boardId}/comments", board.getId())
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value("댓글 작성에 성공하였습니다."));
  }

  @Test
  @DisplayName("content가 비어있는 경우 댓글 등록 실패 및 400 반환")
  void postCommentFailWithEmptyContent() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));
    CommentPostOrModifyRequest request = new CommentPostOrModifyRequest("");

    // when, then
    mockMvc.perform(post("/boards/{boardId}/comments", board.getId())
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("유효하지 않은 데이터입니다."))
        .andExpect(
            jsonPath("$.data.content").value("must not be empty"));
  }

  @Test
  @DisplayName("댓글 작성 실패 - 존재하지 않는 게시글")
  void postCommentFailBoardNotFound() throws Exception {
    // given
    Long invalidBoardId = 99999L;
    CommentPostOrModifyRequest request = new CommentPostOrModifyRequest("유효한 댓글");

    // when, then
    mockMvc.perform(post("/boards/{boardId}/comments", invalidBoardId)
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value(CustomResponseStatus.BOARD_NOT_EXIST.getMessage()));
  }

  @Test
  @DisplayName("댓글 수정 성공 시 200 응답과 성공 메시지 반환")
  void modifyCommentSuccess() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));
    Comment comment = commentRepository.save(createComment(board, testMember, "원본 댓글"));
    CommentPostOrModifyRequest modifyRequest = new CommentPostOrModifyRequest("수정된 댓글");

    // when, then
    mockMvc.perform(patch("/boards/comments/{commentId}", comment.getId())
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(modifyRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value("댓글 수정에 성공하였습니다."));

    assertThat(comment.getContent()).isEqualTo(modifyRequest.content());
  }

  @Test
  @DisplayName("댓글 수정 실패 - content가 비어있는 경우 400 오류")
  void modifyCommentFailWithEmptyContent() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));
    Comment comment = commentRepository.save(createComment(board, testMember, "원본 댓글"));
    CommentPostOrModifyRequest request = new CommentPostOrModifyRequest(""); // 빈 값

    // when, then
    mockMvc.perform(patch("/boards/comments/{commentId}", comment.getId())
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("유효하지 않은 데이터입니다."))
        .andExpect(jsonPath("$.data.content").value("must not be empty")); // 커스터마이징 시 메시지 조정
  }

  @Test
  @DisplayName("존재하지 않는 댓글을 수정 요청시 실패")
  void modifyCommentFailWithInvalidCommentId() throws Exception {
    // given
    Long invalidCommentId = 9999L;
    CommentPostOrModifyRequest modifyRequest = new CommentPostOrModifyRequest("수정 내용");

    // when, then
    mockMvc.perform(patch("/boards/comments/{commentId}", invalidCommentId)
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(modifyRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(
            jsonPath("$.message").value(CustomResponseStatus.COMMENT_NOT_EXIST.getMessage()));
  }

  @Test
  @DisplayName("댓글 수정 실패 - 권한 없는 사용자")
  void modifyCommentFailWithNoAuth() throws Exception {
    // given
    Member otherMember = memberRepository.save(createMember("other@test.com", "other", "img.jpeg"));
    String otherAccessToken = "Bearer " + jwtUtil.createToken(
        otherMember.getId(), TokenType.ACCESS_TOKEN, otherMember.getRole());

    Board board = boardRepository.save(createBoard("title", "content", testMember));
    Comment comment = commentRepository.save(createComment(board, testMember, "원본 댓글"));
    CommentPostOrModifyRequest request = new CommentPostOrModifyRequest("수정 시도");

    // when, then
    mockMvc.perform(patch("/boards/comments/{commentId}", comment.getId())
            .header("Authorization", otherAccessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value(CustomResponseStatus.ACCESS_DENIED.getMessage()));
  }

  @Test
  @DisplayName("댓글 삭제 성공 시 200 응답과 성공 메시지 반환")
  void deleteCommentSuccess() throws Exception {
    // given
    Board board = boardRepository.save(createBoard("title", "content", testMember));
    Comment comment = commentRepository.save(createComment(board, testMember, "삭제할 댓글"));

    // when, then
    mockMvc.perform(delete("/boards/comments/{commentId}", comment.getId())
            .header("Authorization", accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value("댓글 삭제에 성공하였습니다."));
  }

  @Test
  @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글 ID")
  void deleteCommentFailNotFound() throws Exception {
    // given
    Long invalidCommentId = 9999L;

    // when, then
    mockMvc.perform(delete("/boards/comments/{commentId}", invalidCommentId)
            .header("Authorization", accessToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(
            jsonPath("$.message").value(CustomResponseStatus.COMMENT_NOT_EXIST.getMessage()));
  }

  @Test
  @DisplayName("댓글 삭제 실패 - 권한 없는 사용자")
  void deleteCommentFailNoAuth() throws Exception {
    // given
    Member otherMember = memberRepository.save(createMember("other@test.com", "other", "img.jpeg"));
    String otherAccessToken = "Bearer " + jwtUtil.createToken(
        otherMember.getId(), TokenType.ACCESS_TOKEN, otherMember.getRole());

    Board board = boardRepository.save(createBoard("title", "content", testMember));
    Comment comment = commentRepository.save(createComment(board, testMember, "댓글"));

    // when, then
    mockMvc.perform(delete("/boards/comments/{commentId}", comment.getId())
            .header("Authorization", otherAccessToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.data").isEmpty())
        .andExpect(jsonPath("$.message").value(CustomResponseStatus.ACCESS_DENIED.getMessage()));
  }

  private Member createMember(String username, String nickname, String profileImgUrl) {
    return Member.builder()
        .username(username)
        .password("Test1!")
        .nickname(nickname)
        .profileImgUrl(profileImgUrl)
        .role(RoleType.ROLE_MEMBER)
        .build();
  }

  private Board createBoard(String title, String content, Member member) {
    return Board.builder()
        .title(title)
        .content(content)
        .member(member)
        .viewCount(0)
        .build();
  }

  private List<Comment> createComments(Board board, Member member, int size) {
    List<Comment> comments = new ArrayList<>();

    for (int i = 1; i <= size; i++) {
      comments.add(createComment(board, member, "comment" + i));
    }

    return comments;
  }

  private Comment createComment(Board board, Member member, String content) {
    return Comment.builder()
        .board(board)
        .member(member)
        .content(content)
        .build();
  }

  private Likes createLikes(Member member, Board board) {
    return Likes.builder()
        .member(member)
        .board(board)
        .build();
  }
}