package com.elice.homealone.module.member.service.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

@Data
@Configuration
@ConfigurationProperties(prefix = "kakao")
public class KakaoProperties {
    private String requestTokenUri;
    private String clientId;
    private String redirectUri;
    private String uri;
    public String getTokenRequestURL() {
        return UriComponentsBuilder.fromHttpUrl(requestTokenUri)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .toUriString();
    }
}
