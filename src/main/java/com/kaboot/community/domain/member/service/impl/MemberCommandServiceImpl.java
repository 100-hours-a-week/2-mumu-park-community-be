package com.kaboot.community.domain.member.service.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.MemberCommandService;
import com.kaboot.community.domain.member.service.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {
    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;

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

    @Override
    public void withdrawal(String loggedInUsername) {
        Member member = memberRepository.findByUsername(loggedInUsername)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        member.withdrawal(LocalDateTime.now());
    }
}
