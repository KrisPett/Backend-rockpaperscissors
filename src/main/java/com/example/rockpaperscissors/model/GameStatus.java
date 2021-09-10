package com.example.rockpaperscissors.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class GameStatus {
    String id;
    String name;
    Move move;
    Game game;
    String opponentName;
    Move opponentMove;

    @JsonCreator
    public GameStatus(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("move") Move move,
            @JsonProperty("game") Game game,
            @JsonProperty("opponentName") String opponentName,
            @JsonProperty("opponentMove") Move opponentMove) {
        this.id = id;
        this.name = name;
        this.move = move;
        this.game = game;
        this.opponentName = opponentName;
        this.opponentMove = opponentMove;
    }
}
