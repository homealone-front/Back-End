package com.elice.homealone.module.like.service;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.like.dto.LikeReqDto;
import com.elice.homealone.module.like.dto.LikeResDto;
import com.elice.homealone.module.like.entity.Like;
import com.elice.homealone.module.like.repository.LikeRepository;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.AuthService;
import com.elice.homealone.module.member.service.MemberService;
import com.elice.homealone.module.post.entity.Post;
import com.elice.homealone.module.post.sevice.PostService;

import com.elice.homealone.module.recipe.entity.Recipe;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final LikeRepository likeRepository;
    private final MemberService memberService;
    private final PostService postService;

    private final AuthService authService;

    @Transactional
    public LikeResDto createAndDeleteLike(LikeReqDto reqDto){
        try {
            Member member = authService.getMember();
            member = memberService.findById(member.getId());
            Post post = postService.findById(reqDto.getPostId());
            Optional<Like> like = likeRepository.findByMemberIdAndPostId(member.getId(), post.getId());
            if(like.isEmpty()){
                Like newLike = reqDto.toEntity(member, post);
                member.getLikes().add(newLike);
                post.getLikes().add(newLike);
                likeRepository.save(newLike);
                LikeResDto resDto = LikeResDto.fromEntity(newLike);
                resDto.setTotalCount(post.getScraps().size());
                return resDto;
            }

            likeRepository.delete(like.get());
            return null;
        } catch (HomealoneException e) {
            if (e.getErrorCode()== ErrorCode.MEMBER_NOT_FOUND) {
                return null;
            } else {
                throw new HomealoneException(ErrorCode.BAD_REQUEST);
            }
        }
    }

    // 좋아요 등록.
    @Transactional
    public LikeResDto createLike(LikeReqDto reqDto, Member member) {
        Post post = postService.findById(reqDto.getPostId());
        Like like = reqDto.toEntity(member, post);
        member.getLikes().add(like);
        post.getLikes().add(like);
        likeRepository.save(like);

        return LikeResDto.fromEntity(like);
    }

    // 게시물 좋아요 list 조회
    public List<LikeResDto> findLikeListByPostId(Long postId) {
        List<Like> likes = likeRepository.findByPostIdOrderByCreatedAtDesc(postId);
        return likes.stream()
                .map(LikeResDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 좋아요 취소
    @Transactional
    public void deleteLike(Long likeId) {
        Like like = likeRepository.findById(likeId)
                .orElseThrow(() -> new IllegalArgumentException("Like not found with id: " + likeId));
        likeRepository.delete(like);
    }

    // 멤버가 게시글을 좋아요 했는지 여부
    public boolean isLikedByMember(Post post, Member member) {
        return likeRepository.existsByPostAndMember(post, member);
    }

    // 멤버가 좋아요 한 리스트 조회
    public List<Like> findLikesByMemberAndPostIn(Member member, List<Post> posts) {
        return likeRepository.findByMemberAndPostIn(member, posts);
    }


    public void deleteLikeByPost(Post post) {
        likeRepository.deleteLikeByPost(post);
    }
}
