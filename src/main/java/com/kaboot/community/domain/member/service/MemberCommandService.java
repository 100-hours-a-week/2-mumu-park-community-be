package com.kaboot.community.domain.member.service;

import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;

public interface MemberCommandService {
    void update(String authUsername, ModifyRequest modifyRequest);

    void updatePassword(String authUsername, PasswordUpdateRequest passwordUpdateRequest);

    void withdrawal(String authUsername);
}
