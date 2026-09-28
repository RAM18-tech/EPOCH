package com.dtv.dcp.epoch.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
class DroolsConfigurationTest {

    @Mock
    KieServices kieServices;

    @Mock
    KieFileSystem kieFileSystem;

    @Mock
    KieBuilder kieBuilder;

    @Mock
    KieContainer kieContainer;

    @InjectMocks
    DroolsConfiguration droolsConfiguration;

    @BeforeEach
    void setUp() throws Exception {
        initializeMocks();
    }

    private void initializeMocks() throws Exception {
        reset(kieServices); // Ensure mocks are reset before each test
        clearInvocations(kieServices); // Clear any previous invocations
        Field kieServicesField = DroolsConfiguration.class.getDeclaredField("kieServices");
        kieServicesField.setAccessible(true);
        kieServicesField.set(droolsConfiguration, kieServices);
        lenient().when(kieServices.newKieFileSystem()).thenReturn(kieFileSystem);
        lenient().when(kieServices.newKieBuilder(any(KieFileSystem.class))).thenReturn(kieBuilder);
        lenient().when(kieServices.newKieContainer(any())).thenReturn(kieContainer);

        // Mock the KieModule
        KieModule kieModule = mock(KieModule.class);
        lenient().when(kieBuilder.getKieModule()).thenReturn(kieModule);
    }


    private void testKieContainerWithResource(String resource, Executable executable) throws Exception {
        kieFileSystem.write(ResourceFactory.newClassPathResource(resource));
        assertNotNull(executable.execute());
    }

    @Test
    void testGetKieContainerFailures() {
        testKieContainerFailure(() -> droolsConfiguration.getKieContainerForValidateCartServicesRules());
        testKieContainerFailure(() -> droolsConfiguration.getKieContainerForValidateCartSalesRules());
        testKieContainerFailure(() -> droolsConfiguration.getKieContainerValidate());
        testKieContainerFailure(() -> droolsConfiguration.getKieContainerForValidateCartRules());
    }

    private void testKieContainerFailure(Executable executable) {
        doThrow(new RuntimeException("KieFileSystem creation failed")).when(kieServices).newKieFileSystem();
        assertThrows(RuntimeException.class, executable::execute);
    }

    @Test
    void testGetKieContainerWithNullKieServices() throws Exception {
        testKieContainerWithNullKieServices(() -> droolsConfiguration.getKieContainerForValidateCartSalesRules());
        testKieContainerWithNullKieServices(() -> droolsConfiguration.getKieContainerForValidateCartServicesRules());
        testKieContainerWithNullKieServices(() -> droolsConfiguration.getKieContainerValidate());
        testKieContainerWithNullKieServices(() -> droolsConfiguration.getKieContainerForValidateCartRules());
    }

    private void testKieContainerWithNullKieServices(Executable executable) throws Exception {
        Field kieServicesField = DroolsConfiguration.class.getDeclaredField("kieServices");
        kieServicesField.setAccessible(true);
        kieServicesField.set(droolsConfiguration, null);
        assertThrows(NullPointerException.class, executable::execute);
    }

    @Test
    void testGetKieContainerWithInvalidResource() {
        testKieContainerWithInvalidResource(() -> droolsConfiguration.getKieContainerForValidateCartSalesRules());
        testKieContainerWithInvalidResource(() -> droolsConfiguration.getKieContainerForValidateCartServicesRules());
        testKieContainerWithInvalidResource(() -> droolsConfiguration.getKieContainerValidate());
    }

    private void testKieContainerWithInvalidResource(Executable executable) {
        when(kieServices.newKieFileSystem()).thenReturn(kieFileSystem);
        doThrow(new RuntimeException("Invalid resource")).when(kieFileSystem).write(any());
        assertThrows(RuntimeException.class, executable::execute);
    }

    @FunctionalInterface
    private interface Executable {
        Object execute() throws Exception;
    }
}