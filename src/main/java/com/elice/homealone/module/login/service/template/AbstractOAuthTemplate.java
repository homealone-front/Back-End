package com.elice.homealone.module.login.service.template;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.member.entity.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public abstract class AbstractOAuthTemplate {
    protected RestTemplate restTemplate = new RestTemplate();
    protected abstract String getTokenRequestUrl();
    protected abstract String getUserInfoUrl();
    protected abstract Member parseUserInfo(String responseBody) throws JsonProcessingException;

    public String requestAccessToken(String code){
        String tokenRequestUrl = getTokenRequestUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenRequestUrl, HttpMethod.POST, request, String.class);
        System.out.println("response access: "+response);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(response.getBody()).get("access_token").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Member getUserInfo(String accessToken) {
        String userInfoUrl = getUserInfoUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, String.class);
        System.out.println("response info: "+response);

        try {
            return parseUserInfo(response.getBody());

        } catch (JsonProcessingException e) {
            throw new HomealoneException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }



}
