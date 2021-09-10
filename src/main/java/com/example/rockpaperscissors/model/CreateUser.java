package com.example.rockpaperscissors.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CreateUser {
    String name;

    @JsonCreator
    public CreateUser(@JsonProperty("name") String name) {
        this.name = name;
    }
}
