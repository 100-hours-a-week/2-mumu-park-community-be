package com.kaboot.community.member.service;

import com.kaboot.community.member.dto.MemberInfo;
import com.kaboot.community.member.repository.MemberJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberJdbcRepository memberJdbcRepository;
    public Integer calculateAgeByBirthYear(Integer birthYear) {
        return 2025 - birthYear + 1;
    }

    public MemberInfo getMemberInfoById(Long id) {
        return memberJdbcRepository.findByMemberById(id);
    }
}
