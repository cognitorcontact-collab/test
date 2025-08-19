package com.example.football.dao;

import com.example.football.model.CreateSeason;
import com.example.football.model.Season;
import com.example.football.model.enums.SeasonStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SeasonDao {
    
    private final Connection connection;
    
    public List<Season> findAll() {
        List<Season> seasons = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Season ORDER BY year DESC")) {
            
            while (rs.next()) {
                seasons.add(mapRowToSeason(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all seasons", e);
        }
        return seasons;
    }
    
    public Optional<Season> findById(String id) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Season WHERE PK_id = ?")) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSeason(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching season by id: " + id, e);
        }
        return Optional.empty();
    }
    
    public Optional<Season> findByYear(int year) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Season WHERE year = ?")) {
            pstmt.setInt(1, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSeason(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching season by year: " + year, e);
        }
        return Optional.empty();
    }
    
    public Season save(CreateSeason createSeason) {
        Season season = new Season();
        season.setYear(createSeason.getYear());
        season.setAlias(createSeason.getAlias());
        season.setStatus(SeasonStatus.NOT_STARTED);
        
        String query = "INSERT INTO Season (year, alias, status) VALUES (?, ?, ?::status_enum) RETURNING PK_id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, season.getYear());
            pstmt.setString(2, season.getAlias());
            pstmt.setString(3, season.getStatus().name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    season.setId(rs.getString("PK_id"));
                    return season;
                } else {
                    throw new RuntimeException("Failed to insert season, no ID returned");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting season", e);
        }
    }
    
    public Season updateStatus(Season season, SeasonStatus newStatus) {
        if (!isValidStatusTransition(season.getStatus(), newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + season.getStatus() + " to " + newStatus);
        }
        
        String query = "UPDATE Season SET status = ?::status_enum WHERE PK_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newStatus.name());
            pstmt.setString(2, season.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Season status update failed, no rows affected, id: " + season.getId());
            }
            
            season.setStatus(newStatus);
            return season;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating season status", e);
        }
    }
    
    private boolean isValidStatusTransition(SeasonStatus currentStatus, SeasonStatus newStatus) {
        if (currentStatus == newStatus) {
            return true; // No change, always valid
        }
        
        // Check valid transitions
        return switch (currentStatus) {
            case NOT_STARTED -> newStatus == SeasonStatus.STARTED;
            case STARTED -> newStatus == SeasonStatus.FINISHED;
            case FINISHED -> false; // No valid transition from FINISHED
        };
    }
    
    public String findSeasonIdByDate(LocalDate date) {
        // Find the season by the year of the given date
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT PK_id FROM Season WHERE year = ?")) {
            pstmt.setInt(1, date.getYear());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("PK_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding season by date: " + date, e);
        }
        return null;
    }
    
    private Season mapRowToSeason(ResultSet rs) throws SQLException {
        Season season = new Season();
        season.setId(rs.getString("PK_id"));
        season.setYear(rs.getInt("year"));
        season.setAlias(rs.getString("alias"));
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                season.setStatus(SeasonStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                // Handle case when enum value might not match
                season.setStatus(null);
            }
        }
        
        return season;
    }
    
    public List<Season> saveAll(List<CreateSeason> createSeasons) {
        List<Season> savedSeasons = new ArrayList<>();
        for (CreateSeason createSeason : createSeasons) {
            savedSeasons.add(save(createSeason));
        }
        return savedSeasons;
    }
}