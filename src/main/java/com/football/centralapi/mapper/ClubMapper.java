package com.football.centralapi.mapper;

import com.football.centralapi.dto.ClubDTO;
import com.football.centralapi.dto.ClubRankingDTO;
import com.football.centralapi.dto.CoachDTO;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ClubMapper {
    
    public ClubRankingDTO mapToClubRankingDTO(Map<String, Object> data) {
        return ClubRankingDTO.builder()
            .club(ClubDTO.builder()
                .id((String) data.get("club_id"))
                .name((String) data.get("name"))
                .acronym((String) data.get("acronym"))
                .yearCreation((Integer) data.get("yearCreation"))
                .stadium((String) data.get("stadium"))
                .coach(CoachDTO.builder()
                    .name((String) data.get("coach_name"))
                    .nationality((String) data.get("coach_nationality"))
                    .build())
                .build())
            .rankingPoints((Integer) data.get("rankingPoints"))
            .scoredGoals((Integer) data.get("scoredGoals"))
            .concededGoals((Integer) data.get("concededGoals"))
            .differenceGoals((Integer) data.get("differenceGoals"))
            .cleanSheetNumber((Integer) data.get("cleanSheetNumber"))
            .build();
    }
}