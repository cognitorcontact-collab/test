package com.football.centralapi.dto;

import com.football.centralapi.model.enums.Championship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChampionshipRankingDTO {
    private int rank;
    private Championship championship;
    private double differenceGoalsMedian;
}