package com.kaboot.community.domain.member.service.member;

import com.kaboot.community.domain.member.dto.response.MemberInfoResponse;
import com.kaboot.community.domain.member.entity.Member;

public interface MemberQueryService {
    MemberInfoResponse getMemberInfoByUsername(String username);

    boolean isEmailDuplicate(String email);

    boolean isNicknameDuplicate(String nickname);

    Member getMemberByUsername(String username);

    Member getMemberById(Long id);
}
