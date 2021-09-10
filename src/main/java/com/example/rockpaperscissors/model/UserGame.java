package com.example.rockpaperscissors.model;

import lombok.Value;

import java.util.List;

@Value
public class UserGame {
    String id;
    String name;
    List<FinishedGames> games;

}
