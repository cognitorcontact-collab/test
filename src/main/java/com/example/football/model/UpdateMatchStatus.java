package com.example.football.model;

import com.example.football.model.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMatchStatus {
    private MatchStatus status;
}