/**
 * 
 */
package com.dtv.dcp.epoch.service.satellite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.sales.SatelliteSalesOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteRetentionOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessor;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author ap778g
 *
 */
@ExtendWith(MockitoExtension.class)
public class SatelliteOffersServiceImplTest {
	

	@Mock
	SatelliteSalesOffersProcessor satelliteSalesOffersProcessor;
	
	@Mock
	SatelliteServicesOffersProcessor satelliteServicesOffersProcessor;
	
	@Mock
	SatelliteRetentionOffersProcessor satelliteRetentionOffersProcessor;
	
	@InjectMocks
	private SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	
	@InjectMocks
	private SatelliteOffersServiceImpl satelliteOffersServiceImpl;
	
	@Mock
	DMALookUpService dmaLookUpService;	
	
	@Mock
	CpopClient  cpopClient;
	
	/** The headers. */
	HttpHeaders headers;
	
	private static final String OFFER_REQUEST_PROACTIVE_CREDIT_ELIGIBLE = "stms/offer_request_proactive_credit_eligible.json";
	private static final String OFFER_REQUEST_CLOSING = "acquisition/offer_request_closing.json";
	private static final String OFFER_REQUEST_SOS = "acquisition/offer_request_sos.json";
	private static final String OFFER_REQUEST_RETENTION_ADDON = "offer_request_retention_addon.json";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);	
		headers = Mockito.mock(HttpHeaders.class);
	}
		
	@Test
	public void testGetVideoAddonRetentionOffers() throws Exception {
		
		OfferRequest offerRequest= new OfferRequest();
		
		List<String> offerProductTypes= new ArrayList<>();
		offerProductTypes.add(Constants.VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		
		List<String> offerActionType = new ArrayList<>();
		offerActionType.add(Constants.RETENTION_ACTION_TYPE);
		
		offerRequest.setOfferActionType(offerActionType);
		List<String> addOnType = new ArrayList<>();
		addOnType.add("Programming-Bolt-on");
		
		offerRequest.setAddOnType(addOnType);
		OfferRequestWrapper offerRequestWrapper= new OfferRequestWrapper();
		
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList= new ArrayList<>();
		CTOffer ctOffer=new CTOffer();
		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		ctOffer.setAttributes(attributes);
		responseList.add(ctOffer);
		ctOfferResponse.setOffers(responseList);
		offerRequestWrapper.setOfferRequest(offerRequest);
		satelliteOffersServiceImpl.getOffers( offerRequestWrapper);

	}
	
	@Test
	public void testGetVideoAddonAcquistionOffers() throws Exception {
		
		OfferRequest offerRequest= new OfferRequest();
		
		List<String> offerProductTypes= new ArrayList<>();
		offerProductTypes.add(Constants.VIDEO_ADDON);
		offerRequest.setOfferProductType(offerProductTypes);
		
		List<String> offerActionType = new ArrayList<>();
		offerActionType.add(Constants.ACQUISITION_ACTION_TYPE);
		
		offerRequest.setOfferActionType(offerActionType);
		List<String> addOnType = new ArrayList<>();
		addOnType.add("Programming-Bolt-on");
		
		offerRequest.setAddOnType(addOnType);
		OfferRequestWrapper offerRequestWrapper= new OfferRequestWrapper();
		
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList= new ArrayList<>();
		CTOffer ctOffer=new CTOffer();
		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		ctOffer.setAttributes(attributes);
		responseList.add(ctOffer);
		ctOfferResponse.setOffers(responseList);
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		satelliteOffersServiceImpl.getOffers( offerRequestWrapper);

	}

	@Test
	public void testGetUccValidationCoupons() throws Exception {
		OfferRequestWrapper offerRequestWrapper= new OfferRequestWrapper();
		OfferRequest offerRequest= new OfferRequest();
		offerRequest.setOfferTypes(Arrays.asList("coupon"));
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList= new ArrayList<>();
		CTOffer ctOffer=getOfferData();
		responseList.add(ctOffer);
		ctOfferResponse.setOffers(responseList);
		offerRequestWrapper.setOfferRequest(offerRequest);

		doReturn(ctOfferResponse).when(satelliteServicesOffersProcessor).getOffers(Mockito.any());

		CTOfferResponse response=satelliteOffersServiceImpl.getOffers( offerRequestWrapper);
		assertNotNull(response);
		assertEquals("",response.getOffers().get(0).getAttributes().getAssociatedProducts()
				.get(0).getQualifyingProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getRedemptionLimit());

	}

	private CTOffer getOfferData() throws IOException {
		ObjectMapper objectMapper=new ObjectMapper();
		return objectMapper.readValue(getString(),CTOffer.class);
	}

	private String getString() {
		return "{\n" +
				"    \"id\": \"3f2cae85-fcf0-46cf-824a-b24065ff1fe2\",\n" +
				"    \"code\": \"Single_EST_testdata_offer\",\n" +
				"    \"name\": {\n" +
				"        \"en\": \"Single_EST_testdata_offer\"\n" +
				"    },\n" +
				"    \"description\": {\n" +
				"        \"en\": \"Single_EST_testdata_offer\"\n" +
				"    },\n" +
				"    \"startDate\": \"2020-08-01T00:00:00.000Z\",\n" +
				"    \"endDate\": \"2022-08-31T00:00:00.000Z\",\n" +
				"    \"attributes\": {\n" +
				"        \"offerProductType\": \"campaign\",\n" +
				"        \"offerProductTypes\": [\n" +
				"            \"campaign\"\n" +
				"        ],\n" +
				"        \"offerProductFamily\": \"satellite\",\n" +
				"        \"offerProductFamilies\": [\n" +
				"            \"satellite\"\n" +
				"        ],\n" +
				"        \"offerType\": \"coupon\",\n" +
				"        \"offerActionType\": \"Other\",\n" +
				"        \"offerActionTypes\": [\n" +
				"            \"Other\"\n" +
				"        ],\n" +
				"        \"migratedOffer\": false,\n" +
				"        \"associatedProducts\": [\n" +
				"            {\n" +
				"                \"qualifyingProducts\": [\n" +
				"                    {\n" +
				"                        \"constraints\": {\n" +
				"                            \"productFamily\": \"satellite\",\n" +
				"                            \"productTypes\": [\n" +
				"                                \"campaign\"\n" +
				"                            ],\n" +
				"                            \"anchor\": false,\n" +
				"                            \"dependent\": false\n" +
				"                        },\n" +
				"                        \"products\": [\n" +
				"                            {\n" +
				"                                \"id\": \"635683f0-4032-483a-af57-daffb15e2999\",\n" +
				"                                \"typeId\": \"product\",\n" +
				"                                \"obj\": {\n" +
				"                                    \"id\": \"635683f0-4032-483a-af57-daffb15e2999\",\n" +
				"                                    \"code\": \"Single_EST_testdata\",\n" +
				"                                    \"productType\": {\n" +
				"                                        \"typeId\": \"product-type\",\n" +
				"                                        \"id\": \"ca261a8e-ba72-4f7d-bff9-68612d33bc83\",\n" +
				"                                        \"key\": \"campaign\"\n" +
				"                                    },\n" +
				"                                    \"name\": {\n" +
				"                                        \"en\": \"Single_EST_testdata\"\n" +
				"                                    },\n" +
				"                                    \"description\": {\n" +
				"                                        \"en\": \"Single_EST_testdata\"\n" +
				"                                    },\n" +
				"                                    \"categories\": [],\n" +
				"                                    \"variants\": [\n" +
				"                                        {\n" +
				"                                            \"id\": \"1\",\n" +
				"                                            \"prices\": [],\n" +
				"                                            \"attributes\": {\n" +
				"                                                \"campaignId\": \"7735460\",\n" +
				"                                                \"campaignCode\": \"Single_EST_testdata\",\n" +
				"                                                \"campaignName\": \"Single_EST_testdata\",\n" +
				"                                                \"campaignDesc\": \"Single_EST_testdata\",\n" +
				"                                                \"couponType\": \"singleUse\",\n" +
				"                                                \"campaignCategory\": \"standard\",\n" +
				"                                                \"creationDate\": \"2020-08-14T00:00:00.000Z\",\n" +
				"                                                \"campaignStartDate\": \"2020-08-15T00:00:00.000Z\",\n" +
				"                                                \"campaignEndDate\": \"2022-08-31T00:00:00.000Z\",\n" +
				"                                                \"autoRegister\": \"true\",\n" +
				"                                                \"studioSponsorByATT\": true,\n" +
				"                                                \"lockDuration\": \"309\",\n" +
				"                                                \"numberOfCoupons\": 10,\n" +
				"                                                \"campaignMessaging\": \"message\",\n" +
				"                                                \"campaignEligibility\": [\n" +
				"                                                    {\n" +
				"                                                        \"purchaseType\": \"Purchase|Rent\"\n" +
				"                                                    }\n" +
				"                                                ]\n" +
				"                                            }\n" +
				"                                        }\n" +
				"                                    ]\n" +
				"                                },\n" +
				"                                \"productType\": \"campaign\",\n" +
				"                                \"key\": \"Single_EST_testdata\"\n" +
				"                            }\n" +
				"                        ]\n" +
				"                    }\n" +
				"                ]\n" +
				"            }\n" +
				"        ],\n" +
				"        \"benefits\": [\n" +
				"            {\n" +
				"                \"id\": \"82fc72d3-ef00-41d1-a136-8c897599035b\",\n" +
				"                \"billingBenefitCode\": \"Single_EST_testdata_benefit\",\n" +
				"                \"benefitName\": {\n" +
				"                    \"en\": \"Single_EST_testdata_benefit\"\n" +
				"                },\n" +
				"                \"benefitDisplayName\": {\n" +
				"                    \"en\": \"3f2cae85-fcf0-46cf-824a-b24065ff1fe2\"\n" +
				"                },\n" +
				"                \"benefitRank\": 0,\n" +
				"                \"description\": {\n" +
				"                    \"en\": \"Single_EST_testdata_benefit\"\n" +
				"                },\n" +
				"                \"startDate\": \"2020-08-15T00:00:00.000Z\",\n" +
				"                \"retIoApplicableProducts\": false,\n" +
				"                \"endDate\": \"2022-08-31T00:00:00.000Z\",\n" +
				"                \"contractIndicator\": \"all\",\n" +
				"                \"contractVersion\": \"3\",\n" +
				"                \"selfBenefit\": false,\n" +
				"                \"duration\": 0,\n" +
				"                \"period\": \"NoOfMonths\",\n" +
				"                \"billAfterPeriod\": false,\n" +
				"                \"applicableProducts\": [\n" +
				"                    {\n" +
				"                        \"products\": [\n" +
				"                            {\n" +
				"                                \"id\": \"635683f0-4032-483a-af57-daffb15e2999\",\n" +
				"                                \"typeId\": \"product\"\n" +
				"                            }\n" +
				"                        ]\n" +
				"                    }\n" +
				"                ],\n" +
				"                \"maxOccurrence\": 0,\n" +
				"                \"benefitType\": \"flat-off\",\n" +
				"                \"value\": {\n" +
				"                    \"percentage\": 0\n" +
				"                },\n" +
				"                \"freeTrialPromo\": false,\n" +
				"                \"ioOffer\": false,\n" +
				"                \"freeTrialDuration\": 0,\n" +
				"                \"agentOffer\": false,\n" +
				"                \"contractTerm\": \"3\",\n" +
				"                \"minQuantity\": 0,\n" +
				"                \"promoPriority\": 0,\n" +
				"                \"benefitLevel\": \"product\",\n" +
				"                \"notApplibaleForOffer\": false,\n" +
				"                \"enableServiceDisconnect\": false,\n" +
				"                \"quotaBased\": false,\n" +
				"                \"couponPromo\": false,\n" +
				"                \"migrated\": false,\n" +
				"                \"isDependentPromo\": false,\n" +
				"                \"isMigrated\": false,\n" +
				"                \"isQuotaBased\": false\n" +
				"            }\n" +
				"        ],\n" +
				"        \"leadOffer\": false,\n" +
				"        \"isVirtual\": false,\n" +
				"        \"flashSaleOffer\": false\n" +
				"    }\n" +
				"}";
	}

	@Test
	public void testGetProactiveCreditOffers() {
		
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_PROACTIVE_CREDIT_ELIGIBLE, OfferRequest.class);
        	
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_proactive_credit.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
		
	 	when(satelliteServicesOffersProcessor.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
	 	when(dmaLookUpService.hasLocalChannels(Mockito.any(), Mockito.any())).thenReturn(true);
		
	 	CTOfferResponse offerResponse = satelliteOffersServiceImpl.getOffers(offerRequestWrapper);
		assertNotNull(offerResponse);
		assertEquals(1, offerResponse.getOffers().size());

	}
	
	@Test
	public void testGetClosingCreditOffers() {
		
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_CLOSING, OfferRequest.class);
        	
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("acquisition/offer_response_closing.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
		
	 	when(satelliteSalesOffersProcessor.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
	 	when(dmaLookUpService.hasLocalChannels(Mockito.any(), Mockito.any())).thenReturn(true);
		
	 	CTOfferResponse offerResponse = satelliteOffersServiceImpl.getOffers(offerRequestWrapper);
		assertNotNull(offerResponse);
		assertEquals(1, offerResponse.getOffers().size());

	}
	
	@Test
	public void testGetSOSCreditOffers() {
		
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SOS, OfferRequest.class);
        	
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("acquisition/offer_response_sos.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
		
	 	when(satelliteSalesOffersProcessor.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
	 	when(dmaLookUpService.hasLocalChannels(Mockito.any(), Mockito.any())).thenReturn(true);
		
	 	CTOfferResponse offerResponse = satelliteOffersServiceImpl.getOffers(offerRequestWrapper);
		assertNotNull(offerResponse);
		assertEquals(1, offerResponse.getOffers().size());

	}
	
	@Test
	public void testGetRetentionAddonOffers() {
		
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_RETENTION_ADDON, OfferRequest.class);
        	
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("offer_response_retention_addon.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
		
	 	when(satelliteServicesOffersProcessor.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
	 	when(dmaLookUpService.hasLocalChannels(Mockito.any(), Mockito.any())).thenReturn(true);
		
	 	CTOfferResponse offerResponse = satelliteOffersServiceImpl.getOffers(offerRequestWrapper);
		assertNotNull(offerResponse);
		assertEquals(1, offerResponse.getOffers().size());

	}

}