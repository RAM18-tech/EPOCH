package com.dtv.dcp.epoch.service.satellite;

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
import com.dtv.dcp.epoch.model.common.request.CouponsRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesCouponsProcessor;

@ExtendWith(MockitoExtension.class)
public class SatelliteCouponsServicesImplTest {
	
	@InjectMocks
	SatelliteCouponsServicesImpl satelliteCouponsServicesImpl;
	
	@Mock
    SatelliteServicesCouponsProcessor satelliteServicesCouponsProcessor;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);	
	}

	@Test
	public void testGetCoupons() {
		CTCouponResponse ctCouponResponse = new DataReader().readFileToObj("CTCouponResponse.json",CTCouponResponse.class);
		CouponsRequestWrapper couponsRequestWrapper = new CouponsRequestWrapper();
		doReturn(ctCouponResponse).when(satelliteServicesCouponsProcessor).getCoupons(Mockito.any());
		assertNotNull(satelliteCouponsServicesImpl.getCoupons(couponsRequestWrapper));
		
	}

}
