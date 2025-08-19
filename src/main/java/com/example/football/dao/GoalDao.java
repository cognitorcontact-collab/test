package com.example.football.dao;

import com.example.football.model.AddGoal;
import com.example.football.model.Goal;
import com.example.football.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GoalDao {
    
    private final Connection connection;
    
    public List<Goal> findByMatchId(String matchId) {
        List<Goal> goals = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM Goal WHERE FK_match_id = ? ORDER BY minute_of_goal")) {
            pstmt.setString(1, matchId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    goals.add(mapRowToGoal(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching goals by match id: " + matchId, e);
        }
        return goals;
    }
    
    public Goal save(Goal goal) {
        String query = "INSERT INTO Goal (minute_of_goal, own_goal, FK_match_id, FK_player_id, FK_club_id) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING PK_id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, goal.getMinuteOfGoal());
            pstmt.setBoolean(2, goal.getOwnGoal());
            pstmt.setString(3, goal.getMatchId());
            pstmt.setString(4, goal.getPlayerId());
            pstmt.setString(5, goal.getClubId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    goal.setId(rs.getString("PK_id"));
                    return goal;
                } else {
                    throw new RuntimeException("Failed to insert goal, no ID returned");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting goal", e);
        }
    }
    
    public Goal addGoalToMatch(String matchId, AddGoal addGoal) {
        // Validate that minuteOfGoal is between 1 and 90
        if (addGoal.getMinuteOfGoal() < 1 || addGoal.getMinuteOfGoal() > 90) {
            throw new IllegalArgumentException("Minute of goal must be between 1 and 90");
        }
        
        // Check if the match exists and both clubs are playing in this match
        String clubIdFromMatch = isClubPlayingInMatch(matchId, addGoal.getClubId());
        if (clubIdFromMatch == null) {
            throw new IllegalArgumentException("Club is not playing in this match");
        }
        
        // Get player's club
        String playerClubId = getPlayerClubId(addGoal.getScorerId());
        
        // Determine if it's an own goal
        boolean isOwnGoal = playerClubId != null && !playerClubId.equals(addGoal.getClubId());
        
        // Create and save the goal
        Goal goal = new Goal();
        goal.setMinuteOfGoal(addGoal.getMinuteOfGoal());
        goal.setOwnGoal(isOwnGoal);
        goal.setMatchId(matchId);
        goal.setPlayerId(addGoal.getScorerId());
        goal.setClubId(addGoal.getClubId());
        
        return save(goal);
    }
    
    private String isClubPlayingInMatch(String matchId, String clubId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT FK_home_club_id, FK_away_club_id FROM Match WHERE PK_id = ?")) {
            pstmt.setString(1, matchId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String homeClubId = rs.getString("FK_home_club_id");
                    String awayClubId = rs.getString("FK_away_club_id");
                    
                    if (clubId.equals(homeClubId) || clubId.equals(awayClubId)) {
                        return clubId;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if club is playing in match", e);
        }
        return null;
    }
    
    private String getPlayerClubId(String playerId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT FK_club_id FROM Player WHERE PK_id = ?")) {
            pstmt.setString(1, playerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("FK_club_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting player's club", e);
        }
        return null;
    }
    
    public Optional<Player> getPlayerById(String playerId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM Player WHERE PK_id = ?")) {
            pstmt.setString(1, playerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Player player = new Player();
                    player.setId(rs.getString("PK_id"));
                    player.setName(rs.getString("name"));
                    player.setNumber(rs.getObject("number", Integer.class));
                    return Optional.of(player);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting player by id", e);
        }
        return Optional.empty();
    }
    
    private Goal mapRowToGoal(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setId(rs.getString("PK_id"));
        goal.setMinuteOfGoal(rs.getInt("minute_of_goal"));
        goal.setOwnGoal(rs.getBoolean("own_goal"));
        goal.setMatchId(rs.getString("FK_match_id"));
        goal.setPlayerId(rs.getString("FK_player_id"));
        goal.setClubId(rs.getString("FK_club_id"));
        return goal;
    }
    
    public List<Goal> saveAll(List<AddGoal> addGoals, String matchId) {
        List<Goal> savedGoals = new ArrayList<>();
        for (AddGoal addGoal : addGoals) {
            savedGoals.add(addGoalToMatch(matchId, addGoal));
        }
        return savedGoals;
    }
}