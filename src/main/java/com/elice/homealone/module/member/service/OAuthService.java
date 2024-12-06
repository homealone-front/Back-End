package com.elice.homealone.module.member.service;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.member.dto.request.LoginRequestDto;
import com.elice.homealone.module.member.dto.request.SignupRequestDto;
import com.elice.homealone.module.member.service.property.GoogleProperties;
import com.elice.homealone.module.member.service.property.KakaoProperties;
import com.elice.homealone.module.member.service.property.NaverProperties;
import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.template.AbstractOAuthTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final Map<String, AbstractOAuthTemplate> templates;
    private final RestTemplate restTemplate = new RestTemplate();
    private final NaverProperties naverProperties;
    private final KakaoProperties kakaoProperties;
    private final GoogleProperties googleProperties;
    private final AuthService authService;


    /**
     * 경로 변수 platform 마다 각기 다른 로그인 페이지로의 리다이렉트를 돕는 메소드
     * @param platform
     * @return redirectUrl
     */
    public String getRedirectUri(String platform) {
        return switch (platform.toLowerCase()) {
            case "naver" -> naverProperties.getUri();
            case "google" -> googleProperties.getUri();
            case "kakao" -> kakaoProperties.getUri();
            default -> throw new HomealoneException(ErrorCode.BAD_REQUEST);
        };
    }

    /**
     * Authorization Server에서 code를 보내는 것을 받고
     * access token을 통해 Resource Server에서 User information을 받아서
     * 회원가입 하거나 로그인을 하여 TokenDto를 반환하는 메소드
     * @param platform
     * @param code
     * @param response
     * @return TokneDto
     */
    public TokenDto processOAuthLogin(String platform, String code, HttpServletResponse response ) {
        AbstractOAuthTemplate template = templates.get(platform.toLowerCase());
        if (template == null) {
            throw new HomealoneException(ErrorCode.BAD_REQUEST);
        }
        String accessToken = template.requestAccessToken(code);
        Member member = template.getUserInfo(accessToken);
        return signupOrLogin(member, response);
    }

    public TokenDto signupOrLogin(Member member, HttpServletResponse httpServletResponse) {
        try{
            if(!authService.isEmailDuplicate(member.getEmail())){
                authService.signUp(SignupRequestDto.toResponse(member));
            }
        }catch (HomealoneException e) {

        }
        TokenDto tokenDto = authService.login(LoginRequestDto.toResponse(member), httpServletResponse);
        return tokenDto;
    }

}
