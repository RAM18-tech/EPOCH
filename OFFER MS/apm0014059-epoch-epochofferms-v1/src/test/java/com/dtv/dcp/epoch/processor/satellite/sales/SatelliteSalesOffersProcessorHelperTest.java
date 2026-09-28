package com.dtv.dcp.epoch.processor.satellite.sales;

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
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.sales.SalesVideoPlanProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteRetentionOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;

/**
 * @author ap778g
 *
 */
@ExtendWith(MockitoExtension.class)
public class SatelliteSalesOffersProcessorHelperTest {
	
	@InjectMocks
	private SatelliteRetentionOffersProcessor satelliteRetentionOffersProcessor;
	
	@Mock
	SalesVideoAddonProcessor salesVideoAddonProcessor;

	@Mock
	private SatelliteSalesOffersProcessorHelper satelliteSalesOffersProcessorHelper;
	

	@Mock
	OffersUtils offersUtils;
	@Mock
	SalesVideoPlanProcessor salesVideoPlanProcessor;

	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
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
		List<CTOffer> finalOfferList = new ArrayList<>();
		
		ctOffer.setEndDate("2020-02-23");
		ctOffer.setStartDate("2019-02-23");
		ctOffer.setAttributes(attributes);
		ctOffer.setCode("code");
		
		
		finalOfferList.add(ctOffer);
		CTOfferResponse videoAddonOffer = new CTOfferResponse();	
		videoAddonOffer.setOffers(finalOfferList);
		when(salesVideoAddonProcessor.getSatelliteVideoAddonOffers(offerRequestWrapper)).thenReturn(videoAddonOffer);
		CTOfferResponse offersResponse = satelliteRetentionOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
	}
	
	@Test
	public void testGetOffers(){		
		OfferRequestWrapper offerRequestWrapper= new OfferRequestWrapper();		
		OfferRequest offerRequest= new OfferRequest();
		List<String> offerProductTypes= new ArrayList<>();
		offerProductTypes.add(Constants.BASE);
		offerRequest.setOfferProductType(offerProductTypes);		
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList= new ArrayList<>();
		CTOffer ctOffer=new CTOffer();
		OfferAttributes attributes=new OfferAttributes();
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
	public void test1RetentionDowngradeSaveOfferProcessing() {
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

}