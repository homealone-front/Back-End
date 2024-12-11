package com.elice.homealone.module.member.service;


import com.elice.homealone.module.login.service.AuthService;
import com.elice.homealone.module.member.dto.MemberDto;
import com.elice.homealone.module.member.dto.MypageRequest;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MemberService{
    private final MemberRepository memberRepository;
    private final AuthService authService;
    private final MemberQueryService memberQueryService;

    @Transactional
    public void editMember(MypageRequest mypageRequest) {
        Member member = authService.getMember();
        member.upate(mypageRequest);
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
