package com.kaboot.community.member.dto.response;

public record MemberInfoResponse(
        String email,
        String nickname,
        String profileImg
) {
}
