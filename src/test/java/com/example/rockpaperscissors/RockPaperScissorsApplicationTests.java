package com.example.rockpaperscissors;

import com.example.rockpaperscissors.exceptions.TokenNotFoundException;
import com.example.rockpaperscissors.model.*;
import com.example.rockpaperscissors.repository.UserGameRepository;
import com.example.rockpaperscissors.repository.UsersRepository;
import com.example.rockpaperscissors.security.Token;
import com.example.rockpaperscissors.services.GameService;
import com.example.rockpaperscissors.services.TokenService;
import com.example.rockpaperscissors.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
class RockPaperScissorsApplicationTests {
    @LocalServerPort
    private int port;

    private TestRestTemplate testRestTemplate;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserGameRepository userGameRepository;

    @Autowired
    private GameService gameService;

    @BeforeEach
    void setUp() {
        testRestTemplate = new TestRestTemplate();
        tokenService.getTokens().clear();
        userGameRepository.deleteAll();
        gameService.getGameRepository().deleteAll();
        userService.getUsersRepository().deleteAll();
    }

    @Test
    void test_token() throws TokenNotFoundException {
        //Given
        String newToken = testRestTemplate.getForObject(testUrl("/auth/token"), String.class);

        //When
        Token token = tokenService.getTokenById(newToken);

        //Then
        assertNotNull(token);
    }

    @Test
    void test_create_user() {
        //When
        HttpHeaders headers = getHttpHeaders();
        ResponseEntity<User> responseEntity = testRestTemplate.postForEntity(
                testUrl("/user/name"),
                new HttpEntity<>("test", headers),
                User.class);

        //Then
        assertEquals(200, responseEntity.getStatusCodeValue());
        UserEntity userEntity = userService.getUsersRepository().findAll().get(0);
        assertEquals("test", userEntity.getName());
    }

    @Test
    void test_get_user_by_id() {
        //Given
        String userId = UUID.randomUUID().toString();
        userService.getUsersRepository().save(new UserEntity(userId, "userById", Role.OWNER, Move.NONE, new ArrayList<>()));

        //When
        User user = testRestTemplate.getForObject(testUrl("/user/" + userId), User.class);

        //Then
        assertEquals(userId, user.getId());
    }

    @Test
    void test_start_game_without_name_success() {
        //When
        HttpHeaders headers = getHttpHeaders();
        ResponseEntity<GameStatus> responseEntity = testRestTemplate.exchange(
                testUrl("/games/start/"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GameStatus.class);

        //Then
        GameStatus gameStatus = responseEntity.getBody();
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(gameStatus.getId(), gameService.getGameRepository().findAll().get(0).getId());
    }

    @Test
    void test_get_all_available_games() {
        //Given
        List<ResponseEntity<GameStatus>> gameList = IntStream.range(0, 10)
                .boxed()
                .map(i -> {
                    HttpHeaders headers = getHttpHeaders();
                    return testRestTemplate.exchange(
                            testUrl("/games/start/"),
                            HttpMethod.GET,
                            new HttpEntity<>(headers),
                            GameStatus.class);
                }).collect(Collectors.toList());

        //When
        HttpHeaders headers = getHttpHeaders();
        ResponseEntity<GameStatus[]> responseEntity = testRestTemplate.exchange(
                testUrl("/games/"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                GameStatus[].class);

        //Then
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(10, gameList.size());
    }

    @Test
    void test_make_move() {
        //Given
        HttpHeaders headers = getHttpHeaders();
        Move move = randomMove();
        testRestTemplate.exchange(
                testUrl("/games/start/"), HttpMethod.GET, new HttpEntity<>(headers), GameStatus.class);

        //When
        ResponseEntity<GameStatus> responseEntity = testRestTemplate.exchange(
                testUrl("/games/move/" + move), HttpMethod.GET, new HttpEntity<>(headers), GameStatus.class);

        //Then
        GameStatus gameStatus = responseEntity.getBody();
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(move, gameStatus.getMove());
    }

    @Test
    void test_join_game() {
        //Given
        IntStream.range(0, 100)
                .boxed()
                .map(i -> {
                    HttpHeaders ownerHeaders = getHttpHeaders();
                    return testRestTemplate.exchange(
                            testUrl("/games/start/"),
                            HttpMethod.GET,
                            new HttpEntity<>(ownerHeaders),
                            GameStatus.class);
                }).collect(Collectors.toList());

        //When
        List<GameEntity> availableGames = gameService.getAllAvailableGames().collect(Collectors.toList());
        int randomIndex = new Random().nextInt(availableGames.size());
        GameEntity gamePicked = availableGames.get(randomIndex);
        ResponseEntity<GameStatus> responseEntity = testRestTemplate.exchange(
                testUrl("/games/join/" + gamePicked.getId()),
                HttpMethod.GET,
                new HttpEntity<>(getHttpHeaders()),
                GameStatus.class);

        //Then
        GameStatus gameStatus = responseEntity.getBody();
        assertEquals(200, responseEntity.getStatusCodeValue());
        assertEquals(gameStatus.getId(), gamePicked.getId());
        assertEquals(Game.OPEN, gamePicked.getGame());
        assertEquals(Game.ACTIVE, gameStatus.getGame());
    }

    @Test
    void test_parallel_games_success() {
        //Given
        int count = 100;

        long start = System.currentTimeMillis();

        IntStream.range(0, count)
                .boxed()
                .parallel()
                .forEach(i -> testRestTemplate.exchange(testUrl("/games/start/"), HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), GameStatus.class));
        List<GameEntity> games = gameService.getAllAvailableGames().collect(Collectors.toList());

        IntStream.range(0, count)
                .boxed()
                //.parallel()
                .forEach(i -> testRestTemplate.exchange(
                        testUrl("/games/join/" + games.get(new Random().nextInt(games.size())).getId()),
                        HttpMethod.GET,
                        new HttpEntity<>(getHttpHeaders()),
                        GameStatus.class));

        IntStream.range(0, count * 2)
                .boxed()
               // .parallel()
                .forEach(i -> {
                    List<UserEntity> allUsers = userService.getUsersRepository().findAll();
                    List<Token> tokens = tokenService.getAllTokens().collect(Collectors.toList());
                    HttpHeaders headers = new HttpHeaders();
                    Optional<Token> tokenFound = tokens.stream()
                            .filter(token -> allUsers.stream()
                            .anyMatch(userEntity -> userEntity.getId().equals(token.getUserId()))).findFirst();
                    headers.add("token", tokenFound.get().getId());

                    testRestTemplate.exchange(testUrl("/games/move/" + randomMove()), HttpMethod.GET, new HttpEntity<>(headers), GameStatus.class);
                });

        List<GameEntity> activeGames = gameService.getAllGames()
                .filter(gameEntity -> gameEntity.getGame().equals(Game.WIN))
                .collect(Collectors.toList());

        int duration = (int) (System.currentTimeMillis() - start);
        assertEquals(games.size(), count);
        assertEquals(1000, activeGames.size());
        assertEquals(duration, 1);
    }

    private HttpHeaders getHttpHeaders() {
        Optional<String> token = getToken();
        HttpHeaders headers = new HttpHeaders();
        headers.add("token", token.get());
        return headers;
    }

    private Optional<String> getToken() {
        return Optional.ofNullable(testRestTemplate.getForObject(testUrl("/auth/token"), String.class));
    }

    private Move randomMove() {
        switch (new Random().nextInt(3)) {
            case 0 -> {
                return Move.ROCK;
            }
            case 1 -> {
                return Move.PAPER;
            }
            case 2 -> {
                return Move.SCISSORS;
            }
            default -> throw new RuntimeException("Error Move");
        }
    }

    private String testUrl(String path) {
        return "http://localhost:" + port + path;
    }
}