package com.kaboot.community.domain.member.service.member.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.member.dto.response.MemberInfoResponse;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Profile("test")
@Transactional
@ExtendWith(MockitoExtension.class)
class MemberQueryServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberQueryServiceImpl memberQueryService;

    @Test
    @DisplayName("DB에 username이 존재하는 경우 username으로 유저 조회성공")
    void getMemberInfoByUsernameSuccess() {
        //given
        String existUsername = "member@test.com";
        Member getMember = createMember();

        when(memberRepository.findByUsername(existUsername)).thenReturn(Optional.ofNullable(getMember));

        //when
        MemberInfoResponse actualMemberInfoResponse = memberQueryService.getMemberInfoByUsername(existUsername);

        //then
        assertThat(actualMemberInfoResponse.email()).isEqualTo(getMember.getUsername());
        assertThat(actualMemberInfoResponse.nickname()).isEqualTo(getMember.getNickname());
        assertThat(actualMemberInfoResponse.profileImg()).isEqualTo(getMember.getProfileImgUrl());
    }

    @Test
    @DisplayName("DB에 username이 존재하지 않는 경우 username으로 유저 조회실패")
    void getMemberInfoByUsernameFailNotExistUsername() {
        //given
        String notExistUsername = "notexist@test.com";

        when(memberRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        //then, when
        assertThatThrownBy(() -> memberQueryService.getMemberInfoByUsername(notExistUsername))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("DB에 email이 존재하는 경우 true 반환")
    void returnTrueExistEmailInDB() {
        //given
        String existEmail = "member@test.com";

        when(memberRepository.existsByUsername(existEmail)).thenReturn(true);

        //when
        boolean actualEmailExistValue = memberQueryService.isEmailDuplicate(existEmail);

        //then
        assertThat(actualEmailExistValue).isTrue();
    }

    @Test
    @DisplayName("DB에 email이 존재하지 않는 경우 false 반환")
    void returnFalseExistEmailInDB() {
        //given
        String notExistEmail = "notExist@test.com";

        when(memberRepository.existsByUsername(notExistEmail)).thenReturn(false);

        //when
        boolean actualEmailExistValue = memberQueryService.isEmailDuplicate(notExistEmail);

        //then
        assertThat(actualEmailExistValue).isFalse();
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
}