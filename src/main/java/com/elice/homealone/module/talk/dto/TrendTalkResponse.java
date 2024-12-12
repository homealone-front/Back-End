package com.elice.homealone.module.talk.dto;

import com.elice.homealone.module.talk.entity.Talk;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class TrendTalkResponse {
    private Long id;
    private String title;
    private String memberName;
    private Integer commentCount;
    private String createdAt;
    private String contentSummary;
    private Integer likeCount;
    private String imageUrl;

    protected TrendTalkResponse() {

    }

    @Builder
    public TrendTalkResponse(Long id, String title, String memberName, Integer commentCount, String createdAt, String contentSummary, Integer likeCount, String imageUrl) {
        this.id = id;
        this.title = title;
        this.memberName = memberName;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.contentSummary = contentSummary;
        this.likeCount = likeCount;
        this.imageUrl = imageUrl;
    }

    public static TrendTalkResponse toTrendTalkResponse(Talk talk) {
        return TrendTalkResponse.builder()
                .id(talk.getId())
                .title(talk.getTitle())
                .memberName(talk.getMember().getName())
                .imageUrl(talk.getMember().getImageUrl())
                .commentCount(talk.getComments().size())
                .createdAt(talk.getCreatedAt().toString())
                .contentSummary(talk.getPlainContent().length() <= 80 ? talk.getPlainContent() : talk.getPlainContent().substring(0, 80))
                .likeCount(talk.getLikes() != null ? talk.getLikes().size() : 0)
                .build();
    }
}