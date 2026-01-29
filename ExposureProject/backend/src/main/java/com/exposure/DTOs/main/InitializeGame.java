package com.exposure.DTOs.main;

import com.exposure.DTOs.game.BotDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InitializeGame {
    private Long sessionId;
    private List<BotDTO> bots;
    private int questionsLeft;

    public InitializeGame(Long sessionId, List<BotDTO> bots, int questionsLeft) {
        this.sessionId = sessionId;
        this.bots = bots;
        this.questionsLeft = questionsLeft;
    }
}
