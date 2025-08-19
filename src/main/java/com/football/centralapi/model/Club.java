package com.football.centralapi.model;

import com.football.centralapi.model.enums.Championship;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Club {
    private String id;
    private String name;
    private String acronym;
    private int yearCreation;
    private String stadium;
    private Coach coach;
    private Championship championship;
}