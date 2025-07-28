package com.example.agentic;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import org.springframework.stereotype.Component;

@Component
public class ChainWorkflow implements Workflow {
  /**
   * Array of system prompts that define the transformation steps in the chain.
   * Each prompt acts as a gate that validates and transforms the output before
   * proceeding to the next step.
   */
  private static final String[] DEFAULT_SYSTEM_PROMPTS = {

          // Step 1
          """
					Extract only the numerical values and their associated metrics from the text.
					Format each as'value: metric' on a new line.
					Example format:
					92: customer satisfaction
					45%: revenue growth""",
          // Step 2
          """
					Convert all numerical values to percentages where possible.
					If not a percentage or points, convert to decimal (e.g., 92 points -> 92%).
					Keep one number per line.
					Example format:
					92%: customer satisfaction
					45%: revenue growth""",
          // Step 3
          """
					Sort all lines in descending order by numerical value.
					Keep the format 'value: metric' on each line.
					Example:
					92%: customer satisfaction
					87%: employee satisfaction""",
          // Step 4
          """
					Format the sorted data as a markdown table with columns:
					| Metric | Value |
					|:--|--:|
					| Customer Satisfaction | 92% | """
  };



  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("Starting Workflow: {}", ctx.getName());

      String userInput = ctx.getInput(String.class);
      int step = 0;
      String response = userInput;
      ctx.getLogger().info(String.format("\nSTEP %s:\n %s", step++, response));

      for (String prompt : DEFAULT_SYSTEM_PROMPTS) {

        // 1. Compose the input using the response from the previous step.
        String input = String.format("{%s}\n {%s}", prompt, response);

        // 2. Call the PromptActivity with the new input and get the new response.
        response = ctx.callActivity(PromptActivity.class.getName(), input, String.class).await();

        ctx.getLogger().info(String.format("\nSTEP %s:\n %s", step++, response));

      }

      ctx.getLogger().info("Workflow finished with result: {}", response);
      ctx.complete(response);
    };
  }
}
