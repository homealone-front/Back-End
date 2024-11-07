package com.elice.homealone.module.member.service.template;

import com.elice.homealone.module.member.entity.Member;

public interface OAuthStrategy {
    String requestAccessToken(String code);

    Member getUserInfo(String accessToken);
}
