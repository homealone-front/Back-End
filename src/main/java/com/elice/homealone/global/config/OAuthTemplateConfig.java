package com.elice.homealone.global.config;

import com.elice.homealone.module.login.service.property.KakaoProperties;
import com.elice.homealone.module.login.service.property.NaverProperties;

import com.elice.homealone.module.login.service.template.AbstractOAuthTemplate;
import com.elice.homealone.module.login.service.template.KakaoOAuthTemplate;
import com.elice.homealone.module.login.service.template.NaverOAuthTemplate;
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