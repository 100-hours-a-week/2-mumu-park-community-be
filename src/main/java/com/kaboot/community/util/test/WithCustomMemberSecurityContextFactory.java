package com.kaboot.community.util.test;

import com.kaboot.community.config.security.member.PrincipalDetails;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.entity.enums.RoleType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithCustomMemberSecurityContextFactory implements WithSecurityContextFactory<WithCustomMember> {
    @Override
    public SecurityContext createSecurityContext(WithCustomMember customMember) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Member testMember = Member.builder()
                .id(1L)
                .username(customMember.username())
                .password("test1234")
                .nickname("test")
                .profileImgUrl("http://test.jpeg")
                .role(RoleType.ROLE_MEMBER)
                .build();

        PrincipalDetails principal = new PrincipalDetails(testMember);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                principal.getPassword(),
                principal.getAuthorities()
        );

        context.setAuthentication(auth);
        return context;
    }
}
