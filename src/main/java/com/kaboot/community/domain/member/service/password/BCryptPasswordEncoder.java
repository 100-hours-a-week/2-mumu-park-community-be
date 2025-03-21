package com.kaboot.community.domain.member.service.password;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class BCryptPasswordEncoder implements PasswordEncoder {
    @Override
    public String hash(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt());
    }

    @Override
    public boolean matches(String pwd, String hashed) {
        return BCrypt.checkpw(pwd, hashed);
    }
}
