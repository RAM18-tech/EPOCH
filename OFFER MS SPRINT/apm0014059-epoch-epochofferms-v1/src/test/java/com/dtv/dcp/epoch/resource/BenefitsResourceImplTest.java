package com.dtv.dcp.epoch.resource;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.service.ott.BenefitsServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BenefitsResourceImplTest {

    /** The OffersResource resource. */
    @InjectMocks
    private BenefitsResourceImpl benefitsResourceImpl;

    @Mock
    private BenefitsServiceImpl benefitsService;

    /** The headers. */
    HttpHeaders headers;

    ProductRequest productRequest;

    /** The uri info. */
    @Mock
    UriInfo mUriInfo;

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

    /**
     * Setup.
     */
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

        try {

            benefitRequest =  objectMapper.readValue(BENEFIT_REQUEST, BenefitRequest.class);
            BeanUtils.copyProperties(benefitRequest, ctBenefitsRequest);

            ctBenefitsResponse =  objectMapperRes.readValue(BENEFIT_RESPONSE, CTBenefitsResponse.class);
            // JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);//objectMapperRes.readValue(BENEFIT_RESPONSE, JsonNode.class);
        } catch(Exception e) {
            //do nothing
        }

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));


        doReturn(ctBenefitsResponse).when(benefitsService).getBenefits(headers, benefitRequest);
        CTBenefitsResponse response = benefitsResourceImpl.getBenefits(headers, benefitRequest);
        CTBenefitsResponse ctBenefitsResponse1 = benefitsService.getBenefits(headers, benefitRequest);


        assertNotNull(response);
        assertNotNull(ctBenefitsResponse1);
        // assertEquals(20, response.get.getCount());
    }


	/*
	 * @Test public void getBenefitsThrowsServiceException() {
	 * when(benefitsService.getBenefits(any(),any())) .thenThrow(new
	 * ServiceException("Test exception"));
	 * 
	 * assertThrows(ServiceException.class, () -> {
	 * benefitsResourceImpl.getBenefits(headers, any(), any()); }); }
	 */

    @Test
    public void getBenefitsThrowsGeneralException() throws Exception{
    	
    	ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        BenefitRequest benefitRequest = null;
        CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
        CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
        JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);
        
        
        try {

            benefitRequest =  objectMapper.readValue(BENEFIT_REQUEST, BenefitRequest.class);
            BeanUtils.copyProperties(benefitRequest, ctBenefitsRequest);

            ctBenefitsResponse =  objectMapperRes.readValue(BENEFIT_RESPONSE, CTBenefitsResponse.class);
            // JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);//objectMapperRes.readValue(BENEFIT_RESPONSE, JsonNode.class);
        } catch(Exception e) {
            //do nothing
        }
        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));
        
    	NullPointerException npe = new NullPointerException();

        doThrow(npe).when(benefitsService).getBenefits(headers, benefitRequest);
        
		/*
		 * ServiceException thrown = Assertions.assertThrows(ServiceException.class, ()
		 * -> { benefitsResourceImpl.getBenefits(headers, mUriInfo, benefitRequest); },
		 * "NumberFormatException was expected");
		 * 
		 * Assertions.assertEquals("For input string: \"One\"", thrown.getMessage());
		 */
     
    }

}
