package com.example.football.dao;

import com.example.football.model.Player;
import com.example.football.model.PlayerStatistics;
import com.example.football.model.PlayingTime;
import com.example.football.model.enums.DurationUnit;
import com.example.football.model.enums.PlayerPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlayerDao {
    
    private final Connection connection;
    
    public List<Player> findAll() {
        List<Player> players = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Player")) {
            
            while (rs.next()) {
                players.add(mapRowToPlayer(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all players", e);
        }
        return players;
    }
    
    public Optional<Player> findById(String id) {
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Player WHERE PK_id = ?")) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPlayer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching player by id: " + id, e);
        }
        return Optional.empty();
    }
    
    public List<Player> findByClubId(String clubId) {
        List<Player> players = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Player WHERE FK_club_id = ?")) {
            pstmt.setString(1, clubId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    players.add(mapRowToPlayer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching players by club id: " + clubId, e);
        }
        return players;
    }
    
    public List<Player> findByNameContaining(String name) {
        List<Player> players = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(
                "SELECT * FROM Player WHERE LOWER(name) LIKE ?")) {
            pstmt.setString(1, "%" + name.toLowerCase() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    players.add(mapRowToPlayer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching players by name: " + name, e);
        }
        return players;
    }
    
    public List<Player> findByAgeRange(Integer ageMinimum, Integer ageMaximum) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM Player WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (ageMinimum != null) {
            queryBuilder.append(" AND age >= ?");
            params.add(ageMinimum);
        }
        
        if (ageMaximum != null) {
            queryBuilder.append(" AND age <= ?");
            params.add(ageMaximum);
        }
        
        List<Player> players = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(queryBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    players.add(mapRowToPlayer(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching players by age range", e);
        }
        return players;
    }
    
    public Player save(Player player) {
        if (player.getId() == null || player.getId().isEmpty()) {
            return insert(player);
        } else {
            return update(player);
        }
    }
    
    private Player insert(Player player) {
        String query = "INSERT INTO Player (name, number, position, nationality, age, FK_club_id) "
                + "VALUES (?, ?, ?::position_enum, ?, ?, ?) RETURNING PK_id";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player.getName());
            if (player.getNumber() != null) {
                pstmt.setInt(2, player.getNumber());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            if (player.getPosition() != null) {
                pstmt.setString(3, player.getPosition().name());
            } else {
                pstmt.setNull(3, Types.OTHER);
            }
            
            pstmt.setString(4, player.getNationality());
            
            if (player.getAge() != null) {
                pstmt.setInt(5, player.getAge());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            if (player.getClubId() != null && !player.getClubId().isEmpty()) {
                pstmt.setString(6, player.getClubId());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    player.setId(rs.getString("PK_id"));
                    return player;
                } else {
                    throw new RuntimeException("Failed to insert player, no ID returned");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting player", e);
        }
    }
    
    private Player update(Player player) {
        String query = "UPDATE Player SET name = ?, number = ?, position = ?::position_enum, "
                + "nationality = ?, age = ?, FK_club_id = ? WHERE PK_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, player.getName());
            
            if (player.getNumber() != null) {
                pstmt.setInt(2, player.getNumber());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            if (player.getPosition() != null) {
                pstmt.setString(3, player.getPosition().name());
            } else {
                pstmt.setNull(3, Types.OTHER);
            }
            
            pstmt.setString(4, player.getNationality());
            
            if (player.getAge() != null) {
                pstmt.setInt(5, player.getAge());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            
            if (player.getClubId() != null && !player.getClubId().isEmpty()) {
                pstmt.setString(6, player.getClubId());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }
            
            pstmt.setString(7, player.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Player update failed, no rows affected, id: " + player.getId());
            }
            return player;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating player", e);
        }
    }
    
    public void updateClubId(String playerId, String clubId) {
        String query = "UPDATE Player SET FK_club_id = ? WHERE PK_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            if (clubId != null && !clubId.isEmpty()) {
                pstmt.setString(1, clubId);
            } else {
                pstmt.setNull(1, Types.VARCHAR);
            }
            pstmt.setString(2, playerId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Player club update failed, no rows affected, id: " + playerId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating player's club", e);
        }
    }
    
    public void detachPlayersFromClub(String clubId) {
        String query = "UPDATE Player SET FK_club_id = NULL WHERE FK_club_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, clubId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error detaching players from club", e);
        }
    }
    
    public PlayerStatistics getPlayerStatistics(String playerId, String seasonId) {
        int scoredGoals = countScoredGoals(playerId, seasonId);
        PlayingTime playingTime = calculatePlayingTime(playerId, seasonId);
        
        return new PlayerStatistics(scoredGoals, playingTime);
    }
    
    private int countScoredGoals(String playerId, String seasonId) {
        String query = "SELECT COUNT(*) FROM Goal g " +
                "JOIN Match m ON g.FK_match_id = m.PK_id " +
                "WHERE g.FK_player_id = ? AND m.FK_season_id = ? AND g.own_goal = false";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerId);
            pstmt.setString(2, seasonId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting scored goals", e);
        }
    }
    
    private PlayingTime calculatePlayingTime(String playerId, String seasonId) {
        // This is a simple implementation that assumes a player plays full time in each match
        // A more realistic implementation would track substitutions or actual minutes played
        String query = "SELECT COUNT(*) FROM Match m " +
                "JOIN Player p ON (m.FK_home_club_id = p.FK_club_id OR m.FK_away_club_id = p.FK_club_id) " +
                "WHERE p.PK_id = ? AND m.FK_season_id = ? AND m.status = 'FINISHED'::status_enum";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, playerId);
            pstmt.setString(2, seasonId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int matches = rs.getInt(1);
                    // Each match is 90 minutes
                    return new PlayingTime((double) (matches * 90), DurationUnit.MINUTE);
                }
                return new PlayingTime(0.0, DurationUnit.MINUTE);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating playing time", e);
        }
    }
    
    private Player mapRowToPlayer(ResultSet rs) throws SQLException {
        Player player = new Player();
        player.setId(rs.getString("PK_id"));
        player.setName(rs.getString("name"));
        player.setNumber(rs.getObject("number", Integer.class));
        
        String positionStr = rs.getString("position");
        if (positionStr != null) {
            try {
                player.setPosition(PlayerPosition.valueOf(positionStr));
            } catch (IllegalArgumentException e) {
                // Handle case when enum value might not match
                player.setPosition(null);
            }
        }
        
        player.setNationality(rs.getString("nationality"));
        player.setAge(rs.getObject("age", Integer.class));
        player.setClubId(rs.getString("FK_club_id"));
        
        return player;
    }
    
    public List<Player> saveAll(List<Player> players) {
        List<Player> savedPlayers = new ArrayList<>();
        for (Player player : players) {
            savedPlayers.add(save(player));
        }
        return savedPlayers;
    }
}