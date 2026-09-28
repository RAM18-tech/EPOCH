package com.dtv.dcp.epoch.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DroolsConfiguration {

	private static Logger log = LoggerFactory.getLogger(DroolsConfiguration.class);
	
	private final KieServices kieServices = KieServices.Factory.get();

	/**
	 * Configures and provides a new KieContainer instance specifically for validating carts.
	 * This method sets up a new KieFileSystem, writes the "ValidateCart.drl" Drools rule file into it,
	 * builds all components with a KieBuilder, and finally creates a KieContainer from the built KieModule.
	 * This KieContainer is configured to handle cart validation logic defined in the "ValidateCart.drl" file.
	 *
	 * @return KieContainer The configured KieContainer instance for cart validation.
	 */

	@Bean
	public KieContainer getKieContainerValidate() {
		log.debug("getKieContainer() called to get the KieContainer bean");
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		kieFileSystem.write(ResourceFactory.newClassPathResource("ValidateCart.drl"));
		KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
		kb.buildAll();
		KieModule kieModule = kb.getKieModule();
		return kieServices.newKieContainer(kieModule.getReleaseId());
	}

	@Bean
	public KieContainer getKieContainerForValidateCartRules() {
		log.debug("getKieContainerForValidateCartRules() start");
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		kieFileSystem.write(ResourceFactory.newClassPathResource("ValidateCartRules.drl"));
		KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
		kb.buildAll();
		KieModule kieModule = kb.getKieModule();
		log.debug("getKieContainerForValidateCartRules() end");
		return kieServices.newKieContainer(kieModule.getReleaseId());
	}

    @Bean
    public KieContainer getKieContainerForValidateCartSalesRules() {
        log.debug("getKieContainerForValidateCartSalesRules() start");
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource("ValidateCartSalesRules.drl"));
        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();
        log.debug("getKieContainerForValidateCartSalesRules() end");
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

    @Bean
    public KieContainer getKieContainerForValidateCartServicesRules() {
        log.debug("getKieContainerForValidateCartServicesRules() start");
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource("ValidateCartServicesRules.drl"));
        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();
        log.debug("getKieContainerForValidateCartServicesRules() end");
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

    @Bean
    public KieContainer getKieContainerForValidateCartSLSRules() {
        log.debug("getKieContainerForValidateCartSLSRules() start");
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource("ValidateCartSLSRules.drl"));
        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();
        log.debug("getKieContainerForValidateCartSLSRules() end");
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

    @Bean
    public KieContainer getKieContainerForLocalsValidateCart() {
        log.debug("getKieContainerForLocalsValidateCart() start");
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource("LocalsValidateCart.drl"));
        KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
        kb.buildAll();
        KieModule kieModule = kb.getKieModule();
        log.debug("getKieContainerForLocalsValidateCart() end");
        return kieServices.newKieContainer(kieModule.getReleaseId());
    }
}