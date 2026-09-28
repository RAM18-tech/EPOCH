package com.dtv.dcp.epoch.processor.ott.services;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphServiceImpl;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OttProductsServicesProcessorTest {

    private String PRODUCT_REQUEST = "{\n" +
            "    \"accountTypes\": [\n" +
            "        \"Residential\"\n" +
            "    ],\n" +
            "    \"addOnType\": [],\n" +
            "    \"contractIndicator\": [\n" +
            "        \"contract\"\n" +
            "    ],\n" +
            "    \"contractApplicable\": [\n" +
            "        \"contract\"\n" +
            "    ],\n" +
            "    \"customerContext\": {\n" +
            "        \"existingProductFamily\": [\n" +
            "            \"OTT\"\n" +
            "        ],\n" +
            "        \"OTT\": {\n" +
            "            \"products\": [\n" +
            "                {\n" +
            "                    \"basePrice\": \"110.0\",\n" +
            "                    \"productCode\": \"BASE-CHOICE-201811\",\n" +
            "                    \"productType\": \"video-plan\",\n" +
            "                    \"promotions\": [\n" +
            "                        {\n" +
            "                            \"promotionId\": \"PCHO12MCON\",\n" +
            "                            \"promotionStartDate\": \"02/16/2020\",\n" +
            "                            \"promotionEndDate\": \"02/16/2021\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"promotionId\": \"$10OFDTV12\",\n" +
            "                            \"promotionStartDate\": \"02/16/2020\",\n" +
            "                            \"promotionEndDate\": \"02/16/2021\"\n" +
            "                        },\n" +
            "                        {\n" +
            "                            \"promotionId\": \"10OFFOVERLAY\",\n" +
            "                            \"promotionStartDate\": \"02/16/2020\",\n" +
            "                            \"promotionEndDate\": \"02/16/2021\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"0.0\",\n" +
            "                    \"productCode\": \"RSN-TIER4-201812\",\n" +
            "                    \"productType\": \"fee\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"basePrice\": \"0.0\",\n" +
            "                    \"productCode\": \"BOLT-CDVR500FREE-201812\",\n" +
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
            "                    \"productType\": \"video-device\",\n" +
            "                    \"promotions\": [\n" +
            "                        {\n" +
            "                            \"promotionId\": \"OSPREYFREE\",\n" +
            "                            \"promotionStartDate\": \"02/13/2020\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ],\n" +
            "            \"accountNumber\": \"190822152111954\",\n" +
            "            \"isActive\": true,\n" +
            "            \"retentionPromotionIndicator\": false,\n" +
            "            \"freeTrialEligible\": false,\n" +
            "            \"accountType\": \"Residential\",\n" +
            "            \"nextBillingDate\": \"03/16/2020\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"customerEligibility\": {\n" +
            "        \"zipCode\": [\n" +
            "            \"78407\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"productFamily\": [\n" +
            "        \"OTT\"\n" +
            "    ],\n" +
            "    \"salesChannel\": [\n" +
            "        \"online\"\n" +
            "    ],\n" +
            "    \"productTypes\": [\n" +
            "        \"video-plan\",\n" +
            "        \"video-device\",\n" +
            "        \"video-addon\"\n" +
            "    ]\n" +
            "}";

    @InjectMocks
    OttProductsServicesProcessor ottProductsServicesProcessor;

    @Mock
    OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;

    @Mock
    OttProductsServicesProcessorHelper ottProductsServicesProcessorHelper;

    @Mock
    OttAvaialbleOffersProcessor ottAvaialbleOffersProcessor;

    @Mock
    CustomerGraphServiceImpl customerGraphServiceImpl;

    @Mock
    CpopClientHelper cpopClientHelper;

    @Mock
    CPOPProductsHelper cpopProductsHelper;

    @Mock
    private FeatureManagerHelper featureManagerHelper;


    @Mock
    OttServicesBenefitsProcessor ottServicesBenefitsProcessor;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
    public void testProcessServices(){

        // Temp  need to update late once logic updated

        ObjectMapper objectMapper = new ObjectMapper();

        ProductRequest productRequest = null;
        CTProductResponse ctProductResponse = new CTProductResponse();
        CTProductRequest ctProductRequest = new CTProductRequest();
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        Map<String, String> leadAndAvailableGroup = new HashMap<>();
        leadAndAvailableGroup.put("available", "20000");
        leadAndAvailableGroup.put("lead", "20000");
        try {

            productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
            BeanUtils.copyProperties(productRequest, ctProductRequest);
            productRequestWrapper.setProductRequest(productRequest);
            ctProductResponse = new DataReader().readFileToObj("/productsVideoPlan.json",CTProductResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        productRequestWrapper.setDtvnAccount("190822152111954");
        when(ottServicesOffersProcessorHelper.findSalesChannel(Mockito.any(), Mockito.anyString())).thenReturn(Constants.SELF_SERVICE_EXISTING);
        when(ottProductsServicesProcessorHelper.getFilteredProducts(Mockito.any(), Mockito.anyString())).thenReturn(ctProductResponse);
        when(ottAvaialbleOffersProcessor.retrieveAvailablePackageGroup(Mockito.any(), Mockito.any())).thenReturn(leadAndAvailableGroup);
        when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        when(cpopProductsHelper.getVideoAddonProducts(Mockito.any())).thenReturn(ctProductResponse);
        when(cpopProductsHelper.getEquipmentsProducts(Mockito.any())).thenReturn(ctProductResponse);
        when(cpopProductsHelper.getSegmentDescription(Mockito.any())).thenReturn(Arrays.asList(Constants.RESIDENTIAL_ACCOUNT_TYPE));
        ottProductsServicesProcessor.processProductsServices(ctProductRequest, productRequestWrapper);

    }

    @Test
    public void testProcessServicesWithCustomerGraph(){

        // Temp  need to update late once logic updated

        ObjectMapper objectMapper = new ObjectMapper();

        ProductRequest productRequest = null;
        CTProductResponse ctProductResponse = new CTProductResponse();
        CTProductRequest ctProductRequest = new CTProductRequest();
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        Map<String, String> leadAndAvailableGroup = new HashMap<>();
        leadAndAvailableGroup.put("available", "20000");
        leadAndAvailableGroup.put("lead", "20000");
        CGResponse cgResponse = null;
        CTBenefitsResponse ctBenefitsResponse = null;
        try {

            productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
            BeanUtils.copyProperties(productRequest, ctProductRequest);
            productRequestWrapper.setProductRequest(productRequest);
            ctProductResponse = new DataReader().readFileToObj("/productsVideoPlan.json",CTProductResponse.class);
            cgResponse = new DataReader().readFileToObj("/CustomerGraphResponse.json", CGResponse.class);
            ctBenefitsResponse = new DataReader().readFileToObj("/BenefitDetailsResponse.json", CTBenefitsResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        productRequest.setCustomerContext(null);
        productRequestWrapper.setDtvnAccount("190822152111954");
        when(ottServicesOffersProcessorHelper.findSalesChannel(Mockito.any(), Mockito.anyString())).thenReturn(Constants.SELF_SERVICE_EXISTING);
        when(ottProductsServicesProcessorHelper.getFilteredProducts(Mockito.any(), Mockito.anyString())).thenReturn(ctProductResponse);
        when(ottAvaialbleOffersProcessor.retrieveAvailablePackageGroup(Mockito.any(), Mockito.any())).thenReturn(leadAndAvailableGroup);
        when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        when(cpopProductsHelper.getVideoAddonProducts(Mockito.any())).thenReturn(ctProductResponse);
        when(cpopProductsHelper.getEquipmentsProducts(Mockito.any())).thenReturn(ctProductResponse);
        when(cpopProductsHelper.getSegmentDescription(Mockito.any())).thenReturn(Arrays.asList(Constants.RESIDENTIAL_ACCOUNT_TYPE));
        when(customerGraphServiceImpl.getActiveSubscriptions(Mockito.anyString(), Mockito.anyString())).thenReturn(cgResponse);
        when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
        when(ottServicesBenefitsProcessor.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        ottProductsServicesProcessor.processProductsServices(ctProductRequest, productRequestWrapper);

    }
}
