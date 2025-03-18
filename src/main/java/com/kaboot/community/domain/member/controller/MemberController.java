package com.kaboot.community.domain.member.controller;

import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.util.SessionUtil;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.domain.member.dto.response.ExistResponse;
import com.kaboot.community.domain.member.dto.response.MemberInfoResponse;
import com.kaboot.community.domain.member.service.MemberCommandService;
import com.kaboot.community.domain.member.service.MemberQueryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class MemberController {
    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    @GetMapping("/email")
    public ResponseEntity<ApiResponse<ExistResponse>> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = memberQueryService.isEmailDuplicate(email);

        return ResponseEntity.ok(ApiResponse.createSuccess(new ExistResponse(isDuplicate), CustomResponseStatus.SUCCESS));
    }

    @GetMapping("/nickname")
    public ResponseEntity<ApiResponse<ExistResponse>> checkNicknameDuplicate(@RequestParam String nickname) {
        boolean isDuplicate = memberQueryService.isNicknameDuplicate(nickname);
        return ResponseEntity.ok(ApiResponse.createSuccess(new ExistResponse(isDuplicate), CustomResponseStatus.SUCCESS));
    }

    @GetMapping()
    public ResponseEntity<ApiResponse<MemberInfoResponse>> getMemberInfo(
            HttpServletRequest request
    ) {
        String loggedInUsername = SessionUtil.getLoggedInUsername(request);
        System.out.println("loggedInUsername = " + loggedInUsername);
        MemberInfoResponse response = memberQueryService.getMemberInfoByUsername(loggedInUsername);
        return ResponseEntity.ok(ApiResponse.createSuccess(response, CustomResponseStatus.SUCCESS));
    }

    @PatchMapping()
    public ResponseEntity<ApiResponse<Void>> updateMember(
            HttpServletRequest request,
            @RequestBody ModifyRequest modifyRequest
    ) {
        String loggedInUsername = SessionUtil.getLoggedInUsername(request);
        memberCommandService.update(loggedInUsername, modifyRequest);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updateMember(
            HttpServletRequest request,
            @RequestBody PasswordUpdateRequest passwordUpdateRequest
    ) {
        String loggedInUsername = SessionUtil.getLoggedInUsername(request);
        memberCommandService.updatePassword(loggedInUsername, passwordUpdateRequest);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateMember(
            HttpServletRequest request
    ) {
        String loggedInUsername = SessionUtil.getLoggedInUsername(request);
        memberCommandService.withdrawal(loggedInUsername);

        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(CustomResponseStatus.SUCCESS_WITH_NO_CONTENT));
    }
}
