package com.example.football.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coach {
    private String id;
    private String name;
    private String nationality;
}