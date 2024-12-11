package com.elice.homealone.module.member.dto;


import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MypageRequest {
    private String name;
    private LocalDate birth;
    private String firstAddress;
    private String secondAddress;
    private String imageUrl;
}
