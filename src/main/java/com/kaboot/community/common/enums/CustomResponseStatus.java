package com.kaboot.community.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomResponseStatus {
    SUCCESS(HttpStatus.OK.value(), "요청에 성공하였습니다."),
    SUCCESS_WITH_NO_CONTENT(HttpStatus.NO_CONTENT.value() , "요청에 성공하였습니다."),

    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "리프레시 토큰을 찾을 수 없습니다."),
    INVALID_ROLE(HttpStatus.NOT_FOUND.value(), "유효하지 않는 권한입니다."),
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 회원정보 입니다. 아이디와 비밀번호를 확인해주세요."),
    BOARD_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 게시글입니다."),
    COMMENT_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 댓글입니다."),
    MEMBER_ALREADY_EXIST(HttpStatus.CONFLICT.value(), "이미 존재하는 회원 정보입니다. 로그인을 이용해주세요."),
    NICKNAME_ALREADY_EXIST(HttpStatus.CONFLICT.value(), "이미 존재하는 닉네임입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.CONFLICT.value(), "비밀번호가 일치하지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 요청입니다."),
    LIKES_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "유효하지 않은 좋아요 요청입니다."),

    LOGOUT_MEMBER(HttpStatus.NOT_FOUND.value(), "로그아웃 되었습니다. 다시 로그인을 진행해주세요."),
    UNAUTHORIZED_REQUEST(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."),ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "권한이 없습니다."),
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED.value(), "만료된 토큰입니다."),
    NULL_JWT(HttpStatus.BAD_REQUEST.value(),  "토큰이 공백입니다."),

    BAD_JWT(HttpStatus.BAD_REQUEST.value(), "잘못된 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "리프레시 토큰이 만료되었습니다. 재로그인을 진행해주세요."),
    REFRESH_TOKEN_NOT_MATCH(HttpStatus.CONFLICT.value(), "잘못된 리프레시 토큰입니다."),
    LOGIN_FAILED(HttpStatus.CONFLICT.value(), "아이디 또는 비밀번호가 일치하지 않습니다."),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "내부 서버 오류입니다."),
    ;

    private final int httpStatusCode;
    private String message;

    CustomResponseStatus(int httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }

    public CustomResponseStatus withMessage(String customMessage) {
        this.message = customMessage;
        return this;
    }
}
