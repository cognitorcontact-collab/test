package com.example.football.dao;

import com.example.football.model.Coach;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CoachDao {
    
    private final Connection connection;
    
    public Optional<Coach> findById(String id) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Coach WHERE PK_id = ?")) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCoach(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching coach by id: " + id, e);
        }
        return Optional.empty();
    }
    
    public Coach save(Coach coach) {
        if (coach.getId() == null || coach.getId().isEmpty()) {
            return insert(coach);
        } else {
            return update(coach);
        }
    }
    
    private Coach insert(Coach coach) {
        String query = "INSERT INTO Coach (name, nationality) VALUES (?, ?) RETURNING PK_id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, coach.getName());
            pstmt.setString(2, coach.getNationality());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    coach.setId(rs.getString("PK_id"));
                    return coach;
                } else {
                    throw new RuntimeException("Failed to insert coach, no ID returned");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting coach", e);
        }
    }
    
    private Coach update(Coach coach) {
        String query = "UPDATE Coach SET name = ?, nationality = ? WHERE PK_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, coach.getName());
            pstmt.setString(2, coach.getNationality());
            pstmt.setString(3, coach.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Coach update failed, no rows affected, id: " + coach.getId());
            }
            return coach;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating coach", e);
        }
    }
    
    private Coach mapRowToCoach(ResultSet rs) throws SQLException {
        Coach coach = new Coach();
        coach.setId(rs.getString("PK_id"));
        coach.setName(rs.getString("name"));
        coach.setNationality(rs.getString("nationality"));
        return coach;
    }
}