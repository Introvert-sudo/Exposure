package com.exposure.services;

import com.exposure.models.GameSession;
import com.exposure.models.GameStatus;
import com.exposure.models.Mission;
import com.exposure.models.Story;
import com.exposure.repositories.GameSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionService {
    private final GameSessionRepository gameSessionRepository;

    private final StoryGeneratorService storyGeneratorService;

    @Async
    @Transactional
    public void generateStoryAsync(Long session_id, int bots, int lyingBots) {
        GameSession session = gameSessionRepository.findById(session_id).orElseThrow();
        Mission mission = session.getMission();

        try {
            Story story = storyGeneratorService.generateStory(mission, bots, lyingBots);
            session.setStatus(GameStatus.READY);
            session.setStory(story);
        } catch (Exception e) {
            session.setStatus(GameStatus.FAILED);
        }
        gameSessionRepository.save(session);
    }
}
