package com.exposure.DTOs.service.AI;

public record CharacterData(
        String name,
        boolean isGuilty,
        String motive,
        String alibi,
        String actual,
        String personality
) {}
