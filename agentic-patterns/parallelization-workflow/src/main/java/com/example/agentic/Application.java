/* 
* Copyright 2024 - 2024 the original author or authors.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* https://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.agentic;

import java.time.Duration;
import java.util.List;

import io.dapr.spring.workflows.config.EnableDaprWorkflows;
import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.NewWorkflowOptions;
import io.dapr.workflows.client.WorkflowInstanceStatus;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@EnableDaprWorkflows
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	private final String prompt = """
							Analyze how market changes will impact this stakeholder group.
							Provide specific impacts and recommended actions.
							Format with clear sections and priorities.
							""";
	private final List<String> inputs = List.of(
					"""
              Customers:
              - Price sensitive
              - Want better tech
              - Environmental concerns
              """,

					"""
              Employees:
              - Job security worries
              - Need new skills
              - Want clear direction
              """,

					"""
              Investors:
              - Expect growth
              - Want cost control
              - Risk concerns
              """,

					"""
              Suppliers:
              - Capacity constraints
              - Price pressures
              - Tech transitions
              """);


	@Autowired
	private DaprWorkflowClient daprWorkflowClient;

	@Bean
	public CommandLineRunner commandLineRunner() {
		return args -> {
			String workflowInstanceId = daprWorkflowClient.scheduleNewWorkflow(ParallelWorkflow.class,
							new ParallelWorkflow.WorkflowInput(prompt, inputs));
			WorkflowInstanceStatus workflowInstanceStatus = daprWorkflowClient
							.waitForInstanceCompletion(workflowInstanceId, Duration.ofMinutes(10), true);

			List<String> parallelResponse = workflowInstanceStatus.readOutputAs(List.class);
			System.out.println(parallelResponse);

		};
	}

}
