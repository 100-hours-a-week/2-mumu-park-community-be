package com.kaboot.community.domain.member.service.auth;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.config.jwt.dto.AuthTokens;
import com.kaboot.community.config.jwt.dto.TokenInfo;
import com.kaboot.community.config.jwt.enums.TokenType;
import com.kaboot.community.domain.member.dto.request.LoginRequest;
import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.dto.response.ExistResponse;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import com.kaboot.community.domain.member.service.password.BCryptPasswordEncoder;
import com.kaboot.community.util.jwt.JwtUtil;
import com.kaboot.community.util.jwt.TokenGenerator;
import com.kaboot.community.util.redis.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

//@SpringBootTest
@Profile("test")
@Transactional
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private TokenGenerator tokenGenerator;
    @Mock
    private MemberQueryService memberQueryService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입 요청이 유효할 때, 회원가입에 성공한다.")
    void registerWithValidRequest() {
        // given
        RegisterRequest validRequest = new RegisterRequest("test@test.com", "test1!", "testName", "http://test.jpeg");
        when(memberQueryService.isEmailDuplicate(anyString())).thenReturn(new ExistResponse(false));
        // when
        authService.register(validRequest);

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(passwordEncoder, times(1)).hash(any(String.class));
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 회원가입 하는 경우, 회원가입에 실패한다.")
    void existMemberRegister() {
        // given
        RegisterRequest invalidRequest = new RegisterRequest("test@test.com", "test1!", "testName", "http://test.jpeg");
        when(memberQueryService.isEmailDuplicate(any(String.class))).thenReturn(new ExistResponse(true));

        // then
        assertThatThrownBy(() -> authService.register(invalidRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.MEMBER_ALREADY_EXIST.getMessage());
    }

    @Test
    @DisplayName("유효한 아이디, 비밀번호 입력 및 레디스에 기존 RT가 있는경우, 로그인에 성공한다.")
    void loginSuccessWithRefreshTokenInRedis() {
        //given
        LoginRequest validLoginRequest = new LoginRequest("member@test.com", "Test1!");
        Member mockMember = createMember();
        AuthTokens mockAuthTokens = new AuthTokens("validAT", "validRT");

        when(memberQueryService.getMemberByUsername(validLoginRequest.username())).thenReturn(mockMember);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(redisUtil.getData(anyString())).thenReturn(mockAuthTokens.refreshToken());
        when(tokenGenerator.generateToken(anyLong(), any(RoleType.class))).thenReturn(mockAuthTokens);
        //when
        AuthTokens authTokens = authService.login(validLoginRequest);

        //then
        assertThat(authTokens.accessToken()).isEqualTo(mockAuthTokens.accessToken());
        assertThat(authTokens.refreshToken()).isEqualTo(mockAuthTokens.refreshToken());
    }

    @Test
    @DisplayName("유효한 아이디, 비밀번호 입력 및 레디스에 기존 RT가 없는경우, 로그인에 성공한다.")
    void loginSuccessWithoutRefreshTokenInRedis() {
        //given
        LoginRequest validLoginRequest = new LoginRequest("member@test.com", "Test1!");
        Member mockMember = createMember();
        AuthTokens mockAuthTokens = new AuthTokens("validAT", "validRT");

        when(memberQueryService.getMemberByUsername(validLoginRequest.username())).thenReturn(mockMember);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(redisUtil.getData(anyString())).thenReturn(null);
        when(jwtUtil.createToken(anyLong(), any(TokenType.class), any(RoleType.class))).thenReturn(mockAuthTokens.refreshToken());
        when(tokenGenerator.generateTokenWithRF(anyLong(), anyString(), any(RoleType.class))).thenReturn(mockAuthTokens);

        //when
        AuthTokens authTokens = authService.login(validLoginRequest);

        //then
        assertThat(authTokens.accessToken()).isEqualTo(mockAuthTokens.accessToken());
        assertThat(authTokens.refreshToken()).isEqualTo(mockAuthTokens.refreshToken());
    }
    @Test
    @DisplayName("존재하지 않는 아이디로 접근시, 로그인에 실패한다.")
    void loginFailInvalidUsername() {
        //given
        LoginRequest invalidLoginRequest = new LoginRequest("mem@test.com", "Test1!");
        when(memberQueryService.getMemberByUsername(anyString())).thenThrow(new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        //then
        assertThatThrownBy(() ->
                authService.login(invalidLoginRequest)
                ).isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("유효하지 않은 비밀번호로 접근시, 로그인에 실패한다.")
    void loginFailInvalidPassword() {
        //given
        LoginRequest invalidLoginRequest = new LoginRequest("mem@test.com", "invalid!!");
        Member mockMember = createMember();
        when(memberQueryService.getMemberByUsername(anyString())).thenReturn(mockMember);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        //then
        assertThatThrownBy(() ->
                authService.login(invalidLoginRequest)
        ).isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.PASSWORD_NOT_MATCH.getMessage());
    }
    
    @Test
    @DisplayName("유효한 리프레시 토큰 입력시, 토큰 재발급 성공")
    void reissueSuccess() {
        //given
        String validRefreshToken = "validRefreshToken";
        String validNewRefreshToken = "validNewRefreshToken";
        Member mockMember = createMember();
        TokenInfo validTokenInfo = new TokenInfo(1L, RoleType.ROLE_MEMBER.toString());
        AuthTokens mockAuthTokens = new AuthTokens("AT", validNewRefreshToken);

        when(jwtUtil.getTokenClaims(validRefreshToken)).thenReturn(validTokenInfo);
        when(redisUtil.getData(anyString())).thenReturn(validRefreshToken);
        when(memberQueryService.getMemberById(anyLong())).thenReturn(mockMember);
        when(tokenGenerator.generateToken(anyLong(), any(RoleType.class))).thenReturn(mockAuthTokens);

        //when
        AuthTokens actualAuthTokens = authService.reissue(validRefreshToken);

        //then
        assertThat(actualAuthTokens.accessToken()).isEqualTo(mockAuthTokens.accessToken());
        assertThat(actualAuthTokens.refreshToken()).isEqualTo(mockAuthTokens.refreshToken());
    }

    @Test
    @DisplayName("레디스에 RT가 존재하지 않을 경우, 토큰 재발급 실패")
    void reissueFailNotExistInRedis() {
        //given
        String validRefreshToken = "validRefreshToken";
        TokenInfo validTokenInfo = new TokenInfo(1L, RoleType.ROLE_MEMBER.toString());

        when(jwtUtil.getTokenClaims(validRefreshToken)).thenReturn(validTokenInfo);
        when(redisUtil.getData(anyString())).thenReturn(null);

        // then
        assertThatThrownBy(() -> authService.reissue(validRefreshToken))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.REFRESH_TOKEN_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("레디스에 존재하는 RT와 일치하지 않을 경우, 토큰 재발급 실패")
    void reissueFailWithInvalidRT() {
        //given
        String invalidRefreshToken = "invalidRefreshToken";
        String refreshTokenInRedis = "RefreshTokenInRedis";
        Member mockMember = createMember();
        TokenInfo validTokenInfo = new TokenInfo(1L, RoleType.ROLE_MEMBER.toString());

        when(jwtUtil.getTokenClaims(invalidRefreshToken)).thenReturn(validTokenInfo);
        when(redisUtil.getData(anyString())).thenReturn(refreshTokenInRedis);

        // then
        assertThatThrownBy(() -> authService.reissue(invalidRefreshToken))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.REFRESH_TOKEN_NOT_MATCH.getMessage());
    }

    @Test
    @DisplayName("클레임 속 유저의 id로 유저가 조회되지 않을 경우, 토큰 재발급 실패")
    void reissueFailMemberNotExist() {
        //given
        String validRefreshToken = "validRefreshToken";
        String refreshTokenInRedis = "validRefreshToken";
        TokenInfo validTokenInfo = new TokenInfo(1L, RoleType.ROLE_MEMBER.toString());

        when(jwtUtil.getTokenClaims(validRefreshToken)).thenReturn(validTokenInfo);
        when(redisUtil.getData(anyString())).thenReturn(refreshTokenInRedis);
        when(memberQueryService.getMemberById(anyLong())).thenThrow(new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        // then
        assertThatThrownBy(() -> authService.reissue(validRefreshToken))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.MEMBER_NOT_EXIST.getMessage());
    }

    @Test
    @DisplayName("유효한 액세스토큰으로 요청시, 로그아웃 성공")
    void logoutSuccess() {
        //given
        String validAccessTokenBeforeHandling = "validAT.askdfj.alksdjf";
        String validAccessToken = "validAT";
        String validRefreshToken = "validRTInRedis";
        TokenInfo validTokenInfo = new TokenInfo(1L, RoleType.ROLE_MEMBER.toString());

        when(jwtUtil.resolveToken(validAccessTokenBeforeHandling)).thenReturn(validAccessToken);
        when(jwtUtil.getTokenClaims(validAccessToken)).thenReturn(validTokenInfo);
        when(redisUtil.getData(anyString())).thenReturn(validRefreshToken);

        //when
        authService.logout(validAccessTokenBeforeHandling);

        //then
        verify(redisUtil, times(1)).deleteDate(anyString());
        verify(redisUtil, times(1)).setData(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("레디스에 리프레시 토큰이 존재하지 않는 경우, 로그아웃 실패")
    void logoutFailNotExistInRedis() {
        //given
        String validAccessTokenBeforeHandling = "validAT.askdfj.alksdjf";
        String validAccessToken = "validAT";
        TokenInfo validTokenInfo = new TokenInfo(1L, RoleType.ROLE_MEMBER.toString());

        when(jwtUtil.resolveToken(validAccessTokenBeforeHandling)).thenReturn(validAccessToken);
        when(jwtUtil.getTokenClaims(validAccessToken)).thenReturn(validTokenInfo);
        when(redisUtil.getData(anyString())).thenReturn(null);

        // then, when
        assertThatThrownBy(() -> authService.logout(validAccessTokenBeforeHandling))
                .isInstanceOf(CustomException.class)
                .hasMessage(CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND.getMessage());
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