/*
 * Copyright 2025 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
limitations under the License.
*/

package com.example.agentic;

import io.dapr.testcontainers.*;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;

import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.List;

@TestConfiguration()
public class DaprTestContainersConfig {

  @Bean
  GenericContainer zipkinContainer(Network network) {
    GenericContainer zipkinContainer = new GenericContainer(DockerImageName.parse("openzipkin/zipkin:latest"))
            .withNetwork(network)
            .withExposedPorts(9411)
            .withNetworkAliases("zipkin");
    return zipkinContainer;
  }


  @Bean
  @ServiceConnection
  public DaprContainer daprContainer(Network daprNetwork, GenericContainer zipkinContainer) {

    DockerImageName myDaprImage = DockerImageName.parse("daprio/daprd:1.15.7");
    return new DaprContainer(myDaprImage)
            .withAppName("agentic")
            .withNetwork(daprNetwork)
            .withComponent(new Component("kvstore", "state.in-memory", "v1",
                    Collections.singletonMap("actorStateStore", "true")))

            .withConfiguration(new Configuration("daprConfig",
                    new TracingConfigurationSettings("1", true, null,
                            new ZipkinTracingConfigurationSettings("http://zipkin:9411/api/v2/spans")), null))

            //Uncomment if you want to troubleshoot Dapr related problems
//            .withDaprLogLevel(DaprLogLevel.DEBUG)
//            .withLogConsumer(outputFrame -> System.out.println(outputFrame.getUtf8String()))
            .dependsOn(zipkinContainer);

  }

  @Bean
  public Network getDaprNetwork(Environment env) {
    boolean reuse = env.getProperty("reuse", Boolean.class, false);
    if (reuse) {
      Network defaultDaprNetwork = new Network() {
        @Override
        public String getId() {
          return "dapr-network";
        }

        @Override
        public void close() {

        }

        @Override
        public Statement apply(Statement base, Description description) {
          return null;
        }
      };

      List<com.github.dockerjava.api.model.Network> networks = DockerClientFactory.instance().client().listNetworksCmd()
              .withNameFilter("dapr-network").exec();
      if (networks.isEmpty()) {
        Network.builder().createNetworkCmdModifier(cmd -> cmd.withName("dapr-network")).build().getId();
        return defaultDaprNetwork;
      } else {
        return defaultDaprNetwork;
      }
    } else {
      return Network.newNetwork();
    }
  }


}
