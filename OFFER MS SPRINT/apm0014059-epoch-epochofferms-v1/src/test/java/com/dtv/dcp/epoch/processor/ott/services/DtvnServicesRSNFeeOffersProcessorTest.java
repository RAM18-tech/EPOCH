package com.dtv.dcp.epoch.processor.ott.services;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.rsn.RSNRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

 class DtvnServicesRSNFeeOffersProcessorTest {

    @InjectMocks
    DtvnServicesRSNFeeOffersProcessor dtvnServicesRSNFeeOffersProcessor;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

    }

    @Test
     void testbuildRSNRequestForRoadRunner() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        RSNRequest rsnRequest = new RSNRequest();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSalesChannel(List.of(Constants.DIRECTV_ONLINE));
        offerRequest.setContractIndicator(List.of(Constants.ROAD_RUNNER));
        offerRequest.setOfferProductFamily(List.of(Constants.OTT_PRODUCT_FAMILY));
        offerRequestWrapper.setOfferRequest(offerRequest);
        CustomerSubscriptionDetail customerSubscriptionDetail = new CustomerSubscriptionDetail();
        assertDoesNotThrow(() -> dtvnServicesRSNFeeOffersProcessor.buildRSNRequest(offerRequestWrapper, customerSubscriptionDetail,rsnRequest));
    }
}
