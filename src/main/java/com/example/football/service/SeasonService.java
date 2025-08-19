package com.example.football.service;

import com.example.football.dao.SeasonDao;
import com.example.football.model.CreateSeason;
import com.example.football.model.Season;
import com.example.football.model.enums.SeasonStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonService {
    
    private final SeasonDao seasonDao;
    
    public List<Season> getSeasons() {
        return seasonDao.findAll();
    }
    
    public List<Season> createSeasons(List<CreateSeason> createSeasons) {
        return seasonDao.saveAll(createSeasons);
    }
    
    public Season updateSeasonStatus(LocalDate seasonYear, SeasonStatus newStatus) {
        // Find the season by year
        int year = seasonYear.getYear();
        Season season = seasonDao.findByYear(year)
                .orElseThrow(() -> new IllegalArgumentException("Season not found for year: " + year));
        
        // Update the status
        return seasonDao.updateStatus(season, newStatus);
    }
}