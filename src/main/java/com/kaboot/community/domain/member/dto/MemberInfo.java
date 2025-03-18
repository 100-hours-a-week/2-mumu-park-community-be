package com.kaboot.community.domain.member.dto;

import java.time.LocalDateTime;

public record MemberInfo(
        String username,
        String nickname
) {
}
