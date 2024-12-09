package com.elice.homealone.module.recipe.entity;

import com.elice.homealone.module.room.entity.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
public class RecipeViewLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "recipe_id",nullable = false)
    private Recipe recipe;

    @Column
    private LocalDateTime timeStamp;
}
