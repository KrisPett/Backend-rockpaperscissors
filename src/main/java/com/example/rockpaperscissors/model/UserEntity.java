package com.example.rockpaperscissors.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity(name = "user")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {
    @Id private String id;

    private String name;

    private Role role;

    @JoinColumn()
    private Move move;

    @OneToMany(mappedBy = "playerEntity")
    private List<UserGameEntity> gameEntityList;

    public void addUserGameEntity(UserGameEntity gameEntity) {
        gameEntityList.add(gameEntity);
    }
}
