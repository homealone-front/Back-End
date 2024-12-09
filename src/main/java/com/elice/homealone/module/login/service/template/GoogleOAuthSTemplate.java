package com.elice.homealone.module.login.service.template;

import com.elice.homealone.module.member.entity.Member;
import com.fasterxml.jackson.core.JsonProcessingException;

public class GoogleOAuthSTemplate extends AbstractOAuthTemplate {

    @Override
    protected String getTokenRequestUrl() {
        return null;
    }

    @Override
    protected String getUserInfoUrl() {
        return null;
    }

    @Override
    protected Member parseUserInfo(String responseBody) throws JsonProcessingException {
        return null;
    }
}
