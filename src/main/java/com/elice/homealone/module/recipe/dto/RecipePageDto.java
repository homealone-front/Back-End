package com.elice.homealone.module.recipe.dto;

import com.elice.homealone.module.member.entity.Member;
import com.elice.homealone.module.post.dto.PostRelatedDto;
import com.elice.homealone.module.recipe.entity.Recipe;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

@Getter
public class RecipePageDto {
    private Long id;

    private String title;
    private String description;
    private int portions;
    private String recipeType;
    private int recipeTime;
    private String cuisine;

    private String imageUrl;
    private Long userId;
    private String userName;

    @Setter
    private PostRelatedDto relatedDto;
    private Integer view;
    @Setter
    private String userImage;

    protected RecipePageDto() {
    }

    @Builder
    public RecipePageDto(
        Long id,
        String title,
        String description,
        int portions,
        String recipeType,
        int recipeTime,
        String cuisine,
        String imageUrl,
        Long userId,
        String userName,
        Integer view,
        String userImage) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.portions = portions;
        this.recipeType = recipeType;
        this.recipeTime = recipeTime;
        this.cuisine = cuisine;

        this.imageUrl = imageUrl;
        this.userId = userId;
        this.userName = userName;

        this.view = view;
        this.userImage = userImage;
    }


    public static RecipePageDto toTopRecipePageDto(Recipe recipe) {
        String imageUrl = null;
        if(recipe.getImages() != null){
            imageUrl = recipe.getImages().get(0).getImageUrl();
        }

        Member member = (Member) Hibernate.unproxy(recipe.getMember());
        Long userId = recipe.getMember().getId();
        String userName = recipe.getMember().getName();

        RecipePageDto recipePageDto = RecipePageDto.builder()
                .id(recipe.getId())
                .title(recipe.getTitle())
                .description(recipe.getDescription())
                .portions(recipe.getPortions())
                .recipeType(recipe.getRecipeType().getType())
                .recipeTime(recipe.getRecipeTime().getTime())
                .cuisine(recipe.getCuisine().getCuisine())
                .imageUrl(imageUrl)
                .userId(userId)
                .userName(userName)
                .userImage(member.getImageUrl())
                .build();

        PostRelatedDto relatedDto = PostRelatedDto.builder()
                .commentCount(recipe.getComments().size())
                .likeCount(recipe.getLikes().size())
                .scrapCount(recipe.getScraps().size())
                .build();

        recipePageDto.setRelatedDto(relatedDto);

        return recipePageDto;
    }
}
