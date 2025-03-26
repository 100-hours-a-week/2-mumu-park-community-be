package com.kaboot.community.domain.member.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.kaboot.community.config.querydsl.QueryDSLConfig;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
class MemberRepositoryTest {

  @Autowired
  private EntityManager em;

  @Autowired
  private MemberRepository memberRepository;

  @Test
  @DisplayName("username으로 멤버 조회 테스트")
  void fetchByUsername() {
    // given
    List<Member> members = createMembers();
    memberRepository.saveAll(members);

    // when
    Optional<Member> foundMember = memberRepository.findByUsername("member1@test.com");
    Optional<Member> nonExistentMember = memberRepository.findByUsername("nonexistent@test.com");

    // then
    assertThat(foundMember).isPresent();
    assertThat(foundMember.get().getUsername()).isEqualTo("member1@test.com");
    assertThat(foundMember.get().getNickname()).isEqualTo("test1");
    assertThat(foundMember.get().getProfileImgUrl()).isEqualTo("test1.jpeg");

    assertThat(nonExistentMember).isEmpty();
  }

  @Test
  @DisplayName("username이 존재하는 경우 true, 존재 안하면 false 반환")
  void existsByUsername() {
    // given
    List<Member> members = createMembers();
    memberRepository.saveAll(members);

    // when
    boolean actualMemberExists = memberRepository.existsByUsername("member1@test.com");
    boolean actualNonMemberExists = memberRepository.existsByUsername("nonexistent@test.com");

    // then
    assertThat(actualMemberExists).isTrue();
    assertThat(actualNonMemberExists).isFalse();
  }

  @Test
  @DisplayName("nickname 존재하는 경우 true, 존재 안하면 false 반환")
  void existsByNickname() {
    // given
    List<Member> members = createMembers();
    memberRepository.saveAll(members);

    // when
    boolean actualMemberExists = memberRepository.existsByNickname("test1");
    boolean actualNonMemberExists = memberRepository.existsByNickname("non100");

    // then
    assertThat(actualMemberExists).isTrue();
    assertThat(actualNonMemberExists).isFalse();
  }

  @Test
  @DisplayName("username가 같은 경우 예외 발생")
  void usernameDataIntegrityError() {
    //given
    Member member1 = createMember(1L, "duplicate@test.com");
    Member member2 = createMember(2L, "duplicate@test.com");

    //when
    memberRepository.save(member1);

    // then
    assertThatThrownBy(() -> {
      memberRepository.save(member2);
      memberRepository.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("삭제된 멤버는 조회되지 않음 (SQLRestriction 테스트)")
  void notFetchDeletedMember() {
    // given
    Member member = Member.builder()
        .username("test@example.com")
        .password("Test1!")
        .nickname("testUser")
        .profileImgUrl("test.jpeg")
        .role(RoleType.ROLE_MEMBER)
        .build();

    Member savedMember = memberRepository.save(member);
    Long memberId = savedMember.getId();

    // when
    savedMember.withdrawal(LocalDateTime.now());
    memberRepository.save(savedMember);
    memberRepository.flush();

    // 영속성 컨텍스트를 비움으로써 DB에서 다시 조회하도록 적용
    em.clear();

    // then
    Optional<Member> foundMember = memberRepository.findById(memberId);
    assertThat(foundMember).isEmpty();
  }

  private List<Member> createMembers() {
    List<Member> members = new ArrayList<>();

    for (int i = 1; i <= 5; i++) {
      members.add(Member.builder()
          .id((long) i)
          .username("member" + i + "@test.com")
          .password("Test1!")
          .nickname("test" + i)
          .profileImgUrl("test" + i + ".jpeg")
          .role(RoleType.ROLE_MEMBER)
          .build());
    }

    return members;
  }

  private Member createMember(Long id, String username) {
    return Member.builder()
        .id(id)
        .username(username)
        .password("Test1!")
        .nickname("nickname")
        .profileImgUrl("test.jpeg")
        .role(RoleType.ROLE_MEMBER)
        .build();
  }
}