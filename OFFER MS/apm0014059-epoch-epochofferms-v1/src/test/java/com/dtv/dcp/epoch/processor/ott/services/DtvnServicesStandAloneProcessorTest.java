package com.dtv.dcp.epoch.processor.ott.services;

import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;

public class DtvnServicesStandAloneProcessorTest {
	@Mock
	OffersUtils offersUtils;
	
	@Mock
	CpopClientHelper cpopClientHelper;	

	@InjectMocks
	DtvnServicesStandAloneProcessor dtvnServicesStandAloneProcessor;

	public static final String COMPATIBLE_PRODUCTS = "[{\"constraints\":{\"dependent\":false,\"anchor\":false},\"products\":[{\"id\":\"b4f91c96-60b2-4474-acb0-9fa063f71285\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-EPIX-201907\"},{\"id\":\"afd250d0-9739-4035-9717-28fb6fa276a0\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLTNOW-STZ-201810\"}]}]";

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	@Test
	public void testPopulatePackageGroupStandAlone(){		
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setMobility(true);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setOfferProductType(Arrays.asList(Constants.VIDEO_ADDON));
		offerRequestWrapper.setOfferRequest(offerRequest);
		//offerRequestWrapper.getOfferRequest().setOfferProductType(Arrays.asList(Constants.VIDEO_ADDON));
		CTProductResponse ctProductResponse = new CTProductResponse();
		Map<String, String> currentServiceInfo = new HashMap<String,String>();
		List<String> basePackageCompatibleList= new ArrayList<>();
		CustomerSubscriptionDetail customerSubscriptionDetail = new CustomerSubscriptionDetail();
		customerSubscriptionDetail.setAddOnServiceIdList(Arrays.asList("PROMO-HBOSKINNY-201703"));

		ctProductResponse = getCtResponse(ctProductResponse);
		List<ProductWrapper> compatibleMobilityProducts = JsonService.getListObjectFromJsonTreeWithNoRootElement(COMPATIBLE_PRODUCTS, ProductWrapper.class);
		ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().setCompatibleMobilityProducts(compatibleMobilityProducts);
		when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		dtvnServicesStandAloneProcessor.populatePackageGroupStandAlone(offerRequestWrapper,customerSubscriptionDetail, currentServiceInfo,basePackageCompatibleList);
	}
	
	private CTProductResponse getCtResponse(CTProductResponse ctProductResponse) {
		try {
			ctProductResponse = new DataReader().readFileToObj("/ProductsStandAloneAddon.json",CTProductResponse.class);
		} catch(Exception e) {
			//do nothing
		}
		return ctProductResponse;
	}

	@Test
	public void testPopulatePackagePromoEndDate() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setMobility(true);
		CTProductResponse ctProductResponse = new CTProductResponse();
		Map<String, String> currentServiceInfo = new HashMap<String, String>();
		List<String> basePackageCompatibleList = new ArrayList<>();
		CustomerSubscriptionDetail customerSubscriptionDetail = new CustomerSubscriptionDetail();
		customerSubscriptionDetail.setAddOnServiceIdList(Arrays.asList("PROMO-HBOSKINNY-201703"));
		Date currentDatePlusOneDay = new Date();
		LocalDateTime localDateTime = currentDatePlusOneDay.toInstant().atZone(ZoneId.systemDefault())
				.toLocalDateTime();

		localDateTime = localDateTime.plusYears(1).plusMonths(1).plusDays(1);
		localDateTime = localDateTime.plusHours(1).plusMinutes(2).minusMinutes(1).plusSeconds(1);
		Date promoEndDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
		customerSubscriptionDetail.setPromoEndDate(promoEndDate);

		ctProductResponse = getCtResponse(ctProductResponse);
		when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		dtvnServicesStandAloneProcessor.populatePackageGroupStandAlone(offerRequestWrapper, customerSubscriptionDetail,
				currentServiceInfo, basePackageCompatibleList);
	}
}