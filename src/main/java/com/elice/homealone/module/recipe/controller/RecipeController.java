package com.elice.homealone.module.recipe.controller;

import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.post.sevice.PostService;
import com.elice.homealone.module.recipe.dto.RecipePageDto;
import com.elice.homealone.module.recipe.dto.RecipeRequestDto;
import com.elice.homealone.module.recipe.dto.RecipeResponseDto;
import com.elice.homealone.module.recipe.entity.RecipeDetail;
import com.elice.homealone.module.recipe.entity.RecipeImage;
import com.elice.homealone.module.recipe.entity.RecipeIngredient;
import com.elice.homealone.module.recipe.service.RecipeService;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;
    private final PostService postService;

    // 레시피 등록
    @PostMapping
    public ResponseEntity<RecipeResponseDto> createRecipe(@AuthenticationPrincipal Member member, @RequestBody RecipeRequestDto requestDto) {
        RecipeResponseDto responseDto = recipeService.createRecipe(member, requestDto);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // 레시피 페이지 조회
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<Page<RecipePageDto>> getRecipe(
        Pageable pageable,
        @RequestParam(required = false) String all,
        @RequestParam(required = false) Long memberId,
        @RequestParam(required = false) String userName,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String description,
        @RequestParam(required = false) List<String> tags
    ) {
        Page<RecipePageDto> pageDtos = recipeService.findRecipes(pageable, all, memberId, userName, title, description, tags);

        return new ResponseEntity<>(pageDtos, HttpStatus.OK);
    }

    // 레시피 상세 조회
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<RecipeResponseDto> getRecipeById(@PathVariable Long id) {
        RecipeResponseDto recipe = recipeService.findById(id);
        return new ResponseEntity<>(recipe, HttpStatus.OK);
    }

    // 레시피 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@AuthenticationPrincipal Member member, @PathVariable Long id) {
        recipeService.deleteRecipe(member, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // 레시피 수정
    @PatchMapping("/{id}")
    public ResponseEntity<RecipeResponseDto> patchRecipe(@AuthenticationPrincipal Member member, @PathVariable Long id, @RequestBody RecipeRequestDto requestDto) {
        RecipeResponseDto patchedRecipe = recipeService.patchRecipe(member, id, requestDto);
        return new ResponseEntity<>(patchedRecipe, HttpStatus.OK);
    }

    @GetMapping("/trends")
    public ResponseEntity<Page<RecipePageDto>> getRecipeByLikes(@PageableDefault(size=4) Pageable pageable) {
        Page<RecipePageDto> trendRecipesDto = recipeService.findTopRecipeByView(pageable);
        return new ResponseEntity<>(trendRecipesDto, HttpStatus.OK);
    }
}

