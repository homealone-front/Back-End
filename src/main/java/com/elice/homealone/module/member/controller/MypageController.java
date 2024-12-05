package com.elice.homealone.module.member.controller;


import com.elice.homealone.module.comment.dto.CommentResDto;
import com.elice.homealone.module.comment.service.CommentService;
import com.elice.homealone.module.member.dto.MemberDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.AuthService;
import com.elice.homealone.module.post.entity.Post.Type;
import com.elice.homealone.module.post.sevice.PostService;
import com.elice.homealone.module.recipe.dto.RecipePageDto;
import com.elice.homealone.module.recipe.service.RecipeService;
import com.elice.homealone.module.room.dto.RoomResponseDTO;
import com.elice.homealone.module.room.entity.Room;
import com.elice.homealone.module.room.service.RoomService;
import com.elice.homealone.module.talk.Service.TalkService;
import com.elice.homealone.module.talk.dto.TalkResponseDTO;
import com.elice.homealone.module.talk.entity.Talk;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/me")
@Tag(name = "MypageController", description = "마이페이지 관리 API")
public class MypageController {
    private final AuthService authService;
    private final TalkService talkService;
    private final RoomService roomService;
    private final RecipeService recipeService;
    private final CommentService commentService;
    private final PostService postService;

    @Operation(summary = "마이페이지 정보 조회")
    @GetMapping("")
    public ResponseEntity<MemberDto> getMemberInfo() {
        return ResponseEntity.ok(MemberDto.from(authService.getMember()));
    }

    @Operation(summary = "마이페이지 정보 수정")
    @PatchMapping("")
    public ResponseEntity<MemberDto> editMemberInfo(@RequestBody MemberDto memberDTO){
        MemberDto changedMember = MemberDto.from(authService.editMember(memberDTO));
        return ResponseEntity.ok(changedMember);
    }

    @Operation(summary = "방자랑 게시글 회원으로 조회")
    @GetMapping("/room")
    public ResponseEntity<Page<RoomResponseDTO>> findRoomByMember(@PageableDefault(size = 10) Pageable pageable){
        Page<RoomResponseDTO> roomByMember = roomService.findRoomByMember(pageable);
        return ResponseEntity.ok(roomByMember);
    }
    @Operation(summary = "혼잣말 게시글 회원으로 조회")
    @GetMapping("/talk")
    public ResponseEntity<Page<TalkResponseDTO>> findTalkByMember(@PageableDefault(size = 10) Pageable pageable){
        Page<TalkResponseDTO> talkByMember = talkService.findTalkByMember(pageable);
        return ResponseEntity.ok(talkByMember);
    }
    @GetMapping("/recipes")
    public ResponseEntity<Page<RecipePageDto>> findRecipeByMember(@PageableDefault(size=10) Pageable pageable) {
        Member member = authService.getMember();
        Page<RecipePageDto> pageDtos = recipeService.findRecipes(pageable, null, member.getId(), null,null,null,null);
        return new ResponseEntity<>(pageDtos, HttpStatus.OK);
    }

    @GetMapping("/comments")
    public ResponseEntity<Page<CommentResDto>> findCommentByMember(@PageableDefault(size=10) Pageable pageable) {
        Page<CommentResDto> resDtos = commentService.findCommentByMember(pageable);
        return new ResponseEntity<>(resDtos, HttpStatus.OK);
    }

    @GetMapping("/scraps/room")
    public ResponseEntity<Page<RoomResponseDTO>> findRoomByScrap(@PageableDefault(size=10) Pageable pageable) {
        Page<RoomResponseDTO> resDtos = postService.findByScrap(pageable, Type.ROOM, Room.class, RoomResponseDTO::toRoomResponseDTO);
        return new ResponseEntity<>(resDtos, HttpStatus.OK);
    }

    @GetMapping("/scraps/talk")
    public ResponseEntity<Page<TalkResponseDTO>> findTalkByScrap(@PageableDefault(size=10) Pageable pageable) {
        Page<TalkResponseDTO> resDtos = postService.findByScrap(pageable, Type.TALK, Talk.class, TalkResponseDTO::toTalkResponseDTO);
        return new ResponseEntity<>(resDtos, HttpStatus.OK);
    }

    @GetMapping("/scraps/recipes")
    public ResponseEntity<Page<RecipePageDto>> findRecipeByScrap(@PageableDefault(size=10) Pageable pageable) {
        Page<RecipePageDto> resDtos = recipeService.findByScrap(pageable);
        return new ResponseEntity<>(resDtos, HttpStatus.OK);
    }

    @Operation(summary = "이메일 중복 체크")
    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmail(@RequestParam String email) {
        authService.isEmailDuplicate(email);
        return ResponseEntity.ok("사용 가능한 이메일입니다.");
    }

    @Operation(summary = "계정 탈퇴")
    @PatchMapping("/withdrawal")
    public ResponseEntity<String> withdrawal() {
        authService.withdrawal(authService.getMember());
        return ResponseEntity.ok("회원 탈퇴가 완료됐습니다.");
    }



}
