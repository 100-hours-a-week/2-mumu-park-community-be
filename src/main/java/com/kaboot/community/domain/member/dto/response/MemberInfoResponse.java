package com.kaboot.community.domain.member.dto.response;

public record MemberInfoResponse(
        String email,
        String nickname,
        String profileImg
) {
}
