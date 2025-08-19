package com.example.football.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Scorer {
    private Player player;
    private Integer minuteOfGoal;
    private Boolean ownGoal;
}