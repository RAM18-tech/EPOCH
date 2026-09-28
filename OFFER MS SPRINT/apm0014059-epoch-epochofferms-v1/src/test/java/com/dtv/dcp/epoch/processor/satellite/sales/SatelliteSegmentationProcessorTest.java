package com.dtv.dcp.epoch.processor.satellite.sales;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.SatelliteProductsProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;

@ExtendWith(MockitoExtension.class)
public class SatelliteSegmentationProcessorTest {
	
	@InjectMocks
	private SatelliteSegmentationProcessor satelliteSegmentationProcessor;
	
	@Mock
	SatelliteProductsProcessor satelliteProductsProcessor;

	@Mock
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	@Mock
	SatelliteServicesOffersProcessor satelliteServicesOffersProcessor;
	
	@Mock
	OffersUtils util;
	
	private static final String OFFER_REQUEST_SEGMENT_ELIGIBLE = "acquisition/offer_request_segment_eligible.json";
	private static final String OFFER_REQUEST_SEGMENT_INELIGIBLE = "acquisition/offer_request_segment_ineligible.json";
	private static final String OFFER_REQUEST_DEVICE_AGENT = "acquisition/offer_request_device_agent.json";
	private static final String OFFER_REQUEST_DEVICE_AGENT_INELIGIBLE_BP = "acquisition/offer_request_device_agent_ineligible_bp.json";
	private static final String OFFER_REQUEST_DEVICE_MDUDTH_24MO = "acquisition/offer_request_device_mdudth_24mo.json";
	private static final String OFFER_REQUEST_DEVICE_MDUDTH_12MO = "acquisition/offer_request_device_mdudth_12mo.json";
	private static final String OFFER_REQUEST_DEVICE_MDUDTH_M2M = "acquisition/offer_request_device_mdudth_m2m.json";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void testPerformSegmentationEligible() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse offerResponse = new CTOfferResponse();
        CTProductResponse segmentProductResponse = new CTProductResponse();
        CTOfferResponse segmentOfferResponse = new CTOfferResponse();
        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOffer videoPlanSelectedOffer = new CTOffer();
        ctSelectedOffer.add(videoPlanSelectedOffer);
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SEGMENT_ELIGIBLE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            offerResponse = new DataReader().readFileToObj("acquisition/offer_response.json", CTOfferResponse.class);
            
            segmentOfferResponse = new DataReader().readFileToObj("acquisition/segment_offer_response.json", CTOfferResponse.class);
            
            segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response.json", CTProductResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        
        when(satelliteProductsProcessor.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
        when(satelliteCTOffersProcessor.getSatelliteOffers(offerRequestWrapper, new ArrayList<>(Arrays.asList(Constants.SEGMENT)))).thenReturn(segmentOfferResponse);
        when(satelliteServicesOffersProcessor.getListFromString(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isQualifyingOffer(any(), any())).thenReturn(true);
        
        satelliteSegmentationProcessor.performSegmentation(offerRequestWrapper, offerResponse.getOffers(), ctSelectedOffer,false,new HashMap<>());
        
        offerResponse.getOffers().stream().forEach(offer -> {
        	if("OF_BOLTON-EPIX-V2_satellite".equalsIgnoreCase(offer.getCode())) {
        		assertTrue(offer.getAttributes().getBenefits().size() > 0);
        	} else {
        		assertTrue(offer.getAttributes().getBenefits().isEmpty());
        	}
        });
	}
	
	@Test
	public void testPerformSegmentationIneligible() {
		OfferRequest offerRequest = null;
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        CTOfferResponse offerResponse = new CTOfferResponse();
        CTProductResponse segmentProductResponse = new CTProductResponse();
        CTOfferResponse segmentOfferResponse = new CTOfferResponse();
        List<CTOffer> ctSelectedOffer = new ArrayList<CTOffer>();
        CTOffer videoPlanSelectedOffer = new CTOffer();
        ctSelectedOffer.add(videoPlanSelectedOffer);
        try {
        	offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_SEGMENT_INELIGIBLE, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
            
            offerResponse = new DataReader().readFileToObj("acquisition/offer_response.json", CTOfferResponse.class);
            
            segmentOfferResponse = new DataReader().readFileToObj("acquisition/segment_offer_response.json", CTOfferResponse.class);
            
            segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response.json", CTProductResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        
        when(satelliteProductsProcessor.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
        when(satelliteCTOffersProcessor.getSatelliteOffers(offerRequestWrapper, new ArrayList<>(Arrays.asList(Constants.SEGMENT)))).thenReturn(segmentOfferResponse);
        when(satelliteServicesOffersProcessor.getListFromString(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isQualifyingOffer(any(), any())).thenReturn(true);
        
        satelliteSegmentationProcessor.performSegmentation(offerRequestWrapper, offerResponse.getOffers(), ctSelectedOffer,false,new HashMap<>());
        
        offerResponse.getOffers().stream().forEach(offer -> {
        	assertTrue(offer.getAttributes().getBenefits().isEmpty());
        });
	}
	
	@Test
	public void testGetHardwareInstantRebateOffersEligible() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTProductResponse segmentProductResponse = new CTProductResponse();
	    CTOfferResponse segmentOfferResponse = new CTOfferResponse();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_DEVICE_AGENT, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-agent.json", CTOfferResponse.class);
		CTOffer selectedOffer = new CTOffer();
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(selectedOffer);
		
		segmentOfferResponse = new DataReader().readFileToObj("acquisition/segment_offer_response-agent.json", CTOfferResponse.class);
        
        segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response-agent.json", CTProductResponse.class);
		
        when(satelliteProductsProcessor.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
        when(satelliteCTOffersProcessor.getSatelliteOffers(offerRequestWrapper, new ArrayList<>(Arrays.asList(Constants.SEGMENT)))).thenReturn(segmentOfferResponse);
        when(satelliteServicesOffersProcessor.getListFromString(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isQualifyingOffer(any(), any())).thenReturn(true);
        
		satelliteSegmentationProcessor.performSegmentation(offerRequestWrapper, videoDeviceOffers.getOffers(), selectedOffers,false,new HashMap<>());
        assertNotNull(videoDeviceOffers.getOffers());
        assertEquals(9, videoDeviceOffers.getOffers().size());
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()))
				.findAny();
		assertTrue(ctOffer.isPresent());
		assertEquals(8, ctOffer.get().getAttributes().getBenefits().size());
       
        List<String> promoKeys = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.map(Benefit::getCode)
				.collect(Collectors.toList());
        
        assertTrue(promoKeys.contains("PROMO_EQIR-L2-WIRELESS-GENIE-MINI-99_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L3-WIRELESS-GENIE-MINI-99_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L4-WIRELESS-GENIE-MINI-99_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L5-WIRELESS-GENIE-MINI-99_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L6-WIRELESS-GENIE-MINI-50_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L7-WIRELESS-GENIE-MINI-50_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L8-WIRELESS-GENIE-MINI-50_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L9-WIRELESS-GENIE-MINI-50_satellite"));

	}
	
	@Test
	public void testGetHardwareInstantRebateOffersIneligibleBP() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTProductResponse segmentProductResponse = new CTProductResponse();
	    CTOfferResponse segmentOfferResponse = new CTOfferResponse();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_DEVICE_AGENT_INELIGIBLE_BP, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-agent.json", CTOfferResponse.class);
		CTOffer selectedOffer = new CTOffer();
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(selectedOffer);
		
		segmentOfferResponse = new DataReader().readFileToObj("acquisition/segment_offer_response-agent.json", CTOfferResponse.class);
        
        segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response-agent.json", CTProductResponse.class);
		
        when(satelliteProductsProcessor.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
        when(satelliteCTOffersProcessor.getSatelliteOffers(offerRequestWrapper, new ArrayList<>(Arrays.asList(Constants.SEGMENT)))).thenReturn(segmentOfferResponse);
        when(satelliteServicesOffersProcessor.getListFromString(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isQualifyingOffer(any(), any())).thenReturn(false);
        
		satelliteSegmentationProcessor.performSegmentation(offerRequestWrapper, videoDeviceOffers.getOffers(), selectedOffers,false,new HashMap<>());
        assertNotNull(videoDeviceOffers.getOffers());
        assertEquals(9, videoDeviceOffers.getOffers().size());
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()))
				.findAny();
		assertTrue(ctOffer.isPresent());
		assertEquals(0, ctOffer.get().getAttributes().getBenefits().size());
       
	}
	
	@Test
	public void testGetHardwareInstantRebateOffers24Mo() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTProductResponse segmentProductResponse = new CTProductResponse();
	    CTOfferResponse segmentOfferResponse = new CTOfferResponse();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_DEVICE_MDUDTH_24MO, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-agent.json", CTOfferResponse.class);
		
		CTOffer selectedOffer = new DataReader().readFileToObj("acquisition/video-plan-selected-24mo.json", CTOffer.class);
		
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(selectedOffer);
		
		segmentOfferResponse = new DataReader().readFileToObj("acquisition/segment_offer_response-agent.json", CTOfferResponse.class);
        
        segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response-agent.json", CTProductResponse.class);
		
        when(satelliteProductsProcessor.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
        when(satelliteCTOffersProcessor.getSatelliteOffers(offerRequestWrapper, new ArrayList<>(Arrays.asList(Constants.SEGMENT)))).thenReturn(segmentOfferResponse);
        when(satelliteServicesOffersProcessor.getListFromString(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isQualifyingOffer(any(), any())).thenReturn(true);
        when(util.isNotValidBasedOnCommitmentDuration(any(), any(), any())).thenCallRealMethod();
        
		satelliteSegmentationProcessor.performSegmentation(offerRequestWrapper, videoDeviceOffers.getOffers(), selectedOffers,false,new HashMap<>());
        assertNotNull(videoDeviceOffers.getOffers());
        assertEquals(9, videoDeviceOffers.getOffers().size());
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()))
				.findAny();
		assertTrue(ctOffer.isPresent());
		assertEquals(8, ctOffer.get().getAttributes().getBenefits().size());
       
        List<String> promoKeys = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.map(Benefit::getCode)
				.collect(Collectors.toList());
        
        assertTrue(promoKeys.contains("PROMO_EQIR-L2-WIRELESS-GENIE-MINI-99_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L3-WIRELESS-GENIE-MINI-99_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L4-WIRELESS-GENIE-MINI-99_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L5-WIRELESS-GENIE-MINI-99_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L6-WIRELESS-GENIE-MINI-50_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L7-WIRELESS-GENIE-MINI-50_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L8-WIRELESS-GENIE-MINI-50_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L9-WIRELESS-GENIE-MINI-50_satellite"));

	}
	
	@Test
	public void testGetHardwareInstantRebateOffers12Mo() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTProductResponse segmentProductResponse = new CTProductResponse();
	    CTOfferResponse segmentOfferResponse = new CTOfferResponse();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_DEVICE_MDUDTH_12MO, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-agent.json", CTOfferResponse.class);
		
		CTOffer selectedOffer = new DataReader().readFileToObj("acquisition/video-plan-selected-24mo.json", CTOffer.class);
		selectedOffer.getAttributes().setCommitmentDuration("12-month");
		
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(selectedOffer);
		
		segmentOfferResponse = new DataReader().readFileToObj("acquisition/segment_offer_response-agent.json", CTOfferResponse.class);
        
        segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response-agent.json", CTProductResponse.class);
		
        when(satelliteProductsProcessor.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
        when(satelliteCTOffersProcessor.getSatelliteOffers(offerRequestWrapper, new ArrayList<>(Arrays.asList(Constants.SEGMENT)))).thenReturn(segmentOfferResponse);
        when(satelliteServicesOffersProcessor.getListFromString(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isQualifyingOffer(any(), any())).thenReturn(true);
        when(util.isNotValidBasedOnCommitmentDuration(any(), any(), any())).thenCallRealMethod();
        
		satelliteSegmentationProcessor.performSegmentation(offerRequestWrapper, videoDeviceOffers.getOffers(), selectedOffers,false,new HashMap<>());
        assertNotNull(videoDeviceOffers.getOffers());
        assertEquals(9, videoDeviceOffers.getOffers().size());
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()))
				.findAny();
		assertTrue(ctOffer.isPresent());
		assertEquals(2, ctOffer.get().getAttributes().getBenefits().size());
       
        List<String> promoKeys = ctOffer.get().getAttributes().getBenefits().stream().filter(Objects::nonNull)
				.map(Benefit::getCode)
				.collect(Collectors.toList());
        
        assertTrue(promoKeys.contains("PROMO_EQIR-L2-WIRELESS-GENIE-MINI-50_satellite"));
        assertTrue(promoKeys.contains("PROMO_EQIR-L3-WIRELESS-GENIE-MINI-50_satellite"));        

	}
	
	@Test
	public void testGetHardwareInstantRebateOffersM2M() {
		OfferRequest offerRequest = new OfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTProductResponse segmentProductResponse = new CTProductResponse();
	    CTOfferResponse segmentOfferResponse = new CTOfferResponse();
		try {
			offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_DEVICE_MDUDTH_M2M, OfferRequest.class);
            offerRequestWrapper.setOfferRequest(offerRequest);
		} catch (Exception e) {
		}
		
		CTOfferResponse videoDeviceOffers = new DataReader().readFileToObj("acquisition/video-device-agent.json", CTOfferResponse.class);
		
		CTOffer selectedOffer = new DataReader().readFileToObj("acquisition/video-plan-selected-24mo.json", CTOffer.class);
		selectedOffer.getAttributes().setCommitmentDuration("month-to-month");
		
		List<CTOffer> selectedOffers = new ArrayList<>();
		selectedOffers.add(selectedOffer);
		
		segmentOfferResponse = new DataReader().readFileToObj("acquisition/segment_offer_response-agent.json", CTOfferResponse.class);
        
        segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response-agent.json", CTProductResponse.class);
		
        when(satelliteProductsProcessor.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
        when(satelliteCTOffersProcessor.getSatelliteOffers(offerRequestWrapper, new ArrayList<>(Arrays.asList(Constants.SEGMENT)))).thenReturn(segmentOfferResponse);
        when(satelliteServicesOffersProcessor.getListFromString(any())).thenCallRealMethod();
        when(satelliteCTOffersProcessor.isQualifyingOffer(any(), any())).thenReturn(true);
        when(util.isNotValidBasedOnCommitmentDuration(any(), any(), any())).thenCallRealMethod();
        
		satelliteSegmentationProcessor.performSegmentation(offerRequestWrapper, videoDeviceOffers.getOffers(), selectedOffers,false,new HashMap<>());
        assertNotNull(videoDeviceOffers.getOffers());
        assertEquals(9, videoDeviceOffers.getOffers().size());
        
        Optional<CTOffer> ctOffer = videoDeviceOffers.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_DEVICE-GENIE-MINI-WIRELESS-V3_sales_satellite".equals(offer.getCode()))
				.findAny();
		assertTrue(ctOffer.isPresent());
		assertEquals(0, ctOffer.get().getAttributes().getBenefits().size());
       

	}

}
