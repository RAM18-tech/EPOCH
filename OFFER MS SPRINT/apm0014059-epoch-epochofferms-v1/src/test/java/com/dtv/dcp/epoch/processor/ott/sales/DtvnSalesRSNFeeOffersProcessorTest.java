package com.dtv.dcp.epoch.processor.ott.sales;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dtv.dcp.epoch.common.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.integration.rsn.RSNClient;
import com.dtv.dcp.epoch.integration.rsn.RSNFee;
import com.dtv.dcp.epoch.integration.rsn.RSNRequest;
import com.dtv.dcp.epoch.integration.rsn.RSNResponse;
import com.dtv.dcp.epoch.model.common.request.CartContext;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CTCartContext;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.TestUtility;
 class DtvnSalesRSNFeeOffersProcessorTest {

	@InjectMocks
	DtvnSalesRSNFeeOffersProcessor dtvnSalesRSNFeeOffersProcessor;
	
	@Mock
	RSNClient rsnClient;
	
	@Mock
	OffersUtils offersUtils;
	
    @Mock
    OttCTOffersProcessor ottCTOffersProcessor;
    
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	
	}
	@Test
	void testFilterRSNFeeOffers() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setOfferProductFamily(Stream.of("OTT").collect(Collectors.toList()));
		offerRequest.setSalesChannel(Stream.of("online").collect(Collectors.toList()));
		offerRequest.setOfferIds(Stream.of("784446dd-1e27-423f-8bf5-8ca9c87896b2").collect(Collectors.toList()));
		Pagination pagination = new Pagination();
		pagination.setLimit(300);
		pagination.setPage(1);
		offerRequest.setPagination(pagination);
		
		CartContext cartContext = new CartContext();
		offerRequest.setCartContext(cartContext);
		
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CTCartContext ctCartContext = new CTCartContext();
		ctCartContext.setOfferIds(Stream.of("784446dd-1e27-423f-8bf5-8ca9c87896b2").collect(Collectors.toList()));
		ctOfferRequest.setCartContext(ctCartContext);
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
		List<CTOffer> feeOffers = null;
		CTOfferResponse feeOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/CTFeeOfferResponse.json"),CTOfferResponse.class);
		CTOfferResponse ctFeeOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/FeeCTOfferResponse.json"),CTOfferResponse.class);
		feeOffers = ctFeeOfferResponse.getOffers();
		RSNResponse rsnResponse = new RSNResponse();
		RSNFee rsnFee = new RSNFee();
		rsnFee.setAmount(100.00);
		rsnFee.setSku("RSN-TIER1-201812");
		rsnResponse.setFee(rsnFee);
		doReturn(rsnResponse).when(rsnClient).getRSNInfo(ArgumentMatchers.<RSNRequest>any());
		doReturn(feeOfferResponse).when(ottCTOffersProcessor).getOfferByIds(ArgumentMatchers.<OfferRequestWrapper>any());
		dtvnSalesRSNFeeOffersProcessor.filterRSNFeeOffers(offerRequestWrapper, feeOffers);
		List<CTOffer> finalFeeOffers = feeOffers;
		assertDoesNotThrow(() -> dtvnSalesRSNFeeOffersProcessor.filterRSNFeeOffers(offerRequestWrapper, finalFeeOffers));
	}

    @Test
     void testBuildRSNRequestForRoadRunner() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        RSNRequest rsnRequest = new RSNRequest();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSalesChannel(List.of(Constants.DIRECTV_ONLINE));
        offerRequest.setContractIndicator(List.of(Constants.ROAD_RUNNER));
        offerRequest.setOfferProductFamily(List.of(Constants.OTT_PRODUCT_FAMILY));
        offerRequestWrapper.setOfferRequest(offerRequest);
        String billingProductCode = "RSN-TCTIER3-202404";
        assertDoesNotThrow(() -> dtvnSalesRSNFeeOffersProcessor.buildRSNRequest(offerRequestWrapper, rsnRequest, billingProductCode));
    }
}