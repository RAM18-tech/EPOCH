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
public class SatelliteSalesVideoDeviceProcessorTest {

	@InjectMocks
	private SatelliteSalesVideoDeviceProcessor satelliteSalesVideoDeviceProcessor;

	@Mock
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

	}

	private static String enabler_sales_getOffers_device_request = "acquisition/enabler_sales_getOffers_device_request.json";
	private static String getOffers_sales_video_device_response = "acquisition/getOffers_sales_video_device_response.json";
	

	@Test
	public void testGetSatelliteVideoPlanOffers() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

		OfferRequest offerRequest = new DataReader().readFileToObj(enabler_sales_getOffers_device_request,
				OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj(getOffers_sales_video_device_response,
				CTOfferResponse.class);

		when(satelliteCTOffersProcessor.getVideoDeviceOffers(any(), any())).thenReturn(ctOfferResponse);

		CTOfferResponse finalFeeOffers = satelliteSalesVideoDeviceProcessor
				.getSatelliteVideoDeviceOffers(offerRequestWrapper, null);
		assertNotNull(finalFeeOffers);
	}

}