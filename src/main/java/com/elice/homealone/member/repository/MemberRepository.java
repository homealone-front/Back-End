package com.elice.homealone.member.repository;

import com.elice.homealone.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findMemberByEmail(String email);

    Member findMemberById(Long id);
}
