package com.example.rockpaperscissors.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "USER_DOES_NOT_EXIST")
public class UserNotFoundException extends Exception{
}
