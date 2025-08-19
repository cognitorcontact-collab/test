package com.example.football.model;

import com.example.football.model.enums.PlayerPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String id;
    private String name;
    private Integer number;
    private PlayerPosition position;
    private String nationality;
    private Integer age;
    private String clubId; // FK to Club, may be null
}