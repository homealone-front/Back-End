package com.elice.homealone.module.member.service;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.member.service.property.GoogleProperties;
import com.elice.homealone.module.member.service.property.KakaoProperties;
import com.elice.homealone.module.member.service.property.NaverProperties;
import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.entity.Member;
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

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final NaverProperties naverProperties;
    private final KakaoProperties kakaoProperties;
    private final GoogleProperties googleProperties;
    private final AuthService authService;
    @Value("${naver.uri}")
    private String NAVER_URI;
    @Value("${kakao.uri}")
    private String KAKAO_URI;
    @Value("${google.uri}")
    private String GOOGLE_URI;


    /**
     * 경로 변수 platform 마다 각기 다른 로그인 페이지로의 리다이렉트를 돕는 메소드
     * @param platform
     * @return redirectUrl
     */
    public String getRedirectUri(String platform) {
        String redirectUrl = switch (platform.toLowerCase()) {
            case "naver" -> NAVER_URI;
            case "google" -> GOOGLE_URI;
            case "kakao" -> KAKAO_URI;
            default -> throw new HomealoneException(ErrorCode.BAD_REQUEST);
        };
        return redirectUrl;
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
        String accessToken = requestAccessToken(platform, code);
        Member member = getUserInfo(platform, accessToken);
        return signupOrLogin(member, response);
    }
    private String requestAccessToken(String platform, String code) {
        String tokenRequestUrl;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        switch (platform.toLowerCase()) {
            case "naver" -> tokenRequestUrl = naverProperties.getTokenRequestURL();
            case "kakao" -> tokenRequestUrl = kakaoProperties.getTokenRequestURL();
            case "google" -> tokenRequestUrl = googleProperties.getTokenRequestURL();
            default -> throw new HomealoneException(ErrorCode.BAD_REQUEST);
        }
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        System.out.println("Token Request URL: " + tokenRequestUrl);
        System.out.println("Request Body: " + request.getBody());
        ResponseEntity<String> response = restTemplate.exchange(tokenRequestUrl, HttpMethod.POST, request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(response.getBody()).get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    public Member getUserInfo(String platform, String accessToken) {
        String userInfoUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        switch (platform.toLowerCase()) {
            case "naver" -> userInfoUrl = "https://openapi.naver.com/v1/nid/me";
            case "kakao" -> userInfoUrl = "https://kapi.kakao.com/v2/user/me";
            case "google" -> userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
            default -> throw new HomealoneException(ErrorCode.BAD_REQUEST);
        }

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
        System.out.println("response"+response);

        ObjectMapper objectMapper = new ObjectMapper();
        String email;
        String name;
        String profileImageUrl;
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            // 플랫폼별로 JSON 구조가 다를 수 있으므로, 플랫폼에 따라 JSON 접근 경로를 다르게 설정합니다.
            JsonNode responseNode;
            if ("naver".equals(platform.toLowerCase())) {
                responseNode = jsonNode.path("response");
                email = responseNode.has("email") ? responseNode.get("email").asText() : null;
                name = responseNode.has("nickname") ? responseNode.get("nickname").asText() : null;
                profileImageUrl = responseNode.has("profile_image") ? responseNode.get("profile_image").asText() : null;
            } else {
                responseNode = jsonNode.path("kakao_account");
                email = responseNode.has("email") ? responseNode.get("email").asText() : null;

                responseNode = responseNode.path("properties");
                name = responseNode.has("nickname") ? responseNode.get("nickname").asText() : null;
                profileImageUrl = responseNode.has("profile_image") ? responseNode.get("profile_image").asText() : null;
            }

            return Member.builder()
                    .email(email)
                    .name(name)
                    .imageUrl(profileImageUrl)
                    .password("OAUTH2.0!") //TODO 해시함수로 업데이트?
                    .build();

        } catch (JsonProcessingException e) {
            throw new HomealoneException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    public TokenDto signupOrLogin(Member member, HttpServletResponse httpServletResponse) {
        try{
            if(!authService.isEmailDuplicate(member.getEmail())){
                authService.signUp(member.toSignupRequestDto());
            }
        }catch (HomealoneException e) {

        }
        TokenDto tokenDto = authService.login(member.toLoginRequestDto(), httpServletResponse);
        return tokenDto;
    }

}
