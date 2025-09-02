package com.example.agentic;

import io.dapr.durabletask.Task;
import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParallelWorkflow implements Workflow {
  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("Starting Workflow: {}", ctx.getName());

      WorkflowInput workflowInput = ctx.getInput(WorkflowInput.class);

      List<Task<String>> processTasks = workflowInput.inputs()
              .stream()
              .map(input -> ctx.callActivity(PromptActivity.class.getName(), new PromptInput(workflowInput.prompt(), input), String.class))
              .collect(Collectors.toList());

      List<String> workerResponses = ctx.allOf(processTasks).await();

      ctx.complete(workerResponses);
    };
  }

  public record WorkflowInput(String prompt, List<String> inputs){}
  public record PromptInput(String prompt, String input){}
}
