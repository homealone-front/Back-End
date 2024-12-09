package com.elice.homealone.module.login.dto.request;


import com.elice.homealone.module.member.entity.Member;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SignupRequestDto {
    private String name;
    private LocalDate birth;
    @NotNull
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$")
    private String email;
    private String firstAddress;
    private String secondAddress;
    private String password;

    public static SignupRequestDto toResponse(Member member) {
        return new SignupRequestDto(member.getName(), member.getBirth(), member.getEmail(), member.getFirstAddress(), member.getSecondAddress(), member.getPassword());
    }
}
