package com.exposure.DTOs.service.AI;

import java.util.List;

public record TimelineEvent(
        String time,
        String event,
        String location,
        List<String> witnesses
) {}
