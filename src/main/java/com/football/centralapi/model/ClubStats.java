package com.football.centralapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubStats {
    private String clubId;
    private String seasonId;
    private int rankingPoints;
    private int scoredGoals;
    private int concededGoals;
    private int differenceGoals;
    private int cleanSheetNumber;
}