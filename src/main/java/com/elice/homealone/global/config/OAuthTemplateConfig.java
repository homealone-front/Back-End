package com.elice.homealone.global.config;

import com.elice.homealone.module.member.service.property.GoogleProperties;
import com.elice.homealone.module.member.service.property.KakaoProperties;
import com.elice.homealone.module.member.service.property.NaverProperties;

import com.elice.homealone.module.member.service.template.AbstractOAuthTemplate;
import com.elice.homealone.module.member.service.template.NaverOAuthTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class OAuthTemplateConfig {
    private final NaverProperties naverProperties;
    private final KakaoProperties kakaoProperties;
    private final GoogleProperties googleProperties;

    @Bean
    public AbstractOAuthTemplate naverOAuthStrategy() {
        return new NaverOAuthTemplate(naverProperties);
    }

//    @Bean
//    public OAuthStrategy kakaoOAuthStrategy() {
//        return new KakaoOAuthStrategy(kakaoProperties);
//    }
//
//    @Bean
//    public OAuthStrategy googleOAuthStrategy() {
//        return new GoogleOAuthStrategy(googleProperties);
//    }

    @Bean
    public Map<String, AbstractOAuthTemplate> strategies(AbstractOAuthTemplate naverOAuthStrategy) {
        Map<String, AbstractOAuthTemplate> strategies = new HashMap<>();
        strategies.put("naver", naverOAuthStrategy);
//        strategies.put("kakao", kakaoOAuthStrategy);
//        strategies.put("google", googleOAuthStrategy);
        return strategies;
    }
}