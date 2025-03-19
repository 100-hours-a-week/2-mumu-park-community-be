package com.kaboot.community.config.security.member;

import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.service.MemberQueryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class PrincipalDetailsServiceImpl implements UserDetailsService {
    private final MemberQueryService memberQueryService;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Member validMember = memberQueryService.getMemberById(Long.parseLong(id));

        return new PrincipalDetails(validMember);
    }
}
