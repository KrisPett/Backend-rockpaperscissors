package com.example.rockpaperscissors.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "user_game")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGameEntity {
    @Id private String id;

    @JoinColumn private Role role;

    @ManyToOne
    @JoinColumn(name = "player_Id")
    private UserEntity playerEntity;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameEntity gameEntity;
}
