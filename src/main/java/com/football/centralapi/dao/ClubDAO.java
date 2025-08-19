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
public class ClubDAO {
    
    private final DataSource dataSource;
    
    public ClubDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void createOrUpdateClub(String id, String name, String acronym, int yearCreation, 
                                  String stadium, String coachName, String coachNationality,
                                  String championshipId) {
        String sql = "INSERT INTO club (club_id, name, acronym, yearCreation, stadium, coach_name, coach_nationality, championship_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (club_id) " +
                     "DO UPDATE SET name = ?, acronym = ?, yearCreation = ?, stadium = ?, " +
                     "coach_name = ?, coach_nationality = ?, championship_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.setString(3, acronym);
            stmt.setInt(4, yearCreation);
            stmt.setString(5, stadium);
            stmt.setString(6, coachName);
            stmt.setString(7, coachNationality);
            stmt.setString(8, championshipId);
            stmt.setString(9, name);
            stmt.setString(10, acronym);
            stmt.setInt(11, yearCreation);
            stmt.setString(12, stadium);
            stmt.setString(13, coachName);
            stmt.setString(14, coachNationality);
            stmt.setString(15, championshipId);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating or updating club", e);
        }
    }
    
    public void updateClubStats(String clubId, String seasonId, int rankingPoints, 
                               int scoredGoals, int concededGoals, int differenceGoals, 
                               int cleanSheetNumber) {
        String sql = "INSERT INTO club_stats (club_id, season_id, rankingPoints, scoredGoals, " +
                     "concededGoals, differenceGoals, cleanSheetNumber) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (club_id, season_id) " +
                     "DO UPDATE SET rankingPoints = ?, scoredGoals = ?, concededGoals = ?, " +
                     "differenceGoals = ?, cleanSheetNumber = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, clubId);
            stmt.setString(2, seasonId);
            stmt.setInt(3, rankingPoints);
            stmt.setInt(4, scoredGoals);
            stmt.setInt(5, concededGoals);
            stmt.setInt(6, differenceGoals);
            stmt.setInt(7, cleanSheetNumber);
            stmt.setInt(8, rankingPoints);
            stmt.setInt(9, scoredGoals);
            stmt.setInt(10, concededGoals);
            stmt.setInt(11, differenceGoals);
            stmt.setInt(12, cleanSheetNumber);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating club stats", e);
        }
    }
    
    public List<Map<String, Object>> getClubStatsAcrossAllSeasons() {
        String sql = "SELECT c.club_id, c.name, c.acronym, c.yearCreation, c.stadium, " +
                     "c.coach_name, c.coach_nationality, c.championship_id, " +
                     "cs.rankingPoints, cs.scoredGoals, cs.concededGoals, " +
                     "cs.differenceGoals, cs.cleanSheetNumber " +
                     "FROM club c " +
                     "JOIN club_stats cs ON c.club_id = cs.club_id";
        
        List<Map<String, Object>> clubs = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> club = new HashMap<>();
                club.put("club_id", rs.getString("club_id"));
                club.put("name", rs.getString("name"));
                club.put("acronym", rs.getString("acronym"));
                club.put("yearCreation", rs.getInt("yearCreation"));
                club.put("stadium", rs.getString("stadium"));
                club.put("coach_name", rs.getString("coach_name"));
                club.put("coach_nationality", rs.getString("coach_nationality"));
                club.put("championship_id", rs.getString("championship_id"));
                club.put("rankingPoints", rs.getInt("rankingPoints"));
                club.put("scoredGoals", rs.getInt("scoredGoals"));
                club.put("concededGoals", rs.getInt("concededGoals"));
                club.put("differenceGoals", rs.getInt("differenceGoals"));
                club.put("cleanSheetNumber", rs.getInt("cleanSheetNumber"));
                
                clubs.add(club);
            }
            
            return clubs;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving clubs with stats", e);
        }
    }
    
    public Map<String, List<Integer>> getDifferenceGoalsByChampionshipAcrossSeasons() {
        String sql = "SELECT c.championship_id, cs.differenceGoals " +
                     "FROM club c " +
                     "JOIN club_stats cs ON c.club_id = cs.club_id";
        
        Map<String, List<Integer>> result = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String championshipId = rs.getString("championship_id");
                int differenceGoals = rs.getInt("differenceGoals");
                
                if (!result.containsKey(championshipId)) {
                    result.put(championshipId, new ArrayList<>());
                }
                
                result.get(championshipId).add(differenceGoals);
            }
            
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving difference goals by championship", e);
        }
    }
}