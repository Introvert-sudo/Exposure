package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameMissionResponse {
    private String title;
    private String description;
    private int initialQuestionsAmount;

    public GameMissionResponse(String title, String description, int initialQuestionsAmount) {
        this.title = title;
        this.description = description;
        this.initialQuestionsAmount = initialQuestionsAmount;
    }
}
