package com.example.rockpaperscissors.repository;

import com.example.rockpaperscissors.model.UserGameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGameRepository extends JpaRepository<UserGameEntity, String> {
}
