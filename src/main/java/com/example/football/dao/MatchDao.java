package com.example.football.dao;

import com.example.football.model.*;
import com.example.football.model.enums.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class MatchDao {
    
    private final Connection connection;
    private final ClubDao clubDao;
    private final GoalDao goalDao;
    
    public List<Match> findAll() {
        List<Match> matches = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Match ORDER BY match_datetime")) {
            
            while (rs.next()) {
                matches.add(mapRowToMatch(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all matches", e);
        }
        return matches;
    }
    
    public Optional<Match> findById(String id) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Match WHERE PK_id = ?")) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToMatch(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching match by id: " + id, e);
        }
        return Optional.empty();
    }
    
    public List<Match> findBySeasonId(String seasonId) {
        List<Match> matches = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM Match WHERE FK_season_id = ? ORDER BY match_datetime")) {
            pstmt.setString(1, seasonId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching matches by season id: " + seasonId, e);
        }
        return matches;
    }
    
    public List<Match> findBySeasonIdAndFilters(String seasonId, MatchStatus matchStatus,
                                               String clubPlayingName, LocalDate matchAfter,
                                               LocalDate matchBeforeOrEquals) {
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT * FROM Match m JOIN Club c1 ON m.FK_home_club_id = c1.PK_id " +
                        "JOIN Club c2 ON m.FK_away_club_id = c2.PK_id WHERE m.FK_season_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(seasonId);
        
        if (matchStatus != null) {
            queryBuilder.append(" AND m.status = ?::status_enum");
            params.add(matchStatus.name());
        }
        
        if (clubPlayingName != null && !clubPlayingName.isEmpty()) {
            queryBuilder.append(" AND (LOWER(c1.name) LIKE ? OR LOWER(c2.name) LIKE ?)");
            String searchPattern = "%" + clubPlayingName.toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (matchAfter != null) {
            queryBuilder.append(" AND m.match_datetime > ?");
            params.add(Timestamp.valueOf(matchAfter.atStartOfDay()));
        }
        
        if (matchBeforeOrEquals != null) {
            queryBuilder.append(" AND m.match_datetime <= ?");
            params.add(Timestamp.valueOf(matchBeforeOrEquals.plusDays(1).atStartOfDay()));
        }
        
        queryBuilder.append(" ORDER BY m.match_datetime");
        
        List<Match> matches = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(queryBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    matches.add(mapRowToMatch(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching matches with filters", e);
        }
        return matches;
    }
    
    public Match save(Match match) {
        if (match.getId() == null || match.getId().isEmpty()) {
            return insert(match);
        } else {
            return update(match);
        }
    }
    
    private Match insert(Match match) {
        String query = "INSERT INTO Match (match_datetime, stadium, status, FK_season_id, FK_home_club_id, FK_away_club_id) "
                + "VALUES (?, ?, ?::status_enum, ?, ?, ?) RETURNING PK_id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(match.getMatchDatetime()));
            pstmt.setString(2, match.getStadium());
            pstmt.setString(3, match.getActualStatus().name());
            pstmt.setString(4, match.getSeasonId());
            pstmt.setString(5, match.getClubPlayingHome().getId());
            pstmt.setString(6, match.getClubPlayingAway().getId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    match.setId(rs.getString("PK_id"));
                    return match;
                } else {
                    throw new RuntimeException("Failed to insert match, no ID returned");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting match", e);
        }
    }
    
    private Match update(Match match) {
        String query = "UPDATE Match SET match_datetime = ?, stadium = ?, status = ?::status_enum, "
                + "FK_season_id = ?, FK_home_club_id = ?, FK_away_club_id = ? WHERE PK_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(match.getMatchDatetime()));
            pstmt.setString(2, match.getStadium());
            pstmt.setString(3, match.getActualStatus().name());
            pstmt.setString(4, match.getSeasonId());
            pstmt.setString(5, match.getClubPlayingHome().getId());
            pstmt.setString(6, match.getClubPlayingAway().getId());
            pstmt.setString(7, match.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Match update failed, no rows affected, id: " + match.getId());
            }
            return match;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating match", e);
        }
    }
    
    public Match updateStatus(Match match, MatchStatus newStatus) {
        if (!isValidStatusTransition(match.getActualStatus(), newStatus)) {
            throw new IllegalArgumentException("Invalid status transition from " + match.getActualStatus() + " to " + newStatus);
        }
        
        String query = "UPDATE Match SET status = ?::status_enum WHERE PK_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newStatus.name());
            pstmt.setString(2, match.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Match status update failed, no rows affected, id: " + match.getId());
            }
            
            match.setActualStatus(newStatus);
            return match;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating match status", e);
        }
    }
    
    private boolean isValidStatusTransition(MatchStatus currentStatus, MatchStatus newStatus) {
        if (currentStatus == newStatus) {
            return true; // No change, always valid
        }
        
        // Check valid transitions
        return switch (currentStatus) {
            case NOT_STARTED -> newStatus == MatchStatus.STARTED;
            case STARTED -> newStatus == MatchStatus.FINISHED;
            case FINISHED -> false; // No valid transition from FINISHED
        };
    }
    
    public boolean existsMatchesBySeasonId(String seasonId) {
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT 1 FROM Match WHERE FK_season_id = ? LIMIT 1")) {
            pstmt.setString(1, seasonId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if matches exist for season: " + seasonId, e);
        }
    }
    
    public List<Match> createMatchesForSeason(String seasonId, List<Club> clubs) {
        List<Match> createdMatches = new ArrayList<>();
        
        // For each club, create a home and away match against all other clubs
        for (int i = 0; i < clubs.size(); i++) {
            Club homeClub = clubs.get(i);
            
            for (int j = 0; j < clubs.size(); j++) {
                if (i == j) continue; // Skip self
                
                Club awayClub = clubs.get(j);
                
                // Create a new match
                Match match = new Match();
                match.setMatchDatetime(LocalDateTime.now().plusDays((i * clubs.size() + j) % 60)); // Spread matches over time
                match.setStadium(homeClub.getStadium() != null ? homeClub.getStadium() : "Default Stadium");
                match.setActualStatus(MatchStatus.NOT_STARTED);
                match.setSeasonId(seasonId);
                
                // Set up clubs with empty scores initially
                MatchClub homeMatchClub = new MatchClub(
                        homeClub.getId(), homeClub.getName(), homeClub.getAcronym(), 0, new ArrayList<>());
                MatchClub awayMatchClub = new MatchClub(
                        awayClub.getId(), awayClub.getName(), awayClub.getAcronym(), 0, new ArrayList<>());
                
                match.setClubPlayingHome(homeMatchClub);
                match.setClubPlayingAway(awayMatchClub);
                
                // Save the match
                createdMatches.add(save(match));
            }
        }
        
        return createdMatches;
    }
    
    private Match mapRowToMatch(ResultSet rs) throws SQLException {
        String matchId = rs.getString("PK_id");
        
        Match match = new Match();
        match.setId(matchId);
        match.setMatchDatetime(rs.getTimestamp("match_datetime").toLocalDateTime());
        match.setStadium(rs.getString("stadium"));
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                match.setActualStatus(MatchStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                match.setActualStatus(null);
            }
        }
        
        match.setSeasonId(rs.getString("FK_season_id"));
        
        String homeClubId = rs.getString("FK_home_club_id");
        String awayClubId = rs.getString("FK_away_club_id");
        
        // Get the clubs
        clubDao.findById(homeClubId).ifPresent(homeClub -> {
            MatchClub homeMatchClub = new MatchClub(
                    homeClub.getId(), homeClub.getName(), homeClub.getAcronym(), 0, new ArrayList<>());
            match.setClubPlayingHome(homeMatchClub);
        });
        
        clubDao.findById(awayClubId).ifPresent(awayClub -> {
            MatchClub awayMatchClub = new MatchClub(
                    awayClub.getId(), awayClub.getName(), awayClub.getAcronym(), 0, new ArrayList<>());
            match.setClubPlayingAway(awayMatchClub);
        });
        
        // Get the goals
        List<Goal> goals = goalDao.findByMatchId(matchId);
        
        // Organize goals by club and create scorers
        Map<String, List<Scorer>> scorersByClub = new HashMap<>();
        Map<String, Integer> scoreByClub = new HashMap<>();
        
        for (Goal goal : goals) {
            // Initialize maps for club if needed
            scorersByClub.putIfAbsent(goal.getClubId(), new ArrayList<>());
            scoreByClub.putIfAbsent(goal.getClubId(), 0);
            
            // Increment score
            scoreByClub.put(goal.getClubId(), scoreByClub.get(goal.getClubId()) + 1);
            
            // Add scorer
            goalDao.getPlayerById(goal.getPlayerId()).ifPresent(player -> {
                Scorer scorer = new Scorer(player, goal.getMinuteOfGoal(), goal.getOwnGoal());
                scorersByClub.get(goal.getClubId()).add(scorer);
            });
        }
        
        // Update match clubs with scores and scorers
        if (match.getClubPlayingHome() != null && scoreByClub.containsKey(match.getClubPlayingHome().getId())) {
            match.getClubPlayingHome().setScore(scoreByClub.get(match.getClubPlayingHome().getId()));
            match.getClubPlayingHome().setScorers(scorersByClub.get(match.getClubPlayingHome().getId()));
        }
        
        if (match.getClubPlayingAway() != null && scoreByClub.containsKey(match.getClubPlayingAway().getId())) {
            match.getClubPlayingAway().setScore(scoreByClub.get(match.getClubPlayingAway().getId()));
            match.getClubPlayingAway().setScorers(scorersByClub.get(match.getClubPlayingAway().getId()));
        }
        
        return match;
    }
    
    public List<Match> saveAll(List<Match> matches) {
        List<Match> savedMatches = new ArrayList<>();
        for (Match match : matches) {
            savedMatches.add(save(match));
        }
        return savedMatches;
    }
}