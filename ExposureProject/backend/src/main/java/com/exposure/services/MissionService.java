package com.exposure.services;

import com.exposure.DTOs.service.AI.CharacterData;
import com.exposure.DTOs.service.AI.StoryResponse;
import com.exposure.models.Bot;
import com.exposure.models.Mission;
import com.exposure.models.Story;
import com.exposure.repositories.StoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import java.util.List;



@Slf4j
@Service
@RequiredArgsConstructor
public class MissionService {

    private final ChatClient chatClient;
    private final StoryRepository storyRepository;
    private final ObjectMapper objectMapper;

    public Story generateStory(Mission mission, List<Bot> bots, List<Bot> lyingBots) {

        String fullPrompt = buildStoryPrompt(
                mission.getHistory_description(),
                bots,
                lyingBots
        );

        Prompt prompt = new Prompt(
                List.of(new UserMessage(fullPrompt))
        );

        String rawResponse;

        try {
            rawResponse = chatClient
                    .prompt(prompt)
                    .call()
                    .content();
        } catch (Exception e) {

            log.error("LLM call failed");
            log.error("Prompt:\n{}", fullPrompt, e);

            throw new IllegalStateException("LLM call failed", e);
        }

        StoryResponse response;

        try {
            response = objectMapper.readValue(rawResponse, StoryResponse.class);
        } catch (Exception e) {

            log.error("Invalid LLM JSON");
            log.error("Prompt:\n{}", fullPrompt);
            log.error("Raw response:\n{}", rawResponse);

            throw new IllegalStateException(
                    "LLM returned invalid structure",
                    e
            );
        }

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
        long guiltyCount = story.charactersData()
                .stream()
                .filter(CharacterData::isGuilty)
                .count();

        if (guiltyCount != 1) {
            throw new IllegalStateException(
                    "Story must contain exactly one guilty character"
            );
        }
    }

    private String buildStoryPrompt(
            String historyDesc,
            List<Bot> bots,
            List<Bot> lyingBots
    ) {

        String botsInfo = buildBotsInfo(bots);
        String jsonSchema = storyJsonSchema();

        return """
                Ты — генератор детективных историй.
                
                Твоя задача — создать логически целостное преступление,
                которое игрок сможет раскрыть через допросы и анализ.
                
                ОПИСАНИЕ МИССИИ:
                %s
                
                ПЕРСОНАЖИ:
                %s
                
                ТРЕБОВАНИЯ:
                
                - ровно один персонаж виновен (isGuilty = true)
                - виновный лжёт в алиби
                - остальные говорят правду
                - временная линия должна быть логически непротиворечивой
                - мотив виновного должен быть связан с событием
                - улики должны подтверждать реальную версию событий
                
                ФОРМАТ ВЫВОДА:
                
                Верни ТОЛЬКО валидный JSON
                без комментариев
                без markdown
                без пояснений
                
                JSON схема:
                
                %s
                """.formatted(
                historyDesc,
                botsInfo,
                jsonSchema
        );
    }

    private String buildBotsInfo(List<Bot> bots) {

        StringBuilder sb = new StringBuilder();

        for (Bot bot : bots) {
            sb.append("""
            - Name: %s
              Personality: %s
            
            """.formatted(
                    bot.getName(),
                    bot.getPersonality()
            ));
        }

        return sb.toString();
    }

    private String storyJsonSchema() {
        return """
        ТРЕБОВАНИЯ К СТРУКТУРЕ JSON:
        1. story_meta: объект с кратким описанием сюжета (description) и финальной разгадкой (solution).
        2. truth_timeline: список событий в хронологическом порядке. 
           - Каждый элемент содержит: time (время HH:mm), event (что произошло), location (место), witnesses (список имен персонажей-свидетелей).
        3. characters_data: массив данных о всех ботах:
           - name: имя персонажа.
           - isGuilty: логическое значение (true, если это убийца/преступник).
           - motive: почему он это сделал или мог сделать.
           - alibi: что персонаж говорит в свое оправдание.
           - actual: что персонаж делал на самом деле в это время.
           - personality: черты характера и манера речи.
        4. clues: список улик, разбросанных по локациям:
           - type: тип предмета.
           - description: детальное описание.
           - found_at: в какой локации или у кого найдена.
        
        ВНИМАНИЕ: Не используй Markdown-разметку (типа ```json). Верни только чистый JSON-объект.
        """;
    }
}
