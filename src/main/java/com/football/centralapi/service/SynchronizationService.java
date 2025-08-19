package com.football.centralapi.service;

import com.football.centralapi.dao.ChampionshipDAO;
import com.football.centralapi.dao.ClubDAO;
import com.football.centralapi.dao.PlayerDAO;
import com.football.centralapi.dao.SeasonDAO;
import com.football.centralapi.model.enums.Championship;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SynchronizationService {
    
    private final ChampionshipDAO championshipDAO;
    private final ClubDAO clubDAO;
    private final PlayerDAO playerDAO;
    private final SeasonDAO seasonDAO;
    private final RestTemplate restTemplate;
    
    private final Map<Championship, String> apiUrls = new HashMap<>();
    
    public SynchronizationService(ChampionshipDAO championshipDAO, ClubDAO clubDAO, 
                                 PlayerDAO playerDAO, SeasonDAO seasonDAO) {
        this.championshipDAO = championshipDAO;
        this.clubDAO = clubDAO;
        this.playerDAO = playerDAO;
        this.seasonDAO = seasonDAO;
        this.restTemplate = new RestTemplate();
        
        // Initialize API URLs
        apiUrls.put(Championship.PREMIER_LEAGUE, "http://localhost:8080");
        apiUrls.put(Championship.LA_LIGA, "http://localhost:8081");
        apiUrls.put(Championship.BUNDESLIGA, "http://localhost:8082");
        apiUrls.put(Championship.SERIA, "http://localhost:8083");
        apiUrls.put(Championship.LIGUE_1, "http://localhost:8084");
    }
    
    public void synchronizeAllData() {
        // Ensure championships are in the database
        initializeChampionships();
        
        // Synchronize data from each championship API
        for (Map.Entry<Championship, String> entry : apiUrls.entrySet()) {
            Championship championship = entry.getKey();
            String baseUrl = entry.getValue();
            
            synchronizeSeasons(baseUrl);
            synchronizeClubs(baseUrl, championship);
            synchronizePlayers(baseUrl);
            synchronizeClubStatistics(baseUrl);
            synchronizePlayerStatistics(baseUrl);
        }
    }
    
    private void initializeChampionships() {
        for (Map.Entry<Championship, String> entry : apiUrls.entrySet()) {
            championshipDAO.createOrUpdateChampionship(entry.getKey().name(), entry.getKey().name(), entry.getValue());
        }
    }
    
    private void synchronizeSeasons(String baseUrl) {
        String url = baseUrl + "/seasons";
        Map<String, Object>[] seasons = restTemplate.getForObject(url, Map[].class);
        
        if (seasons != null) {
            for (Map<String, Object> season : seasons) {
                String id = (String) season.get("id");
                Integer year = (Integer) season.get("year");
                String alias = (String) season.get("alias");
                
                seasonDAO.createOrUpdateSeason(id, year, alias);
            }
        }
    }
    
    private void synchronizeClubs(String baseUrl, Championship championship) {
        String url = baseUrl + "/clubs";
        Map<String, Object>[] clubs = restTemplate.getForObject(url, Map[].class);
        
        if (clubs != null) {
            for (Map<String, Object> club : clubs) {
                String id = (String) club.get("id");
                String name = (String) club.get("name");
                String acronym = (String) club.get("acronym");
                Integer yearCreation = (Integer) club.get("yearCreation");
                String stadium = (String) club.get("stadium");
                
                Map<String, String> coach = (Map<String, String>) club.get("coach");
                String coachName = coach.get("name");
                String coachNationality = coach.get("nationality");
                
                clubDAO.createOrUpdateClub(id, name, acronym, yearCreation, stadium, 
                                          coachName, coachNationality, championship.name());
            }
        }
    }
    
    private void synchronizePlayers(String baseUrl) {
        String url = baseUrl + "/players";
        Map<String, Object>[] players = restTemplate.getForObject(url, Map[].class);
        
        if (players != null) {
            for (Map<String, Object> playerData : players) {
                Map<String, Object> player = (Map<String, Object>) playerData.get("player");
                Map<String, Object> club = (Map<String, Object>) playerData.get("club");
                
                String id = (String) player.get("id");
                String name = (String) player.get("name");
                Integer number = (Integer) player.get("number");
                String position = (String) player.get("position");
                String nationality = (String) player.get("nationality");
                Integer age = (Integer) player.get("age");
                String clubId = (String) club.get("id");
                
                playerDAO.createOrUpdatePlayer(id, name, number, position, nationality, age, clubId);
            }
        }
    }
    
    private void synchronizeClubStatistics(String baseUrl) {
        // Get the latest season
        List<Map<String, Object>> seasons = seasonDAO.getAllSeasons();
        if (seasons.isEmpty()) return;
        
        // Assume the highest year is the latest season
        Integer latestYear = 0;
        String latestSeasonId = "";
        
        for (Map<String, Object> season : seasons) {
            Integer year = (Integer) season.get("year");
            if (year > latestYear) {
                latestYear = year;
                latestSeasonId = (String) season.get("id");
            }
        }
        
        String url = baseUrl + "/clubs/statistics/" + latestYear;
        Map<String, Object>[] clubStats = restTemplate.getForObject(url, Map[].class);
        
        if (clubStats != null) {
            for (Map<String, Object> stat : clubStats) {
                String clubId = (String) stat.get("id");
                Integer rankingPoints = (Integer) stat.get("rankingPoints");
                Integer scoredGoals = (Integer) stat.get("scoredGoals");
                Integer concededGoals = (Integer) stat.get("concededGoals");
                Integer differenceGoals = (Integer) stat.get("differenceGoals");
                Integer cleanSheetNumber = (Integer) stat.get("cleanSheetNumber");
                
                clubDAO.updateClubStats(clubId, latestSeasonId, rankingPoints, 
                                       scoredGoals, concededGoals, differenceGoals, cleanSheetNumber);
            }
        }
    }
    
    private void synchronizePlayerStatistics(String baseUrl) {
        // Get all players
        List<Map<String, Object>> players = playerDAO.getAllPlayers();
        
        // Get the latest season
        List<Map<String, Object>> seasons = seasonDAO.getAllSeasons();
        if (seasons.isEmpty() || players.isEmpty()) return;
        
        // Assume the highest year is the latest season
        Integer latestYear = 0;
        String latestSeasonId = "";
        
        for (Map<String, Object> season : seasons) {
            Integer year = (Integer) season.get("year");
            if (year > latestYear) {
                latestYear = year;
                latestSeasonId = (String) season.get("id");
            }
        }
        
        for (Map<String, Object> player : players) {
            String playerId = (String) player.get("player_id");
            
            String url = baseUrl + "/players/" + playerId + "/statistics/" + latestYear;
            Map<String, Object> stats;
            
            try {
                stats = restTemplate.getForObject(url, Map.class);
            } catch (Exception e) {
                // Skip if player stats not found
                continue;
            }
            
            if (stats != null) {
                Integer scoredGoals = (Integer) stats.get("scoredGoals");
                Map<String, Object> playingTime = (Map<String, Object>) stats.get("playingTime");
                Double value = Double.valueOf(playingTime.get("value").toString());
                String timeUnit = (String) playingTime.get("durationUnit");
                
                playerDAO.updatePlayerStats(playerId, latestSeasonId, scoredGoals, value, timeUnit);
            }
        }
    }
}