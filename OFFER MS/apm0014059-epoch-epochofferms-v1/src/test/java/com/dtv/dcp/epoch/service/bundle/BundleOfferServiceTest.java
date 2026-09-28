
package com.dtv.dcp.epoch.service.bundle;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.bundle.BundleOfferProcessor;
@ExtendWith(MockitoExtension.class)
public class BundleOfferServiceTest {

	@InjectMocks
	BundleOfferServiceImpl rewardOfferService;
	@Mock
	BundleOfferProcessor BundleOfferProcessor;
	@Mock
	HttpHeaders headers;
	
	@Mock
	OfferRequestWrapper offerRequestWrapper;
	
	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		headers = Mockito.mock(HttpHeaders.class);
	}

	@Test
	public void testGetRewardOffers(){
		OfferRequest offerRequest = constructOfferRequest();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse offersResponse = new CTOfferResponse();
		String sessionId = "123456";

		Mockito.lenient().when(headers.getMediaType()).thenReturn(MediaType.valueOf("application/json"));
		Mockito.lenient().when(headers.getHeaderString("uuid")).thenReturn("123456");

		Mockito.lenient().when(headers.getHeaderString("idpctx-loggedInId")).thenReturn("loginId123");
		Mockito.lenient().when(headers.getHeaderString("idpctx-session-id")).thenReturn(sessionId);
		Mockito.lenient().when(headers.getHeaderString("Idpctx-linkeduverseaccnums")).thenReturn("8756756555");
		Mockito.lenient().when(headers.getHeaderString("idpctx-linkedDirectvNowAccNums")).thenReturn("45645645");
		Mockito.lenient().when(headers.getHeaderString("idpctx-linkedWirelessAccNums")).thenReturn("12345678");
		Mockito.lenient().when(BundleOfferProcessor.getBundleOffers(offerRequestWrapper)).thenReturn(offersResponse);
		rewardOfferService.getBundleOffers( offerRequestWrapper);
		
	}
	
	@Test
	public void testGetBundleOffers() {
		OfferRequest offerRequest = buildOfferRequestForBundleOffers();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		List<String> offerProductFamily = new ArrayList<String>();
		List<String> salesChannel = new ArrayList<String>();
		offerProductFamily.add("OTT");
		offerRequest.setOfferProductFamily(offerProductFamily);
		salesChannel.add(Constants.ONLINE);
		offerRequest.setSalesChannel(salesChannel);

		offerRequestWrapper.setOfferRequest(offerRequest);

		CTOfferResponse offersResponse = new CTOfferResponse();
		String sessionId = "123456";

		Mockito.lenient().when(headers.getMediaType()).thenReturn(MediaType.valueOf("application/json"));
		Mockito.lenient().when(headers.getHeaderString("uuid")).thenReturn("123456");

		Mockito.lenient().when(headers.getHeaderString("idpctx-loggedInId")).thenReturn("loginId123");
		Mockito.lenient().when(headers.getHeaderString("idpctx-session-id")).thenReturn(sessionId);
		Mockito.lenient().when(headers.getHeaderString("Idpctx-linkeduverseaccnums")).thenReturn("8756756555");
		Mockito.lenient().when(headers.getHeaderString("idpctx-linkedDirectvNowAccNums")).thenReturn("45645645");
		Mockito.lenient().when(headers.getHeaderString("idpctx-linkedWirelessAccNums")).thenReturn("12345678");
		Mockito.lenient().when(BundleOfferProcessor.getBundleOffers(offerRequestWrapper)).thenReturn(offersResponse);		
		rewardOfferService.getBundleOffers(offerRequestWrapper);

	}
	
	private OfferRequest constructOfferRequest(){
		OfferRequest offerRequest = new OfferRequest();
		List<String> actionTypes = new ArrayList<>();
		List<String> salesChannel = new ArrayList<>();
		actionTypes.add("Acquisition");
		salesChannel.add("Online");
		offerRequest.setOfferActionType(actionTypes);
		offerRequest.setSalesChannel(salesChannel);
		return offerRequest;
	}
	
	private OfferRequest buildOfferRequestForBundleOffers(){
		OfferRequest offerRequest = new OfferRequest();
		List<String> actionTypes = new ArrayList<>();		
		actionTypes.add("Acquisition");		
		offerRequest.setOfferActionType(actionTypes);		
		return offerRequest;
	}
}
