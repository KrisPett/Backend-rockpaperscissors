package com.example.rockpaperscissors.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class User {
    String id;
    String name;
    Move move;

    @JsonCreator
    public User(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("move") Move move) {
        this.id = id;
        this.name = name;
        this.move = move;
    }
}
