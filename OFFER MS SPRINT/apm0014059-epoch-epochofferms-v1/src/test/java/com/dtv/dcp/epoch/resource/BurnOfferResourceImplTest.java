package com.dtv.dcp.epoch.resource;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.ct.burn.CommerceBurnResponse;
import com.dtv.dcp.epoch.model.ct.burn.CommerceToolError;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequest;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequestWrapper;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPAdditionalOfferHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BurnOfferResourceImplTest {

    private static final String request = "{\n" +
            "  \"promotions\": [\n" +
            "    {\n" +
            "      \"id\": \"some-id\",\n" +
            "      \"code\": \"12345\",\n" +
            "      \"coupon\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"some-id\",\n" +
            "      \"code\": \"12345\",\n" +
            "      \"coupon\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"some-id\",\n" +
            "      \"code\": \"12345\",\n" +
            "      \"coupon\": false\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private static final String commerceResponse = "{\n" +
            "  \"content\": \"success\",\n" +
            "  \"error\": {\n" +
            "    \"errorId\": \"\",\n" +
            "    \"message\": \"\"\n" +
            "  }\n" +
            "}";

    private static final String invalidRequest = "{\n" +
            "  \"promotions\": [\n" +
            "    {\n" +
            "      \"id\": \"some-id\",\n" +
            "      \"code\": \"12\",\n" +
            "      \"coupon\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"some-id\",\n" +
            "      \"code\": \"145\",\n" +
            "      \"coupon\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"some-id\",\n" +
            "      \"code\": \"145\",\n" +
            "      \"coupon\": false\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private static final String invalidRequest1 = "{\n" +
            "  \"promotions\": [\n" +
            "    {\n" +
            "      \"id\": \"\",\n" +
            "      \"code\": \"\",\n" +
            "      \"coupon\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"\",\n" +
            "      \"code\": \"\",\n" +
            "      \"coupon\": false\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private static final String errorRequest = "{\n" +
            "  \"promotions\": [\n" +
            "    {\n" +
            "      \"id\": \"some-id\",\n" +
            "      \"code\": \"12345\",\n" +
            "      \"coupon\": false\n"+
            "	 }\n"+
            "  ]\n" +
            "}";

    @Test
    public void testPerformBurn() throws Exception {
        CpopClient client = Mockito.mock(CpopClient.class);
        CPOPAdditionalOfferHelper helper = Mockito.mock(CPOPAdditionalOfferHelper.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        FeatureManagerHelper featureManagerHelper = Mockito.mock(FeatureManagerHelper.class);
        BurnOfferResourceImpl provider = new BurnOfferResourceImpl(client, helper,featureManagerHelper);

        ObjectMapper mapper = new ObjectMapper();
        OfferBurnRequestWrapper wrapper = mapper.readValue(request, OfferBurnRequestWrapper.class);

        assertEquals(3, wrapper.getPromotions().size());

        Map<String, Boolean> mockCache = new HashMap<>();
        mockCache.put("12345", true);

        Mockito.when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("12345");
        Mockito.when(helper.getQuotaBaseOfferMapFromCache("",""))
                .thenReturn(mockCache);
        Mockito.when(client.burnQuotaBasedPromotion(Mockito.any(OfferBurnRequest.class)))
                .thenReturn(new CommerceBurnResponse("success", null));

        OfferBurnResponse response = provider.burnReward(headers, wrapper);
        assertNotNull(response);
      //  assertEquals(200, response.getStatus());
    }

    @Test
    public void testPerformBurnWithQuotaNotInCache() throws Exception {
        CpopClient client = Mockito.mock(CpopClient.class);
        CPOPAdditionalOfferHelper helper = Mockito.mock(CPOPAdditionalOfferHelper.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        FeatureManagerHelper featureManagerHelper = Mockito.mock(FeatureManagerHelper.class);
        BurnOfferResource resource = new BurnOfferResourceImpl(client, helper,featureManagerHelper);

        ObjectMapper mapper = new ObjectMapper();
        OfferBurnRequestWrapper wrapper = mapper.readValue(invalidRequest, OfferBurnRequestWrapper.class);

        assertEquals(3, wrapper.getPromotions().size());

        Map<String, Boolean> mockCache = new HashMap<>();
        mockCache.put("12345", true);

        Mockito.when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("12345");
        Mockito.when(helper.getQuotaBaseOfferMapFromCache("",""))
                .thenReturn(mockCache);
        Mockito.when(client.burnQuotaBasedPromotion(Mockito.any(OfferBurnRequest.class)))
                .thenReturn(new CommerceBurnResponse("success", null));

        OfferBurnResponse response = resource.burnReward(headers, wrapper);
        assertNotNull(response);
        //assertEquals(200, response.getStatus());
    }

    @Test
    public void testParsingCommerceToolResponse() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CommerceBurnResponse response = mapper.readValue(commerceResponse, CommerceBurnResponse.class);
        assertEquals("", response.getError().getErrorId());
        assertEquals("", response.getError().getMessage());
        assertEquals("success", response.getContent());
    }
    @Test
    public void testInvalidBurnOfferRequest() throws Exception {
    	CpopClient client = Mockito.mock(CpopClient.class);
        CPOPAdditionalOfferHelper helper = Mockito.mock(CPOPAdditionalOfferHelper.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        FeatureManagerHelper featureManagerHelper = Mockito.mock(FeatureManagerHelper.class);
        BurnOfferResource resource = new BurnOfferResourceImpl(client, helper,featureManagerHelper);
        ObjectMapper mapper = new ObjectMapper();
        OfferBurnRequestWrapper wrapper = mapper.readValue(invalidRequest1, OfferBurnRequestWrapper.class);
        Mockito.when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("12345");
        OfferBurnResponse response = resource.burnReward(headers, wrapper);
        assertNotNull(response);
        //OfferBurnResponse burnResponse = (OfferBurnResponse) response.getEntity();
        assertEquals("OFFER_ERR_INV_REQUEST", response.getContent().getPromotions().get(0).getResult().getErrorCode());
    }
    
    @Test
    public void testPerformBurnWithQuotaReached() throws Exception {
        CpopClient client = Mockito.mock(CpopClient.class);
        CPOPAdditionalOfferHelper helper = Mockito.mock(CPOPAdditionalOfferHelper.class);
        HttpHeaders headers = Mockito.mock(HttpHeaders.class);
        FeatureManagerHelper featureManagerHelper = Mockito.mock(FeatureManagerHelper.class);
        BurnOfferResource resource = new BurnOfferResourceImpl(client, helper,featureManagerHelper);
        ObjectMapper mapper = new ObjectMapper();
        OfferBurnRequestWrapper wrapper = mapper.readValue(errorRequest, OfferBurnRequestWrapper.class);
        assertEquals(1, wrapper.getPromotions().size());
        Mockito.when(client.burnQuotaBasedPromotion(wrapper.getPromotions().get(0)))
                .thenReturn(new CommerceBurnResponse(null, new CommerceToolError("eV8353", "Quota Reached")));
        OfferBurnResponse burnResponse = resource.burnReward(headers, wrapper);
        assertNotNull(burnResponse);
        //assertEquals(200, response.getStatus());
        //OfferBurnResponse burnResponse = (OfferBurnResponse) response.getEntity();
        assertNotNull(burnResponse);
        assertEquals("OFFER_ERR_QUOTA_REACH_REWARD", burnResponse.getContent().getPromotions().get(0).getResult().getErrorCode());
        
        Mockito.when(client.burnQuotaBasedPromotion(wrapper.getPromotions().get(0)))
        .thenReturn(new CommerceBurnResponse(null, new CommerceToolError("eV8354", "Quota Reached")));
        OfferBurnResponse burnResponse1 = resource.burnReward(headers, wrapper);
        assertNotNull(burnResponse1);
        //assertEquals(200, response1.getStatus());
       // OfferBurnResponse burnResponse1 = (OfferBurnResponse) response1.getEntity();
        assertNotNull(burnResponse1);
        assertEquals("OFFER_ERROR_VALIDATION_FAILED", burnResponse1.getContent().getPromotions().get(0).getResult().getErrorCode());
        
        Mockito.when(client.burnQuotaBasedPromotion(wrapper.getPromotions().get(0)))
        .thenReturn(new CommerceBurnResponse(null, new CommerceToolError("eV8357", "Quota Reached")));
        OfferBurnResponse burnResponse2 = resource.burnReward(headers, wrapper);
        assertNotNull(burnResponse2);
        //assertEquals(200, response2.getStatus());
       // OfferBurnResponse burnResponse2 = (OfferBurnResponse) response2.getEntity();
        assertNotNull(burnResponse2);
        assertEquals("OFFER_ERROR_VALIDATION_FAILED", burnResponse2.getContent().getPromotions().get(0).getResult().getErrorCode());
        
        Mockito.when(client.burnQuotaBasedPromotion(wrapper.getPromotions().get(0)))
        .thenReturn(new CommerceBurnResponse(null, new CommerceToolError("system_error", "System Error")));
        OfferBurnResponse burnResponse3 = resource.burnReward(headers, wrapper);
        assertNotNull(burnResponse3);
       // assertEquals(200, response3.getStatus());
        //OfferBurnResponse burnResponse3 = (OfferBurnResponse) response3.getEntity();
        assertNotNull(burnResponse3);
        assertEquals("OFFER_ERROR", burnResponse3.getContent().getPromotions().get(0).getResult().getErrorCode());
        
        
    }
}