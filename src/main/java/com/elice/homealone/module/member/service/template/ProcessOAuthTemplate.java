package com.elice.homealone.module.member.service.template;

import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.member.dto.TokenDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public abstract class ProcessOAuthTemplate {
    private final AuthService authService;
    public TokenDto processOAuthLogin(String code, HttpServletResponse response ) {
        String accessToken = requestAccessToken(code);
        Member member = getUserInfo(accessToken);
        return signupOrLogin(member, response);
    }

    public abstract String requestAccessToken(String code);
    public abstract Member getUserInfo(String accessToken);

    protected TokenDto signupOrLogin(Member member, HttpServletResponse response) {
        try{
            if(!authService.isEmailDuplicate(member.getEmail())) authService.signUp(member.toSignupRequestDto());
        }catch (HomealoneException e) {

        }
        TokenDto tokenDto = authService.login(member.toLoginRequestDto(), response);
        return tokenDto;
    }


}
