package com.elice.homealone.module.member.dto;


import com.elice.homealone.module.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MemberDto {
    private Long id;
    private String name;
    private LocalDate birth;
    private String email;
    private String firstAddress;
    private String secondAddress;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static MemberDto from(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .name(member.getName())
                .birth(member.getBirth())
                .email(member.getEmail())
                .firstAddress(member.getFirstAddress())
                .secondAddress(member.getSecondAddress())
                .imageUrl(member.getImageUrl())
                .createdAt(member.getCreatedAt())
                .modifiedAt(member.getModifiedAt())
                .build();
    }
}
