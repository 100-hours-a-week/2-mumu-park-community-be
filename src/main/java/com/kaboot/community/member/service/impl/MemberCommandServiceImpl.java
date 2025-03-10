package com.kaboot.community.member.service.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.member.dto.request.LoginRequest;
import com.kaboot.community.member.dto.request.ModifyRequest;
import com.kaboot.community.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.member.dto.request.RegisterRequest;
import com.kaboot.community.member.entity.Member;
import com.kaboot.community.member.mapper.UserMapper;
import com.kaboot.community.member.repository.MemberRepository;
import com.kaboot.community.member.service.MemberCommandService;
import com.kaboot.community.member.service.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {
    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;

    @Override
    public void register(RegisterRequest registerRequest) {
        memberRepository.save(UserMapper.toEntity(registerRequest));
    }

    @Override
    public void login(LoginRequest loginRequest) {
        Member member = memberRepository.findByUsername(loginRequest.email())
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if (!Objects.equals(loginRequest.password(), member.getPassword())) {
            throw new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST);
        }
    }

    @Override
    public void update(String userEmail, ModifyRequest modifyRequest) {
        Member member = memberRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if (memberQueryService.isNicknameDuplicate(modifyRequest.nickname())) {
            throw new CustomException(CustomResponseStatus.NICKNAME_ALREADY_EXIST);
        }

        member.update(modifyRequest);
    }

    @Override
    public void updatePassword(String userEmail, PasswordUpdateRequest passwordUpdateRequest) {
        Member member = memberRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if (!member.isSamePassword(passwordUpdateRequest.prevPassword())) {
            throw new CustomException(CustomResponseStatus.PASSWORD_NOT_MATCH);
        }

        member.updatePassword(passwordUpdateRequest.newPassword());
    }
}
