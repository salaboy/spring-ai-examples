package com.example.agentic;

import io.dapr.workflows.WorkflowActivity;
import io.dapr.workflows.WorkflowActivityContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class DetermineRouteActivity implements WorkflowActivity {

  private final ChatClient chatClient;

  public DetermineRouteActivity(ChatClient.Builder chatClientBuilder) {
    this.chatClient = chatClientBuilder.build();
  }

  @Override
  public Object run(WorkflowActivityContext ctx) {

    RoutingRequest routingRequest = ctx.getInput(RoutingRequest.class);
    System.out.println("\nAvailable routes: " + routingRequest.availableRoutes());

    String selectorPrompt = String.format("""
                Analyze the input and select the most appropriate support team from these options: %s
                First explain your reasoning, then provide your selection in this JSON format:

                \\{
                    "reasoning": "Brief explanation of why this ticket should be routed to a specific team.
                                Consider key terms, user intent, and urgency level.",
                    "selection": "The chosen team name"
                \\}

                Input: %s""", routingRequest.availableRoutes(), routingRequest.input());

    RoutingResponse routingResponse = chatClient.prompt(selectorPrompt).call().entity(RoutingResponse.class);

    System.out.println(String.format("Routing Analysis:%s\nSelected route: %s",
            routingResponse.reasoning(), routingResponse.selection()));

    return routingResponse.selection();
  }
}
