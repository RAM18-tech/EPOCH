package com.dtv.dcp.epoch.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dtv.dcp.epoch.model.common.request.*;
import com.dtv.dcp.epoch.model.ct.eligibility.Constraint;
import com.dtv.dcp.epoch.model.ct.eligibility.Eligibility;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.DelayProvisioningMessagesByKey;
import com.dtv.dcp.epoch.model.ct.offer.MessageEntry;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.offer.AdditionalEligibility;
import com.dtv.dcp.epoch.model.ct.product.*;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringRunner;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.common.DtvnMidasRule;
import com.dtv.dcp.epoch.model.common.EnterpriseRule;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGAccountInfo;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.processor.ott.services.CustomerSubscriptionDetail;
import com.dtv.dcp.epoch.service.DMALookUpService;

@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
class OffersUtilsTest {

	@Mock
	EnterpriseRule enterpriseRule;

	@InjectMocks
	OffersUtils utils;

    @Mock
    private FeatureManagerHelper featureManagerHelper;

	@Mock
	RedisCacheHelper redisCacheHelper;

	@Mock
	DMALookUpService dmaLookUpService;

	@InjectMocks
	private OffersUtils offersUtils;


	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	private static String enabler_withIsMigrationRequiredFlag_request="enabler/OnAccount_request.json";

	@Test
	 void testFetchDtvnMidasRules() {
		List<DtvnMidasRule> rules = utils.fetchDtvnMidasRules();
		assertTrue(rules.isEmpty());
	}


	@Test
	void testIsAlphanumeric() {
		assertTrue(OffersUtils.isAlphanumeric("123err"));
	}

	@Test
	void testValidateDateFormat() {
		Map<String, String> dates = new HashMap<String, String>();
		dates.put("startDate", "11/26/2019 10:10:10");
		assertNotNull(OffersUtils.validateDateFormat(dates));
		assertTrue(OffersUtils.validateDateFormat(null).isEmpty());
	}

	@Test
	 void testValidateDateFormat_exception() {
		Map<String, String> dates = new HashMap<String, String>();
		dates.put("startDate", "sdfhjkhkljklhjkl");
		OffersUtils.validateDateFormat(dates);
	}

	@Test
	void testIsActive() {
		assertTrue(OffersUtils.validateActiveDates("11/26/2019 10:10:10", "12/26/2030 10:10:10"));
	}

	@Test
	 void testActiveDate() {
		assertTrue(OffersUtils.isDateActive("11/26/2019 10:10:10"));
	}


	@Test
	 void testFilterNonActivationOffers() {
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/FeeCTOfferResponse.json"), CTOfferResponse.class);
		List<CTOffer> filteredOffers = utils.filterOtherFeeOffersByFeeType(ctOfferResponse.getOffers(), "RSNFee");
		assertNotNull(filteredOffers);
		assertEquals(1, filteredOffers.size());
	}

	@Test
	 void filterFeeOffersByFeeType() {
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/FeeCTOfferResponse.json"), CTOfferResponse.class);
		List<CTOffer> filteredOffers = utils.filterFeeOffersByFeeType(ctOfferResponse.getOffers(), "RSNFee");
		assertNotNull(filteredOffers);
		assertEquals(6, filteredOffers.size());
	}

	@Test
	void testConvertLongDateToStringDate() {
		String myDate = OffersUtils.convertLongDateToStringDate("1571767033000");
		assertNotNull(myDate);
	}

	@Test
	 void testServiceFlowUpgrade() {
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerActionTypeUpgrade = new ArrayList<>();
		offerActionTypeUpgrade.add(Constants.UPGRADE_ACTION_TYPE);
		offerRequest.setOfferActionType(offerActionTypeUpgrade);
		boolean condition = utils.isService(offerRequest);
		assertTrue(condition);

		OfferRequest offerRequest1 = new OfferRequest();
		List<String> offerActionTypeDownGrade = new ArrayList<>();
		offerActionTypeDownGrade.add(Constants.DOWNGRADE_ACTION_TYPE);
		offerRequest1.setOfferActionType(offerActionTypeDownGrade);
		boolean condition1 = utils.isService(offerRequest1);
		assertTrue(condition1);

		OfferRequest offerRequest2 = new OfferRequest();
		List<String> offerActionTypeCrossSell = new ArrayList<>();
		offerActionTypeCrossSell.add(Constants.CROSS_SELL_ACTION_TYPE);
		offerRequest2.setOfferActionType(offerActionTypeCrossSell);
		boolean condition2 = utils.isService(offerRequest2);
		assertTrue(condition2);

		OfferRequest offerRequest3 = new OfferRequest();
		List<String> offerActionTypeOther = new ArrayList<>();
		offerActionTypeOther.add(Constants.OTHER_ACTION_TYPE);
		offerRequest3.setOfferActionType(offerActionTypeOther);
		boolean condition3 = utils.isService(offerRequest3);
		assertTrue(condition3);


		OfferRequest offerRequest4 = new OfferRequest();
		List<String> offerActionTypeUpgrade4 = new ArrayList<>();
		offerActionTypeUpgrade4.add(Constants.UPGRADE_ACTION_TYPE);
		offerRequest4.setOfferActionType(offerActionTypeUpgrade);
		offerRequest4.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
		boolean condition4 = utils.isOTTService(offerRequest4);
		assertTrue(condition4);

		OfferRequest offerRequest8 = new OfferRequest();
		List<String> offerActionTypeDownGrade8 = new ArrayList<>();
		offerActionTypeDownGrade8.add(Constants.DOWNGRADE_ACTION_TYPE);
		offerRequest8.setOfferActionType(offerActionTypeDownGrade8);
		offerRequest8.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
		boolean condition8 = utils.isOTTService(offerRequest8);
		assertTrue(condition8);

		OfferRequest offerRequest9 = new OfferRequest();
		List<String> offerActionTypeCrossSell9 = new ArrayList<>();
		offerActionTypeCrossSell9.add(Constants.CROSS_SELL_ACTION_TYPE);
		offerRequest9.setOfferActionType(offerActionTypeCrossSell9);
		offerRequest9.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
		boolean condition9 = utils.isOTTService(offerRequest9);
		assertTrue(condition9);

		OfferRequest offerRequest10 = new OfferRequest();
		List<String> offerActionTypeOther10 = new ArrayList<>();
		offerActionTypeOther10.add(Constants.OTHER_ACTION_TYPE);
		offerRequest10.setOfferActionType(offerActionTypeOther10);
		offerRequest10.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
		boolean condition10 = utils.isOTTService(offerRequest10);
		assertTrue(condition10);

		OfferRequest offerRequest5 = new OfferRequest();
		List<String> offerActionTypeDownGrade5 = new ArrayList<>();
		offerActionTypeDownGrade5.add(Constants.DOWNGRADE_ACTION_TYPE);
		offerRequest5.setOfferActionType(offerActionTypeDownGrade5);
		offerRequest5.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
		boolean condition5 = utils.isOTTService(offerRequest4);
		assertTrue(condition5);

		OfferRequest offerRequest6 = new OfferRequest();
		List<String> offerActionTypeCrossSell6 = new ArrayList<>();
		offerActionTypeCrossSell6.add(Constants.CROSS_SELL_ACTION_TYPE);
		offerRequest6.setOfferActionType(offerActionTypeCrossSell6);
		offerRequest6.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
		boolean condition6 = utils.isOTTService(offerRequest4);
		assertTrue(condition6);

		OfferRequest offerRequest7 = new OfferRequest();
		List<String> offerActionTypeOther7 = new ArrayList<>();
		offerActionTypeOther7.add(Constants.OTHER_ACTION_TYPE);
		offerRequest7.setOfferActionType(offerActionTypeOther7);
		offerRequest7.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
		boolean condition7 = utils.isOTTService(offerRequest4);
		assertTrue(condition7);
	}

	@Test
	 void testServiceCTFlowUpgrade() {
		CTOfferRequest offerRequest = new CTOfferRequest();
		List<String> offerActionTypeUpgrade = new ArrayList<>();
		offerActionTypeUpgrade.add(Constants.UPGRADE_ACTION_TYPE);
		offerRequest.setOfferActionType(offerActionTypeUpgrade);
		boolean condition = utils.isServiceCT(offerRequest);
		assertTrue(condition);

		OfferRequest offerRequest1 = new OfferRequest();
		List<String> offerActionTypeDownGrade = new ArrayList<>();
		offerActionTypeDownGrade.add(Constants.DOWNGRADE_ACTION_TYPE);
		offerRequest1.setOfferActionType(offerActionTypeDownGrade);
		boolean condition1 = utils.isService(offerRequest1);
		assertTrue(condition1);

		OfferRequest offerRequest2 = new OfferRequest();
		List<String> offerActionTypeCrossSell = new ArrayList<>();
		offerActionTypeCrossSell.add(Constants.CROSS_SELL_ACTION_TYPE);
		offerRequest2.setOfferActionType(offerActionTypeCrossSell);
		boolean condition2 = utils.isService(offerRequest2);
		assertTrue(condition2);

		OfferRequest offerRequest3 = new OfferRequest();
		List<String> offerActionTypeOther = new ArrayList<>();
		offerActionTypeOther.add(Constants.OTHER_ACTION_TYPE);
		offerRequest3.setOfferActionType(offerActionTypeOther);
		boolean condition3 = utils.isService(offerRequest3);
		assertTrue(condition3);

		CTOfferRequest offerRequest4 = new CTOfferRequest();
		List<String> offerActionTypeDowngrade4 = new ArrayList<>();
		offerActionTypeDowngrade4.add(Constants.DOWNGRADE_ACTION_TYPE);
		offerRequest.setOfferActionType(offerActionTypeDowngrade4);
		boolean condition4 = utils.isServiceCT(offerRequest);
		assertTrue(condition4);

		CTOfferRequest offerRequest5 = new CTOfferRequest();
		List<String> offerActionTypeCrossSell5 = new ArrayList<>();
		offerActionTypeCrossSell5.add(Constants.CROSS_SELL_ACTION_TYPE);
		offerRequest.setOfferActionType(offerActionTypeCrossSell5);
		boolean condition5 = utils.isServiceCT(offerRequest);
		assertTrue(condition5);

		CTOfferRequest offerRequest6 = new CTOfferRequest();
		List<String> offerActionTypeOther6 = new ArrayList<>();
		offerActionTypeOther6.add(Constants.OTHER_ACTION_TYPE);
		offerRequest.setOfferActionType(offerActionTypeOther6);
		boolean condition6 = utils.isServiceCT(offerRequest);
		assertTrue(condition6);
	}
	

	@Test
	 void testGetUverseCustomerAccountType() {
		CGResponse cgResponse =  new CGResponse();
		CGAccountInfo cgAccountInfo = new CGAccountInfo();
		cgAccountInfo.setAccountType("Employee");
		cgResponse.setAccountInfo(cgAccountInfo);
		String accountType = OffersUtils.getUverseCustomerAccountType(cgResponse);
		assertNotNull(accountType);
	}
	
	@Test
	 void testIsSTMSRequest() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new DataReader().readFileToObj(enabler_withIsMigrationRequiredFlag_request, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		assertTrue(utils.isSTMSRequest(offerRequestWrapper));
	}

	@Test
	  void testContractIntentForRR(){
		DtvnMidasRule dtvnRule =new DtvnMidasRule();
		dtvnRule.setContractIntent(Constants.ROAD_RUNNER);
		assertTrue(OffersUtils.getContractIntentForRR(dtvnRule));
	}
	@Test
	  void testContainsROADRUNNER(){
		OfferRequest offerRequest = new DataReader().readFileToObj("/roadrunner/RoadRunnerOfferRequest.json", OfferRequest.class);
		assertTrue(utils.containsROADRUNNER(offerRequest));
	}

	@Test
	void testCheckIsOemChannel(){
		OfferRequest offerRequest = new DataReader().readFileToObj("/IAP/IAPValidRequest.json", OfferRequest.class);
		assertTrue(utils.checkIsOemChannel(offerRequest));
	}

	@Test
	void testCheckOnlineRelatedChannel(){
		assertTrue(OffersUtils.checkOnlineRelatedChannel(Constants.OEM_IAP_GOOGLE));
	}

	@Test
	void testNeedsAttributeRemoval() {
		// Test case where attribute removal is needed
		List<String> offerProductFamilies = Arrays.asList(Constants.SATELLITE_PRODUCT_FAMILY);
		List<String> offerTypes = Collections.emptyList();
		List<String> productTypes = Collections.emptyList();
		assertTrue(OffersUtils.needsAttributeRemoval(offerProductFamilies, offerTypes, productTypes));

		// Test case where attribute removal is not needed due to offerTypes containing Constants.COUPON
		offerTypes = Arrays.asList(Constants.COUPON);
		assertFalse(OffersUtils.needsAttributeRemoval(offerProductFamilies, offerTypes, productTypes));

		// Test case where attribute removal is not needed due to productTypes containing Constants.CAMPAIGN
		offerTypes = Collections.emptyList();
		productTypes = Arrays.asList(Constants.CAMPAIGN);
		assertFalse(OffersUtils.needsAttributeRemoval(offerProductFamilies, offerTypes, productTypes));

		// Test case where attribute removal is not needed due to offerProductFamilies not containing Constants.SATELLITE_PRODUCT_FAMILY
		offerProductFamilies = Arrays.asList("OtherProductFamily");
		productTypes = Collections.emptyList();
		assertFalse(OffersUtils.needsAttributeRemoval(offerProductFamilies, offerTypes, productTypes));
	}

	@Test
	void testExcludeStandaloneInSubscription() {
		CustomerSubscriptionDetail customerSubscriptionDetail = new CustomerSubscriptionDetail();
		List<CTOffer> filteredStandAloneOffers = new ArrayList<>();
		CTOfferResponse ctOfferResponse = JsonService
				.getObjectFromJson(TestUtility.loadJson("/FeeCTOfferResponse.json"), CTOfferResponse.class);
		utils.excludeStandaloneInSubscription(customerSubscriptionDetail, ctOfferResponse.getOffers(), filteredStandAloneOffers);
		assertNotNull(filteredStandAloneOffers);
	}

	@Test
	void testIsPerformanceUpdatesEnabled() {
		assertFalse(utils.isPerformanceUpdatesEnabled(List.of(Constants.INDIRECT_PARTNER)));
	}


	@Test
	void testIsADEFlow() {
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		ctOfferRequest.setOfferActionType(Arrays.asList("Other"));
		ctOfferRequest.setSalesChannel(Arrays.asList("online"));
		ctOfferRequest.setOfferProductFamily(Arrays.asList("satellite"));

		when(featureManagerHelper.isEnabled("true")).thenReturn(true);

		boolean result = utils.isADEFlow(ctOfferRequest, "true");
		assertTrue(result);

		ctOfferRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION_ACTION_TYPE));
		result = utils.isADEFlow(ctOfferRequest, "true");
		assertFalse(result);

		ctOfferRequest.setOfferActionType(Arrays.asList("Other"));
		ctOfferRequest.setSalesChannel(Arrays.asList("offline"));
		result = utils.isADEFlow(ctOfferRequest, "true");
		assertFalse(result);

		ctOfferRequest.setSalesChannel(Arrays.asList("online"));
		ctOfferRequest.setOfferProductFamily(Arrays.asList("Other"));
		result = utils.isADEFlow(ctOfferRequest, "true");
		assertFalse(result);
	}

	@Test
	void testDistinctByKey() {
		// Test data
		List<String> testData = Arrays.asList("OF_RSN-TIER1-201812_contract", "OF_RSN-TIER5-201812_contract", "OF_FEE-ACTIVATION-201901_contract","OF_RSN-TIER1-201812_contract", "OF_RSN-TIER5-201812_contract", "OF_FEE-ACTIVATION-201901_contract");
		// Expected result
		List<String> expectedResult = Arrays.asList("OF_RSN-TIER1-201812_contract", "OF_RSN-TIER5-201812_contract", "OF_FEE-ACTIVATION-201901_contract");

		// Invoke method
		List<String> result = testData.stream()
				.filter(OffersUtils.distinctByKey(Function.identity()))
				.collect(Collectors.toList());

		// Assertions
		assertEquals(expectedResult, result);
	}

	@Test
	void testGetKey() {
		String primaryKey = "testPrimaryKey";
		String expectedKey = "testPrimaryKey_" + Constants.CACHE_NAME + "_" + Constants.OBJECT_KEY + "_" + Constants.CACHE_VERSION_ID + "_" + Constants.APPLICATION_ID;
		String result = OffersUtils.getKey(primaryKey);
		assertEquals(expectedKey, result);
	}

	@Test
	void test_filterOffersByProductsHavingSubCategoryLocals() {
		CTOfferResponse offerResponse = getOfferResponse();
		List<CTOffer> filterOffers = utils.filterOffersByProductsHavingSubCategoryLocals(offerResponse.getOffers());
		Assertions.assertNotNull(filterOffers);
	}

    private CTOfferResponse getOfferResponse() {

        CTOfferResponse ctOfferResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/CTOfferResponseLocal.json"), CTOfferResponse.class);
        return ctOfferResponse;
    }
	void filterOfferBasedOnZipOrDMATest(){
		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("/OfferResponseDependent.json", CTOfferResponse.class);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setCustomerEligibility(new CustomerEligibility());
		List<String> zipCode = new ArrayList<>();
		zipCode.add("71329");
		List<String> dma = new ArrayList<>();
		dma.add("501");
		offerRequest.getCustomerEligibility().setDma(dma);
		offerRequest.getCustomerEligibility().setZipCode(zipCode);
		utils.filterOfferBasedOnZipOrDMA(ctOfferResponse.getOffers(),offerRequest);
	}

	@Test
	void filterOfferBasedOnZipOrDMATest2(){
		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("/OfferResponseDependent.json", CTOfferResponse.class);
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setCustomerEligibility(new CustomerEligibility());
		List<String> zipCode = new ArrayList<>();
		zipCode.add("71329");
		List<String> dma = new ArrayList<>();
		dma.add("501");
		offerRequest.getCustomerEligibility().setDma(dma);
		offerRequest.getCustomerEligibility().setZipCode(zipCode);
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		Mockito.when(redisCacheHelper.getValues(Constants.DEFAULT_ZIPCODE, Constants.OTT)).thenReturn(zipCode);
		Mockito.when(redisCacheHelper.getValues(Constants.DEFAULT_DMA, Constants.OTT)).thenReturn(dma);
		utils.filterOfferBasedOnZipOrDMA(ctOfferResponse.getOffers(),offerRequestWrapper);
		offerRequest.getCustomerEligibility().setZipCode(null);
		offerRequestWrapper.setOfferRequest(offerRequest);
		utils.filterOfferBasedOnZipOrDMA(ctOfferResponse.getOffers(),offerRequestWrapper);
		offerRequest.getCustomerEligibility().setZipCode(zipCode);
		offerRequest.getCustomerEligibility().setFipsCode(dma);
		offerRequestWrapper.setOfferRequest(offerRequest);
		when(dmaLookUpService.getDMAValue(any(), any())).thenReturn(dma);
		utils.filterOfferBasedOnZipOrDMA(ctOfferResponse.getOffers(),offerRequestWrapper);
	}

	@Test
	void testFilterOffersWithOfferProductTypeNotVideoPlan() {
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOffer offer1 = new CTOffer();
		OfferAttributes attributes1 = new OfferAttributes();
		attributes1.setOfferProductTypes(Collections.singletonList(Constants.VIDEO_ADDON));
		offer1.setAttributes(attributes1);
		CTOffer offer2 = new CTOffer();
		OfferAttributes attributes2 = new OfferAttributes();
		attributes2.setOfferProductTypes(Collections.singletonList(Constants.VIDEO_PLAN));
		offer2.setAttributes(attributes2);
		ctOfferResponse.setOffers(Arrays.asList(offer1, offer2));
		List<CTOffer> result = utils.filterOffersWithOfferProductTypeNotVideoPlan(ctOfferResponse);
		assertEquals(1, result.size());
		assertEquals(Constants.VIDEO_ADDON, result.get(0).getAttributes().getOfferProductTypes().get(0));
	}

	@Test
	void testFilterOffersWithOfferProductTypeNotVideoPlan_EmptyResponse() {
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		ctOfferResponse.setOffers(Collections.emptyList());
		List<CTOffer> result = utils.filterOffersWithOfferProductTypeNotVideoPlan(ctOfferResponse);
		assertTrue(result.isEmpty());
	}

	@Test
	void testUpdateCTOfferResponseIndexToVideoPlan() {
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOffer offer1 = new CTOffer();
		OfferAttributes attributes1 = new OfferAttributes();
		attributes1.setOfferProductType(Constants.VIDEO_PLAN);
		offer1.setAttributes(attributes1);
		CTOffer offer2 = new CTOffer();
		OfferAttributes attributes2 = new OfferAttributes();
		attributes2.setOfferProductType(Constants.VIDEO_ADDON);
		offer2.setAttributes(attributes2);
		ctOfferResponse.setOffers(Arrays.asList(offer1, offer2));
		CTOfferResponse result = utils.updateCTOfferResponseIndexToVideoPlan(ctOfferResponse);
		assertEquals(2, result.getOffers().size());
		assertEquals(Constants.VIDEO_PLAN, result.getOffers().get(0).getAttributes().getOfferProductType());
		assertEquals(Constants.VIDEO_ADDON, result.getOffers().get(1).getAttributes().getOfferProductType());
	}

	@Test
	void testUpdateCTOfferResponseIndexToVideoPlan_EmptyResponse() {
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		ctOfferResponse.setOffers(Collections.emptyList());
		CTOfferResponse result = utils.updateCTOfferResponseIndexToVideoPlan(ctOfferResponse);
		assertNotNull(result);
	}

	@Test
	void testFilterOffersBasedOnSalesChannel_WithMatchingSalesChannel() {
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOffer offer1 = createOfferWithSalesChannel("directvOnline");
		CTOffer offer2 = createOfferWithSalesChannel("directvStreamOnline");
		ctOfferResponse.setOffers(Arrays.asList(offer1, offer2));
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setSalesChannel(Collections.singletonList("directvOnline"));
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		utils.filterOffersBasedOnSalesChannel(ctOfferResponse, offerRequestWrapper);
		assertEquals(1, ctOfferResponse.getOffers().size());
		assertEquals("directvOnline", ctOfferResponse.getOffers().get(0).getAttributes().getEligibility().getConstraints().get(0).getSalesChannel().get(0));
	}

	@Test
	void testFilterOffersBasedOnSalesChannel_NoMatchingSalesChannel() {
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOffer offer1 = createOfferWithSalesChannel("directvStreamOnline");
		ctOfferResponse.setOffers(Collections.singletonList(offer1));
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setSalesChannel(Collections.singletonList("directvOnline"));
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		utils.filterOffersBasedOnSalesChannel(ctOfferResponse, offerRequestWrapper);
		assertEquals(0, ctOfferResponse.getOffers().size());
	}

	@Test
	void testFilterOffersBasedOnSalesChannel_EmptyOffers() {
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		ctOfferResponse.setOffers(Collections.emptyList());
		OfferRequest offerRequest = new OfferRequest();
		offerRequest.setSalesChannel(Collections.singletonList("directvOnline"));
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		utils.filterOffersBasedOnSalesChannel(ctOfferResponse, offerRequestWrapper);
		assertEquals(0, ctOfferResponse.getOffers().size());
	}

	@Test
	void testFilterOffersBasedOnSalesChannel_NullOfferRequestWrapper() {
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOffer offer1 = createOfferWithSalesChannel("directvOnline");
		ctOfferResponse.setOffers(Collections.singletonList(offer1));
		utils.filterOffersBasedOnSalesChannel(ctOfferResponse, null);
		assertEquals(0, ctOfferResponse.getOffers().size());
	}

	private CTOffer createOfferWithSalesChannel(String salesChannel) {
		CTOffer offer = new CTOffer();
		OfferAttributes attributes = new OfferAttributes();
		Eligibility eligibility = new Eligibility();
		Constraint constraint = new Constraint();
		constraint.setSalesChannel(Collections.singletonList(salesChannel));
		eligibility.setConstraints(Collections.singletonList(constraint));
		attributes.setEligibility(eligibility);
		offer.setAttributes(attributes);
		return offer;
	}

	@Test
	void testFilterMDUIncludedProducts_WithNoMatchingExclusions() {
		CTProductResponse response = new CTProductResponse();
		ProductObj product = new ProductObj();
		GenericTypeIdBase genericTypeIdBase = new GenericTypeIdBase();
		genericTypeIdBase.setKey(Constants.VIDEO_PLAN);
		product.setProductType(genericTypeIdBase);
		Variant variant = new Variant();
		Attributes attributes = new Attributes();
		IncludeProductWrapper includedProductWrapper = new IncludeProductWrapper();
		IncludedProduct includedProduct = new IncludedProduct();
		includedProduct.setKey("BASE-ENTERTAINMENT-201811");
		includedProductWrapper.setProducts(Collections.singletonList(includedProduct));
		attributes.setIncludedProducts(Collections.singletonList(includedProductWrapper));
		attributes.setBusinessSegment(Collections.singletonList(Constants.MDU));
		variant.setAttributes(attributes);
		product.setVariants(Collections.singletonList(variant));
		response.setProducts(Collections.singletonList(product));

		when(redisCacheHelper.getValues(Constants.MDU_INCLUDED_PRODUCT_EXCLUSIONS, Constants.OTT))
				.thenReturn(Collections.singletonList("BOLT-ESPNPLUS-202411"));

		offersUtils.filterMDUIncludedProducts(response);

		assertNotNull(response.getProducts());
		assertNotNull(response.getProducts().get(0).getVariants().get(0).getAttributes().getIncludedProducts());
		assertEquals(1, response.getProducts().get(0).getVariants().get(0).getAttributes().getIncludedProducts().size());
	}
	
	@Test
    public void testGetTwoDigitRoundOffValue() {
        Double result = utils.getTwoDigitRoundOffValue(1.00);
        assertNotNull(result);
    }

	@Test
	void getAllMessagesMap_returnsMapWhenMessageGroupsValid() {
		MessageEntry messageTypeEntry = new MessageEntry();
		messageTypeEntry.setName(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE);
		messageTypeEntry.setValue(Map.of(Constants.KEY, Constants.DELAY_PROVISIONING));
		MessageEntry allMessagesEntry = new MessageEntry();
		allMessagesEntry.setName(Constants.MESSAGES_BY_KEY_ALL_MESSAGES);
		List<Object> msgArr = new ArrayList<>();
		msgArr.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "opus"));
		msgArr.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SALES));
		msgArr.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.SHORT_MESSAGE)));
		msgArr.add(createMessageEntryMap(Constants.VALUE, "Test Delay Provisioning Default Short Message for Sales"));
		allMessagesEntry.setValue(List.of(msgArr));
		List<List<MessageEntry>> messageGroups = List.of(List.of(messageTypeEntry, allMessagesEntry));
		Map<String, String> result = offersUtils.getAllMessagesMap(messageGroups, "opus");
		String expectedKey = Constants.DELAY_PROVISIONING + "opus" + Constants.SALES + Constants.SHORT_MESSAGE;
		assertEquals("Test Delay Provisioning Default Short Message for Sales", result.get(expectedKey));
	}

	@Test
	void getAllMessagesMap_returnsEmptyWhenSalesChannelNull() {
		List<List<MessageEntry>> messageGroups = List.of(List.of(new MessageEntry()));
		Map<String, String> result = offersUtils.getAllMessagesMap(messageGroups, null);
		assertTrue(result.isEmpty());
	}

	@Test
	void updateDelayProvisioningMessages_setsMessagesWhenKeysPresent() {
		Attributes attributes = mock(Attributes.class);
		Map<String, String> globalMessagesMap = new HashMap<>();
		String salesChannel = "opus";
		String flowType = Constants.SALES;
		String shortKey = Constants.DELAY_PROVISIONING + salesChannel + flowType + Constants.SHORT_MESSAGE;
		String longKey = Constants.DELAY_PROVISIONING + salesChannel + flowType + Constants.LONG_MESSAGE;
		globalMessagesMap.put(shortKey, "Delay Provisioning Opus Short Message for Sales");
		globalMessagesMap.put(longKey, "Delay Provisioning Opus Long Message for Sales");
		offersUtils.updateDelayProvisioningMessages(Constants.DELAY_PROVISIONING, attributes, globalMessagesMap, salesChannel, flowType);
		ArgumentCaptor<DelayProvisioningMessagesByKey> captor = ArgumentCaptor.forClass(DelayProvisioningMessagesByKey.class);
		verify(attributes).setDelayProvisioningMessagesByKey(captor.capture());
		assertEquals("Delay Provisioning Opus Short Message for Sales", captor.getValue().getShortMessage());
		assertEquals("Delay Provisioning Opus Long Message for Sales", captor.getValue().getLongMessage());
	}

	@Test
	void updateDelayProvisioningMessages_setsNullWhenKeysMissing() {
		Attributes attributes = mock(Attributes.class);
		Map<String, String> globalMessagesMap = new HashMap<>();
		offersUtils.updateDelayProvisioningMessages(Constants.DELAY_PROVISIONING, attributes, globalMessagesMap, "opus", Constants.SALES);
		verify(attributes).setDelayProvisioningMessagesByKey(isNull());
	}

	@Test
	void processDelayProvisioningProductVariantAttributes_updatesMessagesWhenEligible() {
		OffersUtils spy = spy(utils);
		Attributes attributes = mock(Attributes.class);
		List<List<MessageEntry>> messageGroups = buildMessageGroups("opus", Constants.SALES, Constants.SHORT_MESSAGE, "Delay Provisioning Opus Short Message for Sales");
		when(attributes.getGlobalMessagesByKey()).thenReturn(messageGroups);
		spy.processDelayProvisioningProductVariantAttributes(attributes, "opus", true, Constants.SALES);
		verify(spy).updateDelayProvisioningMessages(eq(Constants.DELAY_PROVISIONING), eq(attributes), anyMap(), eq("opus"), eq(Constants.SALES));
		verify(attributes).setDelayProvisioning(null);
		verify(attributes).setDelayProvisioningReasons(null);
	}

	@Test
	void processDelayProvisioningProductVariantAttributes_suppressesWhenNotEligible() {
		OffersUtils spy = spy(utils);
		Attributes attributes = mock(Attributes.class);
		when(attributes.getGlobalMessagesByKey()).thenReturn(new ArrayList<>());
		spy.processDelayProvisioningProductVariantAttributes(attributes, "opus", false, Constants.SALES);
		verify(spy).suppressDelayProvisioning(attributes);
	}

	@Test
	void suppressDelayProvisioning_clearsAttributes() {
		Attributes attributes = mock(Attributes.class);
		offersUtils.suppressDelayProvisioning(attributes);
		verify(attributes).setDelayProvisioning(null);
		verify(attributes).setDelayProvisioningReasons(null);
		verify(attributes).setDelayProvisioningMessagesByKey(null);
	}

	@Test
	void getProductsFlowType_returnsSalesWhenCustomerContextNull() {
		ProductRequest request = mock(ProductRequest.class);
		when(request.getCustomerContext()).thenReturn(null);
		String result = offersUtils.getProductsFlowType(request);
		assertEquals(Constants.SALES, result);
	}

	@Test
	void getProductsFlowType_returnsServicesWhenCustomerContextPresent() {
		ProductRequest request = mock(ProductRequest.class);
		CustomerContext customerContext = mock(CustomerContext.class);
		when(request.getCustomerContext()).thenReturn(customerContext);
		String result = offersUtils.getProductsFlowType(request);
		assertEquals(Constants.SERVICES, result);
	}

	@Test
	void getDelayProvisioningProductsFromRequest_returnsEmptyWhenRequestInvalid() {
		Map<String, Boolean> result = offersUtils.getDelayProvisioningProductsFromRequest(null);
		assertTrue(result.isEmpty());
	}

	@Test
	void getDelayProvisioningProductsFromRequest_returnsMapWhenProductsPresent() {
		ProductRequest request = mock(ProductRequest.class);
		CustomerContext customerContext = mock(CustomerContext.class);
		CartProduct ott = mock(CartProduct.class);
		ProductInfo productInfo = mock(ProductInfo.class);
		when(request.getCustomerContext()).thenReturn(customerContext);
		when(customerContext.getOtt()).thenReturn(ott);
		when(ott.getProducts()).thenReturn(List.of(productInfo));
		when(productInfo.getProductCode()).thenReturn("BOLT-HBO-201610");
		when(productInfo.getDelayProvisioningTillTime()).thenReturn("01/01/2026");
		Map<String, Boolean> result = offersUtils.getDelayProvisioningProductsFromRequest(request);
		assertEquals(Boolean.TRUE, result.get("BOLT-HBO-201610"));
	}

	@Test
	void processDelayProvisioningProducts_callsVariantProcessing() {
		OffersUtils spy = spy(utils);
		ProductObj productObj = mock(ProductObj.class);
		Variant variant1 = mock(Variant.class);
		Variant variant2 = mock(Variant.class);
		ProductRequest request = mock(ProductRequest.class);
		when(productObj.getCode()).thenReturn("BOLT-HBO-201610");
		when(productObj.getVariants()).thenReturn(List.of(variant1, variant2));
		Map<String, Boolean> map = new HashMap<>();
		map.put("BOLT-HBO-201610", true);
		spy.processDelayProvisioningProducts(productObj, map, "opus", request, spy);
		verify(spy, times(2)).processDelayProvisioningProductVariant(any(Variant.class), eq("opus"), eq(true), eq(request), eq(spy));
	}

	@Test
	void processDelayProvisioningProductVariant_updatesAttributesWhenSalesChannelPresent() {
		OffersUtils spy = spy(utils);
		Variant variant = mock(Variant.class);
		Attributes attributes = mock(Attributes.class);
		ProductRequest request = mock(ProductRequest.class);
		when(variant.getAttributes()).thenReturn(attributes);
		when(request.getCustomerContext()).thenReturn(null);
		spy.processDelayProvisioningProductVariant(variant, "opus", true, request, spy);
		verify(spy).processDelayProvisioningProductVariantAttributes(attributes, "opus", true, Constants.SALES);
	}

	@Test
	void processDelayProvisioningProductVariant_suppressesWhenSalesChannelMissing() {
		OffersUtils spy = spy(utils);
		Variant variant = mock(Variant.class);
		Attributes attributes = mock(Attributes.class);
		ProductRequest request = mock(ProductRequest.class);
		when(variant.getAttributes()).thenReturn(attributes);
		spy.processDelayProvisioningProductVariant(variant, null, false, request, spy);
		verify(spy).suppressDelayProvisioning(attributes);
	}

	private List<List<MessageEntry>> buildMessageGroups(String salesChannel, String flowType, String key, String value) {
		MessageEntry messageTypeEntry = new MessageEntry();
		messageTypeEntry.setName(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE);
		messageTypeEntry.setValue(Map.of(Constants.KEY, Constants.DELAY_PROVISIONING));
		MessageEntry allMessagesEntry = new MessageEntry();
		allMessagesEntry.setName(Constants.MESSAGES_BY_KEY_ALL_MESSAGES);
		List<Object> msgArr = new ArrayList<>();
		msgArr.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, salesChannel));
		msgArr.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, flowType));
		msgArr.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, key)));
		msgArr.add(createMessageEntryMap(Constants.VALUE, value));
		allMessagesEntry.setValue(List.of(msgArr));
		return List.of(List.of(messageTypeEntry, allMessagesEntry));
	}

	private Map<String, Object> createMessageEntryMap(String name, Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("value", value);
		return map;
	}

    @Test
    public void testFilterConflictingOfferWhenAllowConflictingOffersPresent(){
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/CTOfferResponseAllowConflictingOffers.json"), CTOfferResponse.class);
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION_ACTION_TYPE));
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BOLT-MYENTERTAINMENT-202410_GENRE");
        offerRequest.setCartOffers(Collections.singletonList(cartOffer));

        OfferRequestWrapper wrapper = new OfferRequestWrapper();
        wrapper.setOfferRequest(offerRequest);
        List<CTOffer> filteredOffers = utils.filterConflictingOfferWhenAllowConflictingOffersPresent(ctOfferResponse.getOffers(), wrapper);
        assertNotNull(filteredOffers);
    }

	private CTOffer createOffer(String code) {
        CTOffer offer = new CTOffer();
        offer.setCode(code);
        offer.setAttributes(new OfferAttributes());
        return offer;
    }

    private CTOffer createOfferWithNonStackableEligibility(String code, List<String> offerIds) {
        return createOfferWithEligibilityType(code, Constants.NON_STACKABLE, offerIds);
    }

    private CTOffer createOfferWithEligibilityType(String code, String eligibilityType, List<String> offerIds) {
        CTOffer offer = createOffer(code);
        AdditionalEligibility additionalEligibility = new AdditionalEligibility();
        additionalEligibility.setEligibilityType(eligibilityType);
        additionalEligibility.setOfferIds(offerIds);
        offer.getAttributes().setAdditionalEligibility(Collections.singletonList(additionalEligibility));
        return offer;
    }

	@Test
    void testFilterOffersBasedOnNonStackableCartOffers_RemovesOnlyNonCartInRegularFlow() {
        OfferRequest offerRequest = new OfferRequest();
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BOLT-MYENTERTAINMENT-202410_GENRE");
        offerRequest.setCartOffers(Collections.singletonList(cartOffer));

        OfferRequestWrapper wrapper = new OfferRequestWrapper();
        wrapper.setOfferRequest(offerRequest);
        wrapper.setFlow("offers");

        CTOffer cartMatchedOffer = createOfferWithNonStackableEligibility("OF_BOLT-MYENTERTAINMENT-202410_GENRE", Arrays.asList("OF_BOLT-HBO-201610_GENRE", "OF_BOLT-AMC-202106_GENRE"));
        CTOffer offerB = createOffer("OF_BOLT-HBO-201610_GENRE");
        CTOffer offerC = createOffer("OF_BOLT-AMC-202106_GENRE");
        CTOffer offerD = createOffer("OF_UNLTDCDVR2021_GENRE");

        List<CTOffer> finalOfferList = new ArrayList<>(Arrays.asList(cartMatchedOffer, offerB, offerC, offerD));

        utils.filterOffersBasedOnNonStackableCartOffers(finalOfferList, wrapper);

        List<String> remainingCodes = finalOfferList.stream().map(CTOffer::getCode).collect(Collectors.toList());
        assertEquals(2, finalOfferList.size());
        assertTrue(remainingCodes.contains("OF_BOLT-MYENTERTAINMENT-202410_GENRE"));
        assertTrue(remainingCodes.contains("OF_UNLTDCDVR2021_GENRE"));
        assertFalse(remainingCodes.contains("OF_BOLT-HBO-201610_GENRE"));
        assertFalse(remainingCodes.contains("OF_BOLT-AMC-202106_GENRE"));
    }

    @Test
    void testFilterOffersBasedOnNonStackableCartOffers_RemovesCartOffersInValidateCartFlow() {
        OfferRequest offerRequest = new OfferRequest();
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BOLT-MYENTERTAINMENT-202410_GENRE");
        offerRequest.setCartOffers(Collections.singletonList(cartOffer));

        OfferRequestWrapper wrapper = new OfferRequestWrapper();
        wrapper.setOfferRequest(offerRequest);
        wrapper.setFlow("validateCart");

        CTOffer cartMatchedOffer = createOfferWithNonStackableEligibility("OF_BOLT-MYENTERTAINMENT-202410_GENRE", Arrays.asList("OF_BOLT-MYENTERTAINMENT-202410_GENRE", "OF_BOLT-HBO-201610_GENRE"));
        CTOffer offerB = createOffer("OF_BOLT-HBO-201610_GENRE");
        CTOffer offerC = createOffer("OF_BOLT-AMC-202106_GENRE");

        List<CTOffer> finalOfferList = new ArrayList<>(Arrays.asList(cartMatchedOffer, offerB, offerC));

        utils.filterOffersBasedOnNonStackableCartOffers(finalOfferList, wrapper);

        List<String> remainingCodes = finalOfferList.stream().map(CTOffer::getCode).collect(Collectors.toList());
        assertEquals(1, finalOfferList.size());
        assertTrue(remainingCodes.contains("OF_BOLT-AMC-202106_GENRE"));
        assertFalse(remainingCodes.contains("OF_BOLT-MYENTERTAINMENT-202410_GENRE"));
        assertFalse(remainingCodes.contains("OF_BOLT-HBO-201610_GENRE"));
    }

    @Test
    void testFilterOffersBasedOnNonStackableCartOffers_IgnoresNonNonStackableEligibilityType() {
        OfferRequest offerRequest = new OfferRequest();
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BOLT-MYENTERTAINMENT-202410_GENRE");
        offerRequest.setCartOffers(Collections.singletonList(cartOffer));

        OfferRequestWrapper wrapper = new OfferRequestWrapper();
        wrapper.setOfferRequest(offerRequest);

        CTOffer cartMatchedOffer = createOfferWithEligibilityType("OF_BOLT-MYENTERTAINMENT-202410_GENRE", "dependentOffer", Arrays.asList("OF_BOLT-HBO-201610_GENRE"));
        CTOffer offerB = createOffer("OF_BOLT-HBO-201610_GENRE");

        List<CTOffer> finalOfferList = new ArrayList<>(Arrays.asList(cartMatchedOffer, offerB));

        utils.filterOffersBasedOnNonStackableCartOffers(finalOfferList, wrapper);

        assertEquals(2, finalOfferList.size());
    }
}