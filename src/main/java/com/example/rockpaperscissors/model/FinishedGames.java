package com.example.rockpaperscissors.model;

import lombok.Value;

@Value
public class FinishedGames {
    String id;
    String playerName;
    Game game;
    String opponentName;
}
