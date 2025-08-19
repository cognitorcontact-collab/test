package com.example.football.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goal {
    private String id;
    private Integer minuteOfGoal;
    private Boolean ownGoal;
    private String matchId;
    private String playerId;
    private String clubId;
}