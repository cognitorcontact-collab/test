package com.football.centralapi.service;

import com.football.centralapi.dao.ClubDAO;
import com.football.centralapi.dto.ChampionshipRankingDTO;
import com.football.centralapi.model.enums.Championship;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChampionshipService {
    
    private final ClubDAO clubDAO;
    
    public ChampionshipService(ClubDAO clubDAO) {
        this.clubDAO = clubDAO;
    }
    
    public List<ChampionshipRankingDTO> getChampionshipRankings() {
        Map<String, List<Integer>> differenceGoalsByChampionship = 
            clubDAO.getDifferenceGoalsByChampionshipAcrossSeasons();
        
        List<ChampionshipRankingDTO> rankings = new ArrayList<>();
        
        for (Map.Entry<String, List<Integer>> entry : differenceGoalsByChampionship.entrySet()) {
            String championshipId = entry.getKey();
            List<Integer> differenceGoals = entry.getValue();
            
            // Calculate median of total difference goals across all seasons
            double median = calculateMedian(differenceGoals);
            
            Championship championship = Championship.valueOf(championshipId);
            
            ChampionshipRankingDTO dto = ChampionshipRankingDTO.builder()
                .championship(championship)
                .differenceGoalsMedian(median)
                .build();
                
            rankings.add(dto);
        }
        
        // Sort by median (ascending) - lower median is better
        rankings.sort(Comparator.comparing(ChampionshipRankingDTO::getDifferenceGoalsMedian));
        
        // Assign ranks
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings;
    }
    
    private double calculateMedian(List<Integer> values) {
        if (values.isEmpty()) {
            return 0;
        }
        
        Collections.sort(values);
        int size = values.size();
        
        if (size % 2 == 0) {
            // Even number of elements, average the middle two
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            // Odd number of elements, return the middle one
            return values.get(size / 2);
        }
    }
}