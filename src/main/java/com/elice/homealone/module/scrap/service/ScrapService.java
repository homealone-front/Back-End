package com.elice.homealone.module.scrap.service;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.login.service.AuthService;
import com.elice.homealone.module.member.service.MemberQueryService;
import com.elice.homealone.module.member.service.MemberService;
import com.elice.homealone.module.scrap.dto.ScrapReqDto;
import com.elice.homealone.module.scrap.dto.ScrapResDto;
import com.elice.homealone.module.scrap.entity.Scrap;
import com.elice.homealone.module.scrap.repository.ScrapRepository;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.post.entity.Post;
import com.elice.homealone.module.post.sevice.PostService;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final PostService postService;
    private final AuthService authService;
    private final MemberService memberService;
    private final MemberQueryService memberQueryService;

    // 북마크 등록
    @Transactional
    public ScrapResDto createScrap(ScrapReqDto reqDto, Member member) {
        Post post = postService.findById(reqDto.getPostId());

        if (!scrapRepository.existsByPostIdAndMemberId(post.getId(), member.getId())) {
            Scrap scrap = Scrap.builder()
                    .post(post)
                    .member(member)
                    .build();
            scrapRepository.save(scrap);
            return ScrapResDto.fromEntity(scrap);
        } else {
            throw new IllegalStateException("이미 북마크 되어있는 게시물입니다.");
        }
    }

    // 게시물 북마크 개수 조회
    public long countScrapsByPostId(Long postId) {
        return scrapRepository.countByPostId(postId);
    }

    // 북마크 취소
    @Transactional
    public void deleteScrap(Long scrapId) {
        Scrap scrap = scrapRepository.findById(scrapId)
                .orElseThrow(() -> new IllegalArgumentException("Scrap not found with id: " + scrapId));
        scrapRepository.delete(scrap);
    }

    // 북마크 등록 / 취소
    @Transactional
    public ScrapResDto createAndDeleteScrap(ScrapReqDto reqDto) {
        try {
            Member member = authService.getMember();
            member = memberQueryService.findById(member.getId());
            Post post = postService.findById(reqDto.getPostId());
            Optional<Scrap> scrap = scrapRepository.findByMemberIdAndPostId(member.getId(), post.getId());
            if(scrap.isEmpty()){
                Scrap newScrap = reqDto.toEntity(member, post);
                member.getScraps().add(newScrap);
                post.getScraps().add(newScrap);
                scrapRepository.save(newScrap);
                ScrapResDto resDto = ScrapResDto.fromEntity(newScrap);
                resDto.setTotalCount(post.getScraps().size());
                return ScrapResDto.fromEntity(newScrap);
            }
            scrapRepository.delete(scrap.get());
            return null;
        } catch (HomealoneException e) {
            if(e.getErrorCode()==ErrorCode.MEMBER_NOT_FOUND) {
                return null;
            } else {
            throw new HomealoneException(ErrorCode.BAD_REQUEST);
            }
        }
    }

    public List<Scrap> findScrapsByMemberAndPostIn(Member member, List<Post> posts) {
        return scrapRepository.findByMemberAndPostIn(member, posts);
    }

    // 멤버가 게시글을 스크랩 했는지 여부
    public boolean isScrapedByMember(Post post, Member member) {
        return scrapRepository.existsByPostAndMember(post, member);
    }

    @Transactional
    public boolean isScrapdeByMember(Long postId, Long memberId) {
        return scrapRepository.existsByPostIdAndMemberId(postId, memberId);
    }

    public List<Scrap> findByMemberIdAndPostType(Long memberId, Post.Type type) {
        List<Scrap> scraps = scrapRepository.findByMemberIdAndPostType(memberId, type);

        return scraps;
    }

    public void deleteScrapByPost(Post post){
        scrapRepository.deleteScrapByPost(post);
    }
}
