package com.elice.homealone.global.config;

import com.elice.homealone.module.member.service.property.KakaoProperties;
import com.elice.homealone.module.member.service.property.NaverProperties;

import com.elice.homealone.module.member.service.template.AbstractOAuthTemplate;
import com.elice.homealone.module.member.service.template.KakaoOAuthTemplate;
import com.elice.homealone.module.member.service.template.NaverOAuthTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class OAuthTemplateConfig {
    private final NaverProperties naverProperties;
    private final KakaoProperties kakaoProperties;

    @Bean
    public AbstractOAuthTemplate naver() {
        return new NaverOAuthTemplate(naverProperties);
    }

    @Bean
    public AbstractOAuthTemplate kakao() {
        return new KakaoOAuthTemplate(kakaoProperties);
    }

}