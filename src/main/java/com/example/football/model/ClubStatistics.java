package com.example.football.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubStatistics {
    private Club club;
    private Integer rankingPoints;
    private Integer scoredGoals;
    private Integer concededGoals;
    private Integer differenceGoals;
    private Integer cleanSheetNumber;
}