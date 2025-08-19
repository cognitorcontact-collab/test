package com.football.centralapi.dto;

import com.football.centralapi.model.enums.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayingTimeDTO {
    private double value;
    private TimeUnit durationUnit;
}