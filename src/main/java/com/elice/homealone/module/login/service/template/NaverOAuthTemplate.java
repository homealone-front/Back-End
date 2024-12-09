package com.elice.homealone.module.login.service.template;

import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.login.service.property.NaverProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NaverOAuthTemplate extends AbstractOAuthTemplate {
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
    public Member parseUserInfo(String responseBody) throws JsonProcessingException { //임시로 퍼블릭

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
