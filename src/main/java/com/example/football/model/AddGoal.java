package com.example.football.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddGoal {
    private String clubId;
    private String scorerIdentifier;
    private Integer minuteOfGoal;
}