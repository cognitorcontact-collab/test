package com.example.football.service;

import com.example.football.dao.ClubDao;
import com.example.football.dao.PlayerDao;
import com.example.football.dao.SeasonDao;
import com.example.football.model.Club;
import com.example.football.model.ClubPlayer;
import com.example.football.model.Player;
import com.example.football.model.PlayerStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {
    
    private final PlayerDao playerDao;
    private final ClubDao clubDao;
    private final SeasonDao seasonDao;
    
    public List<ClubPlayer> getPlayers(String name, Integer ageMinimum, Integer ageMaximum, String clubName) {
        List<Player> players = new ArrayList<>();
        
        // Apply filters step by step
        if (name != null && !name.isEmpty()) {
            players.addAll(playerDao.findByNameContaining(name));
        } else if (ageMinimum != null || ageMaximum != null) {
            players.addAll(playerDao.findByAgeRange(ageMinimum, ageMaximum));
        } else {
            players.addAll(playerDao.findAll());
        }
        
        // Filter by club name if provided
        List<Club> clubs = clubName != null && !clubName.isEmpty() 
                ? clubDao.findByNameContaining(clubName) 
                : clubDao.findAll();
        
        Map<String, Club> clubMap = clubs.stream()
                .collect(Collectors.toMap(Club::getId, Function.identity()));
        
        // Build ClubPlayer objects
        List<ClubPlayer> clubPlayers = new ArrayList<>();
        for (Player player : players) {
            if (player.getClubId() != null && clubMap.containsKey(player.getClubId())) {
                Club club = clubMap.get(player.getClubId());
                
                // Apply club name filter if needed
                if (clubName == null || clubName.isEmpty() || 
                        club.getName().toLowerCase().contains(clubName.toLowerCase())) {
                    ClubPlayer clubPlayer = new ClubPlayer();
                    // Copy player properties
                    clubPlayer.setId(player.getId());
                    clubPlayer.setName(player.getName());
                    clubPlayer.setNumber(player.getNumber());
                    clubPlayer.setPosition(player.getPosition());
                    clubPlayer.setNationality(player.getNationality());
                    clubPlayer.setAge(player.getAge());
                    clubPlayer.setClubId(player.getClubId());
                    // Set club
                    clubPlayer.setClub(club);
                    
                    clubPlayers.add(clubPlayer);
                }
            }
        }
        
        return clubPlayers;
    }
    
    public List<Player> createOrUpdatePlayers(List<Player> players) {
        return playerDao.saveAll(players);
    }
    
    public PlayerStatistics getStatisticsOfPlayerById(String playerId, LocalDate seasonYear) {
        // Find the season ID from the year
        String seasonId = seasonDao.findSeasonIdByDate(seasonYear);
        if (seasonId == null) {
            throw new IllegalArgumentException("Season not found for year: " + seasonYear);
        }
        
        // Get player stats for the season
        return playerDao.getPlayerStatistics(playerId, seasonId);
    }
}