package com.exposure.DTOs.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GameSessionDTO {
    public long sessionId;
    public long userId;
    public List<BotDTO> bots;
}
