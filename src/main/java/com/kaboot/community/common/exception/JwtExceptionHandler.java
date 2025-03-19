package com.kaboot.community.common.exception;

import com.kaboot.community.common.enums.CustomResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exception")
public class JwtExceptionHandler {
    @RequestMapping(value = "/accessDenied")
    public void accessException() {
        throw new CustomException(CustomResponseStatus.ACCESS_DENIED);
    }

    @RequestMapping(value = "/entrypoint/nullToken")
    public void nullTokenException() {
        throw new CustomException(CustomResponseStatus.NULL_JWT);
    }

    @RequestMapping(value = "/entrypoint/expiredToken")
    public void expiredTokenException() {
        throw new CustomException(CustomResponseStatus.EXPIRED_JWT);
    }

    @RequestMapping(value = "/entrypoint/badToken")
    public void badTokenException() {
        throw new CustomException(CustomResponseStatus.BAD_JWT);
    }

    @RequestMapping(value = "/entrypoint/logout")
    public void logoutMemberAccessException() {
        throw new CustomException(CustomResponseStatus.LOGOUT_MEMBER);
    }
}
