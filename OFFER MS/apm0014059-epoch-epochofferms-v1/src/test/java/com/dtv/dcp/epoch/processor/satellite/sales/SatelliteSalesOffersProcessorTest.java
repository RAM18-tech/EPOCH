package com.dtv.dcp.epoch.processor.satellite.sales;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessor;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class SatelliteSalesOffersProcessorTest {
	
	@InjectMocks
	private SatelliteSalesOffersProcessor satelliteSalesOffersProcessor;
	
	@Mock
	SatelliteSalesVideoDeviceProcessor salesVideoDeviceProcessor;
	
	@Mock
	SatelliteSalesFeeProcessor satelliteSalesFeeProcessor;
	
	@InjectMocks
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;
	
	@Mock
	SatelliteSalesVideoPlanProcessor salesVideoPlanProcessor;

	@Mock
	SalesVideoAddonProcessor salesVideoAddonProcessor;
	
	@Mock
	SatelliteSalesInsuranceProcessor salesInsuranceProcessor;
	
	@InjectMocks
	CpopClientHelper cpopClientHelper;
	
	@Mock
	SatelliteServicesOffersProcessor satelliteServicesOffersProcessor;
	
	@Mock
	OffersUtils util;
	
	@Mock
	private DMALookUpService dmaLookUpService;
	
	@Mock
    private FeatureManagerHelper featureHelper;
	
	@Mock
	private RedisCacheHelper redisCacheHelper;

	@Spy
	ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@BeforeEach
    public void init() {
		satelliteCTOffersProcessor = Mockito.spy(new SatelliteCTOffersProcessor());
    	cpopClientHelper = Mockito.spy(new CpopClientHelper());
        MockitoAnnotations.openMocks(this);
    }
	
	private static String OFFER_REQUEST_DEVICE = "{\r\n" + 
			"  \"customerEligibility\": {\r\n" + 
			"    \"zipCode\": [\r\n" + 
			"      \"00058\"\r\n" + 
			"    ],\r\n" + 
			"    \"county\": [\r\n" + 
			"      \"17000\"\r\n" + 
			"    ]\r\n" + 
			"  },\r\n" + 
			"  \"offerActionType\": [\r\n" + 
			"    \"Acquisition\"\r\n" + 
			"  ],\r\n" + 
			"  \"salesChannel\": [\r\n" + 
			"    \"online\"\r\n" + 
			"  ],\r\n" + 
			"  \"offerProductTypes\": [\r\n" + 
			"	\"fee\",\r\n" + 
			"    \"video-device\"\r\n" + 
			"  ],\r\n" + 
			"  \"offerProductFamily\": [\r\n" + 
			"    \"satellite\"\r\n" + 
			"  ],\r\n" + 
			"  \"cartContext\": {\r\n" + 
			"    \"epochOfferCodes\": [\r\n" + 
			"      \"OF_BASE-PREMIER-ALL-INCLUDED_sales_satellite\"\r\n" + 
			"    ]\r\n" + 
			"  }\r\n" + 
			"}\r\n" + 
			"";
	
	private static String OFFER_REQUEST_BP = "{\r\n" + 
			"  \"customerEligibility\": {\r\n" + 
			"    \"zipCode\": [\r\n" + 
			"      \"00058\"\r\n" + 
			"    ],\r\n" + 
			"    \"county\": [\r\n" + 
			"      \"17000\"\r\n" + 
			"    ]\r\n" + 
			"  },\r\n" + 
			"  \"offerActionType\": [\r\n" + 
			"    \"Acquisition\"\r\n" + 
			"  ],\r\n" + 
			"  \"salesChannel\": [\r\n" + 
			"    \"online\"\r\n" + 
			"  ],\r\n" + 
			"  \"offerProductTypes\": [\r\n" + 
			"    \"video-plan\"\r\n" + 
			"  ],\r\n" + 
			"  \"offerProductFamily\": [\r\n" + 
			"    \"satellite\"\r\n" + 
			"  ]\r\n" + 
			"}";
	
	private static String OFFER_REQUEST_ADDON = "{\r\n" + 
			"  \"customerEligibility\": { \r\n" + 
			"        \"zipCode\": [\r\n" + 
			"            \"42101\"\r\n" + 
			"        ],\r\n" + 
			"        \"county\": [\r\n" + 
			"            \"21031\"\r\n" + 
			"        ]\r\n" + 
			"    },\r\n" + 
			"  \"salesChannel\": [\r\n" + 
			"    \"online\"\r\n" + 
			"  ],\r\n" + 
			"  \"offerProductTypes\": [\r\n" + 
			"    \"video-addon\"\r\n" + 
			"  ],\r\n" + 
			"  \"offerActionType\": [\r\n" + 
			"    \"Acquisition\"\r\n" + 
			"  ],\r\n" + 
			"  \"offerProductFamily\": [\r\n" + 
			"    \"satellite\"\r\n" + 
			"  ],\r\n" + 
			"  \"cartContext\": {\r\n" + 
			"    \"epochOfferCodes\": [\r\n" + 
			"      \"OF_BASE-PREMIER-ALL-INCLUDED_sales_satellite\"\r\n" + 
			"    ]\r\n" + 
			"  }\r\n" + 
			"}";
	
	private static String OFFER_REQUEST_INSURANCE = "{\r\n" + 
			"  \"customerEligibility\": {\r\n" + 
			"    \"zipCode\": [\r\n" + 
			"      \"00058\"\r\n" + 
			"    ],\r\n" + 
			"    \"county\": [\r\n" + 
			"      \"17000\"\r\n" + 
			"    ]\r\n" + 
			"  },\r\n" + 
			"  \"offerActionType\": [\r\n" + 
			"    \"Acquisition\"\r\n" + 
			"  ],\r\n" + 
			"  \"salesChannel\": [\r\n" + 
			"    \"salesCRM\"\r\n" + 
			"  ],\r\n" + 
			"  \"offerProductTypes\": [\r\n" + 
			"    \"insurance\"\r\n" + 
			"  ],\r\n" + 
			"  \"offerProductFamily\": [\r\n" + 
			"    \"satellite\"\r\n" + 
			"  ]\r\n" + 
			"}";
	
	private static final String OFFER_REQUEST_DEVICE_AGENT = "acquisition/offer_request_device_agent.json";
	
	private static final String OFFER_REQUEST_DEVICE_ASSISTED_SALES = "acquisition/offer_request_device_assisted_sales.json";
	
	private static final String OFFER_REQUEST_ORDER_MOD_DEVICE = "stms/offer_request_order_mod_device.json";
	private static final String OFFER_REQUEST_SECOND_ORDER_MOD_DEVICE = "stms/offer_request_second_order_mod_device.json";
	private static final String OFFER_RESPONSE_ORDER_MOD_DEVICE = "stms/offer_response_order_mod_device.json";
	
	private static final String OFFER_REQUEST_ORDER_MOD_DEVICE_ELIGIBLE = "stms/offer_request_order_mod_device_eligible.json";
	private static final String OFFER_REQUEST_ORDER_MOD_DEVICE_NO_REBATE = "stms/offer_request_order_mod_device_no_rebate.json";
	private static final String OFFER_RESPONSE_ORDER_MOD_DEVICE_NO_REBATE = "stms/offer_response_order_mod_device_no_rebate.json";
	
	private static final String OFFER_REQUEST_ORDER_MOD_DEVICE_ONLINE_OFF = "stms/offer_request_order_mod_device_online_off.json";
	private static final String OFFER_REQUEST_ORDER_MOD_DEVICE_ONLINE = "stms/offer_request_order_mod_device_online.json";
	private static final String OFFER_REQUEST_ORDER_MOD_DEVICE_DIP = "stms/offer_request_order_mod_device_dip.json";
	
	private static final String OFFER_REQUEST_REWARD = "acquisition/offer_request_reward.json";
	private static final String OFFER_REQUEST_REWARD_INELIGIBLE = "acquisition/offer_request_reward_ineligible.json";
	
	private static final String OFFER_REQUEST_REWARD_MSC_ELIGIBLE = "acquisition/offer_request_reward_msc_eligible.json";
	private static final String OFFER_REQUEST_REWARD_MSC_INELIGIBLE = "acquisition/offer_request_reward_msc_ineligible.json";
	
	private static final String OFFER_REQUEST_CLOSING = "acquisition/offer_request_closing.json";
	private static final String OFFER_REQUEST_CLOSING_INELIGIBLE = "acquisition/offer_request_closing_ineligible.json";
	
	private static final String OFFER_REQUEST_BP_ISP_ELIGIBLE = "acquisition/offer_request_bp_isp_eligible.json";
	private static final String OFFER_REQUEST_BP_ISP_INELIGIBLE_SALESCHANNEL = "acquisition/offer_request_bp_isp_ineligible_saleschannel.json";
	private static final String OFFER_REQUEST_BP_ISP_INELIGIBLE_DEALERCODE = "acquisition/offer_request_bp_isp_ineligible_dealercode.json";
	private static final String OFFER_RESPONSE_BP_ISP = "acquisition/offer_response_bp_isp.json";
	
	private static final String offer_request_MDU_DTH_eligibleDealerCode = "acquisition/offer_request_MDU-DTH_eligibleDealerCode.json";
	private static final String offers_response_MDU_DTH="acquisition/offers_response_MDU-DTH.json";
	private static final String offer_request_MDU_DTH_ineligibleDealerCode = "acquisition/offer_request_MDU-DTH_ineligibleDealerCode.json";
	private static final String offer_request_MDU_DTH_noDealerCode = "acquisition/offer_request_MDU-DTH_noDealerCode.json";
	private static final String offer_request_MDU_DTH_invalidSalesChannel = "acquisition/offer_request_MDU-DTH_invalidSalesChannel.json";
	private static final String offer_request_MDU_DTH_invalidSalesSubChannel = "acquisition/offer_request_MDU-DTH_invalidSalesSubChannel.json";
	
	private static final String offer_request_blockedDealer="acquisition/offer_request_blockedDealer.json";
	private static final String offer_request_salesSubChannel="acquisition/offer_request_blocked_salesSubChannel.json";
	
	@Test
	public void testGetOffersVideoDevice() {
		ObjectMapper objectMapper = new ObjectMapper();
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = objectMapper.readValue(OFFER_REQUEST_DEVICE, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device.json", CTOfferResponse.class);
		CTOfferResponse feeOffers = new DataReader().readFileToObj("acquisition/tv-access-fee.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		
		when(satelliteSalesFeeProcessor.getSatelliteFeeOffers(any(), any(), any())).thenReturn(feeOffers);
		when(salesVideoDeviceProcessor.getSatelliteVideoDeviceOffers(any(), any())).thenReturn(videoDeviceOffers);
		when(satelliteCTOffersProcessor.associateFeeWithDevice(any(), any(), any())).thenReturn(videoDeviceOffers.getOffers());
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& (Objects.nonNull(offer.getAttributes().getContractPrice())
								|| Objects.nonNull(offer.getAttributes().getSpecialPromoPrice())))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& (Objects.isNull(offer.getAttributes().getOfferPrice()) 
								|| Objects.isNull(offer.getAttributes().getDiscountAmount())))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getDiscountAmount().getDollarAmount() == 300)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getDiscountAmount().getDollarAmount() == 0)
				.findAny();
		assertTrue(ctOffer.isPresent());
	}
	
	@Test
	public void testGetOffersVideoDeviceAgent() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_DEVICE_AGENT, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-agent.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		
		when(salesVideoDeviceProcessor.getSatelliteVideoDeviceOffers(any(), any())).thenReturn(videoDeviceOffers);
		when(satelliteCTOffersProcessor.getHardwareInstantRebateOffers(any(), any(), any())).thenReturn(videoDeviceOffers.getOffers());
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& (Objects.nonNull(offer.getAttributes().getContractPrice())
								|| Objects.nonNull(offer.getAttributes().getSpecialPromoPrice())
								|| Objects.nonNull(offer.getAttributes().getOfferPrice())
								|| Objects.nonNull(offer.getAttributes().getDiscountAmount())))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
	}
	
	@Test
	public void testGetOffersVideoDeviceAssistedSales() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_DEVICE_ASSISTED_SALES, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-agent.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		
		when(salesVideoDeviceProcessor.getSatelliteVideoDeviceOffers(any(), any())).thenReturn(videoDeviceOffers);
		when(satelliteCTOffersProcessor.getHardwareInstantRebateOffers(any(), any(), any())).thenReturn(videoDeviceOffers.getOffers());
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& (Objects.nonNull(offer.getAttributes().getContractPrice())
								|| Objects.nonNull(offer.getAttributes().getSpecialPromoPrice())
								|| Objects.nonNull(offer.getAttributes().getOfferPrice())
								|| Objects.nonNull(offer.getAttributes().getDiscountAmount())))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
	}
	
	@Test
	public void testGetOffersVideoPlan() {
		ObjectMapper objectMapper = new ObjectMapper();
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = objectMapper.readValue(OFFER_REQUEST_BP, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoPlanOffers = new DataReader().readFileToObj("acquisition/offer_response_video_plan.json", CTOfferResponse.class);
		
		when(salesVideoPlanProcessor.getSatelliteVideoPlanOffers(any())).thenReturn(videoPlanOffers);
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull).filter(
				offer -> Objects.nonNull(offer.getAttributes()) && (offer.getAttributes().getContractPrice() == null
						|| offer.getAttributes().getSpecialPromoPrice() == null
						|| offer.getAttributes().getOfferPrice() == null
						|| offer.getAttributes().getDiscountAmount() == null ))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getDiscountAmount().getDollarAmount() == 69.01 && offer.getAttributes().getIsLCCBasePackage() == false)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BASE-MAX-ALL-INCLUDED_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getDiscountAmount().getDollarAmount() == 0)
				.findAny();
		assertTrue(ctOffer.isPresent());
	}
	
	@Test
	public void testGetOffersVideoPlanISPEligible() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_BP_ISP_ELIGIBLE, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoPlanOffers = new DataReader().readFileToObj(OFFER_RESPONSE_BP_ISP, CTOfferResponse.class);
		
		when(salesVideoPlanProcessor.getSatelliteVideoPlanOffers(any())).thenReturn(videoPlanOffers);
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull).filter(
				offer -> Objects.nonNull(offer.getAttributes()) && (offer.getAttributes().getContractPrice() == null
						|| offer.getAttributes().getSpecialPromoPrice() == null
						|| offer.getAttributes().getOfferPrice() == null
						|| offer.getAttributes().getDiscountAmount() == null ))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getDiscountAmount().getDollarAmount() == 62.01)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		Optional<Benefit> benefit = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.filter(promo -> "PROMO_5-OFF-24-MOS_satellite".equals(promo.getCode())).findAny();
		assertTrue(benefit.isPresent());
		
	}
	
	@Test
	public void testGetOffersVideoPlanISPIneligibleSaleschannel() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_BP_ISP_INELIGIBLE_SALESCHANNEL, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoPlanOffers = new DataReader().readFileToObj(OFFER_RESPONSE_BP_ISP, CTOfferResponse.class);
		
		when(salesVideoPlanProcessor.getSatelliteVideoPlanOffers(any())).thenReturn(videoPlanOffers);
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull).filter(
				offer -> Objects.nonNull(offer.getAttributes()) && (offer.getAttributes().getContractPrice() == null
						|| offer.getAttributes().getSpecialPromoPrice() == null
						|| offer.getAttributes().getOfferPrice() == null
						|| offer.getAttributes().getDiscountAmount() == null ))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getDiscountAmount().getDollarAmount() == 57.01)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		Optional<Benefit> benefit = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.filter(promo -> "PROMO_5-OFF-24-MOS_satellite".equals(promo.getCode())).findAny();
		assertFalse(benefit.isPresent());
		
	}
	
	@Test
	public void testGetOffersVideoPlanEligibleBillingSystemLNL() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj("enabler/offer_request_enabler_sales_locals.json", OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoPlanOffers = new DataReader().readFileToObj("stms/offer_response_sales_lnl.json", CTOfferResponse.class);
		
		when(salesVideoPlanProcessor.getSatelliteVideoPlanOffers(any())).thenReturn(videoPlanOffers);
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		Mockito.doCallRealMethod().when(util).isSTMSRequest(any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BASE-PREMIER-ALL-INCLUDED-NO-LOCALS-LNL_sales_satellite".equals(offer.getCode()))
				.findAny();
        assertTrue(ctOffer.isPresent());
        
        Optional<Benefit> benefit = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.filter(bft -> "PROMO_NO-LOCALS-SAVINGS_satellite".equals(bft.getCode())).findAny();
        assertTrue(benefit.isPresent());
        
        ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-LNL_sales_satellite".equals(offer.getCode()))
				.findAny();
        assertTrue(ctOffer.isPresent());
        
        benefit = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.filter(bft -> "PROMO_NO-LOCALS-SAVINGS_satellite".equals(bft.getCode())).findAny();
        assertTrue(benefit.isPresent());
		
	}
	
	@Test
	public void testGetOffersVideoPlanIneligibleBillingSystemLNL() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj("stms/offer_request_stms_sales_locals.json", OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoPlanOffers = new DataReader().readFileToObj("stms/offer_response_sales_lnl.json", CTOfferResponse.class);
		
		when(salesVideoPlanProcessor.getSatelliteVideoPlanOffers(any())).thenReturn(videoPlanOffers);
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		Mockito.doCallRealMethod().when(util).isSTMSRequest(any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BASE-PREMIER-ALL-INCLUDED-NO-LOCALS-LNL_sales_satellite".equals(offer.getCode()))
				.findAny();
        assertTrue(ctOffer.isPresent());
        
        Optional<Benefit> benefit = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.filter(bft -> "PROMO_NO-LOCALS-SAVINGS_satellite".equals(bft.getCode())).findAny();
        assertFalse(benefit.isPresent());
        
        ctOffer = videoPlanOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS-LNL_sales_satellite".equals(offer.getCode()))
				.findAny();
        assertTrue(ctOffer.isPresent());
        
        benefit = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.filter(bft -> "PROMO_NO-LOCALS-SAVINGS_satellite".equals(bft.getCode())).findAny();
        assertFalse(benefit.isPresent());
		
	}
	
	@Test
	public void testGetOffersVideoPlanISPIneligibleDealerCode() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_BP_ISP_INELIGIBLE_DEALERCODE, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoPlanOffers = new DataReader().readFileToObj(OFFER_RESPONSE_BP_ISP, CTOfferResponse.class);
		
		when(salesVideoPlanProcessor.getSatelliteVideoPlanOffers(any())).thenReturn(videoPlanOffers);
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull).filter(
				offer -> Objects.nonNull(offer.getAttributes()) && (offer.getAttributes().getContractPrice() == null
						|| offer.getAttributes().getSpecialPromoPrice() == null
						|| offer.getAttributes().getOfferPrice() == null
						|| offer.getAttributes().getDiscountAmount() == null ))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BASE-ULTIMATE-ALL-INCLUDED_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getDiscountAmount().getDollarAmount() == 57.01)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		Optional<Benefit> benefit = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.filter(promo -> "PROMO_5-OFF-24-MOS_satellite".equals(promo.getCode())).findAny();
		assertFalse(benefit.isPresent());
		
	}
	
	@Test
	public void testGetOffersVideoAddon() {
		ObjectMapper objectMapper = new ObjectMapper();
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = objectMapper.readValue(OFFER_REQUEST_ADDON, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoAddonOffers = new DataReader().readFileToObj("acquisition/offer_response_video_addon.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		
		when(salesVideoAddonProcessor.getSatelliteVideoAddonOffersForAcquisition(any(), any())).thenReturn(videoAddonOffers);
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& (Objects.nonNull(offer.getAttributes().getContractPrice())
								|| Objects.nonNull(offer.getAttributes().getSpecialPromoPrice())))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& (Objects.isNull(offer.getAttributes().getOfferPrice()) 
								|| Objects.isNull(offer.getAttributes().getDiscountAmount())))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BOLTON-HBO-SHOW-STARZ-CINE-EPIX_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getDiscountAmount().getDollarAmount() == 53.95)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BOLTON-EPIX_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getDiscountAmount().getDollarAmount() == 0)
				.findAny();
		assertTrue(ctOffer.isPresent());
	}
	
	@Test
	public void testGetOffersInsurance() {
		ObjectMapper objectMapper = new ObjectMapper();
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = objectMapper.readValue(OFFER_REQUEST_INSURANCE, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse insuranceOffers = new DataReader().readFileToObj("acquisition/offer_response_insurance.json", CTOfferResponse.class);
		
		when(salesInsuranceProcessor.getSatelliteInsuranceOffers(any(), any())).thenReturn(insuranceOffers);
		Mockito.doReturn(null).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& (Objects.nonNull(offer.getAttributes().getContractPrice())
								|| Objects.nonNull(offer.getAttributes().getSpecialPromoPrice())))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
		ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& (Objects.isNull(offer.getAttributes().getOfferPrice()) 
								|| Objects.isNull(offer.getAttributes().getDiscountAmount())))
				.findAny();
		assertFalse(ctOffer.isPresent());
		
	}
	
	@Test
	public void testApplyOrderModChangesVideoDevice() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_DEVICE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_DEVICE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        Mockito.doReturn(selectedBp).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
        Mockito.doReturn(ctOfferResponse).when(salesVideoDeviceProcessor).getSatelliteVideoDeviceOffers(any(), any());
        Mockito.doReturn(ctOfferResponse.getOffers()).when(satelliteCTOffersProcessor).getHardwareInstantRebateOffers(any(), any(), any());
        
        CTOfferResponse videoDeviceOffers = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(videoDeviceOffers);
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 1)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 3)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		long count = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE)
				.count();
		assertEquals(2, count);
		
		assertNull(videoDeviceOffers.getIneligibleOffers());
		
	}
	
	@Test
	public void testApplyOrderModChangesVideoDeviceEligible() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_DEVICE_ELIGIBLE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_DEVICE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        Mockito.doReturn(selectedBp).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
        Mockito.doReturn(ctOfferResponse).when(salesVideoDeviceProcessor).getSatelliteVideoDeviceOffers(any(), any());
        Mockito.doReturn(ctOfferResponse.getOffers()).when(satelliteCTOffersProcessor).getHardwareInstantRebateOffers(any(), any(), any());
        
        CTOfferResponse videoDeviceOffers = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(videoDeviceOffers);
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 1)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 3)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		long count = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE)
				.count();
		assertEquals(2, count);
		
		assertNull(videoDeviceOffers.getIneligibleOffers());
		
	}
	
	@Test
	public void testApplyOrderModChangesVideoDeviceNoRebate() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_DEVICE_NO_REBATE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_DEVICE_NO_REBATE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        Mockito.doReturn(selectedBp).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
        Mockito.doReturn(ctOfferResponse).when(salesVideoDeviceProcessor).getSatelliteVideoDeviceOffers(any(), any());
        Mockito.doReturn(ctOfferResponse.getOffers()).when(satelliteCTOffersProcessor).getHardwareInstantRebateOffers(any(), any(), any());
        
        CTOfferResponse videoDeviceOffers = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(videoDeviceOffers);
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 1)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 3)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		long count = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE)
				.count();
		assertEquals(2, count);
		
		assertNull(videoDeviceOffers.getIneligibleOffers());
		
	}
	
	@Test
	public void testGetOffersRewardCard() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_REWARD, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse rewardCardOffers = new DataReader().readFileToObj("acquisition/offer_response_reward.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		CTOffer videoPlan = new DataReader().readFileToObj("acquisition/video-plan-selected-ultimate.json", CTOffer.class);
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(videoPlan);
		selectedOffer.setOffers(selectedOffers);
		
		Mockito.doReturn(rewardCardOffers).when(cpopClientHelper).getOffers(any());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).getSatelliteRewardCardOffers(any(), any());
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		Mockito.doCallRealMethod().when(satelliteServicesOffersProcessor).getIncompatibleProducts(any());
		Mockito.doCallRealMethod().when(util).getProductObjFromOffer(any());
		Mockito.doCallRealMethod().when(util).getProductIdFromOffer(any());
		when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertEquals(1, offersResponse.getOffers().size());
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_REWARD-250_satellite".equals(offer.getCode()))
				.findAny();
		assertTrue(ctOffer.isPresent());		
		
	}
	
	@Test
	public void testGetOffersRewardCardIneligibleBP() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_REWARD_INELIGIBLE, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse rewardCardOffers = new DataReader().readFileToObj("acquisition/offer_response_reward.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		CTOffer videoPlan = new DataReader().readFileToObj("acquisition/video-plan-selected.json", CTOffer.class);
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(videoPlan);
		selectedOffer.setOffers(selectedOffers);
		
		Mockito.doReturn(rewardCardOffers).when(cpopClientHelper).getOffers(any());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).getSatelliteRewardCardOffers(any(), any());
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteServicesOffersProcessor).getIncompatibleProducts(any());
		Mockito.doCallRealMethod().when(util).getProductObjFromOffer(any());
		Mockito.doCallRealMethod().when(util).getProductIdFromOffer(any());
		when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertNull(offersResponse.getOffers());
		
	}
	
	@Test
	public void testApplyOrderModChangesVideoDeviceSecond() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SECOND_ORDER_MOD_DEVICE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_DEVICE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        Mockito.doReturn(selectedBp).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
        Mockito.doReturn(ctOfferResponse).when(salesVideoDeviceProcessor).getSatelliteVideoDeviceOffers(any(), any());
        Mockito.doReturn(ctOfferResponse.getOffers()).when(satelliteCTOffersProcessor).getHardwareInstantRebateOffers(any(), any(), any());
        
        CTOfferResponse videoDeviceOffers = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(videoDeviceOffers);
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 1)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GEMINI-WIRELESS-V2_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 4)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		long count = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE)
				.count();
		assertEquals(2, count);
		
		assertNull(videoDeviceOffers.getIneligibleOffers());
		
	}
	
	@Test
	public void testGetOffersClosingCredit() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_CLOSING, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse rewardCardOffers = new DataReader().readFileToObj("acquisition/offer_response_closing.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		CTOffer videoPlan = new DataReader().readFileToObj("acquisition/video-plan-selected-ultimate.json", CTOffer.class);
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(videoPlan);
		selectedOffer.setOffers(selectedOffers);
		
		Mockito.doReturn(rewardCardOffers).when(cpopClientHelper).getOffers(any());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).getSatelliteClosingOffers(any(), any());
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		Mockito.doCallRealMethod().when(satelliteServicesOffersProcessor).getIncompatibleProducts(any());
		Mockito.doCallRealMethod().when(util).getProductObjFromOffer(any());
		Mockito.doCallRealMethod().when(util).getProductIdFromOffer(any());
		when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertEquals(1, offersResponse.getOffers().size());
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_PROMO-OVERLAY-CREDIT-10-OFF-24-MOS_satellite".equals(offer.getCode()))
				.findAny();
		assertTrue(ctOffer.isPresent());		
		
	}
	
	@Test
	public void testGetOffersClosingCreditIneligibleBP() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_CLOSING_INELIGIBLE, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse rewardCardOffers = new DataReader().readFileToObj("acquisition/offer_response_closing.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		CTOffer videoPlan = new DataReader().readFileToObj("acquisition/video-plan-selected-entertainment.json", CTOffer.class);
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(videoPlan);
		selectedOffer.setOffers(selectedOffers);
		
		Mockito.doReturn(rewardCardOffers).when(cpopClientHelper).getOffers(any());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).getSatelliteClosingOffers(any(), any());
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteServicesOffersProcessor).getIncompatibleProducts(any());
		Mockito.doCallRealMethod().when(util).getProductObjFromOffer(any());
		Mockito.doCallRealMethod().when(util).getProductIdFromOffer(any());
		when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertNull(offersResponse.getOffers());
		
	}
	
	@Test
	public void testGetOffersRewardCardMscEligible() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_REWARD_MSC_ELIGIBLE, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse rewardCardOffers = new DataReader().readFileToObj("acquisition/offer_response_reward_msc.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		CTOffer videoPlan = new DataReader().readFileToObj("acquisition/video-plan-selected-ultimate.json", CTOffer.class);
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(videoPlan);
		selectedOffer.setOffers(selectedOffers);
		
		Mockito.doReturn(rewardCardOffers).when(cpopClientHelper).getOffers(any());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).getSatelliteRewardCardOffers(any(), any());
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateBestPrice(any(), any(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).calculateDiscountAmount(any(), any());
		Mockito.doCallRealMethod().when(util).getTwoDigitRoundOffValue(any());
		Mockito.doCallRealMethod().when(satelliteServicesOffersProcessor).getIncompatibleProducts(any());
		Mockito.doCallRealMethod().when(util).getProductObjFromOffer(any());
		Mockito.doCallRealMethod().when(util).getProductIdFromOffer(any());
		when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertEquals(1, offersResponse.getOffers().size());
		
		Optional<CTOffer> ctOffer = offersResponse.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_REWARD-400-V1_satellite".equals(offer.getCode()))
				.findAny();
		assertTrue(ctOffer.isPresent());		
		
	}
	
	@Test
	public void testGetOffersRewardCardMscIneligible() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_REWARD_MSC_INELIGIBLE, OfferRequest.class);
			offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse rewardCardOffers = new DataReader().readFileToObj("acquisition/offer_response_reward_msc.json", CTOfferResponse.class);
		CTOfferResponse selectedOffer = new CTOfferResponse();
		CTOffer videoPlan = new DataReader().readFileToObj("acquisition/video-plan-selected-ultimate.json", CTOffer.class);
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(videoPlan);
		selectedOffer.setOffers(selectedOffers);
		
		Mockito.doReturn(rewardCardOffers).when(cpopClientHelper).getOffers(any());
		Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).getSatelliteRewardCardOffers(any(), any());
		Mockito.doReturn(selectedOffer).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
		Mockito.doCallRealMethod().when(satelliteServicesOffersProcessor).getIncompatibleProducts(any());
		Mockito.doCallRealMethod().when(util).getProductObjFromOffer(any());
		Mockito.doCallRealMethod().when(util).getProductIdFromOffer(any());
		when(dmaLookUpService.getDMAValue(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(Arrays.asList("682"));
		
		CTOfferResponse offersResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(offersResponse);
		assertNull(offersResponse.getOffers());
		
	}
	
	@Test
    public void testMDUDTHGetOffersWithEligibleDealerCode() {
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest = new DataReader().readFileToObj(offer_request_MDU_DTH_eligibleDealerCode, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		CTOfferResponse offersResponse = new DataReader().readFileToObj(offers_response_MDU_DTH, CTOfferResponse.class);
		Mockito.when(redisCacheHelper.getValues(Constants.MDUDTH_PILOT_DEALERCODES, Constants.SATELLITE_PRODUCT_FAMILY))
		.thenReturn(Arrays.asList("DS0W6","I7OW2","V76YM","IXYMW"));
		when(salesVideoPlanProcessor.getSatelliteVideoPlanOffers(any())).thenReturn(offersResponse);
		
		CTOfferResponse finalResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(finalResponse);
		assertNotNull(finalResponse.getOffers());
		
	}
	
	@Test
    public void testMDUDTHGetOffersWithIneligibleDealerCode() throws Exception {
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest = new DataReader().readFileToObj(offer_request_MDU_DTH_ineligibleDealerCode, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		Mockito.when(redisCacheHelper.getValues(Constants.MDUDTH_PILOT_DEALERCODES, Constants.SATELLITE_PRODUCT_FAMILY))
		.thenReturn(Arrays.asList("DS0W6","I7OW2","V76YM","IXYMW"));
		
		ServiceException ex = catchThrowableOfType(() ->  satelliteSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertThat(ex.getError().getErrorId())
		.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_DEALERCODE));
		
	}
	
	@Test
    public void testMDUDTHGetOffersWithnoDealerCode() throws Exception {
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest = new DataReader().readFileToObj(offer_request_MDU_DTH_noDealerCode, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		Mockito.when(redisCacheHelper.getValues(Constants.MDUDTH_PILOT_DEALERCODES, Constants.SATELLITE_PRODUCT_FAMILY))
		.thenReturn(Arrays.asList("DS0W6","I7OW2","V76YM","IXYMW"));
		
		ServiceException ex = catchThrowableOfType(() ->  satelliteSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertThat(ex.getError().getErrorId())
		.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_DEALERCODE));
		
	}
	
	@Test
    public void testMDUDTHGetOffersWithInvalidSalesChannel() {
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest = new DataReader().readFileToObj(offer_request_MDU_DTH_invalidSalesChannel, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		CTOfferResponse offersResponse = new DataReader().readFileToObj(offers_response_MDU_DTH, CTOfferResponse.class);
		when(salesVideoPlanProcessor.getSatelliteVideoPlanOffers(any())).thenReturn(offersResponse);

		CTOfferResponse finalResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(finalResponse);
		assertNotNull(finalResponse.getOffers());
		
	}
	
	@Test
    public void testMDUDTHGetOffersWithInvalidSalesSubChannel() {
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest = new DataReader().readFileToObj(offer_request_MDU_DTH_invalidSalesSubChannel, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		CTOfferResponse offersResponse = new DataReader().readFileToObj(offers_response_MDU_DTH, CTOfferResponse.class);
		when(salesVideoPlanProcessor.getSatelliteVideoPlanOffers(any())).thenReturn(offersResponse);

		CTOfferResponse finalResponse = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
		assertNotNull(finalResponse);
		assertNotNull(finalResponse.getOffers());
		
	}
	
	@Test
    public void testGetOffersWithBlockedDealerCode() throws Exception {
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest = new DataReader().readFileToObj(offer_request_blockedDealer, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		Mockito.when(redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_DEALERCODE, Constants.SATELLITE_PRODUCT_FAMILY))
		.thenReturn(Arrays.asList("OXDAP","2N6LT","2NYH8","Z5LI7","TG95S","VBGBC","A64GV","RSA80","8D398","RCVZN","GMQ88","I4W0C","70SNB",
				"WMDF3","RTU4F","745W7","PBXG2","AW3YL","OLALD","8VSR4","93ZM2","IMRMQ","5PLTF","LNJ4C","UM7U4","CHJ2X","6KF7I","JVQV0",
				"YCWXN","NTYZB","MXMJC","QAD8U"));
		Mockito.when(redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESCHANNEL, Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESSUBCHANNEL, Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);		
		
		ServiceException ex = catchThrowableOfType(() ->  satelliteSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertThat(ex.getError().getErrorId())
		.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_BLOCKED_DEALER));
	}
	
	@Test
    public void testGetOffersWithBlockedSalesSubChannel() throws Exception {
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest = new DataReader().readFileToObj(offer_request_salesSubChannel, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		Mockito.when(redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_DEALERCODE, Constants.SATELLITE_PRODUCT_FAMILY))
		.thenReturn(Arrays.asList("OXDAP","2N6LT","2NYH8","Z5LI7","TG95S","VBGBC","A64GV","RSA80","8D398","RCVZN","GMQ88","I4W0C","70SNB",
				"WMDF3","RTU4F","745W7","PBXG2","AW3YL","OLALD","8VSR4","93ZM2","IMRMQ","5PLTF","LNJ4C","UM7U4","CHJ2X","6KF7I","JVQV0",
				"YCWXN","NTYZB","MXMJC","QAD8U"));
		Mockito.when(redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESCHANNEL, Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESSUBCHANNEL, Constants.SATELLITE_PRODUCT_FAMILY))
		.thenReturn(Arrays.asList("ND"));		
		
		ServiceException ex = catchThrowableOfType(() ->  satelliteSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertThat(ex.getError().getErrorId())
		.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_BLOCKED_DEALER));
	}
	
	@Test
    public void testGetOffersWithUnblockedSalesSubChannelHavingExclusionList() throws Exception {
		
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest = new DataReader().readFileToObj(offer_request_blockedDealer, OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		
		Mockito.when(redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_DEALERCODE, Constants.SATELLITE_PRODUCT_FAMILY))
		.thenReturn(Arrays.asList("OXDAP","2N6LT","2NYH8","Z5LI7","TG95S","VBGBC","A64GV","RSA80","8D398","RCVZN","GMQ88","I4W0C","70SNB",
				"WMDF3","RTU4F","745W7","PBXG2","AW3YL","OLALD","8VSR4","93ZM2","IMRMQ","5PLTF","LNJ4C","UM7U4","CHJ2X","6KF7I","JVQV0",
				"YCWXN","NTYZB","MXMJC","QAD8U"));
		Mockito.when(redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESCHANNEL, Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(null);
		Mockito.when(redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESSUBCHANNEL, Constants.SATELLITE_PRODUCT_FAMILY))
		.thenReturn(Arrays.asList("ND"));		
		
		ServiceException ex = catchThrowableOfType(() ->  satelliteSalesOffersProcessor.getOffers(offerRequestWrapper),
				ServiceException.class);
		
		assertThat(ex.getError().getErrorId())
		.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_BLOCKED_DEALER));
	}
	
	@Test
	public void testApplyOrderModChangesVideoDeviceOnlineFSTPOfferIdOFF() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_DEVICE_ONLINE_OFF, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("acquisition/video-device-stms-gemwh.json", CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        Mockito.doReturn(selectedBp).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
        Mockito.doReturn(ctOfferResponse).when(salesVideoDeviceProcessor).getSatelliteVideoDeviceOffers(any(), any());
        Mockito.doReturn(ctOfferResponse.getOffers()).when(satelliteCTOffersProcessor).associateFeeWithDevice(any(), any(), any());
        
        CTOfferResponse videoDeviceOffers = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(videoDeviceOffers);
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-HD-DVR_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 1)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 3)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 2)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		long count = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE)
				.count();
		assertEquals(3, count);
		
		assertNull(videoDeviceOffers.getIneligibleOffers());
		
	}
	
	@Test
	public void testApplyOrderModChangesVideoDeviceOnlineFSTPOfferIdON() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_DEVICE_ONLINE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            ctOfferResponse = new DataReader().readFileToObj("acquisition/video-device-stms-gemwh.json", CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        Mockito.doReturn(selectedBp).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
        Mockito.doReturn(ctOfferResponse).when(salesVideoDeviceProcessor).getSatelliteVideoDeviceOffers(any(), any());
        Mockito.doReturn(ctOfferResponse.getOffers()).when(satelliteCTOffersProcessor).associateFeeWithDevice(any(), any(), any());
        
        CTOfferResponse videoDeviceOffers = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(videoDeviceOffers);
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-HD-DVR_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 1)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 3)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 2)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		long count = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE)
				.count();
		assertEquals(3, count);
		
		assertNull(videoDeviceOffers.getIneligibleOffers());
		
	}
	
	@Test
	public void testApplyOrderModChangesVideoDeviceDIPFSTPOfferIdON() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse selectedBp = new CTOfferResponse();
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_ORDER_MOD_DEVICE_DIP, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_ORDER_MOD_DEVICE, CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        Mockito.doReturn(selectedBp).when(satelliteCTOffersProcessor).getOfferByIds(any(), anyBoolean(), anyBoolean());
        Mockito.doReturn(ctOfferResponse).when(salesVideoDeviceProcessor).getSatelliteVideoDeviceOffers(any(), any());
        Mockito.doReturn(ctOfferResponse.getOffers()).when(satelliteCTOffersProcessor).getHardwareInstantRebateOffers(any(), any(), any());
        
        CTOfferResponse videoDeviceOffers = satelliteSalesOffersProcessor.getOffers(offerRequestWrapper);
        assertNotNull(videoDeviceOffers);
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-2-WIRELESS-V1_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 1)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()) && Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE && offer.getAttributes().getQtySelected() == 8)
				.findAny();
		assertTrue(ctOffer.isPresent());
		
		long count = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes())
						&& offer.getAttributes().getIsSelected() == Boolean.TRUE)
				.count();
		assertEquals(2, count);
		
		assertNull(videoDeviceOffers.getIneligibleOffers());
		
	}

}
