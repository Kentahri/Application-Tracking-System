package ats.service;

public interface GeminiChatService {

    String generate(String prompt);

    String generate(String systemPrompt, String userPrompt);

    String generateJson(String systemPrompt, String userPrompt);
}
