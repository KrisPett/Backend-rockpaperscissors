package com.example.rockpaperscissors.services;

import com.example.rockpaperscissors.exceptions.TokenNotFoundException;
import com.example.rockpaperscissors.security.Token;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class TokenService {
    Map<String, Token> tokens = new HashMap<>();

    public Token createToken() {
        Token token = new Token(UUID.randomUUID().toString(), null, null);
        tokens.put(token.getId(), token);
        return token;
    }

    public Stream<Token> getAllTokens() {
        return tokens.values().stream();
    }

    public Token getTokenById(String id) throws TokenNotFoundException {
        return Optional.ofNullable(tokens.get(id))
                .orElseThrow(TokenNotFoundException::new);
    }

    public Map<String, Token> getTokens() {
        return tokens;
    }
}
