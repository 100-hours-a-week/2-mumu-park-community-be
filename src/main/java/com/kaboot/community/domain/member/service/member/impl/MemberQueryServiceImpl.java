package com.kaboot.community.domain.member.service.member.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.domain.member.dto.response.ExistResponse;
import com.kaboot.community.domain.member.dto.response.MemberInfoResponse;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements MemberQueryService {

  private final MemberRepository memberRepository;

  @Override
  public MemberInfoResponse getMemberInfoByUsername(String username) {
    Member member = memberRepository.findByUsername(username)
        .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

    return new MemberInfoResponse(
        member.getUsername(),
        member.getNickname(),
        member.getProfileImgUrl()
    );
  }

  @Override
  public ExistResponse isEmailDuplicate(String email) {
    return new ExistResponse(memberRepository.existsByUsername(email));
  }

  @Override
  public ExistResponse isNicknameDuplicate(String nickname) {
    return new ExistResponse(memberRepository.existsByNickname(nickname));
  }

  @Override
  public Member getMemberByUsername(String username) {
    return memberRepository.findByUsername(username)
        .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));
  }

  @Override
  public Member getMemberById(Long id) {
    return memberRepository.findById(id)
        .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));
  }
}
