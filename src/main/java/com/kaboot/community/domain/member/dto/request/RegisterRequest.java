package com.kaboot.community.domain.member.dto.request;

import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;

public record RegisterRequest(
        String email,
        String password,
        String nickname,
        String profileImage
) {
    public Member toEntity(String hashedPassword) {
        return Member.builder()
                .username(email)
                .password(hashedPassword)
                .nickname(nickname)
                .profileImgUrl(profileImage)
                .role(RoleType.ROLE_MEMBER)
                .build();
    }
}
