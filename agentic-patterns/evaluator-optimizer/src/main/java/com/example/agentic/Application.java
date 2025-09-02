
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

import com.example.agentic.EvaluatorOptimizer.RefinedResponse;

import io.dapr.spring.workflows.config.EnableDaprWorkflows;
import io.dapr.workflows.client.DaprWorkflowClient;
import io.dapr.workflows.client.WorkflowInstanceStatus;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

// ------------------------------------------------------------
// EVALUATOR-OPTIMIZER
// ------------------------------------------------------------

@SpringBootApplication
@EnableDaprWorkflows
public class Application {


	@Autowired
	private DaprWorkflowClient daprWorkflowClient;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner() throws TimeoutException {
		return args -> {
			String userInput = """
					<user input>
					Implement a Stack in Java with:
					1. push(x)
					2. pop()
					3. getMin()
					All operations should be O(1).
					All inner fields should be private and when used should be prefixed with 'this.'.
					</user input>
					""";
		EvaluatorOptimizerWorkflow.IteractionContext iteractionContext = new EvaluatorOptimizerWorkflow
						.IteractionContext(userInput, "", new ArrayList<>(), new ArrayList<>());

		String workflowInstanceId = daprWorkflowClient.scheduleNewWorkflow(EvaluatorOptimizerWorkflow.class, iteractionContext);

		WorkflowInstanceStatus workflowInstanceStatus = daprWorkflowClient.waitForInstanceCompletion(workflowInstanceId, Duration.ofMinutes(10), true);

		RefinedResponse refinedResponse = workflowInstanceStatus.readOutputAs(RefinedResponse.class);

		System.out.println("FINAL OUTPUT:\n : " + refinedResponse);
		};

	}
}
