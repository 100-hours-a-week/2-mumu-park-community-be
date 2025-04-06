package com.kaboot.community.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaboot.community.config.jwt.enums.TokenType;
import com.kaboot.community.config.jwt.filter.JwtAuthenticationFilter;
import com.kaboot.community.domain.member.dto.request.LoginRequest;
import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.auth.AuthService;
import com.kaboot.community.domain.member.service.password.PasswordEncoder;
import com.kaboot.community.util.jwt.JwtUtil;
import com.kaboot.community.util.redis.RedisUtil;
import jakarta.servlet.http.Cookie;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.MvcResult;
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

  @Autowired
  private PasswordEncoder passwordEncoder;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private String accessToken;
  private Member testMember;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .addFilter(new JwtAuthenticationFilter(jwtUtil, redisUtil))
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

    accessToken = "Bearer " + jwtUtil.createToken(savedMember.getId(), TokenType.ACCESS_TOKEN,
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
  @DisplayName("로그인 성공 시 accessToken 반환 및 Set-Cookie에 refreshToken 포함")
  void loginSuccess() throws Exception {
    // given
    String username = "test123@example.com";
    String password = "Test1!";
    String nickname = "tester";

    Member member = Member.builder()
        .username(username)
        .password(passwordEncoder.hash(password))
        .nickname(nickname)
        .profileImgUrl("profile.jpg")
        .role(RoleType.ROLE_MEMBER)
        .build();

    memberRepository.save(member);

    LoginRequest loginRequest = new LoginRequest(username, password);

    // when, then
    mockMvc.perform(post("/auth/tokens")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("로그인이 완료되었습니다."))
        .andExpect(jsonPath("$.data.accessToken").exists())
        .andExpect(header().string("Set-Cookie", Matchers.containsString("refreshToken=")))
        .andExpect(header().string("Set-Cookie", Matchers.containsString("HttpOnly")))
        .andExpect(header().string("Set-Cookie", Matchers.containsString("Path=/")));
  }

  @Test
  @DisplayName("로그아웃 성공 - 로그인 후 로그아웃 흐름")
  void logoutSuccess() throws Exception {
    // 1. 로그인 세팅
    String username = "testlogout@example.com";
    String password = "Test1!";
    String nickname = "tester";

    Member member = Member.builder()
        .username(username)
        .password(passwordEncoder.hash(password))  // 저장 시 해시 필요
        .nickname(nickname)
        .profileImgUrl("profile.jpg")
        .role(RoleType.ROLE_MEMBER)
        .build();
    memberRepository.save(member);

    LoginRequest loginRequest = new LoginRequest(username, password);

    MvcResult loginResult = mockMvc.perform(post("/auth/tokens")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").exists())
        .andReturn();

    // 2. accessToken 추출
    String responseBody = loginResult.getResponse().getContentAsString();
    String accessToken = "Bearer " + objectMapper.readTree(responseBody)
        .get("data").get("accessToken").asText();

    // 3. 로그아웃 요청
    mockMvc.perform(post("/auth/logout")
            .header("Authorization", accessToken)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("로그아웃이 완료되었습니다."))
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @DisplayName("토큰 재발급 성공 - 로그인 후 재발급")
  void reissueSuccessWithLogin() throws Exception {
    // given
    String email = "test123@example.com";
    String password = "Test1!";

    Member member = Member.builder()
        .username(email)
        .password(passwordEncoder.hash(password))
        .nickname("tester")
        .profileImgUrl("img.jpg")
        .role(RoleType.ROLE_MEMBER)
        .build();
    memberRepository.save(member);

    // 1. 로그인 요청
    LoginRequest loginRequest = new LoginRequest(email, password);

    MvcResult loginResult = mockMvc.perform(post("/auth/tokens")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("refreshToken"))
        .andReturn();

    // 2. refreshToken 추출
    String refreshToken = loginResult.getResponse().getCookie("refreshToken").getValue();
    
    // 3. 토큰 재발급 요청
    mockMvc.perform(post("/auth/reissue")
            .cookie(new Cookie("refreshToken", refreshToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("토큰 재발급 성공"))
        .andExpect(jsonPath("$.data.accessToken").exists())
        .andExpect(cookie().exists("refreshToken"));
  }
}