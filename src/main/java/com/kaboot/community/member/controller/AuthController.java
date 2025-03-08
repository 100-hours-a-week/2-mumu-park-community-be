package com.kaboot.community.member.controller;

import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.common.util.SessionUtil;
import com.kaboot.community.member.dto.request.LoginRequest;
import com.kaboot.community.member.dto.request.RegisterRequest;
import com.kaboot.community.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest registerRequest) {
        memberService.register(registerRequest);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<Void>> login(
            HttpServletRequest request,
            @RequestBody LoginRequest loginRequest
    ) {
        memberService.login(loginRequest);

        SessionUtil.setLoggedInUser(request, loginRequest.email());
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> login(
            HttpServletRequest request
    ) {
        String loggedInUserEmail = SessionUtil.getLoggedInUsername(request);

        if (loggedInUserEmail == null) { // 세션에 저장된 로그인 정보가 없으면 로그아웃 불가
            throw new CustomException(CustomResponseStatus.INVALID_REQUEST);
        }

        SessionUtil.invalidateSession(request);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("로그아웃에 성공하였습니다.")));
    }
}
