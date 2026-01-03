package com.exposure.services;

import com.exposure.DTOs.ollama.OllamaRequestDTO;
import com.exposure.DTOs.ollama.OllamaResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


/*
    Ollama Client

    Connects by rest to ollama api to get responses.
 */


@Service
public class OllamaClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OLLAMA_URL =
            "http://localhost:11434/api/generate";

    public String generate(String model, String prompt) {

        OllamaRequestDTO request = new OllamaRequestDTO();
        request.model = model;
        request.prompt = prompt;

        ResponseEntity<OllamaResponseDTO> response =
                restTemplate.postForEntity(
                        OLLAMA_URL,
                        request,
                        OllamaResponseDTO.class
                );

        return response.getBody().response;
    }
}
