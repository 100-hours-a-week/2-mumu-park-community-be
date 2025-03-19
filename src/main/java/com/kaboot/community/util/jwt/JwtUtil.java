package com.kaboot.community.util.jwt;

import com.kaboot.community.config.jwt.dto.TokenInfo;
import com.kaboot.community.config.jwt.enums.TokenType;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
@Getter
@Slf4j
public class JwtUtil {
    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String TOKEN_TYPE = "token_type";
    private static final String TOKEN_TYPE_MEMBER = "MEMBER";
    private static final String ROLE = "role";
    private static final String BEARER = "Bearer ";

    private final String SECRET_KEY;
    private final long ACCESS_TOKEN_EXPIRATION_TIME;
    private final long REFRESH_TOKEN_EXPIRATION_TIME;
    private final UserDetailsService userDetailsService;

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration_time.access_token}") long accessTokenExprTime,
            @Value("${jwt.expiration_time.refresh_token}") long refreshTokenExprTime,
            @Qualifier("principalDetailsServiceImpl") UserDetailsService userDetailsService
    ) {
        this.SECRET_KEY = secretKey;
        this.ACCESS_TOKEN_EXPIRATION_TIME = accessTokenExprTime;
        this.REFRESH_TOKEN_EXPIRATION_TIME = refreshTokenExprTime;
        this.userDetailsService = userDetailsService;
    }

    /***
     * @param secretKey : yml 에 저장되어 있는 secret key
     * @return : secret key 를 인코딩 하여 Key 객체로 리턴
     */
    private Key getSigningKey(String secretKey) {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰을 파싱하여 토큰에 들어있는 Claim을 리턴
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰속(claim)에 있는 클라이언트의 id(pk) 리턴
     */
    public Long getIdInToken(String token) {
        return extractAllClaims(token).get(ID, Long.class);
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰속(claim)에 있는 클라이언트의 role 리턴
     */
    public String getRoleInToken(String token) {
        return extractAllClaims(token).get(TOKEN_TYPE, String.class);
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰을 이용하여 로그인 된 UPA 객체를 가져옴 -> UPA 객체 안에 유저의 권한들이 담겨 있음
     */
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = loadUserDetailsByToken(token);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private UserDetails loadUserDetailsByToken(String token) {
        Long authInfo = getIdInToken(token);

        return userDetailsService.loadUserByUsername(String.valueOf(authInfo));
    }

    /***
     * @param subject : jwt 의 subject 값
     * @param tokenType : AccessToken or RefreshToken
     * @param roleType : 회원 권한
     * @return : 암호화된 JWT
     */
    public String createToken(Long subject, TokenType tokenType, RoleType roleType) {
        Claims claims = createClaims(subject, roleType);

        long expirationTime = getExpirationTime(tokenType);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(SECRET_KEY), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims createClaims(Long subject, RoleType roleType) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(subject));

        claims.put(ID, subject);
        claims.put(TOKEN_TYPE, roleType);

        return claims;
    }

    private long getExpirationTime(TokenType tokenType) {
        return tokenType == TokenType.ACCESS_TOKEN
                ? ACCESS_TOKEN_EXPIRATION_TIME
                : REFRESH_TOKEN_EXPIRATION_TIME;
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰 문자열 앞의 Bearer 을 제거하고 토큰 문자열만 리턴
     */
    public String resolveToken(String token) {
        if (token == null) return "";
        return token.substring(BEARER.length());
    }

    public TokenInfo getTokenClaims(String token) {
        return TokenInfo.builder()
                .id(getIdInToken(token))
                .role(getRoleInToken(token))
                .build();
    }

    /***
     * @param token : 요청이 들어온 토큰
     * @return : 토큰의 유효기간이 얼마나 남았는지 리턴
     */
    public Long getExpiration(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(getSigningKey(SECRET_KEY))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        long now = System.currentTimeMillis();
        return expiration.getTime() - now;
    }

    public Long getExpiration(TokenType tokenType) {
        return tokenType.equals(TokenType.ACCESS_TOKEN) ? ACCESS_TOKEN_EXPIRATION_TIME : REFRESH_TOKEN_EXPIRATION_TIME;
    }
}
