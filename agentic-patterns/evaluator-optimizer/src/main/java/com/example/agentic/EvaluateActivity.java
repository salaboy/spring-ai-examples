package com.example.agentic;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class EvaluateActivity implements WorkflowActivity {

  private final ChatClient chatClient;

  public static final String DEFAULT_EVALUATOR_PROMPT = """
			Evaluate this code implementation for correctness, time complexity, and best practices.
			Ensure the code have proper javadoc documentation.
			Respond with EXACTLY this JSON format on a single line:

			{"evaluation":"PASS, NEEDS_IMPROVEMENT, or FAIL", "feedback":"Your feedback here"}

			The evaluation field must be one of: "PASS", "NEEDS_IMPROVEMENT", "FAIL"
			Use "PASS" only if all criteria are met with no improvements needed.
			""";

  public EvaluateActivity(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public Object run(WorkflowActivityContext workflowActivityContext) {

    EvaluatorOptimizerWorkflow.ActivityInput input = workflowActivityContext.getInput(EvaluatorOptimizerWorkflow.ActivityInput.class);
    EvaluatorOptimizer.EvaluationResponse evaluationResponse = chatClient.prompt()
            .user(u -> u.text("{prompt}\nOriginal task: {task}\nContent to evaluate: {content}")
                    .param("prompt", DEFAULT_EVALUATOR_PROMPT)
                    .param("task", input.task())
                    .param("content", input.context()))
            .call()
            .entity(EvaluatorOptimizer.EvaluationResponse.class);

    System.out.println(String.format("\n=== EVALUATOR OUTPUT ===\nEVALUATION: %s\n\nFEEDBACK: %s\n",
            evaluationResponse.evaluation(), evaluationResponse.feedback()));
    return evaluationResponse;

  }
}
