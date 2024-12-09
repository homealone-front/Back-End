package com.elice.homealone.module.post.sevice;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.like.entity.Like;
import com.elice.homealone.module.like.repository.LikeRepository;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.login.service.AuthService;
import com.elice.homealone.module.post.dto.PostRelatedDto;
import com.elice.homealone.module.post.entity.Post;
import com.elice.homealone.module.post.entity.Post.Type;
import com.elice.homealone.module.post.repository.PostRepository;
import com.elice.homealone.module.recipe.entity.Recipe;
import com.elice.homealone.module.scrap.entity.Scrap;
import com.elice.homealone.module.scrap.repository.ScrapRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final ScrapRepository scrapRepository;
    private final AuthService authService;

    public Post findById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new HomealoneException(ErrorCode.POST_NOT_FOUND));
        return post;
    }

    // 게시물 리스트를 받아 멤버가 좋아요 한 게시물을 돌려준다.
    public Set<Long> getLikedPostIds(Member member, List<Post> posts) {
        List<Like> likes = likeRepository.findByMemberAndPostIn(member, posts);
        return likes.stream()
            .map(like -> like.getPost().getId())
            .collect(Collectors.toSet());
    }

    // 게시물 리스트를 받아 멤버가 스크랩 한 게시물을 돌려준다.
    public Set<Long> getScrapedPostIds(Member member, List<Post> posts) {
        List<Scrap> scraps = scrapRepository.findByMemberAndPostIn(member, posts);
        return scraps.stream()
            .map(scrap -> scrap.getPost().getId())
            .collect(Collectors.toSet());
    }


    public PostRelatedDto getPostRelated(Post post){
        return PostRelatedDto.builder()
            .commentCount(post.getComments().size())
            .likeCount(post.getLikes().size())
            .scrapCount(post.getScraps().size())
            .build();
    }

    public <T, D> Page<D> findByScrap(Pageable pageable, Post.Type postType, Class<T> type, Function<T, D> mapper) {
        Member member = authService.getMember();
        List<Scrap> scraps = scrapRepository.findByMemberIdAndPostType(member.getId(), postType);
        List<T> items = scraps.stream()
            .map(scrap -> type.cast(Hibernate.unproxy(scrap.getPost())))
            .collect(Collectors.toList());
        Page<T> itemPage = PageableExecutionUtils.getPage(
            items,
            pageable,
            items::size
        );
        return itemPage.map(mapper);
    }

    public Page<Recipe> getRecipeByLikes(Pageable pageable) {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Page<Post> postPage = postRepository.findRecipesByLikesInLastWeek(Type.RECIPE, oneWeekAgo, pageable);
        return postPage.map(post -> (Recipe) post);
    }
}
