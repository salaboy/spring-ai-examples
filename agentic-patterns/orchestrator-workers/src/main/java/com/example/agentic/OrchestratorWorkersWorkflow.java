package com.example.agentic;

import io.dapr.durabletask.Task;
import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrchestratorWorkersWorkflow implements Workflow {

  public static final String DEFAULT_ORCHESTRATOR_PROMPT = """
			Analyze this task and break it down into 2-3 distinct approaches:

			Task: {task}

			Return your response in this JSON format:
			\\{
			"analysis": "Explain your understanding of the task and which variations would be valuable.
			             Focus on how each approach serves different aspects of the task.",
			"tasks": [
				\\{
				"type": "formal",
				"description": "Write a precise, technical version that emphasizes specifications"
				\\},
				\\{
				"type": "conversational",
				"description": "Write an engaging, friendly version that connects with readers"
				\\}
			]
			\\}
			""";
	/**
	 * Response from the orchestrator containing task analysis and breakdown into
	 * subtasks.
	 *
	 * @param analysis Detailed explanation of the task and how different approaches
	 *                 serve its aspects
	 * @param tasks    List of subtasks identified by the orchestrator to be
	 *                 executed by workers
	 */
	public static record OrchestratorResponse(String analysis, List<Task> tasks) {
	}

	/**
	 * Represents a subtask identified by the orchestrator that needs to be executed
	 * by a worker.
	 *
	 * @param type        The type or category of the task (e.g., "formal",
	 *                    "conversational")
	 * @param description Detailed description of what the worker should accomplish
	 */
	public static record Task(String type, String description) {
	}

	/**
	 * Final response containing the orchestrator's analysis and combined worker
	 * outputs.
	 *
	 * @param analysis        The orchestrator's understanding and breakdown of the
	 *                        original task
	 * @param workerResponses List of responses from workers, each handling a
	 *                        specific subtask
	 */
	public static record FinalResponse(String analysis, List<String> workerResponses) {
	}

  @Override
  public WorkflowStub create() {

    return ctx -> {
      ctx.getLogger().info("Starting Workflow: {}", ctx.getName());

      String taskDescription = ctx.getInput(String.class);

      OrchestratorResponse orchestratorResponse = ctx.callActivity(DefineTasksActivity.class.getName(),
							taskDescription,
							OrchestratorResponse.class)
							.await();

			ctx.getLogger().info(String.format("\n=== ORCHESTRATOR OUTPUT ===\nANALYSIS: %s\n\nTASKS: %s\n",
							orchestratorResponse.analysis(), orchestratorResponse.tasks()));

			List<io.dapr.durabletask.Task<String>> processTasks = orchestratorResponse.tasks()
							.stream()
							.map(task -> ctx.callActivity(ProcessTaskActivity.class.getName(), task, String.class))
							.collect(Collectors.toList());

			List<String> workerResponses = ctx.allOf(processTasks).await();

      ctx.getLogger().info("\n=== WORKER OUTPUT ===\n{}", workerResponses);

      ctx.complete(new FinalResponse(orchestratorResponse.analysis(), workerResponses));
    };
  }


}
