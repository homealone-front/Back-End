package com.elice.homealone.module.member.service.template;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.property.NaverProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class NaverOAuthStrategy implements OAuthStrategy {
    private final NaverProperties naverProperties;
    private final RestTemplate restTemplate;
    @Override
    public String requestAccessToken(String code) {
        String tokenRequestUrl;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        tokenRequestUrl = naverProperties.getTokenRequestURL(); //여기만 다른 부분
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenRequestUrl, HttpMethod.POST, request, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(response.getBody()).get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Member getUserInfo(String accessToken) {
        String userInfoUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        userInfoUrl = "https://openapi.naver.com/v1/nid/me"; //다른 부분

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
        System.out.println("response"+response);

        ObjectMapper objectMapper = new ObjectMapper();
        String email;
        String name;
        String profileImageUrl;
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            JsonNode responseNode;

            //다른부분 시작
            responseNode = jsonNode.path("response");
            email = responseNode.has("email") ? responseNode.get("email").asText() : null;
            name = responseNode.has("nickname") ? responseNode.get("nickname").asText() : null;
            profileImageUrl = responseNode.has("profile_image") ? responseNode.get("profile_image").asText() : null;
            //다른부분 끝

            return Member.builder()
                    .email(email)
                    .name(name)
                    .imageUrl(profileImageUrl)
                    .password("OAUTH2.0!")
                    .build();

        } catch (JsonProcessingException e) {
            throw new HomealoneException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }
}
