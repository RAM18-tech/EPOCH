package com.dtv.dcp.epoch.resource;


import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.CheckEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.CheckLocalChannelEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.response.CheckEligibilityLocalResponse;
import com.dtv.dcp.epoch.model.common.response.CheckEligibiltyResponse;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.OfferValidationService;
import com.dtv.dcp.epoch.util.CheckEligibilityUtils;
import com.dtv.dcp.epoch.representation.Error;

public class OfferValidationResourceImplTest {

	/** The OfferValidationResource. */
	@InjectMocks
	private OfferValidationResourceImpl offerValidationResourceImpl;
	/** The OfferValidationService resource. */
	@Mock
	private OfferValidationService offerValidationService;
	
	@Mock
	private DMALookUpService dmaLookUpService;
	
	@Mock
	CheckEligibilityUtils checkEligibilityUtils;
	
	/** The headers info. */
	@Mock
	HttpHeaders headers;
	/** The uri info. */
	@Mock
	UriInfo mUriInfo;
	
	/** The Constant IDP_SESSION_ID. */
	private static final String IDPCTX_SESSION_ID = "idpctx-session-id";

	/** The Constant IDPCTX_UUID. */
	private static final String IDPCTX_UUID = "idpctx-uuid";

	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		headers = Mockito.mock(HttpHeaders.class);
		
		List<String> list =  new ArrayList<>();
		list.add("810");
		
    	ReflectionTestUtils.setField(dmaLookUpService, "lccTrialDmaList", list);
	}

	@Test
	public void testOfferValidationSuccess() throws Exception {

		String sessionId = "123456";

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");

		when(headers.getFirst(IDPCTX_SESSION_ID)).thenReturn(sessionId);
		when(headers.getFirst("idpctx-acctinfocus")).thenReturn("1234");
		when(headers.getFirst("idpctx-acctinfocustype")).thenReturn("wireless");
		when(headers.getFirst(IDPCTX_UUID)).thenReturn("UUID123");
		when(headers.getFirst("idpctx-linkedWirelessAccNums")).thenReturn(null);

		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));
		
		OfferValidationRequest offerValidationRequest = new OfferValidationRequest();
		OfferValidationResponse offerValidationRespons = mock(OfferValidationResponse.class);
		

		doReturn(offerValidationRespons).when(offerValidationService).offerValidation(offerValidationRequest, sessionId);
		
		ResponseEntity responseEntity = offerValidationResourceImpl.offerValidation(headers,  offerValidationRequest);
		OfferValidationResponse response = (OfferValidationResponse) responseEntity.getBody();
        
		//OfferValidationResponse response = offerValidationResourceImpl.offerValidation(headers,  offerValidationRequest);

		assertNotNull(response);
	}

	@Test
	public void testOfferValidationServiceException() throws ClientException {
		OfferValidationRequest offerValidationRequest = new OfferValidationRequest();
		mUriInfo = mock(UriInfo.class);
		doThrow((new ServiceException(ErrorMessages.CTLG_WIRELESS_UNHANDLED_EXCEPTION)))
		.when(offerValidationService).offerValidation(null, null);
		ServiceException ex = catchThrowableOfType(() -> offerValidationResourceImpl.offerValidation(null,  offerValidationRequest),
				ServiceException.class);
		
	   ResponseEntity responseEntity = offerValidationResourceImpl.offerValidation(null,  offerValidationRequest);
	    Error response = (Error) responseEntity.getBody();
	    assertNotNull(response);
	        
		//assertNotNull(ex);

	}
	
	@Test
	public void testOfferValidationException() throws Exception {
		OfferValidationRequest offerValidationRequest = new OfferValidationRequest();
		//mUriInfo = mock(UriInfo.class);
		NullPointerException ne = new NullPointerException("");
		doThrow(ne).when(offerValidationService).offerValidation(null, null);
		ServiceException ex = catchThrowableOfType(() -> offerValidationResourceImpl.offerValidation(null, offerValidationRequest),
				ServiceException.class);
		
		ResponseEntity responseEntity = offerValidationResourceImpl.offerValidation(null,  offerValidationRequest);
        Error response = (Error) responseEntity.getBody();
        assertNotNull(response);
	        
		//assertNotNull(ex);
	}

	@Test
	public void testcheckLocalChannelEligibility() throws Exception {
		CheckLocalChannelEligibilityRequest checkLocalChannelEligibilityRequest = new CheckLocalChannelEligibilityRequest();
		CustomerEligibility customerEligibility = new CustomerEligibility();		
		List<String> zipcode = new ArrayList<>();
		zipcode.add("00058");
		customerEligibility.setZipCode(zipcode);
		List<String> county = new ArrayList<>();
		county.add("17000");
		customerEligibility.setCounty(county);
		checkLocalChannelEligibilityRequest.setCustomerEligibility(customerEligibility);
		mUriInfo = mock(UriInfo.class);		
		doReturn(true).when(dmaLookUpService).hasLocalChannels( zipcode.get(0),county.get(0));
		CheckEligibilityLocalResponse response = offerValidationResourceImpl.checkLocalChannelEligibility(headers,  checkLocalChannelEligibilityRequest);
		assertTrue(response.isHasLocalChannels());
	}
	
	@Test
	public void testLCCTrailDMAsuccess() throws Exception {
		CheckLocalChannelEligibilityRequest checkLocalChannelEligibilityRequest = new CheckLocalChannelEligibilityRequest();
		CustomerEligibility customerEligibility = new CustomerEligibility();		
		List<String> zipcode = new ArrayList<>();
		zipcode.add("00200");
		customerEligibility.setZipCode(zipcode);
		List<String> county = new ArrayList<>();
		county.add("41000");
		customerEligibility.setCounty(county);
		checkLocalChannelEligibilityRequest.setCustomerEligibility(customerEligibility);
		mUriInfo = mock(UriInfo.class);		
		doCallRealMethod().when(dmaLookUpService).isLCCTrialMarketDMA(zipcode.get(0),county.get(0));
		doReturn(Arrays.asList("810","839")).when(dmaLookUpService).getDMAValue(zipcode.get(0),county.get(0));
		CheckEligibilityLocalResponse response = offerValidationResourceImpl.checkLocalChannelEligibility(headers,  checkLocalChannelEligibilityRequest);
		assertTrue(response.isIsLCCTrialMarketDMA());
	}
	
	@Test
	public void testLCCTrailDMAfailure() throws Exception {
		CheckLocalChannelEligibilityRequest checkLocalChannelEligibilityRequest = new CheckLocalChannelEligibilityRequest();
		CustomerEligibility customerEligibility = new CustomerEligibility();		
		List<String> zipcode = new ArrayList<>();
		zipcode.add("00107");
		customerEligibility.setZipCode(zipcode);
		List<String> county = new ArrayList<>();
		county.add("17000");
		customerEligibility.setCounty(county);
		checkLocalChannelEligibilityRequest.setCustomerEligibility(customerEligibility);
		mUriInfo = mock(UriInfo.class);		
		doCallRealMethod().when(dmaLookUpService).isLCCTrialMarketDMA(zipcode.get(0),county.get(0));
		doReturn(Arrays.asList("602")).when(dmaLookUpService).getDMAValue(zipcode.get(0),county.get(0));
		CheckEligibilityLocalResponse response = offerValidationResourceImpl.checkLocalChannelEligibility(headers,  checkLocalChannelEligibilityRequest);
		assertFalse(response.isIsLCCTrialMarketDMA());
	}
	
	@Test
	public void testLCCTrailDMANull() throws Exception {
		CheckLocalChannelEligibilityRequest checkLocalChannelEligibilityRequest = new CheckLocalChannelEligibilityRequest();
		CustomerEligibility customerEligibility = new CustomerEligibility();		
		List<String> zipcode = new ArrayList<>();
		zipcode.add("00107");
		customerEligibility.setZipCode(zipcode);
		List<String> county = new ArrayList<>();
		county.add("17000");
		customerEligibility.setCounty(county);
		checkLocalChannelEligibilityRequest.setCustomerEligibility(customerEligibility);
		mUriInfo = mock(UriInfo.class);		
		doReturn(null).when(dmaLookUpService).isLCCTrialMarketDMA(zipcode.get(0),county.get(0));
		CheckEligibilityLocalResponse response = offerValidationResourceImpl.checkLocalChannelEligibility(headers,  checkLocalChannelEligibilityRequest);
		response.toString();
		assertFalse(response.isIsLCCTrialMarketDMA());
	}

	@Test
	public void testCheckEligibilty() {
		CheckEligibilityRequest checkEligibilityRequest = new DataReader().readFileToObj("CheckEligibilityRequest.json", CheckEligibilityRequest.class);
		when(checkEligibilityUtils.checkEligibiltyConditions(checkEligibilityRequest)).thenReturn(new CheckEligibiltyResponse());
		offerValidationResourceImpl.checkEligibility(checkEligibilityRequest);

	}
}
