package com.kaboot.community.domain.member.service.auth;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.config.jwt.dto.AuthTokens;
import com.kaboot.community.config.jwt.dto.TokenInfo;
import com.kaboot.community.config.jwt.enums.TokenType;
import com.kaboot.community.domain.member.dto.request.LoginRequest;
import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.member.MemberQueryService;
import com.kaboot.community.domain.member.service.password.PasswordEncoder;
import com.kaboot.community.util.jwt.JwtUtil;
import com.kaboot.community.util.jwt.TokenGenerator;
import com.kaboot.community.util.redis.RedisUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

  private static final String RT = "RT:";
  private static final String LOGOUT = "LOGOUT";

  private final MemberRepository memberRepository;
  private final MemberQueryService memberQueryService;
  private final PasswordEncoder passwordEncoder;
  private final TokenGenerator tokenGenerator;
  private final RedisUtil redisUtil;
  private final JwtUtil jwtUtil;

  @Override
  public void register(RegisterRequest registerRequest) {
    if (memberQueryService.isEmailDuplicate(registerRequest.email()).isExist()) {
      throw new CustomException(CustomResponseStatus.MEMBER_ALREADY_EXIST);
    }

    memberRepository.save(
        registerRequest.toEntity(passwordEncoder.hash(registerRequest.password())));
  }

  @Override
  public AuthTokens login(LoginRequest loginRequest) {
    Member validMember = memberQueryService.getMemberByUsername(loginRequest.username());

    if (!passwordEncoder.matches(loginRequest.password(), validMember.getPassword())) {
      throw new CustomException(CustomResponseStatus.PASSWORD_NOT_MATCH);
    }

    // 항상 새로운 refreshToken 발급 및 Redis 갱신
    String refreshToken = jwtUtil.createToken(validMember.getId(), TokenType.REFRESH_TOKEN,
        validMember.getRole());
    redisUtil.setData(RT + validMember.getId(), refreshToken,
        jwtUtil.getExpiration(TokenType.REFRESH_TOKEN));

    return tokenGenerator.generateTokenWithRF(validMember.getId(), refreshToken,
        validMember.getRole());
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

    Member findMember = memberQueryService.getMemberById(tokenInfo.id());

    AuthTokens generateToken = tokenGenerator.generateToken(findMember.getId(),
        RoleType.fromString(tokenInfo.role()));
    redisUtil.setData(RT + tokenInfo.id(), generateToken.refreshToken(),
        jwtUtil.getExpiration(TokenType.REFRESH_TOKEN));

    return generateToken;
  }

  @Override
  public void logout(String accessToken) {
    String resolveAccessToken = jwtUtil.resolveToken(accessToken);
    TokenInfo infoInToken = jwtUtil.getTokenClaims(resolveAccessToken);

    String refreshTokenInRedis = getRefreshTokenInRedis(infoInToken.id());
      if (refreshTokenInRedis == null) {
          throw new CustomException(CustomResponseStatus.REFRESH_TOKEN_NOT_FOUND);
      }

    redisUtil.deleteDate(RT + infoInToken.id());
    redisUtil.setData(resolveAccessToken, LOGOUT, jwtUtil.getExpiration(resolveAccessToken));
  }

  private String getRefreshTokenInRedis(Long memberId) {
    return redisUtil.getData(RT + memberId);
  }
}
