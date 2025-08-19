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
public class ChampionshipDAO {
    
    private final DataSource dataSource;
    
    public ChampionshipDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void createOrUpdateChampionship(String id, String name, String apiUrl) {
        String sql = "INSERT INTO championship (championship_id, name, source_api_url) " +
                     "VALUES (?, ?, ?) " +
                     "ON CONFLICT (championship_id) " +
                     "DO UPDATE SET name = ?, source_api_url = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setString(3, apiUrl);
            stmt.setString(4, name);
            stmt.setString(5, apiUrl);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating or updating championship", e);
        }
    }
    
    public List<Map<String, Object>> getAllChampionships() {
        String sql = "SELECT championship_id, name, source_api_url FROM championship";
        List<Map<String, Object>> championships = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> championship = new HashMap<>();
                championship.put("championship_id", rs.getString("championship_id"));
                championship.put("name", rs.getString("name"));
                championship.put("source_api_url", rs.getString("source_api_url"));
                
                championships.add(championship);
            }
            
            return championships;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving championships", e);
        }
    }
    
    public Map<String, Object> getChampionshipById(String id) {
        String sql = "SELECT championship_id, name, source_api_url FROM championship WHERE championship_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> championship = new HashMap<>();
                    championship.put("championship_id", rs.getString("championship_id"));
                    championship.put("name", rs.getString("name"));
                    championship.put("source_api_url", rs.getString("source_api_url"));
                    
                    return championship;
                }
                
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving championship by ID", e);
        }
    }
}