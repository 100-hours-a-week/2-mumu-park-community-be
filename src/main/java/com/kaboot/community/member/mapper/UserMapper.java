package com.kaboot.community.member.mapper;

import com.kaboot.community.member.dto.request.RegisterRequest;
import com.kaboot.community.member.entity.Member;

public class UserMapper {
    public static Member toEntity(RegisterRequest registerRequest) {
        return Member.builder()
                .username(registerRequest.email())
                .password(registerRequest.password())
                .nickname(registerRequest.nickname())
                .profileImgUrl(registerRequest.profileImage())
                .build();
    }
}
