package com.kaboot.community.member.service;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.member.dto.MemberInfo;
import com.kaboot.community.member.dto.request.LoginRequest;
import com.kaboot.community.member.dto.request.ModifyRequest;
import com.kaboot.community.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.member.dto.request.RegisterRequest;
import com.kaboot.community.member.entity.Member;
import com.kaboot.community.member.mapper.UserMapper;
import com.kaboot.community.member.repository.MemberJdbcRepository;
import com.kaboot.community.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberJdbcRepository memberJdbcRepository;
    private final MemberRepository memberRepository;
    public Integer calculateAgeByBirthYear(Integer birthYear) {
        return 2025 - birthYear + 1;
    }

    public MemberInfo getMemberInfoById(Long id) {
        return memberJdbcRepository.findByMemberById(id);
    }

    @Transactional
    public void register(RegisterRequest registerRequest) {
        Member member = UserMapper.toEntity(registerRequest);
        memberRepository.save(member);
    }

    public void login(LoginRequest loginRequest) {
        Member member = memberRepository.findByUsername(loginRequest.email())
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if (!Objects.equals(loginRequest.password(), member.getPassword())) {
            throw new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST);
        }
    }

    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByUsername(email);
    }

    public boolean isNicknameDuplicate(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    @Transactional
    public void update(String userEmail, ModifyRequest modifyRequest) {
        Member member = memberRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if(isNicknameDuplicate(modifyRequest.nickname())) {
            throw new CustomException(CustomResponseStatus.NICKNAME_ALREADY_EXIST);
        }

        member.update(modifyRequest);
    }

    @Transactional
    public void updatePassword(String userEmail, PasswordUpdateRequest passwordUpdateRequest) {
        Member member = memberRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if(!member.isSamePassword(passwordUpdateRequest.prevPassword())) {
            throw new CustomException(CustomResponseStatus.PASSWORD_NOT_MATCH);
        }

        member.updatePassword(passwordUpdateRequest.newPassword());
    }
}
