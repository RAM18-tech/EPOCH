package com.dtv.dcp.epoch;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

import com.dtv.dcp.epoch.util.PropertiesUtil;
import com.dtv.dcp.dpd.SecretsManagementImpl;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class,
		SecurityAutoConfiguration.class,  ManagementWebSecurityAutoConfiguration.class})
@EnableCaching
@EnableConfigurationProperties({PropertiesUtil.class})
public class Application {
	
	@Value("${secrets.global}")
	private String secretsGlobalPath;
	
	@Value("${secrets.environment}")
	private String secretsEnvironmentPath;
	
	@Value("${secrets.cluster}")
	private String secretsClusterPath;

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);
	}
	
	@PostConstruct
	public void getSecretsValue() {
		SecretsManagementImpl secretsManagement =
				new SecretsManagementImpl(secretsGlobalPath, secretsEnvironmentPath, secretsClusterPath);
	}
}