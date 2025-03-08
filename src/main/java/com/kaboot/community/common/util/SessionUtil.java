package com.kaboot.community.common.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class SessionUtil {

    private static final String MEMBER_SESSION_KEY = "member";

    /**
     * 현재 로그인된 사용자의 이메일을 세션에서 가져오는 메서드
     * @param request HttpServletRequest 객체
     * @return 로그인된 사용자의 이메일 (없으면 null 반환)
     */
    public static String getLoggedInUserEmail(HttpServletRequest request) {
        HttpSession session = request.getSession(false); // 기존 세션 가져오기 (없으면 null)
        if (session == null) {
            return null;
        }
        return (String) session.getAttribute(MEMBER_SESSION_KEY);
    }

    /**
     * 사용자를 세션에 저장하는 메서드
     * @param request HttpServletRequest 객체
     * @param email 로그인한 사용자의 이메일
     */
    public static void setLoggedInUser(HttpServletRequest request, String email) {
        HttpSession session = request.getSession(); // 새로운 세션 생성 (없으면 자동 생성)
        session.setAttribute(MEMBER_SESSION_KEY, email);
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
