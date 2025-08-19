package com.example.football.dao;

import com.example.football.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClubDao {
    
    private final Connection connection;
    private final CoachDao coachDao;
    
    public List<Club> findAll() {
        List<Club> clubs = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Club")) {
            
            while (rs.next()) {
                clubs.add(mapRowToClub(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all clubs", e);
        }
        return clubs;
    }
    
    public Optional<Club> findById(String id) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Club WHERE PK_id = ?")) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToClub(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching club by id: " + id, e);
        }
        return Optional.empty();
    }
    
    public List<Club> findByNameContaining(String name) {
        List<Club> clubs = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM Club WHERE LOWER(name) LIKE ?")) {
            pstmt.setString(1, "%" + name.toLowerCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    clubs.add(mapRowToClub(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching clubs by name: " + name, e);
        }
        return clubs;
    }
    
    public Club save(Club club) {
        if (club.getId() == null || club.getId().isEmpty()) {
            return insert(club);
        } else {
            return update(club);
        }
    }
    
    private Club insert(Club club) {
        // First save the coach if it doesn't have an ID
        if (club.getCoach() != null) {
            if (club.getCoach().getId() == null || club.getCoach().getId().isEmpty()) {
                club.setCoach(coachDao.save(club.getCoach()));
            }
        }
        
        String query = "INSERT INTO Club (name, acronym, year_creation, stadium, FK_coach_id) "
                + "VALUES (?, ?, ?, ?, ?) RETURNING PK_id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, club.getName());
            pstmt.setString(2, club.getAcronym());
            
            if (club.getYearCreation() != null) {
                pstmt.setInt(3, club.getYearCreation());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, club.getStadium());
            
            if (club.getCoach() != null && club.getCoach().getId() != null) {
                pstmt.setString(5, club.getCoach().getId());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    club.setId(rs.getString("PK_id"));
                    return club;
                } else {
                    throw new RuntimeException("Failed to insert club, no ID returned");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting club", e);
        }
    }
    
    private Club update(Club club) {
        // First save the coach if it doesn't have an ID
        if (club.getCoach() != null) {
            if (club.getCoach().getId() == null || club.getCoach().getId().isEmpty()) {
                club.setCoach(coachDao.save(club.getCoach()));
            }
        }
        
        String query = "UPDATE Club SET name = ?, acronym = ?, year_creation = ?, stadium = ?, "
                + "FK_coach_id = ? WHERE PK_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, club.getName());
            pstmt.setString(2, club.getAcronym());
            
            if (club.getYearCreation() != null) {
                pstmt.setInt(3, club.getYearCreation());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            
            pstmt.setString(4, club.getStadium());
            
            if (club.getCoach() != null && club.getCoach().getId() != null) {
                pstmt.setString(5, club.getCoach().getId());
            } else {
                pstmt.setNull(5, Types.VARCHAR);
            }
            
            pstmt.setString(6, club.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Club update failed, no rows affected, id: " + club.getId());
            }
            return club;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating club", e);
        }
    }
    
    public List<ClubStatistics> getClubStatistics(String seasonId, boolean hasToBeClassified) {
        List<ClubStatistics> clubStatistics = new ArrayList<>();
        
        // Base query to get club stats
        String query = "SELECT c.*, " +
                "(SELECT COUNT(*) FROM Goal g JOIN Match m ON g.FK_match_id = m.PK_id " +
                "WHERE g.FK_club_id = c.PK_id AND m.FK_season_id = ? AND g.own_goal = false) AS scored_goals, " +
                "(SELECT COUNT(*) FROM Goal g JOIN Match m ON g.FK_match_id = m.PK_id " +
                "WHERE ((m.FK_home_club_id = c.PK_id AND g.FK_club_id != c.PK_id) OR " +
                "(m.FK_away_club_id = c.PK_id AND g.FK_club_id != c.PK_id)) " +
                "AND m.FK_season_id = ?) AS conceded_goals, " +
                // Count clean sheets - when club played and conceded no goals
                "(SELECT COUNT(*) FROM Match m " +
                "LEFT JOIN Goal g ON m.PK_id = g.FK_match_id AND " +
                "((m.FK_home_club_id = c.PK_id AND g.FK_club_id != c.PK_id) OR " +
                "(m.FK_away_club_id = c.PK_id AND g.FK_club_id != c.PK_id)) " +
                "WHERE m.FK_season_id = ? AND m.status = 'FINISHED'::status_enum " +
                "GROUP BY m.PK_id " +
                "HAVING COUNT(g.PK_id) = 0) AS clean_sheets, " +
                // Calculate ranking points
                "(SELECT SUM(CASE " +
                "  WHEN (m.FK_home_club_id = c.PK_id AND home_score > away_score) OR " +
                "       (m.FK_away_club_id = c.PK_id AND away_score > home_score) THEN 3 " +
                "  WHEN home_score = away_score THEN 1 " +
                "  ELSE 0 " +
                "END) FROM " +
                "  (SELECT m.PK_id, m.FK_home_club_id, m.FK_away_club_id, " +
                "    COUNT(CASE WHEN g.FK_club_id = m.FK_home_club_id THEN 1 END) AS home_score, " +
                "    COUNT(CASE WHEN g.FK_club_id = m.FK_away_club_id THEN 1 END) AS away_score " +
                "   FROM Match m " +
                "   LEFT JOIN Goal g ON m.PK_id = g.FK_match_id " +
                "   WHERE m.FK_season_id = ? AND m.status = 'FINISHED'::status_enum " +
                "   GROUP BY m.PK_id) AS match_scores " +
                "WHERE match_scores.FK_home_club_id = c.PK_id OR match_scores.FK_away_club_id = c.PK_id) AS ranking_points " +
                "FROM Club c";
        
        // Add ordering based on classification flag
        if (hasToBeClassified) {
            query += " ORDER BY ranking_points DESC NULLS LAST, " +
                    "(scored_goals - conceded_goals) DESC, " +
                    "clean_sheets DESC, " +
                    "c.name ASC";
        } else {
            query += " ORDER BY c.name ASC";
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, seasonId);
            pstmt.setString(2, seasonId);
            pstmt.setString(3, seasonId);
            pstmt.setString(4, seasonId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Get the club details
                    Club club = mapRowToClub(rs);
                    
                    // Get the statistics
                    Integer scoredGoals = rs.getObject("scored_goals", Integer.class);
                    if (scoredGoals == null) scoredGoals = 0;
                    
                    Integer concededGoals = rs.getObject("conceded_goals", Integer.class);
                    if (concededGoals == null) concededGoals = 0;
                    
                    Integer cleanSheets = rs.getObject("clean_sheets", Integer.class);
                    if (cleanSheets == null) cleanSheets = 0;
                    
                    Integer rankingPoints = rs.getObject("ranking_points", Integer.class);
                    if (rankingPoints == null) rankingPoints = 0;
                    
                    Integer differenceGoals = scoredGoals - concededGoals;
                    
                    ClubStatistics stats = new ClubStatistics(
                            club, rankingPoints, scoredGoals, concededGoals, differenceGoals, cleanSheets);
                    
                    clubStatistics.add(stats);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting club statistics", e);
        }
        
        return clubStatistics;
    }
    
    private Club mapRowToClub(ResultSet rs) throws SQLException {
        Club club = new Club();
        club.setId(rs.getString("PK_id"));
        club.setName(rs.getString("name"));
        club.setAcronym(rs.getString("acronym"));
        club.setYearCreation(rs.getObject("year_creation", Integer.class));
        club.setStadium(rs.getString("stadium"));
        
        String coachId = rs.getString("FK_coach_id");
        if (coachId != null) {
            coachDao.findById(coachId).ifPresent(club::setCoach);
        }
        
        return club;
    }
    
    public List<Club> saveAll(List<Club> clubs) {
        List<Club> savedClubs = new ArrayList<>();
        for (Club club : clubs) {
            savedClubs.add(save(club));
        }
        return savedClubs;
    }
}