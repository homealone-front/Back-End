package com.elice.homealone.module.member.controller;

import com.elice.homealone.module.member.dto.request.LoginRequestDto;
import com.elice.homealone.module.member.dto.request.SignupRequestDto;
import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.service.AuthService;
import com.elice.homealone.module.member.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "AuthController", description = "인증 관리 API")
public class AuthController {
    private final AuthService authService;
    private final OAuthService oAuthService;

    /**
     * 현재 사이트를 읽기전용으로 변경해놓기 위해서 회원가입 기능 임시로 막아둠
     */
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
    @GetMapping("/token/refresh")
    public ResponseEntity<TokenDto>  refreshAceessToken(HttpServletRequest request) {
        TokenDto tokenDto = authService.refreshAccessToken(request);
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
