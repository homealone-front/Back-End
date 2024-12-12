package com.elice.homealone.module.talk.contorller;

import com.elice.homealone.global.exception.response.Response;
import com.elice.homealone.module.talk.Service.TalkService;
import com.elice.homealone.module.talk.dto.TalkRequestDTO;
import com.elice.homealone.module.talk.dto.TalkResponseDTO;
import com.elice.homealone.module.talk.dto.TrendTalkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
@Tag(name = "TalkController", description = "혼잣말 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/talk")
public class TalkController {
    @Autowired
    private TalkService talkService;

    @Operation(summary = "혼잣말 검색, 조회")
    @GetMapping("")
    public ResponseEntity<Page<TalkResponseDTO>> findAll(@RequestParam(required = false) String all,
                                                        @RequestParam(required = false) String title,
                                                        @RequestParam(required = false) String content,
                                                        @RequestParam(required = false) String tag,
                                                        @RequestParam(required = false) String memberName,
                                                        @PageableDefault(size = 20) Pageable pageable){

        Page<TalkResponseDTO> talkSummaryDtos = talkService.searchTalkPost(all,title, content,tag, memberName, pageable);
        return ResponseEntity.ok().body(talkSummaryDtos);
    }
    @Operation(summary = "혼잣말 게시글 생성")
    @PostMapping("")
    public ResponseEntity<TalkResponseDTO.TalkInfoDto> createRoomPost(@Validated @RequestBody TalkRequestDTO talkDto) {
        TalkResponseDTO.TalkInfoDto talkInfoDto = talkService.CreateTalkPost(talkDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(talkInfoDto);
    }
    @Operation(summary = "혼잣말 게시글 수정")
    @PatchMapping("/{talkId}")
    public ResponseEntity<TalkResponseDTO.TalkInfoDto> editRoomPost(@PathVariable Long talkId
                                            , @Validated @RequestBody TalkRequestDTO talkDto//사용자 받아 글쓴 회원과 일치하는지 확인 로직 추가
                                            ){
        TalkResponseDTO.TalkInfoDto talkInfoDto = talkService.EditTalkPost(talkId, talkDto);
        return ResponseEntity.status(HttpStatus.OK).body(talkInfoDto);

    }
    @Operation(summary = "혼잣말 게시글 삭제")
    @DeleteMapping("/{talkId}")
    public ResponseEntity<Response.ApiResponse> deletePost(@PathVariable Long talkId){//사용자 받아 글쓴 회원과 일치하는지 확인 로직 추가
        talkService.deleteRoomPost(talkId);
        Response.ApiResponse response = new Response.ApiResponse("혼잣말 "+talkId+"번 게시글이 성공적으로 지워졌습니다.");
        return ResponseEntity.ok().body(response);
    }
    @Operation(summary = "혼잣말 게시글 상세 조회")
    @GetMapping("/{talkId}")
    public ResponseEntity<TalkResponseDTO.TalkInfoDto> findRoomById (@PathVariable Long talkId){
        TalkResponseDTO.TalkInfoDto byTalkId = talkService.findByTalkId(talkId);
        return ResponseEntity.ok().body(byTalkId);

    }
    @Operation(summary = "혼잣말 인기 게시글 조회")
    @GetMapping("/view")
    public ResponseEntity<Page<TrendTalkResponse>> findTopTalkByView(@PageableDefault(size = 8) Pageable pageable){
        Page<TrendTalkResponse> topTalkByView = talkService.findTopTalkByView(pageable);
        return ResponseEntity.ok(topTalkByView);
    }
    @Operation(summary = "혼잣말 회원으로 조회")
    @GetMapping("/member")
    public ResponseEntity<Page<TalkResponseDTO>> findTalkByMember(@PageableDefault(size = 10) Pageable pageable){
        Page<TalkResponseDTO> talkByMember = talkService.findTalkByMember(pageable);
        return ResponseEntity.ok(talkByMember);
    }

}
