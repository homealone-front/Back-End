package com.elice.homealone.module.member.service.template;

import com.elice.homealone.module.member.entity.Member;

public class KakaoOAuthStrategy implements OAuthStrategy{
    @Override
    public String requestAccessToken(String code) {
        return null;
    }

    @Override
    public Member getUserInfo(String accessToken) {
        return null;
    }
}
