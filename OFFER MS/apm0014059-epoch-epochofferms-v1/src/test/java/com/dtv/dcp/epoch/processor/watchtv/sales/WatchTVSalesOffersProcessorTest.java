package com.dtv.dcp.epoch.processor.watchtv.sales;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPBenefitsHelper;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;
import com.dtv.dcp.epoch.util.TestUtility;

public class WatchTVSalesOffersProcessorTest{
	@InjectMocks
	private WatchTVSalesOffersProcessor watchTVSalesOffersProcessor;
	
	@Mock
	CpopClient cpopClient;
	
	@Mock
	SalesVideoAddonProcessor salesVideoAddonProcessor;
	
	@Mock
	WatchTVSalesOffersProcessorHelper watchTVSalesOffersProcessorHelper;
	
	@Mock
    CPOPProductsHelper cpopProductsHelper;
	
	@Mock
    CPOPBenefitsHelper cpopBenefitsHelper;

	@Mock
	FeatureManagerHelper featureHelper;

	private static final String VIDEO_ADDON = "video-addon";

	private static final String WATCHTV = "OTT";

	private static final String OPUS = "opus";
	
	private static final String ONLINE = "online";

	private static final String RESIDENTIAL = "Residential";

	private static final String ACQUISITION = "Acquisition";
	
	private static final String INTERNATIONAL = "International";

	private static final String STANDALONE = "Standalone";
	
	private static final String[] ADDON_PLANSUBTYPE = new String[] { "cDVR", "Cross-Product-Mobility", "Streams","CrossProduct-WatchTV", "Cross-product", INTERNATIONAL };

	private static final String[] ADDON_TYPE = new String[] { "Programming-Bolt-on", "Bolt-on", STANDALONE };	

//	*//**
//	 * Setup.
//	 *//*
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		doReturn(true).when(featureHelper).isEnabled(Mockito.anyString());
	
	}	
	
	@SuppressWarnings("deprecation")
	@Test
	public void testGetOffersAllVideoAddOns() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(WATCHTV));
		offerRequestWrapper.setOfferRequest(offerRequest);

		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/VideoAddonCTOfferResponse.json"),CTOfferResponse.class);
		doReturn(ctOfferResponse).when(salesVideoAddonProcessor)
				.getVideoAddonOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any());
        doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		
		CTOfferResponse offersResponse = watchTVSalesOffersProcessor.getOffers(offerRequestWrapper);

		assertNotNull(offersResponse);
		assertEquals(26, offersResponse.getOffers().size());
	}
	@SuppressWarnings("deprecation")
	@Test
	public void testGetOffersVideoAddOns() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(WATCHTV));
		offerRequest.setAddOnType(Arrays.asList(ADDON_TYPE));
		offerRequest.setPlanSubType(Arrays.asList(ADDON_PLANSUBTYPE));
		offerRequestWrapper.setOfferRequest(offerRequest);

		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/VideoAddonCTOfferResponse.json"),CTOfferResponse.class);

		doReturn(ctOfferResponse).when(salesVideoAddonProcessor)
				.getVideoAddonOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any());
        doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		
		CTOfferResponse offersResponse = watchTVSalesOffersProcessor.getOffers(offerRequestWrapper);

		assertNotNull(offersResponse);
		assertEquals(26, offersResponse.getOffers().size());
	}
	@Test
	public void testGetOffersAllVideoAddOnsForOnline() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(ONLINE));
		offerRequest.setOfferProductFamily(Arrays.asList(WATCHTV));
		offerRequestWrapper.setOfferRequest(offerRequest);

		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/VideoAddonCTOfferResponse.json"),CTOfferResponse.class);
		doReturn(ctOfferResponse).when(salesVideoAddonProcessor)
				.getWatchTVVideoAddonOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any());
        doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		
		CTOfferResponse offersResponse = watchTVSalesOffersProcessor.getOffers(offerRequestWrapper);

		assertNotNull(offersResponse);
		assertEquals(26, offersResponse.getOffers().size());
	}
}
