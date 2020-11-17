package com.warenversand.workflow;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class HandlerConfiguration {
	private final Logger logger = LoggerFactory.getLogger(HandlerConfiguration.class);

	@Bean
	public void createTopicSubscriberHandler() {
		ExponentialBackoffStrategy fetchTimer = new ExponentialBackoffStrategy(500L, 2, 500L);
		int maxTasksToFetchWithinOnRequest = 1;
		
		ExternalTaskClient externalTaskClient = ExternalTaskClient
				.create()
				.baseUrl("http://localhost:8080/engine-rest")
				.maxTasks(3).backoffStrategy(fetchTimer)
				.build();
		
		externalTaskClient
		 .subscribe("Versicherungsbedarf_prüfen")
		 .handler((externalTask, externalTaskService) -> {
			
			try {
				int moneyValue = externalTask.getVariable("moneyValue");
				
				 Map<String, Object> variables = new HashMap<>();
			  
				
				if(moneyValue >= 10000) {	
					variables.put("insureOrder", true);
				logger.info("moneyValue:" +moneyValue+ "insureOrder:" +true);
				externalTaskService.complete(externalTask, variables);
				}else {
					
					variables.put("insureOrder", false);
					logger.info("moneyValue:" +moneyValue+ "insureOrder:" +false);
					externalTaskService.complete(externalTask, variables );
				}
			}catch(Exception e) {
				externalTaskService.handleBpmnError(externalTask, externalTask.getId(), "Something went wrong!" +e);
			}
		}).open();
	}
}
