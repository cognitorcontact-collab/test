package com.football.centralapi.dao;

import com.football.centralapi.config.DataSource;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PlayerDAO {
    
    private final DataSource dataSource;
    
    public PlayerDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void createOrUpdatePlayer(String id, String name, int number, String position, 
                                    String nationality, int age, String clubId) {
        String sql = "INSERT INTO player (player_id, name, number, position, nationality, age, club_id) " +
                     "VALUES (?, ?, ?, ?::player_position, ?, ?, ?) " +
                     "ON CONFLICT (player_id) " +
                     "DO UPDATE SET name = ?, number = ?, position = ?::player_position, " +
                     "nationality = ?, age = ?, club_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setInt(3, number);
            stmt.setString(4, position);
            stmt.setString(5, nationality);
            stmt.setInt(6, age);
            stmt.setString(7, clubId);
            stmt.setString(8, name);
            stmt.setInt(9, number);
            stmt.setString(10, position);
            stmt.setString(11, nationality);
            stmt.setInt(12, age);
            stmt.setString(13, clubId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating or updating player", e);
        }
    }
    
    public void updatePlayerStats(String playerId, String seasonId, int scoredGoals, 
                                 double playingTimeValue, String playingTimeUnit) {
        String sql = "INSERT INTO player_stats (player_id, season_id, seasonScoredGoals, " +
                     "seasonPlayingTimeValue, seasonPlayingTimeUnit) " +
                     "VALUES (?, ?, ?, ?, ?::time_unit) " +
                     "ON CONFLICT (player_id, season_id) " +
                     "DO UPDATE SET seasonScoredGoals = ?, seasonPlayingTimeValue = ?, " +
                     "seasonPlayingTimeUnit = ?::time_unit";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, playerId);
            stmt.setString(2, seasonId);
            stmt.setInt(3, scoredGoals);
            stmt.setDouble(4, playingTimeValue);
            stmt.setString(5, playingTimeUnit);
            stmt.setInt(6, scoredGoals);
            stmt.setDouble(7, playingTimeValue);
            stmt.setString(8, playingTimeUnit);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating player stats", e);
        }
    }
    
    public List<Map<String, Object>> getAllPlayers() {
        String sql = "SELECT player_id, name, number, position, nationality, age, club_id FROM player";
        List<Map<String, Object>> players = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> player = new HashMap<>();
                player.put("player_id", rs.getString("player_id"));
                player.put("name", rs.getString("name"));
                player.put("number", rs.getInt("number"));
                player.put("position", rs.getString("position"));
                player.put("nationality", rs.getString("nationality"));
                player.put("age", rs.getInt("age"));
                player.put("club_id", rs.getString("club_id"));
                
                players.add(player);
            }
            
            return players;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving players", e);
        }
    }
    
    public List<Map<String, Object>> getPlayerStatsAcrossAllSeasons() {
        String sql = "SELECT p.player_id, p.name, p.number, p.position, p.nationality, p.age, " +
                     "c.championship_id, ps.seasonScoredGoals, ps.seasonPlayingTimeValue, " +
                     "ps.seasonPlayingTimeUnit " +
                     "FROM player p " +
                     "JOIN club c ON p.club_id = c.club_id " +
                     "JOIN player_stats ps ON p.player_id = ps.player_id";
        
        List<Map<String, Object>> playerStats = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("player_id", rs.getString("player_id"));
                stat.put("name", rs.getString("name"));
                stat.put("number", rs.getInt("number"));
                stat.put("position", rs.getString("position"));
                stat.put("nationality", rs.getString("nationality"));
                stat.put("age", rs.getInt("age"));
                stat.put("championship_id", rs.getString("championship_id"));
                stat.put("seasonScoredGoals", rs.getInt("seasonScoredGoals"));
                stat.put("seasonPlayingTimeValue", rs.getDouble("seasonPlayingTimeValue"));
                stat.put("seasonPlayingTimeUnit", rs.getString("seasonPlayingTimeUnit"));
                
                playerStats.add(stat);
            }
            
            return playerStats;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving player stats", e);
        }
    }
}