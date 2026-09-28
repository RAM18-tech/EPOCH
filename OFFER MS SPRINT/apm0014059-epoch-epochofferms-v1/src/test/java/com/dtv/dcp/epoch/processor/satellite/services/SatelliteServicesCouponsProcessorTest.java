package com.dtv.dcp.epoch.processor.satellite.services;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopUCCClient;
import com.dtv.dcp.epoch.model.common.request.CouponsRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;

@ExtendWith(MockitoExtension.class)
public class SatelliteServicesCouponsProcessorTest {
	
	@InjectMocks
	SatelliteServicesCouponsProcessor satelliteServicesCouponsProcessor;
	
	@Mock
	CpopUCCClient cpopUCCClient;
	
	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	
	}
	
	@Test
	public void testGetCoupons() throws Exception {
		
		CouponsRequestWrapper couponsRequestWrapper = new CouponsRequestWrapper();
		couponsRequestWrapper.setCode("TEST");
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",CTCouponResponse.class);

		doReturn(couponResponseData).when(cpopUCCClient).getCoupons( Mockito.any());

		CTCouponResponse response = satelliteServicesCouponsProcessor.getCoupons(couponsRequestWrapper);	
		assertNotNull(response);
	}


}
