package com.elice.homealone.module.scrap.repository;

import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.post.entity.Post;
import com.elice.homealone.module.scrap.entity.Scrap;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    long countByPostId(Long postId);
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);
    void deleteByPostIdAndMemberId(Long postId, Long memberId);

    Optional<Scrap> findByMemberIdAndPostId(Long memberId, Long postId);
    List<Scrap> findByMemberAndPostIn(Member member, List<Post> posts);

    boolean existsByPostAndMember(Post post, Member member);

    List<Scrap> findByMemberId(Long memberId);

    @Query("SELECT s FROM Scrap s JOIN s.post p WHERE s.member.id = :memberId AND p.type = :postType")
    List<Scrap> findByMemberIdAndPostType(Long memberId, Post.Type postType);

    void deleteScrapByPost(Post post);
}
