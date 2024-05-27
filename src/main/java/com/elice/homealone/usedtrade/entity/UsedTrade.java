package com.elice.homealone.usedtrade.entity;

import com.elice.homealone.member.entity.Member;
import com.elice.homealone.post.entity.Post;
import com.elice.homealone.tag.dto.PostTagDto;
import com.elice.homealone.tag.entity.PostTag;
import com.elice.homealone.usedtrade.dto.UsedTradeResponseDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsedTrade extends Post {

    @Column(name="title")
    private String title;

    @Column(name="price")
    private int price;

    @Column(name="location")
    private String location;

    @Column(name="content")
    private String content;

    @OneToMany(mappedBy = "usedTrade")
    private List<UsedTradeImage> images = new ArrayList<>();

    public UsedTradeResponseDto toDto(){

        //관련태그 추가 무한참조를 막기위한 로직
        List<PostTag> tags = super.getTags();
        PostTagDto postTag;
        List<PostTagDto> tagDtos = new ArrayList<>();

        for(PostTag tag : tags){
            postTag = tag.toDto();
            tagDtos.add(postTag);
        }


        return UsedTradeResponseDto.builder()
                .id(super.getId())
                .tags(tagDtos)
                .member(this.getMember().toDto())
                .title(this.getTitle())
                .price(this.getPrice())
                .location(this.getLocation())
                .content(this.getContent())
                .images(this.getImages())
                .build();
    }

    public UsedTradeResponseDto toAllListDto(){
        List<UsedTradeImage> allImages = this.getImages();
        UsedTradeImage mainImage = null;
        for(UsedTradeImage image : allImages){
            if(image.isMain()){
                mainImage = image;
                break;
            }
        }
        return UsedTradeResponseDto.builder()
                .id(super.getId())
                .member(this.getMember().toDto())
                .title(this.getTitle())
                .price(this.getPrice())
                .location(this.getLocation())
                .content(this.getContent())
                .mainImage(mainImage)
                .build();
    }

}
