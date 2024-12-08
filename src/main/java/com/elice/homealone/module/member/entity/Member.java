package com.elice.homealone.module.member.entity;

import com.elice.homealone.module.chatting.entity.Chatting;
import com.elice.homealone.module.comment.entity.Comment;

import com.elice.homealone.global.common.BaseTimeEntity;
import com.elice.homealone.module.commentlike.entity.CommentLike;
import com.elice.homealone.module.login.dto.request.SignupRequestDto;
import com.elice.homealone.module.like.entity.Like;
import com.elice.homealone.module.post.entity.Post;
import com.elice.homealone.module.scrap.entity.Scrap;
import jakarta.persistence.*;

import java.util.List;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "member")
public class Member extends BaseTimeEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 15, nullable = false)
    private String name;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "first_address")
    private String firstAddress;

    @Column(name = "second_address")
    private String secondAddress;

    @Column(name = "phone")
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.ROLE_USER;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "deleted_at", nullable = false)
    private boolean deletedAt = false;

    public Member(String email, String password) {
        this.email=email;
        this.password=password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public static Member from(SignupRequestDto signupRequestDto) {
        return Member.builder()
                .name(signupRequestDto.getName())
                .birth(signupRequestDto.getBirth())
                .email(signupRequestDto.getEmail())
                .firstAddress(signupRequestDto.getFirstAddress())
                .secondAddress(signupRequestDto.getSecondAddress())
                .password(signupRequestDto.getPassword())
                .build();
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !deletedAt;
    }

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Post> posts;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Like> likes;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<CommentLike> commentLikes;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Scrap> scraps;

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY)
    private List<Chatting> chat_rooms;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    private List<Comment> comments;
}
