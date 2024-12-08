package com.elice.homealone.module.member.service;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.AuthException;
import com.elice.homealone.module.member.dto.request.LoginRequestDto;
import com.elice.homealone.module.member.dto.request.SignupRequestDto;
import com.elice.homealone.module.member.service.property.GoogleProperties;
import com.elice.homealone.module.member.service.property.KakaoProperties;
import com.elice.homealone.module.member.service.property.NaverProperties;
import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.template.AbstractOAuthTemplate;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final Map<String, AbstractOAuthTemplate> templates;
    private final NaverProperties naverProperties;
    private final KakaoProperties kakaoProperties;
    private final GoogleProperties googleProperties;
    private final AuthService authService;

    public String getRedirectUri(String platform) {
        return switch (platform.toLowerCase()) {
            case "naver" -> naverProperties.getUri();
            case "google" -> googleProperties.getUri();
            case "kakao" -> kakaoProperties.getUri();
            default -> throw new AuthException(ErrorCode.BAD_REQUEST);
        };
    }

    public TokenDto processOAuthLogin(String platform, String code, HttpServletResponse response ) {
        AbstractOAuthTemplate template = templates.get(platform.toLowerCase());
        if (template == null) {
            throw new AuthException(ErrorCode.BAD_REQUEST);
        }
        System.out.println("template:"+template);
        String accessToken = template.requestAccessToken(code);
        Member member = template.getUserInfo(accessToken);
        return signupOrLogin(member, response);
    }

    public TokenDto signupOrLogin(Member member, HttpServletResponse httpServletResponse) {
        try{
            if(!authService.isEmailDuplicate(member.getEmail())){
                authService.signUp(SignupRequestDto.toResponse(member));
            }
        }catch (AuthException e) {

        }
        TokenDto tokenDto = authService.login(LoginRequestDto.toResponse(member), httpServletResponse);
        return tokenDto;
    }

}
