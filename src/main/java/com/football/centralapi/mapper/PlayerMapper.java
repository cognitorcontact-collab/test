package com.football.centralapi.mapper;

import com.football.centralapi.dto.PlayingTimeDTO;
import com.football.centralapi.dto.PlayerRankingDTO;
import com.football.centralapi.model.enums.Championship;
import com.football.centralapi.model.enums.PlayerPosition;
import com.football.centralapi.model.enums.TimeUnit;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PlayerMapper {
    
    public PlayerRankingDTO mapToPlayerRankingDTO(Map<String, Object> data) {
        return PlayerRankingDTO.builder()
            .id((String) data.get("player_id"))
            .name((String) data.get("name"))
            .number((Integer) data.get("number"))
            .position(PlayerPosition.valueOf((String) data.get("position")))
            .nationality((String) data.get("nationality"))
            .age((Integer) data.get("age"))
            .championship(Championship.valueOf((String) data.get("championship_id")))
            .scoredGoals((Integer) data.get("seasonScoredGoals"))
            .playingTime(PlayingTimeDTO.builder()
                .value((Double) data.get("seasonPlayingTimeValue"))
                .durationUnit(TimeUnit.valueOf((String) data.get("seasonPlayingTimeUnit")))
                .build())
            .build();
    }
}