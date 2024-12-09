package com.elice.homealone.module.recipe.repository;

import com.elice.homealone.module.recipe.entity.Recipe;
import com.elice.homealone.module.recipe.entity.RecipeViewLog;
import com.elice.homealone.module.room.entity.Room;
import com.elice.homealone.module.room.entity.RoomViewLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecipeViewRepository extends JpaRepository<RecipeViewLog,Long> {
    List<RecipeViewRepository> findByTimeStampBefore(LocalDateTime dateTime);
    void deleteByTimeStampBefore(LocalDateTime dateTime);
    @Query("SELECT r FROM RecipeViewLog rv Join rv.recipe r WHERE rv.timeStamp >= :monthAgo GROUP BY r ORDER BY COUNT(rv) DESC ")
    Page<Recipe> findTopRecipesByViewCountInLastWeek(@Param("monthAgo") LocalDateTime monthAgo, Pageable pageable);

}
