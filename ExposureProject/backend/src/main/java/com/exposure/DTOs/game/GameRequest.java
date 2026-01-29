package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GameRequest {
    public String userId;
    public List<Long> selectedBotIds;
    public Long missionId;
}
