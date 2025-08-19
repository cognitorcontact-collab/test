package com.football.centralapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClubDTO {
    private String id;
    private String name;
    private String acronym;
    private int yearCreation;
    private String stadium;
    private CoachDTO coach;
}