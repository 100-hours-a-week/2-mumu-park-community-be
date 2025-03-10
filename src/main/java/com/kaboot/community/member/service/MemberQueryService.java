package com.kaboot.community.member.service;

import com.kaboot.community.member.dto.response.MemberInfoResponse;
import com.kaboot.community.member.entity.Member;

public interface MemberQueryService {
    MemberInfoResponse getMemberInfoById(Long id);

    boolean isEmailDuplicate(String email);

    boolean isNicknameDuplicate(String nickname);

    Member getMemberByUsername(String username);
}
