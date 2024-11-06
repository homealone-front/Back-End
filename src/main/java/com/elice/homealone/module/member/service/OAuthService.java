package com.elice.homealone.module.member.service;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.global.oauth.GoogleProperties;
import com.elice.homealone.global.oauth.KakaoProperties;
import com.elice.homealone.global.oauth.NaverProperties;
import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.dto.response.KakaoUserResponse;
import com.elice.homealone.module.member.dto.response.NaverTokenResponse;
import com.elice.homealone.module.member.dto.response.NaverUserResponse;
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


    public String getRedirectUri(String platform) {
        String redirectUrl = switch (platform.toLowerCase()) {
            case "naver" -> NAVER_URI;
            case "google" -> GOOGLE_URI;
            case "kakao" -> KAKAO_URI;
            default -> throw new HomealoneException(ErrorCode.BAD_REQUEST);
        };
        return redirectUrl;
    }

    // code로 accessToken 요청 및 사용자 정보 처리
    public TokenDto processOAuthLogin(String platform, String code, HttpServletResponse response ) {
        // 1. 인증 code로 accessToken 요청
        String accessToken = requestAccessToken(platform, code);
        // 2. accessToken으로 사용자 정보 요청 및 Member 객체 생성
        Member member = getUserInfo(platform, accessToken);
        // 3. 회원 가입/로그인 처리 후 토큰 발급
        return signupOrLogin(member, response);
    }

    private String requestAccessToken(String platform, String code) {
        String tokenRequestUrl;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // 플랫폼 별  AccessToken 발급 요청
        switch (platform.toLowerCase()) {
            case "naver" -> tokenRequestUrl = naverProperties.getTokenRequestURL();
            case "kakao" -> tokenRequestUrl = kakaoProperties.getTokenRequestURL();
            case "google" -> tokenRequestUrl = googleProperties.getTokenRequestURL();
            default -> throw new HomealoneException(ErrorCode.BAD_REQUEST);
        }
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(tokenRequestUrl, HttpMethod.POST, request, String.class);

        // accessToken 파싱
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
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            // 플랫폼별로 JSON 구조가 다를 수 있으므로, 플랫폼에 따라 JSON 접근 경로를 다르게 설정합니다.
            JsonNode responseNode;
            if ("naver".equals(platform.toLowerCase())) {
                responseNode = jsonNode.path("response");
            } else {
                responseNode = jsonNode;
            }
            String email = responseNode.has("email") ? responseNode.get("email").asText() : null;
            String name = responseNode.has("nickname") ? responseNode.get("nickname").asText() : null;
            String profileImageUrl = responseNode.has("profile_image") ? responseNode.get("profile_image").asText() : null;

            return Member.builder()
                    .email(email)
                    .name(name)
                    .imageUrl(profileImageUrl)
                    .password("OAUTH2.0!") //TODO 해시함수로 업데이트?
                    .build();

        } catch (JsonProcessingException e) {
            throw new RuntimeException("사용자 정보 파싱에 실패했습니다.", e);
        }
    }


    //accessToken을 통해 유저정보 획득
    private NaverUserResponse.NaverUserDetail toRequestProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);

        ResponseEntity<NaverUserResponse> response =
                restTemplate.exchange("https://openapi.naver.com/v1/nid/me", HttpMethod.GET, request, NaverUserResponse.class);
        return response.getBody().getNaverUserDetail();
    }


    public Member getKakaoUserInfo(String kakaoAcessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", kakaoAcessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoUserResponse kakaoUserDto = null;
        try {
            kakaoUserDto = objectMapper.readValue(response.getBody(), KakaoUserResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return kakaoUserDto.toMember();
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






    public Member getNaverUserInfo(String accessToken) {
        NaverUserResponse.NaverUserDetail profile = toRequestProfile(accessToken);
        return Member.builder()
                .name(profile.getNickname())
                .email(profile.getEmail())
                .imageUrl(profile.getProfileImage())
                .password(profile.getId())
                .build();
    }
//    private String toRequestAccessToken(String code) {
//        ResponseEntity<NaverTokenResponse> response =
//                restTemplate.exchange(naverProperties.getTokenRequestURL(code), HttpMethod.GET, null, NaverTokenResponse.class);
//        // Validate를 만드는 것을 추천
//        return response.getBody().getAccessToken();
//    }

    //front에서 처리 해주시면서 kakao login과 동일하게 처리하도록 해당 코드는 deprecate함
    //GetMapping의 query parameter로 code를 받아올 때 사용함
//    public Member getNaverUserInfo(String code) {
//        String accessToken = toRequestAccessToken(code);
//        NaverUserResponse.NaverUserDetail profile = toRequestProfile(accessToken);
//
//        return Member.builder()
//                .name(profile.getNickname())
//                .email(profile.getEmail())
//                .imageUrl(profile.getProfileImage())
//                .password(profile.getId())
//                .build();
//    }
//    private String toRequestAccessToken(String code) {
//        ResponseEntity<NaverTokenResponse> response =
//                restTemplate.exchange(naverProperties.getRequestURL(code), HttpMethod.GET, null, NaverTokenResponse.class);
//        // Validate를 만드는 것을 추천
//        return response.getBody().getAccessToken();
//    }




//        public OAuthTokenDto getAccessToken(String code) {
//            RestTemplate rt = new RestTemplate();
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
//
//            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
//            params.add("grant_type", "authorization_code");
//            params.add("client_id", clientId);
//            params.add("redirect_uri", redirectUri);
//            params.add("code", code);
//
//            HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);
//
//            ResponseEntity<String> response = rt.exchange(
//                    "https://kauth.kakao.com/oauth/token", // https://{요청할 서버 주소}
//                    HttpMethod.POST, // 요청할 방식
//                    kakaoTokenRequest, // 요청할 때 보낼 데이터
//                    String.class // 요청 시 반환되는 데이터 타입
//            );
//
//            ObjectMapper objectMapper = new ObjectMapper();
//            OAuthTokenDto oAuthTokenDTO = null;
//            try {
//                oAuthTokenDTO = objectMapper.readValue(response.getBody(), OAuthTokenDto.class);
//            } catch (JsonProcessingException e) {
//                throw new RuntimeException(e);
//            }
//            return oAuthTokenDTO;
//        }
}
