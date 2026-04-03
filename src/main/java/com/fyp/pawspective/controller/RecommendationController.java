package com.fyp.pawspective.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@RestController
public class RecommendationController {
    private final ChatClient chatClient;

    public RecommendationController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/recommendation")
    public BreedRecommendation getRecommendation(@RequestBody HashMap<String, String> context) {
        // Create an output converter for the BreedRecommendation class
        BeanOutputConverter<BreedRecommendation> outputConverter = new BeanOutputConverter<>(BreedRecommendation.class);

        // Build a prompt with the user context and the format instructions
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Provide the most suitable pet breed and justifications given the following criteria:\n\n");

        // Add all context parameters
        for (var entry : context.entrySet()) {
            promptBuilder.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        promptBuilder.append("\n\n").append(outputConverter.getFormat());

        // Use the fluent ChatClient API
        String response = chatClient.prompt()
                .user(promptBuilder.toString())
                .call()
                .content();

        // Parse the response
        return outputConverter.convert(response);
    }

    // Data class for structured recommendation output
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BreedRecommendation {
        private String breedName;
        private String petType; // dog, cat, etc.
        private String description;
        private String matchReasoning;
        private List<String> careRequirements;
        private List<String> traits;
    }
}