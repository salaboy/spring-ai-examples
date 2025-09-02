package com.example.agentic;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class GenerateActivity implements WorkflowActivity {

  private final ChatClient chatClient;

  public static final String DEFAULT_GENERATOR_PROMPT = """
			Your goal is to complete the task based on the input. If there are feedback
			from your previous generations, you should reflect on them to improve your solution.

			CRITICAL: Your response must be a SINGLE LINE of valid JSON with NO LINE BREAKS except those explicitly escaped with \\n.
			Here is the exact format to follow, including all quotes and braces:

			{"thoughts":"Brief description here","response":"public class Example {\\n    // Code here\\n}"}

			Rules for the response field:
			1. ALL line breaks must use \\n
			2. ALL quotes must use \\"
			3. ALL backslashes must be doubled: \\
			4. NO actual line breaks or formatting - everything on one line
			5. NO tabs or special characters
			6. Java code must be complete and properly escaped

			Example of properly formatted response:
			{"thoughts":"Implementing counter","response":"public class Counter {\\n    private int count;\\n    public Counter() {\\n        count = 0;\\n    }\\n    public void increment() {\\n        count++;\\n    }\\n}"}

			Follow this format EXACTLY - your response must be valid JSON on a single line.
			""";

  public GenerateActivity(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public Object run(WorkflowActivityContext workflowActivityContext) {

    EvaluatorOptimizerWorkflow.ActivityInput input = workflowActivityContext.getInput(EvaluatorOptimizerWorkflow.ActivityInput.class);
    EvaluatorOptimizer.Generation generationResponse = chatClient.prompt()
            .user(u -> u.text("{prompt}\n{context}\nTask: {task}")
                    .param("prompt", DEFAULT_GENERATOR_PROMPT)
                    .param("context", input.context())
                    .param("task", input.task()))
            .call()
            .entity(EvaluatorOptimizer.Generation.class);

    System.out.println(String.format("\n=== GENERATOR OUTPUT ===\nTHOUGHTS: %s\n\nRESPONSE:\n %s\n",
            generationResponse.thoughts(), generationResponse.response()));
    return generationResponse;

  }
}
