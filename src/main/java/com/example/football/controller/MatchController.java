package com.example.football.controller;

import com.example.football.model.AddGoal;
import com.example.football.model.Match;
import com.example.football.model.UpdateMatchStatus;
import com.example.football.model.enums.MatchStatus;
import com.example.football.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MatchController {
    
    private final MatchService matchService;
    
    @GetMapping("/matches/{seasonYear}")
    public ResponseEntity<List<Match>> getMatchesBySeason(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate seasonYear,
            @RequestParam(required = false) MatchStatus matchStatus,
            @RequestParam(required = false) String clubPlayingName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate matchAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate matchBeforeOrEquals) {
        
        try {
            List<Match> matches = matchService.getMatchesBySeasonYearAndFilters(
                    seasonYear, matchStatus, clubPlayingName, matchAfter, matchBeforeOrEquals);
            return ResponseEntity.ok(matches);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/matches/{id}/status")
    public ResponseEntity<Match> updateMatchStatus(
            @PathVariable String id,
            @RequestBody UpdateMatchStatus updateStatus) {
        
        try {
            Match updatedMatch = matchService.updateMatchStatus(id, updateStatus.getStatus());
            return ResponseEntity.ok(updatedMatch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/matches/{id}/goals")
    public ResponseEntity<Match> addGoalsToMatch(
            @PathVariable String id,
            @RequestBody List<AddGoal> addGoals) {
        
        try {
            Match updatedMatch = matchService.addGoalsToMatch(id, addGoals);
            return ResponseEntity.ok(updatedMatch);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/matchMaker/{seasonYear}")
    public ResponseEntity<List<Match>> createMatchesForSeason(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate seasonYear) {
        
        try {
            List<Match> createdMatches = matchService.createMatchesForSeason(seasonYear);
            return ResponseEntity.ok(createdMatches);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Season not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
}