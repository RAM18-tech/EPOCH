package com.dtv.dcp.epoch.processor.satellite.sales;

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
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;

@ExtendWith(MockitoExtension.class)
public class SatelliteSalesInsuranceProcessorTest {
	@InjectMocks
	private SatelliteSalesInsuranceProcessor satelliteSalesInsuranceProcessor;

	@Mock
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

	}

	private static String sale_insurance_fee_request = "acquisition/sale_insurance_fee_request.json";
	private static String sale_insurance_fee_response = "acquisition/sale_insurance_fee_response.json";

	@Test
	public void testGetSatelliteVideoPlanOffers() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

		OfferRequest offerRequest = new DataReader().readFileToObj(sale_insurance_fee_request,
				OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj(sale_insurance_fee_response,
				CTOfferResponse.class);

		when(satelliteCTOffersProcessor.getInsuranceOffers(any(), any())).thenReturn(ctOfferResponse);

		CTOfferResponse finalFeeOffers = satelliteSalesInsuranceProcessor
				.getSatelliteInsuranceOffers(offerRequestWrapper, null);
		assertNotNull(finalFeeOffers);
	}

}