package com.dtv.dcp.epoch.processor.satellite.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.common.CouponResponseData;
import com.dtv.dcp.epoch.model.common.EligibilityContext;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.validatecoupons.Coupon;
import com.dtv.dcp.epoch.model.ct.coupon.PurchaseDetails;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class SatelliteServicesOffersProcessorUCCHelperTest {

	
	@InjectMocks
	SatelliteServicesOffersProcessorUCCHelper satelliteServicesOffersProcessorUCCHelper;
	
	@Mock
	SatelliteServicesOffersProcessor satelliteServicesOffersProcessor;

	private static final String CT_RESPONSE_COUPON_GET_OFFER = "CTResponsrCouponGetOffer.json";  
	private static final String COUPON_GET_OFFER = "couponGetOffer.json";

	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	
	}
	
	@Test
	public void getEmptyCTResponseTest() {
		CTOfferResponse response = satelliteServicesOffersProcessorUCCHelper.getEmptyCTResponse();
		assertNotNull(response);
	}
	
	@Test
	public void getCurrentTimeTest() {
		String currentTime = satelliteServicesOffersProcessorUCCHelper.getCurrentTime();
		assertNotNull(currentTime);
	}
	
	@Test
	public void getCouponPurchaseDetailsTest() {
		CustomerCoupons customerCoupons = new CustomerCoupons();
		customerCoupons.setBillingSystem("");
		customerCoupons.setCouponCode("ABC");
		customerCoupons.setCouponType("singleUse");
		customerCoupons.setDiscountAmount("$4.99");
		customerCoupons.setLockDuration("300");
		customerCoupons.setNetAmount("$1.99");
		customerCoupons.setRedemptionDate("2020-10-23 05:12:30.000+0000");
		customerCoupons.setRetailPrice("$4.99");
		customerCoupons.setStatus("R");
		customerCoupons.setStatusDescription("Redeemed");
		customerCoupons.setStatusUpdateTime("2020-10-23 05:12:59.701+0000");
		customerCoupons.setTitlePurchased("Choke SD");
		customerCoupons.setTmsProgramId("123");
		customerCoupons.setUsedDate("");
		PurchaseDetails purchaseDetails = satelliteServicesOffersProcessorUCCHelper.getCouponPurchaseDetails(customerCoupons);
		assertNotNull(purchaseDetails);
	}
	
	@Test
	public void getDifferenceTimeTest() {
		String diffTime = satelliteServicesOffersProcessorUCCHelper.getDifferenceTime("2020-09-23 11:34:41.378+0000");
		assertNotNull(diffTime);
	}

	@Test
	public void getDecimalPointTest() {
		String redemption = satelliteServicesOffersProcessorUCCHelper.getDecimalPoint("10");
		assertNotNull(redemption);
	}
	
	@Test
	public void updateTitlesResponseCTOfferResponseTest() {
		CTOfferResponse offerResponse = new DataReader().readFileToObj(CT_RESPONSE_COUPON_GET_OFFER,CTOfferResponse.class);
		satelliteServicesOffersProcessorUCCHelper.updateTitlesResponse(offerResponse);
	}
	
	@Test
	public void updateTitlesResponseCouponResponseDataTest() {
		CouponResponseData offerResponse = new DataReader().readFileToObj(COUPON_GET_OFFER,CouponResponseData.class);
		satelliteServicesOffersProcessorUCCHelper.updateTitlesResponse(offerResponse);
	}
	
	@Test
	public void getEligibiityTest() {
		List<EligibilityContext> campaignEligibility = new ArrayList<EligibilityContext>();
		EligibilityContext eligibilityContext = new EligibilityContext();
		eligibilityContext.setContentCategory("yes");
		eligibilityContext.setPurchaseType("rent");
		eligibilityContext.setTitle("Evc");
		campaignEligibility.add(eligibilityContext);
		satelliteServicesOffersProcessorUCCHelper.getEligibiity(campaignEligibility);
	}
	
	@Test
	public void purchaseDetailsUpdateForResponseTest() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		Coupon coupon = new Coupon();
		List<PurchaseDetails> purchaseDetailsResponseList = new ArrayList<PurchaseDetails>();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		//CTOfferResponse offerResponse = new DataReader().readFileToObj(CT_RESPONSE_COUPON_GET_OFFER,CTOfferResponse.class);
		OfferRequest offerRequest = new DataReader().readFileToObj("getOfferCouponOfferRequest.json",OfferRequest.class);
		//CustomerCouponsResults customerCouponsResults = new DataReader().readFileToObj("CustomerCouponsResults.json",CustomerCouponsResults.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
		Map<String, PurchaseDetails> purchaseDetailsList = new HashMap<String, PurchaseDetails>();
		PurchaseDetails purchaseDetails = new PurchaseDetails();
		purchaseDetails.setDiscountAmount("4.99");
		purchaseDetails.setNetAmount("4.99");
		purchaseDetails.setRetailPrice("0.00");
		purchaseDetails.setTitlePurchased("Choke SD");
		purchaseDetails.setTmsProgramID("");
		purchaseDetails.setUsedDate("2020-09-23 11:34:41.378+0000");
		purchaseDetailsList.put("abc", purchaseDetails);
		satelliteServicesOffersProcessorUCCHelper.purchaseDetailsUpdateForResponse(offerRequestWrapper, coupon, purchaseDetailsResponseList, purchaseDetails);
	}
	
	@Test
	public void updateUCCCTOfferResponseTest() {
		Coupon coupon = new Coupon();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CTOfferResponse offerResponse = new DataReader().readFileToObj(CT_RESPONSE_COUPON_GET_OFFER,CTOfferResponse.class);
		OfferRequest offerRequest = new DataReader().readFileToObj("getOfferCouponOfferRequest.json",OfferRequest.class);
		//CustomerCouponsResults customerCouponsResults = new DataReader().readFileToObj("CustomerCouponsResults.json",CustomerCouponsResults.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
		Map<String, PurchaseDetails> purchaseDetailsList = new HashMap<String, PurchaseDetails>();
		PurchaseDetails purchaseDetails = new PurchaseDetails();
		purchaseDetails.setDiscountAmount("4.99");
		purchaseDetails.setNetAmount("4.99");
		purchaseDetails.setRetailPrice("0.00");
		purchaseDetails.setTitlePurchased("Choke SD");
		purchaseDetails.setTmsProgramID("");
		purchaseDetails.setUsedDate("2020-09-23 11:34:41.378+0000");
		purchaseDetailsList.put("V38OYNCX9B", purchaseDetails);
		coupon.setCouponStatus("Available");
		Map<String, String> couponStatusList = mock(Map.class);
		couponStatusList.put("205PBWJDAH","Available");
		ReflectionTestUtils.setField(satelliteServicesOffersProcessor,"couponStatusList" ,couponStatusList);
		when(satelliteServicesOffersProcessor.couponStatusList.get(any())).thenReturn("Available");
		satelliteServicesOffersProcessorUCCHelper.updateUCCCTOfferResponse(satelliteServicesOffersProcessor, offerRequestWrapper, purchaseDetailsList, offerResponse);
	}

}
