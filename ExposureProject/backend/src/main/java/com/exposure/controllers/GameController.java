package com.exposure.controllers;

import com.exposure.interfaces.BotResponseInterface;
import com.exposure.models.GameSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/api/game")
public class GameController {
    BotResponseInterface botResponseInterface;

    // TODO: Отправлять тут стартовые данные про игровую сессию по запросу
    @PostMapping("/start")
    public String getPage() {
        // Инициализация сессии
        // TODO: создать в бд ботов и мокать просто через айди
        // TODO: Кидать пользователя через токен на веб

        // то есть, сначала ты берешь токен с фронта и проверяешь есть ли такой вообще юзер.
        // дальше получаешь ботов который он выбрал (у нас они замоканные, поэтому с главной страницы кидаешь айди 2 ботов)
        // потом создаешь игровую сессию.
        // после этого, тебе нужно создать DTO для фронта и кинуть ему.

        GameSession gameSession = new GameSession();

        // Отправка DTO фронту
        return null;
    }

    // TODO: На вопрос он спрашивает у бота через
    //  клиент оллама и отправляет назад ответ
    @PostMapping("/question")
    public void onQuestion() {
        // берешь с фронта запрос, токен. По токену проверяешь игрока, активную сессию.
        // Если все ок, то стандартно просто берешь вопрос, из ззапроса айди бота, достаешь бота и кидаешь в аи и возвращаешь ответ.
    }

    // TODO: Когда игрок выбирает бота которому верит происходит проверка
    //  и вывод результата
    @PostMapping
    public void onChoice() {
        // опять таки, проверяешь есть ли игрок, все ли ок с сессией.
        // после этого проверяешь по сессии правильного бота ли выбрал игрок по айди или нет и отправляешь ответ.
    }

    // TODO: Обработка завершенния игровой сессии.
    @PostMapping("/endsession")
    public void endSession() {
        // тут игрок просто хочет закончить игровую сессию. Удаляешь сессию и отправляешь ответ пользователю.
    }
}
