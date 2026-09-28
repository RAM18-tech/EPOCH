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
public class SatelliteSalesVideoPlanProcessorTest {

	@InjectMocks
	private SatelliteSalesVideoPlanProcessor satelliteSalesVideoPlanProcessor;

	@Mock
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

	}

	private static String offer_request_video_plan = "acquisition/offer_request_video_plan.json";
	private static String offer_response_video_plan = "acquisition/offer_response_video_plan.json";

	@Test
	public void testGetSatelliteVideoPlanOffers() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

		OfferRequest offerRequest = new DataReader().readFileToObj(offer_request_video_plan,
				OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj(offer_response_video_plan,
				CTOfferResponse.class);

		when(satelliteCTOffersProcessor.getVideoPlanOffers(any())).thenReturn(ctOfferResponse);

		CTOfferResponse finalFeeOffers = satelliteSalesVideoPlanProcessor
				.getSatelliteVideoPlanOffers(offerRequestWrapper);
		assertNotNull(finalFeeOffers);
	}

	@Test
	public void testGetSatelliteVideoPlanOffersForRetention() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

		OfferRequest offerRequest = new DataReader().readFileToObj(offer_request_video_plan,
				OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj(offer_response_video_plan,
				CTOfferResponse.class);

		when(satelliteCTOffersProcessor.getVideoPlanOffersForRetention(any())).thenReturn(ctOfferResponse);

		CTOfferResponse finalFeeOffers = satelliteSalesVideoPlanProcessor
				.getSatelliteVideoPlanOffersForRetention(offerRequestWrapper);
		assertNotNull(finalFeeOffers);
	}
}