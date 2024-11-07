package com.elice.homealone.module.member.service.template;

import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.AuthService;
import com.elice.homealone.module.member.service.OAuthService;
import io.swagger.v3.oas.annotations.Operation;

public class NaverOAuth2Login extends ProcessOAuthTemplate {

    public NaverOAuth2Login(AuthService authService) {
        super(authService);
    }

    @Override
    public String requestAccessToken(String code) {
        return null;
    }

    @Override
    public Member getUserInfo(String accessToken) {
        return null;
    }
}
