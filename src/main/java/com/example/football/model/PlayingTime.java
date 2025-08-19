package com.example.football.model;

import com.example.football.model.enums.DurationUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayingTime {
    private Double value;
    private DurationUnit durationUnit;
}