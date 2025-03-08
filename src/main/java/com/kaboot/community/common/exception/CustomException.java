package com.kaboot.community.common.exception;

import com.kaboot.community.common.enums.CustomResponseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CustomException extends RuntimeException{
    private final CustomResponseStatus customResponseStatus;
}
