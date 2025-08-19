package com.example.football.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Club {
    private String id;
    private String name;
    private String acronym;
    private Integer yearCreation;
    private String stadium;
    private Coach coach;
}