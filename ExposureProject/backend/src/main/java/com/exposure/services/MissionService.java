package com.exposure.services;

import com.exposure.DTOs.service.AI.CharacterData;
import com.exposure.DTOs.service.AI.StoryResponse;
import com.exposure.models.Bot;
import com.exposure.models.Mission;
import com.exposure.models.Story;
import com.exposure.repositories.StoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class MissionService {

    private final ChatClient chatClient;
    private final StoryRepository storyRepository;
    private final ObjectMapper objectMapper;

    public Story generateStory(Mission mission, List<Bot> bots, List<Bot> lying_bots) { // TODO: добавить еще в промпт лгущих ботов.
        String prompt = createStoryPrompt(
                mission.getHistory_description(),
                bots
        );

        StoryResponse response;
        try {
            response = chatClient.prompt(prompt)
                    .call()
                    .entity(StoryResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("LLM returned invalid structured response", e);
        }

        // минимальная логическая проверка (очень советую оставить)
        validateStory(response);

        String json;
        try {
            json = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize StoryResponse", e);
        }

        Story story = new Story();
        story.setMission(mission);
        story.setGeneratedStoryJson(json);

        return storyRepository.save(story);
    }

    private void validateStory(StoryResponse story) {
        long guiltyCount = story.characters_data()
                .stream()
                .filter(CharacterData::isGuilty)
                .count();

        if (guiltyCount != 1) { // Пока что бизнес логика подразумевает одного виновного, но я оставил список просто чтобы в будущем можно было легко изменить
            throw new IllegalStateException("Story must contain exactly one guilty character");
        }
    }

    private String createStoryPrompt(String historyDesc, List<Bot> bots) {
        String jsonSchema = """
        {
          "story_meta": {
            "description": "string",
            "solution": "string"
          },
          "truth_timeline": [
            {
              "time": "HH:mm",
              "event": "string",
              "location": "string",
              "witnesses": ["string"]
            }
          ],
          "characters_data": [
            {
              "name": "string",
              "isGuilty": boolean,
              "motive": "string",
              "alibi": "string",
              "actual": "string",
              "personality": "string"
            }
          ],
          "clues": [
            { "type": "string", "description": "string", "found_at": "string" }
          ]
        }
        """;

        StringBuilder botsInfo = new StringBuilder();
        for (Bot bot : bots) {
            botsInfo.append("""
                - Name: %s
                  Personality: %s
                """.formatted(bot.getName(), bot.getPersonality()));
        }

        PromptTemplate promptTemplate = new PromptTemplate("""
            Ты — генератор детективных историй.

            Создай историю на основе следующего описания:
            {history_description}

            Участники и их характеры:
            {bots_info}

            История должна быть в строгом соответствии с предоставленной JSON схемой.
            Один из персонажей должен быть виновен (isGuilty = true).
            У виновного персонажа алиби должно отличаться от реального местоположения.
            У остальных персонажей alibi должно совпадать с actual.

            JSON схема:
            {json_schema}

            ВАЖНО:
            - Верни ТОЛЬКО JSON
            - Все поля обязательны
            - boolean только true/false
            - Время строго HH:mm
            """);

        Map<String, Object> model = new HashMap<>();
        model.put("history_description", historyDesc);
        model.put("bots_info", botsInfo.toString());
        model.put("json_schema", jsonSchema);

        return promptTemplate.render(model);
    }
}
