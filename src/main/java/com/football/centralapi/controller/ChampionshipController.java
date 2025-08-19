package com.football.centralapi.controller;

import com.football.centralapi.dto.ChampionshipRankingDTO;
import com.football.centralapi.service.ChampionshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/championshipRankings")
public class ChampionshipController {

    private final ChampionshipService championshipService;

    public ChampionshipController(ChampionshipService championshipService) {
        this.championshipService = championshipService;
    }

    @GetMapping
    public ResponseEntity<List<ChampionshipRankingDTO>> getChampionshipRankings() {
        List<ChampionshipRankingDTO> rankings = championshipService.getChampionshipRankings();
        return ResponseEntity.ok(rankings);
    }
}