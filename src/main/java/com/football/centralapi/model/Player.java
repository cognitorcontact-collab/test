package com.football.centralapi.model;

import com.football.centralapi.model.enums.PlayerPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String id;
    private String name;
    private int number;
    private PlayerPosition position;
    private String nationality;
    private int age;
    private String clubId;
}