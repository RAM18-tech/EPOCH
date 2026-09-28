package com.dtv.dcp.epoch.integration.common;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.OfferFilterService;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityAttribute;
import com.dtv.dcp.epoch.model.ct.offer.GlobalEligibilityRule;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OfferFilterServiceTest {
	
	@InjectMocks
	OfferFilterService offerFilterService;
	
	@Mock
	RedisCacheHelper redisCacheHelper;
	
	String OFFER_REQUEST="{\r\n"
			+ "  \"pagination\" : {\r\n"
			+ "    \"page\" : 1,\r\n"
			+ "    \"limit\" : 100\r\n"
			+ "  },\r\n"
			+ "  \"offerActionType\" : [ \"Acquisition\" ],\r\n"
			+ "  \"salesChannel\" : [ \"opus\" ],\r\n"
			+ "  \"offerProductTypes\" : [ \"video-addon\"],\r\n"
			+ "  \"offerProductFamily\" : [ \"OTT\" ],\r\n"
			+ "  \"addOnType\" : [ \"Programming-Bolt-on\", \"Bolt-on\", \"Standalone\", \"Subscription\" ],\r\n"
			+ "  \"planSubType\" : [ \"cDVR\", \"Cross-Product-Mobility\", \"Streams\", \"CrossProduct-WatchTV\", \"Cross-product\", \"International\" ],\r\n"
			+ "  \"contractIndicator\" : [ \"TAZCONTRACT\" ],\r\n"
			+ "  \"channelEligibility\" : {\r\n"
			+ "    \"opusChannel\" : \"AEG\",\r\n"
			+ "    \"opusSubChannel\" : \"AEG\",\r\n"
			+ "    \"opusStoreId\" : \"X53P\"\r\n"
			+ "  }\r\n"
			+ "}";
	
	@BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

	@Test
	public void testApplyFilters() {        
        ObjectMapper objectMapper = new ObjectMapper();
        OfferRequest offerRequest = null;
		try {
			offerRequest = objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
		} catch (Exception e) {
			System.out.println(e.getMessage());

		}
		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("CTOfferResponse_Test.json", CTOfferResponse.class);
        
		List<GlobalEligibilityRule> epochGloablEligibilityRules = new ArrayList<GlobalEligibilityRule>();
        epochGloablEligibilityRules.add(getGlobalEligibilityRule());        
        Mockito.when(redisCacheHelper.getGlobalEligibilityRules(Constants.OTT)).thenReturn(epochGloablEligibilityRules);
        
        ctOfferResponse =offerFilterService.applyFilters(offerRequest, ctOfferResponse);
        
        Assertions.assertNotNull(ctOfferResponse, "Successfully covered the applyFilters test");
        Assertions.assertEquals(ctOfferResponse.getOffers().size(), 2);
	}
	
	@Test
	public void testApplyFiltersWhenException() {        
        ObjectMapper objectMapper = new ObjectMapper();
        OfferRequest offerRequest = null;
		try {
			offerRequest = objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
		} catch (Exception e) {
			System.out.println(e.getMessage());

		}
		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("CTOfferResponse_Test.json", CTOfferResponse.class);
        
		List<GlobalEligibilityRule> epochGloablEligibilityRules = new ArrayList<GlobalEligibilityRule>();
        epochGloablEligibilityRules.add(getBadGlobalEligibilityRule());        
        Mockito.when(redisCacheHelper.getGlobalEligibilityRules(Constants.OTT)).thenReturn(epochGloablEligibilityRules);
		
        ctOfferResponse =offerFilterService.applyFilters(offerRequest, ctOfferResponse);
        Assertions.assertEquals(ctOfferResponse.getOffers().size(), 0);
	}
	
	private GlobalEligibilityRule getGlobalEligibilityRule() {
    	GlobalEligibilityRule rule=new GlobalEligibilityRule();
        rule.setPredicate("salesChannel && opusStoreId");
        
        GlobalEligibilityAttribute atr = new GlobalEligibilityAttribute();
        atr.setAttributeName("salesChannel");
        atr.setRequestAttributePath("salesChannel");
        atr.setStaticValue("request.contains(\"opus\")");
        
        GlobalEligibilityAttribute atr2 = new GlobalEligibilityAttribute();
        atr2.setAttributeName("opusStoreId");
        atr2.setRequestAttributePath("channelEligibility.opusStoreId");
        atr2.setResponseAttributePath("attributes.opusStoreIds");
        atr2.setStaticValue("response.contains(request)");
        
        rule.setEligibilityAtributes(List.of(atr, atr2));
        return rule;
    }
	
	private GlobalEligibilityRule getBadGlobalEligibilityRule() {
    	GlobalEligibilityRule rule=new GlobalEligibilityRule();
        rule.setPredicate("salesChannel && opusStoreId");
        
        GlobalEligibilityAttribute atr = new GlobalEligibilityAttribute();
        atr.setAttributeName("salesChannel");
        atr.setRequestAttributePath("salesChanel");
        atr.setStaticValue("request.contains(\"opus\")");
        
        GlobalEligibilityAttribute atr2 = new GlobalEligibilityAttribute();
        atr2.setAttributeName("opusStoreId");
        atr2.setRequestAttributePath("channelEligibility.opusStoreId");
        atr2.setResponseAttributePath("attributes.opusStoreIds");
        atr2.setStaticValue("response.contains(request)");
        
        rule.setEligibilityAtributes(List.of(atr, atr2));
        return rule;
    }
}
