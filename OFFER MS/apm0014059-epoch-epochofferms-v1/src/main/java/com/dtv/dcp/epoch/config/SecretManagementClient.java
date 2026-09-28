
package com.dtv.dcp.epoch.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Component("SecretManagementClient")
public class SecretManagementClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecretManagementClient.class);
	
	@Value("${secret.file.name}")
	private String secretName;
	
	public Map<String, String> retrieveCredentials() {
		LOGGER.info("Inside download Feed files method");
		Map<String, String> resultMap = new HashMap<String, String>();
		Region region = Region.of("us-east-1");
		try {
			// Create a Secrets Manager client
			SecretsManagerClient client = SecretsManagerClient.builder().region(region).build();
			GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
			GetSecretValueResponse getSecretValueResponse;
			getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
			String secret = getSecretValueResponse.secretString();
			ObjectMapper mapper = new ObjectMapper();
			
			if (secret.trim().startsWith("[")) {
				// It's an array
				List<Map<String, String>> list = mapper.readValue(secret,
						new TypeReference<List<Map<String, String>>>() {
						});
				resultMap = list.get(0); // or merge as needed
			} else {
				// It's a single object
				resultMap = mapper.readValue(secret, new TypeReference<Map<String, String>>() {
				});
			}			
		} catch (Exception e) {
			LOGGER.error("Exception retrieveValues [{}]", e.getMessage());
		}
		return resultMap;
	}
}