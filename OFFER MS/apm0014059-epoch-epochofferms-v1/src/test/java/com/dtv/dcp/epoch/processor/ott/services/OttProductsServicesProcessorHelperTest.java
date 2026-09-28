package com.dtv.dcp.epoch.processor.ott.services;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OttProductsServicesProcessorHelperTest {

    @InjectMocks
    OttProductsServicesProcessorHelper ottProductsServicesProcessorHelper;

    @Mock
    CpopClientHelper cpopClientHelper;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

    }

    private String PRODUCT_REQUEST = "{\n" +
            "    \"accountTypes\": [\n" +
            "        \"Residential\"\n" +
            "    ],\n" +
            "    \"addOnType\": [],\n" +
            "    \"contractIndicator\": [\n" +
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
    @Test
    public void testGetFilteedProducts(){

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

        when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        ottProductsServicesProcessorHelper.getFilteredProducts(productRequestWrapper, Constants.MOBILITY);
        ottProductsServicesProcessorHelper.getFilteredProducts(productRequestWrapper, Constants.RESIDENTIAL_ACCOUNT_TYPE);
        ottProductsServicesProcessorHelper.getVideoAddonProducts(productRequestWrapper);


    }
    @Test 
    public void testGetFilteedProductsException(){

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

//        when(cpopClientHelper.getProducts(Mockito.any())).thenThrow(new ServiceException(ErrorMessages.CT_ERROR)
//                .addDetail(ErrorMessages.CT_ERROR_DETAILS));
        when(cpopClientHelper.getProducts(Mockito.any())).thenThrow(new ServiceException(ErrorMessages.CT_ERROR)
                .addDetail(ErrorMessages.CT_ERROR_DETAILS));
//        ottProductsServicesProcessorHelper.getFilteredProducts(productRequestWrapper, Constants.MOBILITY);
		ServiceException ex = catchThrowableOfType(() ->  ottProductsServicesProcessorHelper.getFilteredProducts(productRequestWrapper, "base"),
				ServiceException.class);
		assertNotNull(ex);
        productRequestWrapper.setMobility(true);
        ServiceException ex1 = catchThrowableOfType(() ->  ottProductsServicesProcessorHelper.getFilteredProducts(productRequestWrapper, "base"),
				ServiceException.class);
		assertNotNull(ex1);
        //ottProductsServicesProcessorHelper.getVideoAddonProducts(productRequestWrapper);


    }
    @Test
    public void testGetProductsFromCT(){

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

        when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);

        ottProductsServicesProcessorHelper.getProductsFromCT(ctProductRequest);

    }
    @Test 
    public void testGetProductsFromCTException(){

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

        when(cpopClientHelper.getProducts(Mockito.any())).thenThrow(new ServiceException(ErrorMessages.CT_ERROR)
                .addDetail(ErrorMessages.CT_ERROR_DETAILS));

        ServiceException ex = catchThrowableOfType(() ->  ottProductsServicesProcessorHelper.getProductsFromCT(ctProductRequest),
				ServiceException.class);
		assertNotNull(ex);

    }
    @Test
    public void testGetNonMobilityProducts(){

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

        when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);

        ottProductsServicesProcessorHelper.getNonMobilityProducts(productRequestWrapper, "base");
        ottProductsServicesProcessorHelper.getNonMobilityProducts(productRequestWrapper, "standalone");

    }
    @Test
    public void testExcludeCustomerContextWireless(){

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
       // productRequest.setCustomerContext(null);
        productRequestWrapper.setDtvnAccount("190822152111954");
        CTCustomerContext ctCustomerContext = new CTCustomerContext();
        ctCustomerContext.setProductFamily(productRequest.getProductFamily().get(0));
        ctCustomerContext.setProducts(new ArrayList<>());
        List<CTCustomerContext> list = new ArrayList<>();
        ctProductRequest.setCustomerContext(list);
        when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        productRequestWrapper.setCtProductRequest(ctProductRequest);
        ottProductsServicesProcessorHelper.excludeCustomerContextWireless(productRequestWrapper);


    }
}
