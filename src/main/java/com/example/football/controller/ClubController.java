package com.example.football.controller;

import com.example.football.model.Club;
import com.example.football.model.ClubStatistics;
import com.example.football.model.Player;
import com.example.football.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClubController {
    
    private final ClubService clubService;
    
    @GetMapping("/clubs")
    public ResponseEntity<List<Club>> getClubs() {
        List<Club> clubs = clubService.getClubs();
        return ResponseEntity.ok(clubs);
    }
    
    @PutMapping("/clubs")
    public ResponseEntity<List<Club>> createOrUpdateClubs(@RequestBody List<Club> clubs) {
        List<Club> createdOrUpdatedClubs = clubService.createOrUpdateClubs(clubs);
        return ResponseEntity.ok(createdOrUpdatedClubs);
    }
    
    @GetMapping("/clubs/{id}/players")
    public ResponseEntity<List<Player>> getClubPlayers(@PathVariable String id) {
        try {
            List<Player> players = clubService.getPlayersByClubId(id);
            return ResponseEntity.ok(players);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/clubs/{id}/players")
    public ResponseEntity<List<Player>> replacePlayersInClub(
            @PathVariable String id,
            @RequestBody List<Player> players) {
        
        try {
            List<Player> updatedPlayers = clubService.replacePlayersInClub(id, players);
            return ResponseEntity.ok(updatedPlayers);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already attached")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/clubs/{id}/players")
    public ResponseEntity<List<Player>> addPlayersToClub(
            @PathVariable String id,
            @RequestBody List<Player> players) {
        
        try {
            List<Player> updatedPlayers = clubService.addPlayersToClub(id, players);
            return ResponseEntity.ok(updatedPlayers);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already attached")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/clubs/statistics/{seasonYear}")
    public ResponseEntity<List<ClubStatistics>> getClubStatistics(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate seasonYear,
            @RequestParam(required = false, defaultValue = "false") boolean hasToBeClassified) {
        
        try {
            List<ClubStatistics> statistics = clubService.getClubStatistics(seasonYear, hasToBeClassified);
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}