package com.kaboot.community.domain.member.dto.request;

import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record RegisterRequest(
        @NotEmpty
        @Email
        String email,

        @NotEmpty
        String password,

        @NotEmpty
        String nickname,

        @NotEmpty
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
