package com.football.centralapi.controller;

import com.football.centralapi.dto.PlayerRankingDTO;
import com.football.centralapi.model.enums.TimeUnit;
import com.football.centralapi.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/bestPlayers")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<List<PlayerRankingDTO>> getBestPlayers(
            @RequestParam(defaultValue = "5") int top,
            @RequestParam TimeUnit playingTimeUnit) {
        List<PlayerRankingDTO> bestPlayers = playerService.getBestPlayers(top, playingTimeUnit);
        return ResponseEntity.ok(bestPlayers);
    }
}