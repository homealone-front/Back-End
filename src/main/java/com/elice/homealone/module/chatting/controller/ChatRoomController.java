package com.elice.homealone.module.chatting.controller;

import com.elice.homealone.global.exception.response.Response;
import com.elice.homealone.module.chatting.entity.ChatDto;
import com.elice.homealone.module.chatting.repository.ChatRoomRepository;
import com.elice.homealone.module.chatting.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
@CrossOrigin
@RestController
@Tag(name = "ChatRoomController", description = "채팅방 관리 API")
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;


    //회원의 모든 채팅방 목록 반환
    @Operation(summary = "회원 채팅방 목록 조회")
    @GetMapping("/chattings")
    public ResponseEntity<List<ChatDto>> chattingRooms() {

        return ResponseEntity.ok().body(chatRoomService.findChatrooms());

    }

    //선택 채팅방 조회
    @Operation(summary = "채팅방 데이터 조회")
    @GetMapping("/chatting/{chatroomId}")
    public ResponseEntity<ChatDto> chatroomInfo(@PathVariable Long chatroomId) {

        return ResponseEntity.ok().body(chatRoomService.findChatList(chatroomId));
    }

    //채팅방 생성
    @Operation(summary = "중고거래 게시판에서 채팅방 개설")
    @PostMapping("/chatting")
    public ResponseEntity<ChatDto> makeChat(@RequestBody ChatDto chatDto) {

        return ResponseEntity.ok().body(chatRoomService.makeChat(chatDto));
    }

    //채팅방 삭제
    @Operation(summary = "채팅방 삭제")
    @DeleteMapping("/chatting/{chatroomId}")
    public ResponseEntity<Response.ApiResponse> deleteChatroom(@PathVariable Long chatroomId) {

        chatRoomService.deleteChatroom(chatroomId);
        Response.ApiResponse response = new Response.ApiResponse("현재 회원의 " + chatroomId + "번 채팅방을 나갔습니다");

        return ResponseEntity.ok().body(response);
    }

}
