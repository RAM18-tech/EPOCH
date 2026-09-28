package com.dtv.dcp.epoch.processor.ott.sales;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

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
import org.mockito.Spy;
import org.springframework.beans.BeanUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPBenefitsHelper;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;
import com.dtv.dcp.epoch.util.TestUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

class OttSalesOffersProcessorTest{
	@InjectMocks
	@Spy
	private OttSalesOffersProcessor OttSalesOffersProcessor;
	
	@Mock
	CpopClient cpopClient;
	
	@Mock
	SalesVideoPlanProcessor salesVideoPlanProcessor;
	
	@Mock
	SalesVideoAddonProcessor salesVideoAddonProcessor;
	
	@Mock
	SalesEquipmentProcessor salesEquipmentProcessor;
	
	@Mock
	OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;
	
	@Mock
    CPOPProductsHelper cpopProductsHelper;
	
	@Mock
    CPOPBenefitsHelper cpopBenefitsHelper;

	@Mock
	FeatureManagerHelper featureHelper;
	
	@Mock
	OffersUtils offersUtils;

    @InjectMocks
    OffersUtils offersUtilsInjectMock;
	
	@Mock
	DtvnSalesRSNFeeOffersProcessor dtvnSalesRSNFeeOffersProcessor;

	@Mock
	RewardProcessor rewardProcessor;

	@Mock
	ProtectionPlanProcessor protectionPlanProcessor;

	private static final String VIDEO_ADDON = "video-addon";

	private static final String OTT = "OTT";

	private static final String OPUS = "opus";

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
	 void testGetOffersVideoPlan() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.VIDEO_PLAN);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setContractIndicator(Arrays.asList(Constants.EDSP_STRING));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/SalesVideoPlanOfferResponse.json"),CTOfferResponse.class);

        doReturn(ctOfferResponse).when(salesVideoPlanProcessor)
				.getVideoPlanOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.<List<CTOffer>>any(), ArgumentMatchers.<OfferRequestWrapper>any());

		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any(),ArgumentMatchers.any());
		doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);

		assertNotNull(offersResponse);
//		assertEquals(28, offersResponse.getOffers().size());
	}
	@SuppressWarnings("deprecation")
	@Test
	 void testGetOffersStandAloneOffers() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequest.setAddOnType(Arrays.asList(STANDALONE ));
		offerRequest.setPlanSubType(Arrays.asList(INTERNATIONAL));
		offerRequestWrapper.setOfferRequest(offerRequest);

		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/StanaloneCTOfferResponse.json"),CTOfferResponse.class);
		doReturn(ctOfferResponse).when(salesVideoAddonProcessor)
				.getVideoAddonOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.<List<CTOffer>>any(), ArgumentMatchers.<OfferRequestWrapper>any());
		
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any(),ArgumentMatchers.any());
        doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);

		assertNotNull(offersResponse);
		//assertEquals(3, offersResponse.getOffers().size());
	}	
	@SuppressWarnings("deprecation")
	@Test
	void testGetOffersAllVideoAddOns() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);

		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/VideoAddonCTOfferResponse.json"),CTOfferResponse.class);
		doReturn(ctOfferResponse).when(salesVideoAddonProcessor)
				.getVideoAddonOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.<List<CTOffer>>any(), ArgumentMatchers.<OfferRequestWrapper>any());
		
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any(),ArgumentMatchers.any());
        doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);

		assertNotNull(offersResponse);
		//assertEquals(26, offersResponse.getOffers().size());
	}
	@SuppressWarnings("deprecation")
	@Test
	 void testGetOffersVideoAddOns() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequest.setAddOnType(Arrays.asList(ADDON_TYPE));
		offerRequest.setPlanSubType(Arrays.asList(ADDON_PLANSUBTYPE));
		offerRequestWrapper.setOfferRequest(offerRequest);

		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/VideoAddonCTOfferResponse.json"),CTOfferResponse.class);

		doReturn(ctOfferResponse).when(salesVideoAddonProcessor)
				.getVideoAddonOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.<List<CTOffer>>any(), ArgumentMatchers.<OfferRequestWrapper>any());
		
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any(),ArgumentMatchers.any());
        doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);

		assertNotNull(offersResponse);
//		assertEquals(26, offersResponse.getOffers().size());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	 void testGetOffersVideoDevice() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add("video-device");
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/VideoDeviceCTOfferResponse.json"),CTOfferResponse.class);

		doReturn(ctOfferResponse).when(salesEquipmentProcessor).getEquipmentOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.<List<CTOffer>>any(), ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any(),ArgumentMatchers.any());
        doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);

		assertNotNull(offersResponse);
		assertEquals(6, offersResponse.getOffers().size());
	}
	@Test
	 void testGetOffersVideoDeviceError() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add("video-device");
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		ServiceException serviceException = new ServiceException(ErrorMessages.CT_ERROR);
		doThrow(serviceException).when(salesEquipmentProcessor).getEquipmentOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		
		ServiceException ex = catchThrowableOfType(() -> OttSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertNotNull(ex);
	}
	
	@Test
	 void testGetOffersVideoPlanError() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.VIDEO_PLAN);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequest.setContractIndicator(Arrays.asList(Constants.EDSP_STRING));
		offerRequestWrapper.setOfferRequest(offerRequest);

		ServiceException serviceException = new ServiceException(ErrorMessages.CT_ERROR);
		doThrow(serviceException).when(salesVideoPlanProcessor)
				.getVideoPlanOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		ServiceException ex = catchThrowableOfType(() -> OttSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertNotNull(ex);
	}
	
	@Test
	 void testGetOffersVideoAddOnsError() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequest.setAddOnType(Arrays.asList(ADDON_TYPE));
		offerRequest.setPlanSubType(Arrays.asList(ADDON_PLANSUBTYPE));
		offerRequestWrapper.setOfferRequest(offerRequest);

		ServiceException serviceException = new ServiceException(ErrorMessages.CT_ERROR);

		doThrow(serviceException).when(salesVideoAddonProcessor)
				.getVideoAddonOffers(ArgumentMatchers.<OfferRequestWrapper>any());

		ServiceException ex = catchThrowableOfType(() -> OttSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertNotNull(ex);

	}
	
	@Test
	 void testGetOffersFeeError() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add("fee");
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		ServiceException serviceException = new ServiceException(ErrorMessages.CT_ERROR);
		doThrow(serviceException).when(salesEquipmentProcessor).getFeeOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		
		ServiceException ex = catchThrowableOfType(() -> OttSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertNotNull(ex);
	}
	
	@Test
	 void testGetOffersMigrationError() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequest.setAddOnType(Arrays.asList(ADDON_TYPE));
		offerRequest.setPlanSubType(Arrays.asList(ADDON_PLANSUBTYPE));
		offerRequestWrapper.setOfferRequest(offerRequest);
		ServiceException serviceException = new ServiceException(ErrorMessages.MIGRATION_OFFERS_INVALID_REQUEST);

		doThrow(serviceException).when(salesVideoAddonProcessor)
				.getVideoAddonOffers(ArgumentMatchers.<OfferRequestWrapper>any());

		ServiceException ex = catchThrowableOfType(() -> OttSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertNotNull(ex);

	}
	
	@Test
	 void testProcessOffersFetchedBySearchIds() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
        ObjectMapper objectMapper = new ObjectMapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        
        String REWARD_REQUEST = "{\n" +
                "    \"offerProductFamily\": [\n" +
                "        \"OTT\"\n" +
                "    ],\n" +
                "    \"salesChannel\": [\n" +
                "        \"opus\", \"online\"\n" +
                "    ],\n" +
                "    \"state\": \"staged\",\n" +
                "    \"pagination\": {\n" +
                "        \"page\": 1,\n" +
                "        \"limit\": 300\n" +
                "    },\n" +
                "    \"heartValue\": \"TV 1\"\n" +
                "}";

		 try {

            offerRequest =  objectMapper.readValue(REWARD_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        } catch(Exception e) {
            //do nothing
        }
		 
        offerRequestWrapper.setCartModeMobility(true);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        List<CTOffer> responseList= new ArrayList<>();
        CTOffer ctOffer=new CTOffer();
        OfferAttributes attributes=new OfferAttributes();
        attributes.setOfferProductSubtype(Constants.BASE);
        ctOffer.setAttributes(attributes);
        responseList.add(ctOffer);
        ctOfferResponse.setOffers(responseList);
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferRequest ctofferRequest = new CTOfferRequest();
        offerRequestWrapper.setCtOfferRequest(ctofferRequest);
		
		CTOfferResponse offers = new DataReader().readFileToObj("CTVideoPlanOfferResponse.json",CTOfferResponse.class);
		
		OttSalesOffersProcessor.processOffersFetchedBySearchIds(offers, offerRequestWrapper);
		assertNotNull(offers);
	}

	@Test
	 void testGetVideoAddOnsFRONTPORCHWithDirectvOnlineSalesChannel() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(List.of(ACQUISITION));
		offerRequest.setSalesChannel(List.of(Constants.DIRECTV_ONLINE));
		offerRequest.setContractIndicator(List.of((Constants.FRONTPORCH_STRING)));
		offerRequest.setOfferProductFamily(List.of(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/VideoAddonCTOfferResponseFFP.json"), CTOfferResponse.class);
		doReturn(ctOfferResponse).when(salesVideoAddonProcessor).getVideoAddonOffers(ArgumentMatchers.any());
                doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.any(), ArgumentMatchers.any());
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.any(),ArgumentMatchers.any());
		doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse, offerRequestWrapper);
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).excludeVirtualOffers((List<CTOffer>) ArgumentMatchers.<CTOfferResponse>any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).handleIneligibleOffersAndDelayedMessages((List<CTOffer>) ArgumentMatchers.<CTOfferResponse>any(), ArgumentMatchers.any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).filterInEligibleOffers((List<CTOffer>) ArgumentMatchers.<CTOfferResponse>any(), ArgumentMatchers.any());
        doReturn(false).when(featureHelper).isEnabled(Constants.FEATURE_FLAG_CONFLICTING_OFFER_NEW_LOGIC);
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertEquals(1, offersResponse.getOffers().size());
		assertEquals(Constants.FRONTPORCH_STRING, offersResponse.getOffers().get(0).getAttributes().getContractIndicator());
	}
	@Test
	 void testGetVideoPlansFRONTPORCHWithDirectvOnlineSalesChannel() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.VIDEO_PLAN);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(List.of(ACQUISITION));
		offerRequest.setSalesChannel(List.of(Constants.DIRECTV_ONLINE));
		offerRequest.setContractIndicator(List.of(Constants.FRONTPORCH_STRING));
		offerRequest.setOfferProductFamily(List.of(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/SalesVideoPlanofferForFFP.json"), CTOfferResponse.class);
		doReturn(ctOfferResponse).when(salesVideoPlanProcessor)
				.getVideoPlanOffers(ArgumentMatchers.any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.any(), ArgumentMatchers.any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).filterMaxPlusForOpus((List<CTOffer>) ArgumentMatchers.<CTOfferResponse>any(), ArgumentMatchers.any());
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.any(),ArgumentMatchers.any());
		doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse, offerRequestWrapper);
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertEquals(2, offersResponse.getOffers().size());
		assertEquals(Constants.FRONTPORCH_STRING, offersResponse.getOffers().get(0).getAttributes().getContractIndicator());

	}

	@Test
	 void testGetOffersRewardOffers() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.REWARD);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/RewardCardOffers.json"),CTOfferResponse.class);
		doReturn(ctOfferResponse).when(rewardProcessor).getRewardOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.<List<CTOffer>>any(), ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any(),ArgumentMatchers.any());
		doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertEquals(2, offersResponse.getOffers().size());
	}
	@Test
	 void testGetOffersProtectionPlan() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.PROTECTION_PLAN);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/CTProtectionPlan.json"),CTOfferResponse.class);
		doReturn(ctOfferResponse).when(protectionPlanProcessor).getProtectionPlanOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.<List<CTOffer>>any(), ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.<CTOfferResponse>any(),ArgumentMatchers.any());
		doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse,offerRequestWrapper);
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertEquals(2, offersResponse.getOffers().size());
	}


	@Test
	 void testRewardCardException(){
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.REWARD);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);

		ServiceException serviceException = new ServiceException(ErrorMessages.CT_ERROR);

		doThrow(serviceException).when(rewardProcessor)
				.getRewardOffers(ArgumentMatchers.<OfferRequestWrapper>any());

		ServiceException ex = catchThrowableOfType(() -> OttSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);

		assertNotNull(ex);

	}

	@Test
	 void testProtectionPlanException(){
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(Constants.PROTECTION_PLAN);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
		offerRequest.setSalesChannel(Arrays.asList(OPUS));
		offerRequest.setOfferProductFamily(Arrays.asList(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);

		ServiceException serviceException = new ServiceException(ErrorMessages.CT_ERROR);

		doThrow(serviceException).when(protectionPlanProcessor)
				.getProtectionPlanOffers(ArgumentMatchers.<OfferRequestWrapper>any());

		ServiceException ex = catchThrowableOfType(() -> OttSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);

		assertNotNull(ex);

	}

	@Test
	 void testGetVideoAddOnsROADRUNNERWithDirectvOnlineSalesChannel() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerProductTypes = new ArrayList<>();
		offerProductTypes.add(VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		offerRequest.setOfferActionType(List.of(ACQUISITION));
		offerRequest.setSalesChannel(List.of(Constants.DIRECTV_ONLINE));
		offerRequest.setContractIndicator(List.of((Constants.ROAD_RUNNER)));
		offerRequest.setOfferProductFamily(List.of(OTT));
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/roadrunner/VideoAddonCTofferResponseRoadrunner.json"), CTOfferResponse.class);
		doReturn(ctOfferResponse).when(salesVideoAddonProcessor).getVideoAddonOffers(ArgumentMatchers.any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).dtvNowRulesProcessing(
				ArgumentMatchers.any(), ArgumentMatchers.any());
		doReturn(ctOfferResponse).when(cpopProductsHelper).filterInvalidPrices(ArgumentMatchers.any(),ArgumentMatchers.any());
		doReturn(ctOfferResponse).when(cpopBenefitsHelper).filterInvalidBenefits(ctOfferResponse, offerRequestWrapper);
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).excludeVirtualOffers((List<CTOffer>) ArgumentMatchers.<CTOfferResponse>any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).filterInEligibleOffers((List<CTOffer>) ArgumentMatchers.<CTOfferResponse>any(), ArgumentMatchers.any());
		doReturn(ctOfferResponse.getOffers()).when(ottSalesOffersProcessorHelper).handleIneligibleOffersAndDelayedMessages((List<CTOffer>) ArgumentMatchers.<CTOfferResponse>any(), ArgumentMatchers.any());
        doReturn(false).when(featureHelper).isEnabled(Constants.FEATURE_FLAG_CONFLICTING_OFFER_NEW_LOGIC);
		CTOfferResponse offersResponse = OttSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertEquals(1, offersResponse.getOffers().size());
		assertEquals(Constants.ROAD_RUNNER, offersResponse.getOffers().get(0).getAttributes().getContractIndicator());
	}
	@Test
	public void filterOfferBasedOnCartOfferQuantity(){
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setCartOffers(new ArrayList<>());
		CartOffer cartOffer = new CartOffer();
		cartOffer.setOfferCode("OF_HARD-GEMINILEASE-202311_TAZCONTRACT");
		cartOffer.setQuantity(2);
		offerRequest.getCartOffers().add(cartOffer);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse offerResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/CTRewardOffers.json"),CTOfferResponse.class);

		ottSalesOffersProcessorHelper.filterOfferBasedOnCartOfferQuantity(offerResponse.getOffers(), offerRequestWrapper);

        offersUtilsInjectMock.setDepenentOfferObject(offerResponse.getOffers());

	}
}
