package com.football.centralapi.service;

import com.football.centralapi.dao.ClubDAO;
import com.football.centralapi.dto.ClubRankingDTO;
import com.football.centralapi.mapper.ClubMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClubService {
    
    private final ClubDAO clubDAO;
    private final ClubMapper clubMapper;
    
    public ClubService(ClubDAO clubDAO, ClubMapper clubMapper) {
        this.clubDAO = clubDAO;
        this.clubMapper = clubMapper;
    }
    
    public List<ClubRankingDTO> getBestClubs(int top) {
        List<Map<String, Object>> clubStats = clubDAO.getClubStatsAcrossAllSeasons();
        
        // Group stats by club to combine across seasons
        Map<String, ClubRankingDTO> clubRankings = new HashMap<>();
        
        for (Map<String, Object> stat : clubStats) {
            String clubId = (String) stat.get("club_id");
            
            if (!clubRankings.containsKey(clubId)) {
                ClubRankingDTO dto = clubMapper.mapToClubRankingDTO(stat);
                clubRankings.put(clubId, dto);
            } else {
                ClubRankingDTO existing = clubRankings.get(clubId);
                
                // Sum up all statistics
                existing.setRankingPoints(existing.getRankingPoints() + (Integer) stat.get("rankingPoints"));
                existing.setScoredGoals(existing.getScoredGoals() + (Integer) stat.get("scoredGoals"));
                existing.setConcededGoals(existing.getConcededGoals() + (Integer) stat.get("concededGoals"));
                existing.setDifferenceGoals(existing.getDifferenceGoals() + (Integer) stat.get("differenceGoals"));
                existing.setCleanSheetNumber(existing.getCleanSheetNumber() + (Integer) stat.get("cleanSheetNumber"));
            }
        }
        
        // Sort by total ranking points (desc), then by total goal difference (desc), then by total clean sheets (desc)
        List<ClubRankingDTO> sortedRankings = new ArrayList<>(clubRankings.values());
        sortedRankings.sort(Comparator
            .comparing(ClubRankingDTO::getRankingPoints, Comparator.reverseOrder())
            .thenComparing(ClubRankingDTO::getDifferenceGoals, Comparator.reverseOrder())
            .thenComparing(ClubRankingDTO::getCleanSheetNumber, Comparator.reverseOrder()));
        
        // Assign ranks
        for (int i = 0; i < sortedRankings.size(); i++) {
            sortedRankings.get(i).setRank(i + 1);
        }
        
        // Limit to top N
        return sortedRankings.stream()
            .limit(top)
            .collect(Collectors.toList());
    }
}