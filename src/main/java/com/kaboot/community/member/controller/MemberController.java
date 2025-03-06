package com.kaboot.community.member.controller;

import com.kaboot.community.member.dto.MemberInfo;
import com.kaboot.community.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/members")
//@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

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
}
