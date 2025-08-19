package com.example.football.controller;

import com.example.football.model.CreateSeason;
import com.example.football.model.Season;
import com.example.football.model.UpdateSeasonStatus;
import com.example.football.service.SeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SeasonController {
    
    private final SeasonService seasonService;
    
    @GetMapping("/seasons")
    public ResponseEntity<List<Season>> getSeasons() {
        List<Season> seasons = seasonService.getSeasons();
        return ResponseEntity.ok(seasons);
    }
    
    @PostMapping("/seasons")
    public ResponseEntity<List<Season>> createSeasons(@RequestBody List<CreateSeason> createSeasons) {
        List<Season> createdSeasons = seasonService.createSeasons(createSeasons);
        return ResponseEntity.ok(createdSeasons);
    }
    
    @PutMapping("/seasons/{seasonYear}/status")
    public ResponseEntity<Season> updateSeasonStatus(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate seasonYear,
            @RequestBody UpdateSeasonStatus updateStatus) {
        
        try {
            Season updatedSeason = seasonService.updateSeasonStatus(seasonYear, updateStatus.getStatus());
            return ResponseEntity.ok(updatedSeason);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}