package com.elice.homealone.module.member.controller;


import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final OAuthService oAuthService;

    /**
     * 현재 사이트를 읽기전용으로 변경해놓기 위해서 로그인 기능 임시로 막아둠
     */
    @Operation(summary = "네이버 로그인 페이지 이동")
    @GetMapping("/naver")
    public String naverLoginRedirect() {
        return "hello stranger";
        //return NAVER_URL;
    }

    @Operation(summary = "네이버 로그인")
    @PostMapping("/naver/login")
    public ResponseEntity<TokenDto> naverLogin(@RequestBody Map<String, String> body
            , HttpServletResponse httpServletResponse) {
        Member member = oAuthService.getNaverUserInfo(body.get("accessToken"));
        TokenDto tokenDto = oAuthService.signupOrLogin(member, httpServletResponse);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", tokenDto.getAccessToken());
        return new ResponseEntity<>(tokenDto, httpHeaders, HttpStatus.OK);
    }

    /**
     * 현재 사이트를 읽기전용으로 변경해놓기 위해서 로그인 기능 임시로 막아둠
     */
    @Operation(summary = "카카오 로그인 페이지 이동")
    @GetMapping("/kakao")
    public String kakaoResponseUrl() {
        return "hello stranger";
//        return KAKAO_URL;
    }

    @Operation(summary = "카카오 로그인")
    @PostMapping("/kakao/login")
    public ResponseEntity<TokenDto> kakaoLogin (@RequestBody Map<String, String> body, HttpServletResponse httpServletResponse) {
        Member member = oAuthService.getKakaoUserInfo(body.get("accessToken"));
        TokenDto tokenDto = oAuthService.signupOrLogin(member, httpServletResponse);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", tokenDto.getAccessToken());
        return new ResponseEntity<>(tokenDto, httpHeaders, HttpStatus.OK);
    }
}
