package com.kaboot.community.domain.member.mapper;

import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.util.password.PasswordUtil;

public class UserMapper {
    public static Member toEntity(RegisterRequest registerRequest) {
        return Member.builder()
                .username(registerRequest.email())
                .password(PasswordUtil.hashPassword(registerRequest.password()))
                .nickname(registerRequest.nickname())
                .profileImgUrl(registerRequest.profileImage())
                .role(RoleType.ROLE_MEMBER)
                .build();
    }
}
