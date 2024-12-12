package com.elice.homealone.module.room.dto;

import com.elice.homealone.module.talk.dto.TrendTalkResponse;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
@Getter
public class TrendRoomResponseWrapper {
    private List<TrendRoomResponse> content;
    private int pageNumber; // 현재 페이지 번호
    private int pageSize; // 페이지 크기
    private long totalElements; // 전체 데이터 개수
    private int totalPages; // 전체 페이지 수
    private boolean last; // 마지막 페이지 여부

    protected TrendRoomResponseWrapper() {

    }

    public TrendRoomResponseWrapper(Page<TrendRoomResponse> page) {
        this.content = page.getContent();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
    }


    // Wrapper를 Page 객체로 변환
    public Page<TrendRoomResponse> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }


}