package com.dtv.dcp.epoch.processor.satellite.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.sales.SalesVideoPlanProcessor;
import com.dtv.dcp.epoch.processor.satellite.sales.SatelliteSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.processor.satellite.sales.SatelliteSalesVideoPlanProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;

/**
 * @author ap778g
 *
 */
@ExtendWith(MockitoExtension.class)
public class SatelliteRetentionOffersProcessorTest {
	@InjectMocks
	private SatelliteRetentionOffersProcessor satelliteRetentionOffersProcessor;

	@Mock
	CpopClient cpopClient;

	@Mock
	SalesVideoPlanProcessor salesVideoPlanProcessor;

	@Mock
	SalesVideoAddonProcessor salesVideoAddonProcessor;

	@Mock
	OffersUtils util;
	
	@Mock
	SatelliteSalesVideoPlanProcessor satelliteSalesVideoPlanProcessor;
	
	@Mock
	SatelliteSalesOffersProcessorHelper satelliteSalesOffersProcessorHelper;
	
	private static final String OFFER_REQUEST_RETENTION_DISPUTE = "stms/offer_request_retention_dispute.json";
	private static final String RETENTION_DISPUTE_OFFERS = "stms/retention_dispute_offers.json";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testGetOffers() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.BASE);
		offerRequest.setOfferProductType(offerProductTypes);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList = new ArrayList<>();
		CTOffer ctOffer = new CTOffer();
		OfferAttributes attributes = new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		ctOffer.setAttributes(attributes);
		responseList.add(ctOffer);
		ctOfferResponse.setOffers(responseList);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse offersResponse = satelliteRetentionOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
	}

	@Test
	public void testPremiumGetOffers() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		OfferAttributes attributes = new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		CTOffer ctOffer = new CTOffer();
		ctOffer.setAttributes(attributes);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList = new ArrayList<>();
		responseList.add(ctOffer);
		ctOfferResponse.setOffers(responseList);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse offersResponse = satelliteRetentionOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
	}

	@Test
	public void testRetentionDowngradeSaveOfferProcessing() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		OfferAttributes attributes = new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		CTOffer ctOffer = new CTOffer();
		ctOffer.setAttributes(attributes);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList = new ArrayList<>();
		responseList.add(ctOffer);
		ctOfferResponse.setOffers(responseList);
		offerRequestWrapper.setOfferRequest(offerRequest);		
		CTOfferResponse videoAddonOffer = null;
		when(salesVideoAddonProcessor.getSatelliteVideoAddonOffers(offerRequestWrapper)).thenReturn(videoAddonOffer);
		CTOfferResponse offersResponse = satelliteRetentionOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
	}
	
	@Test
	public void testRetentionDisputeGetOffers() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_RETENTION_DISPUTE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(RETENTION_DISPUTE_OFFERS, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(satelliteSalesVideoPlanProcessor.getSatelliteVideoPlanOffersForRetention(offerRequestWrapper)).thenReturn(ctOfferResponse);
        when(satelliteSalesOffersProcessorHelper.retentionDowngradeSaveOfferProcessing(ctOfferResponse.getOffers(), offerRequestWrapper)).thenCallRealMethod();
        CTOfferResponse retentionOffers = satelliteRetentionOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(retentionOffers);
        assertEquals(14, retentionOffers.getOffers().size());
	}

}
