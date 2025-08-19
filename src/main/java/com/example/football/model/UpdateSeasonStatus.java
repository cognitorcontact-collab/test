package com.example.football.model;

import com.example.football.model.enums.SeasonStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSeasonStatus {
    private SeasonStatus status;
}