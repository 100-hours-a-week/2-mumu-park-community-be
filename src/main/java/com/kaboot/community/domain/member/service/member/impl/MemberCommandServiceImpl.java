package com.kaboot.community.domain.member.service.member.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.service.member.MemberCommandService;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import com.kaboot.community.domain.member.service.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {
    private final MemberQueryService memberQueryService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @PreAuthorize("isAuthenticated() and hasAuthority('MEMBER') and authentication.principal.username == #authUsername")
    public void update(String authUsername, ModifyRequest modifyRequest) {
        Member validMember = memberQueryService.getMemberByUsername(authUsername);

        if (memberQueryService.isNicknameDuplicate(modifyRequest.nickname()).isExist()) {
            throw new CustomException(CustomResponseStatus.NICKNAME_ALREADY_EXIST);
        }

        validMember.update(modifyRequest);
    }

    @Override
    @PreAuthorize("isAuthenticated() and hasAuthority('MEMBER') and authentication.principal.username == #authUsername")
    public void updatePassword(String authUsername, PasswordUpdateRequest passwordUpdateRequest) {
        Member validMember = memberQueryService.getMemberByUsername(authUsername);

        validMember.updatePassword(passwordEncoder.hash(passwordUpdateRequest.newPassword()));
    }

    @Override
    @PreAuthorize("isAuthenticated() and hasAuthority('MEMBER') and authentication.principal.username == #authUsername")
    public void withdrawal(String authUsername) {
        Member validMember = memberQueryService.getMemberByUsername(authUsername);

        validMember.withdrawal(LocalDateTime.now());
    }
}
