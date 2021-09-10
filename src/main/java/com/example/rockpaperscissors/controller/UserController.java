package com.example.rockpaperscissors.controller;

import com.example.rockpaperscissors.exceptions.TokenNotFoundException;
import com.example.rockpaperscissors.exceptions.UserNotFoundException;
import com.example.rockpaperscissors.model.*;
import com.example.rockpaperscissors.security.Token;
import com.example.rockpaperscissors.services.GameService;
import com.example.rockpaperscissors.services.TokenService;
import com.example.rockpaperscissors.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@AllArgsConstructor
@RequestMapping("user")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {
    UserService userService;
    TokenService tokenService;
    GameService gameService;

    @GetMapping("/all-users")
    public Stream<UserGame> getAllUsers() {
        return userService.getAllUsers().map(this::convertToUserGame);
    }

    @GetMapping
    public UserGame getUserByTokenId(@RequestHeader(value = "token") String tokenId) throws UserNotFoundException, TokenNotFoundException {
        Token tokenById = tokenService.getTokenById(tokenId);
        return convertToUserGame(userService.getUserById(tokenById.getUserId()));
    }

    @PostMapping("/name")
    public void createUser(
            @RequestBody String name,
            @RequestHeader(value = "token") String tokenId) throws TokenNotFoundException {
        Token tokenById = tokenService.getTokenById(tokenId);
        UserEntity user = userService.createUser(name);
        Optional.of(tokenById).ifPresent(token -> token.setUserId(user.getId()));
    }

    public UserGame convertToUserGame(UserEntity userEntity) {
        return new UserGame(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getGameEntityList().stream()
                        .map(userGameEntity -> new FinishedGames(
                                userGameEntity.getGameEntity().getId(),
                                convertPlayerPerspective(userGameEntity),
                                convertGamePerspective(userGameEntity),
                                convertOpponentPerspective(userGameEntity, userEntity)))
                        .collect(Collectors.toList()));
    }

    private Game convertGamePerspective(UserGameEntity userGameEntity) {
        if (userGameEntity.getRole().equals(Role.JOINER)) {
            switch (userGameEntity.getGameEntity().getGame()) {
                case WIN -> { return Game.LOSE; }
                case LOSE -> { return Game.WIN; }
            }
        } return userGameEntity.getGameEntity().getGame();
    }

    private String convertPlayerPerspective(UserGameEntity userGameEntity) {
        if (userGameEntity.getRole().equals(Role.JOINER)) {
            return userGameEntity.getGameEntity().getOpponentEntity().getName();
        }
        return userGameEntity.getGameEntity().getPlayerEntity().getName();
    }

    private String convertOpponentPerspective(UserGameEntity userGameEntity, UserEntity userEntity) {
        if (userGameEntity.getGameEntity().getOpponentEntity() == null) return "null";
        if (userGameEntity.getGameEntity().getPlayerEntity().getId().equals(userEntity.getId())) {
            return userGameEntity.getGameEntity().getOpponentEntity().getName();

        } else if (userGameEntity.getGameEntity().getOpponentEntity().getId().equals(userEntity.getId())) {
            return userGameEntity.getGameEntity().getPlayerEntity().getName();
        }
        return "null";
    }
}
