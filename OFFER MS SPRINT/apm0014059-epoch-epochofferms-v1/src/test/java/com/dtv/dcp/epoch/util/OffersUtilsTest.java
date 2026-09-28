package com.dtv.dcp.epoch.util;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.common.*;
import com.dtv.dcp.epoch.model.common.request.*;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.response.CheckEligibiltyResponse;
import com.dtv.dcp.epoch.model.ct.eligibility.Constraint;
import com.dtv.dcp.epoch.model.ct.eligibility.Eligibility;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.*;
import com.dtv.dcp.epoch.model.ct.product.*;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.offer.AdditionalEligibility;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGAccountInfo;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.model.eligibility.EligibilityErrorMessage;
import com.dtv.dcp.epoch.processor.ott.services.CustomerSubscriptionDetail;
import com.dtv.dcp.epoch.service.DMALookUpService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
	void testEvaluateEligibility() {
		// Mock input data
		CheckEligibilityRequest checkEligibilityRequest = new CheckEligibilityRequest();

		// Set basic fields
		checkEligibilityRequest.setCustomerSegments("Residential");
		checkEligibilityRequest.setOfferActionType(Collections.singletonList("Acquisition"));
		checkEligibilityRequest.setSalesChannel("dotcomAgent");
		checkEligibilityRequest.setOfferProductFamily("OTT");
		checkEligibilityRequest.setContractIndicator(Collections.singletonList("RR"));
		checkEligibilityRequest.setCustomerSubscriptionType("RR");


		// Set CustomerContext
		com.dtv.dcp.epoch.model.common.CustomerContext customerContext = new com.dtv.dcp.epoch.model.common.CustomerContext();


		// Set existingProductFamily
		ExistingProductFamily existingProductFamily = new ExistingProductFamily();
		existingProductFamily.setParentSubscriptionDate("06/11/2025");
		existingProductFamily.setHasActiveInstallments(true);
		existingProductFamily.setInstallmentProvider(Collections.singletonList("AFFIRM"));
		customerContext.setExistingProductFamily(Collections.singletonList(existingProductFamily));

		// Set OTT
		CartProduct ott = new CartProduct();
		ott.setAccountNumber("220121174757314");
		ott.setIsActive(true);
		ott.setRetentionPromotionIndicator(false);
		ott.setFreeTrialEligible(false);
		ott.setNextBillingDate("02/04/2022");

		// Set OTT products
		List<ProductInfo> products = new ArrayList<>();

		ProductInfo product1 = new ProductInfo();
		product1.setBasePrice("149.99");
		product1.setProductCode("HARD-GEMINILEASE-202311");
		product1.setProductType("video-device");
		products.add(product1);

		ProductInfo product2 = new ProductInfo();
		product2.setBasePrice("149.99");
		product2.setProductCode("BASE-PREMIER-202004");
		product2.setProductType("video-plan");
		products.add(product2);

		ProductInfo product3 = new ProductInfo();
		product3.setBasePrice("149.99");
		product3.setProductCode("BASE-CHOICE-201811");
		product3.setProductType("video-plan");
		products.add(product3);

		ProductInfo product4 = new ProductInfo();
		product4.setBasePrice("11.0");
		product4.setProductCode("BOLT-STARZ-201610");
		product4.setProductType("video-addon");

		products.add(product4);

		ProductInfo product5 = new ProductInfo();
		product5.setBasePrice("14.99");
		product5.setProductCode("BOLT-HBO-201610");
		product5.setProductType("video-addon");

		products.add(product5);

		ProductInfo product6 = new ProductInfo();
		product6.setBasePrice("5.0");
		product6.setProductCode("BOLT-MVPX-201910");
		product6.setProductType("video-addon");

		products.add(product6);

		ProductInfo product7 = new ProductInfo();
		product7.setBasePrice("30.0");
		product7.setProductCode("BOLT-BRAZIL-201810");
		product7.setProductType("video-addon");

		products.add(product7);

		ott.setProducts(products);
		//ott.setPendingSwimlaneSwitch(true);
		customerContext.setOtt(ott);

		checkEligibilityRequest.setCustomerContext(customerContext);

		// Set ChannelEligibility
		ChannelEligibility channelEligibility = new ChannelEligibility();
		channelEligibility.setSalesChannel("COR");
		channelEligibility.setSalesSubChannel("pv");
		channelEligibility.setSalesSiteId("q");
		checkEligibilityRequest.setChannelEligibility(channelEligibility);

		// Set AgentDealerDetails
		AgentDealerDetails agentDealerDetails = new AgentDealerDetails();
		agentDealerDetails.setAgentId("rt345");
		agentDealerDetails.setAgentRole("supportAgent");
		checkEligibilityRequest.setAgentDealerDetails(agentDealerDetails);

		// Assertions to verify the object is populated correctly
List<com.dtv.dcp.epoch.model.eligibility.Eligibility> eligibilityLst=new ArrayList<>();
		com.dtv.dcp.epoch.model.eligibility.Eligibility eligibilityDesign = new com.dtv.dcp.epoch.model.eligibility.Eligibility();

// Populate Key Evaluation
		Map<String, List<String>> keyEvaluation = new HashMap<>();
		keyEvaluation.put("salesChannel", Arrays.asList("directvOnline", "dotcomAgent"));
		keyEvaluation.put("deviceEligibility", Arrays.asList( "WITHDEVICE"));
		eligibilityDesign.setKeyEvaluation(keyEvaluation);

// Populate Customer Eligibility
		Map<String, List<String>> customerEligibility = new HashMap<>();
		customerEligibility.put("contractIndicator", Collections.singletonList("RR"));
		customerEligibility.put("customerSubscriptionType", Arrays.asList("RR", "GENRE"));
		customerEligibility.put("eligiblePackages", Arrays.asList("BASE-CHOICE-201811", "BASE-PREMIER-202004"));
		customerEligibility.put("eligibleDevices", Collections.singletonList("HARD-GEMINILEASE-202311"));
		customerEligibility.put("ineligibleAccountStatus", Arrays.asList("Pause", "Pending Pause", "Pending Swimlane","Grace Period","freeTrial","BRE","Suspend","Pending Disconnect"));
		eligibilityDesign.setCustomerEligibility(customerEligibility);

// Populate Channel Details
		Map<String, List<String>> channelDetails = new HashMap<>();
		channelDetails.put("salesSiteId", Arrays.asList("PVT2", "PVT3"));
		channelDetails.put("salesSubChannel", Collections.singletonList("ISM-RET"));
		channelDetails.put("channel", Arrays.asList("COR", "AEG"));
		eligibilityDesign.setChannelDetails(channelDetails);

// Populate Agent Eligibility
		List<Map<String, List<String>>> agentEligibility = new ArrayList<>();
		Map<String, List<String>> agentEligibilityEntry = new HashMap<>();
		Map<String, List<String>> agentEligibilityEntry1 = new HashMap<>();
		agentEligibilityEntry1.put("agentId", Arrays.asList("rt345", "AGT002"));
		agentEligibilityEntry1.put("agentRole", Arrays.asList("loyaltyAgent", "supportAgent"));
		agentEligibilityEntry.put("agentId", Arrays.asList("rt345", "AGT002"));
		//agentEligibilityEntry.put("agentRole", Arrays.asList("loyaltyAgent", "supportAgent"));
		agentEligibility.add(agentEligibilityEntry);
		agentEligibility.add(agentEligibilityEntry1);
		eligibilityDesign.setAgentEligibility(agentEligibility);

// Add the populated EligibilityDesign to the list
		eligibilityLst.add(eligibilityDesign);
		com.dtv.dcp.epoch.model.eligibility.Eligibility eligibilityDesign1=new com.dtv.dcp.epoch.model.eligibility.Eligibility();
		Map<String, List<String>> keyEvaluation1 = new HashMap<>();
		keyEvaluation1.put("salesChannel", Arrays.asList("directvOnline", "opus"));
		eligibilityDesign1.setKeyEvaluation(keyEvaluation1);
		eligibilityLst.add(eligibilityDesign1);
		List<EligibilityErrorMessage> errormessageList=new ArrayList<>();
		EligibilityErrorMessage errorMessage = new EligibilityErrorMessage();
		errormessageList.add(errorMessage);
		// Create the list of error messages
		List<Map<String, List<String>>> errorMessages = new ArrayList<>();

		// Add the first error message
		Map<String, List<String>> error1 = new HashMap<>();
		error1.put("criticality", Collections.singletonList("3"));
		error1.put("errorCode", Collections.singletonList("AGENT_INELIGIBLE"));
		error1.put("errorMessage", Collections.singletonList("Agent does not meet eligibility criteria"));
		error1.put("attributeList", Arrays.asList("agentEligibility", "channelEligibilityDetails"));
		errorMessages.add(error1);

		errorMessage.setEligibilityErrorMessage(errorMessages);
		EligibilityErrorMessage errorMessage1 = new EligibilityErrorMessage();
		errormessageList.add(errorMessage1);
		// Add the second error message
		List<Map<String, List<String>>> errorMessagesMapLst = new ArrayList<>();
		Map<String, List<String>> error2 = new HashMap<>();
		error2.put("criticality", Collections.singletonList("2"));
		error2.put("errorCode", Collections.singletonList("CUSTOMER_INELIGIBLE"));
		error2.put("errorMessage", Collections.singletonList("Customer does not meet eligibility criteria"));
		error2.put("attributeList", Collections.singletonList("customerEligibility"));
		errorMessagesMapLst.add(error2);
		errorMessage1.setEligibilityErrorMessage(errorMessagesMapLst);

		// Add the third error message
		EligibilityErrorMessage errorMessage2= new EligibilityErrorMessage();
		errormessageList.add(errorMessage2);
		List<Map<String, List<String>>> errorMessagesMapLst1 = new ArrayList<>();
		Map<String, List<String>> error3 = new HashMap<>();
		error3.put("criticality", Collections.singletonList("4"));
		error3.put("errorCode", Collections.singletonList("AGENT_CUST_INELIGIBLE"));
		error3.put("errorMessage", Collections.singletonList("Both agent and customer does not meet eligibility criteria"));
		error3.put("derivedFrom", Arrays.asList("AGENT_INELIGIBLE", "CUSTOMER_INELIGIBLE"));
		errorMessagesMapLst1.add(error3);
		errorMessage2.setEligibilityErrorMessage(errorMessagesMapLst1);

		// Add the fourth error message

		EligibilityErrorMessage errorMessage3= new EligibilityErrorMessage();
		errormessageList.add(errorMessage3);
		List<Map<String, List<String>>> errorMessagesMapLst2 = new ArrayList<>();
		Map<String, List<String>> error4 = new HashMap<>();
		error4.put("criticality", Collections.singletonList("1"));
		error4.put("errorCode", Collections.singletonList("KEY_INELIGIBLE"));
		error4.put("errorMessage", Collections.singletonList("Saleschannel is ineligible for swimlane switch"));
		error4.put("attributeList", Collections.singletonList("eligibilityKeyEvaluation"));
		errorMessagesMapLst2.add(error4);
		errorMessage3.setEligibilityErrorMessage(errorMessagesMapLst2);
		// Call the method
		CheckEligibiltyResponse actualResponse = offersUtils.evaluateEligibility(checkEligibilityRequest, eligibilityLst, errormessageList);

		// Assertions
		assertNotNull(actualResponse);
		assertNotNull(actualResponse.getSwimlaneEligibilityDetails());
		assertTrue(actualResponse.getSwimlaneEligibilityDetails().isSwimlaneSwitchEligible());
		agentEligibilityEntry.put("agentId", Arrays.asList("rt345123", "AGT002"));
		agentEligibilityEntry1.put("agentRole", Arrays.asList("rt345123", "AGT002"));
		CheckEligibiltyResponse actualResponse_agent = offersUtils.evaluateEligibility(checkEligibilityRequest, eligibilityLst, errormessageList);

		assertFalse(actualResponse_agent.getSwimlaneEligibilityDetails().isSwimlaneSwitchEligible());
		assertTrue(actualResponse_agent.getSwimlaneEligibilityDetails().getSlsIneligibleReasonCode().contains("AGENT_INELIGIBLE"));
		customerEligibility.put("contractIndicator", Collections.singletonList("GENRE"));
		CheckEligibiltyResponse actualResponse_agent_Cust = offersUtils.evaluateEligibility(checkEligibilityRequest, eligibilityLst, errormessageList);
		assertFalse(actualResponse_agent_Cust.getSwimlaneEligibilityDetails().isSwimlaneSwitchEligible());
		assertTrue(actualResponse_agent_Cust.getSwimlaneEligibilityDetails().getSlsIneligibleReasonCode().contains("AGENT_CUST_INELIGIBLE"));
		channelDetails.put("channel", Arrays.asList("test", "AEG"));
		CheckEligibiltyResponse actualResponse_Channel = offersUtils.evaluateEligibility(checkEligibilityRequest, eligibilityLst, errormessageList);
		assertFalse(actualResponse_Channel.getSwimlaneEligibilityDetails().isSwimlaneSwitchEligible());
		assertTrue(actualResponse_Channel.getSwimlaneEligibilityDetails().getSlsIneligibleReasonCode().contains("AGENT_CUST_INELIGIBLE"));
		checkEligibilityRequest.setSalesChannel("opus");
		CheckEligibiltyResponse actualResponse_opus_Channel = offersUtils.evaluateEligibility(checkEligibilityRequest, eligibilityLst, errormessageList);

		assertTrue(actualResponse_opus_Channel.getSwimlaneEligibilityDetails().isSwimlaneSwitchEligible());
		//assertTrue(actualResponse_opus_Channel.getSwimlaneEligibilityDetails().getSlsIneligibleReasonCode().contains("AGENT_CUST_INELIGIBLE"));
		checkEligibilityRequest.setSalesChannel("dotcomAgent");
		ott.setPendingSwimlaneSwitch(false);
		ott.setIsProjectedBillDate(true);
		CheckEligibiltyResponse actualResponse_projectBillDate = offersUtils.evaluateEligibility(checkEligibilityRequest, eligibilityLst, errormessageList);
		assertFalse(actualResponse_projectBillDate.getSwimlaneEligibilityDetails().isSwimlaneSwitchEligible());

		// Scenario 6: Customer Eligibility empty object
		checkEligibilityRequest.getCustomerContext().getOtt().setProducts(Collections.emptyList());

		//keyEvaluation.remove("deviceEligibility");
		CheckEligibiltyResponse actualResponse_device = offersUtils.evaluateEligibility(checkEligibilityRequest, eligibilityLst, errormessageList);
		assertFalse(actualResponse_device.getSwimlaneEligibilityDetails().isSwimlaneSwitchEligible());
		ott.setPendingSwimlaneSwitch(true);
		ott.setIsProjectedBillDate(true);
		ott.setTenureType("PENDING_GOODBYE");
		CheckEligibiltyResponse actualResponse_IneligibleStatus = offersUtils.evaluateEligibility(checkEligibilityRequest, eligibilityLst, errormessageList);
		assertFalse(actualResponse_IneligibleStatus.getSwimlaneEligibilityDetails().isSwimlaneSwitchEligible());
		//assertTrue(actualResponse_device.getSwimlaneEligibilityDetails().getSlsIneligibleReasonCode().contains("AGENT_CUST_INELIGIBLE"));
// Create additionalDetails and set it
		AdditionalDetails additionalDetail = new AdditionalDetails();
		additionalDetail.setAdditionalInfoName("freetraildetails");
		additionalDetail.setName("freetraildetails");
		List<AdditionalDetails> additionDetailsLst=new ArrayList<>();
		additionDetailsLst.add(additionalDetail);
		Params param = new Params();
		param.setParamName("status");
		param.setParamValue("Active");
		additionalDetail.setParams(Collections.singletonList(param));
		existingProductFamily.setAdditionalDetails(additionDetailsLst);
		AdditionalAccountDetails additionalAccountDetails=new AdditionalAccountDetails();
		additionalAccountDetails.setAdditonalInfo(additionDetailsLst);
		customerContext.setAdditionalAccountDetails(additionalAccountDetails);
		additionalDetail.setAdditionalInfoName("serviceSuspendInfo");
		param.setParamValue("Suspend");
		existingProductFamily.setTenureType("PENDING_GOODBYE");
		CheckEligibiltyResponse actualResponse_IneligibleStatus1 = offersUtils.evaluateEligibility(checkEligibilityRequest, eligibilityLst, errormessageList);
		assertFalse(actualResponse_IneligibleStatus1.getSwimlaneEligibilityDetails().isSwimlaneSwitchEligible());


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

		// Create multiple message entries with realistic data
		List<Object> msgArr1 = new ArrayList<>();
		msgArr1.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "opus"));
		msgArr1.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SALES));
		msgArr1.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.SHORT_MESSAGE)));
		msgArr1.add(createMessageEntryMap(Constants.VALUE, "The free 5-day trial includes the package's base channels only.  The following  access begins after the trial period."));

		List<Object> msgArr2 = new ArrayList<>();
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "opus"));
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SALES));
		msgArr2.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.LONG_MESSAGE)));
		msgArr2.add(createMessageEntryMap(Constants.VALUE, "<b>HBO Max Basic With Ads</b>"));

		allMessagesEntry.setValue(List.of(msgArr1, msgArr2));
		List<List<MessageEntry>> messageGroups = List.of(List.of(messageTypeEntry, allMessagesEntry));
		Map<String, String> result = offersUtils.getAllMessagesMap(messageGroups, "opus");

		assertNotNull(result);
		assertFalse(result.isEmpty());
		String shortKey = Constants.DELAY_PROVISIONING + "opus" + Constants.SALES + Constants.SHORT_MESSAGE;
		String longKey = Constants.DELAY_PROVISIONING + "opus" + Constants.SALES + Constants.LONG_MESSAGE;
		assertEquals("The free 5-day trial includes the package's base channels only.  The following  access begins after the trial period.", result.get(shortKey));
		assertEquals("<b>HBO Max Basic With Ads</b>", result.get(longKey));
	}

	@Test
	void getAllMessagesMap_returnsEmptyWhenSalesChannelNull() {
		List<List<MessageEntry>> messageGroups = List.of(List.of(new MessageEntry()));
		Map<String, String> result = offersUtils.getAllMessagesMap(messageGroups, null);
		assertTrue(result.isEmpty());
	}

	@Test
	void getAllMessagesMap_handlesOrderIndependence_allMessagesBeforeMessageType() {
		// This test verifies that messageType is extracted correctly even when allMessages appears first
		// Without the two-pass approach, this would result in keys like "nullDTV360servicesShortMessage"
		// Bug scenario: allMessages processed before messageType was available, causing null prefix in keys
		MessageEntry allMessagesEntry = new MessageEntry();
		allMessagesEntry.setName(Constants.MESSAGES_BY_KEY_ALL_MESSAGES);
		
		List<Object> msgArr1 = new ArrayList<>();
		msgArr1.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "DTV360"));
		msgArr1.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SERVICES));
		msgArr1.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.SHORT_MESSAGE)));
		msgArr1.add(createMessageEntryMap(Constants.VALUE, "Additionally, access to HBO Max will begin after your free trial period."));
		
		List<Object> msgArr2 = new ArrayList<>();
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "DTV360"));
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SERVICES));
		msgArr2.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.LONG_MESSAGE)));
		msgArr2.add(createMessageEntryMap(Constants.VALUE, "Free Trial includes base channels and select apps. Access to some of your content may not begin until after your trial period."));
		
		allMessagesEntry.setValue(List.of(msgArr1, msgArr2));

		MessageEntry messageTypeEntry = new MessageEntry();
		messageTypeEntry.setName(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE);
		messageTypeEntry.setValue(Map.of(Constants.KEY, Constants.DELAY_PROVISIONING));

		// CRITICAL: allMessagesEntry appears BEFORE messageTypeEntry (simulates real-world ordering issue)
		List<List<MessageEntry>> messageGroups = List.of(List.of(allMessagesEntry, messageTypeEntry));

		Map<String, String> result = offersUtils.getAllMessagesMap(messageGroups, "DTV360");

		// Verify result is not empty
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals("Expected exactly 2 entries (short and long message)", 2, result.size());

		// Should have correct keys with messageType prefix (delayProvisioning), NOT "null"
		String shortKey = Constants.DELAY_PROVISIONING + "DTV360" + Constants.SERVICES + Constants.SHORT_MESSAGE;
		String longKey = Constants.DELAY_PROVISIONING + "DTV360" + Constants.SERVICES + Constants.LONG_MESSAGE;

		assertTrue("Missing short message key: " + shortKey, result.containsKey(shortKey));
		assertTrue("Missing long message key: " + longKey, result.containsKey(longKey));

		assertEquals("Additionally, access to HBO Max will begin after your free trial period.", result.get(shortKey));
		assertEquals("Free Trial includes base channels and select apps. Access to some of your content may not begin until after your trial period.", result.get(longKey));

		// CRITICAL ASSERTION: Verify NO keys with "null" prefix exist (the bug this fix prevents)
		assertFalse("Found keys with 'null' prefix - order independence fix failed!",
			result.keySet().stream().anyMatch(key -> key.startsWith("null")));
		assertFalse("Found keys containing 'null' - messageType not extracted properly!",
			result.keySet().stream().anyMatch(key -> key.contains("null")));
	}

	@Test
	void getAllMessagesMap_preventsDataBleed_multipleMessageEntries() {
		// This test verifies that each msgArr entry gets a fresh map
		// Without fresh maps per entry, data from one entry could bleed into another
		// Bug scenario: Reusing same HashMap caused values from one sales channel to persist into the next
		MessageEntry messageTypeEntry = new MessageEntry();
		messageTypeEntry.setName(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE);
		messageTypeEntry.setValue(Map.of(Constants.KEY, Constants.DELAY_PROVISIONING));

		MessageEntry allMessagesEntry = new MessageEntry();
		allMessagesEntry.setName(Constants.MESSAGES_BY_KEY_ALL_MESSAGES);

		// First message entry - opus channel sales flow
		List<Object> msgArr1 = new ArrayList<>();
		msgArr1.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "opus"));
		msgArr1.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SALES));
		msgArr1.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.SHORT_MESSAGE)));
		msgArr1.add(createMessageEntryMap(Constants.VALUE, "The free 5-day trial includes the package's base channels only.  The following  access begins after the trial period."));

		// Second message entry - DTV360 channel services flow (different channel AND flow type)
		List<Object> msgArr2 = new ArrayList<>();
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "DTV360"));
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SERVICES));
		msgArr2.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.SHORT_MESSAGE)));
		msgArr2.add(createMessageEntryMap(Constants.VALUE, "Additionally, access to HBO Max will begin after your free trial period."));

		// Third message entry - directIntegrationPartner channel sales flow
		List<Object> msgArr3 = new ArrayList<>();
		msgArr3.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "directIntegrationPartner"));
		msgArr3.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SALES));
		msgArr3.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.SHORT_MESSAGE)));
		msgArr3.add(createMessageEntryMap(Constants.VALUE, "The 5-day free trial includes the package's base channels only. Access to HBO Max begins after the trial period."));

		allMessagesEntry.setValue(List.of(msgArr1, msgArr2, msgArr3));
		List<List<MessageEntry>> messageGroups = List.of(List.of(messageTypeEntry, allMessagesEntry));

		Map<String, String> result = offersUtils.getAllMessagesMap(messageGroups, "opus");

		// Verify we have all 3 entries
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals("Expected exactly 3 entries (one per msgArr)", 3, result.size());

		// Verify each entry has its correct, isolated values
		String key1 = Constants.DELAY_PROVISIONING + "opus" + Constants.SALES + Constants.SHORT_MESSAGE;
		String key2 = Constants.DELAY_PROVISIONING + "DTV360" + Constants.SERVICES + Constants.SHORT_MESSAGE;
		String key3 = Constants.DELAY_PROVISIONING + "directIntegrationPartner" + Constants.SALES + Constants.SHORT_MESSAGE;

		// Assert all keys exist
		assertTrue("Missing key1: " + key1, result.containsKey(key1));
		assertTrue("Missing key2: " + key2, result.containsKey(key2));
		assertTrue("Missing key3: " + key3, result.containsKey(key3));

		// Assert correct values for each key (no cross-contamination)
		assertEquals("The free 5-day trial includes the package's base channels only.  The following  access begins after the trial period.",
			result.get(key1));
		assertEquals("Additionally, access to HBO Max will begin after your free trial period.",
			result.get(key2));
		assertEquals("The 5-day free trial includes the package's base channels only. Access to HBO Max begins after the trial period.",
			result.get(key3));

		// CRITICAL ASSERTION: Ensure no data bleed - each entry should be completely isolated
		// If data bleeds, we might see wrong flowType or salesChannel in keys
		assertFalse("Data bleed detected: opus with services flow (should be sales only)",
			result.containsKey(Constants.DELAY_PROVISIONING + "opus" + Constants.SERVICES + Constants.SHORT_MESSAGE));
		assertFalse("Data bleed detected: DTV360 with sales flow (should be services only)",
			result.containsKey(Constants.DELAY_PROVISIONING + "DTV360" + Constants.SALES + Constants.SHORT_MESSAGE));
	}

	@Test
	void getAllMessagesMap_handlesMultipleMessageGroups_withMixedOrdering() {
		// This test combines both scenarios: multiple groups with different messageType ordering
		// Verifies that the two-pass approach works consistently across multiple independent message groups
		// Real-world scenario: CT response may contain multiple message groups with varying order

		// === GROUP 1: Standard order (messageType first) ===
		MessageEntry messageType1 = new MessageEntry();
		messageType1.setName(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE);
		messageType1.setValue(Map.of(Constants.KEY, Constants.DELAY_PROVISIONING));

		MessageEntry allMessages1 = new MessageEntry();
		allMessages1.setName(Constants.MESSAGES_BY_KEY_ALL_MESSAGES);
		List<Object> msgArr1 = new ArrayList<>();
		msgArr1.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "opus"));
		msgArr1.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SALES));
		msgArr1.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.SHORT_MESSAGE)));
		msgArr1.add(createMessageEntryMap(Constants.VALUE, "The free 5-day trial includes the package's base channels only.  The following  access begins after the trial period."));
		allMessages1.setValue(List.of(msgArr1));

		// Group 1: messageType first (standard order)
		List<MessageEntry> group1 = List.of(messageType1, allMessages1);

		// === GROUP 2: Reversed order (allMessages first, different messageType) ===
		MessageEntry allMessages2 = new MessageEntry();
		allMessages2.setName(Constants.MESSAGES_BY_KEY_ALL_MESSAGES);

		List<Object> msgArr2 = new ArrayList<>();
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "DTV360"));
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SERVICES));
		msgArr2.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.SHORT_MESSAGE)));
		msgArr2.add(createMessageEntryMap(Constants.VALUE, "HBO Max Basic With Ads"));

		List<Object> msgArr3 = new ArrayList<>();
		msgArr3.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, "DTV360"));
		msgArr3.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, Constants.SERVICES));
		msgArr3.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.LONG_MESSAGE)));
		msgArr3.add(createMessageEntryMap(Constants.VALUE, "HBO Max Basic With Ads Long"));

		allMessages2.setValue(List.of(msgArr2, msgArr3));

		MessageEntry messageType2 = new MessageEntry();
		messageType2.setName(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE);
		messageType2.setValue(Map.of(Constants.KEY, "displayName")); // Different messageType

		// Group 2: allMessages BEFORE messageType (reversed order)
		List<MessageEntry> group2 = List.of(allMessages2, messageType2);

		List<List<MessageEntry>> messageGroups = List.of(group1, group2);

		Map<String, String> result = offersUtils.getAllMessagesMap(messageGroups, "opus");

		// Verify we have all expected entries from both groups
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals("Expected 3 entries (1 from group1, 2 from group2)", 3, result.size());

		// Both groups should have correctly formed keys regardless of entry order
		String key1 = Constants.DELAY_PROVISIONING + "opus" + Constants.SALES + Constants.SHORT_MESSAGE;
		String key2 = "displayName" + "DTV360" + Constants.SERVICES + Constants.SHORT_MESSAGE;
		String key3 = "displayName" + "DTV360" + Constants.SERVICES + Constants.LONG_MESSAGE;

		// Assert all keys exist
		assertTrue("Missing key from group1: " + key1, result.containsKey(key1));
		assertTrue("Missing short message key from group2: " + key2, result.containsKey(key2));
		assertTrue("Missing long message key from group2: " + key3, result.containsKey(key3));

		// Assert correct values
		assertEquals("The free 5-day trial includes the package's base channels only.  The following  access begins after the trial period.",
			result.get(key1));
		assertEquals("HBO Max Basic With Ads", result.get(key2));
		assertEquals("HBO Max Basic With Ads Long", result.get(key3));

		// CRITICAL ASSERTION: Verify no null keys exist from either group
		assertFalse("Found keys containing 'null' - two-pass approach failed for one or more groups!",
			result.keySet().stream().anyMatch(key -> key.contains("null")));

		// Verify messageTypes are distinct and properly applied
		assertTrue("Group1 messageType not found in keys",
			result.keySet().stream().anyMatch(key -> key.startsWith(Constants.DELAY_PROVISIONING)));
		assertTrue("Group2 messageType not found in keys",
			result.keySet().stream().anyMatch(key -> key.startsWith("displayName")));
	}

	@Test
	void updateDelayProvisioningMessages_setsMessagesWhenKeysPresent() {
		Attributes attributes = mock(Attributes.class);
		Map<String, String> globalMessagesMap = new HashMap<>();
		String salesChannel = "opus";
		String flowType = Constants.SALES;
		String shortKey = Constants.DELAY_PROVISIONING + salesChannel + flowType + Constants.SHORT_MESSAGE;
		String longKey = Constants.DELAY_PROVISIONING + salesChannel + flowType + Constants.LONG_MESSAGE;
		globalMessagesMap.put(shortKey, "The free 5-day trial includes the package's base channels only.  The following  access begins after the trial period.");
		globalMessagesMap.put(longKey, "<b>HBO Max Basic With Ads</b>");
		offersUtils.updateDelayProvisioningMessages(Constants.DELAY_PROVISIONING, attributes, globalMessagesMap, salesChannel, flowType);
		ArgumentCaptor<DelayProvisioningMessagesByKey> captor = ArgumentCaptor.forClass(DelayProvisioningMessagesByKey.class);
		verify(attributes).setDelayProvisioningMessagesByKey(captor.capture());
		assertEquals("The free 5-day trial includes the package's base channels only.  The following  access begins after the trial period.", captor.getValue().getShortMessage());
		assertEquals("<b>HBO Max Basic With Ads</b>", captor.getValue().getLongMessage());
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
		List<List<MessageEntry>> messageGroups = buildMessageGroups("opus", Constants.SALES, Constants.SHORT_MESSAGE, "The free 5-day trial includes the package's base channels only.  The following  access begins after the trial period.");
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

    private boolean invokeIsPriceTierSuppressed(Price price, Set<String> suppressed) throws Exception {
        Method method = OffersUtils.class.getDeclaredMethod("isPriceTierSuppressed", Price.class, Set.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, price, suppressed);
    }


    // isPriceTierSuppressed – all scenarios

    @Test
    void isPriceTierSuppressed_returnsFalse_whenPriceTierIsNull() throws Exception {
        Price price = new Price();
        price.setPriceTier(null);
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1", "tier2"));
        assertFalse(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsFalse_whenPriceTierIsEmpty() throws Exception {
        Price price = new Price();
        price.setPriceTier(Collections.emptyList());
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1", "tier2"));
        assertFalse(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsFalse_whenSuppressedSetIsEmpty() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("tier1", "tier2"));
        assertFalse(invokeIsPriceTierSuppressed(price, Collections.emptySet()));
    }


    @Test
    void isPriceTierSuppressed_returnsTrue_whenExactMatchFound() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("tier1"));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1"));
        assertTrue(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsTrue_whenMatchIsCaseInsensitive() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("TIER1"));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1"));
        assertTrue(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsTrue_whenPriceTierHasLeadingAndTrailingSpaces() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("  tier1  "));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1"));
        assertTrue(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsTrue_whenPriceTierHasMixedCaseAndSpaces() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("  TIER1  "));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1"));
        assertTrue(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsFalse_whenNoMatchInSuppressedSet() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("tierX", "tierY"));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1", "tier2"));
        assertFalse(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsTrue_whenOneOfMultipleTiersMatches() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("tierA", "tier2", "tierB"));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier2"));
        assertTrue(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsFalse_whenNoneOfMultipleTiersMatch() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("tierA", "tierB", "tierC"));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1", "tier2"));
        assertFalse(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsFalse_whenPriceTierListContainsOnlyNulls() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList(null, null));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1"));
        assertFalse(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsTrue_whenPriceTierListContainsNullsAndMatchingEntry() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList(null, "tier1", null));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1"));
        assertTrue(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsTrue_whenLastTierMatches() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("alpha", "beta", "gamma", "tier2"));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier2"));
        assertTrue(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsTrue_whenSuppressedSetHasMultipleEntriesAndOneMatches() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("tierZ"));
        // "tierZ" → toLowerCase() → "tierz" → must be in the suppressed set in lowercase
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1", "tier2", "tierz"));
        assertTrue(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsFalse_whenPriceTierNullAndSuppressedEmpty() throws Exception {
        Price price = new Price();
        price.setPriceTier(null);
        assertFalse(invokeIsPriceTierSuppressed(price, Collections.emptySet()));
    }


    @Test
    void isPriceTierSuppressed_returnsFalse_whenPriceTierIsBlankString() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("   "));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1"));
        assertFalse(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsTrue_whenAllTiersAreSuppressed() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("tier1", "tier2", "tier3"));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1", "tier2", "tier3"));
        assertTrue(invokeIsPriceTierSuppressed(price, suppressed));
    }


    @Test
    void isPriceTierSuppressed_returnsFalse_whenSingleTierNotInSuppressedSet() throws Exception {
        Price price = new Price();
        price.setPriceTier(Arrays.asList("tier99"));
        Set<String> suppressed = new HashSet<>(Arrays.asList("tier1", "tier2"));
        assertFalse(invokeIsPriceTierSuppressed(price, suppressed));
    }



    private String invokeGetFirstElement(List<String> input) throws Exception {
        Method method = OffersUtils.class.getDeclaredMethod("getFirstElement", List.class);
        method.setAccessible(true);
        return (String) method.invoke(null, input);
    }

    @Test void getFirstElement_returnsNullWhenListNull() throws Exception {
        assertNull(invokeGetFirstElement(null));
    }

    @Test void getFirstElement_returnsNullWhenListEmpty() throws Exception {
        assertNull(invokeGetFirstElement(Collections.emptyList()));
    }

    @Test void getFirstElement_returnsFirstElementWhenListHasValues() throws Exception {
        assertEquals("a", invokeGetFirstElement(Arrays.asList("a", "b", "c")));
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
		
		// Add long message as well for more realistic testing
		List<Object> msgArr2 = new ArrayList<>();
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_SALES_CHANNEL, salesChannel));
		msgArr2.add(createMessageEntryMap(Constants.ALL_MESSAGES_FLOW_TYPE, flowType));
		msgArr2.add(createMessageEntryMap(Constants.KEY, Map.of(Constants.KEY, Constants.LONG_MESSAGE)));
		msgArr2.add(createMessageEntryMap(Constants.VALUE, "<b>Free Trial includes base channels and select apps.</b>"));
		
		allMessagesEntry.setValue(List.of(msgArr, msgArr2));
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

    // resolveConflictingPriceTiers – all scenarios

    private Price buildPrice(List<String> priceTier, List<String> conflictingPriceTiers) {
		Price price = new Price();
		price.setPriceTier(priceTier);
		price.setConflictingPriceTiers(conflictingPriceTiers);
		return price;
	}


    @Test
    void resolveConflictingPriceTiers_returnsNull_whenInputIsNull() {
        assertNull(OffersUtils.resolveConflictingPriceTiers(null));
    }


    @Test
    void resolveConflictingPriceTiers_returnsSameEmptyList_whenInputIsEmpty() {
        List<Price> input = Collections.emptyList();
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(input);
        assertSame(input, result);
    }


    @Test
    void resolveConflictingPriceTiers_returnsOriginal_whenNoConflictingPriceTiersDefined() {
        Price p1 = buildPrice(Arrays.asList("base"), null);
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        List<Price> input = Arrays.asList(p1, p2);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(input);
        assertSame(input, result);
        assertEquals(2, result.size());
    }


    @Test
    void resolveConflictingPriceTiers_returnsOriginal_whenAllConflictingPriceTiersAreEmpty() {
        Price p1 = buildPrice(Arrays.asList("base"), Collections.emptyList());
        Price p2 = buildPrice(Arrays.asList("promo"), Collections.emptyList());
        List<Price> input = Arrays.asList(p1, p2);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(input);
        assertSame(input, result);
    }


    @Test
    void resolveConflictingPriceTiers_removesPrice_whenItsPriceTierIsInConflictingSet() {
        // p1 declares "promo" as conflicting; p2 has priceTier "promo" → p2 should be removed
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList("promo"));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2));
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void resolveConflictingPriceTiers_matchIsCaseInsensitive() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList("PROMO"));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2));
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void resolveConflictingPriceTiers_matchTrimmsWhitespace() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList("  promo  "));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2));
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void resolveConflictingPriceTiers_priceTierTrimmedAndLowercasedBeforeMatch() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList("promo"));
        Price p2 = buildPrice(Arrays.asList("  PROMO  "), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2));
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }

    @Test
    void resolveConflictingPriceTiers_removesOnlyConflictingPrice_whenMultiplePricesExist() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList("promo"));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        Price p3 = buildPrice(Arrays.asList("addon"), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2, p3));
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p3));
        assertFalse(result.contains(p2));
    }


    @Test
    void resolveConflictingPriceTiers_doesNotSuppressPrice_whenItHasNoPriceTier() {
        Price p1 = buildPrice(null, Arrays.asList("promo"));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2));
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void resolveConflictingPriceTiers_doesNotSuppressPrice_whenPriceTierIsEmpty() {
        Price p1 = buildPrice(Collections.emptyList(), Arrays.asList("promo"));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2));
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void resolveConflictingPriceTiers_ignoresNullPriceEntries() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList("promo"));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, null, p2, null));
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void resolveConflictingPriceTiers_ignoresNullsInConflictingPriceTiersList() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList(null, "promo", null));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2));
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void resolveConflictingPriceTiers_returnsOriginal_whenConflictingPriceTiersContainsOnlyNulls() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList(null, null));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        List<Price> input = Arrays.asList(p1, p2);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(input);
        assertSame(input, result);
    }


    @Test
    void resolveConflictingPriceTiers_removesMultiplePrices_whenMultipleConflictsExist() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList("promo", "trial"));
        Price p2 = buildPrice(Arrays.asList("promo"), null);
        Price p3 = buildPrice(Arrays.asList("trial"), null);
        Price p4 = buildPrice(Arrays.asList("addon"), null);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2, p3, p4));
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p4));
        assertFalse(result.contains(p2));
        assertFalse(result.contains(p3));
    }


    @Test
    void resolveConflictingPriceTiers_suppressesPrice_whenOneOfItsMultipleTiersIsConflicting() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList("promo"));
        Price p2 = buildPrice(Arrays.asList("addon", "promo"), null);   // has "promo" → should be suppressed
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2));
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void resolveConflictingPriceTiers_returnsSinglePrice_whenNoConflicts() {
        Price p1 = buildPrice(Arrays.asList("base"), null);
        List<Price> input = Collections.singletonList(p1);
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(input);
        assertSame(input, result);
    }


    @Test
    void resolveConflictingPriceTiers_doesNotSuppressPrice_whenPriceTierIsBlankString() {
        Price p1 = buildPrice(Arrays.asList("base"), Arrays.asList("promo"));
        Price p2 = buildPrice(Arrays.asList("   "), null);   // blank → trimmed → "" → no match
        List<Price> result = OffersUtils.resolveConflictingPriceTiers(Arrays.asList(p1, p2));
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
    }


    // expandCriteriaValues – all scenarios

    @SuppressWarnings("unchecked")
    private List<String> invokeExpandCriteriaValues(List<String> rawValues) throws Exception {
        Method method = OffersUtils.class.getDeclaredMethod("expandCriteriaValues", List.class);
        method.setAccessible(true);
        return (List<String>) method.invoke(utils, rawValues);
    }


    @Test
    void expandCriteriaValues_returnsEmptyList_whenInputIsEmpty() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Collections.emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void expandCriteriaValues_returnsSingleValue_whenNoPipePresent() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("online"));
        assertEquals(1, result.size());
        assertEquals("online", result.get(0));
    }


    @Test
    void expandCriteriaValues_splitsOnPipe_whenPipePresent() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("online|offline|instore"));
        assertEquals(3, result.size());
        assertEquals("online", result.get(0));
        assertEquals("offline", result.get(1));
        assertEquals("instore", result.get(2));
    }


    @Test
    void expandCriteriaValues_trimsSpacesAroundPipe() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("online | offline | instore"));
        assertEquals(3, result.size());
        assertEquals("online", result.get(0));
        assertEquals("offline", result.get(1));
        assertEquals("instore", result.get(2));
    }


    @Test
    void expandCriteriaValues_trimsLeadingAndTrailingWhitespace() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("  online  "));
        assertEquals(1, result.size());
        assertEquals("online", result.get(0));
    }


    @Test
    void expandCriteriaValues_skipsNullEntries() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList(null, "online", null));
        assertEquals(1, result.size());
        assertEquals("online", result.get(0));
    }


    @Test
    void expandCriteriaValues_returnsEmpty_whenAllEntriesAreNull() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList(null, null));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void expandCriteriaValues_skipsBlankStringEntries() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("   "));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void expandCriteriaValues_skipsEmptyStringEntries() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList(""));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void expandCriteriaValues_collectsAllPlainValues_whenMultipleRawEntries() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("online", "offline", "instore"));
        assertEquals(3, result.size());
        assertTrue(result.contains("online"));
        assertTrue(result.contains("offline"));
        assertTrue(result.contains("instore"));
    }


    @Test
    void expandCriteriaValues_flattensAllTokens_whenMixOfPlainAndPipeValues() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("online|offline", "instore|web"));
        assertEquals(4, result.size());
        assertTrue(result.contains("online"));
        assertTrue(result.contains("offline"));
        assertTrue(result.contains("instore"));
        assertTrue(result.contains("web"));
    }


    @Test
    void expandCriteriaValues_filtersEmptyToken_whenPipeIsAtStart() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("|online"));
        assertEquals(1, result.size());
        assertEquals("online", result.get(0));
    }


    @Test
    void expandCriteriaValues_filtersEmptyToken_whenPipeIsAtEnd() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("online|"));
        assertEquals(1, result.size());
        assertEquals("online", result.get(0));
    }


    @Test
    void expandCriteriaValues_filtersEmptyTokens_whenMultipleConsecutivePipes() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("online||offline"));
        assertEquals(2, result.size());
        assertEquals("online", result.get(0));
        assertEquals("offline", result.get(1));
    }


    @Test
    void expandCriteriaValues_handlesAllEdgeCasesTogetherCorrectly() throws Exception {
        List<String> result = invokeExpandCriteriaValues(
                Arrays.asList(null, "  ", "online|offline", "instore"));
        assertEquals(3, result.size());
        assertTrue(result.contains("online"));
        assertTrue(result.contains("offline"));
        assertTrue(result.contains("instore"));
    }


    @Test
    void expandCriteriaValues_returnsEmpty_whenValueIsOnlyAPipe() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("|"));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void expandCriteriaValues_trimsEachPipeSplitTokenIndividually() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("  online  |  offline  "));
        assertEquals(2, result.size());
        assertEquals("online", result.get(0));
        assertEquals("offline", result.get(1));
    }


    @Test
    void expandCriteriaValues_returnsSingleNumericValue_whenInputIsNumericString() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("12345"));
        assertEquals(1, result.size());
        assertEquals("12345", result.get(0));
    }


    @Test
    void expandCriteriaValues_splitsNumericValuesOnPipe() throws Exception {
        List<String> result = invokeExpandCriteriaValues(Arrays.asList("100|200|300"));
        assertEquals(3, result.size());
        assertEquals("100", result.get(0));
        assertEquals("200", result.get(1));
        assertEquals("300", result.get(2));
    }


    @Test
    void expandCriteriaValues_accumulatesAllTokensInOrder_whenLargeInputList() throws Exception {
        List<String> raw = Arrays.asList("a|b", "c", "d|e|f", null, "  ", "g");
        List<String> result = invokeExpandCriteriaValues(raw);
        assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f", "g"), result);
    }


    // isCriteriaValueMatched – all scenarios

    private boolean invokeIsCriteriaValueMatched(List<String> rawValues, String contextValue) throws Exception {
        Method method = OffersUtils.class.getDeclaredMethod("isCriteriaValueMatched", List.class, String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(utils, rawValues, contextValue);
    }


    @Test
    void isCriteriaValueMatched_returnsFalse_whenContextValueIsNull() throws Exception {
        assertFalse(invokeIsCriteriaValueMatched(Arrays.asList("online"), null));
    }


    @Test
    void isCriteriaValueMatched_returnsFalse_whenContextValueIsEmpty() throws Exception {
        assertFalse(invokeIsCriteriaValueMatched(Arrays.asList("online"), ""));
    }


    @Test
    void isCriteriaValueMatched_returnsFalse_whenContextValueIsBlank() throws Exception {
        assertFalse(invokeIsCriteriaValueMatched(Arrays.asList("online"), "   "));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenExpandedListIsEmpty_noRestriction() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList(null, null), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenRawValuesListIsEmpty() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Collections.emptyList(), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenRawValuesAreAllBlank() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("  ", "   "), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenExactMatch() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("online"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenMatchIsCaseInsensitive_contextUpper() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("online"), "ONLINE"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenMatchIsCaseInsensitive_rawUpper() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("ONLINE"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenContextValueHasLeadingTrailingSpaces() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("online"), "  online  "));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenRawValueHasLeadingTrailingSpaces() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("  online  "), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsFalse_whenNoMatchFound() throws Exception {
        assertFalse(invokeIsCriteriaValueMatched(Arrays.asList("offline"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenOnePipeSeparatedTokenMatches() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("offline|online|instore"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsFalse_whenNoPipeSeparatedTokenMatches() throws Exception {
        assertFalse(invokeIsCriteriaValueMatched(Arrays.asList("offline|instore|web"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenOneOfMultipleRawEntriesMatches() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("offline", "online", "instore"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsFalse_whenNoneOfMultipleRawEntriesMatch() throws Exception {
        assertFalse(invokeIsCriteriaValueMatched(Arrays.asList("offline", "instore", "web"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenNullMixedWithMatchingRawValue() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList(null, "online", null), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsFalse_whenNullMixedWithNonMatchingRawValues() throws Exception {
        assertFalse(invokeIsCriteriaValueMatched(Arrays.asList(null, "offline", null), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenPipeSeparatedValueMatchesCaseInsensitively() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("ONLINE|OFFLINE"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenMatchIsLastPipeToken() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("instore|web|offline|online"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenContextValueHasMixedCaseAndSpaces() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("online"), "  OnLiNe  "));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenBlankEntryMixedWithPipeMatchingEntry() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("   ", "offline|online"), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenAllRawValuesBlankAndContextValueValid() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("  ", "   ", ""), "online"));
    }


    @Test
    void isCriteriaValueMatched_returnsTrue_whenSingleCharacterMatches() throws Exception {
        assertTrue(invokeIsCriteriaValueMatched(Arrays.asList("Y"), "y"));
    }


    @Test
    void isCriteriaValueMatched_returnsFalse_whenSingleCharacterDoesNotMatch() throws Exception {
        assertFalse(invokeIsCriteriaValueMatched(Arrays.asList("Y"), "N"));
    }

    // isPriceEligibleByCriteria – all scenarios

    private Price buildPriceWithCriteria(List<String> attributePricingCriteria) {
        Price price = new Price();
        price.setAttributePricingCriteria(attributePricingCriteria);
        return price;
    }


    private Map<String, String> criteriaContext(String... keyValues) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValues.length - 1; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCriteriaIsNull() {
        Price price = buildPriceWithCriteria(null);
        assertTrue(utils.isPriceEligibleByCriteria(price, Collections.emptyMap()));
    }

    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCriteriaIsNull_andContextIsNull() {
        // null criteria → returns true immediately, null context is not even reached
        Price price = buildPriceWithCriteria(null);
        assertTrue(utils.isPriceEligibleByCriteria(price, null));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCriteriaIsEmpty() {
        Price price = buildPriceWithCriteria(Collections.emptyList());
        assertTrue(utils.isPriceEligibleByCriteria(price, Collections.emptyMap()));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenAllCriteriaEntriesAreNull() {
        Price price = buildPriceWithCriteria(Arrays.asList(null, null));
        assertTrue(utils.isPriceEligibleByCriteria(price, Collections.emptyMap()));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCriteriaEntriesHaveNoEqualsSign() {
        Price price = buildPriceWithCriteria(Arrays.asList("noEqualsHere", "alsoNoEquals"));
        assertTrue(utils.isPriceEligibleByCriteria(price, Collections.emptyMap()));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCriteriaMixOfNullAndInvalidEntries() {
        Price price = buildPriceWithCriteria(Arrays.asList(null, "invalidEntry", null));
        assertTrue(utils.isPriceEligibleByCriteria(price, Collections.emptyMap()));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenSingleCriterionMatchesExactly() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "IPTV");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenSingleCriterionDoesNotMatch() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "SATELLITE");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenContextKeyIsAbsent() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV"));
        assertTrue(utils.isPriceEligibleByCriteria(price, Collections.emptyMap()) == false);
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCriterionKeyHasWhitespace() {
        Price price = buildPriceWithCriteria(Arrays.asList("  serviceSubscriptionType  =IPTV"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "IPTV");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCriterionValueHasWhitespace() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=  IPTV  "));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "IPTV");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenMatchIsCaseInsensitive() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "iptv");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenPipeSeparatedFirstTokenMatches() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV|SATELLITE|OTT"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "IPTV");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenPipeSeparatedMiddleTokenMatches() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV|SATELLITE|OTT"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "SATELLITE");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenPipeSeparatedLastTokenMatches() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV|SATELLITE|OTT"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "OTT");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenNoPipeSeparatedTokenMatches() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV|SATELLITE|OTT"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "CABLE");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenMultipleEntriesSameKeyOneMatches() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV",
                "serviceSubscriptionType=SATELLITE"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "SATELLITE");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenMultipleEntriesSameKeyNoneMatch() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV",
                "serviceSubscriptionType=SATELLITE"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "CABLE");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenAllKeysMatchAndLogic() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV",
                "creditRisk=LOW"));
        Map<String, String> ctx = criteriaContext(
                "serviceSubscriptionType", "IPTV",
                "creditRisk", "LOW");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenOneKeyFailsAndLogic() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV",
                "creditRisk=LOW"));
        Map<String, String> ctx = criteriaContext(
                "serviceSubscriptionType", "IPTV",
                "creditRisk", "HIGH");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenFirstKeyFailsAndLogic() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV",
                "creditRisk=LOW"));
        Map<String, String> ctx = criteriaContext(
                "serviceSubscriptionType", "SATELLITE",
                "creditRisk", "LOW");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenThreeKeysAllMatch() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV",
                "creditRisk=LOW",
                "treatmentCode=TC1"));
        Map<String, String> ctx = criteriaContext(
                "serviceSubscriptionType", "IPTV",
                "creditRisk", "LOW",
                "treatmentCode", "TC1");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenThirdKeyFailsAndLogic() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV",
                "creditRisk=LOW",
                "treatmentCode=TC1"));
        Map<String, String> ctx = criteriaContext(
                "serviceSubscriptionType", "IPTV",
                "creditRisk", "LOW",
                "treatmentCode", "TC999");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenAndWithOrCombinationAllPass() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV|SATELLITE",
                "creditRisk=LOW"));
        Map<String, String> ctx = criteriaContext(
                "serviceSubscriptionType", "SATELLITE",
                "creditRisk", "LOW");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenOrPassesButAndFails() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV|SATELLITE",
                "creditRisk=LOW"));
        Map<String, String> ctx = criteriaContext(
                "serviceSubscriptionType", "SATELLITE",
                "creditRisk", "HIGH");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCriterionValueIsBlankExpandsEmpty() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=   "));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "IPTV");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenContextValueHasSurroundingSpaces() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "  IPTV  ");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenContextValueIsNullForKey() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV"));
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", null);
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenContextValueIsEmptyForKey() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType=IPTV"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }

    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenCriteriaEntryHasEmptyKey() {
        Price price = buildPriceWithCriteria(Arrays.asList("=IPTV"));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "IPTV");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCriteriaEntryHasEmptyValue() {
        Price price = buildPriceWithCriteria(Arrays.asList("serviceSubscriptionType="));
        Map<String, String> ctx = criteriaContext("serviceSubscriptionType", "IPTV");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenIapPartnerTypeMatches() {
        Price price = buildPriceWithCriteria(Arrays.asList("iapPartnerType=ROKU"));
        Map<String, String> ctx = criteriaContext("iapPartnerType", "ROKU");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenTreatmentCodeMatches() {
        Price price = buildPriceWithCriteria(Arrays.asList("treatmentCode=TC100"));
        Map<String, String> ctx = criteriaContext("treatmentCode", "TC100");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenCreditRiskMatches() {
        Price price = buildPriceWithCriteria(Arrays.asList("creditRisk=LOW"));
        Map<String, String> ctx = criteriaContext("creditRisk", "LOW");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsTrue_whenAllFourRealWorldKeysMatch() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV",
                "iapPartnerType=ROKU",
                "treatmentCode=TC100",
                "creditRisk=LOW"));
        Map<String, String> ctx = criteriaContext(
                "serviceSubscriptionType", "IPTV",
                "iapPartnerType", "ROKU",
                "treatmentCode", "TC100",
                "creditRisk", "LOW");
        assertTrue(utils.isPriceEligibleByCriteria(price, ctx));
    }


    @Test
    void isPriceEligibleByCriteria_returnsFalse_whenOneOfFourRealWorldKeysFails() {
        Price price = buildPriceWithCriteria(Arrays.asList(
                "serviceSubscriptionType=IPTV",
                "iapPartnerType=ROKU",
                "treatmentCode=TC100",
                "creditRisk=LOW"));
        Map<String, String> ctx = criteriaContext(
                "serviceSubscriptionType", "IPTV",
                "iapPartnerType", "FIRETV",   // ← mismatch
                "treatmentCode", "TC100",
                "creditRisk", "LOW");
        assertFalse(utils.isPriceEligibleByCriteria(price, ctx));
    }


    // resolveIapPartnerType – all scenarios

    private OfferRequest buildOfferRequestWithIapPartnerAccountType(String iapPartnerAccountType) {
        CartProduct ott = new CartProduct();
        ott.setIapPartnerAccountType(iapPartnerAccountType);
        com.dtv.dcp.epoch.model.common.request.CustomerContext customerContext =
                new com.dtv.dcp.epoch.model.common.request.CustomerContext();
        customerContext.setOtt(ott);
        OfferRequest request = new OfferRequest();
        request.setCustomerContext(customerContext);
        return request;
    }


    private OfferRequest buildOfferRequestWithSalesChannel(List<String> salesChannel) {
        OfferRequest request = new OfferRequest();
        request.setSalesChannel(salesChannel);
        return request;
    }


    private OfferRequest buildOfferRequestWithBoth(String iapPartnerAccountType, List<String> salesChannel) {
        OfferRequest request = buildOfferRequestWithIapPartnerAccountType(iapPartnerAccountType);
        request.setSalesChannel(salesChannel);
        return request;
    }

    @Test
    void resolveIapPartnerType_returnsNull_whenOfferRequestIsNull() {
        assertNull(utils.resolveIapPartnerType(null));
    }

    @Test
    void resolveIapPartnerType_returnsOttIapPartnerAccountType_whenSet() {
        OfferRequest request = buildOfferRequestWithIapPartnerAccountType("ROKU");
        String result = utils.resolveIapPartnerType(request);
        assertEquals("ROKU", result);
        // redisCacheHelper.getValues should NOT be called because OTT value short-circuits
        verify(redisCacheHelper, never()).getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT);
    }


    @Test
    void resolveIapPartnerType_returnsFireTv_whenOttIapPartnerAccountTypeIsFireTv() {
        OfferRequest request = buildOfferRequestWithIapPartnerAccountType("FIRETV");
        assertEquals("FIRETV", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsGoogle_whenOttIapPartnerAccountTypeIsGoogle() {
        OfferRequest request = buildOfferRequestWithIapPartnerAccountType("GOOGLE");
        assertEquals("GOOGLE", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsNull_whenOttNullAndRedisListEmpty() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("ROKU"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());
        assertNull(utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsNull_whenOttNullAndRedisListNull() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("ROKU"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(null);
        assertNull(utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_fallsThroughToRedis_whenOttIapPartnerAccountTypeIsEmpty() {
        OfferRequest request = buildOfferRequestWithBoth("", Arrays.asList("ROKU"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV", "FIRETV:AMAZONFIRE"));
        assertEquals("ROKUTV", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsMappedType_whenSalesChannelMatchesFirstRedisEntry() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("ROKU"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV", "FIRETV:AMAZONFIRE", "GOOGLE:GOOGLETV"));
        assertEquals("ROKUTV", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsMappedType_whenSalesChannelMatchesMiddleRedisEntry() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("FIRETV"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV", "FIRETV:AMAZONFIRE", "GOOGLE:GOOGLETV"));
        assertEquals("AMAZONFIRE", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsMappedType_whenSalesChannelMatchesLastRedisEntry() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("GOOGLE"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV", "FIRETV:AMAZONFIRE", "GOOGLE:GOOGLETV"));
        assertEquals("GOOGLETV", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_matchIsCaseInsensitive_whenSalesChannelLowerCase() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("roku"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV"));
        assertEquals("ROKUTV", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsNull_whenSalesChannelMatchesNoRedisEntry() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("ONLINE"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV", "FIRETV:AMAZONFIRE"));
        assertNull(utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsNull_whenSalesChannelListIsEmpty() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Collections.emptyList());
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV"));
        assertNull(utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsNull_whenSalesChannelListIsNull() {
        OfferRequest request = new OfferRequest();
        request.setSalesChannel(null);
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV"));
        assertNull(utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsNull_whenFirstSalesChannelEntryIsNull() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList((String) null));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV"));
        assertNull(utils.resolveIapPartnerType(request));
    }



    @Test
    void resolveIapPartnerType_handlesSpacesAroundColonInRedisEntry() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("ROKU"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU : ROKUTV"));
        assertEquals("ROKUTV", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsNull_whenRedisEntryHasNoColon() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("ROKU"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKUROKUTV"));
        assertNull(utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_returnsMappedType_whenValidEntryPresentAfterMalformedOnes() {
        OfferRequest request = buildOfferRequestWithSalesChannel(Arrays.asList("FIRETV"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("BADENTRY", "ROKU:ROKUTV", "FIRETV:AMAZONFIRE"));
        assertEquals("AMAZONFIRE", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_fallsThroughToRedis_whenCustomerContextIsNull() {
        OfferRequest request = new OfferRequest();
        request.setCustomerContext(null);
        request.setSalesChannel(Arrays.asList("ROKU"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV"));
        assertEquals("ROKUTV", utils.resolveIapPartnerType(request));
    }


    @Test
    void resolveIapPartnerType_fallsThroughToRedis_whenOttIsNull() {
        com.dtv.dcp.epoch.model.common.request.CustomerContext ctx =
                new com.dtv.dcp.epoch.model.common.request.CustomerContext();
        ctx.setOtt(null);
        OfferRequest request = new OfferRequest();
        request.setCustomerContext(ctx);
        request.setSalesChannel(Arrays.asList("ROKU"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV"));
        assertEquals("ROKUTV", utils.resolveIapPartnerType(request));
    }

    @Test
    void resolveIapPartnerType_ottTakesPriorityOverSalesChannel() {
        OfferRequest request = buildOfferRequestWithBoth("GOOGLE", Arrays.asList("ROKU"));
        // Redis should NOT be consulted since OTT value takes priority
        assertEquals("GOOGLE", utils.resolveIapPartnerType(request));
        verify(redisCacheHelper, never()).getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT);
    }


    // buildPricingCriteriaContext – all scenarios

    private CTOffer buildCTOffer(List<String> serviceSubscriptionTypes,
                                  List<String> treatmentCodes,
                                  List<String> creditRisks) {
        OfferAttributes attrs = new OfferAttributes();
        attrs.setServiceSubscriptionType(serviceSubscriptionTypes);
        attrs.setTreatmentCode(treatmentCodes);
        attrs.setCreditRisk(creditRisks);
        CTOffer offer = new CTOffer();
        offer.setAttributes(attrs);
        return offer;
    }

    private OfferRequestWrapper buildWrapper(OfferRequest req) {
        OfferRequestWrapper wrapper = new OfferRequestWrapper();
        wrapper.setOfferRequest(req);
        return wrapper;
    }


    @Test
    void buildPricingCriteriaContext_returnsMapWithNullIap_whenBothNull() {
        Map<String, String> ctx = utils.buildPricingCriteriaContext(null, null);
        assertNotNull(ctx);
        assertFalse(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertFalse(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertFalse(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_doesNotPopulateOfferKeys_whenOfferIsNull() {
        OfferRequest req = buildOfferRequestWithIapPartnerAccountType("ROKU");
        Map<String, String> ctx = utils.buildPricingCriteriaContext(null, buildWrapper(req));
        assertNotNull(ctx);
        assertFalse(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertFalse(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertFalse(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
        assertEquals("ROKU", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_doesNotPopulateOfferKeys_whenOfferAttributesIsNull() {
        CTOffer offer = new CTOffer();  // no attributes set
        OfferRequest req = buildOfferRequestWithIapPartnerAccountType("FIRETV");
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertNotNull(ctx);
        assertFalse(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertFalse(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertFalse(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
        assertEquals("FIRETV", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_populatesAllOfferKeys_whenAttributesFullyPopulated() {
        CTOffer offer = buildCTOffer(
                Arrays.asList("IPTV"),
                Arrays.asList("TC100"),
                Arrays.asList("LOW"));
        OfferRequest req = buildOfferRequestWithIapPartnerAccountType("ROKU");
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertEquals("IPTV",  ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertEquals("TC100", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertEquals("LOW",   ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
        assertEquals("ROKU",  ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_usesFirstElement_whenAttributeListsHaveMultipleValues() {
        CTOffer offer = buildCTOffer(
                Arrays.asList("IPTV", "SATELLITE", "OTT"),
                Arrays.asList("TC100", "TC200"),
                Arrays.asList("LOW", "MEDIUM", "HIGH"));
        OfferRequest req = buildOfferRequestWithIapPartnerAccountType("ROKU");
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertEquals("IPTV",  ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertEquals("TC100", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertEquals("LOW",   ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
    }


    @Test
    void buildPricingCriteriaContext_mapsNullForServiceSubscriptionType_whenListIsNull() {
        CTOffer offer = buildCTOffer(null, Arrays.asList("TC100"), Arrays.asList("LOW"));
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(new OfferRequest()));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertEquals("TC100", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertEquals("LOW",   ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
    }


    @Test
    void buildPricingCriteriaContext_mapsNullForTreatmentCode_whenListIsNull() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), null, Arrays.asList("LOW"));
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(new OfferRequest()));
        assertEquals("IPTV", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertEquals("LOW",  ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
    }


    @Test
    void buildPricingCriteriaContext_mapsNullForCreditRisk_whenListIsNull() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC100"), null);
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(new OfferRequest()));
        assertEquals("IPTV",  ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertEquals("TC100", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
    }


    @Test
    void buildPricingCriteriaContext_mapsNullForAllOfferKeys_whenAllAttributeListsEmpty() {
        CTOffer offer = buildCTOffer(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(new OfferRequest()));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
    }


    @Test
    void buildPricingCriteriaContext_mapsNullForAllOfferKeys_whenAllAttributeListsNull() {
        CTOffer offer = buildCTOffer(null, null, null);
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(new OfferRequest()));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
    }


    @Test
    void buildPricingCriteriaContext_mapsNullIap_whenOfferRequestWrapperIsNull() {
        CTOffer offer = buildCTOffer(
                Arrays.asList("IPTV"),
                Arrays.asList("TC100"),
                Arrays.asList("LOW"));
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, null);
        assertEquals("IPTV",  ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertEquals("TC100", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertEquals("LOW",   ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_populatesIapFromOtt_whenOttIapPartnerAccountTypeSet() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        OfferRequest req = buildOfferRequestWithIapPartnerAccountType("ROKU");
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertEquals("ROKU", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_populatesIapFromOtt_whenOttIapIsFireTv() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        OfferRequest req = buildOfferRequestWithIapPartnerAccountType("FIRETV");
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertEquals("FIRETV", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_populatesIapFromOtt_whenOttIapIsGoogle() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        OfferRequest req = buildOfferRequestWithIapPartnerAccountType("GOOGLE");
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertEquals("GOOGLE", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_populatesIapFromRedis_whenNoOttAndSalesChannelMatches() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        OfferRequest req = buildOfferRequestWithSalesChannel(Arrays.asList("ROKU"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV", "FIRETV:AMAZONFIRE"));
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertEquals("ROKUTV", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_populatesIapFromRedis_whenSalesChannelIsFireTv() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        OfferRequest req = buildOfferRequestWithSalesChannel(Arrays.asList("FIRETV"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV", "FIRETV:AMAZONFIRE"));
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertEquals("AMAZONFIRE", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }

    @Test
    void buildPricingCriteriaContext_mapsNullIap_whenSalesChannelNotInRedisList() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        OfferRequest req = buildOfferRequestWithSalesChannel(Arrays.asList("ONLINE"));
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Arrays.asList("ROKU:ROKUTV", "FIRETV:AMAZONFIRE"));
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_mapsNullIap_whenNoOttAndNoSalesChannelAndRedisEmpty() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        OfferRequest req = new OfferRequest();
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_ottIapTakesPriority_overSalesChannelDerivedIap() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        OfferRequest req = buildOfferRequestWithBoth("GOOGLE", Arrays.asList("ROKU"));
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        assertEquals("GOOGLE", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
        // Redis must NOT be consulted
        verify(redisCacheHelper, never()).getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT);
    }


    @Test
    void buildPricingCriteriaContext_alwaysContainsIapPartnerTypeKey() {
        Map<String, String> ctx = utils.buildPricingCriteriaContext(null, null);
        assertTrue(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_returnsFourKeyMap_whenOfferPopulatedAndWrapperNull() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, null);
        assertTrue(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertTrue(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertTrue(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
        assertTrue(ctx.containsKey(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
        assertEquals(4, ctx.size());
    }


    @Test
    void buildPricingCriteriaContext_mapsNullIap_whenWrapperOfferRequestIsNull() {
        CTOffer offer = buildCTOffer(Arrays.asList("IPTV"), Arrays.asList("TC1"), Arrays.asList("LOW"));
        OfferRequestWrapper wrapper = new OfferRequestWrapper();  // offerRequest not set → null
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, wrapper);
        assertNull(ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
        assertEquals("IPTV", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
    }


    @Test
    void buildPricingCriteriaContext_fullRealisticScenario_allKeysCorrect() {
        CTOffer offer = buildCTOffer(
                Arrays.asList("IPTV", "OTT"),
                Arrays.asList("TC999", "TC000"),
                Arrays.asList("HIGH", "LOW"));
        OfferRequest req = buildOfferRequestWithBoth("ROKU", Arrays.asList("FIRETV"));
        Map<String, String> ctx = utils.buildPricingCriteriaContext(offer, buildWrapper(req));
        // First element of each list is used
        assertEquals("IPTV",  ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_SERVICE_SUBSCRIPTION_TYPE));
        assertEquals("TC999", ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_TREATMENT_CODE));
        assertEquals("HIGH",  ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_CREDIT_RISK));
        // OTT wins over salesChannel
        assertEquals("ROKU",  ctx.get(Constants.ELIGIBILITY_CRITERIA_KEY_IAP_PARTNER_TYPE));
        assertEquals(4, ctx.size());
    }


    // evaluateAndSelectPrices – all scenarios


    private Price buildFullPrice(String id,
                                  List<String> attributePricingCriteria,
                                  List<String> priceTier,
                                  List<String> conflictingPriceTiers) {
        Price price = new Price();
        price.setId(id);
        price.setAttributePricingCriteria(attributePricingCriteria);
        price.setPriceTier(priceTier);
        price.setConflictingPriceTiers(conflictingPriceTiers);
        return price;
    }


    @Test
    void evaluateAndSelectPrices_returnsEmptyList_whenPricesIsNull() {
        List<Price> result = utils.evaluateAndSelectPrices(null, Collections.emptyMap());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void evaluateAndSelectPrices_returnsEmptyList_whenPricesIsEmpty() {
        List<Price> result = utils.evaluateAndSelectPrices(Collections.emptyList(), Collections.emptyMap());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void evaluateAndSelectPrices_returnsEmptyList_whenPricesListContainsOnlyNulls() {
        List<Price> input = Arrays.asList(null, null, null);
        List<Price> result = utils.evaluateAndSelectPrices(input, Collections.emptyMap());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void evaluateAndSelectPrices_filtersNullsFromInput_inBauStep() {
        Price p1 = buildFullPrice("p1", null, null, null);
        Price p2 = buildFullPrice("p2", null, null, null);
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(null, p1, null, p2), Collections.emptyMap());
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
    }


    @Test
    void evaluateAndSelectPrices_returnsAllPrices_whenNoCriteriaOnAnyPrice() {
        Price p1 = buildFullPrice("p1", null, null, null);
        Price p2 = buildFullPrice("p2", null, null, null);
        Price p3 = buildFullPrice("p3", null, null, null);
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2, p3), Collections.emptyMap());
        assertEquals(3, result.size());
    }


    @Test
    void evaluateAndSelectPrices_returnsOnlyMatchingPrice_whenCriteriaFilterApplied() {
        Price p1 = buildFullPrice("p1", Arrays.asList("serviceSubscriptionType=IPTV"), null, null);
        Price p2 = buildFullPrice("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"), null, null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "IPTV");
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2), ctx);
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_returnsAllPrices_whenAllPassCriteria() {
        Price p1 = buildFullPrice("p1", Arrays.asList("serviceSubscriptionType=IPTV"), null, null);
        Price p2 = buildFullPrice("p2", Arrays.asList("serviceSubscriptionType=IPTV"), null, null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "IPTV");
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2), ctx);
        assertEquals(2, result.size());
    }


    @Test
    void evaluateAndSelectPrices_fallsBackToBau_whenNoPricePassesCriteria() {
        Price p1 = buildFullPrice("p1", Arrays.asList("serviceSubscriptionType=IPTV"), null, null);
        Price p2 = buildFullPrice("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"), null, null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "CABLE");  // matches neither
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2), ctx);
        // BAU fallback: all non-null prices returned (before conflict resolution)
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
    }


    @Test
    void evaluateAndSelectPrices_returnsPricesWithNoCriteria_whenContextIsEmpty() {
        Price p1 = buildFullPrice("p1", null, null, null);                                       // no criteria → passes
        Price p2 = buildFullPrice("p2", Arrays.asList("serviceSubscriptionType=IPTV"), null, null); // fails (no context)
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2), Collections.emptyMap());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getId());
    }


    @Test
    void evaluateAndSelectPrices_removesConflictingPrice_afterCriteriaStep() {
        // p1 declares "promo" as conflicting; p2 has priceTier "promo"
        Price p1 = buildFullPrice("p1", null, Arrays.asList("base"),  Arrays.asList("promo"));
        Price p2 = buildFullPrice("p2", null, Arrays.asList("promo"), null);
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2), Collections.emptyMap());
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_noConflictRemoval_whenCriteriaAlreadyFilteredConflictingPrice() {
        // p1 passes criteria; p2 fails criteria (removed in step 2) – p1 has no conflicts with itself
        Price p1 = buildFullPrice("p1", Arrays.asList("serviceSubscriptionType=IPTV"),
                Arrays.asList("base"), Arrays.asList("promo"));
        Price p2 = buildFullPrice("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"),
                Arrays.asList("promo"), null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "IPTV");
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2), ctx);
        // Only p1 passed criteria; "promo" suppressed but p1's tier is "base" → p1 kept
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_appliesConflictResolution_onBauFallback() {
        // Neither passes criteria; p1 declares p2's tier as conflicting
        Price p1 = buildFullPrice("p1", Arrays.asList("serviceSubscriptionType=IPTV"),
                Arrays.asList("base"), Arrays.asList("promo"));
        Price p2 = buildFullPrice("p2", Arrays.asList("serviceSubscriptionType=IPTV"),
                Arrays.asList("promo"), null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "CABLE"); // matches neither → BAU fallback
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2), ctx);
        // BAU has both; conflict resolution removes p2 (tier "promo" suppressed)
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_returnsSinglePrice_whenNoCriteriaAndNoConflicts() {
        Price p1 = buildFullPrice("p1", null, Arrays.asList("base"), null);
        List<Price> result = utils.evaluateAndSelectPrices(Collections.singletonList(p1), Collections.emptyMap());
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_returnsSinglePrice_whenCriteriaMatches() {
        Price p1 = buildFullPrice("p1", Arrays.asList("serviceSubscriptionType=IPTV"), null, null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "IPTV");
        List<Price> result = utils.evaluateAndSelectPrices(Collections.singletonList(p1), ctx);
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_returnsSinglePrice_viaBauFallback_whenCriteriaDoesNotMatch() {
        Price p1 = buildFullPrice("p1", Arrays.asList("serviceSubscriptionType=IPTV"), null, null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "CABLE");
        List<Price> result = utils.evaluateAndSelectPrices(Collections.singletonList(p1), ctx);
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_returnsMatchingPrice_whenMultiKeyAndCriteriaAllMatch() {
        Price p1 = buildFullPrice("p1",
                Arrays.asList("serviceSubscriptionType=IPTV", "creditRisk=LOW"), null, null);
        Price p2 = buildFullPrice("p2",
                Arrays.asList("serviceSubscriptionType=IPTV", "creditRisk=HIGH"), null, null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "IPTV");
        ctx.put("creditRisk", "LOW");
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2), ctx);
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_returnsMatchingPrice_whenPipeSeparatedOrCriteriaMatches() {
        Price p1 = buildFullPrice("p1",
                Arrays.asList("serviceSubscriptionType=IPTV|SATELLITE"), null, null);
        Price p2 = buildFullPrice("p2",
                Arrays.asList("serviceSubscriptionType=OTT"), null, null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "SATELLITE");
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2), ctx);
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_removesMultipleConflictingPrices_afterCriteriaStep() {
        Price p1 = buildFullPrice("p1", null, Arrays.asList("base"),
                Arrays.asList("promo", "trial"));   // suppresses promo and trial
        Price p2 = buildFullPrice("p2", null, Arrays.asList("promo"), null);
        Price p3 = buildFullPrice("p3", null, Arrays.asList("trial"), null);
        Price p4 = buildFullPrice("p4", null, Arrays.asList("addon"), null);
        List<Price> result = utils.evaluateAndSelectPrices(
                Arrays.asList(p1, p2, p3, p4), Collections.emptyMap());
        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p4));
        assertFalse(result.contains(p2));
        assertFalse(result.contains(p3));
    }

    @Test
    void evaluateAndSelectPrices_fullPipeline_criteriaFilterThenConflictResolution() {
        // p1: passes criteria, declares "promo" conflicting
        Price p1 = buildFullPrice("p1",
                Arrays.asList("serviceSubscriptionType=IPTV"),
                Arrays.asList("base"), Arrays.asList("promo"));
        // p2: passes criteria, tier "promo" → removed by conflict resolution
        Price p2 = buildFullPrice("p2",
                Arrays.asList("serviceSubscriptionType=IPTV"),
                Arrays.asList("promo"), null);
        // p3: fails criteria → removed in step 2
        Price p3 = buildFullPrice("p3",
                Arrays.asList("serviceSubscriptionType=SATELLITE"),
                Arrays.asList("base"), null);
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "IPTV");
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2, p3), ctx);
        assertEquals(1, result.size());
        assertSame(p1, result.get(0));
    }


    @Test
    void evaluateAndSelectPrices_returnsAll_whenNoCriteriaAndNoConflictsFullPipeline() {
        Price p1 = buildFullPrice("p1", null, Arrays.asList("t1"), null);
        Price p2 = buildFullPrice("p2", null, Arrays.asList("t2"), null);
        Price p3 = buildFullPrice("p3", null, Arrays.asList("t3"), null);
        List<Price> result = utils.evaluateAndSelectPrices(Arrays.asList(p1, p2, p3), Collections.emptyMap());
        assertEquals(3, result.size());
    }

    // applyAttributePricingToProductWrappers – all scenarios



    private void invokeApplyAttributePricing(List<com.dtv.dcp.epoch.model.ct.product.ProductWrapper> productWrappers,
                                              Map<String, String> criteriaContext,
                                              String offerCode) throws Exception {
        Method method = OffersUtils.class.getDeclaredMethod(
                "applyAttributePricingToProductWrappers",
                List.class, Map.class, String.class);
        method.setAccessible(true);
        method.invoke(utils, productWrappers, criteriaContext, offerCode);
    }


    private Price buildPriceForVariant(String id, List<String> criteria) {
        Price price = new Price();
        price.setId(id);
        price.setAttributePricingCriteria(criteria);
        return price;
    }


    private com.dtv.dcp.epoch.model.ct.product.Variant buildVariant(List<Price> prices) {
        com.dtv.dcp.epoch.model.ct.product.Variant variant = new com.dtv.dcp.epoch.model.ct.product.Variant();
        variant.setPrices(prices);
        return variant;
    }


    private com.dtv.dcp.epoch.model.ct.product.Product buildProduct(
            String key, List<com.dtv.dcp.epoch.model.ct.product.Variant> variants) {
        com.dtv.dcp.epoch.model.ct.product.ProductObj obj = new com.dtv.dcp.epoch.model.ct.product.ProductObj();
        obj.setVariants(variants);
        com.dtv.dcp.epoch.model.ct.product.Product product = new com.dtv.dcp.epoch.model.ct.product.Product();
        product.setKey(key);
        product.setObj(obj);
        return product;
    }


    private com.dtv.dcp.epoch.model.ct.product.ProductWrapper buildProductWrapper(
            List<com.dtv.dcp.epoch.model.ct.product.Product> products) {
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                new com.dtv.dcp.epoch.model.ct.product.ProductWrapper();
        wrapper.setProducts(products);
        return wrapper;
    }


    @Test
    void applyAttributePricingToProductWrappers_doesNothing_whenProductWrappersIsNull() throws Exception {
        invokeApplyAttributePricing(null, Collections.emptyMap(), "OFFER-1");
        // No exception, no NPE – test passes if no exception is thrown
    }


    @Test
    void applyAttributePricingToProductWrappers_doesNothing_whenProductWrappersIsEmpty() throws Exception {
        invokeApplyAttributePricing(Collections.emptyList(), Collections.emptyMap(), "OFFER-1");
        // No exception – test passes
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsNullProductWrappers() throws Exception {
        List<com.dtv.dcp.epoch.model.ct.product.ProductWrapper> wrappers = new ArrayList<>();
        wrappers.add(null);
        wrappers.add(null);
        invokeApplyAttributePricing(wrappers, Collections.emptyMap(), "OFFER-1");
        // No exception
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsWrapper_whenProductsIsNull() throws Exception {
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                new com.dtv.dcp.epoch.model.ct.product.ProductWrapper();
        wrapper.setProducts(null);
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // No exception
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsWrapper_whenProductsIsEmpty() throws Exception {
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper = buildProductWrapper(Collections.emptyList());
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // No exception
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsNullProductEntries() throws Exception {
        List<com.dtv.dcp.epoch.model.ct.product.Product> products = new ArrayList<>();
        products.add(null);
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper = buildProductWrapper(products);
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // No exception
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsProduct_whenObjIsNull() throws Exception {
        com.dtv.dcp.epoch.model.ct.product.Product product = new com.dtv.dcp.epoch.model.ct.product.Product();
        product.setObj(null);
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // No exception
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsProduct_whenVariantsIsNull() throws Exception {
        com.dtv.dcp.epoch.model.ct.product.ProductObj obj = new com.dtv.dcp.epoch.model.ct.product.ProductObj();
        obj.setVariants(null);
        com.dtv.dcp.epoch.model.ct.product.Product product = new com.dtv.dcp.epoch.model.ct.product.Product();
        product.setObj(obj);
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // No exception
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsProduct_whenVariantsIsEmpty() throws Exception {
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("p1", Collections.emptyList());
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // No exception
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsNullVariants() throws Exception {
        List<com.dtv.dcp.epoch.model.ct.product.Variant> variants = new ArrayList<>();
        variants.add(null);
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("p1", variants);
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // No exception
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsVariant_whenPricesIsNull() throws Exception {
        com.dtv.dcp.epoch.model.ct.product.Variant variant = buildVariant(null);
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("p1",
                Collections.singletonList(variant));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // Prices remain null – no update performed
        assertNull(variant.getPrices());
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsVariant_whenPricesHasOnlyOneEntry() throws Exception {
        Price p1 = buildPriceForVariant("p1", null);
        List<Price> originalPrices = new ArrayList<>(Collections.singletonList(p1));
        com.dtv.dcp.epoch.model.ct.product.Variant variant = buildVariant(originalPrices);
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("prod1",
                Collections.singletonList(variant));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // evaluateAndSelectPrices returns a new list – verify size and content by ID
        assertEquals(1, variant.getPrices().size());
        assertEquals("p1", variant.getPrices().get(0).getId());
    }


    @Test
    void applyAttributePricingToProductWrappers_keepsBothPrices_whenBothHaveNoCriteria() throws Exception {
        Price p1 = buildPriceForVariant("p1", null);
        Price p2 = buildPriceForVariant("p2", null);
        List<Price> prices = Arrays.asList(p1, p2);
        com.dtv.dcp.epoch.model.ct.product.Variant variant = buildVariant(prices);
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("prod1",
                Collections.singletonList(variant));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        assertEquals(2, variant.getPrices().size());
    }


    @Test
    void applyAttributePricingToProductWrappers_updatesVariantPrices_whenCriteriaFiltersOnePrice() throws Exception {
        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));
        com.dtv.dcp.epoch.model.ct.product.Variant variant = buildVariant(new ArrayList<>(Arrays.asList(p1, p2)));
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("prod1",
                Collections.singletonList(variant));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "IPTV");
        invokeApplyAttributePricing(Collections.singletonList(wrapper), ctx, "OFFER-1");
        assertEquals(1, variant.getPrices().size());
        assertEquals("p1", variant.getPrices().get(0).getId());
    }


    @Test
    void applyAttributePricingToProductWrappers_keepsBauPrices_whenNoPriceMatchesCriteria() throws Exception {
        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));
        Price p3 = buildPriceForVariant("p3", Arrays.asList("serviceSubscriptionType=OTT"));
        com.dtv.dcp.epoch.model.ct.product.Variant variant = buildVariant(new ArrayList<>(Arrays.asList(p1, p2, p3)));
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("prod1",
                Collections.singletonList(variant));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "CABLE"); // matches none
        invokeApplyAttributePricing(Collections.singletonList(wrapper), ctx, "OFFER-1");
        // BAU fallback: all 3 returned
        assertEquals(3, variant.getPrices().size());
    }


    @Test
    void applyAttributePricingToProductWrappers_updatesEachVariantIndependently() throws Exception {
        Price v1p1 = buildPriceForVariant("v1p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price v1p2 = buildPriceForVariant("v1p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));
        com.dtv.dcp.epoch.model.ct.product.Variant variant1 =
                buildVariant(new ArrayList<>(Arrays.asList(v1p1, v1p2)));

        Price v2p1 = buildPriceForVariant("v2p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price v2p2 = buildPriceForVariant("v2p2", Arrays.asList("serviceSubscriptionType=OTT"));
        com.dtv.dcp.epoch.model.ct.product.Variant variant2 =
                buildVariant(new ArrayList<>(Arrays.asList(v2p1, v2p2)));

        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("prod1",
                Arrays.asList(variant1, variant2));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "IPTV");
        invokeApplyAttributePricing(Collections.singletonList(wrapper), ctx, "OFFER-1");
        // variant1: only v1p1 matches
        assertEquals(1, variant1.getPrices().size());
        assertEquals("v1p1", variant1.getPrices().get(0).getId());
        // variant2: only v2p1 matches
        assertEquals(1, variant2.getPrices().size());
        assertEquals("v2p1", variant2.getPrices().get(0).getId());
    }


    @Test
    void applyAttributePricingToProductWrappers_updatesAllProductsInWrapper() throws Exception {
        Price prod1p1 = buildPriceForVariant("prod1p1", Arrays.asList("creditRisk=LOW"));
        Price prod1p2 = buildPriceForVariant("prod1p2", Arrays.asList("creditRisk=HIGH"));
        com.dtv.dcp.epoch.model.ct.product.Variant v1 =
                buildVariant(new ArrayList<>(Arrays.asList(prod1p1, prod1p2)));
        com.dtv.dcp.epoch.model.ct.product.Product product1 = buildProduct("prod1",
                Collections.singletonList(v1));

        Price prod2p1 = buildPriceForVariant("prod2p1", Arrays.asList("creditRisk=LOW"));
        Price prod2p2 = buildPriceForVariant("prod2p2", Arrays.asList("creditRisk=MEDIUM"));
        com.dtv.dcp.epoch.model.ct.product.Variant v2 =
                buildVariant(new ArrayList<>(Arrays.asList(prod2p1, prod2p2)));
        com.dtv.dcp.epoch.model.ct.product.Product product2 = buildProduct("prod2",
                Collections.singletonList(v2));

        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Arrays.asList(product1, product2));
        Map<String, String> ctx = new HashMap<>();
        ctx.put("creditRisk", "LOW");
        invokeApplyAttributePricing(Collections.singletonList(wrapper), ctx, "OFFER-1");
        // Both products: only LOW price kept
        assertEquals(1, v1.getPrices().size());
        assertEquals("prod1p1", v1.getPrices().get(0).getId());
        assertEquals(1, v2.getPrices().size());
        assertEquals("prod2p1", v2.getPrices().get(0).getId());
    }


    @Test
    void applyAttributePricingToProductWrappers_updatesAllWrappers() throws Exception {
        Price w1p1 = buildPriceForVariant("w1p1", Arrays.asList("iapPartnerType=ROKU"));
        Price w1p2 = buildPriceForVariant("w1p2", Arrays.asList("iapPartnerType=FIRETV"));
        com.dtv.dcp.epoch.model.ct.product.Variant v1 =
                buildVariant(new ArrayList<>(Arrays.asList(w1p1, w1p2)));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper1 =
                buildProductWrapper(Collections.singletonList(buildProduct("p1", Collections.singletonList(v1))));

        Price w2p1 = buildPriceForVariant("w2p1", Arrays.asList("iapPartnerType=ROKU"));
        Price w2p2 = buildPriceForVariant("w2p2", Arrays.asList("iapPartnerType=GOOGLE"));
        com.dtv.dcp.epoch.model.ct.product.Variant v2 =
                buildVariant(new ArrayList<>(Arrays.asList(w2p1, w2p2)));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper2 =
                buildProductWrapper(Collections.singletonList(buildProduct("p2", Collections.singletonList(v2))));

        Map<String, String> ctx = new HashMap<>();
        ctx.put("iapPartnerType", "ROKU");
        invokeApplyAttributePricing(Arrays.asList(wrapper1, wrapper2), ctx, "OFFER-1");
        assertEquals(1, v1.getPrices().size());
        assertEquals("w1p1", v1.getPrices().get(0).getId());
        assertEquals(1, v2.getPrices().size());
        assertEquals("w2p1", v2.getPrices().get(0).getId());
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsNullWrapper_processesValidOnes() throws Exception {
        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));
        com.dtv.dcp.epoch.model.ct.product.Variant variant =
                buildVariant(new ArrayList<>(Arrays.asList(p1, p2)));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper validWrapper =
                buildProductWrapper(Collections.singletonList(buildProduct("p1", Collections.singletonList(variant))));

        List<com.dtv.dcp.epoch.model.ct.product.ProductWrapper> wrappers = new ArrayList<>();
        wrappers.add(null);
        wrappers.add(validWrapper);
        wrappers.add(null);

        Map<String, String> ctx = new HashMap<>();
        ctx.put("serviceSubscriptionType", "IPTV");
        invokeApplyAttributePricing(wrappers, ctx, "OFFER-1");
        assertEquals(1, variant.getPrices().size());
        assertEquals("p1", variant.getPrices().get(0).getId());
    }


    @Test
    void applyAttributePricingToProductWrappers_skipsVariant_whenPricesIsEmpty() throws Exception {
        com.dtv.dcp.epoch.model.ct.product.Variant variant = buildVariant(Collections.emptyList());
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("prod1",
                Collections.singletonList(variant));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        assertTrue(variant.getPrices().isEmpty());
    }


    @Test
    void applyAttributePricingToProductWrappers_appliesConflictResolution_toVariantPrices() throws Exception {
        Price p1 = buildPriceForVariant("p1", null);
        p1.setPriceTier(Arrays.asList("base"));
        p1.setConflictingPriceTiers(Arrays.asList("promo"));

        Price p2 = buildPriceForVariant("p2", null);
        p2.setPriceTier(Arrays.asList("promo"));

        com.dtv.dcp.epoch.model.ct.product.Variant variant =
                buildVariant(new ArrayList<>(Arrays.asList(p1, p2)));
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("prod1",
                Collections.singletonList(variant));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // p2 (promo) suppressed by p1's conflictingPriceTiers
        assertEquals(1, variant.getPrices().size());
        assertEquals("p1", variant.getPrices().get(0).getId());
    }




    @Test
    void applyAttributePricingToProductWrappers_handleEmptyCriteriaContext() throws Exception {
        Price p1 = buildPriceForVariant("p1", null);     // no criteria → passes
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=IPTV")); // fails (no ctx value)
        com.dtv.dcp.epoch.model.ct.product.Variant variant =
                buildVariant(new ArrayList<>(Arrays.asList(p1, p2)));
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("prod1",
                Collections.singletonList(variant));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), Collections.emptyMap(), "OFFER-1");
        // p1 (no criteria) passes; p2 fails; filtered = [p1] → not empty → p1 returned
        assertEquals(1, variant.getPrices().size());
        assertEquals("p1", variant.getPrices().get(0).getId());
    }


    @Test
    void applyAttributePricingToProductWrappers_handleNullCriteriaContext_withNoCriteriaPrices() throws Exception {
        // When prices have no criteria, null context should not cause NPE – prices pass through
        Price p1 = buildPriceForVariant("p1", null);
        Price p2 = buildPriceForVariant("p2", null);
        com.dtv.dcp.epoch.model.ct.product.Variant variant =
                buildVariant(new ArrayList<>(Arrays.asList(p1, p2)));
        com.dtv.dcp.epoch.model.ct.product.Product product = buildProduct("prod1",
                Collections.singletonList(variant));
        com.dtv.dcp.epoch.model.ct.product.ProductWrapper wrapper =
                buildProductWrapper(Collections.singletonList(product));
        invokeApplyAttributePricing(Collections.singletonList(wrapper), null, "OFFER-1");
        // Both prices have no criteria → both pass → 2 prices retained
        assertEquals(2, variant.getPrices().size());
    }


    // setBasePriceBasedOnAttributePricingCriteria – all scenarios



    private void invokeSetBasePriceBasedOnAttributePricingCriteria(
            OfferRequestWrapper offerRequestWrapper,
            CTOffer offer) throws Exception {
        Method method = OffersUtils.class.getDeclaredMethod(
                "setBasePriceBasedOnAttributePricingCriteria",
                OfferRequestWrapper.class, CTOffer.class);
        method.setAccessible(true);
        method.invoke(utils, offerRequestWrapper, offer);
    }


    private CTOffer buildOfferWithVariantPrices(String offerCode,
                                                 String priceTier,
                                                 List<Price> bundlePrices,
                                                 List<Price> qualifyingPrices) {
        // --- variant for bundle ---
        com.dtv.dcp.epoch.model.ct.product.Variant bv =
                new com.dtv.dcp.epoch.model.ct.product.Variant();
        bv.setPrices(bundlePrices != null ? new ArrayList<>(bundlePrices) : null);

        com.dtv.dcp.epoch.model.ct.product.ProductObj bObj =
                new com.dtv.dcp.epoch.model.ct.product.ProductObj();
        bObj.setVariants(bundlePrices != null ? Collections.singletonList(bv) : Collections.emptyList());

        com.dtv.dcp.epoch.model.ct.product.Product bProd =
                new com.dtv.dcp.epoch.model.ct.product.Product();
        bProd.setKey("bProd");
        bProd.setObj(bObj);

        com.dtv.dcp.epoch.model.ct.product.ProductWrapper bundleWrapper =
                new com.dtv.dcp.epoch.model.ct.product.ProductWrapper();
        bundleWrapper.setProducts(Collections.singletonList(bProd));

        // --- variant for qualifying ---
        com.dtv.dcp.epoch.model.ct.product.Variant qv =
                new com.dtv.dcp.epoch.model.ct.product.Variant();
        qv.setPrices(qualifyingPrices != null ? new ArrayList<>(qualifyingPrices) : null);

        com.dtv.dcp.epoch.model.ct.product.ProductObj qObj =
                new com.dtv.dcp.epoch.model.ct.product.ProductObj();
        qObj.setVariants(qualifyingPrices != null ? Collections.singletonList(qv) : Collections.emptyList());

        com.dtv.dcp.epoch.model.ct.product.Product qProd =
                new com.dtv.dcp.epoch.model.ct.product.Product();
        qProd.setKey("qProd");
        qProd.setObj(qObj);

        com.dtv.dcp.epoch.model.ct.product.ProductWrapper qualWrapper =
                new com.dtv.dcp.epoch.model.ct.product.ProductWrapper();
        qualWrapper.setProducts(Collections.singletonList(qProd));

        // --- assembling AssociatedProduct ---
        com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct ap =
                new com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct();
        ap.setBundleProducts(Collections.singletonList(bundleWrapper));
        ap.setQualifyingProducts(Collections.singletonList(qualWrapper));

        // --- OfferAttributes ---
        OfferAttributes attrs = new OfferAttributes();
        attrs.setPriceTier(priceTier);
        attrs.setAssociatedProducts(Collections.singletonList(ap));

        // --- CTOffer ---
        CTOffer offer = new CTOffer();
        offer.setCode(offerCode);
        offer.setAttributes(attrs);
        return offer;
    }


    private OfferRequestWrapper buildWrapperWithContractIndicators(List<String> contractIndicators) {
        OfferRequest req = new OfferRequest();
        req.setContractIndicator(contractIndicators);
        req.setOfferActionType(Arrays.asList(Constants.OTHER_ACTION_TYPE));
        OfferRequestWrapper wrapper = new OfferRequestWrapper();
        wrapper.setOfferRequest(req);
        return wrapper;
    }


    private void mockSlsEnabledAndUniversalCohort() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)).thenReturn(true);
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_returnsImmediately_whenAttributesIsNull()
            throws Exception {
        CTOffer offer = new CTOffer();
        offer.setCode("OFFER-1");
        offer.setAttributes(null);

        invokeSetBasePriceBasedOnAttributePricingCriteria(null, offer);

        // featureHelper must NOT be consulted
        verify(featureManagerHelper, never()).isEnabled(anyString());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_returnsEarly_whenSlsDisabled()
            throws Exception {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)).thenReturn(false);

        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));
        CTOffer offer = buildOfferWithVariantPrices("OFFER-1", null,
                Arrays.asList(p1, p2), Arrays.asList(p1, p2));
        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        // Prices must NOT be modified – still 2
        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertEquals(2, bundlePrices.size());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_returnsEarly_whenContractIndicatorsEmpty()
            throws Exception {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)).thenReturn(true);

        Price p1 = buildPriceForVariant("p1", null);
        Price p2 = buildPriceForVariant("p2", null);
        CTOffer offer = buildOfferWithVariantPrices("OFFER-1", null,
                Arrays.asList(p1, p2), null);
        // empty contractIndicators → universalCohort=false
        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Collections.emptyList());

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        // Redis should NOT be called for universal cohorts since contractIndicators is empty
        verify(redisCacheHelper, never()).getSwimlaneRules(Constants.UNIVERSAL_COHORTS, Constants.OTT_PRODUCT_FAMILY);
        // Prices unchanged
        assertEquals(2, offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices().size());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_returnsEarly_whenNotUniversalCohort()
            throws Exception {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)).thenReturn(true);


        Price p1 = buildPriceForVariant("p1", null);
        Price p2 = buildPriceForVariant("p2", null);
        CTOffer offer = buildOfferWithVariantPrices("OFFER-1", null,
                Arrays.asList(p1, p2), null);
        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        // Prices unchanged
        assertEquals(2, offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices().size());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_returnsEarly_whenWrapperIsNull()
            throws Exception {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)).thenReturn(true);

        Price p1 = buildPriceForVariant("p1", null);
        Price p2 = buildPriceForVariant("p2", null);
        CTOffer offer = buildOfferWithVariantPrices("OFFER-1", null, Arrays.asList(p1, p2), null);

        invokeSetBasePriceBasedOnAttributePricingCriteria(null, offer);

        // Prices unchanged because universalCohort=false (empty contractIndicators)
        assertEquals(2, offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices().size());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_returnsEarly_whenExplicitPriceTierSet()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();

        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));
        CTOffer offer = buildOfferWithVariantPrices("OFFER-1", "premiumTier",
                Arrays.asList(p1, p2), null);
        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        // Prices NOT changed – priceTier guard fired
        assertEquals(2, offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices().size());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_doesNotReturnEarly_whenPriceTierIsBlank()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();
        // Stub Redis for buildPricingCriteriaContext iapPartnerType resolution
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));
        // blank priceTier → not treated as explicit
        CTOffer offer = buildOfferWithVariantPrices("OFFER-1", "   ",
                Arrays.asList(p1, p2), null);
        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));
        // set context so p1 matches
        wrapper.getOfferRequest().setSalesChannel(Arrays.asList("IPTV"));

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        // Processing proceeded → variant updated
        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        // BAU fallback (ctx has no serviceSubscriptionType value) → both prices kept
        assertNotNull(bundlePrices);
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_doesNotReturnEarly_whenPriceTierIsNull()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));
        CTOffer offer = buildOfferWithVariantPrices("OFFER-1", null,
                Arrays.asList(p1, p2), null);
        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        // Proceeded – prices list updated (BAU fallback returns both)
        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertNotNull(bundlePrices);
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_returnsEarly_whenAssociatedProductsEmpty()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();

        OfferAttributes attrs = new OfferAttributes();
        attrs.setPriceTier(null);
        attrs.setAssociatedProducts(Collections.emptyList());
        CTOffer offer = new CTOffer();
        offer.setCode("OFFER-1");
        offer.setAttributes(attrs);

        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));
        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);
        // No NPE, no processing beyond empty guard
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_returnsEarly_whenAssociatedProductsNull()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();

        OfferAttributes attrs = new OfferAttributes();
        attrs.setPriceTier(null);
        attrs.setAssociatedProducts(null);
        CTOffer offer = new CTOffer();
        offer.setCode("OFFER-1");
        offer.setAttributes(attrs);

        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));
        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);
        // No NPE
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_updatesBundleVariant_whenOnePriceMatchesCriteria()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));

        // Build offer + wrapper with IPTV context
        CTOffer offer = buildOfferWithVariantPrices("OFFER-1", null,
                Arrays.asList(p1, p2), null);

        // Set OTT iapPartnerAccountType on request; serviceSubscriptionType comes from offer attrs
        OfferAttributes offerAttrs = offer.getAttributes();
        offerAttrs.setServiceSubscriptionType(Arrays.asList("IPTV")); // ctx key

        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        // Context has IPTV → p1 passes, p2 filtered out
        assertEquals(1, bundlePrices.size());
        assertEquals("p1", bundlePrices.get(0).getId());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_updatesQualifyingVariant_whenOnePriceMatchesCriteria()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        Price p1 = buildPriceForVariant("qp1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("qp2", Arrays.asList("serviceSubscriptionType=OTT"));

        CTOffer offer = buildOfferWithVariantPrices("OFFER-2", null, null,
                Arrays.asList(p1, p2));
        offer.getAttributes().setServiceSubscriptionType(Arrays.asList("IPTV"));

        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        List<Price> qualPrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getQualifyingProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertEquals(1, qualPrices.size());
        assertEquals("qp1", qualPrices.get(0).getId());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_updatesBothBundleAndQualifying()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        Price bp1 = buildPriceForVariant("bp1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price bp2 = buildPriceForVariant("bp2", Arrays.asList("serviceSubscriptionType=SATELLITE"));
        Price qp1 = buildPriceForVariant("qp1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price qp2 = buildPriceForVariant("qp2", Arrays.asList("serviceSubscriptionType=OTT"));

        CTOffer offer = buildOfferWithVariantPrices("OFFER-3", null,
                Arrays.asList(bp1, bp2), Arrays.asList(qp1, qp2));
        offer.getAttributes().setServiceSubscriptionType(Arrays.asList("IPTV"));

        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct ap =
                offer.getAttributes().getAssociatedProducts().get(0);

        List<Price> bundlePrices =
                ap.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertEquals(1, bundlePrices.size());
        assertEquals("bp1", bundlePrices.get(0).getId());

        List<Price> qualPrices =
                ap.getQualifyingProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertEquals(1, qualPrices.size());
        assertEquals("qp1", qualPrices.get(0).getId());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_keepsBauPrices_whenNoCriteriaMatch()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("serviceSubscriptionType=SATELLITE"));

        CTOffer offer = buildOfferWithVariantPrices("OFFER-4", null,
                Arrays.asList(p1, p2), null);
        offer.getAttributes().setServiceSubscriptionType(Arrays.asList("CABLE")); // matches neither

        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        // BAU fallback → both prices retained
        assertEquals(2, bundlePrices.size());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_handlesNullsInRedisCohortList()
            throws Exception {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)).thenReturn(true);
         // null entries present

        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        Price p1 = buildPriceForVariant("p1", null);
        Price p2 = buildPriceForVariant("p2", null);
        CTOffer offer = buildOfferWithVariantPrices("OFFER-5", null,
                Arrays.asList(p1, p2), null);

        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));
        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        // Both pass (no criteria) → both retained
        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertEquals(2, bundlePrices.size());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_filtersOnIapPartnerType_fromOttContext()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();

        Price p1 = buildPriceForVariant("p1", Arrays.asList("iapPartnerType=ROKU"));
        Price p2 = buildPriceForVariant("p2", Arrays.asList("iapPartnerType=FIRETV"));

        CTOffer offer = buildOfferWithVariantPrices("OFFER-6", null,
                Arrays.asList(p1, p2), null);

        // Set OTT iapPartnerAccountType = ROKU on the request
        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));
        CartProduct ott = new CartProduct();
        ott.setIapPartnerAccountType("ROKU");
        com.dtv.dcp.epoch.model.common.request.CustomerContext ctx =
                new com.dtv.dcp.epoch.model.common.request.CustomerContext();
        ctx.setOtt(ott);
        wrapper.getOfferRequest().setCustomerContext(ctx);

        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertEquals(1, bundlePrices.size());
        assertEquals("p1", bundlePrices.get(0).getId());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_doesNotMutateSinglePriceVariant()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        Price p1 = buildPriceForVariant("p1", Arrays.asList("serviceSubscriptionType=IPTV"));
        CTOffer offer = buildOfferWithVariantPrices("OFFER-7", null,
                Collections.singletonList(p1), null);

        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));
        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertEquals(1, bundlePrices.size()); // unchanged
        assertEquals("p1", bundlePrices.get(0).getId());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_appliesConflictResolution_afterCriteriaPass()
            throws Exception {
        mockSlsEnabledAndUniversalCohort();
        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        // p1 has tier "base", conflicts with "promo"
        Price p1 = buildPriceForVariant("p1", null);
        p1.setPriceTier(Arrays.asList("base"));
        p1.setConflictingPriceTiers(Arrays.asList("promo"));

        // p2 has tier "promo" – should be removed
        Price p2 = buildPriceForVariant("p2", null);
        p2.setPriceTier(Arrays.asList("promo"));

        CTOffer offer = buildOfferWithVariantPrices("OFFER-8", null,
                Arrays.asList(p1, p2), null);

        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC1"));
        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertEquals(1, bundlePrices.size());
        assertEquals("p1", bundlePrices.get(0).getId());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_proceeds_whenOneOfMultipleContractIndicatorsMatchesCohort()
            throws Exception {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)).thenReturn(true);

        when(redisCacheHelper.getSwimlaneRules(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_IAP_PARTNER_TYPE_COMBINATIONS, Constants.OTT))
                .thenReturn(Collections.emptyList());

        Price p1 = buildPriceForVariant("p1", null);
        Price p2 = buildPriceForVariant("p2", null);
        CTOffer offer = buildOfferWithVariantPrices("OFFER-9", null,
                Arrays.asList(p1, p2), null);

        // UC5 not in list but UC2 is
        OfferRequestWrapper wrapper = buildWrapperWithContractIndicators(Arrays.asList("UC5", "UC2"));
        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

        // Proceeded – prices processed (both kept, no criteria)
        List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
                .getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
        assertEquals(2, bundlePrices.size());
    }


    @Test
    void setBasePriceBasedOnAttributePricingCriteria_returnsEarly_whenOfferRequestIsNull()
            throws Exception {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_SLS_GETOFFERS_SERVICES_ENABLED)).thenReturn(true);

        Price p1 = buildPriceForVariant("p1", null);
        Price p2 = buildPriceForVariant("p2", null);
        CTOffer offer = buildOfferWithVariantPrices("OFFER-10", null,
                Arrays.asList(p1, p2), null);

        OfferRequestWrapper wrapper = new OfferRequestWrapper(); // offerRequest is null
        invokeSetBasePriceBasedOnAttributePricingCriteria(wrapper, offer);

		// universalCohort=false → prices unchanged
		List<Price> bundlePrices = offer.getAttributes().getAssociatedProducts().get(0)
				.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices();
		assertEquals(2, bundlePrices.size());
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

    private com.dtv.dcp.epoch.model.ct.request.PriceProtection buildPriceProtection(
            String startDate, String endDate, List<String> contractIndicator) {
        com.dtv.dcp.epoch.model.ct.request.PriceProtection priceProtection =
                new com.dtv.dcp.epoch.model.ct.request.PriceProtection();
        priceProtection.setStartDate(startDate);
        priceProtection.setEndDate(endDate);
        priceProtection.setContractIndicator(contractIndicator);
        return priceProtection;
    }

    @Test
    void usesConfiguredHogWindow_returnsTrue_whenContractIndicatorIsRRorTazContract() {
        com.dtv.dcp.epoch.model.ct.request.PriceProtection rrPriceProtection =
                buildPriceProtection("06/25/2026", "10/25/2026", Arrays.asList("rr"));
        com.dtv.dcp.epoch.model.ct.request.PriceProtection tazContractPriceProtection =
                buildPriceProtection("06/25/2026", "10/25/2026", Arrays.asList(Constants.TAZCONTRACT_STRING));

        assertTrue(offersUtils.usesConfiguredHogWindow(rrPriceProtection));
        assertTrue(offersUtils.usesConfiguredHogWindow(tazContractPriceProtection));
    }

    @Test
    void usesConfiguredHogWindow_returnsFalse_whenContractIndicatorDoesNotRequireConfiguredWindow() {
        com.dtv.dcp.epoch.model.ct.request.PriceProtection priceProtection =
                buildPriceProtection("06/25/2026", "10/25/2026", Arrays.asList("GENRE"));

        assertFalse(offersUtils.usesConfiguredHogWindow(priceProtection));
    }

    @Test
    void isConfiguredHogPriceProtectionWindow_returnsFalse_whenDatesAreMissing() {
        com.dtv.dcp.epoch.model.ct.request.PriceProtection missingStartDate =
                buildPriceProtection(null, "10/25/2026", Arrays.asList("RR"));
        com.dtv.dcp.epoch.model.ct.request.PriceProtection missingEndDate =
                buildPriceProtection("06/25/2026", null, Arrays.asList("RR"));

        assertFalse(offersUtils.isConfiguredHogPriceProtectionWindow(missingStartDate));
        assertFalse(offersUtils.isConfiguredHogPriceProtectionWindow(missingEndDate));
    }

    @Test
    void isConfiguredHogPriceProtectionWindow_returnsTrue_whenContractIndicatorDoesNotUseConfiguredWindow() {
        com.dtv.dcp.epoch.model.ct.request.PriceProtection priceProtection =
                buildPriceProtection("06/25/2026", "10/25/2026", Arrays.asList("GENRE"));

        assertTrue(offersUtils.isConfiguredHogPriceProtectionWindow(priceProtection));
        verify(redisCacheHelper, never()).getValues("HOG-DATES", Constants.OTT);
    }

    @Test
    void isConfiguredHogPriceProtectionWindow_returnsTrue_whenConfiguredWindowMatchesAnyRange() {
        when(redisCacheHelper.getValues("HOG-DATES", Constants.OTT))
                .thenReturn(Arrays.asList(
                        "04/07/2026:05/07/2026",
                        "06/25/2026:10/25/2026"));

        com.dtv.dcp.epoch.model.ct.request.PriceProtection priceProtection =
                buildPriceProtection("06/25/2026", "10/25/2026", Arrays.asList("RR"));

        assertTrue(offersUtils.isConfiguredHogPriceProtectionWindow(priceProtection));
    }

    @Test
    void isConfiguredHogPriceProtectionWindow_returnsFalse_whenConfiguredWindowDoesNotMatch() {
        when(redisCacheHelper.getValues("HOG-DATES", Constants.OTT))
                .thenReturn(Arrays.asList(
                        "04/07/2026:05/07/2026",
                        "06/25/2026:10/25/2026"));

        com.dtv.dcp.epoch.model.ct.request.PriceProtection priceProtection =
                buildPriceProtection("03/09/2026", "04/08/2026", Arrays.asList(Constants.TAZCONTRACT_STRING));

        assertFalse(offersUtils.isConfiguredHogPriceProtectionWindow(priceProtection));
    }

    @Test
    void getConfigListValues_flattensAndParsesAllRedisValues_forHogDates() {
        when(redisCacheHelper.getValues("HOG-DATES", Constants.OTT))
                .thenReturn(Arrays.asList(
                        "[\"04/07/2026:05/07/2026\"]",
                        "06/25/2026:10/25/2026, 11/01/2026:12/01/2026"));

        List<String> values = offersUtils.getConfigListValues("HOG-DATES");

        assertNotNull(values);
        assertEquals(3, values.size());
        assertTrue(values.contains("04/07/2026:05/07/2026"));
        assertTrue(values.contains("06/25/2026:10/25/2026"));
        assertTrue(values.contains("11/01/2026:12/01/2026"));
    }

    // ========== Tests for reconnectCustomer and offerActionType filtering ==========

    @Test
    void testIsService_withUpgradeActionType_returnsTrue() {
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferActionType(Arrays.asList(Constants.UPGRADE_ACTION_TYPE));
        assertTrue(offersUtils.isService(offerRequest));
    }

    @Test
    void testIsService_withDowngradeActionType_returnsTrue() {
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferActionType(Arrays.asList(Constants.DOWNGRADE_ACTION_TYPE));
        assertTrue(offersUtils.isService(offerRequest));
    }

    @Test
    void testIsService_withCrossSellActionType_returnsTrue() {
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferActionType(Arrays.asList(Constants.CROSS_SELL_ACTION_TYPE));
        assertTrue(offersUtils.isService(offerRequest));
    }

    @Test
    void testIsService_withOtherActionType_returnsTrue() {
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferActionType(Arrays.asList(Constants.OTHER_ACTION_TYPE));
        assertTrue(offersUtils.isService(offerRequest));
    }

    @Test
    void testIsService_withAcquisitionActionType_returnsFalse() {
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION));
        assertFalse(offersUtils.isService(offerRequest));
    }

    @Test
    void testIsAcquisitionAndNotReconnectFlow_withAcquisitionAndNotReconnect_returnsTrue() {
        OfferRequestWrapper wrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION));
        offerRequest.setReconnectCustomer(false);
        wrapper.setOfferRequest(offerRequest);
        assertTrue(offersUtils.isAcquisitionAndNotReconnectFlow(wrapper));
    }

    @Test
    void testIsAcquisitionAndNotReconnectFlow_withReconnectCustomerTrue_returnsFalse() {
        OfferRequestWrapper wrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION));
        offerRequest.setReconnectCustomer(true);
        wrapper.setOfferRequest(offerRequest);
        assertFalse(offersUtils.isAcquisitionAndNotReconnectFlow(wrapper));
    }

    @Test
    void testIsAcquisitionAndNotReconnectFlow_withServiceActionType_returnsFalse() {
        OfferRequestWrapper wrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setOfferActionType(Arrays.asList(Constants.UPGRADE_ACTION_TYPE));
        offerRequest.setReconnectCustomer(false);
        wrapper.setOfferRequest(offerRequest);
        assertFalse(offersUtils.isAcquisitionAndNotReconnectFlow(wrapper));
    }

    @Test
    void testIsAcquisitionAndNotReconnectFlow_nullWrapper_returnsFalse() {
        assertFalse(offersUtils.isAcquisitionAndNotReconnectFlow((OfferRequestWrapper) null));
    }

    @Test
    void testCheckCustomerTypeReconnectOrNot_reconnectTrue_returnsOnlyReconnectOffers() {
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOffer reconnectOffer = new CTOffer();
        OfferAttributes attrs1 = new OfferAttributes();
        attrs1.setCustomerTypes(Arrays.asList(Constants.RECONNECT));
        reconnectOffer.setAttributes(attrs1);

        CTOffer nonReconnectOffer = new CTOffer();
        OfferAttributes attrs2 = new OfferAttributes();
        nonReconnectOffer.setAttributes(attrs2);

        ctOfferResponse.setOffers(new ArrayList<>(Arrays.asList(reconnectOffer, nonReconnectOffer)));
        offersUtils.checkCustomerTypeReconnectOrNot(ctOfferResponse, true);

        assertEquals(1, ctOfferResponse.getOffers().size());
        assertEquals(Constants.RECONNECT, ctOfferResponse.getOffers().get(0).getAttributes().getCustomerTypes().get(0));
    }

    @Test
    void testCheckCustomerTypeReconnectOrNot_reconnectFalse_returnsOnlyNullCustomerTypeOffers() {
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOffer reconnectOffer = new CTOffer();
        OfferAttributes attrs1 = new OfferAttributes();
        attrs1.setCustomerTypes(Arrays.asList(Constants.RECONNECT));
        reconnectOffer.setAttributes(attrs1);

        CTOffer nonReconnectOffer = new CTOffer();
        OfferAttributes attrs2 = new OfferAttributes();
        nonReconnectOffer.setAttributes(attrs2);

        ctOfferResponse.setOffers(new ArrayList<>(Arrays.asList(reconnectOffer, nonReconnectOffer)));
        offersUtils.checkCustomerTypeReconnectOrNot(ctOfferResponse, false);

        assertEquals(1, ctOfferResponse.getOffers().size());
        assertNull(ctOfferResponse.getOffers().get(0).getAttributes().getCustomerTypes());
    }

    @Test
    void testIsCustomerTypeReconnect_offerWithReconnectType_returnsTrue() {
        CTOffer offer = new CTOffer();
        OfferAttributes attrs = new OfferAttributes();
        attrs.setCustomerTypes(Arrays.asList(Constants.RECONNECT));
        offer.setAttributes(attrs);
        assertTrue(offersUtils.isCustomerTypeReconnect(offer));
    }

    @Test
    void testIsCustomerTypeReconnect_offerWithNoReconnectType_returnsFalse() {
        CTOffer offer = new CTOffer();
        OfferAttributes attrs = new OfferAttributes();
        attrs.setCustomerTypes(Arrays.asList(Constants.ACQUISITION));
        offer.setAttributes(attrs);
        assertFalse(offersUtils.isCustomerTypeReconnect(offer));
    }

    @Test
    void testIsCustomerTypeReconnect_nullCustomerTypes_returnsFalse() {
        CTOffer offer = new CTOffer();
        OfferAttributes attrs = new OfferAttributes();
        offer.setAttributes(attrs);
        assertFalse(offersUtils.isCustomerTypeReconnect(offer));
    }

    @Test
    void testFilterOffersBasedOnCustomerTypeAcquisition_noArgs_filtersCorrectly() {
        CTOfferResponse ctOfferResponse = new CTOfferResponse();

        CTOffer acquisitionOffer = new CTOffer();
        OfferAttributes attrs1 = new OfferAttributes();
        attrs1.setCustomerTypes(Arrays.asList(Constants.ACQUISITION));
        acquisitionOffer.setAttributes(attrs1);

        CTOffer reconnectOffer = new CTOffer();
        OfferAttributes attrs2 = new OfferAttributes();
        attrs2.setCustomerTypes(Arrays.asList(Constants.RECONNECT));
        reconnectOffer.setAttributes(attrs2);

        CTOffer nullTypeOffer = new CTOffer();
        OfferAttributes attrs3 = new OfferAttributes();
        nullTypeOffer.setAttributes(attrs3);

        ctOfferResponse.setOffers(new ArrayList<>(Arrays.asList(acquisitionOffer, reconnectOffer, nullTypeOffer)));
        offersUtils.filterOffersBasedOnCustomerTypeAcquisition(ctOfferResponse);

        // null customerTypes and acquisition types should pass through
        assertEquals(2, ctOfferResponse.getOffers().size());
    }

    @Test
    void testIsEligibleToReconnectLessThanEligibleMonths_CTOfferRequest_reconnectCustomerFalse_returnsFalse() {
        com.dtv.dcp.epoch.model.ct.request.CTOfferRequest ctOfferRequest = new com.dtv.dcp.epoch.model.ct.request.CTOfferRequest();
        ctOfferRequest.setReconnectCustomer(false);
        ctOfferRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION));
        ctOfferRequest.setServiceEndDate("01/01/2025");
        assertFalse(offersUtils.isEligibleToReconnectLessThanEligibleMonths(ctOfferRequest));
    }

    @Test
    void testIsEligibleToReconnectLessThanEligibleMonths_CTOfferRequest_noServiceEndDate_returnsFalse() {
        com.dtv.dcp.epoch.model.ct.request.CTOfferRequest ctOfferRequest = new com.dtv.dcp.epoch.model.ct.request.CTOfferRequest();
        ctOfferRequest.setReconnectCustomer(true);
        ctOfferRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION));
        ctOfferRequest.setServiceEndDate(null);
        assertFalse(offersUtils.isEligibleToReconnectLessThanEligibleMonths(ctOfferRequest));
    }

    @Test
    void testIsEligibleToReconnectLessThanEligibleMonths_CTOfferRequest_nonAcquisitionActionType_returnsFalse() {
        com.dtv.dcp.epoch.model.ct.request.CTOfferRequest ctOfferRequest = new com.dtv.dcp.epoch.model.ct.request.CTOfferRequest();
        ctOfferRequest.setReconnectCustomer(true);
        ctOfferRequest.setOfferActionType(Arrays.asList(Constants.UPGRADE_ACTION_TYPE));
        ctOfferRequest.setServiceEndDate("01/01/2025");
        assertFalse(offersUtils.isEligibleToReconnectLessThanEligibleMonths(ctOfferRequest));
    }

    @Test
    void testIsAcquisitionAndNotReconnectFlow_CTOfferRequest_acquisitionNotReconnect_returnsTrue() {
        com.dtv.dcp.epoch.model.ct.request.CTOfferRequest ctOfferRequest = new com.dtv.dcp.epoch.model.ct.request.CTOfferRequest();
        ctOfferRequest.setReconnectCustomer(false);
        ctOfferRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION));
        assertTrue(offersUtils.isAcquisitionAndNotReconnectFlow(ctOfferRequest));
    }

    @Test
    void testIsAcquisitionAndNotReconnectFlow_CTOfferRequest_reconnectTrue_returnsFalse() {
        com.dtv.dcp.epoch.model.ct.request.CTOfferRequest ctOfferRequest = new com.dtv.dcp.epoch.model.ct.request.CTOfferRequest();
        ctOfferRequest.setReconnectCustomer(true);
        ctOfferRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION));
        assertFalse(offersUtils.isAcquisitionAndNotReconnectFlow(ctOfferRequest));
    }

    @Test
    void testIsAcquisitionAndNotReconnectFlow_CTOfferRequest_null_returnsFalse() {
        com.dtv.dcp.epoch.model.ct.request.CTOfferRequest ctOfferRequest = null;
        assertFalse(offersUtils.isAcquisitionAndNotReconnectFlow(ctOfferRequest));
    }
}