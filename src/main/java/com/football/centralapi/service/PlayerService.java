package com.football.centralapi.service;

import com.football.centralapi.dao.PlayerDAO;
import com.football.centralapi.dto.PlayingTimeDTO;
import com.football.centralapi.dto.PlayerRankingDTO;
import com.football.centralapi.mapper.PlayerMapper;
import com.football.centralapi.model.enums.TimeUnit;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerService {
    
    private final PlayerDAO playerDAO;
    private final PlayerMapper playerMapper;
    
    public PlayerService(PlayerDAO playerDAO, PlayerMapper playerMapper) {
        this.playerDAO = playerDAO;
        this.playerMapper = playerMapper;
    }
    
    public List<PlayerRankingDTO> getBestPlayers(int top, TimeUnit playingTimeUnit) {
        List<Map<String, Object>> playerStats = playerDAO.getPlayerStatsAcrossAllSeasons();
        
        // Group stats by player to combine across seasons
        Map<String, PlayerRankingDTO> playerRankings = new HashMap<>();
        
        for (Map<String, Object> stat : playerStats) {
            String playerId = (String) stat.get("player_id");
            
            if (!playerRankings.containsKey(playerId)) {
                PlayerRankingDTO dto = playerMapper.mapToPlayerRankingDTO(stat);
                playerRankings.put(playerId, dto);
            } else {
                PlayerRankingDTO existing = playerRankings.get(playerId);
                
                // Add scored goals
                existing.setScoredGoals(existing.getScoredGoals() + (Integer) stat.get("seasonScoredGoals"));
                
                // Add playing time after converting to the same unit
                double convertedTime = convertPlayingTime(
                    (Double) stat.get("seasonPlayingTimeValue"),
                    TimeUnit.valueOf((String) stat.get("seasonPlayingTimeUnit")),
                    existing.getPlayingTime().getDurationUnit()
                );
                
                existing.getPlayingTime().setValue(
                    existing.getPlayingTime().getValue() + convertedTime
                );
            }
        }
        
        // Convert final times to requested unit
        for (PlayerRankingDTO player : playerRankings.values()) {
            double convertedTime = convertPlayingTime(
                player.getPlayingTime().getValue(),
                player.getPlayingTime().getDurationUnit(),
                playingTimeUnit
            );
            
            player.getPlayingTime().setValue(convertedTime);
            player.getPlayingTime().setDurationUnit(playingTimeUnit);
        }
        
        // Sort by total scored goals (desc) then by total playing time (desc)
        List<PlayerRankingDTO> sortedRankings = new ArrayList<>(playerRankings.values());
        sortedRankings.sort(Comparator
            .comparing(PlayerRankingDTO::getScoredGoals, Comparator.reverseOrder())
            .thenComparing(p -> p.getPlayingTime().getValue(), Comparator.reverseOrder()));
        
        // Assign ranks
        for (int i = 0; i < sortedRankings.size(); i++) {
            sortedRankings.get(i).setRank(i + 1);
        }
        
        // Limit to top N
        return sortedRankings.stream()
            .limit(top)
            .collect(Collectors.toList());
    }
    
    private double convertPlayingTime(double value, TimeUnit fromUnit, TimeUnit toUnit) {
        // First convert to seconds
        double inSeconds;
        switch (fromUnit) {
            case SECOND:
                inSeconds = value;
                break;
            case MINUTE:
                inSeconds = value * 60;
                break;
            case HOUR:
                inSeconds = value * 3600;
                break;
            default:
                throw new IllegalArgumentException("Unknown time unit: " + fromUnit);
        }
        
        // Then convert to target unit
        switch (toUnit) {
            case SECOND:
                return inSeconds;
            case MINUTE:
                return inSeconds / 60;
            case HOUR:
                return inSeconds / 3600;
            default:
                throw new IllegalArgumentException("Unknown time unit: " + toUnit);
        }
    }
}