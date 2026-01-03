package com.exposure.services;

import com.exposure.models.Bot;
import org.springframework.stereotype.Service;



@Service
public class PromptGenerator {
    public String generatePrompt(Bot bot, String userMessage) {
        String botName = bot.getName();
        String botPersonality = bot.getPersonality();

        StringBuilder prompt = new StringBuilder();

        prompt.append("Ты находишься в режиме ролевой игры. Твоя роль: ").append(botName).append(".\n");
        prompt.append("Твоя личность и предыстория: ").append(botPersonality).append(".\n\n");

        prompt.append("Инструкции по поведению:\n");
        prompt.append("- Отвечай строго в соответствии со своим характером.\n");
        prompt.append("- Не выходи из роли и не упоминай, что ты искусственный интеллект.\n");
        prompt.append("- Используй стиль речи, подходящий твоему персонажу.\n\n");

        prompt.append("Текущее сообщение от пользователя: ").append(userMessage).append("\n");
        prompt.append("Твой ответ (от имени ").append(botName).append("):");

        // TODO: перенести сводку правил в файл?

        return prompt.toString();
    }
}
