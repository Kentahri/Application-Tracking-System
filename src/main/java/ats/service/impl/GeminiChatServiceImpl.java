package ats.service.impl;

import ats.service.GeminiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiChatServiceImpl implements GeminiChatService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.chat-model}")
    private String model;

    private final RestClient restClient = RestClient.create();

    @Override
    public String generate(String prompt) {
        return generate(null, prompt);
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        return generateContent(systemPrompt, userPrompt, false);
    }

    @Override
    public String generateJson(String systemPrompt, String userPrompt) {
        return generateContent(systemPrompt, userPrompt, true);
    }

    private String generateContent(String systemPrompt, String userPrompt, boolean jsonResponse) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = new HashMap<>();
        body.put(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", userPrompt))
                ))
        );

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", jsonResponse ? 0.1 : 0.5);
        generationConfig.put("maxOutputTokens", jsonResponse ? 1800 : 2500);
        if (jsonResponse) {
            generationConfig.put("responseMimeType", "application/json");
        }

        body.put(
                "generationConfig", generationConfig
        );

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            body.put("systemInstruction", Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
            ));
        }

        Map<?, ?> response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        List<?> candidates = (List<?>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return "Tôi chưa tạo được câu trả lời từ dữ liệu hiện tại.";
        }

        Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
        Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
        if (content == null) {
            return "Tôi chưa tạo được câu trả lời từ dữ liệu hiện tại.";
        }

        List<?> parts = (List<?>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            return "Tôi chưa tạo được câu trả lời từ dữ liệu hiện tại.";
        }

        Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
        Object text = firstPart.get("text");
        return text != null ? text.toString() : "Tôi chưa tạo được câu trả lời từ dữ liệu hiện tại.";
    }
}
