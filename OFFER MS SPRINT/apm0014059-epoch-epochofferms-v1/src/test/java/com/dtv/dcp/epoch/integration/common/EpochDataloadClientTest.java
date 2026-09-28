package com.dtv.dcp.epoch.integration.common;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.integration.EpochDataloadClient;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class EpochDataloadClientTest {

    @InjectMocks
    EpochDataloadClient epochDataloadClient;
    
    @Mock
    BaseRestClient baseRestClient;
    
    RestTemplate restTemplate;
    
    RestTemplateBeanFactory restTemplateBeanFactory;
    
    private static final String BENEFIT_RESPONSE = "BenefitDetailsResponse.json";

    /**
     * Setup.
     */
	@BeforeEach
    public void init() throws Exception {
        restTemplateBeanFactory = Mockito.mock(RestTemplateBeanFactory.class);
        restTemplate = Mockito.mock(RestTemplate.class);
        MockitoAnnotations.openMocks(this);
        try {
            ReflectionTestUtils.setField(epochDataloadClient, "baseUrl", "https://url.att.com/");
        } catch (Exception e) {
        }
    }

    @Test
    public void testGetSatelliteBenefits() {
        CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
        ctBenefitsRequest.setState("staged");
        ctBenefitsRequest.setBenefitCodes(Arrays.asList("123456"));
        Mockito.when(restTemplate.getForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any())).thenReturn(returnBenefitsResponse());
        CTBenefitsResponse ctBenefitsResponse = epochDataloadClient.getSatelliteBenefits(ctBenefitsRequest);
        assertNotNull(ctBenefitsResponse);

    }

    public ResponseEntity returnBenefitsResponse() {
    	ObjectMapper objectMapper = new ObjectMapper();
    	CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
        JsonNode jsonNode = null;
        try {
        	ctBenefitsResponse = new DataReader().readFileToObj(BENEFIT_RESPONSE, CTBenefitsResponse.class);
            jsonNode = objectMapper.convertValue(ctBenefitsResponse, JsonNode.class);
        } catch (Exception e) {
        }

        HttpHeaders responseHeaders = new HttpHeaders();
        return new ResponseEntity<>(jsonNode, responseHeaders, HttpStatus.OK);
    }
    
}
