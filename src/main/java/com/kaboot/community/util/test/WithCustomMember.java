package com.kaboot.community.util.test;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithCustomMemberSecurityContextFactory.class)
public @interface WithCustomMember {
    String username() default "member@test.com";
    String[] authorities() default {"MEMBER"};
}
