package com.kaboot.community.domain.member.service.impl;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.config.jwt.dto.AuthTokens;
import com.kaboot.community.config.jwt.dto.TokenInfo;
import com.kaboot.community.config.jwt.enums.TokenType;
import com.kaboot.community.domain.member.dto.request.ModifyRequest;
import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.service.MemberQueryService;
import com.kaboot.community.domain.member.dto.request.LoginRequest;
import com.kaboot.community.domain.member.dto.request.PasswordUpdateRequest;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.mapper.UserMapper;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.MemberCommandService;
import com.kaboot.community.util.jwt.JwtUtil;
import com.kaboot.community.util.jwt.TokenGenerator;
import com.kaboot.community.util.password.PasswordUtil;
import com.kaboot.community.util.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {
    private static final String RT = "RT:";
    private static final String LOGOUT = "LOGOUT";

    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;
    private final TokenGenerator tokenGenerator;
    private final RedisUtil redisUtil;
    private final JwtUtil jwtUtil;

    @Override
    public void register(RegisterRequest registerRequest) {
        memberRepository.save(UserMapper.toEntity(registerRequest));
    }

    @Override
    public void login(LoginRequest loginRequest) {
        Member member = memberRepository.findByUsername(loginRequest.email())
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if (!Objects.equals(loginRequest.password(), member.getPassword())) {
            throw new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST);
        }
    }

    @Override
    public AuthTokens loginV2(LoginRequest loginRequest) {
        Member validMember = memberRepository.findByUsername(loginRequest.email())
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if(!PasswordUtil.isSamePassword(loginRequest.password(), validMember.getPassword())) {
            throw new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST);
        }

        String refreshToken = redisUtil.getData(RT + validMember.getId());
        if (refreshToken == null) {
            refreshToken = jwtUtil.createToken(validMember.getId(), TokenType.REFRESH_TOKEN, validMember.getRole());
            redisUtil.setData(RT + validMember.getId(), refreshToken, jwtUtil.getExpiration(TokenType.REFRESH_TOKEN));
        }

        return tokenGenerator.generateTokenWithoutRFToken(validMember.getId(), validMember.getRole());
    }

    @Override
    public void update(String userEmail, ModifyRequest modifyRequest) {
        Member member = memberRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if (memberQueryService.isNicknameDuplicate(modifyRequest.nickname())) {
            throw new CustomException(CustomResponseStatus.NICKNAME_ALREADY_EXIST);
        }

        member.update(modifyRequest);
    }

    @Override
    public void updatePassword(String userEmail, PasswordUpdateRequest passwordUpdateRequest) {
        Member member = memberRepository.findByUsername(userEmail)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        if (!member.isSamePassword(passwordUpdateRequest.prevPassword())) {
            throw new CustomException(CustomResponseStatus.PASSWORD_NOT_MATCH);
        }

        member.updatePassword(passwordUpdateRequest.newPassword());
    }

    @Override
    public void withdrawal(String loggedInUsername) {
        Member member = memberRepository.findByUsername(loggedInUsername)
                .orElseThrow(() -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST));

        member.withdrawal(LocalDateTime.now());
    }

    @Override
    public AuthTokens reissue(String refreshToken) {
        TokenInfo tokenInfo = jwtUtil.getTokenClaims(refreshToken);

        String refreshTokenInRedis = redisUtil.getData(RT + tokenInfo.id());
        if (refreshTokenInRedis == null) {
            throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_EXPIRED);
        }
        if (!Objects.equals(refreshToken, refreshTokenInRedis)) {
            throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_MATCH);
        }

        Member findMember = memberRepository.findById(tokenInfo.id()).orElseThrow(
                () -> new CustomException(CustomResponseStatus.MEMBER_NOT_EXIST)
        );

        AuthTokens generateToken = tokenGenerator.generateTokenWithoutRFToken(findMember.getId(), RoleType.fromString(tokenInfo.role()));
        redisUtil.setData(RT + tokenInfo.id(), generateToken.refreshToken(), jwtUtil.getExpiration(TokenType.REFRESH_TOKEN));

        return generateToken;
    }

    @Override
    public void logout(String accessToken) {
        String resolveAccessToken = jwtUtil.resolveToken(accessToken);
        TokenInfo infoInToken = jwtUtil.getTokenClaims(resolveAccessToken);
        String refreshTokenInRedis = redisUtil.getData(RT + infoInToken.id());

        if (refreshTokenInRedis == null) throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND);

        redisUtil.deleteDate(RT + infoInToken.id());
        redisUtil.setData(resolveAccessToken, LOGOUT, jwtUtil.getExpiration(resolveAccessToken));
    }
}
