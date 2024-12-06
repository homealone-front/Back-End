package com.elice.homealone.module.member.service.template;

import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.property.KakaoProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
public class KakaoOAuthTemplate extends AbstractOAuthTemplate{
    private final KakaoProperties kakaoProperties;

    @Override
    protected String getTokenRequestUrl() {
        return kakaoProperties.getTokenRequestURL();
    }

    @Override
    protected String getUserInfoUrl() {
        return "https://kapi.kakao.com/v2/user/me";
    }

    @Override
    protected Member parseUserInfo(String responseBody) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode responseNode = jsonNode.path("kakao_account");
        String email = responseNode.get("email").asText();
        responseNode = responseNode.path("profile");
        String name = responseNode.has("nickname") ? responseNode.get("nickname").asText() : email;
        String profileImageUrl = responseNode.has("profile_image_url") ? responseNode.get("profile_image_url").asText() : "";

        return Member.builder()
                .email(email)
                .name(name)
                .imageUrl(profileImageUrl)
                .password("OAUTH2.0!") //TODO 해시함수로 업데이트?
                .build();
    }
}
