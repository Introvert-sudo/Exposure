package com.exposure.events;

public record GameSessionCreatedEvent(Long sessionId, int botsCount, int lyingBotsCount) {}
