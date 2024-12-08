package com.elice.homealone.member.service;

import com.elice.homealone.global.jwt.JwtTokenProvider;
import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.login.dto.request.LoginRequestDto;
import com.elice.homealone.module.login.dto.request.SignupRequestDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.repository.MemberRepository;
import com.elice.homealone.module.login.service.AuthService;
import com.elice.homealone.module.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    private AuthService authService;
    @Mock
    private MemberService memberService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void testSingUpSuccess() {
        SignupRequestDto signupRequestDto = new SignupRequestDto(
                "홍길동",
                LocalDate.of(2001,01,01),
                "test@homealone.site",
                "서울시 강남구",
                "역삼동 123번지",
                "Qwer@1234"
        );

        Member mockMember = Member.from(signupRequestDto);
        assertEquals(signupRequestDto.getEmail(), mockMember.getEmail());

    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void testLoginSuccess() {

        // Given
        String email = "user@homealone.site";
        String password = "Qwer@1234";
        String hashedPassword = "HashedQwer@1234";
        String accessToken = "mockAccessToken";
        String refreshToken = "mockRefreshToken";

        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);
        Member mockMember = new Member(email, hashedPassword);

        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse(); //가짜 HTTP 응답 객체

        when(memberService.findByEmail(email)).thenReturn(mockMember);
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(email)).thenReturn(accessToken);
        when(jwtTokenProvider.createRefreshToken(email)).thenReturn(refreshToken);

        // When
        TokenDto tokenDto = authService.login(loginRequestDto, httpServletResponse);

        // Then
        assertEquals("Bearer " + accessToken, tokenDto.getAccessToken());
        assertEquals(refreshToken, httpServletResponse.getCookies()[0].getValue()); // 쿠키 저장 확인
        verify(memberService, times(1)).findByEmail(email); // 이메일 조회 호출 1번 확인
    }
}
