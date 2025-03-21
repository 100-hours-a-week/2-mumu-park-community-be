package com.kaboot.community.domain.member.service.password;

public interface PasswordEncoder {
    String hash(String pwd);
    boolean matches(String pwd, String hashed);
}
