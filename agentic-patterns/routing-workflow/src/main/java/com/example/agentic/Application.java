
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
import io.dapr.workflows.client.WorkflowInstanceStatus;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// ------------------------------------------------------------
// ROUTER WORKFLOW
// ------------------------------------------------------------

@EnableDaprWorkflows
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}


	@Autowired
	private DaprWorkflowClient daprWorkflowClient;

	@Bean
	public CommandLineRunner commandLineRunner(ChatClient.Builder chatClientBuilder) {
		
		return args -> {

			List<String> tickets = List.of(
							"""
                  Subject: Can't access my account
                  Message: Hi, I've been trying to log in for the past hour but keep getting an 'invalid password' error.
                  I'm sure I'm using the right password. Can you help me regain access? This is urgent as I need to
                  submit a report by end of day.
                  - John""",

							"""
                  Subject: Unexpected charge on my card
                  Message: Hello, I just noticed a charge of .99 on my credit card from your company, but I thought
                  I was on the .99 plan. Can you explain this charge and adjust it if it's a mistake?
                  Thanks,
                  Sarah""",

							"""
                  Subject: How to export data?
                  Message: I need to export all my project data to Excel. I've looked through the docs but can't
                  figure out how to do a bulk export. Is this possible? If so, could you walk me through the steps?
                  Best regards,
                  Mike""");


			String instanceId = daprWorkflowClient.scheduleNewWorkflow(RoutingWorkflow.class, tickets);


			WorkflowInstanceStatus workflowInstanceStatus = daprWorkflowClient.waitForInstanceCompletion(instanceId, Duration.ofMinutes(10), true);

			List<String> ticketsResponse = workflowInstanceStatus.readOutputAs(List.class);

			int i = 1;
			for (String ticket : tickets) {
				System.out.println("\nTicket " + i);
				System.out.println("------------------------------------------------------------");
				System.out.println(ticket);
				System.out.println("------------------------------------------------------------");
				System.out.println(ticketsResponse.get(i-1));
				i++;
			}

		};
	}
}
