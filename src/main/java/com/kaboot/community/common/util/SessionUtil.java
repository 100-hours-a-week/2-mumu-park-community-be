package com.kaboot.community.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    private static final String MEMBER_SESSION_KEY = "member";

    /**
     * 현재 로그인된 사용자의 이메일을 세션에서 가져오는 메서드
     * @param request HttpServletRequest 객체
     * @return 로그인된 사용자의 이메일 (없으면 null 반환)
     */
    public static String getLoggedInUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // 기존 세션 가져오기 (없으면 null)
        if (session == null) {
            System.out.println("[SessionUtil] 세션이 없습니다");
            return null;
        }

        String email = (String) session.getAttribute(MEMBER_SESSION_KEY);
        System.out.println("[SessionUtil] 세션에서 가져온 이메일: " + email);
        return email;
    }

    /**
     * 사용자를 세션에 저장하는 메서드
     * @param request HttpServletRequest 객체
     * @param email 로그인한 사용자의 이메일
     */
    public static void setLoggedInUser(HttpServletRequest request, HttpServletResponse response, String email) {
        HttpSession session = request.getSession(); // 새로운 세션 생성 (없으면 자동 생성)
        session.setAttribute(MEMBER_SESSION_KEY, email);
        setCookieForSession(response, email);
    }

    /**
     * 세션 ID를 쿠키에 설정하는 메서드
     * @param response HttpServletResponse 객체
     * @param sessionId 세션 ID
     */
    private static void setCookieForSession(HttpServletResponse response, String sessionId) {
        // 1. 일반적인 쿠키 설정
        Cookie cookie = new Cookie("JSESSIONID", sessionId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        // 2. SameSite 속성을 포함한 쿠키 설정 (최신 브라우저 대응)
        String cookieHeader = String.format("JSESSIONID=%s; Path=/; HttpOnly; SameSite=Lax", sessionId);
        response.setHeader("Set-Cookie", cookieHeader);

        System.out.println("[SessionUtil] 쿠키 설정 완료: " + cookieHeader);
    }

    /**
     * 현재 사용자의 세션을 무효화(로그아웃)하는 메서드
     * @param request HttpServletRequest 객체
     */
    public static void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 삭제
        }
    }
}
