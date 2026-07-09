package ats.service.impl;

import ats.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiEmbeddingService implements EmbeddingService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.embedding-model}")
    private String model;

    @Value("${gemini.embedding-dimension}")
    private Integer dimension;

    private final RestClient restClient = RestClient.create();


    @Override
    public List<Float> embed(String text) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":embedContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "content", Map.of(
                        "parts", List.of(Map.of("text", text))
                ),
                "outputDimensionality", dimension
        );

        Map<?, ?> response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        Map<?, ?> embedding = (Map) response.get("embedding");
        List<Number> values = (List<Number>) embedding.get("values");

        return values.stream()
                .map(Number::floatValue)
                .toList();
    }
}
