package com.kaboot.community.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CustomResponseStatus {
    SUCCESS(HttpStatus.OK.value(), "요청에 성공하였습니다."),
    SUCCESS_WITH_NO_CONTENT(HttpStatus.NO_CONTENT.value() , "요청에 성공하였습니다."),

    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND.value(), "존재하지 않는 회원정보 입니다. 아이디와 비밀번호를 확인해주세요."),
    MEMBER_ALREADY_EXIST(HttpStatus.CONFLICT.value(), "이미 존재하는 회원 정보입니다. 로그인을 이용해주세요."),
    NICKNAME_ALREADY_EXIST(HttpStatus.CONFLICT.value(), "이미 존재하는 닉네임입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.CONFLICT.value(), "비밀번호가 일치하지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 요청입니다."),

    UNAUTHORIZED_REQUEST(HttpStatus.UNAUTHORIZED.value(), "권한이 없습니다."),

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
