package com.kaboot.community.domain.board.repository.board;

import static org.assertj.core.api.Assertions.assertThat;

import com.kaboot.community.config.querydsl.QueryDSLConfig;
import com.kaboot.community.domain.board.dto.request.PostOrModifyRequest;
import com.kaboot.community.domain.board.dto.response.BoardDetailResponse;
import com.kaboot.community.domain.board.dto.response.BoardsResponse.BoardSimpleInfo;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;
import com.kaboot.community.domain.board.entity.Likes;
import com.kaboot.community.domain.board.repository.comment.CommentRepository;
import com.kaboot.community.domain.board.repository.likes.LikesRepository;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDSLConfig.class})
class BoardRepositoryTest {

  @Autowired
  private BoardRepository boardRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private LikesRepository likesRepository;

  @Autowired
  private EntityManager entityManager;

  private Member testMember;

  @BeforeEach
  void setUp() {
    testMember = createMember("test@example.com", "tester", "test.jpeg");
    memberRepository.save(testMember);
  }

  @Test
  @DisplayName("게시글 저장 및 ID로 조회 테스트")
  void saveAndFindById() {
    // given
    Board board = createBoard("Test Title", "Test Content", testMember);

    // when
    Board savedBoard = boardRepository.saveAndFlush(board);

    // then
    assertThat(savedBoard.getId()).isNotNull();

    Optional<Board> foundBoard = boardRepository.findById(savedBoard.getId());
    assertThat(foundBoard).isPresent();
    assertThat(foundBoard.get().getTitle()).isEqualTo("Test Title");
    assertThat(foundBoard.get().getContent()).isEqualTo("Test Content");
    assertThat(foundBoard.get().getMember().getId()).isEqualTo(testMember.getId());
  }

  @Test
  @DisplayName("게시글 수정 테스트")
  void updateBoard() {
    // given
    Board board = createBoard("Original Title", "Original Content", testMember);
    Board savedBoard = boardRepository.save(board);

    // when
    PostOrModifyRequest modifyRequest = new PostOrModifyRequest(
        "Updated Title",
        "Updated Content",
        "updated.jpeg",
        "http://example.com/updated.jpeg"
    );

    savedBoard.update(modifyRequest);
    boardRepository.saveAndFlush(savedBoard);

    // then
    Board updatedBoard = boardRepository.findById(savedBoard.getId()).get();
    assertThat(updatedBoard.getTitle()).isEqualTo(modifyRequest.title());
    assertThat(updatedBoard.getContent()).isEqualTo(modifyRequest.content());
    assertThat(updatedBoard.getImageOriginalName()).isEqualTo(modifyRequest.imageOriginalName());
    assertThat(updatedBoard.getImgUrl()).isEqualTo(modifyRequest.imageUrl());
  }

  @Test
  @DisplayName("조회수 증가 테스트")
  void increaseViewCount() {
    // given
    Board board = createBoard("title", "test content", testMember);
    Board savedBoard = boardRepository.save(board);

    // when
    savedBoard.increaseViewCount();
    boardRepository.saveAndFlush(savedBoard);

    // then
    Board foundBoard = boardRepository.findById(savedBoard.getId()).get();
    assertThat(foundBoard.getViewCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("삭제된 게시글은 조회되지 않음 (SQLRestriction 테스트)")
  void softDeletedBoardNotFound() {
    // given
    Board board = createBoard("Test Title", "Test Content", testMember);
    Board savedBoard = boardRepository.save(board);
    Long boardId = savedBoard.getId();

    // when
    board.delete(LocalDateTime.now());
    boardRepository.saveAndFlush(savedBoard);
    entityManager.clear();

    // then
    Optional<Board> foundBoard = boardRepository.findById(boardId);
    assertThat(foundBoard).isEmpty();
  }

  @Test
  @DisplayName("게시글의 id를 통해 상세정보를 조회한다.")
  void fetchBoardDetailById() {
    //given
    Board board = createBoard("Test title", "Test content", testMember);
    Board savedBoard = boardRepository.save(board);
    int commentCount = 5;
    List<Comment> comments = createComments(savedBoard, testMember, commentCount);
    commentRepository.saveAllAndFlush(comments);
    Long boardId = savedBoard.getId();

    //when
    BoardDetailResponse boardDetailInfoById = boardRepository.getBoardDetailInfoById(boardId);
    entityManager.clear();

    //then
    assertThat(boardDetailInfoById.boardDetail().boardId()).isEqualTo(boardId);
    assertThat(boardDetailInfoById.boardDetail().content()).isEqualTo(savedBoard.getContent());
    assertThat(boardDetailInfoById.boardDetail().viewCount()).isEqualTo(savedBoard.getViewCount());

    for (int i = 1; i <= commentCount; i++) {
      assertThat(boardDetailInfoById.comments().get(i - 1).content()).isEqualTo("comment" + i);
    }
  }

  @Test
  @DisplayName("게시글 목록 조회시 정해진 페이지 수만큼 출력된다.")
  void fethBoardList() {
    //given
    Long cursor = null;
    int pageSize = 2;

    Member member1 = createMember("user1@test.com", "user1", "url");
    Member member2 = createMember("user2@test.com", "user2", "url");

    Board board1 = createBoard("title1", "content1", member1);
    Board board2 = createBoard("title2", "content2", member1);
    Board board3 = createBoard("title3", "content3", member2);

    List<Comment> board1CommentsByMember1 = createComments(board1, member1, 3);
    List<Comment> board1CommentsByMember2 = createComments(board1, member2, 2);
    List<Comment> board2CommentsByMember1 = createComments(board2, member1, 4);

    Likes likes1 = createLikes(board2, member1);
    Likes likes2 = createLikes(board2, member2);
    Likes likes3 = createLikes(board3, member1);

    memberRepository.saveAllAndFlush(List.of(member1, member2));
    boardRepository.saveAllAndFlush(List.of(board1, board2, board3));
    commentRepository.saveAllAndFlush(board1CommentsByMember1);
    commentRepository.saveAllAndFlush(board1CommentsByMember2);
    commentRepository.saveAllAndFlush(board2CommentsByMember1);
    likesRepository.saveAllAndFlush(List.of(likes1, likes2, likes3));

    //when
    List<BoardSimpleInfo> boardSimpleInfo = boardRepository.getBoardSimpleInfo(cursor, pageSize);

    //then
    assertThat(boardSimpleInfo).hasSize(pageSize);

    assertThat(boardSimpleInfo).extracting("title")
        .containsExactlyInAnyOrder(board3.getTitle(), board2.getTitle());

    assertThat(boardSimpleInfo).extracting("authorNickname")
        .containsExactlyInAnyOrder(member2.getNickname(), member1.getNickname());

    assertThat(boardSimpleInfo).extracting("authorProfileImg")
        .containsExactlyInAnyOrder(member2.getProfileImgUrl(), member1.getProfileImgUrl());

    assertThat(boardSimpleInfo.get(0).likeCnt()).isEqualTo(1);
    assertThat(boardSimpleInfo.get(0).commentCnt()).isZero();

    assertThat(boardSimpleInfo.get(1).likeCnt()).isEqualTo(2);
    assertThat(boardSimpleInfo.get(1).commentCnt()).isEqualTo(4);
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

  private Likes createLikes(Board board, Member member) {
    return Likes.builder()
        .board(board)
        .member(member)
        .build();
  }
}