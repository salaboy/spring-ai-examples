package com.example.agentic;

import io.dapr.workflows.Workflow;
import io.dapr.workflows.WorkflowStub;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class RoutingWorkflow implements Workflow {

  Map<String, String> supportRoutes = Map.of("billing",
          """
              You are a billing support specialist. Follow these guidelines:
              1. Always start with "Billing Support Response:"
              2. First acknowledge the specific billing issue
              3. Explain any charges or discrepancies clearly
              4. List concrete next steps with timeline
              5. End with payment options if relevant

              Keep responses professional but friendly.

              Input: """,

          "technical",
          """
              You are a technical support engineer. Follow these guidelines:
              1. Always start with "Technical Support Response:"
              2. List exact steps to resolve the issue
              3. Include system requirements if relevant
              4. Provide workarounds for common problems
              5. End with escalation path if needed

              Use clear, numbered steps and technical details.

              Input: """,

          "account",
          """
              You are an account security specialist. Follow these guidelines:
              1. Always start with "Account Support Response:"
              2. Prioritize account security and verification
              3. Provide clear steps for account recovery/changes
              4. Include security tips and warnings
              5. Set clear expectations for resolution time

              Maintain a serious, security-focused tone.

              Input: """,

          "product",
          """
              You are a product specialist. Follow these guidelines:
              1. Always start with "Product Support Response:"
              2. Focus on feature education and best practices
              3. Include specific examples of usage
              4. Link to relevant documentation sections
              5. Suggest related features that might help

              Be educational and encouraging in tone.

              Input: """);

  @Override
  public WorkflowStub create() {
    return ctx -> {
      ctx.getLogger().info("Starting Workflow: {}", ctx.getName());

      List<String> inputs = ctx.getInput(List.class);

      Assert.notNull(inputs, "Input text cannot be null");
      Assert.notEmpty(supportRoutes, "Routes map cannot be null or empty");

      // Determine the appropriate route for the input
      //String routeKey = determineRoute(input, routes.keySet());

      List<String> contents = new ArrayList<>();
      for(String input : inputs) {

        String routeKey = ctx.callActivity(DetermineRouteActivity.class.getName(), new RoutingRequest(input, supportRoutes.keySet()), String.class).await();

        // Get the selected prompt from the routes map
        String selectedPrompt = supportRoutes.get(routeKey);

        if (selectedPrompt == null) {
          throw new IllegalArgumentException("Selected route '" + routeKey + "' not found in routes map");
        }
        
        // Process the input with the selected prompt
        //return chatClient.prompt(selectedPrompt + "\nInput: " + input).call().content();
        String content = ctx.callActivity(PromptActivity.class.getName(), new PromptRequest(selectedPrompt, input), String.class).await();
        contents.add(content);
      }

      ctx.complete(contents);
    };
  }


}
