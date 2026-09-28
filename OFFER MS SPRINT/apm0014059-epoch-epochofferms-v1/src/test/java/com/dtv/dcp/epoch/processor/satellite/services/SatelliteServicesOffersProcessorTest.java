package com.dtv.dcp.epoch.processor.satellite.services;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.CpopUCCClient;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.model.common.request.CartContext;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.Price;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.dtv.dcp.epoch.model.ct.request.Product;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;
import com.dtv.dcp.epoch.model.customergraph.CustomerCouponsResults;
import com.dtv.dcp.epoch.model.customergraph.CustomerGraphPublisherMessage;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.service.AccountLookupService;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphClientService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.PnpGroupUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author ka6579
 *
 */
@ExtendWith(MockitoExtension.class)
public class SatelliteServicesOffersProcessorTest {
	
	@InjectMocks
	SatelliteServicesOffersProcessor satelliteServicesOffersProcessor;
	
	@Mock
	CpopUCCClientHelper cpopUCCClientHelper;
	
	@Mock
	CustomerGraphService customerGraphService;
	
	@Mock
	FeatureManagerHelper featureManagerHelper;
	
	@Mock
	CustomerGraphClientService customerGraphClientService;
	
	@Mock
	KafkaTemplate<String, CustomerGraphPublisherMessage> kafkaTemplateCG;
	
	@Mock
	CustomerCoupons customerCoupons;
	
	@Mock
	SatelliteServicesOffersProcessorUCCHelper satelliteServicesOffersProcessorUCCHelper;
	
	@Mock
	OffersUtils util;
	
	@Mock
	private SatelliteCTOffersProcessor satelliteCTOffersProcessor;
	
	@Mock
    CpopUCCClient cpopClient;
	
	@Mock
	CpopClientHelper cpopClientHelper;
	
	@Mock
	private AccountLookupService accountLookupService;
	
	@Mock
	private DMALookUpService dmaLookUpService;
	
	@Mock
	private PnpGroupUtils pnpGroupUtils;
	
	@Mock
	private RedisCacheHelper redisCacheHelper;

	@Spy
	ObjectMapper objectMapper = new ObjectMapper();

	private static final String CT_RESPONSE_COUPON_GET_OFFER = "CTResponsrCouponGetOffer.json"; 
	
    private static final String OFFER_REQUEST_SATELLITE_SERVICES_FILTER = "enabler/offer_request_satellite_enabler.json";
	
	private static final String OFFER_REQUEST_SATELLITE_SALES_FILTER = "acquisition/offer_request_satellite_stms.json";
	
	private static final String OFFER_REQUEST_SATELLITE_ACCESSORY_FILTER= "enabler/offer_request_satellite_accessory_enabler.json";
	
	private static final String OFFER_REQUEST_SATELLITE_ACCESSORY_STMS = "offer_request_satellite_accessory_stms.json";
	
	private static final String OFFER_REQUEST_SATELLITE_SERVICES_CART_CONTEXT_FILTER = "enabler/offer_request_satellite_cart_context.json";
	
	private static final String OFFER_REQUEST_SATELLITE_DEVICE_ENABLER = "enabler/offer_request_satellite_device_enabler.json";
	
	private static final String OFFER_REQUEST_SATELLITE_DEVICE_STMS = "offer_request_satellite_device_stms.json";
	
	private static final String OFFER_REQUEST_SATELLITE_ACCESSORY_CARTOFFERS="enabler/offer_requst_satellite_accessory_cartOffers.json";
	
	private static final String OFFER_REQUEST_SATELLITE_SERVICES_ENABLER_PRICE_PROTECTION = "enabler/offer_request_satellite_enabler_price_protection.json";
	
	private static final String OFFER_REQUEST_SATELLITE_BILLING_SYSTEM = "offer_request_satellite_billing_system.json";
	
	private static final String OFFER_REQUEST_PARTNER_NAME="stms/offer_request_partnerName.json";
	
	private static final String OFFER_RESPONSE_PARTNER_NAME="stms/offersResponse_partnerName.json";
	
	private static final String OFFER_REQUEST_IGNORECASE_BUSINESS_SEGMENT="stms/offer_request_centurylink.json";
	
	private static final String OFFER_REQUEST_TARGETED_OFFER_BCODE = "stms/offer_request_targeted_offer_bcode.json";
	
	private static final String OFFER_REQUEST_EXISTING_PROMO = "enabler/offer_request_existing_promo.json";
	
	private static final String OFFER_REQUEST_NO_EXISTING_PROMO = "enabler/offer_request_no_existing_promo.json";
	
	private static final String OFFER_REQUEST_ALL_ELIGIBLE = "stms/offer_request_all_eligible.json";
	
	private static final String OFFER_REQUEST_ACCOUNT_STATUS_INELIGIBLE = "stms/offer_request_account_status_ineligible.json";
	
	private static final String OFFER_REQUEST_HEART_VALUE_INELIGIBLE = "stms/offer_request_heart_value_ineligible.json";
	
	private static final String OFFER_REQUEST_ABP_INELIGIBLE = "stms/offer_request_abp_ineligible.json";
	
	private static final String OFFER_REQUEST_PB_INELIGIBLE = "stms/offer_request_pb_ineligible.json";
	
	private static final String OFFER_REQUEST_ACTIVATION_DATE_INELIGIBLE = "stms/offer_request_activation_date_ineligible.json";
	
	private static final String OFFER_REQUEST_COMMISSION_DATE_INELIGIBLE = "stms/offer_request_commission_date_ineligible.json";
	
	private static final String OFFER_REQUEST_PROACTIVE_CREDIT_ELIGIBLE = "stms/offer_request_proactive_credit_eligible.json";
	
	private static final String OFFER_REQUEST_PROACTIVE_CREDIT_INELIGIBLE = "stms/offer_request_proactive_credit_ineligible.json";

	private static final String OFFER_REQUEST_RETENTION_DISPUTE_CREDIT = "stms/offer_request_retention_dispute_credit.json";
	private static final String RETENTION_DISPUTE_OFFER_RESP_CREDIT = "stms/retention_dispute_offer_response_credit.json";
	
	private static final String OFFER_REQUEST_STMS_PSO_CREDIT = "offer_request_stms_pso_credit.json";
	private static final String OFFER_REQUEST_ENABLER_PSO_CREDIT = "offer_request_enabler_pso_credit.json";
	private static final String PSO_RESP_CREDIT = "pso_response_credit.json";

	private static final String OFFER_REQUEST_ENABLER_WITH_PP = "enabler/offer_request_enabler_with_pp.json";
	private static final String OFFER_REQUEST_ENABLER_NO_PP = "enabler/offer_request_enabler_no_pp.json";
	private static final String OFFER_RESPONSE_PNP = "enabler/offer_response_pnp.json";
	
	private static final String OFFER_REQUEST_ENABLER_LOCALS = "enabler/offer_request_enabler_locals.json";
	private static final String OFFER_REQUEST_ENABLER_NO_LOCALS = "enabler/offer_request_enabler_no_locals.json";
	private static final String OFFER_RESPONSE_LNL = "enabler/offer_response_lnl.json";
	
	private static final String IPOR_CCID_OFFER_REQUEST = "stms/ipor_ccid_offer_request.json";
	private static final String IPOR_OFFER_REQUEST = "stms/ipor_without_ccid_request.json";
	private static final String INCORRECT_CCID_OFFER_REQUEST = "stms/incorrect_ccid_request.json";
	private static final String IPOR_OFFER_RESPONSE = "stms/ipor_offer_response.json";
	
	private static final String COMBINED_RETENTION_CALL_REQUEST="stms/Dispute_request_withAll_productTypes_request.json";
	private static final String COMBINED_RETETNION_CALL_RESPONSE="stms/Dispute_request_withAll_productTypes_offers_response.json";
	private static final String CREDIT_OFFERS_WITH_OTHER_ACTION_TYPES_REQUEST="enabler/credit_offers_with_other_action_types_request.json";
	private static final String CREDIT_OFFERS_WITH_OTHER_ACTION_TYPES_RESPONSE="enabler/credit_offers_with_other_action_types_offers_response.json";
	
	private static final String OFFER_RESPONSE_TENURE_RULE_ENABLER = "enabler/offer_response_tenure_rule_enabler.json";
	
	private static final String OFFER_REQUEST_LCC_ON_ACCOUNT = "stms/offer_request_lcc_on_account.json";

    private static final String CT_RESPONSE_COUPON_GET_OFFER_ADE_PERF_FLOW = "CTResponsrCouponGetOfferADEPerfFlow.json";
    private static final String SUBCATEGORY_SERVICE_ADDON_REQUEST = "stms/subcategory_filter_addon_service_request.json";
    private static final String SUBCATEGORY_SERVICE_ADDON_RESPONSE = "stms/subcategory_filter_addon_service_response.json";
    
    private static final String OFFER_REQUEST_LOCALS_RR = "stms/offer_request_locals_rr.json";
    private static final String OFFER_REQUEST_LOCALS_BAU = "stms/offer_request_locals_bau.json";
    private static final String OFFER_REQUEST_NO_LOCALS_RR = "stms/offer_request_no_locals_rr.json";
    private static final String OFFER_RESPONSE_RR = "stms/offer_response_rr.json";
    
    private static final String OFFER_REQUEST_LOCALS_RR_ENABLER = "enabler/offer_request_locals_rr.json";
    private static final String OFFER_REQUEST_LOCALS_BAU_ENABLER = "enabler/offer_request_locals_bau.json";
    private static final String OFFER_REQUEST_NO_LOCALS_RR_ENABLER = "enabler/offer_request_no_locals_rr.json";
    
    private static final String PRICE_SUBCATEGORY_ADDON_RESPONSE = "stms/price_subcategory_filter_addon_response.json";

	private static final String OFFER_REQUEST_SERVICES = "stms/offer_request_services.json";
	private static final String OFFER_RESPONSE_SERVICES = "stms/offer_response_services.json";

	private static final String DTVS_ADE_CT_RESPONSE = "DTVS_ADE_CTResponse.json";
	private static final String DTVS_ADE_GET_OFFER_REQUEST = "DTVS_ADE_GetOfferRequest.json";
	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	
	}
	
	@Test
	public void testGetOffers() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CTOfferResponse offerResponse = new DataReader().readFileToObj(CT_RESPONSE_COUPON_GET_OFFER,CTOfferResponse.class);
		OfferRequest offerRequest = new DataReader().readFileToObj("getOfferCouponOfferRequest.json",OfferRequest.class);
		CustomerCouponsResults customerCouponsResults = new DataReader().readFileToObj("CustomerCouponsResults.json",CustomerCouponsResults.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
		Mockito.when(satelliteServicesOffersProcessorUCCHelper.getDifferenceTime(Mockito.anyString())).thenReturn("4000");
		doReturn(true).when(featureManagerHelper).isEnabled(Constants.SVC_EPOCH_UCC_ENABLED);
		doReturn(true).when(featureManagerHelper).isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP);
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.anyString(),Mockito.anyString(),Mockito.anyString() , Mockito.any(), Mockito.anyString());
		doReturn(offerResponse).when(cpopUCCClientHelper).getOffers(ctOfferRequest);
		assertNotNull(satelliteServicesOffersProcessor.getOffers(offerRequestWrapper));
		
	}

    @Test
    public void testGetOffers_ADE_Perf_Flow() {
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse offerResponse = new DataReader().readFileToObj(CT_RESPONSE_COUPON_GET_OFFER_ADE_PERF_FLOW, CTOfferResponse.class);
        OfferRequest offerRequest = new DataReader().readFileToObj("getOffeOtherOfferRequest.json", OfferRequest.class);
        CustomerCouponsResults customerCouponsResults = new DataReader().readFileToObj("CustomerCouponsResults.json", CustomerCouponsResults.class);
        offerRequestWrapper.setOfferRequest(offerRequest);
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
        Mockito.when(featureManagerHelper.isEnabled(any())).thenReturn(true);
        Mockito.lenient().when(satelliteServicesOffersProcessorUCCHelper.getDifferenceTime(Mockito.anyString())).thenReturn("4000");
        Mockito.lenient().doReturn(true).when(featureManagerHelper).isEnabled(Constants.SVC_EPOCH_UCC_ENABLED);
        Mockito.lenient().doReturn(true).when(featureManagerHelper).isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP);
        Mockito.lenient().doReturn(false).when(featureManagerHelper).isEnabled(Constants.FEATURE_SVC_EPOCH_ASYNC_CALLS_ENABLED);
        Mockito.lenient().doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyString());
        Mockito.lenient().doReturn(offerResponse).when(cpopUCCClientHelper).getOffers(ctOfferRequest);
        assertNotNull(satelliteServicesOffersProcessor.getOffers(offerRequestWrapper));

    }

	@Test
	public void testGetOffersUccFlagDisabled() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CTOfferResponse offerResponse = new DataReader().readFileToObj(CT_RESPONSE_COUPON_GET_OFFER,CTOfferResponse.class);
		OfferRequest offerRequest = new DataReader().readFileToObj("getOfferCouponOfferRequest.json",OfferRequest.class);
		CustomerCouponsResults customerCouponsResults = new DataReader().readFileToObj("CustomerCouponsResults.json",CustomerCouponsResults.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
		doReturn(false).when(featureManagerHelper).isEnabled(Constants.SVC_EPOCH_UCC_ENABLED);
		Mockito.lenient().doReturn(true).when(featureManagerHelper).isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP);
		Mockito.lenient().doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.anyString(),Mockito.anyString(),Mockito.anyString() , Mockito.any(), Mockito.anyString());
		Mockito.lenient().doReturn(offerResponse).when(cpopUCCClientHelper).getOffers(ctOfferRequest);
		doReturn(offerResponse).when(satelliteServicesOffersProcessorUCCHelper).getEmptyCTResponse();	
		assertNotNull(satelliteServicesOffersProcessor.getOffers(offerRequestWrapper));
		
	}
	
	@Test
	public void testGetOffersOpus() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CTOfferResponse offerResponse = new DataReader().readFileToObj(CT_RESPONSE_COUPON_GET_OFFER,CTOfferResponse.class);
		OfferRequest offerRequest = new DataReader().readFileToObj("getOfferCouponOffersOpusReq.json",OfferRequest.class);
		CustomerCouponsResults customerCouponsResults = new DataReader().readFileToObj("CustomerCouponsResults.json",CustomerCouponsResults.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
		doReturn(false).when(featureManagerHelper).isEnabled(Constants.SVC_EPOCH_UCC_ENABLED);
		Mockito.lenient().doReturn(true).when(featureManagerHelper).isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP);
		Mockito.lenient().doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.anyString(),Mockito.anyString(),Mockito.anyString() , Mockito.any(), Mockito.anyString());
		Mockito.lenient().doReturn(offerResponse).when(cpopUCCClientHelper).getOffers(ctOfferRequest);
		doReturn(offerResponse).when(satelliteServicesOffersProcessorUCCHelper).getEmptyCTResponse();	
		assertNotNull(satelliteServicesOffersProcessor.getOffers(offerRequestWrapper));
		
	}

	@Test
	public void testGetOffersFromCT() {
		CTOfferResponse offerResponse = new DataReader().readFileToObj(CT_RESPONSE_COUPON_GET_OFFER,CTOfferResponse.class);
		CTOfferRequest ctOfferRequest = new DataReader().readFileToObj("CTRequestCouponType.json",CTOfferRequest.class);
		Mockito.when(cpopUCCClientHelper.getOffers(Mockito.any())).thenReturn(offerResponse);
		assertNotNull(satelliteServicesOffersProcessor.getOffersFromCT(ctOfferRequest));
	}
	
	@Test
	public void testGetOffersFromCTAsync() {
		CTOfferResponse offerResponse = new DataReader().readFileToObj(DTVS_ADE_CT_RESPONSE,CTOfferResponse.class);
		CTOfferRequest ctOfferRequest = new DataReader().readFileToObj(DTVS_ADE_GET_OFFER_REQUEST,CTOfferRequest.class);
		Mockito.when(cpopUCCClientHelper.getOffers(Mockito.any())).thenReturn(offerResponse);
		assertNotNull(satelliteServicesOffersProcessor.getOffersFromCTAsync(ctOfferRequest));
	}		
	
	@Test
	public void testGetProductIdFromOffer() {
		CTOffer offer = new DataReader().readFileToObj("acquisition/video-plan-selected.json", CTOffer.class);
		com.dtv.dcp.epoch.model.ct.product.Product product = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
				                                                    .getProducts().get(0);
		String productID=product.getId();
		Mockito.lenient().doReturn(productID).when(util).getProductIdFromOffer(offer);
		assertTrue(!productID.isEmpty());
	}
	
	@Test
	public void testHasLocalChannels() {
		CTOffer offer = new DataReader().readFileToObj("acquisition/video-plan-selected.json", CTOffer.class);
		ProductObj product = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj();
		boolean value=product.getVariants().get(0).getAttributes().getHasLocalChannels();
		assertTrue(value);
	}
	
	@Test
	public void testGetOffersSatelliteForBillingCode() {
		
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequestWrapper offerRequestWrapper=new OfferRequestWrapper();
    	
    	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_SALES_FILTER, OfferRequest.class);
        
        List<CTCustomerContext> customerContextList = new ArrayList<>();
		CTCustomerContext ctCustomerContext = new CTCustomerContext();
		ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
		ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
		ctCustomerContext.setNftvFlag(offerRequest.getCustomerContext().getSatellite().getNftvFlag());
		ctCustomerContext.setFtvCount(offerRequest.getCustomerContext().getSatellite().getFtvCount());
		List<com.dtv.dcp.epoch.model.ct.request.Product> productList=new ArrayList<>();
       com.dtv.dcp.epoch.model.ct.request.Product products = new com.dtv.dcp.epoch.model.ct.request.Product();
        products.setBillingProductCode("prod3470006");
        products.setProductType(Constants.VIDEO_DEVICE);
        productList.add(products);
        ctCustomerContext.setProducts(productList);
		customerContextList.add(ctCustomerContext);
        ctOfferRequest.setCustomerContext(customerContextList);
        offerRequestWrapper.setOfferRequest(offerRequest);
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
        
        CTOfferResponse ctOfferResponseMock = new DataReader().readFileToObj("acquisition/video-device.json",CTOfferResponse.class);
        
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponseMock);
        
        ctOfferResponse=satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        
        ProductObj product = ctOfferResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
        																		  .getProducts().get(0).getObj();
         
        assertEquals(11, ctOfferResponse.getOffers().size());
        assertNotNull(product);
        List<Price> prices  = product.getVariants().get(0).getPrices();
        assertEquals(1, prices.size());
        assertNotNull(ctOfferResponse);
    
	}

	@Test
	public void testGetOffersSatelliteForPricePlanCode() {
		
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequestWrapper offerRequestWrapper=new OfferRequestWrapper();
        
        offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_SERVICES_FILTER, OfferRequest.class);
        
        List<CTCustomerContext> customerContextList = new ArrayList<>();
		CTCustomerContext ctCustomerContext = new CTCustomerContext();
		ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
		ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
		List<com.dtv.dcp.epoch.model.ct.request.Product> productList=new ArrayList<>();
       com.dtv.dcp.epoch.model.ct.request.Product products = new com.dtv.dcp.epoch.model.ct.request.Product();
        products.setPricePlanCode("88830604");
        products.setPickCode("P0030");
        products.setProductType(Constants.VIDEO_ADDON);
        productList.add(products);
        ctCustomerContext.setProducts(productList);
		customerContextList.add(ctCustomerContext);
        ctOfferRequest.setCustomerContext(customerContextList);
        offerRequestWrapper.setOfferRequest(offerRequest);
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
        
        CTOfferResponse ctOfferResponseMock = new DataReader().readFileToObj("enabler/video-addon.json",CTOfferResponse.class);
        
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponseMock);
        ctOfferResponse=satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        
        ProductObj product = ctOfferResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
        					 .getProducts().get(0).getObj();
         
        assertEquals(4, ctOfferResponse.getOffers().size());
        assertNotNull(product);
        List<Price> prices  = product.getVariants().get(0).getPrices();
        assertEquals(1, prices.size());
        assertNotNull(ctOfferResponse);
    
	}
	
	@Test
	public void testGetOffersSatelliteForComponentCode() {
		
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequestWrapper offerRequestWrapper=new OfferRequestWrapper();
            
        offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_ACCESSORY_FILTER, OfferRequest.class);
            
        List<CTCustomerContext> customerContextList = new ArrayList<>();
		CTCustomerContext ctCustomerContext = new CTCustomerContext();
		ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
		ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
		List<com.dtv.dcp.epoch.model.ct.request.Product> productList=new ArrayList<>();
       com.dtv.dcp.epoch.model.ct.request.Product products = new com.dtv.dcp.epoch.model.ct.request.Product();
        products.setComponentCode("DTVRVU");
        products.setModelNumber("C41-100");
        products.setProductType(Constants.VIDEO_DEVICE);
        productList.add(products);
        ctCustomerContext.setProducts(productList);
		customerContextList.add(ctCustomerContext);
        ctOfferRequest.setCustomerContext(customerContextList);
        offerRequestWrapper.setOfferRequest(offerRequest);
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
        
        CTOfferResponse ctOfferResponseMock = new DataReader().readFileToObj("enabler/video-accessory.json",CTOfferResponse.class);
        
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponseMock);
        
        CTProductResponse ctProductResponseMock = new DataReader().readFileToObj("enabler/products_device.json",CTProductResponse.class);
        when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponseMock);
		
        ctOfferResponse=satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertEquals(3, ctOfferResponse.getOffers().size());
        ProductObj product = ctProductResponseMock.getProducts().get(2);
        assertNotNull(product);
        assertNotNull(ctOfferResponse);
      
	}
	
	@Test
    public void testGetOffersRemoteProtectionPlanEnabler() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_ACCESSORY_FILTER, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
            CTCustomerContext ctCustomerContext = new CTCustomerContext();
            List<Product> productList = new ArrayList<>();
            Product product = new Product();
            product.setComponentCode("DTVRVU");
            product.setModelNumber("C41-100");
            product.setProductType(Constants.VIDEO_DEVICE);
            productList.add(product);
            ctCustomerContext.setProducts(productList);
			ctCustomerContext.setRemoteReplacementType("protectionPlan");
			customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
            
            CustomerContext customerContext = offerRequest.getCustomerContext();
            CartProduct satellite = customerContext.getSatellite();
            satellite.setRemoteReplacementType("protectionPlan");
            offerRequest.setCustomerContext(customerContext);
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/CTRemoteOfferResponse.json", CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        
        CTProductResponse ctProductResponse = new DataReader().readFileToObj("enabler/products_device.json",CTProductResponse.class);
        when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponse);
        
        CTOfferResponse ctOfferResponsePp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResponsePp);
        assertNotNull(ctOfferResponsePp.getOffers());
		assertEquals(3, ctOfferResponsePp.getOffers().size());
    }
    
    @Test
    public void testGetOffersNoRemoteTypeEnabler() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_ACCESSORY_FILTER, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		List<Product> productList = new ArrayList<>();
            Product product = new Product();
            product.setComponentCode("DTVRVU");
            product.setModelNumber("C41-100");
            product.setProductType(Constants.VIDEO_DEVICE);
            productList.add(product);
            ctCustomerContext.setProducts(productList);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
            
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/CTRemoteOfferResponse.json", CTOfferResponse.class);;
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        
        CTProductResponse ctProductResponse = new DataReader().readFileToObj("enabler/products_device.json",CTProductResponse.class);
        when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponse);
        
        CTOfferResponse ctOfferResponseBau = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResponseBau);
        assertNotNull(ctOfferResponseBau.getOffers());
		assertEquals(5, ctOfferResponseBau.getOffers().size());
    }
    
    @Test
    public void testGetOffersRemoteProtectionPlanSTMS() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_ACCESSORY_STMS, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
            CTCustomerContext ctCustomerContext = new CTCustomerContext();
			ctCustomerContext.setRemoteReplacementType("protectionPlan");
			customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
            
            CustomerContext customerContext = offerRequest.getCustomerContext();
            CartProduct satellite = customerContext.getSatellite();
            satellite.setRemoteReplacementType("protectionPlan");
            offerRequest.setCustomerContext(customerContext);
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/CTRemoteOfferResponse.json", CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResponsePp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResponsePp);
        assertNotNull(ctOfferResponsePp.getOffers());
		assertEquals(3, ctOfferResponsePp.getOffers().size());
    }
    
    @Test
    public void testGetOffersNoRemoteTypeSTMS() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_ACCESSORY_STMS, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/CTRemoteOfferResponse.json", CTOfferResponse.class);;
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        
        CTProductResponse ctProductResponse = new DataReader().readFileToObj("enabler/products_device.json",CTProductResponse.class);
        Mockito.lenient().when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponse);
        
        CTOfferResponse ctOfferResponseBau = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResponseBau);
        assertNotNull(ctOfferResponseBau.getOffers());
		assertEquals(5, ctOfferResponseBau.getOffers().size());
    }
    
    @Test
	public void testGetOffersSatelliteForCartContext() {
		
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequestWrapper offerRequestWrapper=new OfferRequestWrapper();
        List<CartOffer> cartOffers = new ArrayList<>();
        List<Product> cartProducts = new ArrayList<>();
        
        
        offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_SERVICES_CART_CONTEXT_FILTER, OfferRequest.class);
        CartContext cartContext = new CartContext();
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS_satellite");
        cartOffer.setQuantity(1);
        cartOffers.add(cartOffer);
        Product cartProduct = new Product();
        cartProduct.setPricePlanCode("88890325");
        cartProduct.setProductType(Constants.VIDEO_PLAN);
        cartProducts.add(cartProduct);
        cartContext.setCartOffers(cartOffers);
        cartContext.setCartProducts(cartProducts);
        
        List<CTCustomerContext> customerContextList = new ArrayList<>();
		CTCustomerContext ctCustomerContext = new CTCustomerContext();
		ctCustomerContext.setPolicy(offerRequest.getCustomerContext().getSatellite().getPolicy());
		List<Product> productList=new ArrayList<>();
        Product products = new Product();
        products.setPricePlanCode("88830604");
        products.setPickCode("P0030");
        products.setProductType(Constants.VIDEO_ADDON);
        
        productList.add(products);
        ctCustomerContext.setProducts(productList);
		customerContextList.add(ctCustomerContext);
        ctOfferRequest.setCustomerContext(customerContextList);
      
        offerRequest.setCartContext(cartContext);
        offerRequestWrapper.setOfferRequest(offerRequest);
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
        
        CTOfferResponse ctOfferResponseMock = new DataReader().readFileToObj("enabler/video-addon.json",CTOfferResponse.class);
        
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponseMock);
        ctOfferResponse=satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        
        ProductObj product = ctOfferResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
        					 .getProducts().get(0).getObj();
         
        assertEquals(4, ctOfferResponse.getOffers().size());
        assertNotNull(product);
        assertNotNull(ctOfferResponse);
    
	}

    
    @Test
    public void testGetOffersSatelliteDeviceEnabler() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse cartOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_DEVICE_ENABLER, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		List<Product> productList = new ArrayList<>();
            Product product = new Product();
            product.setComponentCode("DTVGenie");
            product.setPricePlanCode("422193475");
            product.setProductType(Constants.VIDEO_DEVICE);
            productList.add(product);
            ctCustomerContext.setProducts(productList);
            ctCustomerContext.setServiceOrderDate("03/09/2020");
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/CTDeviceFeeOfferResponse.json", CTOfferResponse.class);
            cartOfferResponse = new DataReader().readFileToObj("enabler/CTDeviceFeeCartOfferResponse.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(satelliteCTOffersProcessor.getOfferByIds(any(), anyBoolean(), anyBoolean())).thenReturn(cartOfferResponse);
        when(cpopUCCClientHelper.getProductObjByid(any(Map.class), any(String.class))).thenCallRealMethod();
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        when(util.getProductIdFromOffer(any())).thenCallRealMethod();
        when(util.filterOffersByProductType(any(), any())).thenCallRealMethod();
        when(util.filterFeeOffersByFeeType(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
		assertEquals(8, ctOfferResp.getOffers().size());
		
		CTOffer matchingObject = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-HD-DVR-RECEIVER_satellite".equals(offer.getCode())).
                findFirst().orElse(null);
		assertNotNull(matchingObject);
		assertEquals("callToOrder", matchingObject.getAttributes().getAllowedAction());
		assertNotNull(matchingObject.getAttributes().getReceiverOrderLimit());
		assertNotNull(matchingObject.getAttributes().getReceiverAccountLimit());
		
    }
    
    @Test
    public void testGetOffersSatelliteDeviceSTMS() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse cartOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_DEVICE_STMS, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		List<Product> productList = new ArrayList<>();
            Product product = new Product();
            product.setManufacturer("DIRECTV");
            product.setModelNumber("HR44-200");
            product.setProductType(Constants.VIDEO_DEVICE);
            productList.add(product);
            ctCustomerContext.setProducts(productList);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("CTDeviceOfferResponse.json", CTOfferResponse.class);
            cartOfferResponse = new DataReader().readFileToObj("CTDeviceCartOfferResponse.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(satelliteCTOffersProcessor.getOfferByIds(any(), anyBoolean(), anyBoolean())).thenReturn(cartOfferResponse);
        when(cpopUCCClientHelper.getProductObjByid(any(Map.class), any(String.class))).thenCallRealMethod();
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        when(util.getProductIdFromOffer(any())).thenCallRealMethod();
        when(util.filterOffersByProductType(any(), any())).thenCallRealMethod();
        when(util.filterFeeOffersByFeeType(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
		assertEquals(7, ctOfferResp.getOffers().size());
		
		CTOffer matchingObject = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_DEVICE-HD-DVR-RECEIVER_satellite".equals(offer.getCode())).
                findFirst().orElse(null);
		assertNotNull(matchingObject);
		assertEquals("callToOrder", matchingObject.getAttributes().getAllowedAction());
		assertNotNull(matchingObject.getAttributes().getReceiverOrderLimit());
		assertNotNull(matchingObject.getAttributes().getReceiverAccountLimit());
    }
    
    @Test
   	public void testGetOffersForAccessoryCartContext() {
   		
   		OfferRequest offerRequest = null;
           CTOfferResponse ctOfferResponse = null;
           CTOfferRequest ctOfferRequest = new CTOfferRequest();
           OfferRequestWrapper offerRequestWrapper=new OfferRequestWrapper();
               
           offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_ACCESSORY_CARTOFFERS, OfferRequest.class);
               
           List<CTCustomerContext> customerContextList = new ArrayList<>();
   		CTCustomerContext ctCustomerContext = new CTCustomerContext();
   		List<com.dtv.dcp.epoch.model.ct.request.Product> productList=new ArrayList<>();
          com.dtv.dcp.epoch.model.ct.request.Product products = new com.dtv.dcp.epoch.model.ct.request.Product();
           products.setComponentCode("DTVRVU");
           products.setModelNumber("C41-100");
           products.setProductType(Constants.VIDEO_DEVICE);
           productList.add(products);
           ctCustomerContext.setProducts(productList);
   		customerContextList.add(ctCustomerContext);
           ctOfferRequest.setCustomerContext(customerContextList);
           offerRequestWrapper.setOfferRequest(offerRequest);
           offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
           
           CTOfferResponse cartResponse = new DataReader().readFileToObj("enabler/cartOfferResponse.json",CTOfferResponse.class);
           when(satelliteCTOffersProcessor.getOfferByIds(any(), anyBoolean(), anyBoolean())).thenReturn(cartResponse);
           
       	CTOfferResponse ctOfferResponseMock = new DataReader().readFileToObj("enabler/device-and-accessory_response.json",CTOfferResponse.class);
           when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponseMock);
           
           CTProductResponse ctProductResponseMock = new DataReader().readFileToObj("enabler/products_device.json",CTProductResponse.class);
          Mockito.lenient().when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponseMock);
           
           String id1=cartResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getId();
           String id2=cartResponse.getOffers().get(1).getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getId();
           String id3=ctOfferResponseMock.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getId();
       	
       	when(util.getProductIdFromOffer(any())).thenReturn(id1,id2,id3);
       	
           ctOfferResponse=satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
           assertEquals(14, ctOfferResponse.getOffers().size());
           ProductObj product = ctProductResponseMock.getProducts().get(3);
           assertNotNull(product);

   		String value= product.getVariants().get(0).getAttributes().getBillingParams().get(0).getValue();
   		
   		String cartOfferValue=cartResponse.getOffers().get(1).getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).
   	            getProducts().get(0).getObj().getVariants().get(0).getAttributes().getBillingParams().get(0).getValue();
   		assertNotSame(value, cartOfferValue);
   		assertEquals("DTVHDDVR", cartOfferValue);
           assertNotNull(ctOfferResponse);
   	}

	@Test
	public void testGetOffersSatelliteForPricePlanCodeWithPriceProtection() {
		
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequestWrapper offerRequestWrapper=new OfferRequestWrapper();
        
        offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_SERVICES_ENABLER_PRICE_PROTECTION, OfferRequest.class);
        
        List<CTCustomerContext> customerContextList = new ArrayList<>();
		CTCustomerContext ctCustomerContext = new CTCustomerContext();
		ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
		ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
		List<com.dtv.dcp.epoch.model.ct.request.Product> productList=new ArrayList<>();
       com.dtv.dcp.epoch.model.ct.request.Product products = new com.dtv.dcp.epoch.model.ct.request.Product();
        products.setPricePlanCode("88830604");
        products.setPickCode("P0030");
        products.setProductType(Constants.ADDON);
        productList.add(products);
        PriceProtection priceProtection = new PriceProtection();
        priceProtection.setStartDate("12/29/2021");
        priceProtection.setEndDate("12/26/2022");
        priceProtection.setNextBillingDate("01/27/2022");
        ctCustomerContext.setProducts(productList);
		customerContextList.add(ctCustomerContext);
        ctOfferRequest.setCustomerContext(customerContextList);
        ctOfferRequest.setPriceProtection(priceProtection);
        offerRequestWrapper.setOfferRequest(offerRequest);
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
        
        CTOfferResponse ctOfferResponseMock = new DataReader().readFileToObj("enabler/video-addon.json",CTOfferResponse.class);
        
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponseMock);
        ctOfferResponse=satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        
        ProductObj product = ctOfferResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
        					 .getProducts().get(0).getObj();
         
        assertEquals(4, ctOfferResponse.getOffers().size());
        assertNotNull(product);
        List<Price> prices  = product.getVariants().get(0).getPrices();
        assertEquals(1, prices.size());
        assertNotNull(ctOfferResponse);
    
	}
	
    @Test
    public void testGetOffersSatelliteBillingSystem() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SATELLITE_BILLING_SYSTEM, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("CTInsuranceOfferResponse.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
		assertEquals(3, ctOfferResp.getOffers().size());
		
    }
    
    @Test
    public void testGetOffersWithPartnerNameInRequest() {
    	
    	OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferResponse ctOfferResponse=new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		
		OfferRequest offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_PARTNER_NAME, OfferRequest.class);
    	offerRequest.setHasLocalChannels(false);
    	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
    	
        offerRequestWrapper.setOfferRequest(offerRequest);
        List<CTCustomerContext> customercontextList=new ArrayList<>();
        CTCustomerContext customerContext=new CTCustomerContext();
        customerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
        customerContext.setPartnerName(offerRequest.getCustomerContext().getSatellite().getPartnerName());
        customerContext.setFtvCount(offerRequest.getCustomerContext().getSatellite().getFtvCount());
        customerContext.setNftvFlag(offerRequest.getCustomerContext().getSatellite().getNftvFlag());
        customerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
        customercontextList.add(customerContext);
        offerRequestWrapper.getCtOfferRequest().setCustomerContext(customercontextList);
        
        ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_PARTNER_NAME, CTOfferResponse.class);
		
		when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        
        Optional<CTOffer> offer = ctOfferResponse.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        List<Price> prices=offer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj()
        		.getVariants().get(0).getPrices();
        
        Price price=prices.get(2);
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp.getOffers());
    	
        Optional<CTOffer> matchingObject = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(matchingObject.isPresent());
        Price finalPrice=matchingObject.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj()
        		.getVariants().get(0).getPrices().get(0);
		assertTrue(price.getValue().getDollarAmount().equals(finalPrice.getValue().getDollarAmount()));
    }
    
    @Test
    public void testGetOffersWithCaseInsensitiveBusinessSegmentAndEmployeeSegment() {
    	
    	OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferResponse ctOfferResponse=new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		
		OfferRequest offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_IGNORECASE_BUSINESS_SEGMENT, OfferRequest.class);
    	offerRequest.setHasLocalChannels(false);
    	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
    	
        offerRequestWrapper.setOfferRequest(offerRequest);
        List<CTCustomerContext> customercontextList=new ArrayList<>();
        CTCustomerContext customerContext=new CTCustomerContext();
        customerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
        customerContext.setPartnerName(offerRequest.getCustomerContext().getSatellite().getPartnerName());
        customerContext.setFtvCount(offerRequest.getCustomerContext().getSatellite().getFtvCount());
        customerContext.setNftvFlag(offerRequest.getCustomerContext().getSatellite().getNftvFlag());
        customerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
        offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
        customercontextList.add(customerContext);
        offerRequestWrapper.getCtOfferRequest().setCustomerContext(customercontextList);
        
        ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_PARTNER_NAME, CTOfferResponse.class);
		
		when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        
        Optional<CTOffer> offer = ctOfferResponse.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        List<Price> prices=offer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj()
        		.getVariants().get(0).getPrices();
        
        Price price=prices.get(2);
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp.getOffers());
    	
        Optional<CTOffer> matchingObject = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(matchingObject.isPresent());
        Price finalPrice=matchingObject.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj()
        		.getVariants().get(0).getPrices().get(0);
		assertTrue(price.getValue().getDollarAmount().equals(finalPrice.getValue().getDollarAmount()));
    }
    
    @Test
    public void testGetOffersSatelliteTargetedOfferBcodeExclusion() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_TARGETED_OFFER_BCODE, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offerResponse_targeted_offer.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        doCallRealMethod().when(satelliteCTOffersProcessor).applyBcodeExclusion(any(), any());
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> matchingObject = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-HBO-MAX-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(matchingObject.isPresent());
        long count = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> p.getCode().startsWith("OF_BOLTON-HBO-MAX")).count();
        assertEquals(1, count);
    }
    
    @Test
    public void testGetOffersStackabilityExistingActivePromos() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_EXISTING_PROMO, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	LocalDate localDate = LocalDate.now();
        	localDate = localDate.plusDays(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String endDate = localDate.format(formatter);        
            offerRequest.getCustomerContext().getSatellite().getExistingPromotions().get(0).setEndDate(endDate);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/offerResponse_existing_promo.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        
        doCallRealMethod().when(satelliteCTOffersProcessor).applyBcodeExclusion(any(), any());
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> matchingObject = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-MOVIES-EXTRA-PACK_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(matchingObject.isPresent());
        long count = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> p.getCode().startsWith("OF_BOLTON-MOVIES-EXTRA-PACK")).count();
        assertEquals(1, count);
    }
    
    @Test
    public void testGetOffersStackabilityExistingExpiredPromos() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_EXISTING_PROMO, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/offerResponse_existing_promo.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        
        doCallRealMethod().when(satelliteCTOffersProcessor).applyBcodeExclusion(any(), any());
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> matchingObject = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-MOVIES-EXTRA-PACK-V1_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(matchingObject.isPresent());
        long count = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> p.getCode().startsWith("OF_BOLTON-MOVIES-EXTRA-PACK")).count();
        assertEquals(1, count);
    }
    
    @Test
    public void testGetOffersStackabilityNoExistingPromos() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_NO_EXISTING_PROMO, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/offerResponse_existing_promo.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        
        doCallRealMethod().when(satelliteCTOffersProcessor).applyBcodeExclusion(any(), any());
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> matchingObject = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-MOVIES-EXTRA-PACK-V1_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(matchingObject.isPresent());
        long count = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> p.getCode().startsWith("OF_BOLTON-MOVIES-EXTRA-PACK")).count();
        assertEquals(1, count);
    }
    
    @Test
    public void testGetOffersSatelliteAllEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ALL_ELIGIBLE, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_showtime.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> specialPriceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SPECIAL-PRICE-SHOWTIME-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(specialPriceOffer.isPresent());
		
    }
    
    @Test
    public void testGetOffersSatelliteAccountStatusInEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ACCOUNT_STATUS_INELIGIBLE, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_showtime.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> showTimeOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SHOWTIME_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(showTimeOffer.isPresent());
        Optional<CTOffer> specialPriceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SPECIAL-PRICE-SHOWTIME-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(specialPriceOffer.isPresent());
		
    }    
        
    @Test
    public void testGetOffersSatelliteHeartValueInEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_HEART_VALUE_INELIGIBLE, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_showtime.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> showTimeOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SHOWTIME_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(showTimeOffer.isPresent());
        Optional<CTOffer> specialPriceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SPECIAL-PRICE-SHOWTIME-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(specialPriceOffer.isPresent());
		
    }    
        
    @Test
    public void testGetOffersSatelliteABPInEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ABP_INELIGIBLE, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_showtime.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> showTimeOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SHOWTIME_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(showTimeOffer.isPresent());
        Optional<CTOffer> specialPriceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SPECIAL-PRICE-SHOWTIME-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(specialPriceOffer.isPresent());
		
    }  
        
    @Test
    public void testGetOffersSatellitePBInEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_PB_INELIGIBLE, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_showtime.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> showTimeOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SHOWTIME_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(showTimeOffer.isPresent());
        Optional<CTOffer> specialPriceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SPECIAL-PRICE-SHOWTIME-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(specialPriceOffer.isPresent());
		
    }    
    
    @Test
    public void testGetOffersSatelliteActivationDateInEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ACTIVATION_DATE_INELIGIBLE, OfferRequest.class);
        	
        	LocalDate localDate = LocalDate.now();
        	localDate = localDate.minusDays(14);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String activationDate = localDate.format(formatter);        
            offerRequest.getCustomerContext().getSatellite().setActivationDate(activationDate);
            
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_showtime.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> showTimeOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SHOWTIME_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(showTimeOffer.isPresent());
        Optional<CTOffer> specialPriceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SPECIAL-PRICE-SHOWTIME-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(specialPriceOffer.isPresent());
		
    } 
        
    @Test
    public void testGetOffersSatelliteCommissionDateInEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_COMMISSION_DATE_INELIGIBLE, OfferRequest.class);
        	
        	LocalDate localDate = LocalDate.now();
        	localDate = localDate.minusDays(14);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String commissionDate = localDate.format(formatter);        
            offerRequest.getCustomerContext().getSatellite().setCommissionStartDate(commissionDate);
            
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_showtime.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> showTimeOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SHOWTIME_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(showTimeOffer.isPresent());
        Optional<CTOffer> specialPriceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-SPECIAL-PRICE-SHOWTIME-V2_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(specialPriceOffer.isPresent());
		
    }
    
    @Test
    public void testGetOffersSatelliteProactiveCreditEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_PROACTIVE_CREDIT_ELIGIBLE, OfferRequest.class);        	
        	
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_proactive_credit.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> pcOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_PROMO-PROACTIVE-CREDIT-5-OFF-12-MO_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(pcOffer.isPresent());
		
    }
    
    @Test
    public void testGetOffersSatelliteProactiveCreditInEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_PROACTIVE_CREDIT_INELIGIBLE, OfferRequest.class);
        	
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("stms/offer_response_proactive_credit.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        
        Optional<CTOffer> pcOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_PROMO-PROACTIVE-CREDIT-5-OFF-12-MO_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(pcOffer.isPresent());
		
    }
    
    @Test
	public void testGetRetentionDisputeOffers() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTCustomerContext ctCustomerContext = new CTCustomerContext();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_RETENTION_DISPUTE_CREDIT, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
            ctOfferRequest.setCustomerContext(new ArrayList<>());
            ctOfferRequest.getCustomerContext().add(ctCustomerContext);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(RETENTION_DISPUTE_OFFER_RESP_CREDIT, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
        CTOfferResponse retentionOffers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(retentionOffers);
        assertEquals(14, retentionOffers.getOffers().size());
        Optional<CTOffer> matchingObject = retentionOffers.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_PRO-LCL-DISP-10-OFF-1MO_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(matchingObject.isPresent());
	}
    
    @Test
	public void testGetRetentionDisputeOffersDTV() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTCustomerContext ctCustomerContext = new CTCustomerContext();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_RETENTION_DISPUTE_CREDIT, OfferRequest.class);
        	offerRequest.getCustomerContext().getSatellite().setBusinessSegment("DTV");
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
            ctOfferRequest.setCustomerContext(new ArrayList<>());
            ctOfferRequest.getCustomerContext().add(ctCustomerContext);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(RETENTION_DISPUTE_OFFER_RESP_CREDIT, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
        CTOfferResponse retentionOffers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(retentionOffers);
        assertEquals(13, retentionOffers.getOffers().size());
        Optional<CTOffer> matchingObject = retentionOffers.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_PRO-LCL-DISP-10-OFF-1MO_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(matchingObject.isPresent());
	}
    
    @Test
	public void testGetPremiumSaveOffersSTMS() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_STMS_PSO_CREDIT, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setBillingProductCode("P2141");
            product.setProductType(Constants.VIDEO_ADDON);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(PSO_RESP_CREDIT, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
       Mockito.lenient().when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse psoOffer = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(psoOffer);
        assertEquals(1, psoOffer.getOffers().size());
	}
    
    @Test
	public void testGetPremiumSaveOffersEnabler() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ENABLER_PSO_CREDIT, OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88830604");
            product.setPickCode("P0027");
            product.setProductType(Constants.VIDEO_ADDON);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(PSO_RESP_CREDIT, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        
        Mockito.lenient().when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        
        CTOfferResponse psoOffer = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(psoOffer);
        assertEquals(1, psoOffer.getOffers().size());
	}
    
    @Test
    public void testGetOffersSatellite2ndChanceOffers() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj("enabler/request_activationDate_withinTenure.json", OfferRequest.class);
        	
        	LocalDate localDate = LocalDate.now();
        	localDate = localDate.minusDays(10);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String activationDate = localDate.format(formatter);        
            offerRequest.getCustomerContext().getSatellite().setActivationDate(activationDate);
            
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88890295");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/offer_pick4+epix_2ndChance.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.getProductIdFromOffer(any())).thenCallRealMethod();
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> secondChanceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-HBO-SHOW-STARZ-CINE-EPIX_second_chance_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(secondChanceOffer.isPresent());		
    }
    
    @Test
    public void testGetOffersSatellite2ndChanceOffers_Mdyyyy() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj("enabler/request_activationDate_withinTenure.json", OfferRequest.class);
        	
            offerRequest.getCustomerContext().getSatellite().setActivationDate("1/2/2023");
            
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88890295");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/offer_pick4+epix_2ndChance.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> secondChanceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-HBO-SHOW-STARZ-CINE-EPIX_second_chance_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(secondChanceOffer.isPresent());		
    }
    
    @Test
    public void testGetOffers2ndChanceOffersWithoutActivationDate() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj("enabler/request_without_activationDate.json", OfferRequest.class);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88890295");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("enabler/offer_pick4+epix_2ndChance.json", CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> secondChanceOffer = ctOfferResp.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-HBO-SHOW-STARZ-CINE-EPIX_second_chance_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(secondChanceOffer.isPresent());		
    }

	@Test
	public void testGetOffersSatelliteForServerDate() {
		OfferRequest offerRequest = null;
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		try {
			offerRequest = new DataReader().readFileToObj("enabler/serverDate_getOffers_services_request.json",
					OfferRequest.class);
			String serverDate = offerRequest.getServerDate();
			offerRequest.setServerDate(serverDate);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);
			List<CTCustomerContext> customerContextList = new ArrayList<>();
			CTCustomerContext ctCustomerContext = new CTCustomerContext();
			List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
			com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
			product.setPricePlanCode("88890295");
			product.setProductType(Constants.VIDEO_PLAN);
			products.add(product);
			ctCustomerContext.setProducts(products);
			customerContextList.add(ctCustomerContext);
			ctOfferRequest.setCustomerContext(customerContextList);
			offerRequestWrapper.setOfferRequest(offerRequest);
			offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
			ctOfferResponse = new DataReader().readFileToObj(
					"enabler/serverDate_getOffers_services_CtOfferResponse_selected.json", CTOfferResponse.class);
		} catch (Exception e) {
			// do nothing
		}
		when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
		when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
		when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
		when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(any())).thenCallRealMethod();
		
		CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(ctOfferResp);
		assertNotNull(ctOfferResp.getOffers());
		Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BOLTON-MLB-EXTRA-INNINGS-2023-V4_satellite".equalsIgnoreCase(p.getCode())).findFirst();

		assertTrue(ctOffer.isPresent());
		
		ProductObj product = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj();
		List<Price> prices = product.getVariants().get(0).getPrices();
		assertEquals(1, prices.size());

	}
	
	@Test
	public void testGetOffersEnablerWithPP() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ENABLER_WITH_PP, OfferRequest.class);
        	offerRequest.setHasLocalChannels(true);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88849754");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_PNP, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.hasLocalChannels(any())).thenCallRealMethod();
        when(pnpGroupUtils.getPnpGroup(any(), any(), any())).thenReturn("2023-PNP-1");
        when(satelliteCTOffersProcessor.filterInvalidPrices(any(), any(), any())).thenCallRealMethod();
        
        Mockito.lenient().when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BASE-PREFERRED-CHOICE-ALL-INCLUDED_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertNotNull(ctOffer.get());
        
        ProductObj product = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj();
        List<Price> prices = product.getVariants().get(0).getPrices();
		assertEquals(1, prices.size());
		assertEquals("2023-PNP-1", prices.get(0).getCustomerGroup());
		assertEquals(99, prices.get(0).getValue().getDollarAmount());
	}
	
	@Test
	public void testGetOffersEnablerWithoutPP() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ENABLER_NO_PP, OfferRequest.class);
        	offerRequest.setHasLocalChannels(true);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88849754");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_PNP, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.hasLocalChannels(any())).thenCallRealMethod();
        when(pnpGroupUtils.getPnpGroup(any(), any(), any())).thenReturn(null);
        when(satelliteCTOffersProcessor.filterInvalidPrices(any(), any(), any())).thenCallRealMethod();
        
        Mockito.lenient().when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BASE-PREFERRED-CHOICE-ALL-INCLUDED_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertNotNull(ctOffer.get());
        
        ProductObj product = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj();
        List<Price> prices = product.getVariants().get(0).getPrices();
		assertEquals(1, prices.size());
		assertEquals("04", prices.get(0).getCustomerGroup());
	}
	
	@Test
	public void testGetOffersEnablerLocals() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ENABLER_LOCALS, OfferRequest.class);
        	offerRequest.setHasLocalChannels(true);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88849914");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_LNL, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.hasLocalChannels(any())).thenCallRealMethod();
        when(util.isEligibleForServedMarket(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.filterInvalidPrices(any(), any(), any())).thenCallRealMethod();
        
        Mockito.lenient().when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BASE-PREMIER-ALL-INCLUDED-NO-LOCALS-LNL_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertTrue(ctOffer.isPresent());
        ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-LNL_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertTrue(ctOffer.isPresent());
        
	}
	
	@Test
	public void testGetOffersEnablerNoLocals() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ENABLER_NO_LOCALS, OfferRequest.class);
        	offerRequest.setHasLocalChannels(false);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88890335");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_LNL, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.hasLocalChannels(any())).thenCallRealMethod();
        when(util.isEligibleForServedMarket(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.filterInvalidPrices(any(), any(), any())).thenCallRealMethod();
        
        Mockito.lenient().when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BASE-PREMIER-ALL-INCLUDED-NO-LOCALS-LNL_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertFalse(ctOffer.isPresent());
        ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-LNL_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertFalse(ctOffer.isPresent());
        
	}
	
	@Test
	public void testGetOffersIPORWithMappedCCID() {
		
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(IPOR_CCID_OFFER_REQUEST, OfferRequest.class);
        	offerRequest.setHasLocalChannels(false);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88890335");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(IPOR_OFFER_RESPONSE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        assertTrue(ctOfferResp.getOffers().size()==1);
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BOLTON-HISTORY-VAULT_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertTrue(ctOffer.isPresent());
	}
	
	@Test
	public void testGetOffersIPORWithoutCCID() {
		
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(IPOR_OFFER_REQUEST, OfferRequest.class);
        	offerRequest.setHasLocalChannels(false);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88890335");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(IPOR_OFFER_RESPONSE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BOLTON-HISTORY-VAULT_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertFalse(ctOffer.isPresent());
        assertTrue(ctOfferResp.getOffers().size()==0);
	}
	
	@Test
	public void testGetOffersIPORWithIncorrectCCID() {
		
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(INCORRECT_CCID_OFFER_REQUEST, OfferRequest.class);
        	offerRequest.setHasLocalChannels(false);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88890335");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(IPOR_OFFER_RESPONSE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_BOLTON-HISTORY-VAULT_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertFalse(ctOffer.isPresent());
        assertTrue(ctOfferResp.getOffers().size()==0);
	}
	
	@Test
	public void testSTMSGetOffersWithCombinedRetentionAndOtherActionTypes() {
		
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTCustomerContext ctCustomerContext = new CTCustomerContext();
        try {
        	offerRequest = new DataReader().readFileToObj(COMBINED_RETENTION_CALL_REQUEST, OfferRequest.class);
        	offerRequest.setHasLocalChannels(false);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
            ctOfferRequest.setCustomerContext(new ArrayList<>());
            ctOfferRequest.getCustomerContext().add(ctCustomerContext);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(COMBINED_RETETNION_CALL_RESPONSE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        when(util.isEligibleForServedMarket(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        doCallRealMethod().when(satelliteCTOffersProcessor).applyBcodeExclusion(any(), any());
        
        when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> retention_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_PRO-LCL-DISP-10-OFF-1MO_satellite")).findFirst();
        
        Optional<CTOffer> bp_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_BASE-PREMIER-ALL-INCLUDED_satellite")).findFirst();
        
        Optional<CTOffer> addon_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_BOLTON-AMERICAS-PLUS_satellite")).findFirst();
        
        Optional<CTOffer> insurance_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_INSURANCE-PROTECTION-PLAN-V1_satellite")).findFirst();
        
        Optional<CTOffer> device_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_DEVICE-GENIE-MINI-WIRELESS_satellite")).findFirst();
        
        Optional<CTOffer> accessory_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_ACCESSORY-UNIVERSAL-REMOTE_satellite")).findFirst();
        
        assertTrue(retention_offer.isPresent() && bp_offer.isPresent() && addon_offer.isPresent() && insurance_offer.isPresent() 
        		&& device_offer.isPresent() && accessory_offer.isPresent());
	}
	
	@Test
	public void testEnablerGetOffersWithCombinedRetentionAndOtherActionTypes() {
		
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(CREDIT_OFFERS_WITH_OTHER_ACTION_TYPES_REQUEST, OfferRequest.class);
        	offerRequest.setHasLocalChannels(false);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(CREDIT_OFFERS_WITH_OTHER_ACTION_TYPES_RESPONSE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.getConflictingOfferIds(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> retention_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_PROMO-REACTIVE-CREDIT-10-OFF-12MOS_satellite")).findFirst();
        
        Optional<CTOffer> bp_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_BASE-PREMIER-NO-LOCALS_satellite")).findFirst();
        
        Optional<CTOffer> addon_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_BOLTON-AMERICAS-PLUS_satellite")).findFirst();
        
        Optional<CTOffer> insurance_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_INSURANCE-PROTECTION-PLAN_services_satellite")).findFirst();
        
        Optional<CTOffer> device_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_DEVICE-GENIE-MINI-WIRELESS_satellite")).findFirst();
        
        Optional<CTOffer> accessory_offer=ctOfferResp.getOffers().stream().filter(offer-> offer.getCode()
        		.equalsIgnoreCase("OF_ACCESSORY-UNIVERSAL-REMOTE_satellite")).findFirst();
        
        assertTrue(retention_offer.isPresent() && bp_offer.isPresent() && addon_offer.isPresent() && insurance_offer.isPresent() 
        		&& device_offer.isPresent() && accessory_offer.isPresent());
	}
	
	@Test
	public void testGetOffersTenureRuleEligible() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ENABLER_PSO_CREDIT, OfferRequest.class);
        	
        	LocalDate localDate = LocalDate.now();
        	localDate = localDate.minusDays(185);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String activationDate = localDate.format(formatter);        
            offerRequest.getCustomerContext().getSatellite().setActivationDate(activationDate);
            
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88890345");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
           com.dtv.dcp.epoch.model.ct.request.Product product1 = new com.dtv.dcp.epoch.model.ct.request.Product();
            product1.setPricePlanCode("88830604");
            product1.setPickCode("P0027");
            product1.setProductType(Constants.VIDEO_ADDON);
            products.add(product1);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_TENURE_RULE_ENABLER, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        Mockito.lenient().when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_PROMO_STARZ-HALF-OFF-3-MOS_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertTrue(ctOffer.isPresent());
        
	}
	
	@Test
	public void testGetOffersTenureRuleIneligible() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ENABLER_PSO_CREDIT, OfferRequest.class);
        	
        	LocalDate localDate = LocalDate.now();
        	localDate = localDate.minusDays(175);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String activationDate = localDate.format(formatter);        
            offerRequest.getCustomerContext().getSatellite().setActivationDate(activationDate);
            
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88890345");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
           com.dtv.dcp.epoch.model.ct.request.Product product1 = new com.dtv.dcp.epoch.model.ct.request.Product();
            product1.setPricePlanCode("88830604");
            product1.setPickCode("P0027");
            product1.setProductType(Constants.VIDEO_ADDON);
            products.add(product1);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_TENURE_RULE_ENABLER, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        Mockito.lenient().when(satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(any(), any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_PROMO_STARZ-HALF-OFF-3-MOS_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertFalse(ctOffer.isPresent());
        
	}
	
	@Test
    public void testGetOffersTenureCommissionDateInEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_STMS_PSO_CREDIT, OfferRequest.class);
        	
        	LocalDate localDate = LocalDate.now();
        	localDate = localDate.minusDays(265);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String commissionDate = localDate.format(formatter);        
            offerRequest.getCustomerContext().getSatellite().setCommissionStartDate(commissionDate);
            
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setBillingProductCode("P2141");
            product.setProductType(Constants.VIDEO_ADDON);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_TENURE_RULE_ENABLER, CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_PROMO_STARZ-HALF-OFF-3-MOS_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertFalse(ctOffer.isPresent());
		
    }
	
	@Test
    public void testGetOffersTenureCommissionDateEligible() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_STMS_PSO_CREDIT, OfferRequest.class);
        	
        	LocalDate localDate = LocalDate.now();
        	localDate = localDate.minusDays(275);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String commissionDate = localDate.format(formatter);        
            offerRequest.getCustomerContext().getSatellite().setCommissionStartDate(commissionDate);
            
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setBillingProductCode("P2141");
            product.setProductType(Constants.VIDEO_ADDON);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_TENURE_RULE_ENABLER, CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> "OF_PROMO_STARZ-HALF-OFF-3-MOS_satellite".equalsIgnoreCase(p.getCode())).findFirst();        
        assertTrue(ctOffer.isPresent());
		
    }
	
	@Test
    public void testGetOffersRemoveBPOffersWhenLCC() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_LCC_ON_ACCOUNT, OfferRequest.class);
        	
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(CREDIT_OFFERS_WITH_OTHER_ACTION_TYPES_RESPONSE, CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        
        CTOfferResponse ctOfferResp = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(ctOfferResp);
        assertNotNull(ctOfferResp.getOffers());
        Optional<CTOffer> ctOffer = ctOfferResp.getOffers().stream().filter(Objects::nonNull)
				.filter(p -> Constants.VIDEO_PLAN.equalsIgnoreCase(p.getAttributes().getOfferProductType())).findFirst();        
        assertFalse(ctOffer.isPresent());
		
    }

    @Test
    public void testGetOffersServiceVideoAddonForSubCategoryDisplayTypeFilter() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(SUBCATEGORY_SERVICE_ADDON_REQUEST, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);

            ctOfferResponse = new DataReader().readFileToObj(SUBCATEGORY_SERVICE_ADDON_RESPONSE, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        CustomerEligibility custEligibility = offerRequestWrapper.getOfferRequest().getCustomerEligibility();
        List<String> zipCode = custEligibility.getZipCode();
        List<String> county = custEligibility.getCounty();

        when(dmaLookUpService.getDMAValue(zipCode.get(0), county.get(0))).thenReturn(Arrays.asList("682"));
        when(cpopUCCClientHelper.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        doCallRealMethod().when(util).processOffers(any(),any());
        doCallRealMethod().when(satelliteCTOffersProcessor).applyBcodeExclusion(offerRequestWrapper,ctOfferResponse);

        CTOfferResponse videoAddonOffers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(videoAddonOffers);
        Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-BET-PLUS_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
        Assert.assertEquals("Popular", ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts()
                .get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getSubCategory());
        
        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-PEACOCK_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
//        Assert.assertEquals("hide", ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts()
//                .get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getDisplayType());

    }
    
    @Test
    public void testGetOffersLocalsRoadrunnerEnabler() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_LOCALS_RR_ENABLER, OfferRequest.class);
        	offerRequest.setHasLocalChannels(true);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		ctCustomerContext.setDtvSwimlane(offerRequest.getCustomerContext().getSatellite().getDtvSwimlane());
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("434913624");
            //product.setPriceCode("1");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_RR, CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(util.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
        when(util.isRoadrunner(Mockito.any())).thenCallRealMethod();
        when(util.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.filterInvalidPrices(any(), any(), any())).thenCallRealMethod();
        
        CTOfferResponse offers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(offers);
        assertNotNull(offers.getOffers());
        
        long count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN))
                .count();
        assertEquals(11, count);

        count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> util.isRoadrunner(offer))
                .count();
        assertEquals(11, count);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().equals("OF_BOLTON-LOCAL-CHANNELS-INCLUDED_satellite"))
                .findAny();
        assertTrue(ctOffer.isPresent());
		
    }
    
    @Test
    public void testGetOffersNoLocalsRoadrunnerEnabler() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_NO_LOCALS_RR_ENABLER, OfferRequest.class);
        	offerRequest.setHasLocalChannels(false);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		ctCustomerContext.setDtvSwimlane(offerRequest.getCustomerContext().getSatellite().getDtvSwimlane());
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("434913624");
            //product.setPriceCode("1");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_RR, CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(util.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
        when(util.isRoadrunner(Mockito.any())).thenCallRealMethod();
        when(util.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        
        CTOfferResponse offers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(offers);
        assertNotNull(offers.getOffers());
        
        long count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN))
                .count();
        assertEquals(11, count);

        count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> util.isRoadrunner(offer))
                .count();
        assertEquals(11, count);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().equals("OF_BOLTON-LOCAL-CHANNELS-INCLUDED_satellite"))
                .findAny();
        assertFalse(ctOffer.isPresent());
		
    }
    
    @Test
    public void testGetOffersLocalsBAUEnabler() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_LOCALS_BAU_ENABLER, OfferRequest.class);
        	offerRequest.setHasLocalChannels(true);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		ctCustomerContext.setPolicy(offerRequest.getCustomerContext().getSatellite().getPolicy());
    		ctCustomerContext.setDtvSwimlane(offerRequest.getCustomerContext().getSatellite().getDtvSwimlane());
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setPricePlanCode("88849674");
            product.setPriceCode("DVALL1");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_RR, CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(util.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
        when(util.isRoadrunner(Mockito.any())).thenCallRealMethod();
        when(util.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.filterInvalidPrices(any(), any(), any())).thenCallRealMethod();
        when(util.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(util.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();
        
        CTOfferResponse offers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(offers);
        assertNotNull(offers.getOffers());
        
        long count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN))
                .count();
        assertEquals(16, count);

        count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> util.isRoadrunner(offer))
                .count();
        assertEquals(0, count);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().equals("OF_BOLTON-LOCAL-CHANNELS-INCLUDED_satellite"))
                .findAny();
        assertFalse(ctOffer.isPresent());
		
    }
    
    @Test
    public void testGetOffersLocalsRoadrunner() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_LOCALS_RR, OfferRequest.class);
        	offerRequest.setHasLocalChannels(true);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
    		ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
    		ctCustomerContext.setNftvFlag(offerRequest.getCustomerContext().getSatellite().getNftvFlag());
    		ctCustomerContext.setFtvCount(offerRequest.getCustomerContext().getSatellite().getFtvCount());
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
            com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setBillingProductCode("P121334");
            product.setBillingReferenceID("1");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_RR, CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(util.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
        when(util.isRoadrunner(Mockito.any())).thenCallRealMethod();
        when(util.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.filterInvalidPrices(any(), any(), any())).thenCallRealMethod();
        
        CTOfferResponse offers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(offers);
        assertNotNull(offers.getOffers());
        
        long count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN))
                .count();
        assertEquals(11, count);

        count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> util.isRoadrunner(offer))
                .count();
        assertEquals(11, count);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().equals("OF_BOLTON-LOCAL-CHANNELS-INCLUDED_satellite"))
                .findAny();
        assertTrue(ctOffer.isPresent());
		
    }
    
    @Test
    public void testGetOffersNoLocalsRoadrunner() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_NO_LOCALS_RR, OfferRequest.class);
        	offerRequest.setHasLocalChannels(false);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
    		ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
    		ctCustomerContext.setNftvFlag(offerRequest.getCustomerContext().getSatellite().getNftvFlag());
    		ctCustomerContext.setFtvCount(offerRequest.getCustomerContext().getSatellite().getFtvCount());
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setBillingProductCode("P121334");
            product.setBillingReferenceID("1");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_RR, CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(util.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
        when(util.isRoadrunner(Mockito.any())).thenCallRealMethod();
        when(util.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        
        CTOfferResponse offers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(offers);
        assertNotNull(offers.getOffers());
        
        long count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN))
                .count();
        assertEquals(11, count);

        count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> util.isRoadrunner(offer))
                .count();
        assertEquals(11, count);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().equals("OF_BOLTON-LOCAL-CHANNELS-INCLUDED_satellite"))
                .findAny();
        assertFalse(ctOffer.isPresent());
		
    }
    
    @Test
    public void testGetOffersLocalsBAU() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_LOCALS_BAU, OfferRequest.class);
        	offerRequest.setHasLocalChannels(true);
        	BeanUtils.copyProperties(offerRequest, ctOfferRequest);
        	
        	List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
    		ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
    		ctCustomerContext.setNftvFlag(offerRequest.getCustomerContext().getSatellite().getNftvFlag());
    		ctCustomerContext.setFtvCount(offerRequest.getCustomerContext().getSatellite().getFtvCount());
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
           com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setBillingProductCode("P103351");
            product.setBillingReferenceID("1");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
        	
            offerRequestWrapper.setOfferRequest(offerRequest);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_RR, CTOfferResponse.class);
        } catch(Exception e) {
            //do nothing
        }
        when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
        when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
        when(util.isSTMSRequest(any())).thenCallRealMethod();
        when(util.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
        when(util.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
        when(util.isRoadrunner(Mockito.any())).thenCallRealMethod();
        when(util.getProductIdFromOffer(Mockito.any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.filterInvalidPrices(any(), any(), any())).thenCallRealMethod();
        when(util.hasLocalChannels(Mockito.any())).thenCallRealMethod();
        when(util.isEligibleForServedMarket(Mockito.any())).thenCallRealMethod();
        
        CTOfferResponse offers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(offers);
        assertNotNull(offers.getOffers());
        
        long count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getAttributes().getOfferProductType().equals(Constants.VIDEO_PLAN))
                .count();
        assertEquals(19, count);

        count = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> util.isRoadrunner(offer))
                .count();
        assertEquals(0, count);

        Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> offer.getCode().equals("OF_BOLTON-LOCAL-CHANNELS-INCLUDED_satellite"))
                .findAny();
        assertFalse(ctOffer.isPresent());
		
    }
    
	@Test
	public void testAdeOffersLiteResponse() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		OfferRequest offerRequest = new DataReader().readFileToObj("enabler/ade_offer_lite_request.json",
				OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setCtOfferRequest(ctOfferRequest);

		CTOfferResponse offerResponse = new DataReader().readFileToObj("enabler/ct_ade_offer_lite_response.json",
				CTOfferResponse.class);

		doReturn(offerResponse).when(cpopUCCClientHelper).getOffers(ctOfferRequest);
		CTOfferResponse response = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
		Optional<CTOffer> ctOffer = response.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_PROMO-REACTIVE-CREDIT-10-OFF-12MOS_satellite".equals(offer.getCode())
						&& Objects.nonNull(offer.getAttributes()))
				.findAny();
		assertEquals(
				"Enjoy $10 off for 12 mo. (save $120).",ctOffer.get().getDescription().getEn());
		assertEquals("DTV_Additional_DS2340", ctOffer.get().getAttributes().getEnablerSalesOfferId());
		assertEquals(0.0, ctOffer.get().getAttributes().getOfferPrice().getDollarAmount());
		assertEquals("$120 Savings ($10 credit on your monthly bill for 12 months)", ctOffer.get().getAttributes().getDisplayName().getEn());
		assertNull(ctOffer.get().getStartDate());
		assertNull(ctOffer.get().getStatus());
		assertNull(ctOffer.get().getEndDate());
		assertNotNull(response);
	}
	
	@Test
    public void testGetOffersServiceVideoAddonForSubCategoryPriceFilter() {
        OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            offerRequest = new DataReader().readFileToObj(SUBCATEGORY_SERVICE_ADDON_REQUEST, OfferRequest.class);
            offerRequest.setHasLocalChannels(false);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            List<CTCustomerContext> customerContextList = new ArrayList<>();
    		CTCustomerContext ctCustomerContext = new CTCustomerContext();
    		ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
    		ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
    		ctCustomerContext.setNftvFlag(offerRequest.getCustomerContext().getSatellite().getNftvFlag());
    		ctCustomerContext.setFtvCount(offerRequest.getCustomerContext().getSatellite().getFtvCount());
        	List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
        	com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
            product.setBillingProductCode("P102774");
            product.setBillingReferenceID("1");
            product.setProductType(Constants.VIDEO_PLAN);
            products.add(product);
            ctCustomerContext.setProducts(products);
    		customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
            
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);

            ctOfferResponse = new DataReader().readFileToObj(PRICE_SUBCATEGORY_ADDON_RESPONSE, CTOfferResponse.class);

        } catch (Exception e) {
            //do nothing
        }
        CustomerEligibility custEligibility = offerRequestWrapper.getOfferRequest().getCustomerEligibility();
        List<String> zipCode = custEligibility.getZipCode();
        List<String> county = custEligibility.getCounty();

        when(dmaLookUpService.getDMAValue(zipCode.get(0), county.get(0))).thenReturn(Arrays.asList("682"));
        when(cpopUCCClientHelper.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
        doCallRealMethod().when(util).processOffers(any(),any());
        doCallRealMethod().when(satelliteCTOffersProcessor).applyBcodeExclusion(offerRequestWrapper,ctOfferResponse);

        CTOfferResponse videoAddonOffers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(videoAddonOffers);
        Optional<CTOffer> ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-PEACOCK-V3_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
        assertEquals("EmbeddedSVOD", ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts()
                .get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getSubCategory());
        
        ctOffer = videoAddonOffers.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-PEACOCK_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes()))
                .findAny();
        assertEquals("Streaming", ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts()
                .get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getSubCategory());

	}

	//@Test
	public void testGetOffersQualIncomFilterON() {
		OfferRequest offerRequest = null;
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SERVICES, OfferRequest.class);
			offerRequest.setHasLocalChannels(true);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);

			List<CTCustomerContext> customerContextList = new ArrayList<>();
			CTCustomerContext ctCustomerContext = new CTCustomerContext();
			ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
			ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
			ctCustomerContext.setNftvFlag(offerRequest.getCustomerContext().getSatellite().getNftvFlag());
			ctCustomerContext.setFtvCount(offerRequest.getCustomerContext().getSatellite().getFtvCount());
			List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
			com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
			product.setBillingProductCode("P121334");
			product.setBillingReferenceID("1");
			product.setProductType(Constants.VIDEO_PLAN);
			products.add(product);
			ctCustomerContext.setProducts(products);
			customerContextList.add(ctCustomerContext);
			ctOfferRequest.setCustomerContext(customerContextList);

			offerRequestWrapper.setOfferRequest(offerRequest);
			offerRequestWrapper.setCtOfferRequest(ctOfferRequest);

			ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SERVICES, CTOfferResponse.class);
		} catch(Exception e) {
			//do nothing
		}
		when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
		when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
		when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
		when(featureManagerHelper.isEnabled(any())).thenReturn(true);

		CTOfferResponse offers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offers);
		assertNotNull(offers.getOffers());

		Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> offer.getCode().equals("OF_BOLTON-PEACOCK-V3_satellite"))
				.findAny();
		assertFalse(ctOffer.isPresent());

		ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> offer.getCode().equals("OF_BOLTON-MEP-PEACOCK-BUNDLE_satellite"))
				.findAny();
		assertTrue(ctOffer.isPresent());

	}

	//@Test
	public void testGetOffersQualIncomFilterOFF() {
		OfferRequest offerRequest = null;
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SERVICES, OfferRequest.class);
			offerRequest.setHasLocalChannels(true);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);

			List<CTCustomerContext> customerContextList = new ArrayList<>();
			CTCustomerContext ctCustomerContext = new CTCustomerContext();
			ctCustomerContext.setBusinessSegment(offerRequest.getCustomerContext().getSatellite().getBusinessSegment());
			ctCustomerContext.setEmployeeSegment(offerRequest.getCustomerContext().getSatellite().getEmployeeSegment());
			ctCustomerContext.setNftvFlag(offerRequest.getCustomerContext().getSatellite().getNftvFlag());
			ctCustomerContext.setFtvCount(offerRequest.getCustomerContext().getSatellite().getFtvCount());
			List<com.dtv.dcp.epoch.model.ct.request.Product> products = new ArrayList<>();
			com.dtv.dcp.epoch.model.ct.request.Product product = new com.dtv.dcp.epoch.model.ct.request.Product();
			product.setBillingProductCode("P121334");
			product.setBillingReferenceID("1");
			product.setProductType(Constants.VIDEO_PLAN);
			products.add(product);
			ctCustomerContext.setProducts(products);
			customerContextList.add(ctCustomerContext);
			ctOfferRequest.setCustomerContext(customerContextList);

			offerRequestWrapper.setOfferRequest(offerRequest);
			offerRequestWrapper.setCtOfferRequest(ctOfferRequest);

			ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_SERVICES, CTOfferResponse.class);
		} catch(Exception e) {
			//do nothing
		}
		when(cpopUCCClientHelper.getOffers(any())).thenReturn(ctOfferResponse);
		when(util.isNotValidBasedOnBillingSystem(any(), any())).thenCallRealMethod();
		when(util.isSTMSRequest(any())).thenCallRealMethod();
		when(util.getProductObjFromOffer(Mockito.any())).thenCallRealMethod();
		when(util.isLocalChannelVideoAddon(Mockito.any())).thenCallRealMethod();
		when(featureManagerHelper.isEnabled("svc-epoch-ade-cache-perf-change")).thenReturn(true);
		when(featureManagerHelper.isEnabled(Constants.FEATURE_QUALIFIER_INCOMP_FILTER_ENABLED)).thenReturn(false);
		when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)).thenReturn(false);

		CTOfferResponse offers = satelliteServicesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offers);
		assertNotNull(offers.getOffers());

		Optional<CTOffer> ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> offer.getCode().equals("OF_BOLTON-PEACOCK-V3_satellite"))
				.findAny();
		assertTrue(ctOffer.isPresent());

		ctOffer = offers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> offer.getCode().equals("OF_BOLTON-MEP-PEACOCK-BUNDLE_satellite"))
				.findAny();
		assertTrue(ctOffer.isPresent());


	}

}