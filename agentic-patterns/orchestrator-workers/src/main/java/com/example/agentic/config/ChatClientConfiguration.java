package com.example.agentic.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfiguration {
  @Bean
  public ChatClient chatClient(ChatClient.Builder chatClientBuilder){
    return chatClientBuilder.clone().build();
  }
}
