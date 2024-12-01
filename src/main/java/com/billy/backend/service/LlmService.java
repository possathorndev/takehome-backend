package com.billy.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@Service
public class LlmService {
    
    private final WebClient webClient;
    private static final String NVIDIA_API_BASE_URL = "https://integrate.api.nvidia.com";
    private static final String CHAT_COMPLETION_URI = "/v1/chat/completions";
    @Value("${api.key}")
    private String apiKey;

    public LlmService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(NVIDIA_API_BASE_URL).build();
    }

    public String chatCompletionApi(String productName) {
        try {
            String requestBody = String.format(
                "{\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"You are an AI that helps sellers create product descriptions. The user will provide a product name, and you will return a short, precise, and customer-focused product description. Just return the product description without quoting the response.\\n\\nProduct name: %s\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"model\": \"meta/llama-3.1-405b-instruct\",\n" +
                "  \"temperature\": 0.2,\n" +
                "  \"top_p\": 0.7,\n" +
                "  \"frequency_penalty\": 0,\n" +
                "  \"presence_penalty\": 0,\n" +
                "  \"max_tokens\": 1024,\n" +
                "  \"stream\": true\n" +
                "}", productName);

            StringBuilder contentBuilder = new StringBuilder();

            Mono<Void> response = webClient.post()
                .uri(CHAT_COMPLETION_URI)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> {
                    String content = extractContentFromChunk(chunk);
                    if (content != null) {
                        contentBuilder.append(content);
                    }
                })
                .then();

            response.block();

            return contentBuilder.toString();
        } catch (WebClientResponseException e) {
            return "~ Error: Chat Completion API " + e.getMessage(); 
        }
    }

    private String extractContentFromChunk(String chunk) {
        int contentStart = chunk.indexOf("\"content\":\"");
        if (contentStart == -1) {
            return null;
        }
        contentStart += "\"content\":\"".length();
        int contentEnd = chunk.indexOf("\"", contentStart);
        if (contentEnd == -1) {
            return null;
        }
        return chunk.substring(contentStart, contentEnd);
    }
}
