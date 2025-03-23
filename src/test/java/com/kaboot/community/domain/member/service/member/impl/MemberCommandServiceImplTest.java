package com.kaboot.community.domain.member.service.member.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.domain.member.dto.response.ExistResponse;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import com.kaboot.community.domain.member.service.password.PasswordEncoder;
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

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Profile("test")
@Transactional
@ExtendWith(MockitoExtension.class)
class MemberCommandServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberCommandServiceImpl commandService;

    @Test
    @DisplayName("유효한 닉네임과 프로필 이미지 URL이 주어진다면 프로필 수정 성공")
    void profileUpdateSuccess() throws Exception {
        // given
        String authUsername = "member@test.com";
        ModifyRequest validModifyRequest = new ModifyRequest("newNickname", "newProfile.jpeg");
        Member member = createMember();

        when(memberQueryService.getMemberByUsername(anyString())).thenReturn(member);
        when(memberQueryService.isNicknameDuplicate(anyString())).thenReturn(new ExistResponse(false));

        // when
        commandService.update(authUsername, validModifyRequest);

        // then
        assertThat(member.getNickname()).isEqualTo(validModifyRequest.nickname());
        assertThat(member.getProfileImgUrl()).isEqualTo(validModifyRequest.profileImg());
    }

    @Test
    @DisplayName("닉네임이 중복된 경우 프로필 수정 실패")
    void profileUpdateFailDuplicateNickname() throws Exception {
        // given
        String authUsername = "member@test.com";
        ModifyRequest validModifyRequest = new ModifyRequest("newNickname", "newProfile.jpeg");
        Member member = createMember();

        when(memberQueryService.getMemberByUsername(anyString())).thenReturn(member);
        when(memberQueryService.isNicknameDuplicate(anyString())).thenReturn(new ExistResponse(true));

        // then, when
        assertThatThrownBy(() -> commandService.update(authUsername, validModifyRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.NICKNAME_ALREADY_EXIST.getMessage());
    }
    
    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePasswordSuccess() throws Exception {
        //given
        String authUsername = "member@test.com";
        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest("newPassword");
        Member member = createMember();

        when(memberQueryService.getMemberByUsername(anyString())).thenReturn(member);
        when(passwordEncoder.hash(anyString())).thenReturn(passwordUpdateRequest.newPassword());

        //when
        commandService.updatePassword(authUsername, passwordUpdateRequest);

        //then
        assertThat(member.getPassword()).isEqualTo(passwordUpdateRequest.newPassword());
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