package com.elice.homealone.member.controller;

import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.service.OAuthService;
import com.elice.homealone.module.member.service.property.NaverProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuthService oAuthService;

    @Autowired
    private NaverProperties naverProperties;

    @Test
    @DisplayName("소셜 로그인 리다이렉트 URL 반환 테스트")
    void testOAuth2LoginRedirect() throws Exception {
        // Given: 플랫폼에 따른 리다이렉트 URL을 Mocking
        String platform = "naver";
        String redirectUrl = naverProperties.getUri(); // 프로퍼티에서 가져온 값
        Mockito.when(oAuthService.getRedirectUri(platform)).thenReturn(redirectUrl);

        // When: 리다이렉트 URL API 호출
        mockMvc.perform(get("/oauth/" + platform)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Then: 상태코드 200 확인
                .andExpect(content().string(redirectUrl)); // 반환된 URL이 예상값인지 확인
    }
//
//    @Test
//    @DisplayName("소셜 로그인 콜백 테스트")
//    void testOAuth2Callback() throws Exception {
//        // Given: 플랫폼과 요청 데이터를 설정
//        String platform = "naver";
//        String code = "test-code";
//        TokenDto mockTokenDto = new TokenDto("mock-access-token", "mock-refresh-token");
//
//        Mockito.when(oAuthService.processOAuthLogin(Mockito.eq(platform), Mockito.eq(code), Mockito.any()))
//                .thenReturn(mockTokenDto);
//
//        // When: 로그인 콜백 API 호출
//        mockMvc.perform(MockMvcRequestBuilders.post("/oauth/" + platform + "/callback")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"code\": \"" + code + "\"}"))
//                .andExpect(status().isOk()) // Then: HTTP 200 상태인지 확인
//                .andExpect(jsonPath("$.accessToken").value("mock-access-token")) // AccessToken 확인
//                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token")); // RefreshToken 확인
//    }
}