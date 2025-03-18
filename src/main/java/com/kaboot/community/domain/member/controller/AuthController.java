package com.kaboot.community.domain.member.controller;

import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.common.util.SessionUtil;
import com.kaboot.community.config.jwt.dto.AuthTokens;
import com.kaboot.community.domain.member.dto.request.LoginRequest;
import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.dto.response.LoginResponse;
import com.kaboot.community.domain.member.dto.response.ReissueResponse;
import com.kaboot.community.domain.member.service.MemberCommandService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberCommandService memberCommandService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest registerRequest) {
        memberCommandService.register(registerRequest);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<Void>> login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody LoginRequest loginRequest
    ) {
        memberCommandService.login(loginRequest);

        SessionUtil.setLoggedInUser(request, response, loginRequest.email());
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PostMapping("/tokens")
    public ResponseEntity<ApiResponse<LoginResponse>> loginWithToken(
            HttpServletResponse response,
            @RequestBody LoginRequest loginRequest
    ) {
        System.out.println("loginRequest = " + loginRequest);
        AuthTokens tokens = memberCommandService.loginV2(loginRequest);
        setRefreshTokenInCookie(response, tokens.refreshToken());

        return ResponseEntity.ok().body(ApiResponse.createSuccess(LoginResponse.from(tokens.accessToken()), CustomResponseStatus.SUCCESS));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String accessToken
    ) {
        memberCommandService.logout(accessToken);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
                CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("로그아웃이 완료되었습니다."))
        );
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        AuthTokens authTokens = memberCommandService.reissue(refreshToken);
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
//        refreshTokenCookie.setSecure(true);
        response.addCookie(refreshTokenCookie);
    }
}
