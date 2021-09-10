package com.example.rockpaperscissors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class GameNotAllowedException extends Exception {
    public GameNotAllowedException(String message) {
        super(message);
    }
}
