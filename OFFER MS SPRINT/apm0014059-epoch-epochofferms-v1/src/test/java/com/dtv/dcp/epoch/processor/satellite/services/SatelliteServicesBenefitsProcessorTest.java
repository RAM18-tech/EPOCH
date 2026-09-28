package com.dtv.dcp.epoch.processor.satellite.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.BenefitRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;

@ExtendWith(MockitoExtension.class)
public class SatelliteServicesBenefitsProcessorTest {

	@InjectMocks
	private SatelliteServicesBenefitsProcessor satelliteServicesBenefitsProcessor;

	@Mock
	CpopClient cpopClient;
	
	private static String BENEFIT_REQUEST_STMS = "stms/benefit_request.json";
	
	private static String BENEFIT_REQUEST_ENABLER = "enabler/benefit_request.json";

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetBenefitsStms() {
		BenefitRequest benefitRequest = new BenefitRequest();
		BenefitRequestWrapper benefitRequestWrapper = new BenefitRequestWrapper();
		
		try {
			benefitRequest = new DataReader().readFileToObj(BENEFIT_REQUEST_STMS, BenefitRequest.class);
			benefitRequestWrapper.setBenefitRequest(benefitRequest);
		} catch (Exception e) {
		}
		
		CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("stms/benefit_response.json", CTBenefitsResponse.class);
		
		when(cpopClient.getSatelliteBenefits(any())).thenReturn(ctBenefitsResponse);
		
		CTBenefitsResponse benefitResponse = satelliteServicesBenefitsProcessor.getBenefits(benefitRequestWrapper);
		
		assertNotNull(benefitResponse);
		assertNotNull(benefitResponse.getBenefits());
		assertEquals(1, benefitResponse.getBenefits().size());
	}
	
	@Test
	public void testGetBenefitsEnabler() {
		BenefitRequest benefitRequest = new BenefitRequest();
		BenefitRequestWrapper benefitRequestWrapper = new BenefitRequestWrapper();
		
		try {
			benefitRequest = new DataReader().readFileToObj(BENEFIT_REQUEST_ENABLER, BenefitRequest.class);
			benefitRequestWrapper.setBenefitRequest(benefitRequest);
		} catch (Exception e) {
		}
		
		CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("enabler/benefit_response.json", CTBenefitsResponse.class);
		
		when(cpopClient.getSatelliteBenefits(any())).thenReturn(ctBenefitsResponse);
		
		CTBenefitsResponse benefitResponse = satelliteServicesBenefitsProcessor.getBenefits(benefitRequestWrapper);
		
		assertNotNull(benefitResponse);
		assertNotNull(benefitResponse.getBenefits());
		assertEquals(1, benefitResponse.getBenefits().size());
	}

}
