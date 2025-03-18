package com.kaboot.community.domain.member.entity.enums;

import com.kaboot.community.common.enums.CustomResponseStatus;
import com.kaboot.community.common.exception.CustomException;

public enum RoleType {
    ROLE_MEMBER,
    ROLE_ADMIN;

    public static RoleType fromString(String value) {
        for (RoleType role : RoleType.values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }

        throw new CustomException(CustomResponseStatus.INVALID_ROLE);
    }
}
