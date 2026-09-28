package com.dtv.dcp.epoch.integration.common;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.BuildMiddlewareCTFilters;
import com.dtv.dcp.epoch.integration.CpopBackUpClient;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.CpopIxpClient;
import com.dtv.dcp.epoch.integration.CpopPVTClient;
import com.dtv.dcp.epoch.integration.CpopPrimaryClient;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.ct.burn.CommerceBurnResponse;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequest;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTCouponsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.CTShoppingCartRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.ct.response.CTShoppingCartResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

public class CpopClientTest {
	
    @InjectMocks
    CpopClient cpopClient;

    @Mock
    private FeatureManagerHelper featureManagerHelper;

    @Mock
    CpopBackUpClient cpopBackUpClient;

    @Mock
    CpopPrimaryClient cpopPrimaryClient;
    
    @Mock
	CpopPVTClient cpopPVTClient;
	
    @Mock
	CpopClientHelper cpopClientHelper;

    @Mock
    private CpopIxpClient ixpClient;
    
    @Mock
    private OffersUtils offersUtils;
    
    @Mock
    private BuildMiddlewareCTFilters middlewareFilters;
    
    /**
     * Setup.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(cpopClient, "idpConfigEnv", "local");
    }

    @Test
    public void testGetProductsCpopPVTClient() {
        CTProductRequest ctProductRequest = new CTProductRequest();
        CTProductResponse ctProductResponse = new CTProductResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopBackUpClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopPVTClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        cpopClient.getProducts(ctProductRequest);
    }
    @Test
    public void testGetProductsCpopBackupClient() {
        CTProductRequest ctProductRequest = new CTProductRequest();
        CTProductResponse ctProductResponse = new CTProductResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopBackUpClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopPVTClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        cpopClient.getProducts(ctProductRequest);
    }
    @Test
    public void testGetProductsCpopPrimaryClient() {
        CTProductRequest ctProductRequest = new CTProductRequest();
        CTProductResponse ctProductResponse = new CTProductResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        Mockito.when(cpopPrimaryClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopBackUpClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopPVTClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        cpopClient.getProducts(ctProductRequest);
    }    
    @Test
    public void testGetProductsFeatureDisabled() {
        CTProductRequest ctProductRequest = new CTProductRequest();
        CTProductResponse ctProductResponse = new CTProductResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
        Mockito.when(cpopPrimaryClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopBackUpClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopPVTClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        cpopClient.getProducts(ctProductRequest);
    }
    @Test
    public void testGetOffers() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopBackUpClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopPVTClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        List<String> zipCodeList= new ArrayList<String>();
        zipCodeList.add("75082");
        Mockito.when(offersUtils.fetchCTZipcodes()).thenReturn(zipCodeList);
        
        cpopClient.getOffers(ctOfferRequest);
    }
    @Test
    public void testGetOffersFeatureDisabled() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
        Mockito.when(cpopPrimaryClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopBackUpClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopPVTClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        cpopClient.getOffers(ctOfferRequest);
    }
    @Test
    public void testGetOffersfromCacheCpopPVTClient() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        ctOfferRequest.setOfferActionType(Stream.of(Constants.ACQUISITION_ACTION_TYPE).collect(Collectors.toList()));
        ctOfferRequest.setSalesChannel(Stream.of(Constants.ONLINE).collect(Collectors.toList()));
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopBackUpClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopPVTClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        cpopClient.getOffers(ctOfferRequest);
    }
    @Test
    public void testGetOffersfromCacheCpopBackUpClient() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        ctOfferRequest.setOfferActionType(Stream.of(Constants.ACQUISITION_ACTION_TYPE).collect(Collectors.toList()));
        ctOfferRequest.setSalesChannel(Stream.of(Constants.ONLINE).collect(Collectors.toList()));
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(true);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopBackUpClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopPVTClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        cpopClient.getOffers(ctOfferRequest);
    }    
    @Test
    public void testGetOffersfromCacheCpopPrimaryClient() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        ctOfferRequest.setOfferActionType(Stream.of(Constants.ACQUISITION_ACTION_TYPE).collect(Collectors.toList()));
        ctOfferRequest.setSalesChannel(Stream.of(Constants.ONLINE).collect(Collectors.toList()));
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(cpopPrimaryClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopBackUpClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopPVTClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        cpopClient.getOffers(ctOfferRequest);
    }  
    @Test
    public void testGetOffersFromCTBackUpClient() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopBackUpClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopPVTClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        cpopClient.getOffersFromCT(ctOfferRequest);
    }
    @Test
    public void testGetOffersFromCTPrimaryClient() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        Mockito.when(cpopPrimaryClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopBackUpClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopPVTClient.getOffers(Mockito.anyString(),Mockito.anyString())).thenReturn(ctOfferResponse);
        cpopClient.getOffersFromCT(ctOfferRequest);
    }    

    @Test
    public void testGetBenefitsCpopPVTClient() {
        CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
        CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);        
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        Mockito.when(cpopBackUpClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        Mockito.when(cpopPVTClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        cpopClient.getBenefits(ctBenefitsRequest);
    }
    @Test
    public void testGetBenefitsCpopBackUpClient() {
        CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
        CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);        
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        Mockito.when(cpopBackUpClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        Mockito.when(cpopPVTClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        cpopClient.getBenefits(ctBenefitsRequest);
    }  
    @Test
    public void testGetBenefitsCpopPrimaryClient() {
        CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
        CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);        
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
        Mockito.when(cpopPrimaryClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        Mockito.when(cpopBackUpClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        Mockito.when(cpopPVTClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        cpopClient.getBenefits(ctBenefitsRequest);
    }     
    @Test
    public void testGetBenefitsFeatureDisabled() {
        CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
        CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
        Mockito.when(cpopPrimaryClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        Mockito.when(cpopBackUpClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        Mockito.when(cpopPVTClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        cpopClient.getBenefits(ctBenefitsRequest);
    }
    @Test
    public void testGetCouponsCpopPVTClient() {
    	CTCouponsRequest ctCouponsRequest = new CTCouponsRequest();
    	CTCouponResponse ctCouponResponse = new CTCouponResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);        
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getCoupons(Mockito.any())).thenReturn(ctCouponResponse);
        Mockito.when(cpopBackUpClient.getCoupons(Mockito.any())).thenReturn(ctCouponResponse);
        Mockito.when(cpopPVTClient.getCoupons(Mockito.any())).thenReturn(ctCouponResponse);
        cpopClient.getCoupons(ctCouponsRequest);
    }
    @Test
    public void testGetCouponsCpopBackUpClient() {
    	CTCouponsRequest ctCouponsRequest = new CTCouponsRequest();
    	CTCouponResponse ctCouponResponse = new CTCouponResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);        
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getCoupons(Mockito.any())).thenReturn(ctCouponResponse);
        Mockito.when(cpopBackUpClient.getCoupons(Mockito.any())).thenReturn(ctCouponResponse);
        Mockito.when(cpopPVTClient.getCoupons(Mockito.any())).thenReturn(ctCouponResponse);
        cpopClient.getCoupons(ctCouponsRequest);
    }
    @Test
    public void testGetCouponsCpopPrimaryClient() {
    	CTCouponsRequest ctCouponsRequest = new CTCouponsRequest();
    	CTCouponResponse ctCouponResponse = new CTCouponResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);        
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        Mockito.when(cpopPrimaryClient.getCoupons(Mockito.any())).thenReturn(ctCouponResponse);
        Mockito.when(cpopBackUpClient.getCoupons(Mockito.any())).thenReturn(ctCouponResponse);
        Mockito.when(cpopPVTClient.getCoupons(Mockito.any())).thenReturn(ctCouponResponse);
        cpopClient.getCoupons(ctCouponsRequest);
    }
    @Test
    public void testValidateShoppingCartCpopPVTClient() {
        CTShoppingCartRequest ctShoppingCartRequest = new CTShoppingCartRequest();
        CTShoppingCartResponse ctShoppingCartResponse = new CTShoppingCartResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(cpopPrimaryClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopBackUpClient.validateShoppingCart(Mockito.any(),Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopPVTClient.validateShoppingCart(Mockito.any(),Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        cpopClient.validateShoppingCart(ctShoppingCartRequest, false);
    }
    @Test
    public void testValidateShoppingCartCpopBackUpClient() {
        CTShoppingCartRequest ctShoppingCartRequest = new CTShoppingCartRequest();
        CTShoppingCartResponse ctShoppingCartResponse = new CTShoppingCartResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(true);
        Mockito.when(cpopPrimaryClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopBackUpClient.validateShoppingCart(Mockito.any(),Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopPVTClient.validateShoppingCart(Mockito.any(),Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        cpopClient.validateShoppingCart(ctShoppingCartRequest, false);
    }
    @Test
    public void testValidateShoppingCartCpopPrimaryClient() {
        CTShoppingCartRequest ctShoppingCartRequest = new CTShoppingCartRequest();
        CTShoppingCartResponse ctShoppingCartResponse = new CTShoppingCartResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);        
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        Mockito.when(cpopPrimaryClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopBackUpClient.validateShoppingCart(Mockito.any(),Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopPVTClient.validateShoppingCart(Mockito.any(),Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        cpopClient.validateShoppingCart(ctShoppingCartRequest, false);
    }
    @Test
    public void testValidateShoppingCartFeatureDisabled() {
        CTShoppingCartRequest ctShoppingCartRequest = new CTShoppingCartRequest();
        CTShoppingCartResponse ctShoppingCartResponse = new CTShoppingCartResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
        Mockito.when(cpopPrimaryClient.validateShoppingCart(Mockito.any(),Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopBackUpClient.validateShoppingCart(Mockito.any(),Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopPVTClient.validateShoppingCart(Mockito.any(),Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        cpopClient.validateShoppingCart(ctShoppingCartRequest, false);
    }
    @Test
    public void testBurnQuotaBasedPromotion() {
        OfferBurnRequest offerBurnRequest = new OfferBurnRequest();
        CommerceBurnResponse commerceBurnResponse = new CommerceBurnResponse();
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.when(cpopPrimaryClient.burnQuotaBasedPromotion(Mockito.any())).thenReturn(commerceBurnResponse);
        Mockito.when(cpopBackUpClient.burnQuotaBasedPromotion(Mockito.any())).thenReturn(commerceBurnResponse);
        Mockito.when(cpopPVTClient.burnQuotaBasedPromotion(Mockito.any())).thenReturn(commerceBurnResponse);
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        cpopClient.burnQuotaBasedPromotion(offerBurnRequest);
    }
    @Test
    public void testBurnQuotaBasedPromotionBackupClient() {
        OfferBurnRequest offerBurnRequest = new OfferBurnRequest();
        CommerceBurnResponse commerceBurnResponse = new CommerceBurnResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(true);
        Mockito.when(cpopPrimaryClient.burnQuotaBasedPromotion(Mockito.any())).thenReturn(commerceBurnResponse);
        Mockito.when(cpopBackUpClient.burnQuotaBasedPromotion(Mockito.any())).thenReturn(commerceBurnResponse);
        Mockito.when(cpopPVTClient.burnQuotaBasedPromotion(Mockito.any())).thenReturn(commerceBurnResponse);
        cpopClient.burnQuotaBasedPromotion(offerBurnRequest);
    }
    @Test
    public void testBurnQuotaBasedPromotionPrimaryClient() {
        OfferBurnRequest offerBurnRequest = new OfferBurnRequest();
        CommerceBurnResponse commerceBurnResponse = new CommerceBurnResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        Mockito.when(cpopPrimaryClient.burnQuotaBasedPromotion(Mockito.any())).thenReturn(commerceBurnResponse);
        Mockito.when(cpopBackUpClient.burnQuotaBasedPromotion(Mockito.any())).thenReturn(commerceBurnResponse);
        Mockito.when(cpopPVTClient.burnQuotaBasedPromotion(Mockito.any())).thenReturn(commerceBurnResponse);
        cpopClient.burnQuotaBasedPromotion(offerBurnRequest);
    }
    @Test
    public void testFilterBenefitCodeSuppressOffer(){
        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("CTOfferResponseBenefitCodesToSuppress.json",CTOfferResponse.class);
    	CTOfferRequest ctOfferRequest = new CTOfferRequest();
		List<String> offersList = new ArrayList<String>();
		offersList.add("DEACT001");
		ctOfferRequest.setBenefitsCodesToSuppressOffer(offersList);
		cpopClient.filterBenefitCodeSuppressOffer(ctOfferResponse, ctOfferRequest);
    }
    
    @Test
    public void testFilterEmployeeOffers(){
        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("CTEmployeeVideoAddonOffers.json",CTOfferResponse.class);
    	CTOfferRequest ctOfferRequest = new CTOfferRequest();
    	//ctOfferRequest.setCustomerSegments(Stream.of("Employee").collect(Collectors.toList()));
		List<String> offersList = new ArrayList<String>();
		cpopClient.filterEmployeeOffers(ctOfferResponse, ctOfferRequest);
    }
    
    @Test
    public void testFilterEmployeeOffersWithCustomerSegment(){
        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("CTEmployeeVideoAddonOffers.json",CTOfferResponse.class);
    	CTOfferRequest ctOfferRequest = new CTOfferRequest();
    	ctOfferRequest.setCustomerSegments(Stream.of("Employee").collect(Collectors.toList()));
		List<String> offersList = new ArrayList<String>();
		cpopClient.filterEmployeeOffers(ctOfferResponse, ctOfferRequest);
    }
    
    @Test
    public void testGetProductsByTypeCpopPVTClient() {
        CTProductResponse ctProductResponse = new CTProductResponse();
        Mockito.when(cpopPVTClient.getProductsByType(Mockito.any())).thenReturn(ctProductResponse);
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
        cpopClient.getProductsByType(Constants.SEGMENT);
        assertNotNull(ctProductResponse);
    }
    
    @Test
    public void testGetProductsByTypeCpopBackupClient() {
        CTProductResponse ctProductResponse = new CTProductResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(true);
        Mockito.when(cpopBackUpClient.getProductsByType(Mockito.any())).thenReturn(ctProductResponse);
        cpopClient.getProductsByType(Constants.SEGMENT);
        assertNotNull(ctProductResponse);
    }
    
    @Test
    public void testGetProductsByTypeCpopPrimaryClient() {
        CTProductResponse ctProductResponse = new CTProductResponse();
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        Mockito.when(cpopPrimaryClient.getProductsByType(Mockito.any())).thenReturn(ctProductResponse);
        cpopClient.getProductsByType(Constants.SEGMENT);
        assertNotNull(ctProductResponse);
    }
    
    @Test
    public void testGetOffersAgentIndirectCaching() {
    	CTOfferRequest ctOfferRequest = new CTOfferRequest();
    	CTOfferResponse offers = new CTOfferResponse();
    	ctOfferRequest.setOfferActionType(new ArrayList<>(Arrays.asList(Constants.ACQUISITION)));
    	ctOfferRequest.setSalesChannel(new ArrayList<>(Arrays.asList(Constants.DIRECT_INTEGRATION_PARTNER)));
    	ctOfferRequest.setOfferProductType(new ArrayList<>(Arrays.asList(Constants.VIDEO_DEVICE)));
    	ctOfferRequest.setOfferProductFamily(new ArrayList<>(Arrays.asList(Constants.SATELLITE_PRODUCT_FAMILY)));
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getOffers(anyString(), anyString())).thenReturn(offers);
        offers = cpopClient.getOffers(ctOfferRequest);
        assertNotNull(offers);
    }
    
    @Test
    public void testGetOffersAssistedSalesCaching() {
    	CTOfferRequest ctOfferRequest = new CTOfferRequest();
    	CTOfferResponse offers = new CTOfferResponse();
    	ctOfferRequest.setOfferActionType(new ArrayList<>(Arrays.asList(Constants.ACQUISITION)));
    	ctOfferRequest.setSalesChannel(new ArrayList<>(Arrays.asList(Constants.ASSISTED_SALES)));
    	ctOfferRequest.setOfferProductType(new ArrayList<>(Arrays.asList(Constants.VIDEO_DEVICE)));
    	ctOfferRequest.setOfferProductFamily(new ArrayList<>(Arrays.asList(Constants.SATELLITE_PRODUCT_FAMILY)));
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
        Mockito.when(cpopPrimaryClient.getOffers(anyString(), anyString())).thenReturn(offers);
        offers = cpopClient.getOffers(ctOfferRequest);
        assertNotNull(offers);
    }
  
    @Test
    public void testGetOffersfromCacheRewardZipCode() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        ctOfferRequest.setOfferActionType(Stream.of(Constants.ACQUISITION_ACTION_TYPE).collect(Collectors.toList()));
        ctOfferRequest.setSalesChannel(Stream.of(Constants.DIRECTV_ONLINE).collect(Collectors.toList()));
        ctOfferRequest.setOfferProductType(Stream.of(Constants.REWARD).collect(Collectors.toList()));
        ctOfferRequest.setOfferProductFamily(Stream.of(Constants.OTT_PRODUCT_FAMILY).collect(Collectors.toList()));
        CustomerEligibility customerEligibility = new CustomerEligibility();
        customerEligibility.setZipCode(Stream.of("75082").collect(Collectors.toList()));
        ctOfferRequest.setCustomerEligibility(customerEligibility);

        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(cpopPrimaryClient.getOffers(Mockito.anyString(), Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopBackUpClient.getOffers(Mockito.anyString(), Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopPVTClient.getOffers(Mockito.anyString(), Mockito.anyString())).thenReturn(ctOfferResponse);
        cpopClient.getOffersFromCache(ctOfferRequest);
    }
    @Test
    public void testGetOffersfromCacheRewardDMA() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        ctOfferRequest.setOfferActionType(Stream.of(Constants.ACQUISITION_ACTION_TYPE).collect(Collectors.toList()));
        ctOfferRequest.setSalesChannel(Stream.of(Constants.DIRECTV_ONLINE).collect(Collectors.toList()));
        ctOfferRequest.setOfferProductType(Stream.of(Constants.REWARD).collect(Collectors.toList()));
        ctOfferRequest.setOfferProductFamily(Stream.of(Constants.OTT_PRODUCT_FAMILY).collect(Collectors.toList()));
        CustomerEligibility customerEligibility = new CustomerEligibility();
        customerEligibility.setDma(Stream.of("623").collect(Collectors.toList()));
        ctOfferRequest.setCustomerEligibility(customerEligibility);

        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(cpopPrimaryClient.getOffers(Mockito.anyString(), Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopBackUpClient.getOffers(Mockito.anyString(), Mockito.anyString())).thenReturn(ctOfferResponse);
        Mockito.when(cpopPVTClient.getOffers(Mockito.anyString(), Mockito.anyString())).thenReturn(ctOfferResponse);
        cpopClient.getOffersFromCache(ctOfferRequest);
    }
    @Test
	public void testGetProductsByTypeFromCache() {
		CTProductResponse ctProductResponse = new CTProductResponse();
		Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP))
				.thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
		cpopClient.getProductsByTypeFromCache(Constants.SEGMENT);
		assertNotNull(ctProductResponse);
	}
    @Test
   	public void testGetProductsByTypeFromCache_enabledBackup_true() {
   		CTProductResponse ctProductResponse = new CTProductResponse();
   		Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
   		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP))
   				.thenReturn(true);
   		Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
   		cpopClient.getProductsByTypeFromCache(Constants.SEGMENT);
   		assertNotNull(ctProductResponse);
   	}
    @Test
   	public void testGetProductsByTypeFromCache_PVTEnabled() {
   		CTProductResponse ctProductResponse = new CTProductResponse();
   		Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
   		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP))
   				.thenReturn(false);
   		Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
   		cpopClient.getProductsByTypeFromCache(Constants.SEGMENT);
   		assertNotNull(ctProductResponse);
   	}
    
    @Test
	public void testGetProductsFromCache_PVTEnabled() {
		CTProductResponse ctProductResponse = new CTProductResponse();
		Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(true);
		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP))
				.thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
		Mockito.when(cpopPrimaryClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		Mockito.when(cpopBackUpClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		Mockito.when(cpopPVTClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		cpopClient.getProductsFromCache(new CTProductRequest());
	}
    @Test
	public void testGetProductsFromCache() {
		CTProductResponse ctProductResponse = new CTProductResponse();
		Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP))
				.thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
		Mockito.when(cpopPrimaryClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		Mockito.when(cpopBackUpClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		Mockito.when(cpopPVTClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		cpopClient.getProductsFromCache(new CTProductRequest());
	}
    @Test
	public void testGetProductsFromCache__enabledBackup_true() {
		CTProductResponse ctProductResponse = new CTProductResponse();
		Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP))
				.thenReturn(true);
		Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
		Mockito.when(cpopPrimaryClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		Mockito.when(cpopBackUpClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		Mockito.when(cpopPVTClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		cpopClient.getProductsFromCache(new CTProductRequest());
	}
    
    @Test
	public void testLoadGlobalConfigurations() {
		Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP))
				.thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
		cpopClient.loadGlobalConfigurations("OTT");
	}
    
    @Test
	public void testLoadGlobalConfigurations_satellite() {
		Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP))
				.thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
		cpopClient.loadGlobalConfigurations("satellite");
	}
    
    @Test
	public void testLoadGlobalEligibilityRules() {
		Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP))
				.thenReturn(false);
		Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
		cpopClient.loadGlobalEligibilityRules("OTT");
	}

    @Test
    public void testLoadValidateCartRules() {
        Mockito.when(cpopClientHelper.isPVTEnabled()).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)).thenReturn(false);
        Mockito.when(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_INTERNAL)).thenReturn(true);
        cpopClient.loadValidateCartRules("OTT");


    }

}