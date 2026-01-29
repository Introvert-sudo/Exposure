package com.exposure.controllers;

import com.exposure.DTOs.game.BotDTO;
import com.exposure.DTOs.main.MissionInfo;
import com.exposure.models.Bot;
import com.exposure.models.GameSession;
import com.exposure.models.Mission;
import com.exposure.repositories.BotRepository;
import com.exposure.repositories.GameSessionRepository;
import com.exposure.repositories.MissionRepository;
import com.exposure.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/main")
public class MainController {
    private final BotRepository botRepository;
    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;
    private final MissionRepository missionRepository;

    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    // TODO: Защита для токенов (обернуть в try-catch)
    @Transactional
    @GetMapping
    public ResponseEntity<?> getPage(@RequestHeader("Authorization") String token) {

        System.out.println(token);
        Long userId = Long.parseLong(token);

        if (userRepository.findById(userId).isPresent()) {
            List<GameSession> activeSessions = gameSessionRepository.findAllByUserIdAndIsActiveTrue(userId);
            activeSessions.forEach(session -> session.setIsActive(false));

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    // Mock function
    @GetMapping("/bots")
    public List<BotDTO> getBots() {
        /*
        
        На будущее - нужно переделать фронт, чтобы был список ботов (просто нужно будет попробовать сделать
        Lazy loading)
        Ну и естественно, брать ботов из базы данных, а не хардкодить.

        */

        // Это решение временное, так что лайно. Оно ограничивает главную страничку до 2 ботов.
        Optional<Bot> bot1 = botRepository.findBotById(Long.parseLong("2"));
        Optional<Bot> bot2 = botRepository.findBotById(Long.parseLong("3"));

        if (bot1.isPresent() && bot2.isPresent()) {
            BotDTO botDTO1 = new BotDTO(bot1.get().getId(), bot1.get().getName());
            BotDTO botDTO2 = new BotDTO(bot2.get().getId(), bot2.get().getName());

            return List.of(botDTO1, botDTO2);
        }

        return null;
    }

    // TODO: Добавить GET метод, который будет отправлять список доступных миссий пользователю.
    @GetMapping("/missions")
    public ResponseEntity<?> getMissions() {
        try {
            List<Mission> missions = missionRepository.findAll(); // TODO: После изменить на Lazy loading
            List<MissionInfo> missionDTOs = missions.stream()
                    .map(m -> new MissionInfo(m.getId(), m.getTitle()))
                    .toList();

            return ResponseEntity.ok(missionDTOs);
        } catch (Exception e) {
            logger.error("Error while getting missions: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
