package com.example.rockpaperscissors.services;

import com.example.rockpaperscissors.exceptions.UserNotFoundException;
import com.example.rockpaperscissors.model.Move;
import com.example.rockpaperscissors.model.Role;
import com.example.rockpaperscissors.model.UserEntity;
import com.example.rockpaperscissors.repository.GamesRepository;
import com.example.rockpaperscissors.repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class UserService {
    UsersRepository usersRepository;
    GamesRepository gamesRepository;

    public UserService(UsersRepository usersRepository, GamesRepository gamesRepository) {
        this.usersRepository = usersRepository;
        this.gamesRepository = gamesRepository;
    }

    public Stream<UserEntity> getAllUsers() {
        return usersRepository.findAll().stream();
    }

    public UserEntity createUser(String name) {
        name = name.substring(1, name.length() - 1);
        UserEntity user = new UserEntity(UUID.randomUUID().toString(), name, Role.OWNER, Move.NONE, new ArrayList<>());
        usersRepository.save(user);
        return user;
    }

    public UserEntity getUserById(String id) throws UserNotFoundException {
        return usersRepository.findById(id).
                orElseThrow(UserNotFoundException::new);
    }

    public UsersRepository getUsersRepository() {
        return usersRepository;
    }
}
