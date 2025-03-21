package com.kaboot.community.common.exception;

import com.kaboot.community.common.enums.CustomResponseStatus;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{
    private final CustomResponseStatus customResponseStatus;

    public CustomException(CustomResponseStatus customResponseStatus) {
        super(customResponseStatus.getMessage());
        this.customResponseStatus = customResponseStatus;
    }
}
