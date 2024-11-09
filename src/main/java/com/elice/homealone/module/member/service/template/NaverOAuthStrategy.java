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
public class NaverOAuthStrategy extends AbstractOAuthStrategy {
    private final NaverProperties naverProperties;

    @Override
    protected String getTokenRequestUrl() {
        return naverProperties.getTokenRequestURL();
    }

    @Override
    protected String getUserInfoUrl() {
        return "https://openapi.naver.com/v1/nid/me";
    }

    @Override
    protected Member parseUserInfo(String responseBody) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
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
    }
}
