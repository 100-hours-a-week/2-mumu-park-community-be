package com.kaboot.community.domain.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaboot.community.config.jwt.enums.TokenType;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.util.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberControllerTest {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private JwtUtil jwtUtil;

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
  @DisplayName("이메일이 존재하면 200응답 및 isExist : true 반환")
  void existEmailReturn200AndTrue() throws Exception {
    // given
    String email = "test@example.com";

    // when & then
    mockMvc.perform(get("/users/email")
            .param("email", email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isExist").value(true))
        .andExpect(jsonPath("$.message").value("이메일 중복 체크에 성공하였습니다."));
  }

  @Test
  @DisplayName("이메일이 존재하지 않으면 200응답 및 isExist : false 반환")
  void notExistEmailReturn200AndFalse() throws Exception {
    // given
    String email = "notexist@example.com";

    // when & then
    mockMvc.perform(get("/users/email")
            .param("email", email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isExist").value(false))
        .andExpect(jsonPath("$.message").value("이메일 중복 체크에 성공하였습니다."));
  }

  @Test
  @DisplayName("닉네임이 존재하면 200응답 및 isExist : true 반환")
  void existNicknameReturn200AndTrue() throws Exception {
    // given
    String existNickname = testMember.getNickname();

    // when & then
    mockMvc.perform(get("/users/nickname")
            .param("nickname", existNickname))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isExist").value(true))
        .andExpect(jsonPath("$.message").value("닉네임 중복 체크에 성공하였습니다."));
  }

  @Test
  @DisplayName("닉네임 존재하지 않으면 200응답 및 isExist : false 반환")
  void notExistNicknameReturn200AndFalse() throws Exception {
    // given
    String notExistNickname = "notexist";

    // when & then
    mockMvc.perform(get("/users/nickname")
            .param("nickname", notExistNickname))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isExist").value(false))
        .andExpect(jsonPath("$.message").value("닉네임 중복 체크에 성공하였습니다."));
  }

  @Test
  @DisplayName("DB에 회원이 존재하는 경우 회원 정보 조회 성공")
  void getMemberInfoSuccess() throws Exception {
    // when & then
    mockMvc.perform(
            get("/users")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nickname").value(testMember.getNickname()))
        .andExpect(jsonPath("$.data.email").value(testMember.getUsername()))
        .andExpect(jsonPath("$.data.profileImg").value(testMember.getProfileImgUrl()))
        .andExpect(jsonPath("$.message").value("유저 정보 조회에 성공하였습니다."));
  }

  @Test
  @DisplayName("회원 정보 업데이트 성공")
  void updateMemberInfoSuccess() throws Exception {
    // given
    ModifyRequest validModifyRequest = new ModifyRequest("new nickname", "new img url");

    // when & then
    mockMvc.perform(
            patch("/users")
                .header("Authorization", accessToken)
                .content(objectMapper.writeValueAsString(validModifyRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("유저 정보 수정에 성공하였습니다."));

    assertThat(testMember.getNickname()).isEqualTo(validModifyRequest.nickname());
    assertThat(testMember.getProfileImgUrl()).isEqualTo(validModifyRequest.profileImg());
  }

  @Test
  @DisplayName("닉네임 필드 공백인 경우 회원정보 수정 실패")
  void updateMemberInfoFailInvalidDto() throws Exception {
    // given
    ModifyRequest inValidModifyRequest = new ModifyRequest("", "new img url");

    // when & then
    mockMvc.perform(
            patch("/users")
                .header("Authorization", accessToken)
                .content(objectMapper.writeValueAsString(inValidModifyRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("유효하지 않은 데이터입니다."))
        .andExpect(jsonPath("$.data.nickname").value("must not be empty"));
  }

  @Test
  @DisplayName("회원 비밀번호 업데이트 성공")
  void updatePasswordSuccess() throws Exception {
    // given
    PasswordUpdateRequest validUpdateRequest = new PasswordUpdateRequest("newPassword");
    String prevPassword = testMember.getPassword();
    // when & then
    mockMvc.perform(
            patch("/users/password")
                .header("Authorization", accessToken)
                .content(objectMapper.writeValueAsString(validUpdateRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("비밀번호 변경에 성공하였습니다."));

    assertThat(prevPassword).isNotEqualTo(testMember.getPassword());
  }

  @Test
  @DisplayName("dto 공백시 회원 비밀번호 수정 실패")
  void updatePasswordFailEmptyDto() throws Exception {
    // given
    PasswordUpdateRequest invalidUpdateRequest = new PasswordUpdateRequest("");
    // when & then
    mockMvc.perform(
            patch("/users/password")
                .header("Authorization", accessToken)
                .content(objectMapper.writeValueAsString(invalidUpdateRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("유효하지 않은 데이터입니다."))
        .andExpect(jsonPath("$.data.newPassword").value("must not be empty"));
  }

  @Test
  @DisplayName("회원 탈퇴 성공")
  void withdrawalSuccess() throws Exception {
    // when & then
    mockMvc.perform(
            delete("/users")
                .header("Authorization", accessToken)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("회원 탈퇴에 성공하였습니다."));

    assertThat(testMember.getDeletedAt()).isNotNull();
  }
}