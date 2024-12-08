package com.elice.homealone.module.chatting.service;

import com.elice.homealone.module.chatting.entity.ChatDto;
import com.elice.homealone.module.chatting.entity.ChatMessage;
import com.elice.homealone.module.chatting.entity.Chatting;
import com.elice.homealone.module.chatting.entity.MessageDto;
import com.elice.homealone.module.chatting.repository.ChatMessageRepository;
import com.elice.homealone.module.chatting.repository.ChatRoomRepository;
import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.repository.MemberRepository;
import com.elice.homealone.module.login.service.AuthService;

import com.elice.homealone.module.member.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.*;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final MemberService memberService;


    //중고거래 채팅방 생성 메소드
    @Transactional
    public ChatDto makeChat(ChatDto chatDto) {
        Long receiver_id = chatDto.getReceiverId();
        Member receiver = memberRepository.findMemberById(receiver_id);

        //현재 로그인된 사용자 정보
        Member member = authService.getMember();
        Long curMemberId = member.getId();

        /* 테스트용
        Member member = memberRepository.findMemberById(5L);
        Long curMemberId = member.getId();
        */

        //현재 사용자와 중고거래 게시물 작성자가 같을 때
        if(receiver_id == curMemberId) {
            throw new HomealoneException(ErrorCode.CHATROOM_CREATION_FAILED);
        }

        //chatting 테이블 생성해 저장
        Chatting chatroom = chatRoomRepository.save(chatDto.toEntity(member, receiver));

        return chatroom.toDto();
    }

    @Transactional
    public ChatDto findChatList(Long chatroomId) {

        //현재 로그인된 사용자 정보
        Member member = authService.getMember();
        Long curMemberId = member.getId();

        /* 테스트용
        Member member = memberRepository.findMemberById(5L);
        Long curMemberId = member.getId();
        */


        //chatroomId에 따른 채팅방이 존재하지 않으면 예외 던지기
        Chatting chatting = chatRoomRepository.findById(chatroomId).orElseThrow(() ->
                new HomealoneException(ErrorCode.CHATTING_ROOM_NOT_FOUND));
        //현재 로그인한 회원이 자신이 속해있지 않은 채팅방 id를 통해 접근하려할 때
        if(chatting.getSender() != member && chatting.getReceiver() != member) {
            throw new HomealoneException(ErrorCode.NOT_MY_CHATROOM);
        }

        //채팅 참여자들의 메시지 dto 리스트
        List<ChatMessage> senderChatList = chatMessageRepository.findAllChatMessageByChattingIdOrderBySendDateAsc(chatroomId);
        List<MessageDto> Messages = new ArrayList<>();
        for(ChatMessage message : senderChatList) {
            Messages.add(message.toDto());
        }

        ChatDto responseDtos = ChatDto.builder()
                .id(chatroomId)
                .chatroomName(chatting.getChatroomName())
                .senderName(chatting.getSender() != null ? chatting.getSender().getName() : "(이름없음)")
                .receiverName(chatting.getReceiver() != null ? chatting.getReceiver().getName() : "(이름없음)")
                .senderId(chatting.getSender() != null ? chatting.getSender().getId() : null)
                .receiverId(chatting.getReceiver() != null ? chatting.getReceiver().getId() : null)
                .Messages(Messages)
                .currentId(curMemberId)
                .build();

        return responseDtos;
    }

    @Transactional
    public List<ChatDto> findChatrooms() {

        //현재 로그인된 사용자 정보
        Member member = authService.getMember();

        /* 테스트용
        Member member = memberRepository.findMemberById(5L);
        Long curMemberId = member.getId();
        */

        //member는 현재 로그인한 사용자 즉 sender
        List<Chatting> chattings = chatRoomRepository.findAllChattingBySenderId(member.getId());
        List<ChatDto> chatDtoList = new ArrayList<>();
        for(Chatting chatting : chattings) {
            chatDtoList.add(chatting.toDto());
        }

        return chatDtoList;
    }

    @Transactional
    public void deleteChatroom(Long chatroomId) {

        //현재 로그인된 사용자 정보
        Member member = authService.getMember();
        Long curMemberId = member.getId();

        /* 테스트용
        Member member = memberRepository.findMemberById(5L);
        Long curMemberId = member.getId();
        */

        //삭제하려는 채팅방 정보
        Chatting chatting = chatRoomRepository.findById(chatroomId)
                .orElseThrow(() -> new HomealoneException(ErrorCode.CHATTING_ROOM_NOT_FOUND));
        Member sender = chatting.getSender();
        Member receiver = chatting.getReceiver();

        if (sender != null && receiver != null) {
            if (curMemberId.equals(sender.getId())) {
                //member.getChat_rooms().remove(chatting); //회원이 가진 채팅방 리스트에서 해당 채팅방 삭제
                chatting.setSender(null);
            } else if (curMemberId.equals(receiver.getId())) {
                //member.getChat_rooms().remove(chatting); //회원이 가진 채팅방 리스트에서 해당 채팅방 삭제
                chatting.setReceiver(null); //채팅방에서 해당 회원 id 삭제
            } else {
                throw new HomealoneException(ErrorCode.NOT_MY_CHATROOM);
            }
        } else { // 채팅방의 구성원 둘 중 한 명이라도 채팅방 나간 상태일 때
            if ((sender != null && curMemberId.equals(sender.getId())) ||
                    (receiver != null && curMemberId.equals(receiver.getId()))) {
                //member.getChat_rooms().remove(chatting); //회원이 가진 채팅방 리스트에서 해당 채팅방 삭제
                chatRoomRepository.delete(chatting); //채팅방 삭제하기(매핑된 메시지들도 cascade로 같이 삭제됨)
            } else {
                throw new HomealoneException(ErrorCode.NOT_MY_CHATROOM);
            }
        }
    }
}
