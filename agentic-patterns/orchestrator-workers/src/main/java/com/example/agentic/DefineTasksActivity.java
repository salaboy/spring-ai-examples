package com.example.agentic;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.example.agentic.OrchestratorWorkersWorkflow.DEFAULT_ORCHESTRATOR_PROMPT;

@Component
public class DefineTasksActivity implements WorkflowActivity {

  @Autowired
  private ChatClient chatClient;

  @Override
  public Object run(WorkflowActivityContext workflowActivityContext) {
    var taskDescription = workflowActivityContext.getInput(String.class);
    // Step 1: Get orchestrator response
    return this.chatClient.prompt()
            .user(u -> u.text(DEFAULT_ORCHESTRATOR_PROMPT)
                    .param("task", taskDescription))
            .call()
            .entity(OrchestratorWorkersWorkflow.OrchestratorResponse.class);
  }
}
