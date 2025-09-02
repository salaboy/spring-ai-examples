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
    ParallelWorkflow.PromptInput promptInput = workflowActivityContext.getInput(ParallelWorkflow.PromptInput.class);
    try{
      return chatClient.prompt(promptInput.prompt() + "\nInput: " + promptInput.input()).call().content();
    } catch (Exception e){
      throw new RuntimeException("Failed to process input: " + promptInput.input(), e);
    }

  }
}
