package com.elice.homealone.member.controller;

import com.elice.homealone.member.dto.response.KakaoUserResponse;
import com.elice.homealone.member.dto.request.LoginRequestDto;
import com.elice.homealone.member.dto.request.SignupRequestDto;
import com.elice.homealone.member.dto.TokenDto;
import com.elice.homealone.member.service.AuthService;
import com.elice.homealone.member.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "AuthController", description = "인증 관리 API")
public class AuthController {
    private final AuthService authService;
    private final OAuthService oAuthService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody SignupRequestDto signupRequestDTO) {
        authService.signUp(signupRequestDTO);
        return new ResponseEntity<>("회원가입에 성공했습니다.", HttpStatus.OK);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto loginRequestDTO,
                                          HttpServletResponse response) {
        TokenDto tokenDto = authService.login(loginRequestDTO, response);
        return new ResponseEntity<>(tokenDto, HttpStatus.OK);
    }


    @Operation(summary = "AccessToken 재발급")
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenDto> refreshAceessToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        TokenDto tokenDto = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(tokenDto);
    }

    @Operation(summary = "로그아웃")
    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest httpServletRequest,
                                       HttpServletResponse httpServletResponse) {
        authService.logout(httpServletRequest, httpServletResponse);
        return new ResponseEntity<>("로그아웃 되었습니다.", HttpStatus.OK);
    }

}
