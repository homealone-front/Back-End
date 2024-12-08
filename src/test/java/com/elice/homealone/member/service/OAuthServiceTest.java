package com.elice.homealone.member.service;


import com.elice.homealone.module.login.dto.TokenDto;
import com.elice.homealone.module.login.dto.request.LoginRequestDto;
import com.elice.homealone.module.login.dto.request.SignupRequestDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.login.service.AuthService;
import com.elice.homealone.module.login.service.template.AbstractOAuthTemplate;
import com.elice.homealone.module.member.service.OAuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuthServiceTest {
    @Mock
    private Map<String, AbstractOAuthTemplate> templates;
    @Mock
    private AbstractOAuthTemplate naverOAuthTemplate;
    @Mock
    private AbstractOAuthTemplate kakaoOAuthTemplate;
    @Mock
    private AuthService authService;
    @InjectMocks
    private OAuthService oAuthService;

    @Test
    @DisplayName("네이버 로그인 흐름 테스트")
    void testProcessOAuthLogin_Naver() {
        //Given
        String platform = "naver";
        String code = "test-code";
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Member mockMember = Member.builder()
                .email("test@naver.com")
                .name("케빈")
                .build();

        String mockAccessToken = "mock-access-token";
        TokenDto mockTokenDto = new TokenDto();
        mockTokenDto.setAccessToken("mock-access-token");

        // Mock OAuthTemplate 동작
        when(templates.get(platform)).thenReturn(naverOAuthTemplate);
        when(naverOAuthTemplate.requestAccessToken(code)).thenReturn(mockAccessToken);
        when(naverOAuthTemplate.getUserInfo(mockAccessToken)).thenReturn(mockMember);

        // Mock AuthService 동작
        when(authService.isEmailDuplicate(mockMember.getEmail())).thenReturn(false); // 중복 이메일 없음
        doNothing().when(authService).signUp(any(SignupRequestDto.class)); // 회원가입 호출
        when(authService.login(any(LoginRequestDto.class), eq(response))).thenReturn(mockTokenDto);

        // When
        TokenDto result = oAuthService.processOAuthLogin(platform, code, response);

        // Then
        assertNotNull(result); // 객체가 null인지 확인
        assertEquals(mockAccessToken, result.getAccessToken()); //?
        verify(templates).get(platform); // 플랫폼에 맞는 템플릿 호출 확인
        verify(naverOAuthTemplate).requestAccessToken(code); // Access Token 요청 확인
        verify(naverOAuthTemplate).getUserInfo(mockAccessToken); // 사용자 정보 가져오기 확인
    }

}
