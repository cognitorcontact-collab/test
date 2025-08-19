package com.football.centralapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubRankingDTO {
    private int rank;
    private ClubDTO club;
    private int rankingPoints;
    private int scoredGoals;
    private int concededGoals;
    private int differenceGoals;
    private int cleanSheetNumber;
}