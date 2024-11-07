package com.elice.homealone.module.recipe.service;

import com.elice.homealone.global.exception.ErrorCode;
import com.elice.homealone.global.exception.HomealoneException;
import com.elice.homealone.module.like.service.LikeService;
import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.member.service.AuthService;
import com.elice.homealone.module.member.service.MemberService;
import com.elice.homealone.module.post.dto.PostRelatedDto;
import com.elice.homealone.module.post.entity.Post;
import com.elice.homealone.module.post.sevice.PostService;
import com.elice.homealone.module.recipe.dto.RecipeDetailDto;
import com.elice.homealone.module.recipe.dto.RecipeImageDto;
import com.elice.homealone.module.recipe.dto.RecipePageDto;
import com.elice.homealone.module.recipe.dto.RecipeResponseDto;

import com.elice.homealone.module.recipe.repository.RecipeRepository.RecipeRepository;
import com.elice.homealone.module.recipe.dto.RecipeIngredientDto;
import com.elice.homealone.module.recipe.dto.RecipeRequestDto;
import com.elice.homealone.module.recipe.entity.Recipe;
import com.elice.homealone.module.scrap.service.ScrapService;
import com.elice.homealone.module.tag.Service.PostTagService;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {

    private final AuthService authService;
    private final PostService postService;
    private final RecipeImageService recipeImageService;
    private final RecipeDetailService recipeDetailService;
    private final RecipeIngredientService recipeIngredientService;
    private final PostTagService postTagService;

    private final LikeService likeService;

    private final RecipeRepository recipeRepository;
    private final ScrapService scrapService;


    // 레시피 등록
    @Transactional
    public RecipeResponseDto createRecipe(Member member, RecipeRequestDto requestDto) {
        if (member == null) {
            throw new HomealoneException(ErrorCode.NOT_UNAUTHORIZED_ACTION);
        }
        // 레시피 dto를 통해 기본 레시피 엔티티를 생성
        try {
            Recipe recipe = requestDto.toBaseEntity(member);
            recipeRepository.save(recipe);

            // 레시피 dto 이미지 리스트로 레시피 이미지 생성 후 레시피 엔티티에 추가
            List<RecipeImageDto> images = requestDto.getImages();
            recipeImageService.addRecipeImages(recipe, images);

            // 레시피 dto 재료 리스트를 통해 레시피 재료 생성 후 레시피 엔티티에 추가
            List<RecipeIngredientDto> ingredientDtos = requestDto.getIngredients();
            if(ingredientDtos != null){
                for(RecipeIngredientDto ingredientDto : ingredientDtos) {
                    recipe.addIngredients(recipeIngredientService.createRecipeIngredient(ingredientDto));
                }
            }

            // 레시피 dto 디테일 리스트로 레시피 디테일 생성 후 레시피 엔티티에 추가
            Optional.ofNullable(requestDto.getDetails())
                .ifPresent(detailDtos -> detailDtos.stream()
                    .map(recipeDetailService::createRecipeDetail)
                    .forEach(recipe::addDetail));

            // 태그 리스트로 태그 생성 후 레시피 엔티티(포스트 엔티티) 에 추가
            Optional.ofNullable(requestDto.getPostTags())
                .ifPresent(tagDtos -> tagDtos.stream()
                    .map(postTagService::createPostTag)
                    .forEach(recipe::addTag));

            return recipe.toResponseDto();

        } catch (Exception e) {
            throw new HomealoneException(ErrorCode.RECIPE_CREATION_FAILED);
        }
    }

    // QueryDsl 레시피 페이지 조회
    public Page<RecipePageDto> findRecipes(
        Pageable pageable,
        String all,
        Long memberId,
        String userName,
        String title,
        String description,
        List<String> tags
    ) {
        List<Recipe> recipes = recipeRepository.findRecipes(pageable, all, memberId, userName, title, description, tags);
        Page<Recipe> recipePage = PageableExecutionUtils.getPage(
            recipes,
            pageable,
            () -> recipeRepository.countRecipes( all, memberId, userName, title, description, tags)
        );

        try {
            Member member = authService.getMember();
            // List<Recipe> -> List<Post>
            List<Post> posts = recipes.stream()
                .map(post -> (Post) post)
                .toList();

            Set<Long> likedRecipeIds = getLikedPostIds(member, posts);
            Set<Long> scrapedRecipeIds = getScrapedPostIds(member, posts);

            return recipePage.map(recipe -> createRecipePageDto(recipe, likedRecipeIds, scrapedRecipeIds));
        } catch (HomealoneException e) {
            if (e.getErrorCode()==ErrorCode.MEMBER_NOT_FOUND) {
                return recipePage.map(this::createRecipePageDto);
            } else {
                throw new HomealoneException(ErrorCode.RECIPE_NOT_FOUND);
            }
        }
    }

    // 레시피 상세 조회
    public RecipeResponseDto findById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(()-> new HomealoneException(ErrorCode.RECIPE_NOT_FOUND));

        RecipeResponseDto resDto = recipe.toResponseDto();
        PostRelatedDto relatedDto = postService.getPostRelated(recipe);
        resDto.setRelatedDto(relatedDto);

        try {
            Member member = authService.getMember();
            relatedDto.setLikeByCurrentUser(likeService.isLikedByMember(recipe, member));
            relatedDto.setBookmarked(scrapService.isScrapedByMember(recipe, member));

//            member = memberService.findById(resDto.getUserId());
//            resDto.setUserImage(member.getImageUrl());
            return resDto;
        } catch (HomealoneException e) {
            if (e.getErrorCode()==ErrorCode.MEMBER_NOT_FOUND) {
                return resDto;
            } else {
                throw new HomealoneException(ErrorCode.RECIPE_NOT_FOUND);
            }
        }
    }

    // 레시피 삭제
    @Transactional
    public void deleteRecipe(Member member, Long id) {
        boolean isAdmin = authService.isAdmin(member);
        if (!isAdmin && member == null) {
            throw new HomealoneException(ErrorCode.NOT_UNAUTHORIZED_ACTION);
        }

        Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(()-> new HomealoneException(ErrorCode.RECIPE_NOT_FOUND));
        postTagService.deletePostTagByRecipe(recipe);
        recipeImageService.deleteImageByRecipe(recipe);
        recipeIngredientService.deleteRecipeIngredientByRecipe(recipe);
        recipeDetailService.deleteDetailByRecipe(recipe);
        recipeRepository.delete(recipe);
    }

    // 레시피 업데이트
    @Transactional
    public RecipeResponseDto patchRecipe(Member member, Long id, RecipeRequestDto requestDto) {
        if (member == null) {
            throw new HomealoneException(ErrorCode.NOT_UNAUTHORIZED_ACTION);
        }

        Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(()-> new HomealoneException(ErrorCode.RECIPE_NOT_FOUND));

        // 기본 레시피 수정
        recipe.setTitle(requestDto.getTitle());
        recipe.setDescription(requestDto.getDescription());
        recipe.setPortions(requestDto.getPortions());
        recipe.setRecipeType(requestDto.getRecipeType());
        recipe.setRecipeTime(requestDto.getRecipeTime());
        recipe.setCuisine(requestDto.getCuisine());

        // 연관 관계 수정 (이미지, 재료, 디테일, 태그)

        // 이미지 전체 삭제 후 재생성
        recipeImageService.deleteImageByRecipe(recipe);
        List<RecipeImageDto> imageDtos = requestDto.getImages();
        recipeImageService.addRecipeImages(recipe, imageDtos);

        // 재료 수정
        recipeIngredientService.deleteRecipeIngredientByRecipe(recipe);
        List<RecipeIngredientDto> ingredientDtos = requestDto.getIngredients();
        recipeIngredientService.addRecipeIngredients(recipe, ingredientDtos);

        // 레시피 디테일 수정
        recipeDetailService.deleteDetailByRecipe(recipe);
        List<RecipeDetailDto> detailDtos = requestDto.getDetails();
        recipeDetailService.addRecipeDetails(recipe, detailDtos);

        Recipe updatedRecipe;
        updatedRecipe = recipeRepository.saveAndFlush(recipe);

        return updatedRecipe.toResponseDto();
    }

    private Set<Long> getLikedPostIds(Member member, List<Post> posts) {
        return postService.getLikedPostIds(member, posts);
    }

    private Set<Long> getScrapedPostIds(Member member, List<Post> posts) {
        return postService.getScrapedPostIds(member, posts);
    }

    private RecipePageDto createRecipePageDto(Recipe recipe, Set<Long> likedRecipeIds, Set<Long> scrapedRecipeIds) {
        RecipePageDto pageDto = recipe.toPageDto();
        pageDto.setRelatedDto(postService.getPostRelated(recipe));
        pageDto.getRelatedDto().setLikeByCurrentUser(likedRecipeIds.contains(recipe.getId()));
        pageDto.getRelatedDto().setBookmarked(scrapedRecipeIds.contains(recipe.getId()));

        return pageDto;
    }

    private RecipePageDto createRecipePageDto(Recipe recipe) {
        RecipePageDto pageDto = recipe.toPageDto();
        pageDto.setRelatedDto(postService.getPostRelated(recipe));

        return pageDto;
    }

    // 로그인 한 멤버가 스크랩 한 레시피를 반환 해준다.
    public Page<RecipePageDto> findByScrap(Pageable pageable) {
        return postService.findByScrap(pageable, Post.Type.RECIPE, Recipe.class, this::createRecipePageDto);
    }

    public Page<RecipePageDto> getRecipeByLikes(Pageable pageable) {
        Page<Recipe> recipePage = postService.getRecipeByLikes(pageable);

        try {
            Member member = authService.getMember();
            // List<Recipe> -> List<Post>
            List<Post> posts = recipePage.stream()
                .map(post -> (Post) post)
                .toList();

            Set<Long> likedRecipeIds = getLikedPostIds(member, posts);
            Set<Long> scrapedRecipeIds = getScrapedPostIds(member, posts);

            return recipePage.map(recipe -> createRecipePageDto(recipe, likedRecipeIds, scrapedRecipeIds));
        } catch (HomealoneException e) {
            if (e.getErrorCode()==ErrorCode.MEMBER_NOT_FOUND) {
                return recipePage.map(this::createRecipePageDto);
            } else {
                throw new HomealoneException(ErrorCode.RECIPE_NOT_FOUND);
            }
        }
    }
}
