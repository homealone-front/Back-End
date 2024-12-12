package com.elice.homealone.module.room.dto;

import com.elice.homealone.module.room.entity.Room;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TrendRoomResponse {
    private Long id;
    private String title;
    private String thumbnailUrl;
    private String memberName;
    private Integer commentCount;
    private String createdAt;
    private String contentSummary;
    @Builder.Default
    private Integer likeCount = 0;
    private String imageUrl; //member, front 요청으로 필드명 다시 원래대로 고침

    protected TrendRoomResponse() {

    }

    @Builder
    public TrendRoomResponse(Long id, String title, String thumbnailUrl, String memberName, Integer commentCount, String createdAt, String contentSummary, Integer likeCount, String imageUrl) {
        this.id = id;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.memberName = memberName;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.contentSummary = contentSummary;
        this.likeCount = likeCount;
        this.imageUrl = imageUrl;
    }

    public static TrendRoomResponse toTrendRoomResponse(Room room){
        return TrendRoomResponse.builder()
                .id(room.getId())
                .title(room.getTitle())
                .thumbnailUrl(room.getThumbnailUrl())
                .memberName(room.getMember().getName())
                .commentCount(room.getComments().size())
                .createdAt(room.getCreatedAt().toString())
                .contentSummary(room.getPlainContent().length() <=80 ? room.getPlainContent() : room.getPlainContent().substring(0,80))
                .likeCount( room.getLikes() != null ? room.getLikes().size() : 0)
                .imageUrl(room.getMember().getImageUrl())
                .build();
    }
}
