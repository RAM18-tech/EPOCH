package com.dtv.dcp.epoch.processor.helper;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.BenefitRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.TestUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CPOPBenefitsHelperTest {

    @InjectMocks
    CPOPBenefitsHelper cpopBenefitsHelper;

    private static String ctOfferRes = TestUtility.loadJson("/CTVideoPlanOfferResponse.json");

    private static final String OTT = "OTT";

    private static final String OPUS = "opus";

    private static final String ACQUISITION = "Acquisition";


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

    }

    @Test
    public void testGetBenefitsFilter() {
        // Temp  need to update late once logic updated

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        BenefitRequest benefitRequest = null;
        CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
        CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
        //JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);

        try {

            benefitRequest = objectMapper.readValue(BENEFIT_REQUEST, BenefitRequest.class);
            BeanUtils.copyProperties(benefitRequest, ctBenefitsRequest);

            ctBenefitsResponse = objectMapperRes.readValue(BENEFIT_RESPONSE, CTBenefitsResponse.class);
            // JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);//objectMapperRes.readValue(BENEFIT_RESPONSE, JsonNode.class);
        } catch (Exception e) {
            //do nothing
        }

        OfferAttributes attributes = new OfferAttributes();
        attributes.setOfferProductSubtype(Constants.BASE);

        BenefitRequestWrapper benefitRequestWrapper = new BenefitRequestWrapper();
        benefitRequestWrapper.setBenefitRequest(benefitRequest);


//        when(cpopClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
//
//        when(cpopBenefitsHelper.filterByDate(Mockito.any())).thenReturn(ctBenefitsResponse.getBenefits());


        List<Benefit> list = cpopBenefitsHelper.filterByDate(ctBenefitsResponse.getBenefits());


        assertNotNull(list);
    }

    @Test
    public void testFilterExpiredBenefits() {


        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductTypes = new ArrayList<>();
        offerProductTypes.add(Constants.VIDEO_PLAN);
        offerRequest.setOfferProductType(offerProductTypes);
        offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
        //offerRequest.setCustomerSegments(Arrays.asList(RESIDENTIAL));
        offerRequest.setSalesChannel(Arrays.asList(OPUS));
        offerRequest.setOfferProductFamily(Arrays.asList(OTT));
        offerRequest.setContractIndicator(Arrays.asList("contract"));
        offerRequestWrapper.setOfferRequest(offerRequest);

        ObjectMapper objectMapperRes = new ObjectMapper();
        CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();

        CTOfferResponse ctOfferResponse = null;

        try {
            ctOfferResponse = objectMapperRes.readValue(ctOfferRes, CTOfferResponse.class);
        } catch (Exception e) {
        }

        CTOfferResponse list = cpopBenefitsHelper.filterInvalidBenefits(ctOfferResponse, offerRequestWrapper);

        assertNotNull(list);
    }

    @Test
    public void testFilterOffersWithBenefits() {


        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductTypes = new ArrayList<>();
        offerProductTypes.add(Constants.VIDEO_PLAN);
        offerRequest.setOfferProductType(offerProductTypes);
        offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
        //offerRequest.setCustomerSegments(Arrays.asList(RESIDENTIAL));
        offerRequest.setSalesChannel(Arrays.asList(OPUS));
        offerRequest.setOfferProductFamily(Arrays.asList(OTT));
        offerRequest.setContractIndicator(Arrays.asList("contract"));
        offerRequestWrapper.setOfferRequest(offerRequest);

        ObjectMapper objectMapperRes = new ObjectMapper();
        CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();

        CTOfferResponse ctOfferResponse = null;

        try {
            ctOfferResponse = objectMapperRes.readValue(ctOfferRes, CTOfferResponse.class);
        } catch (Exception e) {
        }

        CTOfferResponse list = cpopBenefitsHelper.filterOffersBySalesChannelOfBenefits(ctOfferResponse, offerRequestWrapper);

        assertNotNull(list);
    }

    @Test
    public void testfilterInvalidBenefits_AssistedSales() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductTypes = new ArrayList<>();
        offerProductTypes.add(Constants.VIDEO_PLAN);
        offerRequest.setOfferProductType(offerProductTypes);
        offerRequest.setOfferActionType(Arrays.asList(ACQUISITION));
        offerRequest.setSalesChannel(Arrays.asList(Constants.ASSISTED_SALES));
        offerRequest.setOfferProductFamily(Arrays.asList(OTT));
        offerRequest.setContractIndicator(Arrays.asList("TAZCONTRACT"));
        offerRequestWrapper.setOfferRequest(offerRequest);
        ObjectMapper objectMapperRes = new ObjectMapper();
        CTOfferResponse ctOfferResponse = null;
        try {
            ctOfferResponse = objectMapperRes.readValue(ctOfferRes, CTOfferResponse.class);
        } catch (Exception e) {
        }
        CTOfferResponse list = cpopBenefitsHelper.filterInvalidBenefits(ctOfferResponse, offerRequestWrapper);
        assertNotNull(list);
    }
}
