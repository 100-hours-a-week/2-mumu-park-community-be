package com.kaboot.community.domain.member.controller;

import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.config.jwt.dto.AuthTokens;
import com.kaboot.community.domain.member.dto.request.LoginRequest;
import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.dto.response.LoginResponse;
import com.kaboot.community.domain.member.dto.response.ReissueResponse;
import com.kaboot.community.domain.member.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Void>> register(
      @RequestBody @Valid RegisterRequest registerRequest) {
    authService.register(registerRequest);

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("회원가입이 완료되었습니다."))
    );
  }

  @PostMapping("/tokens")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      HttpServletResponse response,
      @RequestBody @Valid LoginRequest loginRequest
  ) {
    AuthTokens tokens = authService.login(loginRequest);
    setRefreshTokenInCookie(response, tokens.refreshToken());

    return ResponseEntity.ok().body(ApiResponse.createSuccess(
        LoginResponse.from(tokens.accessToken()),
        CustomResponseStatus.SUCCESS.withMessage("로그인이 완료되었습니다."))
    );
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @RequestHeader("Authorization") String accessToken
  ) {
    authService.logout(accessToken);

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("로그아웃이 완료되었습니다."))
    );
  }

  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse<ReissueResponse>> reissue(
      @CookieValue("refreshToken") String refreshToken,
      HttpServletResponse response
  ) {
    AuthTokens authTokens = authService.reissue(refreshToken);
    setRefreshTokenInCookie(response, authTokens.refreshToken());

    return ResponseEntity.ok().body(ApiResponse.createSuccess(
        new ReissueResponse(authTokens.accessToken()),
        CustomResponseStatus.SUCCESS.withMessage("토큰 재발급 성공"))
    );
  }

  private void setRefreshTokenInCookie(
      HttpServletResponse response,
      String refreshToken
  ) {
    Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    // refreshTokenCookie.setSecure(true); // HTTPS를 사용할 경우 주석 해제

    // 기존 쿠키 추가
    response.addCookie(refreshTokenCookie);

    // SameSite 속성 추가 (헤더 재정의)
    String cookieValue = String.format("refreshToken=%s; HttpOnly; Path=/; SameSite=Lax",
        refreshToken);
    // HTTPS를 사용할 경우: String cookieValue = String.format("refreshToken=%s; HttpOnly; Path=/; Secure; SameSite=Lax", refreshToken);

    response.setHeader("Set-Cookie", cookieValue);
  }
}
