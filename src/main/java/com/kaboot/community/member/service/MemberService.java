package com.kaboot.community.member.service;

import com.kaboot.community.member.dto.MemberInfo;
import com.kaboot.community.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    public Integer calculateAgeByBirthYear(Integer birthYear) {
        return 2025 - birthYear + 1;
    }

    public MemberInfo getMemberInfoById(Long id) {
        return memberRepository.findByMemberById(id);
    }
}
