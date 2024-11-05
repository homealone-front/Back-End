package com.elice.homealone.module.member.controller;


import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "AuthController", description = "OAUTH2.0 인증 관리 API")
public class OAuthController {
    @Value("${naver.url}")
    private String NAVER_URL;
    @Value("${kakao.url}")
    private String KAKAO_URL;
    @Value("${google.url}")
    private String GOOGLE_URL;
    private final OAuthService oAuthService;

    @Operation(summary = "소셜 로그인 페이지 리다이렉트")
    @GetMapping("/{platform}")
    public void OAuth2LoginRedirect(@PathVariable String platform, HttpServletResponse response) throws IOException {
        String redirectUrl = oAuthService.getRedirectUrl(platform);
        response.sendRedirect(redirectUrl);
    }

    @Operation(summary = "소셜 로그인 콜백 (code 수신 및 accessToken 발급)")
    @GetMapping("/{platform}/callback")
    public ResponseEntity<TokenDto> handleSocialCallback(@PathVariable String platform, @RequestParam String code, HttpServletResponse response) {
        // code로 accessToken을 발급받고 사용자 정보 조회 후 로그인/회원가입 처리
        TokenDto tokenDto = oAuthService.processOAuthLogin(platform, code, response);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", tokenDto.getAccessToken());
        return new ResponseEntity<>(tokenDto, httpHeaders, HttpStatus.OK);
    }
//
//    @Operation(summary = "네이버 로그인 페이지 이동")
//    @GetMapping("/naver")
//    public String naverLoginRedirect() {
//        return NAVER_URL;
//    }
//
//    @Operation(summary = "카카오 로그인 페이지 이동")
//    @GetMapping("/kakao")
//    public String kakaoResponseUrl() {
//        return KAKAO_URL;
//    }

//    @Operation(summary = "네이버 로그인")
//    @PostMapping("/naver/login")
//    public ResponseEntity<TokenDto> naverLogin(@RequestBody Map<String, String> body
//            , HttpServletResponse httpServletResponse) {
//        Member member = oAuthService.getNaverUserInfo(body.get("accessToken"));
//        TokenDto tokenDto = oAuthService.signupOrLogin(member, httpServletResponse);
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("Authorization", tokenDto.getAccessToken());
//        return new ResponseEntity<>(tokenDto, httpHeaders, HttpStatus.OK);
//    }
//
//    @Operation(summary = "카카오 로그인")
//    @PostMapping("/kakao/login")
//    public ResponseEntity<TokenDto> kakaoLogin (@RequestBody Map<String, String> body, HttpServletResponse httpServletResponse) {
//        Member member = oAuthService.getKakaoUserInfo(body.get("accessToken"));
//        TokenDto tokenDto = oAuthService.signupOrLogin(member, httpServletResponse);
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set("Authorization", tokenDto.getAccessToken());
//        return new ResponseEntity<>(tokenDto, httpHeaders, HttpStatus.OK);
//    }
}
