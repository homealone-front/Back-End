package com.elice.homealone.module.recipe.service;

import com.elice.homealone.module.recipe.entity.Recipe;
import com.elice.homealone.module.recipe.entity.RecipeViewLog;
import com.elice.homealone.module.recipe.repository.RecipeRepository.RecipeRepository;
import com.elice.homealone.module.recipe.repository.RecipeViewRepository;
import com.elice.homealone.module.room.entity.Room;
import com.elice.homealone.module.room.entity.RoomViewLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RecipeViewLogService {
    private final RecipeViewRepository recipeViewRepository;

    @Transactional
    public void logView(Recipe recipe){
        RecipeViewLog recipeViewLog = new RecipeViewLog(null, recipe, LocalDateTime.now());
        recipeViewRepository.save(recipeViewLog);
    }
    @Transactional
    public Page<Recipe> findTop4RecipesByViewCountInLastWeek(LocalDateTime localDateTime, Pageable pageable){
        // 서비스나 호출하는 곳에서
        Page<Recipe> topRecipes = recipeViewRepository.findTopRecipesByViewCountInLastWeek(localDateTime, pageable);
        return topRecipes;

    }

}
