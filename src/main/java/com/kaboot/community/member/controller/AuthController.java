package com.kaboot.community.member.controller;

import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.member.dto.request.LoginRequest;
import com.kaboot.community.member.dto.request.RegisterRequest;
import com.kaboot.community.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

        HttpSession session = request.getSession();
        session.setAttribute("member", loginRequest.email());
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

}
