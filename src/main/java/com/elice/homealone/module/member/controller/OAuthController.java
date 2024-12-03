package com.elice.homealone.module.member.controller;


import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "OAuthController", description = "OAUTH2.0 인증 관리 API")
public class OAuthController {
    //테스트2
    private final OAuthService oAuthService;
    @Operation(summary = "소셜 로그인 페이지 리다이렉트")
    @GetMapping("/{platform}")
    public String OAuth2LoginRedirect(@PathVariable String platform){
        String redirectUrl = oAuthService.getRedirectUri(platform);
        return redirectUrl;
    }
    @Operation(summary = "소셜 로그인 콜백 (code 수신 및 accessToken 발급)")
    @PostMapping ("/{platform}/callback")
    public ResponseEntity<TokenDto> getAccessToken (@PathVariable String platform, @RequestBody Map<String, Object> requestBody, HttpServletResponse response){
        String code = (String) requestBody.get("code");
        TokenDto tokenDto = oAuthService.processOAuthLogin(platform, code, response);
        System.out.println("이거까지 찍히면 진짜 토큰 발급 되는거임");
        return new ResponseEntity<>(tokenDto, HttpStatus.OK);
    }
    
//    //로컬 테스트 코드
//    private final OAuthService oAuthService;
//    @Operation(summary = "소셜 로그인 페이지 리다이렉트")
//    @GetMapping("/{platform}")
//    public ResponseEntity<Void> OAuth2LoginRedirect(@PathVariable String platform) {
//        String redirectUrl = oAuthService.getRedirectUri(platform);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setLocation(URI.create(redirectUrl));
//        return new ResponseEntity<>(headers, HttpStatus.FOUND);
//    }
//    @Operation(summary = "소셜 로그인 콜백 (code 수신 및 accessToken 발급)")
//    @GetMapping ("/{platform}/callback")
//    public String getAccessToken (@PathVariable String platform, @RequestParam String code, HttpServletResponse response){
//        TokenDto tokenDto = oAuthService.processOAuthLogin(platform, code, response);
//        System.out.println("이거까지 찍히면 진짜 토큰 발급 되는거임");
//        return tokenDto.getAccessToken();
//    }
}

