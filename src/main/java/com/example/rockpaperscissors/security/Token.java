package com.example.rockpaperscissors.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Token {
    String id;
    String userId;
    String gameId;
}
