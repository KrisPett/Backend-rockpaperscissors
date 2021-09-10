package com.example.rockpaperscissors.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity(name = "games")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameEntity {
    @Id private String id;

    @JoinColumn(name = "game")
    private Game game;

    @ManyToOne()
    @JoinColumn(name = "player_Id")
    private UserEntity playerEntity;

    @ManyToOne
    @JoinColumn(name = "opponent_Id")
    private UserEntity opponentEntity;
}
