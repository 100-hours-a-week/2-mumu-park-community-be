package com.kaboot.community.domain.member.service.auth;

import com.kaboot.community.domain.member.dto.request.RegisterRequest;
import com.kaboot.community.domain.member.entity.Member;
import com.kaboot.community.domain.member.repository.MemberRepository;
import com.kaboot.community.domain.member.service.password.BCryptPasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.*;

//@SpringBootTest
@Profile("test")
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입 요청이 유효할 때, 회원가입에 성공한다.")
    void registerWithValidRequest() {
        //given
        RegisterRequest validRequest = new RegisterRequest("test@test.com", "test1!", "testName", "http://test.jpeg");

        authService.register(validRequest);

        verify(memberRepository, times(1)).save(any(Member.class));
        verify(passwordEncoder, times(1)).hash(any(String.class));
    }

}