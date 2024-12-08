package com.elice.homealone.module.login.dto.request;

import com.elice.homealone.module.member.entity.Member;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginRequestDto {
    private String email;
    private String password;
    public LoginRequestDto(String email, String password) {
        this.email=email;
        this.password=password;
    }
    public static LoginRequestDto toResponse(Member member) {
        return new LoginRequestDto(member.getEmail(), member.getPassword());
    }
}
