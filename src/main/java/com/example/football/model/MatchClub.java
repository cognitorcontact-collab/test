package com.example.football.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchClub {
    private String id;
    private String name;
    private String acronym;
    private Integer score;
    private List<Scorer> scorers;
}