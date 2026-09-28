/**
 * 
 */
package com.dtv.dcp.epoch.processor.ott.services;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;

/**
 * @author ap778g
 *
 */
public class DtvnServicesFeeOffersProcessorTest {

	@Mock
	OffersUtils offersUtils;

	@InjectMocks
	public DtvnServicesFeeOffersProcessor dtvnServicesFeeOffersProcessor;

	@InjectMocks
	OttAvaialbleOffersProcessor ottPromoContinuationProcessor;

	@Mock
	public OttCTOffersProcessor ottCTOffersProcessor;

	@Mock
	OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	//@Test
	public void testRetrieveFeeOffers() {

		OfferRequest offerRequest = new OfferRequest();

		CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponse.json", CTOfferResponse.class);
		;
		doReturn(cTOfferResponse).when(ottCTOffersProcessor).getOffersFromCT(any(), any());
		doReturn(cTOfferResponse).when(ottServicesOffersProcessorHelper).getFilteredOffers(any(), any());

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setAgentType("SPECIAL");

		HttpHeaders headers = Mockito.mock(HttpHeaders.class);
		Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");
		Mockito.when(ottCTOffersProcessor.getFeeOffers(offerRequestWrapper)).thenReturn(cTOfferResponse);

		List<CTOffer> retrieveCTOffer = dtvnServicesFeeOffersProcessor.retrieveFeeOffers(offerRequestWrapper);
		assertNotNull(retrieveCTOffer);

	}

}
