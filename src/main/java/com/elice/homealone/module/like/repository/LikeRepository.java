package com.elice.homealone.module.like.repository;

import com.elice.homealone.module.like.entity.Like;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.post.entity.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long>, LikeRepositoryCustom {
    List<Like> findByPostId(Long postId);
    List<Like> findByPostIdOrderByCreatedAtDesc(Long postId);

    boolean existsByPostAndMember(Post post, Member member);
    List<Like> findByMemberAndPostIn(Member member, List<Post> posts);

    Optional<Like> findByMemberIdAndPostId(Long memberId, Long postId);

    void deleteLikeByPost(Post post);
}
