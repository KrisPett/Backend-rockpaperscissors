package com.example.rockpaperscissors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "GAME_DOES_NOT_EXIST")
public class GameNotFoundException extends Exception{
}
