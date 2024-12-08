package com.elice.homealone.module.room.controller;

import com.elice.homealone.global.exception.response.Response;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.room.service.RoomService;
import com.elice.homealone.module.room.dto.RoomRequestDTO;
import com.elice.homealone.module.room.dto.RoomResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name ="RoomController",description = "방자랑 게시글 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/room")
public class RoomController {
    @Autowired
    private RoomService roomService;
    @Operation(summary = "방자랑 게시글 조회, 검색")
    @GetMapping("")
    public ResponseEntity<Page<RoomResponseDTO>> findAll
                                                        (@RequestParam(required = false) String all,
                                                         @RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String content,
                                                        @RequestParam(required = false) String tag,
                                                        @RequestParam(required = false) String memberName,
                                                        @PageableDefault(size = 20) Pageable pageable){
        Page<RoomResponseDTO> RoomResponseDTO = roomService.searchRoomPost(all,title, content,tag, memberName, pageable);
        return ResponseEntity.ok().body(RoomResponseDTO);
    }
    @Operation(summary = "방자랑 게시글 생성")
    @PostMapping("")
    public ResponseEntity<RoomResponseDTO.RoomInfoDto> createRoomPost(@Validated @RequestBody RoomRequestDTO roomDto
                                                                ) {

        //사용자 검증 로직
        RoomResponseDTO.RoomInfoDto roomInfoDto = roomService.CreateRoomPost(roomDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(roomInfoDto);
    }
    @Operation(summary = "방자랑 게시글 수정")
    @PatchMapping("/{roomId}")
    public ResponseEntity<RoomResponseDTO.RoomInfoDto> editRoomPost(@PathVariable Long roomId
                                            , @Validated @RequestBody RoomRequestDTO roomDto//사용자 받아 글쓴 회원과 일치하는지 확인 로직 추가
                                            ,   @AuthenticationPrincipal Member member){
        //사용자 검증 로직
        String email = member.getUsername();
        RoomResponseDTO.RoomInfoDto roomInfoDto = roomService.EditRoomPost(roomId,roomDto);
        return ResponseEntity.status(HttpStatus.OK).body(roomInfoDto);

    }
    @Operation(summary = "방자랑 게시글 삭제")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Response.ApiResponse> deletePost(@PathVariable Long roomId){//사용자 받아 글쓴 회원과 일치하는지 확인 로직 추가
        roomService.deleteRoomPost(roomId);
        Response.ApiResponse response = new Response.ApiResponse("방자랑 "+roomId+"번 게시글이 성공적으로 지워졌습니다.");
        return ResponseEntity.ok().body(response);
    }
    @Operation(summary = "방자랑 게시글 상세 조회")
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponseDTO.RoomInfoDto> findRoomById( @PathVariable Long roomId) {
        //로그인한 상태가 아니면 null보냄
        RoomResponseDTO.RoomInfoDto byRoomId = roomService.findByRoomId(roomId);

        return ResponseEntity.ok(byRoomId);
    }
    @Operation(summary = "방자랑 게시글 인기글 조회")
    @GetMapping("/view")
    public ResponseEntity<Page<RoomResponseDTO>> findTopRoomByView(@PageableDefault(size = 4) Pageable pageable){
        Page<RoomResponseDTO> topRoomByView = roomService.findTopRoomByView(pageable);
        return ResponseEntity.ok(topRoomByView);
    }
    @Operation(summary = "방자랑 게시글 회원으로 조회")
    @GetMapping("/member")
    public ResponseEntity<Page<RoomResponseDTO>> findRoomByMember(@PageableDefault(size = 10) Pageable pageable){
        Page<RoomResponseDTO> roomByMember = roomService.findRoomByMember(pageable);
        return ResponseEntity.ok(roomByMember);
    }
}
