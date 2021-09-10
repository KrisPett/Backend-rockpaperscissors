package com.example.rockpaperscissors.services;

import com.example.rockpaperscissors.exceptions.GameNotAllowedException;
import com.example.rockpaperscissors.exceptions.GameNotFoundException;
import com.example.rockpaperscissors.exceptions.UserNotFoundException;
import com.example.rockpaperscissors.model.*;
import com.example.rockpaperscissors.repository.GamesRepository;
import com.example.rockpaperscissors.repository.UserGameRepository;
import com.example.rockpaperscissors.repository.UsersRepository;
import com.example.rockpaperscissors.security.Token;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class GameService {
    GamesRepository gameRepository;
    UsersRepository usersRepository;
    UserGameRepository userGameRepository;

    public GameService(GamesRepository gameRepository, UsersRepository usersRepository, UserGameRepository userGameRepository) {
        this.gameRepository = gameRepository;
        this.usersRepository = usersRepository;
        this.userGameRepository = userGameRepository;
    }

    public GameEntity startGame(Token token) throws UserNotFoundException {
        checkIfUserIsCreated(token);
        return createNewGame(token);
    }

    private GameEntity createNewGame(Token token) throws UserNotFoundException {
        UserEntity userEntity = usersRepository.findById(token.getUserId()).orElseThrow(UserNotFoundException::new);
        GameEntity newGame = createNewGame(userEntity);
        Optional.of(token).ifPresent(token1 -> token1.setGameId(newGame.getId()));
        token.setGameId(newGame.getId());
        UserGameEntity userGameEntity = new UserGameEntity(UUID.randomUUID().toString(), Role.OWNER, userEntity, newGame);
        userEntity.addUserGameEntity(userGameEntity);
        gameRepository.save(newGame);
        userGameRepository.save(userGameEntity);
        return newGame;
    }

    private void checkIfUserIsCreated(Token token) {
        if (token.getUserId() == null) {
            createAnonymousUser(token);
        }
    }

    private void createAnonymousUser(Token token) {
        UserEntity userEntity = new UserEntity(
                UUID.randomUUID().toString(),
                "anonymous",
                Role.OWNER,
                Move.NONE,
                new ArrayList<>());
        token.setUserId(userEntity.getId());
        usersRepository.save(userEntity);
    }

    private GameEntity createNewGame(UserEntity user) {
        return new GameEntity(
                UUID.randomUUID().toString(),
                Game.OPEN,
                user,
                null);
    }

    final Object lock = new Object();

    public GameEntity joinGame(String gameId, Token token) throws GameNotFoundException, GameNotAllowedException, UserNotFoundException {
        GameEntity game = gameRepository.findById(gameId).orElseThrow(GameNotFoundException::new);
        checkIfUserIsCreated(token);
        checkIfOpponentIdIsUnique(token.getUserId(), game);
        return joinIfGameIsOpen(game, token);
    }

    private GameEntity joinIfGameIsOpen(GameEntity game, Token token) throws GameNotAllowedException, UserNotFoundException {
        synchronized (lock) {
            if (game.getGame().equals(Game.OPEN)) {
                UserEntity userEntity = usersRepository.findById(token.getUserId()).orElseThrow(UserNotFoundException::new);
                UserGameEntity userGameEntity = new UserGameEntity(UUID.randomUUID().toString(), Role.JOINER, userEntity, game);
                userEntity.setRole(Role.JOINER);
                userEntity.addUserGameEntity(userGameEntity);
                userEntity.setMove(Move.NONE);
                game.setOpponentEntity(userEntity);
                token.setGameId(game.getId());
                updateGameStatusCode(game);
                userGameRepository.save(userGameEntity);
                gameRepository.save(game);
                return game;
            }
            throw new GameNotAllowedException("GAME_ALREADY_STARTED");
        }
    }

    private void checkIfOpponentIdIsUnique(String userId, GameEntity game) throws GameNotAllowedException {
        if (game.getPlayerEntity().getId().equals(userId)) {
            throw new GameNotAllowedException("PLAYER_ALREADY_IN_GAME");
        }
    }

    public Stream<GameEntity> getAllAvailableGames() {
        return gameRepository.findAll().stream()
                .filter(gameEntity -> gameEntity.getGame() == Game.OPEN);
    }

    public Stream<GameEntity> getAllGames() {
        return gameRepository.findAll().stream();
    }

    public User getGameInfo(UserEntity userEntity) {
        return new User(userEntity.getId(), userEntity.getName(), userEntity.getMove());
    }

    public GameEntity makeMove(Move sign, UserEntity userById, Token token) throws GameNotFoundException {
        if (checkIfUserIsNotInGame(sign, userById, token)) {
            return null;
        }
        return changeMoveForUserInGame(sign, userById, token);
    }

    private GameEntity changeMoveForUserInGame(Move sign, UserEntity userById, Token token) throws GameNotFoundException {
        GameEntity gameById = getGameById(token.getGameId());
        changeMove(sign, userById, token, gameById);
        GameEntity gameEntity = updateGameStatusCode(gameById);
        usersRepository.save(userById);
        gameRepository.save(gameEntity);
        return gameEntity;
    }

    private boolean checkIfUserIsNotInGame(Move sign, UserEntity userById, Token token) {
        if (token.getGameId() == null) {
            userById.setMove(sign);
            usersRepository.save(userById);
            return true;
        }
        return false;
    }

    private GameEntity updateGameStatusCode(GameEntity gameById) {
        UserEntity playerEntity = gameById.getPlayerEntity();
        UserEntity opponentEntity = gameById.getOpponentEntity();

        if (opponentEntity != null) {
            switch (playerEntity.getMove()) { //Determine Winner
                case ROCK -> {
                    switch (opponentEntity.getMove()) {
                        case ROCK -> gameById.setGame(Game.DRAW);
                        case SCISSORS -> gameById.setGame(Game.WIN);
                        case PAPER -> gameById.setGame(Game.LOSE);
                    }
                }
                case PAPER -> {
                    switch (opponentEntity.getMove()) {
                        case PAPER -> gameById.setGame(Game.DRAW);
                        case ROCK -> gameById.setGame(Game.WIN);
                        case SCISSORS -> gameById.setGame(Game.LOSE);
                    }
                }
                case SCISSORS -> {
                    switch (opponentEntity.getMove()) {
                        case SCISSORS -> gameById.setGame(Game.DRAW);
                        case PAPER -> gameById.setGame(Game.WIN);
                        case ROCK -> gameById.setGame(Game.LOSE);
                    }
                }
                default -> gameById.setGame(Game.ACTIVE);
            }
        }
        return gameById;
    }

    private void changeMove(Move sign, UserEntity userById, Token token, GameEntity gameById) {
        userById.setMove(sign);
        if (gameById.getPlayerEntity().getId().equals(token.getUserId())) {
            gameById.setPlayerEntity(userById);
        } else if (gameById.getOpponentEntity().getId().equals(token.getUserId())) {
            gameById.setOpponentEntity(userById);
        }
    }

    public GameEntity getGameById(String id) throws GameNotFoundException {
        return gameRepository.findById(id)
                .orElseThrow(GameNotFoundException::new);
    }

    public GamesRepository getGameRepository() {
        return gameRepository;
    }

    public String deleteGameById(Token tokenById) throws GameNotFoundException {
        UserGameEntity userGameEntity1 = userGameRepository.findAll().stream()
                .filter(userGameEntity -> userGameEntity.getGameEntity().getId().equals(tokenById.getGameId()))
                .findFirst()
                .orElseThrow(GameNotFoundException::new);
        userGameRepository.deleteById(userGameEntity1.getId());
        gameRepository.deleteById(tokenById.getGameId());
        return tokenById.getGameId();
    }
}
