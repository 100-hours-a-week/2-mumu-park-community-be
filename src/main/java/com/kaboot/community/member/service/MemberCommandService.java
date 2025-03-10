package com.kaboot.community.member.service;

import com.kaboot.community.member.dto.request.LoginRequest;
import com.kaboot.community.member.dto.request.ModifyRequest;
import com.kaboot.community.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.member.dto.request.RegisterRequest;

public interface MemberCommandService {
    void register(RegisterRequest registerRequest);

    void login(LoginRequest loginRequest);

    void update(String userEmail, ModifyRequest modifyRequest);

    void updatePassword(String userEmail, PasswordUpdateRequest passwordUpdateRequest);
}
