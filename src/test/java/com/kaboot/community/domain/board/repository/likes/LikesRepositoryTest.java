package com.kaboot.community.domain.board.repository.likes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kaboot.community.config.querydsl.QueryDSLConfig;
import com.kaboot.community.domain.board.entity.Board;
import com.kaboot.community.domain.board.entity.Likes;
import com.kaboot.community.domain.board.repository.board.BoardRepository;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDSLConfig.class})
class LikesRepositoryTest {

  @Autowired
  private LikesRepository likesRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private BoardRepository boardRepository;

  private Member testMember;
  private Board testBoard;

  @BeforeEach
  void setUp() {
    testMember = createMember("test@example.com", "tester", "test.jpeg");
    memberRepository.save(testMember);

    testBoard = createBoard("Test Board", "Test Content", testMember);
    boardRepository.save(testBoard);
  }

  @Test
  @DisplayName("좋아요 저장 및 ID로 조회 테스트")
  void saveAndFindById() {
    // given
    Likes likes = createLikes(testMember, testBoard);

    // when
    Likes savedLikes = likesRepository.save(likes);

    // then
    assertThat(savedLikes.getId()).isNotNull();

    Optional<Likes> foundLikes = likesRepository.findById(savedLikes.getId());
    assertThat(foundLikes).isPresent();
    assertThat(foundLikes.get().getMember().getId()).isEqualTo(testMember.getId());
    assertThat(foundLikes.get().getBoard().getId()).isEqualTo(testBoard.getId());
  }

  @Test
  @DisplayName("게시글 ID와 멤버 ID로 좋아요 조회 테스트")
  void findByBoardIdAndMemberId() {
    // given
    Likes likes = createLikes(testMember, testBoard);
    likesRepository.save(likes);

    // when
    Optional<Likes> foundLikes = likesRepository.findByBoardIdAndMemberId(
        testBoard.getId(), testMember.getId());

    // then
    assertThat(foundLikes).isPresent();
    assertThat(foundLikes.get().getMember().getId()).isEqualTo(testMember.getId());
    assertThat(foundLikes.get().getBoard().getId()).isEqualTo(testBoard.getId());
  }

  @Test
  @DisplayName("존재하지 않는 게시글 ID와 멤버 ID로 좋아요 조회 시 빈 Optional 반환")
  void findByNonExistentBoardIdAndMemberId() {
    // given
    Long nonExistentBoardId = 999L;
    Long nonExistentMemberId = 999L;

    // when
    Optional<Likes> foundLikes = likesRepository.findByBoardIdAndMemberId(
        nonExistentBoardId, nonExistentMemberId);

    // then
    assertThat(foundLikes).isEmpty();
  }

  @Test
  @DisplayName("좋아요 삭제 테스트")
  void deleteLikes() {
    // given
    Likes likes = createLikes(testMember, testBoard);
    Likes savedLikes = likesRepository.save(likes);
    Long likesId = savedLikes.getId();

    // when
    likesRepository.delete(savedLikes);
    likesRepository.flush();

    // then
    Optional<Likes> deletedLikes = likesRepository.findById(likesId);
    assertThat(deletedLikes).isEmpty();

    Optional<Likes> deletedLikesByBoardAndMember = likesRepository.findByBoardIdAndMemberId(
        testBoard.getId(), testMember.getId());
    assertThat(deletedLikesByBoardAndMember).isEmpty();
  }

  @Test
  @DisplayName("동일한 멤버가 동일한 게시글에 여러 좋아요를 생성할 수 없음 (유니크 제약조건)")
  void uniqueConstraintViolation() {
    // given
    Likes likes1 = createLikes(testMember, testBoard);
    likesRepository.save(likes1);

    Likes likes2 = createLikes(testMember, testBoard);

    // when, then
    assertThatThrownBy(() -> {
      likesRepository.save(likes2);
      likesRepository.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("여러 멤버가 동일한 게시글에 좋아요를 할 수 있음")
  void multipleUsersCanLikeSameBoard() {
    // given
    Member anotherMember = createMember("another@example.com", "another", "another.jpeg");
    memberRepository.save(anotherMember);

    Likes likes1 = createLikes(testMember, testBoard);
    Likes likes2 = createLikes(anotherMember, testBoard);

    // when
    likesRepository.saveAll(List.of(likes1, likes2));
    likesRepository.flush();

    // then
    Optional<Likes> foundLikes1 = likesRepository.findByBoardIdAndMemberId(
        testBoard.getId(), testMember.getId());

    Optional<Likes> foundLikes2 = likesRepository.findByBoardIdAndMemberId(
        testBoard.getId(), anotherMember.getId());

    assertThat(foundLikes1).isPresent();
    assertThat(foundLikes2).isPresent();
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
        .build();
  }

  private Likes createLikes(Member member, Board board) {
    return Likes.builder()
        .member(member)
        .board(board)
        .build();
  }
}