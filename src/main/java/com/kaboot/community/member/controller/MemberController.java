package com.kaboot.community.member.controller;

import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.util.SessionUtil;
import com.kaboot.community.member.dto.request.ModifyRequest;
import com.kaboot.community.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.member.dto.response.ExistResponse;
import com.kaboot.community.member.dto.response.MemberInfoResponse;
import com.kaboot.community.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/email")
    public ResponseEntity<ApiResponse<ExistResponse>> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = memberService.isEmailDuplicate(email);
        return ResponseEntity.ok(ApiResponse.createSuccess(new ExistResponse(isDuplicate), CustomResponseStatus.SUCCESS));
    }

    @GetMapping("/nickname")
    public ResponseEntity<ApiResponse<ExistResponse>> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean isDuplicate = memberService.isNicknameDuplicate(nickname);
        return ResponseEntity.ok(ApiResponse.createSuccess(new ExistResponse(isDuplicate), CustomResponseStatus.SUCCESS));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMemberInfo(
            @PathVariable Long id
    ) {
        MemberInfoResponse response = memberService.getMemberInfoById(id);
        return ResponseEntity.ok(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @PatchMapping("")
    public ResponseEntity<ApiResponse<Void>> updateMember(
            HttpServletRequest request,
            @RequestBody ModifyRequest modifyRequest
    ) {
        String loggedInUserEmail = SessionUtil.getLoggedInUsername(request);
        memberService.update(loggedInUserEmail, modifyRequest);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updateMember(
            HttpServletRequest request,
            @RequestBody PasswordUpdateRequest passwordUpdateRequest
    ) {
        String loggedInUserEmail = SessionUtil.getLoggedInUsername(request);
        memberService.updatePassword(loggedInUserEmail, passwordUpdateRequest);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }
}
