package com.kaboot.community.domain.member.service.member.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.config.security.member.PrincipalDetails;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.domain.member.dto.response.ExistResponse;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.member.MemberCommandService;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import com.kaboot.community.domain.member.service.password.PasswordEncoder;
import com.kaboot.community.util.test.WithCustomMember;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.DenyAllPermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.ExpressionBasedAnnotationAttributeFactory;
import org.springframework.security.access.expression.method.ExpressionBasedPreInvocationAdvice;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PreInvocationAuthorizationAdviceVoter;
import org.springframework.security.access.prepost.PrePostAnnotationSecurityMetadataSource;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@Profile("test")
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

    private MemberCommandService memberCommandServiceProxy;

    @BeforeEach
    void setUp() {
        // 실제 서비스 인스턴스 생성
        MemberCommandServiceImpl realService = new MemberCommandServiceImpl(memberQueryService, passwordEncoder);

        // ExpressionHandler 생성
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();

        // PrePostInvocationAttributeFactory 생성
        ExpressionBasedAnnotationAttributeFactory attributeFactory =
                new ExpressionBasedAnnotationAttributeFactory(expressionHandler);
        // 메서드 보안 인터셉터 설정
        MethodSecurityInterceptor interceptor = new MethodSecurityInterceptor();
        interceptor.setSecurityMetadataSource(new PrePostAnnotationSecurityMetadataSource(attributeFactory));

        // AccessDecisionManager 설정
        List<AccessDecisionVoter<?>> voters = new ArrayList<>();
        ExpressionBasedPreInvocationAdvice preInvocationAdvice = new ExpressionBasedPreInvocationAdvice();
        preInvocationAdvice.setExpressionHandler(expressionHandler);
        voters.add(new PreInvocationAuthorizationAdviceVoter(preInvocationAdvice));

        AffirmativeBased accessDecisionManager = new AffirmativeBased(voters);
        interceptor.setAccessDecisionManager(accessDecisionManager);

        // 간단한 인증 관리자 설정
        interceptor.setAuthenticationManager(authentication -> authentication);

        // 프록시 생성
        ProxyFactory factory = new ProxyFactory(realService);
        factory.addAdvice(interceptor);

        // 프록시 객체 얻기
        memberCommandServiceProxy = (MemberCommandService) factory.getProxy();
    }

    @Test
    @DisplayName("본인이 아닌 경우 프로필 수정 실패")
    @WithCustomMember(username = "different@test.com")
    void profileUpdateFailNotAuthorize() throws Exception {
        // SecurityContext 설정
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                new PrincipalDetails(Member.builder().username("different@test.com").build()),
                null,
                List.of(new SimpleGrantedAuthority("MEMBER"))
        );

        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // given
        String authUsername = "member@test.com";
        ModifyRequest validModifyRequest = new ModifyRequest("newNickname", "newProfile.jpeg");

        // then, when
        assertThatThrownBy(() -> memberCommandServiceProxy.update(authUsername, validModifyRequest))
                .isInstanceOf(AccessDeniedException.class);

        // 테스트 후 SecurityContext 정리
        SecurityContextHolder.clearContext();
    }

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

//    @Test
//    @DisplayName("본인이 아닌 경우 프로필 수정 실패")
//    @WithCustomMember(username = "different@test.com")
//    void profileUpdateFailNotAuthorize() throws Exception {
//        // given
//        Member member = createMember();
//        String authUsername = "member@test.com";
//        ModifyRequest validModifyRequest = new ModifyRequest("newNickname", "newProfile.jpeg");
//
//        when(memberQueryService.getMemberByUsername(anyString())).thenReturn(member);
//        when(memberQueryService.isNicknameDuplicate(anyString())).thenReturn(new ExistResponse(false));
//
//        // then, when
//        assertThatThrownBy(() -> commandService.update(authUsername, validModifyRequest))
//                .isInstanceOf(AccessDeniedException.class)
//                .hasMessage("Access Denied");
//    }

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