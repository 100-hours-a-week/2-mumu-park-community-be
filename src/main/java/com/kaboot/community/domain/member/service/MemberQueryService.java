package com.kaboot.community.domain.member.service;

import com.kaboot.community.domain.member.dto.response.MemberInfoResponse;
import com.kaboot.community.domain.member.entity.Member;

public interface MemberQueryService {
    MemberInfoResponse getMemberInfoByUsername(String username);

    boolean isEmailDuplicate(String email);

    boolean isNicknameDuplicate(String nickname);

    Member getMemberByUsername(String username);
}
