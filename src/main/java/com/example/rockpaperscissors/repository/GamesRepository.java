package com.example.rockpaperscissors.repository;

import com.example.rockpaperscissors.model.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GamesRepository extends JpaRepository<GameEntity, String> {
}
