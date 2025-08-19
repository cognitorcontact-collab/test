package com.example.football.controller;

import com.example.football.model.ClubPlayer;
import com.example.football.model.Player;
import com.example.football.model.PlayerStatistics;
import com.example.football.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlayerController {
    
    private final PlayerService playerService;
    
    @GetMapping("/players")
    public ResponseEntity<List<ClubPlayer>> getPlayers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer ageMinimum,
            @RequestParam(required = false) Integer ageMaximum,
            @RequestParam(required = false) String clubName) {
        
        List<ClubPlayer> players = playerService.getPlayers(name, ageMinimum, ageMaximum, clubName);
        return ResponseEntity.ok(players);
    }
    
    @PutMapping("/players")
    public ResponseEntity<List<Player>> createOrUpdatePlayers(@RequestBody List<Player> players) {
        List<Player> createdOrUpdatedPlayers = playerService.createOrUpdatePlayers(players);
        return ResponseEntity.ok(createdOrUpdatedPlayers);
    }
    
    @GetMapping("/players/{id}/statistics/{seasonYear}")
    public ResponseEntity<PlayerStatistics> getStatisticsOfPlayerById(
            @PathVariable String id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate seasonYear) {
        
        try {
            PlayerStatistics statistics = playerService.getStatisticsOfPlayerById(id, seasonYear);
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}