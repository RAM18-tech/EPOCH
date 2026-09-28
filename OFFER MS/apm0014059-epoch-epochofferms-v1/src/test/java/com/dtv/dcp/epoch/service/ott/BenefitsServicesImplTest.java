
package com.dtv.dcp.epoch.service.ott;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.springframework.http.MediaType;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.BenefitRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.services.OttServicesBenefitsProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesBenefitsProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class BenefitsServicesImplTest {
	

	
	@Mock
	private SatelliteServicesBenefitsProcessor satelliteServicesBenefitsProcessor;
	
	@Mock
	private OttServicesBenefitsProcessor ottServicesBenefitsProcessor;

	
	@InjectMocks
	private BenefitsServiceImpl benefitsService;
	
	@Mock
	CpopClient  cpopClient;
	@Mock
	FeatureManagerHelper featureManagerHelper;
	/** The headers. */
	HttpHeaders headers;


	private static String BENEFIT_REQUEST = "{\n" +
			"    \"productTypes\": [\n" +
			"        \"video-addon\"\n" +
			"    ],\n" +
			"    \"benefitIds\": [\n" +
			"       \"cc6b7b22-27b8-4863-b5d8-47a893fafd9a\"\n" +
			"    ],\n" +
			"    \"benefitCodes\": [\n" +
			"    \t\"BC_CPOP_flatoff_data1\"\n" +
			"    \t],\n" +
			"    \"userType\": \"agent\",\n" +
			"    \"account\": {\n" +
			"        \"accountType\": \"mobility\",\n" +
			"        \"contractIndicator\": true,\n" +
			"        \"contractVersion\": \"v1\",\n" +
			"        \"zipCode\": \"32957\"\n" +
			"    },\n" +
			"    \"state\": \"staged\"\n" +
			"}";
	private static String BENEFIT_REQUEST_TEST1 = "{\n" +
			"    \"productTypes\": [\n" +
			"        \"video-addon\"\n" +
			"    ],\n" +
			"    \"benefitIds\": [\n" +
			"       \"cc6b7b22-27b8-4863-b5d8-47a893fafd9a\"\n" +
			"    ], "+
			"    \"userType\": \"agent\",\n" +
			"    \"account\": {\n" +
			"        \"accountType\": \"mobility\",\n" +
			"        \"contractIndicator\": true,\n" +
			"        \"contractVersion\": \"v1\",\n" +
			"        \"zipCode\": \"32957\"\n" +
			"    },\n" +
			"    \"state\": \"staged\"\n" +
			"}";
	private static String BENEFIT_RESPONSE = "{\n" +
			"    \"limit\": 200,\n" +
			"    \"offset\": 0,\n" +
			"    \"count\": 1,\n" +
			"    \"total\": 1,\n" +
			"    \"benefits\": [\n" +
			"        {\n" +
			"            \"duration\": 25,\n" +
			"            \"billingBenefitCode\": \"BC_Cpop_percoff_1\",\n" +
			"            \"promoSourceSkuProductType\": \"video-plan\",\n" +
			"            \"beneficiaryProductFamily\": \"OTT\",\n" +
			"            \"billAfterPeriod\": true,\n" +
			"            \"benefitType\": \"percent-off\",\n" +
			"            \"promoSourceSkuProductFamily\": \"OTT\",\n" +
			"            \"contractIndicator\": \"non-contract\",\n" +
			"            \"promoPriority\": 4,\n" +
			"            \"benefitDisplayName\": {\n" +
			"                \"en\": \"BDNCpop_percoff_1\"\n" +
			"            },\n" +
			"            \"beneficiaryProductType\": [\n" +
			"                \"video-plan\"\n" +
			"            ],\n" +
			"            \"benefitLevel\": \"account\",\n" +
			"            \"freeTrialPromo\": false,\n" +
			"            \"period\": \"NoOfDays\",\n" +
			"            \"benefitCategory\": \"Product\",\n" +
			"            \"applicableProducts\": [\n" +
			"                {\n" +
			"                    \"products\": [\n" +
			"                        {\n" +
			"                            \"typeId\": \"product\",\n" +
			"                            \"id\": \"30087085-93a0-472d-a3ff-1ab543c6c348\",\n" +
			"                            \"key\": \"BASE-PLUS-2018\",\n" +
			"                            \"productType\": \"video-plan\"\n" +
			"                        }\n" +
			"                    ]\n" +
			"                }\n" +
			"            ],\n" +
			"            \"id\": \"cc6b7b22-27b8-4863-b5d8-47a893fafd9a\",\n" +
			"            \"version\": 2,\n" +
			"            \"code\": \"BC_Cpop_percoff_1\",\n" +
			"            \"benefitName\": {\n" +
			"                \"en\": \"BN_Cpop_percoff_1\"\n" +
			"            },\n" +
			"            \"description\": {\n" +
			"                \"en\": \"\"\n" +
			"            },\n" +
			"            \"startDate\": \"2019-10-16T00:00:00.000Z\",\n" +
			"            \"endDate\": \"2019-11-01T00:00:00.000Z\",\n" +
			"            \"value\": {\n" +
			"                \"percentage\": 25\n" +
			"            },\n" +
			"            \"isQuotaBased\": false\n" +
			"        }\n" +
			"    ]\n" +
			"}";

	private static String BENEFIT_REQUEST_SATELLITE = "stms/benefit_request.json";

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);	
		headers = Mockito.mock(HttpHeaders.class);
		
	}
	
	
		
		
	@Test
	public void testGetBenefits() throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		BenefitRequest benefitRequest = null;
		CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
		CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
		JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);

		try {

			benefitRequest =  objectMapper.readValue(BENEFIT_REQUEST, BenefitRequest.class);
			BeanUtils.copyProperties(benefitRequest, ctBenefitsRequest);

			ctBenefitsResponse =  objectMapperRes.readValue(BENEFIT_RESPONSE, CTBenefitsResponse.class);
			// JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);//objectMapperRes.readValue(BENEFIT_RESPONSE, JsonNode.class);
		} catch(Exception e) {
			//do nothing
		}
		
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
		
		Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
		
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LOGGEDINID)).thenReturn("test_idpctx-loggedInId");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)).thenReturn("test_idpctx-linkedWirelessAccNums");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)).thenReturn("test_idpctx-linkedDirectvNowAccNums");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("test_idpctx-session-id");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)).thenReturn("test_Idpctx-linkeduverseaccnums");
		
		BenefitRequestWrapper benefitRequestWrapper = new BenefitRequestWrapper();
		
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList= new ArrayList<>();
		CTOffer ctOffer=new CTOffer();
		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		ctOffer.setAttributes(attributes);
		responseList.add(ctOffer);
		ctOfferResponse.setOffers(responseList);
		benefitRequestWrapper.setBenefitRequest(benefitRequest);
		
		Mockito.lenient().doReturn(ctBenefitsResponse).when(cpopClient).getBenefits(Mockito.any());
		Mockito.lenient().when(ottServicesBenefitsProcessor.getBenefits(benefitRequestWrapper)).thenReturn(ctBenefitsResponse);
		doReturn(ctBenefitsResponse).when(ottServicesBenefitsProcessor).getBenefits(Mockito.any());

		benefitsService.getBenefits(headers, benefitRequest);
		
		

	}
	@Test
	public void testGetBenefitsOption1() throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		BenefitRequest benefitRequest = null;
		CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
		CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
		JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);

		try {

			benefitRequest =  objectMapper.readValue(BENEFIT_REQUEST_TEST1, BenefitRequest.class);
			BeanUtils.copyProperties(benefitRequest, ctBenefitsRequest);

			ctBenefitsResponse =  objectMapperRes.readValue(BENEFIT_RESPONSE, CTBenefitsResponse.class);
			// JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);//objectMapperRes.readValue(BENEFIT_RESPONSE, JsonNode.class);
		} catch(Exception e) {
			//do nothing
		}

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

		Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LOGGEDINID)).thenReturn("test_idpctx-loggedInId");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)).thenReturn("test_idpctx-linkedWirelessAccNums");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)).thenReturn("test_idpctx-linkedDirectvNowAccNums");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("test_idpctx-session-id");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)).thenReturn("test_Idpctx-linkeduverseaccnums");

		BenefitRequestWrapper benefitRequestWrapper = new BenefitRequestWrapper();

		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList= new ArrayList<>();
		CTOffer ctOffer=new CTOffer();
		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		ctOffer.setAttributes(attributes);
		responseList.add(ctOffer);
		ctOfferResponse.setOffers(responseList);
		benefitRequestWrapper.setBenefitRequest(benefitRequest);

		Mockito.lenient().doReturn(ctBenefitsResponse).when(cpopClient).getBenefits(Mockito.any());
		Mockito.lenient().when(ottServicesBenefitsProcessor.getBenefits(benefitRequestWrapper)).thenReturn(ctBenefitsResponse);
		doReturn(ctBenefitsResponse).when(ottServicesBenefitsProcessor).getBenefits(Mockito.any());
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);

		benefitsService.getBenefits(headers, benefitRequest);



	}
	
	@Test
	public void testGetBenefitsSatellite() {
		BenefitRequest benefitRequest = new BenefitRequest();
		
		try {
			benefitRequest = new DataReader().readFileToObj(BENEFIT_REQUEST_SATELLITE, BenefitRequest.class);
		} catch (Exception e) {
		}
		
		CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("stms/benefit_response.json", CTBenefitsResponse.class);
		
		Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

		when(headers.getFirst(Constants.IDPCTX_LOGGEDINID)).thenReturn("test_idpctx-loggedInId");
		when(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)).thenReturn("test_idpctx-linkedWirelessAccNums");
		when(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)).thenReturn("test_idpctx-linkedDirectvNowAccNums");
		when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("test_idpctx-session-id");
		when(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)).thenReturn("test_Idpctx-linkeduverseaccnums");
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		
		when(satelliteServicesBenefitsProcessor.getBenefits(any())).thenReturn(ctBenefitsResponse);
		
		CTBenefitsResponse benefitResponse = benefitsService.getBenefits(headers, benefitRequest);
		
		assertNotNull(benefitResponse);
		assertNotNull(benefitResponse.getBenefits());
		assertEquals(1, benefitResponse.getBenefits().size());
	}

}
