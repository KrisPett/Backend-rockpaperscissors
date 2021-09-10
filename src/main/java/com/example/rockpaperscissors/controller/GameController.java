package com.example.rockpaperscissors.controller;

import com.example.rockpaperscissors.exceptions.GameNotAllowedException;
import com.example.rockpaperscissors.exceptions.GameNotFoundException;
import com.example.rockpaperscissors.exceptions.TokenNotFoundException;
import com.example.rockpaperscissors.exceptions.UserNotFoundException;
import com.example.rockpaperscissors.model.*;
import com.example.rockpaperscissors.security.Token;
import com.example.rockpaperscissors.services.GameService;
import com.example.rockpaperscissors.services.TokenService;
import com.example.rockpaperscissors.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@AllArgsConstructor
@RequestMapping("/games")
@CrossOrigin(origins ="*", allowedHeaders ="*")
public class GameController {
    TokenService tokenService;
    GameService gameService;
    UserService userService;

    @GetMapping("/start")
    public GameStatus startGame(@RequestHeader(value = "token") String tokenId) throws UserNotFoundException, TokenNotFoundException {
        Token tokenById = tokenService.getTokenById(tokenId);
        GameEntity gameEntity = gameService.startGame(tokenById);
        return convertToGameStatus(gameEntity, tokenById);
    }

    @GetMapping("/join/{gameId}")
    public GameStatus joinGame(
            @RequestHeader(value = "token") String tokenId,
            @PathVariable String gameId) throws TokenNotFoundException, GameNotFoundException, UserNotFoundException, GameNotAllowedException {
        Token tokenById = tokenService.getTokenById(tokenId);
        GameEntity gameEntity = gameService.joinGame(gameId, tokenById);
        return convertToGameStatus(gameEntity, tokenById);
    }

    @GetMapping("/status")
    public GameStatus gameStatus(@RequestHeader(value = "token") String tokenId) throws GameNotFoundException, TokenNotFoundException {
        Token tokenById = tokenService.getTokenById(tokenId);
        GameEntity gameById = gameService.getGameById(tokenById.getGameId());
        return convertToGameStatus(gameById, tokenById);
    }

    @GetMapping
    public List<GameStatus> gameList(@RequestHeader(value = "token") String tokenId) throws TokenNotFoundException {
        Token tokenById = tokenService.getTokenById(tokenId);
        return gameService.getAllAvailableGames()
                .map(gameEntity -> convertToGameStatus(gameEntity, tokenById))
                .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public User gameInfo(
            @RequestHeader(value = "token") String tokenId,
            @PathVariable String id) throws UserNotFoundException, TokenNotFoundException {
        tokenService.getTokenById(tokenId);
        UserEntity userEntity = userService.getUserById(id);
        return gameService.getGameInfo(userEntity);
    }

    @GetMapping("/move/{sign}")
    public GameStatus makeMove(
            @RequestHeader(value = "token") String tokenId,
            @PathVariable Move sign) throws TokenNotFoundException, UserNotFoundException, GameNotFoundException {
        Token tokenById = tokenService.getTokenById(tokenId);
        UserEntity userById = userService.getUserById(tokenById.getUserId());
        GameEntity gameEntity = gameService.makeMove(sign, userById, tokenById);
        if (gameEntity == null) return null;
        return convertToGameStatus(gameEntity, tokenById);
    }

    @GetMapping("/all-games")
    public Stream<GameStatus> getAllGames() {
        return gameService.getAllGames()
                .map(gameEntity -> convertToGameStatus(gameEntity, new Token(null, null, null)));
    }

    @DeleteMapping("/delete")
    public String deleteGameById(@RequestHeader(value = "token") String tokenId) throws TokenNotFoundException, GameNotFoundException {
        Token tokenById = tokenService.getTokenById(tokenId);
        return gameService.deleteGameById(tokenById);
    }

    private GameStatus convertToGameStatus(GameEntity gameEntity, Token token) {
        if (checkIfOpponentEntityIsMethodArgument(gameEntity, token)) {
            return convertOpponentsPerspective(gameEntity);
        }
        return convertPlayersPerspective(gameEntity);
    }

    private boolean checkIfOpponentEntityIsMethodArgument(GameEntity gameEntity, Token token) {
        if (gameEntity.getOpponentEntity() != null && gameEntity.getOpponentEntity().getId().equals(token.getUserId())) {
            return true;
        }
        return false;
    }

    private GameStatus convertOpponentsPerspective(GameEntity gameEntity) {
        return GameStatus.builder()
                .id(gameEntity.getId())
                .name(gameEntity.getOpponentEntity() != null ?
                        gameEntity.getOpponentEntity().getName() : "Player-Unknown")
                .move(gameEntity.getOpponentEntity().getMove() != null ?
                        gameEntity.getOpponentEntity().getMove() : Move.NONE)
                .game(configureOpponentGame(gameEntity))
                .opponentName(gameEntity.getPlayerEntity() != null ?
                        gameEntity.getPlayerEntity().getName() : "Opponent-Unknown")
                .opponentMove(gameEntity.getPlayerEntity() != null ?
                        gameEntity.getPlayerEntity().getMove() : Move.NONE)
                .build();
    }

    private GameStatus convertPlayersPerspective(GameEntity gameEntity) {
        return GameStatus.builder()
                .id(gameEntity.getId())
                .name(gameEntity.getPlayerEntity() != null ?
                        gameEntity.getPlayerEntity().getName() : "Player-Unknown")
                .move(gameEntity.getPlayerEntity() != null ?
                        gameEntity.getPlayerEntity().getMove() : Move.NONE)
                .game(gameEntity.getGame())
                .opponentName(gameEntity.getOpponentEntity() != null ?
                        gameEntity.getOpponentEntity().getName() : "Opponent-Unknown")
                .opponentMove(gameEntity.getOpponentEntity() != null ?
                        gameEntity.getOpponentEntity().getMove() : Move.NONE)
                .build();
    }

    private Game configureOpponentGame(GameEntity gameEntity) {
        switch (gameEntity.getGame()) {
            case WIN -> { return Game.LOSE; }
            case LOSE -> { return Game.WIN; }
        }return gameEntity.getGame();
    }
}
