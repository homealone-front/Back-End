package com.elice.homealone.module.member.service;


import com.elice.homealone.module.login.service.AuthService;
import com.elice.homealone.module.member.dto.MemberDto;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MemberService{
    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final MemberQueryService memberQueryService;

    public Member editMember(MemberDto memberDTO) {
        Member member = authService.getMember();
        Optional.ofNullable(memberDTO.getName()).ifPresent(name->member.setName(name));
        Optional.ofNullable(memberDTO.getBirth()).ifPresent(birth->member.setBirth(birth));
        Optional.ofNullable(memberDTO.getFirstAddress()).ifPresent(first->member.setFirstAddress(first));
        Optional.ofNullable(memberDTO.getSecondAddress()).ifPresent(second->member.setSecondAddress(second));
        Optional.ofNullable(memberDTO.getImageUrl()).ifPresent(address->member.setImageUrl(address));
        memberRepository.save(member);
        return member;
    }

    /**
     * 회원 탈퇴 withdrawal
     */
    public boolean withdrawal(Member member) {
        Member findedMember = memberQueryService.findByEmail(member.getEmail());
        findedMember.setDeletedAt(true);
        return true;
    }

    /**
     * 회원 삭제 delete
     */
    public void deleteMember(Long memberId) {
        Member findedMember = memberQueryService.findById(memberId);
        memberRepository.delete(findedMember);
    }

}
