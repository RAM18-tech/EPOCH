package com.dtv.dcp.epoch.processor.ott.sales;



import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.dtvnaccount.DtvnAccountClient;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.OttMobilityOffersProcessor;


public class OttSalesMobilityProcessorHelperTest {
	
	
	@InjectMocks
	private OttSalesMobilityProcessorHelper ottSalesMobilityProcessorHelper;

	@Mock
	OttCTOffersProcessor ottCTOffersProcessor;
	
	@Mock
	OttMobilityOffersProcessor ottMobilityOffersProcessor;
	
	@Mock
    DtvnAccountClient dtvnAccountClient;
	
	public static final String WIRELESS_CLIENT = "WIRELESS_CLIENT";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	
	}
	
	@Test
	@Disabled
	public void testGetVideoPlanOffers() throws ClientException{
		
		// Temp  need to update late once logic updated
		
		List<CTOffer> offerList =new ArrayList<CTOffer>();
		CTOfferResponse ctOfferResponse=new CTOfferResponse();
		OfferRequestWrapper offerRequestWrapper=new OfferRequestWrapper();
		CTOffer ctOffer=new CTOffer();
		offerList.add(ctOffer);
		String accountNumbers="34658346584365";
		String sessionId="34658346584365";
		ctOfferResponse.setOffers(offerList);
		
		Boolean b=true;
		
		//videoPlanCTOfferList.add(ctOffer);
		when(ottCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper, true)).thenReturn(ctOfferResponse);
		
		when(ottMobilityOffersProcessor.retrieveOffersResponse(offerList, accountNumbers, true, sessionId, offerRequestWrapper)).thenReturn(offerList);
		
		when(dtvnAccountClient.getDtvnCustomerAccountStatus(Mockito.anyString())).thenReturn(b);
		
		//doReturn(ctOfferResponse).when(ottCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper, true, true));
		
		//doReturn(offerList).when(ottMobilityOffersProcessor.retrieveOffersResponse(offerList, Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), offerRequestWrapper));
	
		//doReturn(b).when(dtvnAccountClient.getDtvnCustomerAccountStatus(Mockito.anyString()));
		
		ottSalesMobilityProcessorHelper.getFilteredOffers(offerRequestWrapper, "base");
		ottSalesMobilityProcessorHelper.getFilteredOffers(offerRequestWrapper, "standalone");
		
		
	//	validation to do
	}
	
	// Coverage miss1
	@Test
	public void testStandaloneBaseOffersExceptionScenario() throws Exception {

		List<String> offerServiceType = new ArrayList<String>();
		offerServiceType.add("DirecTVNow");
		when(ottMobilityOffersProcessor.retrieveOffersResponse(Mockito.any(),Mockito.anyString(), Mockito.anyBoolean(),Mockito.anyString(), Mockito.any())).thenThrow(new NullPointerException());
		ServiceException ex = catchThrowableOfType(() -> ottSalesMobilityProcessorHelper.getFilteredOffers(null, "base"),
				ServiceException.class);
		assertNotNull(ex);
	}
		
	/*@Test(expected=ServiceException.class)
	public void testGetBaseOffersFailureWithDTVN() throws Exception {
		List<String> offerServiceType = new ArrayList<String>();
		offerServiceType.add("DirecTVNow");
		String loggedInId = "qayslid_01111813607";
		String dtvnAccount = "123";
		BundleOfferResponse actualResponse = dtvnBasePackageProcessor.retrieveBaseOffers(getBaseOfferRequestWrapper(loggedInId, null, dtvnAccount, ""));
		assertNull(actualResponse);
	}
	*/


	@SuppressWarnings("deprecation")
	@Test
	public void testGetBaseOffersFailureWirelessClientException() throws Exception {
		
		
		List<CTOffer> offerList =new ArrayList<CTOffer>();
		CTOfferResponse ctOfferResponse=new CTOfferResponse();
		OfferRequestWrapper offerRequestWrapper=new OfferRequestWrapper();
		CTOffer ctOffer=new CTOffer();
		offerList.add(ctOffer);
		String accountNumbers="34658346584365";
		String sessionId="34658346584365";
		ctOfferResponse.setOffers(offerList);
		List<String> offerServiceType = new ArrayList<String>();
		List<CTOffer> offersList = new ArrayList<>();
		String loggedInId = "qayslid_01111813607";
		offerServiceType.add("DirecTVNow");
		String authorizedAccountsWireless  = "177068234476";
		String dtvnAccount = null;
		offerRequestWrapper.setLoggedInId(loggedInId);
		Boolean b=true;
		offerRequestWrapper.setAuthAccountsWireless(authorizedAccountsWireless);
		offerRequestWrapper.setDtvnAccount(dtvnAccount);
		when(ottCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper, true)).thenReturn(ctOfferResponse);
		
		when(ottMobilityOffersProcessor.retrieveOffersResponse(offerList, accountNumbers, true, sessionId, offerRequestWrapper)).thenReturn(offerList);
		
		when(dtvnAccountClient.getDtvnCustomerAccountStatus(Mockito.anyString())).thenReturn(b);
		
		String offers = "{\"direcTVNow\":{\"baseOffers\":[{\"offerId\":\"7456863862\",\"offerDescription\":\"Includes favorites like IFC, The Travel Channel and The Weather Channel.\",\"ranking\":500,\"product\":{\"productId\":\"7456863871\",\"productType\":\"DTV Now base package\",\"displayName\":\"Live a Little\",\"longDescription\":\"You can change or cancel your subscription at any time at directvnow.com. There are no refunds or credits for any partial-month periods or unwatched content.\",\"billingProductCode\":\"BASE-LIVEALITTLE-201610\",\"billingProductId\":\"1401\",\"numberOfChannels\":\"60+ live channels\",\"price\":{\"priceType\":\"Retail Price\",\"basePrice\":35,\"bestPrice\":35,\"recurrenceIndicator\":true},\"promotions\":[{\"promotionCode\":\"7456863903\",\"promoType\":\"Free\",\"promotionCategory\":\"Product\",\"name\":\"Live a Little Free For 14 Days\",\"displayName\":\"Free For 7 Days\",\"duration\":\"8\",\"durationPeriod\":\"NoOfDays\",\"billingProductCode\":\"LAL14\",\"billingProductId\":\"175\",\"displayOrder\":1,\"myATTDisclosureMsg\":\"After free trial, auto-billed monthly unless canceled.\"}]}},{\"offerId\":\"7456863861\",\"offerDescription\":\"Includes favorites like Discovery, ESPNU, and VICELAND.\",\"ranking\":510,\"product\":{\"productId\":\"7456863870\",\"productType\":\"DTV Now base package\",\"displayName\":\"Just Right\",\"longDescription\":\"You can change or cancel your subscription at any time at directvnow.com. There are no refunds or credits for any partial-month periods or unwatched content.\",\"billingProductCode\":\"BASE-JUSTRIGHT-201610\",\"billingProductId\":\"1402\",\"numberOfChannels\":\"80+ live channels\",\"price\":{\"priceType\":\"Retail Price\",\"basePrice\":50,\"bestPrice\":50,\"recurrenceIndicator\":true},\"promotions\":[{\"promotionCode\":\"7456863905\",\"promoType\":\"Free\",\"promotionCategory\":\"Product\",\"name\":\"Just Right Free For 14 Days\",\"displayName\":\"Free For 7 Days\",\"duration\":\"8\",\"durationPeriod\":\"NoOfDays\",\"billingProductCode\":\"JR14\",\"billingProductId\":\"176\",\"displayOrder\":1,\"myATTDisclosureMsg\":\"After free trial, auto-billed monthly unless canceled.\"}]}},{\"offerId\":\"7456863863\",\"offerDescription\":\"Includes favorites like TNT, Sundance TV, and Lifetime.\",\"ranking\":530,\"product\":{\"productId\":\"7456863869\",\"productType\":\"DTV Now base package\",\"displayName\":\"Gotta Have It\",\"longDescription\":\"You can change or cancel your subscription at any time at directvnow.com. There are no refunds or credits for any partial-month periods or unwatched content.\",\"billingProductCode\":\"BASE-OMG-201610\",\"billingProductId\":\"1404\",\"numberOfChannels\":\"120+ live channels\",\"price\":{\"priceType\":\"Retail Price\",\"basePrice\":70,\"bestPrice\":70,\"recurrenceIndicator\":true},\"promotions\":[{\"promotionCode\":\"7456863901\",\"promoType\":\"Free\",\"promotionCategory\":\"Product\",\"name\":\"The Works Free For 14 Days\",\"displayName\":\"Free For 7 Days\",\"duration\":\"8\",\"durationPeriod\":\"NoOfDays\",\"billingProductCode\":\"TW14\",\"billingProductId\":\"178\",\"displayOrder\":1,\"myATTDisclosureMsg\":\"After free trial, auto-billed monthly unless canceled.\"}]}},{\"offerId\":\"7456863860\",\"offerDescription\":\"Includes Favorites like Comedy Central, Spike, and Syfy.\",\"ranking\":520,\"product\":{\"productId\":\"7456863868\",\"productType\":\"DTV Now base package\",\"displayName\":\"Go Big\",\"longDescription\":\"You can change or cancel your subscription at any time at directvnow.com. There are no refunds or credits for any partial-month periods or unwatched content.\",\"billingProductCode\":\"BASE-GOBIG-201610\",\"billingProductId\":\"1403\",\"numberOfChannels\":\"100+ live channels\",\"price\":{\"priceType\":\"Retail Price\",\"basePrice\":60,\"bestPrice\":60,\"recurrenceIndicator\":true},\"promotions\":[{\"promotionCode\":\"7456863904\",\"promoType\":\"Free\",\"promotionCategory\":\"Product\",\"name\":\"Bring It On Free For 14 Days\",\"displayName\":\"Free For 7 Days\",\"duration\":\"8\",\"durationPeriod\":\"NoOfDays\",\"billingProductCode\":\"BIO14\",\"billingProductId\":\"177\",\"displayOrder\":1,\"myATTDisclosureMsg\":\"After free trial, auto-billed monthly unless canceled.\"}]}}]}}";
		//BundleOfferResponse bundleOfferResponse = JsonService.getObjectFromJson(offers, BundleOfferResponse.class);
		//offersList = bundleOfferResponse.getdTVNowOfferResponse().getBasePackages();
		ClientException ce = new ClientException("", "", 0);
		ce.setSource(WIRELESS_CLIENT);
		ce.setErrorId("errorId");
		ce.setHttpCode(404);
		ce.setMessage("message");
		doThrow(ce).when(ottMobilityOffersProcessor).retrieveOffersResponse(Mockito.any(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.any());
		
		assertNotNull(ce.getErrorId());
		assertNotNull(ce.getHttpCode());
		ServiceException ex = catchThrowableOfType(() -> ottSalesMobilityProcessorHelper.getFilteredOffers(offerRequestWrapper, "base"),
				ServiceException.class);
		assertNotNull(ex);
	}

}
