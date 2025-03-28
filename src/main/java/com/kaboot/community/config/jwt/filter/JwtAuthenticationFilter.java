package com.kaboot.community.config.jwt.filter;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;
import com.kaboot.community.util.jwt.JwtUtil;
import com.kaboot.community.util.redis.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION = "Authorization";
  private static final String LOGOUT = "LOGOUT";

  private final JwtUtil jwtUtil;
  private final RedisUtil redisUtil;

  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String resolveToken = jwtUtil.resolveToken(request.getHeader(AUTHORIZATION));

    if (Objects.equals(resolveToken, "")) {
      request.getRequestDispatcher("/exception/entrypoint/nullToken").forward(request, response);
      return;
    }

    try {
      handleBlacklistedToken(resolveToken);
      Authentication authentication = jwtUtil.getAuthentication(resolveToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);
//      filterChain.doFilter(request, response);
    } catch (CustomException e) {
      request.getRequestDispatcher("/exception/entrypoint/logout").forward(request, response);
    } catch (ExpiredJwtException e) {
      request.getRequestDispatcher("/exception/entrypoint/expiredToken").forward(request, response);
    } catch (JwtException | IllegalArgumentException e) {
      request.getRequestDispatcher("/exception/entrypoint/badToken").forward(request, response);
    }

    filterChain.doFilter(request, response);
  }

  // 로그아웃한 사용자가 접근하는지 파악. -> 접근할경우 예외발생
  private void handleBlacklistedToken(String resolveToken) throws CustomException {
    String redisLogoutValue = redisUtil.getData(resolveToken);
    if (redisLogoutValue != null && redisLogoutValue.equals(LOGOUT)) {
      throw new CustomException(CustomResponseStatus.LOGOUT_MEMBER);
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String[] excludePath = {
        "/auth/register",
        "/auth/tokens",
        "/auth/reissue",
        "/boards",
        "/favicon.ico"
    };
    String path = request.getRequestURI();
    String method = request.getMethod();

    if (!Objects.equals(method, "GET") && path.startsWith("/boards")) {
      return false;
    }

    // /boards/{boardId}/likes 패턴 확인을 위한 AntPathMatcher 사용
    PathMatcher pathMatcher = new AntPathMatcher();
    if (Objects.equals(method, "GET") && pathMatcher.match("/boards/*/likes", path)) {
      return false;  // 이 패턴에 해당하면 필터를 적용 (건너뛰지 않음)
    }

    return Arrays.stream(excludePath).anyMatch(path::startsWith);
  }
}
