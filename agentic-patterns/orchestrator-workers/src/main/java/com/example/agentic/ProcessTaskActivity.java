package com.example.agentic;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.example.agentic.Application.ORIGINAL_TASK;

@Component
public class ProcessTaskActivity implements WorkflowActivity {


  public static final String DEFAULT_WORKER_PROMPT = """
          Generate content based on:
          Task: {original_task}
          Style: {task_type}
          Guidelines: {task_description}
          """;

  @Autowired
  private ChatClient chatClient;

  @Override
  public Object run(WorkflowActivityContext workflowActivityContext) {
    var task = workflowActivityContext.getInput(OrchestratorWorkersWorkflow.Task.class);

    return this.chatClient.prompt()
            .user(u -> u.text(DEFAULT_WORKER_PROMPT)
                    .param("original_task", ORIGINAL_TASK)
                    .param("task_type", task.type())
                    .param("task_description", task.description()))
            .call()
            .content();


  }
}
