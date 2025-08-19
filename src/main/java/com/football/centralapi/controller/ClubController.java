package com.football.centralapi.controller;

import com.football.centralapi.dto.ClubRankingDTO;
import com.football.centralapi.service.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bestClubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @GetMapping
    public ResponseEntity<List<ClubRankingDTO>> getBestClubs(
            @RequestParam(defaultValue = "5") int top) {
        List<ClubRankingDTO> bestClubs = clubService.getBestClubs(top);
        return ResponseEntity.ok(bestClubs);
    }
}