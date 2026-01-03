package com.exposure.services;

import com.exposure.models.Bot;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptGeneratorTest {

    private final PromptGenerator generator = new PromptGenerator();

    @Test
    void generatePrompt_ShouldContainBotNameAndPersonality() {
        Bot bot = new Bot("Siri", "Помощник");

        String prompt = generator.generatePrompt(bot, "Как погода?");

        assertTrue(prompt.contains("Siri"));
        assertTrue(prompt.contains("Помощник"));
        assertTrue(prompt.contains("Как погода?"));
    }
}