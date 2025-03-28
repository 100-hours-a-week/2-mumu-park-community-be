package com.kaboot.community.domain.member.controller;

import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.config.security.member.PrincipalDetails;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.domain.member.dto.response.ExistResponse;
import com.kaboot.community.domain.member.dto.response.MemberInfoResponse;
import com.kaboot.community.domain.member.service.member.MemberCommandService;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class MemberController {

  private final MemberQueryService memberQueryService;
  private final MemberCommandService memberCommandService;

  @GetMapping("/email")
  public ResponseEntity<ApiResponse<ExistResponse>> checkEmailDuplicate(
      @RequestParam String email) {
    ExistResponse response = memberQueryService.isEmailDuplicate(email);

    return ResponseEntity.ok(ApiResponse.createSuccess(
        response,
        CustomResponseStatus.SUCCESS
    ));
  }

  @GetMapping("/nickname")
  public ResponseEntity<ApiResponse<ExistResponse>> checkNicknameDuplicate(
      @RequestParam String nickname) {
    ExistResponse response = memberQueryService.isNicknameDuplicate(nickname);
    return ResponseEntity.ok(ApiResponse.createSuccess(
        response,
        CustomResponseStatus.SUCCESS
    ));
  }

  @GetMapping()
  public ResponseEntity<ApiResponse<MemberInfoResponse>> getMemberInfo(
      @AuthenticationPrincipal PrincipalDetails principalDetails
  ) {
    MemberInfoResponse response = memberQueryService.getMemberInfoByUsername(
        principalDetails.getUsername());
    return ResponseEntity.ok(ApiResponse.createSuccess(
        response,
        CustomResponseStatus.SUCCESS
    ));
  }

  @PatchMapping()
  public ResponseEntity<ApiResponse<Void>> updateMember(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestBody @Valid ModifyRequest modifyRequest
  ) {
    memberCommandService.update(principalDetails.getUsername(), modifyRequest);

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT
    ));
  }

  @PatchMapping("/password")
  public ResponseEntity<ApiResponse<Void>> updatePassword(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestBody @Valid PasswordUpdateRequest passwordUpdateRequest
  ) {
    memberCommandService.updatePassword(principalDetails.getUsername(), passwordUpdateRequest);

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("비밀번호 변경에 성공하였습니다.")
    ));
  }

  @DeleteMapping()
  public ResponseEntity<ApiResponse<Void>> withdrawal(
      @AuthenticationPrincipal PrincipalDetails principalDetails
  ) {
    memberCommandService.withdrawal(principalDetails.getUsername());

    return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent(
        CustomResponseStatus.SUCCESS_WITH_NO_CONTENT.withMessage("회원 탈퇴에 성공하였습니다.")
    ));
  }
}
