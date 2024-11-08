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

        tokenRequestUrl = naverProperties.getTokenRequestURL();

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

    @Override
    public Member getUserInfo(String accessToken) {
        String userInfoUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        userInfoUrl = naverProperties.getTokenRequestURL();

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
        System.out.println("response"+response);

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            JsonNode responseNode = jsonNode.path("response");
            String email = responseNode.get("email").asText();
            String name = responseNode.has("nickname") ? responseNode.get("nickname").asText() : email;
            String profileImageUrl = responseNode.has("profile_image") ? responseNode.get("profile_image").asText() : "";

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
}
