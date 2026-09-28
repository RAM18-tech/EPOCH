package com.dtv.dcp.epoch.service.watchtv;

import static org.mockito.Mockito.doReturn;

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
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.watchtv.services.WatchTVServicesOffersProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class WatchTVOffersServiceImplTest {

    @Mock
    private WatchTVServicesOffersProcessor watchTVServicesOffersProcessor;


    @InjectMocks
    private WatchTVOffersServiceImpl watchTVOffersService;



    @Mock
    CpopClient cpopClient;

    /** The headers. */
    HttpHeaders headers;


    private static String RETENTION_REQUEST = "{\n" +
            "    \"offerActionType\": [\n" +
            "        \"Retention\"\n" +
            "    ],\n" +
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


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        headers = Mockito.mock(HttpHeaders.class);

    }




    @Test
    public void testGetOfferedPromotions() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        OfferRequest offerRequest = null;
       // CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
       // JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);

        try {

            offerRequest =  objectMapper.readValue(RETENTION_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);

            // ctBenefitsResponse =  objectMapperRes.readValue(BENEFIT_RESPONSE, CTBenefitsResponse.class);
            // JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);//objectMapperRes.readValue(BENEFIT_RESPONSE, JsonNode.class);
        } catch(Exception e) {
            //do nothing
        }

        //OfferRequest offerRequest= new OfferRequest();

//        List<String> offerProductTypes= new ArrayList<>();
//        offerProductTypes.add(Constants.VIDEO_ADDON);
//        offerRequest.setOfferProductTypes(offerProductTypes);
//
//        List<String> offerActionType = new ArrayList<>();
//        offerActionType.add(Constants.RETENTION_ACTION_TYPE);
//
//        offerRequest.setOfferActionType(offerActionType);
//        List<String> addOnType = new ArrayList<>();
//        addOnType.add("Programming-Bolt-on");
//
//        offerRequest.setAddOnType(addOnType);

        Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LOGGEDINID)).thenReturn("test_idpctx-loggedInId");
        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)).thenReturn("test_idpctx-linkedWirelessAccNums");
        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)).thenReturn("test_idpctx-linkedDirectvNowAccNums");
        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("test_idpctx-session-id");
        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)).thenReturn("test_Idpctx-linkeduverseaccnums");

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        List<CTOffer> responseList= new ArrayList<>();
        CTOffer ctOffer=new CTOffer();
        OfferAttributes attributes=new OfferAttributes();
        attributes.setOfferProductSubtype(Constants.BASE);
        ctOffer.setAttributes(attributes);
        responseList.add(ctOffer);
        ctOfferResponse.setOffers(responseList);
        offerRequestWrapper.setOfferRequest(offerRequest);

        Mockito.lenient().doReturn(ctOfferResponse).when(cpopClient).getOffers(Mockito.any());
        Mockito.lenient().when(watchTVServicesOffersProcessor.getOffers(offerRequestWrapper )).thenReturn(ctOfferResponse);
        doReturn(ctOfferResponse).when(watchTVServicesOffersProcessor).getOffers(Mockito.any());

        watchTVOffersService.getOffers( offerRequestWrapper);

    }
    
    
    @Test
	public void testGetOffered() throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();
		OfferRequest offerRequest = null;
		CTOfferRequest ctOfferRequest = new CTOfferRequest();

		try {

			offerRequest = objectMapper.readValue(RETENTION_REQUEST, OfferRequest.class);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);

		} catch (Exception e) {
			// do nothing
		}

		Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LOGGEDINID)).thenReturn("test_idpctx-loggedInId");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS))
				.thenReturn("test_idpctx-linkedWirelessAccNums");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)).thenReturn("test_idpctx-linkedDirectvNowAccNums");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("test_idpctx-session-id");
		Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS))
				.thenReturn("test_Idpctx-linkeduverseaccnums");

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> responseList = new ArrayList<>();
		CTOffer ctOffer = new CTOffer();
		OfferAttributes attributes = new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		ctOffer.setAttributes(attributes);
		responseList.add(ctOffer);
		ctOfferResponse.setOffers(responseList);
		offerRequestWrapper.setOfferRequest(offerRequest);

		Mockito.lenient().doReturn(ctOfferResponse).when(cpopClient).getOffers(Mockito.any());
		Mockito.lenient().when(watchTVServicesOffersProcessor.getOffers(offerRequestWrapper)).thenReturn(null);
		doReturn(null).when(watchTVServicesOffersProcessor).getOffers(Mockito.any());
		watchTVOffersService.getOffers(offerRequestWrapper);

	}

}
