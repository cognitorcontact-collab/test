package com.football.centralapi.dto;

import com.football.centralapi.model.enums.Championship;
import com.football.centralapi.model.enums.PlayerPosition;
import com.football.centralapi.model.enums.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRankingDTO {
    private int rank;
    private String id;
    private String name;
    private int number;
    private PlayerPosition position;
    private String nationality;
    private int age;
    private Championship championship;
    private int scoredGoals;
    private PlayingTimeDTO playingTime;
}