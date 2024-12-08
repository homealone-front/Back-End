package com.elice.homealone.module.member.dto;


import com.elice.homealone.module.member.entity.Member;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Builder
    public MemberDto(Long id, String name, LocalDate birth, String email, String firstAddress, String secondAddress, String imageUrl, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.name = name;
        this.birth = birth;
        this.email = email;
        this.firstAddress = firstAddress;
        this.secondAddress = secondAddress;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

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
