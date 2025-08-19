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
public class SeasonDAO {
    
    private final DataSource dataSource;
    
    public SeasonDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void createOrUpdateSeason(String id, int year, String alias) {
        String sql = "INSERT INTO season (season_id, year, alias) " +
                     "VALUES (?, ?, ?) " +
                     "ON CONFLICT (season_id) " +
                     "DO UPDATE SET year = ?, alias = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            stmt.setInt(2, year);
            stmt.setString(3, alias);
            stmt.setInt(4, year);
            stmt.setString(5, alias);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating or updating season", e);
        }
    }
    
    public List<Map<String, Object>> getAllSeasons() {
        String sql = "SELECT season_id, year, alias FROM season";
        List<Map<String, Object>> seasons = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> season = new HashMap<>();
                season.put("id", rs.getString("season_id"));
                season.put("year", rs.getInt("year"));
                season.put("alias", rs.getString("alias"));
                
                seasons.add(season);
            }
            
            return seasons;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving seasons", e);
        }
    }
    
    public Map<String, Object> getSeasonById(String id) {
        String sql = "SELECT season_id, year, alias FROM season WHERE season_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> season = new HashMap<>();
                    season.put("id", rs.getString("season_id"));
                    season.put("year", rs.getInt("year"));
                    season.put("alias", rs.getString("alias"));
                    
                    return season;
                }
                
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving season by ID", e);
        }
    }
    
    public String getLatestSeasonId() {
        String sql = "SELECT season_id FROM season ORDER BY year DESC LIMIT 1";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getString("season_id");
            }
            
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving latest season", e);
        }
    }
}