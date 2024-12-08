package com.elice.homealone.module.member.controller;

import com.elice.homealone.module.member.dto.MemberDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.AuthService;
import com.elice.homealone.module.member.service.MemberQueryService;
import com.elice.homealone.module.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "AdminController", description = "관리자 API")
public class AdminController {
    private final MemberService memberService;
    private final MemberQueryService memberQueryService;
    @Operation(summary = "전체 회원 조회")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/member")
    public ResponseEntity<Page<Member>> getAllMember(@PageableDefault(size = 3) Pageable pageable) {
        Page<Member> members = memberQueryService.findAll(pageable);
        return ResponseEntity.ok(members);
    }

    @Operation(summary = "회원 조회")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable Long memberId) {
        MemberDto memberDTO = MemberDto.from(memberQueryService.findById(memberId));
        return ResponseEntity.ok(memberDTO);
    }

    @Operation(summary = "회원 삭제")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/member/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
