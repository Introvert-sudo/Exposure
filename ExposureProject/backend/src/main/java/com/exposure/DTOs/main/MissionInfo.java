package com.exposure.DTOs.main;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MissionInfo {
    private Long id;
    private String title;

    public MissionInfo(Long id, String title) {
        this.id = id;
        this.title = title;
    }
}
