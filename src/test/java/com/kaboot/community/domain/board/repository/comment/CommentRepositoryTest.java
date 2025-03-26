package com.kaboot.community.domain.board.repository.comment;

import static org.assertj.core.api.Assertions.assertThat;

import com.kaboot.community.config.querydsl.QueryDSLConfig;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Comment;
import com.kaboot.community.domain.board.repository.board.BoardRepository;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
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
class CommentRepositoryTest {

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private BoardRepository boardRepository;

  @Autowired
  private EntityManager entityManager;

  private Member testMember;
  private Board testBoard;

  @BeforeEach
  void setUp() {
    testMember = Member.builder()
        .username("test@example.com")
        .password("Test1!")
        .nickname("tester")
        .profileImgUrl("test.jpeg")
        .role(RoleType.ROLE_MEMBER)
        .build();
    memberRepository.save(testMember);

    testBoard = Board.builder()
        .title("Test Board")
        .content("Test Content")
        .member(testMember)
        .build();
    boardRepository.save(testBoard);
  }

  @Test
  @DisplayName("댓글 저장 및 조회 테스트")
  void saveAndFindComment() {
    // given
    Comment comment = Comment.builder()
        .content("Test Comment")
        .member(testMember)
        .board(testBoard)
        .build();

    // when
    Comment savedComment = commentRepository.save(comment);

    // then
    assertThat(savedComment.getId()).isNotNull();

    Optional<Comment> foundComment = commentRepository.findById(savedComment.getId());
    assertThat(foundComment).isPresent();
    assertThat(foundComment.get().getContent()).isEqualTo("Test Comment");
    assertThat(foundComment.get().getMember().getId()).isEqualTo(testMember.getId());
    assertThat(foundComment.get().getBoard().getId()).isEqualTo(testBoard.getId());
  }

  @Test
  @DisplayName("소프트 삭제된 댓글은 조회되지 않음 (SQLRestriction 테스트)")
  void softDeletedCommentNotFound() {
    // given
    Comment comment = Comment.builder()
        .content("Test Comment")
        .member(testMember)
        .board(testBoard)
        .build();
    Comment savedComment = commentRepository.save(comment);
    Long commentId = savedComment.getId();

    // when
    savedComment.delete(LocalDateTime.now());
    commentRepository.saveAndFlush(savedComment);
    entityManager.clear(); // 영속성 컨텍스트 초기화

    // then
    Optional<Comment> foundComment = commentRepository.findById(commentId);
    assertThat(foundComment).isEmpty();
  }

  @Test
  @DisplayName("특정 게시글의 모든 댓글 조회")
  void findAllCommentsByBoard() {
    // given
    Comment comment1 = Comment.builder()
        .content("Comment 1")
        .member(testMember)
        .board(testBoard)
        .build();

    Comment comment2 = Comment.builder()
        .content("Comment 2")
        .member(testMember)
        .board(testBoard)
        .build();

    commentRepository.saveAll(List.of(comment1, comment2));

    // when - 만약 findByBoardId 메서드가 없다면 직접 JPQL 사용
    List<Comment> comments = entityManager.createQuery(
            "SELECT c FROM Comment c WHERE c.board.id = :boardId", Comment.class)
        .setParameter("boardId", testBoard.getId())
        .getResultList();

    // then
    assertThat(comments).hasSize(2);
    assertThat(comments).extracting("content")
        .containsExactlyInAnyOrder("Comment 1", "Comment 2");
  }

  @Test
  @DisplayName("특정 게시글의 댓글 소프트 삭제 후 조회")
  void findCommentsAfterSoftDelete() {
    // given
    Comment comment1 = Comment.builder()
        .content("Comment 1")
        .member(testMember)
        .board(testBoard)
        .build();

    Comment comment2 = Comment.builder()
        .content("Comment 2")
        .member(testMember)
        .board(testBoard)
        .build();

    commentRepository.saveAll(List.of(comment1, comment2));

    // when
    comment1.delete(LocalDateTime.now());
    commentRepository.saveAndFlush(comment1);
    entityManager.clear();

    List<Comment> comments = entityManager.createQuery(
            "SELECT c FROM Comment c WHERE c.board.id = :boardId", Comment.class)
        .setParameter("boardId", testBoard.getId())
        .getResultList();

    // then - SQLRestriction으로 인해 삭제된 댓글은 제외됨
    assertThat(comments).hasSize(1);
    assertThat(comments.get(0).getContent()).isEqualTo("Comment 2");
  }
}