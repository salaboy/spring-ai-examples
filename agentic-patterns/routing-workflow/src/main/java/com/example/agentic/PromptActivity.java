package com.example.agentic;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class PromptActivity implements WorkflowActivity {

  private final ChatClient chatClient;

  public PromptActivity(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public Object run(WorkflowActivityContext workflowActivityContext) {

    PromptRequest promptRequest = workflowActivityContext.getInput(PromptRequest.class);
    String prompt = promptRequest.prompt() + "\nInput: " + promptRequest.input();
    return chatClient.prompt(prompt).call().content();

  }
}
