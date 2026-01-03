package com.exposure.services;

import com.exposure.models.Bot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotServiceTest {

    @Mock
    private PromptGenerator promptGenerator;

    @Mock
    private OllamaClient ollamaClient;

    @InjectMocks
    private BotService botService;

    @Test
    void smokeTestBotResponse() {
        Bot testBot = new Bot("Geralt", "Мрачный ведьмак из Ривии");

        String mockPrompt = "System: Ты Geralt...";
        String mockAiResponse = "Хм... Выглядит как заказ.";

        when(promptGenerator.generatePrompt(eq(testBot), anyString()))
                .thenReturn(mockPrompt);

        when(ollamaClient.generate(anyString(), eq(mockPrompt)))
                .thenReturn(mockAiResponse);

        String result = botService.getResponse(testBot, "Нужна помощь с монстром");

        assertEquals("Хм... Выглядит как заказ.", result);
    }
}
