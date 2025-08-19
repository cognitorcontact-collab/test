package com.example.football.model;

import com.example.football.model.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    private String id;
    private LocalDateTime matchDatetime;
    private String stadium;
    private MatchStatus actualStatus;
    private String seasonId;
    private MatchClub clubPlayingHome;
    private MatchClub clubPlayingAway;
}