package com.dtv.dcp.epoch.processor.ott.services;

import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPBenefitsHelper;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.TestUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OttServicesIoOffersProcessorTest {

    public static String OFFER_REQUEST = "{\n" +
            "    \"accountTypes\": [\n" +
            "        \"Residential\"\n" +
            "    ],\n" +
            "    \"addOnType\": [],\n" +
            "    \"contractIndicator\": [\n" +
            "        \"contract\"\n" +
            "    ],\n" +
            "    \n" +
            "    \"customerContext\": {\n" +
            "        \"existingProductFamily\": [\n" +
            "            \"OTT\"\n" +
            "        ],\n" +
            "        \"OTT\": {\n" +
            "            \"products\": [\n" +
            "                {\n" +
            "                    \"basePrice\": \"0.0\",\n" +
            "                    \"productCode\": \"RSN-TIER5-201812\",\n" +
            "                    \"productType\": \"fee\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"5.0\",\n" +
            "                    \"productCode\": \"BOLT-DEPORTES-201802\",\n" +
            "                    \"productType\": \"video-addon\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"0.0\",\n" +
            "                    \"productCode\": \"BOLT-CDVR500FREE-201812\",\n" +
            "                    \"productType\": \"video-addon\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"14.99\",\n" +
            "                    \"productCode\": \"BOLT-HBO-201610\",\n" +
            "                    \"productType\": \"video-addon\",\n" +
            "                    \"promotions\": [\n" +
            "                        {\n" +
            "                            \"promotionId\": \"PHBO3MON\",\n" +
            "                            \"promotionStartDate\": \"04/03/2020\",\n" +
            "                            \"promotionEndDate\": \"07/03/2020\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"11.0\",\n" +
            "                    \"productCode\": \"BOLT-SHOWTIME-201707\",\n" +
            "                    \"productType\": \"video-addon\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"124.0\",\n" +
            "                    \"productCode\": \"BASE-XTRA-201811\",\n" +
            "                    \"productType\": \"video-plan\",\n" +
            "                    \"promotions\": [\n" +
            "                        {\n" +
            "                            \"promotionId\": \"PXTR12MCON\",\n" +
            "                            \"promotionStartDate\": \"04/03/2020\",\n" +
            "                            \"promotionEndDate\": \"04/03/2021\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"promotionId\": \"UMS_10OFFBBTV1M\",\n" +
            "                            \"promotionStartDate\": \"04/03/2020\",\n" +
            "                            \"promotionEndDate\": \"05/03/2020\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"promotionId\": \"10OFFOVERLAY\",\n" +
            "                            \"promotionStartDate\": \"05/03/2020\",\n" +
            "                            \"promotionEndDate\": \"05/03/2021\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"0.0\",\n" +
            "                    \"productCode\": \"BOLT-BRAZIL-201810\",\n" +
            "                    \"productType\": \"video-addon\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"5.0\",\n" +
            "                    \"productCode\": \"BOLT-MVPX-201910\",\n" +
            "                    \"productType\": \"video-addon\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"0.0\",\n" +
            "                    \"productCode\": \"BOLT-3RDSTREAMFREE-201804\",\n" +
            "                    \"productType\": \"video-addon\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"0.0\",\n" +
            "                    \"productCode\": \"HARD-OSPREY-042019\",\n" +
            "                    \"productType\": \"video-device\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"0.0\",\n" +
            "                    \"productCode\": \"HARD-OSPREY-042019\",\n" +
            "                    \"productType\": \"video-device\",\n" +
            "                    \"promotions\": [\n" +
            "                        {\n" +
            "                            \"promotionId\": \"OSPREYFREE\",\n" +
            "                            \"promotionStartDate\": \"03/31/2020\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"0.0\",\n" +
            "                    \"productCode\": \"HARD-OSPREY-042019\",\n" +
            "                    \"productType\": \"video-device\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"accountNumber\": \"200401143281152\",\n" +
            "            \"isActive\": true,\n" +
            "            \"retentionPromotionIndicator\": false,\n" +
            "            \"freeTrialEligible\": false,\n" +
            "            \"accountType\": \"Residential\",\n" +
            "            \"nextBillingDate\": \"05/03/2020\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"customerEligibility\": {\n" +
            "        \"zipCode\": [\n" +
            "            \"91911\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"offerActionType\": [\n" +
            "        \"Upgrade\",\n" +
            "        \"Downgrade\",\n" +
            "        \"Cross-sell\",\n" +
            "        \"Other\"\n" +
            "    ],\n" +
            "    \"offerProductFamily\": [\n" +
            "        \"OTT\"\n" +
            "    ],\n" +
            "    \"salesChannel\": [\n" +
            "        \"opus\"\n" +
            "    ],\n" +
            "    \"offerProductTypes\": [\n" +
            "        \"video-plan\"\n" +
            "    ]\n" +
            "}";

    @InjectMocks
    OttServicesIoOffersProcessor ottServicesIoOffersProcessor;

    @Mock
    CPOPBenefitsHelper cpopBenefitsHelper;

    @Mock
    CPOPProductsHelper cpopProductsHelper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    public void testProcessIoOffers(){

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        ObjectMapper objectMapperCusRes = new ObjectMapper();
        OfferRequest offerRequest = null;

        CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();

        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/OfferResponseWithIO.json"),CTOfferResponse.class);
        try {

            //ctOfferResponse = new DataReader().readFileToObj("/OfferResponseWithIO.json", CTOfferResponse.class);
            offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);

        } catch(Exception e) {
            //do nothing
            // System.out.println("Error : "+e);
        }



        offerRequest.setContractIndicator(Arrays.asList("contract"));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

        offerRequestWrapper.setOfferRequest(offerRequest);
        Mockito.when(cpopBenefitsHelper.filterInvalidBenefits(Mockito.any(), Mockito.any())).thenReturn(ctOfferResponse);
        Mockito.when(cpopProductsHelper.filterProductsByRetIOApplicableProducts(Mockito.any())).thenReturn(ctOfferResponse);
        Mockito.when(cpopProductsHelper.filterInvalidPrices(Mockito.any(),Mockito.any())).thenReturn(ctOfferResponse);
        ottServicesIoOffersProcessor.processIoOffers(offerRequestWrapper, ctOfferResponse.getIoOffers());
    }

    @Test
    public void testProcessIoOffersException(){

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        ObjectMapper objectMapperCusRes = new ObjectMapper();
        OfferRequest offerRequest = null;

        CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();

        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/OfferResponseWithIO.json"),CTOfferResponse.class);
        try {

            //ctOfferResponse = new DataReader().readFileToObj("/OfferResponseWithIO.json", CTOfferResponse.class);
            offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);

        } catch(Exception e) {
            //do nothing
            // System.out.println("Error : "+e);
        }



        offerRequest.setContractIndicator(Arrays.asList("contract"));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        //offerRequestWrapper.getOfferRequest().setCustomerContext(null);
        offerRequestWrapper.setOfferRequest(offerRequest);
        Mockito.when(cpopBenefitsHelper.filterInvalidBenefits(Mockito.any(), Mockito.any())).thenThrow(new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
                .addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001));
		ServiceException ex = catchThrowableOfType(() -> ottServicesIoOffersProcessor.processIoOffers(offerRequestWrapper, ctOfferResponse.getIoOffers()),
				ServiceException.class);
    }
    @Test
    public void testExtractNonIOOffers(){

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        ObjectMapper objectMapperCusRes = new ObjectMapper();
        OfferRequest offerRequest = null;

        CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();

        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/OfferResponseWithIO.json"),CTOfferResponse.class);
        try {

            //ctOfferResponse = new DataReader().readFileToObj("/OfferResponseWithIO.json", CTOfferResponse.class);
            offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);

        } catch(Exception e) {
            //do nothing
            // System.out.println("Error : "+e);
        }



        offerRequest.setContractIndicator(Arrays.asList("contract"));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

        offerRequestWrapper.setOfferRequest(offerRequest);
        Mockito.when(cpopBenefitsHelper.filterInvalidBenefits(Mockito.any(), Mockito.any())).thenReturn(ctOfferResponse);
        ottServicesIoOffersProcessor.extractNonIOOffers(ctOfferResponse.getOffers());
    }
}
