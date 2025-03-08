package com.kaboot.community.member.controller;

import com.kaboot.community.common.dto.ApiResponse;
import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.member.dto.MemberInfo;
import com.kaboot.community.member.dto.response.ExistResponse;
import com.kaboot.community.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping()
    public ResponseEntity<String> getMumu() {
        return ResponseEntity.ok().body("hi my name is mumu");
    }

    @GetMapping("/age/{birthYear}")
    public ResponseEntity<String> getMumuAge(@PathVariable Integer birthYear) {
        Integer age = memberService.calculateAgeByBirthYear(birthYear);

        return ResponseEntity.ok().body("hi my name is mumu, his age is " + age);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberInfo> getMemberInfo(@PathVariable Long id) {
        MemberInfo memberInfo = memberService.getMemberInfoById(id);

        return ResponseEntity.ok().body(memberInfo);
    }

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
}
