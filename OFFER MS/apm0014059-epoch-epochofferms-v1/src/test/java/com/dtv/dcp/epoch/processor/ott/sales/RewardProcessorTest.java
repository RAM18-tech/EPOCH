package com.dtv.dcp.epoch.processor.ott.sales;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.TestUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RewardProcessorTest {

    @Mock
    OttCTOffersProcessor ottCTOffersProcessor;

    @InjectMocks
    RewardProcessor rewardProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRewardOffersReturnsExpectedOffers() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSalesChannel(Arrays.asList("directIntegrationPartner"));
        offerRequest.setMarketingSourceCode(Arrays.asList("DIP12"));
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/RewardCardOffers.json"),CTOfferResponse.class);

        when(ottCTOffersProcessor.getRewardOffers(offerRequestWrapper)).thenReturn(ctOfferResponse);

        CTOfferResponse actualResponse = rewardProcessor.getRewardOffers(offerRequestWrapper);

        assertEquals(ctOfferResponse, actualResponse);
    }

    @Test
    void getRewardOffersFiltersOffersWhenDirectIntegrationPartner() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSalesChannel(Arrays.asList("directIntegrationPartner"));
        offerRequest.setMarketingSourceCode(Arrays.asList("nonmatching"));
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/RewardCardOffers.json"),CTOfferResponse.class);

        when(ottCTOffersProcessor.getRewardOffers(offerRequestWrapper)).thenReturn(ctOfferResponse);

        CTOfferResponse actualResponse = rewardProcessor.getRewardOffers(offerRequestWrapper);

        assertTrue(actualResponse.getOffers().isEmpty());
    }

    @Test
    public void getRewardOfferWhenNoMarketingCodeInRequest() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSalesChannel(Arrays.asList("directIntegrationPartner"));

        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/RewardCardOffers.json"), CTOfferResponse.class);

        when(ottCTOffersProcessor.getRewardOffers(offerRequestWrapper)).thenReturn(ctOfferResponse);

        CTOfferResponse actualResponse = rewardProcessor.getRewardOffers(offerRequestWrapper);

        assertEquals(0, actualResponse.getOffers().size());
    }

    @Test
    void getRewardOffersReturnsWithAssistedSalesMSC() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSalesChannel(Arrays.asList(Constants.ASSISTED_SALES));
        offerRequest.setMarketingSourceCode(Arrays.asList("3067"));
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/assistedSales/assistedSales_reward_response.json"),CTOfferResponse.class);

        when(ottCTOffersProcessor.getRewardOffers(offerRequestWrapper)).thenReturn(ctOfferResponse);

        CTOfferResponse actualResponse = rewardProcessor.getRewardOffers(offerRequestWrapper);
        assertEquals(ctOfferResponse, actualResponse);
    }
}