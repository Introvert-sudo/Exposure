package com.exposure.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/main")
public class MainController {
    // TODO: обработка сессии захода на сайт если нужно
    @GetMapping
    public String getPage() {
        return null;
    }
}
