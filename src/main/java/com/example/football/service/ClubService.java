package com.example.football.service;

import com.example.football.dao.ClubDao;
import com.example.football.dao.PlayerDao;
import com.example.football.dao.SeasonDao;
import com.example.football.model.Club;
import com.example.football.model.ClubStatistics;
import com.example.football.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {
    
    private final ClubDao clubDao;
    private final PlayerDao playerDao;
    private final SeasonDao seasonDao;
    
    public List<Club> getClubs() {
        return clubDao.findAll();
    }
    
    public List<Club> createOrUpdateClubs(List<Club> clubs) {
        return clubDao.saveAll(clubs);
    }
    
    public List<Player> getPlayersByClubId(String clubId) {
        // Check if the club exists
        Club club = clubDao.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found with id: " + clubId));
        
        return playerDao.findByClubId(clubId);
    }
    
    public List<Player> replacePlayersInClub(String clubId, List<Player> players) {
        // Check if the club exists
        Club club = clubDao.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found with id: " + clubId));
        
        // Check if any player is already attached to another club
        for (Player player : players) {
            if (player.getId() != null && !player.getId().isEmpty()) {
                playerDao.findById(player.getId()).ifPresent(existingPlayer -> {
                    if (existingPlayer.getClubId() != null && !existingPlayer.getClubId().isEmpty() 
                            && !existingPlayer.getClubId().equals(clubId)) {
                        throw new IllegalArgumentException("Player " + existingPlayer.getName() + 
                                " (ID: " + existingPlayer.getId() + ") is already attached to another club");
                    }
                });
            }
        }
        
        // First, detach all current players from the club
        playerDao.detachPlayersFromClub(clubId);
        
        // Then, save all new players and attach them to the club
        for (Player player : players) {
            player.setClubId(clubId);
            playerDao.save(player);
        }
        
        // Return updated list of players in the club
        return playerDao.findByClubId(clubId);
    }
    
    public List<Player> addPlayersToClub(String clubId, List<Player> players) {
        // Check if the club exists
        Club club = clubDao.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found with id: " + clubId));
        
        // Check if any player is already attached to another club
        for (Player player : players) {
            if (player.getId() != null && !player.getId().isEmpty()) {
                playerDao.findById(player.getId()).ifPresent(existingPlayer -> {
                    if (existingPlayer.getClubId() != null && !existingPlayer.getClubId().isEmpty() 
                            && !existingPlayer.getClubId().equals(clubId)) {
                        throw new IllegalArgumentException("Player " + existingPlayer.getName() + 
                                " (ID: " + existingPlayer.getId() + ") is already attached to another club");
                    }
                });
            }
        }
        
        // Save all new players and attach them to the club
        for (Player player : players) {
            player.setClubId(clubId);
            playerDao.save(player);
        }
        
        // Return updated list of players in the club
        return playerDao.findByClubId(clubId);
    }
    
    public List<ClubStatistics> getClubStatistics(LocalDate seasonYear, boolean hasToBeClassified) {
        // Find the season ID from the year
        String seasonId = seasonDao.findSeasonIdByDate(seasonYear);
        if (seasonId == null) {
            throw new IllegalArgumentException("Season not found for year: " + seasonYear);
        }
        
        return clubDao.getClubStatistics(seasonId, hasToBeClassified);
    }
}