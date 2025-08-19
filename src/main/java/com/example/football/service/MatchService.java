package com.example.football.service;

import com.example.football.dao.ClubDao;
import com.example.football.dao.GoalDao;
import com.example.football.dao.MatchDao;
import com.example.football.dao.SeasonDao;
import com.example.football.model.AddGoal;
import com.example.football.model.Club;
import com.example.football.model.Match;
import com.example.football.model.enums.MatchStatus;
import com.example.football.model.enums.SeasonStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {
    
    private final MatchDao matchDao;
    private final GoalDao goalDao;
    private final ClubDao clubDao;
    private final SeasonDao seasonDao;
    
    public List<Match> getMatchesBySeasonYearAndFilters(LocalDate seasonYear, MatchStatus matchStatus,
                                                      String clubPlayingName, LocalDate matchAfter,
                                                      LocalDate matchBeforeOrEquals) {
        // Find the season ID from the year
        String seasonId = seasonDao.findSeasonIdByDate(seasonYear);
        if (seasonId == null) {
            throw new IllegalArgumentException("Season not found for year: " + seasonYear);
        }
        
        return matchDao.findBySeasonIdAndFilters(seasonId, matchStatus, clubPlayingName, matchAfter, matchBeforeOrEquals);
    }
    
    public Match updateMatchStatus(String matchId, MatchStatus newStatus) {
        // Find the match
        Match match = matchDao.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));
        
        // Update the status
        return matchDao.updateStatus(match, newStatus);
    }
    
    public Match addGoalsToMatch(String matchId, List<AddGoal> addGoals) {
        // Find the match
        Match match = matchDao.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));
        
        // Check that match status is STARTED
        if (match.getActualStatus() != MatchStatus.STARTED) {
            throw new IllegalArgumentException("Cannot add goals to a match that is not in STARTED status");
        }
        
        // Add goals
        goalDao.saveAll(addGoals, matchId);
        
        // Return updated match
        return matchDao.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found with id: " + matchId));
    }
    
    public List<Match> createMatchesForSeason(LocalDate seasonYear) {
        // Find the season by year
        int year = seasonYear.getYear();
        String seasonId = seasonDao.findByYear(year)
                .orElseThrow(() -> new IllegalArgumentException("Season not found for year: " + year))
                .getId();
        
        // Check if season status is STARTED
        if (seasonDao.findById(seasonId).orElseThrow().getStatus() != SeasonStatus.STARTED) {
            throw new IllegalArgumentException("Season must be in STARTED status to create matches");
        }
        
        // Check if matches already exist for this season
        if (matchDao.existsMatchesBySeasonId(seasonId)) {
            throw new IllegalArgumentException("Matches already exist for this season");
        }
        
        // Get all clubs
        List<Club> clubs = clubDao.findAll();
        if (clubs.isEmpty()) {
            throw new IllegalArgumentException("No clubs found to create matches");
        }
        
        // Create matches
        return matchDao.createMatchesForSeason(seasonId, clubs);
    }
}