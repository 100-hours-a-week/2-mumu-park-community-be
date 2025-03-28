package com.kaboot.community.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaboot.community.config.jwt.enums.TokenType;
import com.kaboot.community.config.jwt.filter.JwtAuthenticationFilter;
import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.auth.AuthService;
import com.kaboot.community.util.jwt.JwtUtil;
import com.kaboot.community.util.redis.RedisUtil;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
@Transactional
class AuthControllerTest {

  @InjectMocks
  private AuthController authController;

  @Mock
  private AuthService authService;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private RedisUtil redisUtil;

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
        .addFilter(new JwtAuthenticationFilter(jwtUtil, redisUtil))
//        .standaloneSetup(authController)
//        .setControllerAdvice(new CustomExceptionHandler())
        .apply(springSecurity())
        .build();

    testMember = Member.builder()
        .username("test@example.com")
        .password("Test1!")
        .nickname("tester")
        .profileImgUrl("test.jpeg")
        .role(RoleType.ROLE_MEMBER)
        .build();

    Member savedMember = memberRepository.save(testMember);

    accessToken = jwtUtil.createToken(savedMember.getId(), TokenType.ACCESS_TOKEN,
        savedMember.getRole());

    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("회원가입 성공시 201 반환")
  void registerSuccessWithValidRequest() throws Exception {
    // given
    RegisterRequest validRegisterRequest = new RegisterRequest("test@test.com", "pwd1!", "testname",
        "image");
    doNothing().when(authService).register(any(RegisterRequest.class));

    authService.register(validRegisterRequest);
    // when & then
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRegisterRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatusCode").value(NO_CONTENT.value()))
        .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
        .andExpect(jsonPath("$.data").doesNotExist());
  }

  static Stream<Object[]> invalidRegistrationData() {
    return Stream.of(
        new Object[]{"", "pwd1", "testName", "image", "email", "must not be empty"},
        new Object[]{"test@test.com", "", "testName", "image", "password", "must not be empty"},
        new Object[]{"test@test.com", "testName", "", "image", "nickname", "must not be empty"},
        new Object[]{"invalid", "pwd1", "testName", "image", "email", "must not be empty"}
    );
  }

  @ParameterizedTest
  @MethodSource("invalidRegistrationData")
  @DisplayName("회원가입시 필수 필드가 비어있다면 400 오류 발생")
  void registerFailWithInvalidRequest(
      String email, String password, String nickname, String profileImage, String fieldName
  ) throws Exception {
    // given
    RegisterRequest invalidRegisterRequest = new RegisterRequest(email, password, nickname,
        profileImage);

    // when & then
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRegisterRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.httpStatusCode").value(400))
        .andExpect(jsonPath("$.message").value("유효하지 않은 데이터입니다."))
        .andExpect(jsonPath("$.data." + fieldName).exists());
  }

  @Test
  @DisplayName("로그아웃 통합 테스트 - 성공 케이스")
  void logoutSuccess() throws Exception {
    //when, then
    mockMvc.perform(post("/auth/logout")
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.httpStatusCode").value(204))
        .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("액세스 토큰이 헤더에 없는 경우 로그아웃 실패")
  void logoutFailWithoutAT() throws Exception {
    //when, then
    mockMvc.perform(post("/auth/logout")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
//        .andExpect(jsonPath("$.httpStatusCode").value(HttpStatus.BAD_REQUEST.value()))
//        .andExpect(jsonPath("$.message").value("토큰이 공백입니다."))
//        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("토큰 재발급 테스트")
  void reissueSuccess() {
    //given

    //when

    //then
  }


}