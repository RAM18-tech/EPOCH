package com.dtv.dcp.epoch.processor.satellite.sales;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

@ExtendWith(MockitoExtension.class)
public class SatelliteSalesFeeProcessorTest {
	
	@InjectMocks
	SatelliteSalesFeeProcessor satelliteSalesFeeProcessor;
	
	@Mock
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;
	
	@Mock
	OffersUtils offersUtils;

	@Mock
	private FeatureManagerHelper featureHelper;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

	}
	
	private static String enabler_sales_getOffers_addon_request="acquisition/enabler_sales_getOffers_addon_request.json";
	private static String enabler_sales_getOffers_device_request="acquisition/enabler_sales_getOffers_device_request.json";
	private static String getOffers_sales_fee_response="acquisition/getOffers_sales_fee_response.json";
	private static String video_device_offers="acquisition/video-device.json";
	private static String video_addon_offer="enabler/oneSelected-offer.json";
	
	@Test
	public void testEnablerSalesFeeGetOffersForDevice() {
		
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		
		OfferRequest offerRequest = new DataReader().readFileToObj(enabler_sales_getOffers_device_request, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctSelectedOffersResponse = new DataReader().readFileToObj(video_device_offers, CTOfferResponse.class);
		
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).filterOffersBasedOnBillingSystem(any(), any());
		CTOfferResponse feeOfferResponse = new DataReader().readFileToObj(getOffers_sales_fee_response, CTOfferResponse.class);
		when(satelliteCTOffersProcessor.getSatelliteFeeOffers(any(), any())).thenReturn(feeOfferResponse);
		
		when(offersUtils.filterFeeOffersByFeeType(any(),any())).thenCallRealMethod();
		
		CTOfferResponse finalFeeOffers=satelliteSalesFeeProcessor.getSatelliteFeeOffers(offerRequestWrapper, ctSelectedOffersResponse.getOffers()
				, new HashMap<>());
		assertNotNull(finalFeeOffers);
		assertEquals(finalFeeOffers.getOffers().size(),2);
	}
	
	@Test
	public void testEnablerSalesFeeGetOffersForAddon() {
		
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		
		OfferRequest offerRequest = new DataReader().readFileToObj(enabler_sales_getOffers_addon_request, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctSelectedOffersResponse = new CTOfferResponse();
		List<CTOffer> offers=new ArrayList<>();
		CTOfferResponse offer = new DataReader().readFileToObj(video_addon_offer, CTOfferResponse.class);
		offers.addAll(offer.getOffers());
		ctSelectedOffersResponse.setOffers(offers);
		CTOfferResponse feeOfferResponse = new DataReader().readFileToObj(getOffers_sales_fee_response, CTOfferResponse.class);
		when(satelliteCTOffersProcessor.getSatelliteFeeOffers(any(), any())).thenReturn(feeOfferResponse);;
		
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).filterOffersBasedOnBillingSystem(any(), any());
		when(offersUtils.filterFeeOffersByFeeType(any(),any())).thenCallRealMethod();
		
		CTOfferResponse finalFeeOffers=satelliteSalesFeeProcessor.getSatelliteFeeOffers(offerRequestWrapper, ctSelectedOffersResponse.getOffers()
				, new HashMap<>());
		assertNotNull(finalFeeOffers);
		assertEquals(finalFeeOffers.getOffers().size(),3);
	}
	
	@Test
	public void testSTMSServiceActivationFeeGetOffers() {
		
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		
		OfferRequest offerRequest = new DataReader().readFileToObj("acquisition/SAF_getOffers_0$_request.json", OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctSelectedOffersResponse = new CTOfferResponse();
		List<CTOffer> offers=new ArrayList<>();
		CTOfferResponse offer = new DataReader().readFileToObj("stms/stms_deviceOffers_response.json", CTOfferResponse.class);
		offers.addAll(offer.getOffers());
		ctSelectedOffersResponse.setOffers(offers);
		CTOfferResponse feeOfferResponse = new DataReader().readFileToObj("acquisition/SAF_fee_response.json", CTOfferResponse.class);
		when(satelliteCTOffersProcessor.getSatelliteFeeOffers(any(), any())).thenReturn(feeOfferResponse);;
		when(featureHelper.isEnabled(Constants.FEATURE_EPOCH_SERVICE_ACTIVATION_FEE_ENABLED)).thenReturn(true);
		when(offersUtils.filterFeeOffersByFeeType(any(),any())).thenCallRealMethod();
		
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).applyFilterOnPriceforBillRefId(any(), any(), any());
		
		Optional<CTOffer> rackRate_offer = feeOfferResponse.getOffers().stream().filter(Objects::nonNull)
                .filter(ctOffer -> "OF_FEE-SERVICE-ACTIVATION-SALES-V1_satellite".equals(ctOffer.getCode())).findAny();
		 assertTrue(rackRate_offer.get().getAttributes().getAssociatedProducts().get(0)
		         .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices().size()==2);
		
		CTOfferResponse finalFeeOffers=satelliteSalesFeeProcessor.getSatelliteFeeOffers(offerRequestWrapper, ctSelectedOffersResponse.getOffers()
				, new HashMap<>());
		assertNotNull(finalFeeOffers);
		assertEquals(finalFeeOffers.getOffers().size(),2);
		 Optional<CTOffer> v2_offer = finalFeeOffers.getOffers().stream().filter(Objects::nonNull)
                 .filter(ctOffer -> "OF_FEE-SERVICE-ACTIVATION-SALES-V2_satellite".equals(ctOffer.getCode())).findAny();
         assertTrue(v2_offer.isPresent());
         
         Optional<CTOffer> v1_offer = finalFeeOffers.getOffers().stream().filter(Objects::nonNull)
                 .filter(ctOffer -> "OF_FEE-SERVICE-ACTIVATION-SALES-V1_satellite".equals(ctOffer.getCode())).findAny();
         assertTrue(v1_offer.isPresent());
         
         assertTrue(v1_offer.get().getAttributes().getAssociatedProducts().get(0)
         .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices().size()==1);
	}

}