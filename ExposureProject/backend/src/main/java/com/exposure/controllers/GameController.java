package com.exposure.controllers;

import com.exposure.DTOs.game.*;
import com.exposure.DTOs.main.InitializeGame;
import com.exposure.events.GameSessionCreatedEvent;
import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.*;
import com.exposure.repositories.*;
import com.exposure.services.MissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:5173")
public class GameController {
    private final BotRepository botRepository;
    private final UserRepository userRepository;
    private final GameSessionRepository gameSessionRepository;
    private final MissionRepository missionRepository;
    private final StoryRepository storyRepository;

    private final BotResponseInterface botResponseService;

    private final Logger logger = LoggerFactory.getLogger(GameController.class);

    private final ApplicationEventPublisher eventPublisher;


    /*
        TODO: нужно сделать защиту против того, что пользователь вызовет старт игры несколько раз и не завершит сессию.
     */
    @Transactional
    @PostMapping("/start")
    public ResponseEntity<?> getPage(@RequestBody GameRequest request) {

        if (request.userId == null || request.selectedBotIds == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> userOpt = userRepository.findById(Long.parseLong(request.userId));
        List<Long> selectedBotIds = request.selectedBotIds;
        Long missionId = request.missionId != null ? request.missionId : 1L;
        Optional<Mission> missionOpt = missionRepository.findById(missionId);


        if (userOpt.isPresent()
                && selectedBotIds != null
                && !selectedBotIds.isEmpty()
                && missionOpt.isPresent()) {

            List<Bot> bots = botRepository.findAllById(selectedBotIds);

            if (bots.size() == selectedBotIds.size()) {
                User user = userOpt.get();

                List<Bot> mutableBots = new ArrayList<>(bots);
                Collections.shuffle(mutableBots);
                Bot randomLiar = mutableBots.getFirst();
                List<Bot> lyingBots = List.of(randomLiar);

                Mission mission = missionOpt.get();
                int initialLimit = mission.getInitialQuestionsAmount();

                GameSession gameSession = new GameSession(user, bots, lyingBots, initialLimit, mission);
                gameSessionRepository.save(gameSession);

                eventPublisher.publishEvent(new GameSessionCreatedEvent(
                        gameSession.getId(), bots.size(), lyingBots.size()
                ));

                for (Bot bot : bots) {
                    Chat chat = new Chat();

                    chat.getMembers().add(user);
                    chat.getMembers().add(bot);

                    gameSession.addChat(chat);
                }

                List<BotDTO> botDTOs = bots.stream()
                        .map(b -> new BotDTO(b.getId(), b.getName()))
                        .toList();

                return ResponseEntity.ok(new InitializeGame(gameSession.getId(), botDTOs, initialLimit));
            }
        }

        return ResponseEntity.badRequest().build();
    }


    @PostMapping("/question")
    @Transactional
    public ResponseEntity<?> question(@RequestBody QuestionRequest request) {
        User user = userRepository.findById(request.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Bot bot = botRepository.findById(request.botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot not found"));
        GameSession gameSession = gameSessionRepository.findById(request.sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!gameSession.getUser().getId().equals(user.getId())) return ResponseEntity.badRequest().build();
        if (!gameSession.getBots().contains(bot)) return ResponseEntity.badRequest().build();
        if (gameSession.getQuestionsLeft() <= 0) return ResponseEntity.status(403).body("No questions left");

        Chat chat = gameSession.getChats().stream()
                .filter(c -> c.getMembers().contains(user) && c.getMembers().contains(bot))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chat between user and bot not initialized"));

        Story story = gameSession.getStory();

        // BotStates state = gameSession.isBotLying(bot.getId()) ? BotStates.LYING : BotStates.NOT_LYING;
        String botResponseText = botResponseService.getResponse(bot, request.question, chat, story, gameSession);

        if (botResponseText != null && botResponseText.length() > 1000) {
            botResponseText = botResponseText.substring(0, 997) + "...";
        }

        saveMessage(chat, user, request.question);
        saveMessage(chat, bot, botResponseText);

        gameSession.decreaseQuestionLeft();
        return ResponseEntity.ok(new QuestionResponse(botResponseText, gameSession.getQuestionsLeft()));
    }


    /*
    TODO: переместить этот метод в CHAT MODEL.
     */
    private void saveMessage(Chat chat, SessionMember sender, String text) {
        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setText(text);
        message.setSentAt(LocalDateTime.now());

        chat.getMessages().add(message);
    }


    @PostMapping("/choice")
    public ResponseEntity<?> choice(@RequestBody ChoiceRequest request) {
        if (request.userId == null || request.botId == null || request.sessionId == null) {
            return ResponseEntity.badRequest().body("ID cannot be null");
        }

        User user = userRepository.findById(request.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Bot bot = botRepository.findById(request.botId)
                .orElseThrow(() -> new IllegalArgumentException("Bot not found"));
        GameSession gameSession = gameSessionRepository.findById(request.sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!gameSession.getUser().getId().equals(user.getId())) return ResponseEntity.badRequest().build();
        if (!gameSession.getBots().contains(bot)) return ResponseEntity.badRequest().build();

        if (gameSession.isBotLying(bot.getId())) {
            return ResponseEntity.ok(new ChoiceResponse(true, bot.getId()));
        } else {
            return ResponseEntity.ok(new ChoiceResponse(false, bot.getId()));
        }
    }

    @GetMapping("/mission/{sessionId}")
    public ResponseEntity<?> missions(@PathVariable("sessionId") String sessionIdStr) {
        try {
            Long sessionId = Long.parseLong(sessionIdStr);
            GameSession gameSession = gameSessionRepository.findById(sessionId).orElseThrow();
            Mission mission = gameSession.getMission();

            if (mission != null) {
                GameMissionResponse response = new GameMissionResponse(
                        mission.getTitle(),
                        mission.getDescription(),
                        mission.getInitialQuestionsAmount());

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error in \"/mission\": ", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/status/{sessionId}")
    public ResponseEntity<?> status(@PathVariable("sessionId") String sessionId) {
        try {
            Long id = Long.parseLong(sessionId);
            GameSession gameSession = gameSessionRepository.findById(id).orElseThrow();

            Map<String, String> response = new HashMap<>();

            if (gameSession.getStatus().equals(GameStatus.READY) && gameSession.getStory() != null) {
                response.put("status", "READY");
                return ResponseEntity.ok(response);
            }

            response.put("status", "GENERATING");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Status controller error: ", e);
            return ResponseEntity.status(404).body(Map.of("error", "Session not found"));
        }
    }
}
