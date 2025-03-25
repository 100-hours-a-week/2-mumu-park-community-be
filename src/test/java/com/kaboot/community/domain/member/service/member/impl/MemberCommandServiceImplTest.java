package com.kaboot.community.domain.member.service.member.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.domain.member.dto.response.ExistResponse;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import com.kaboot.community.domain.member.service.password.PasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@Profile("test")
@ExtendWith(MockitoExtension.class)
class MemberCommandServiceImplTest {
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
    @DisplayName("본인이 아닌 경우 프로필 수정 실패")
    void profileUpdateFailNotAuthorize() throws Exception {
        // given
        String differentUsername = "different@test.com";
        ModifyRequest validModifyRequest = new ModifyRequest("newNickname", "newProfile.jpeg");
        Member member = createMember();

        when(memberQueryService.getMemberByUsername(anyString())).thenReturn(member);

        // then, when
        assertThatThrownBy(() -> commandService.update(differentUsername, validModifyRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.ACCESS_DENIED.getMessage());
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

    @Test
    @DisplayName("본인이 아닌 경우 비밀번호 변경 실패")
    void passwordUpdateFailNotAuthorize() throws Exception {
        // given
        String differentUsername = "different@test.com";
        PasswordUpdateRequest passwordUpdateRequest = new PasswordUpdateRequest("newpwd");
        Member member = createMember();

        when(memberQueryService.getMemberByUsername(anyString())).thenReturn(member);

        // then, when
        assertThatThrownBy(() -> commandService.updatePassword(differentUsername, passwordUpdateRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("자신이 탈퇴요청을 한 경우 회원 탈퇴 성공")
    void withdrawalSuccess() {
        // given
        String authUsername = "member@test.com";
        Member member = createMember();

        when(memberQueryService.getMemberByUsername(authUsername)).thenReturn(member);

        // when
        commandService.withdrawal(authUsername);

        // then
        assertThat(member.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("본인이 아닌 경우 탈퇴 실패")
    void withdrawalFailNotAuthorize() throws Exception {
        // given
        String differentUsername = "different@test.com";
        Member member = createMember();

        when(memberQueryService.getMemberByUsername(anyString())).thenReturn(member);

        // then, when
        assertThatThrownBy(() -> commandService.withdrawal(differentUsername))
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
}