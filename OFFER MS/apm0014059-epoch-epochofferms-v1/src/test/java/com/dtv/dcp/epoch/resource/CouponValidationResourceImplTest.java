package com.dtv.dcp.epoch.resource;


import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.common.AccountContext;
import com.dtv.dcp.epoch.model.common.PurchaseContext;
import com.dtv.dcp.epoch.model.common.request.CouponValidationRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.response.CouponValidationResponse;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.Product;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;
import com.dtv.dcp.epoch.model.customergraph.CustomerCouponsResults;
import com.dtv.dcp.epoch.model.customergraph.CustomerGraphPublisherMessage;
import com.dtv.dcp.epoch.processor.helper.CPOPValidateCouponHelper;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessorUCCHelper;
import com.dtv.dcp.epoch.service.CouponValidationService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphClientService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.service.satellite.SatelliteCouponsService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;


public class CouponValidationResourceImplTest {

	/** The OffersResource resource. */
	@InjectMocks
	CouponValidationResourceImpl couponValidationResourceImpl;

	// @Mock
	// private BenefitsServiceImpl benefitsService;

	/** The headers. */
	@Mock
	HttpHeaders headers;

	@Mock
	CouponValidationRequest couponValidationRequest;

	@Mock
	CPOPValidateCouponHelper cpopValidateCouponHelper;

	@Mock
	CouponValidationService couponValidationService;

	@Mock
	CustomerGraphService customerGraphService;

	@Mock
	FeatureManagerHelper featureManagerHelper;

	@Mock
	SatelliteCouponsService satelliteCouponsService;

	@Mock
	CustomerGraphClientService customerGraphClientService;

	@Mock
	SatelliteServicesOffersProcessorUCCHelper satelliteServicesOffersProcessorUCCHelper;

	@Mock
	KafkaTemplate<String, CustomerGraphPublisherMessage> kafkaTemplateCG;

	/** The uri info. */
	@Mock
	UriInfo mUriInfo;

	private static String COUPON_VALIDATE_REQUEST = "{\n    "
			+ "\"couponcode\": \"TEST_CAM_DISCOUNT_002\",\n    "
			+ "\"accountContext\": {\n        \"BAN\": \"2112\"\n    },\n   "
			+ " \"purchaseContext\": {\n       "
			+ " \"purchaseType\": \"Buy\",\n       "
			+ " \"purchaseAmount\": \"10$\",\n       "
			+ " \"contentCategory\": \"Adult\",\n        "
			+ "\"format\": \"4K\",\n        "
			+ "\"title\": \"Superman\",\n        "
			+ "\"genre\": \"Horror\",\n        "
			+ "\"rating\": \"PG13\",        \n        "
			+ "\"studio\": \"Sony\",\n        "
			+ "\"newRelease\": true,\n       "
			+ " \"releaseDate\": \"22/12/2020\",\n        "
			+ "\"tmsProgramID\":\"1222323\"\n    }\n}";


	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		headers = Mockito.mock(HttpHeaders.class);
	}

	@Test
	public void testCouponValidate() throws Exception {

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();

		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");

		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = new CustomerCouponsResults();
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",CTCouponResponse.class);
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);
		List<CustomerCoupons> customerCouponsList = new ArrayList<CustomerCoupons>();
		CustomerCoupons customerCoupons = new DataReader().readFileToObj("UCCValidateCustomerCouponsUsed.json",CustomerCoupons.class);
		customerCouponsList.add(customerCoupons);
		CustomerCoupons customerCouponsTwo = new CustomerCoupons("TEST_CAM_DISCOUNT_002", "A", "Available", "Enabler/STMS", "", "", "", "", "", "", "", "", "", "");
		customerCouponsList.add(customerCouponsTwo);
		customerCouponsResults.setCoupons(customerCouponsList);

		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons( Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers( Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());
		doReturn(offerRequestWrapper).when(couponValidationService).generateOfferRequest( Mockito.any());
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any(), Mockito.any());
		doReturn(true).when(couponValidationService).ValidateCampaignEligibility(Mockito.any(),Mockito.any());

		

		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		////CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals(Constants.SUCCESS, couponValidationResponse.getContent().getCoupons().get(0).getResults().getStatus());
	}

	@Test
	public void testCouponValidateCouponExpired() throws Exception {

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");

		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();

		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		//purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");

		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = new CustomerCouponsResults();
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",CTCouponResponse.class);
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);
		List<CustomerCoupons> customerCouponsList = new ArrayList<CustomerCoupons>();
		CustomerCoupons customerCoupons = new DataReader().readFileToObj("UCCValidateCustomerCouponsUsed.json",CustomerCoupons.class);
		customerCouponsList.add(customerCoupons);
		CustomerCoupons customerCouponsTwo = new CustomerCoupons("TEST_CAM_DISCOUNT_002", "E", "Expired", "Enabler/STMS", "", "", "", "", "", "", "", "", "","");
		customerCouponsList.add(customerCouponsTwo);

		offerResponse.getProducts().get(0).getVariants().get(0).getAttributes().setCampaignEndDate("2020-01-15T00:00:00.000Z");

		customerCouponsResults.setCoupons(customerCouponsList);
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons( Mockito.any());
		//doReturn(couponResponseData).when(satelliteCouponsService).getCoupons( Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers( Mockito.any());
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any(), Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());
		doReturn(offerRequestWrapper).when(couponValidationService).generateOfferRequest( Mockito.any());
		doReturn(true).when(couponValidationService).ValidateCampaignEligibility(Mockito.any(),Mockito.any());

		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		////CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals(Constants.COUPON_EXPIRED, couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());
	}

	@Test
	public void testCouponValidateCouponExists() throws Exception {

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");

		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();

		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");


		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = new CustomerCouponsResults();
		CTCouponResponse couponResponseData = null;
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);
		List<CustomerCoupons> customerCouponsList = new ArrayList<CustomerCoupons>();
		CustomerCoupons customerCoupons = new DataReader().readFileToObj("UCCValidateCustomerCouponsUsed.json",CustomerCoupons.class);
		customerCouponsList.add(customerCoupons);
		CustomerCoupons customerCouponsTwo = new CustomerCoupons("TEST_CAM_DISCOUNT_002", "A", "Available", "Enabler/STMS", "", "", "", "", "", "", "", "", "", "");
		customerCouponsList.add(customerCouponsTwo);

		customerCouponsResults.setCoupons(customerCouponsList);

		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons( Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers( Mockito.any());
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any(), Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());
		doReturn(offerRequestWrapper).when(couponValidationService).generateOfferRequest( Mockito.any());
		doReturn(true).when(couponValidationService).ValidateCampaignEligibility(Mockito.any(),Mockito.any());
		
		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		////CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals(Constants.COUPON_NOT_EXISTS, couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());
	}

	@Test
	public void testCouponValidateAccountInvalid() throws Exception {

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");

		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();

		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");

		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = null;
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",CTCouponResponse.class);
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);

		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons( Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers( Mockito.any());
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any(), Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());
		doReturn(offerRequestWrapper).when(couponValidationService).generateOfferRequest( Mockito.any());
		doReturn(true).when(couponValidationService).ValidateCampaignEligibility(Mockito.any(),Mockito.any());
		
		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		////CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals(Constants.COUPON_ACCOUNT_INELIGIBLE, couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());
	}

	@Test
	public void testCouponValidatePurchaseInEligible() throws Exception {
		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");

		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();


		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");


		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = new CustomerCouponsResults();
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",CTCouponResponse.class);
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);
		List<CustomerCoupons> customerCouponsList = new ArrayList<CustomerCoupons>();
		CustomerCoupons customerCoupons = new DataReader().readFileToObj("UCCValidateCustomerCouponsUsed.json",CustomerCoupons.class);
		customerCouponsList.add(customerCoupons);
		CustomerCoupons customerCouponsTwo = new CustomerCoupons("TEST_CAM_DISCOUNT_002", "A", "Available", "Enabler/STMS", "", "", "", "", "", "", "", "", "", "");
		customerCouponsList.add(customerCouponsTwo);
		customerCouponsResults.setCoupons(customerCouponsList);
		List<String> title = new ArrayList<>();
		title.add("Interstellar");
		offerResponse.getProducts().get(0).getVariants().get(0).getAttributes().getCampaignEligibility().get(0).setTitle(title);
		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons( Mockito.any());

		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons( Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers( Mockito.any());
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any(), Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());
		doReturn(offerRequestWrapper).when(couponValidationService).generateOfferRequest( Mockito.any());
		doReturn(false).when(couponValidationService).ValidateCampaignEligibility(Mockito.any(),Mockito.any());

		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers,  couponValidationRequest);
		//CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals(Constants.COUPON_INVALID_PURCHASE, couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());
	}


	@Test
	public void testCouponValidateCouponRedeemed() throws Exception {

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");

		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();


		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");


		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = new CustomerCouponsResults();
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",CTCouponResponse.class);
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);
		List<CustomerCoupons> customerCouponsList = new ArrayList<CustomerCoupons>();
		CustomerCoupons customerCoupons = new DataReader().readFileToObj("UCCValidateCustomerCouponsUsed.json",CustomerCoupons.class);
		customerCouponsList.add(customerCoupons);
		CustomerCoupons customerCouponsTwo = new CustomerCoupons("TEST_CAM_DISCOUNT_002", "U", "Available", "Enabler/STMS", "", "", "", "", "", "", "", "", "", "");
		customerCouponsList.add(customerCouponsTwo);
		customerCouponsResults.setCoupons(customerCouponsList);

		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons( Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers( Mockito.any());
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any(), Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());
		doReturn(offerRequestWrapper).when(couponValidationService).generateOfferRequest( Mockito.any());
		doReturn(true).when(couponValidationService).ValidateCampaignEligibility(Mockito.any(),Mockito.any());

		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers,  couponValidationRequest);
		//CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals(Constants.COUPON_ALREADY_REDEEMED, couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());
	}

	@Test
	public void testCouponValidateQuotaEligibility() throws Exception {
		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");

		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();

		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");


		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = new CustomerCouponsResults();
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",CTCouponResponse.class);
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);
		List<CustomerCoupons> customerCouponsList = new ArrayList<CustomerCoupons>();
		CustomerCoupons customerCoupons = new DataReader().readFileToObj("UCCValidateCustomerCouponsUsed.json",CustomerCoupons.class);
		// CustomerCoupons customerCoupons = new CustomerCoupons("Spring", "U", "Used", "Enabler/STMS", "2020-08-04 11:59:01.310+0000", "2020-08-04 11:59:01.310+0000", "9999", "MV002181580000", "Ninja", "7.99", "5.99", "2.00", "");
		customerCouponsList.add(customerCoupons);
		CustomerCoupons customerCouponsTwo = new CustomerCoupons("TEST_CAM_DISCOUNT_002", "R", "Redeemed", "Enabler/STMS", "", "", "", "", "", "", "", "", "", "");
		customerCouponsList.add(customerCouponsTwo);
		customerCouponsResults.setCoupons(customerCouponsList);

		couponResponseData.getResults().get(0).setMaxApplications(0);
		offerResponse.getProducts().get(0).getVariants().get(0).getAttributes().setCouponType("multi-use-1");;

		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons( Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers( Mockito.any());
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.any(),Mockito.any(),Mockito.any(), Mockito.any(), Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());
		doReturn(offerRequestWrapper).when(couponValidationService).generateOfferRequest( Mockito.any());
		doReturn(true).when(couponValidationService).ValidateCampaignEligibility(Mockito.any(),Mockito.any());

		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers,  couponValidationRequest);
		//CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals(Constants.COUPON_ALREADY_REDEEMED, couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());
	}

	public OfferRequestWrapper generateOfferRequest(String campaignCode)
	{
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferRequest cTOfferRequest = new CTOfferRequest();
		//bundleProductIds.add(productId);
		//cTOfferRequest.setBundleProducts(bundleProductIds);
		CTCustomerContext customerContext  = new CTCustomerContext();
		//customerContext.setProductFamily("satellite");
		Product products = new Product();
		List<Product> productList =  new ArrayList<Product>();
		products.setProductCode(campaignCode);
		productList.add(products);
		customerContext.setProducts(productList);
		List<CTCustomerContext> customerContextList = new ArrayList<CTCustomerContext>();
		List<String> offerType =  new ArrayList<String>();
		offerType.add(Constants.COUPON);
		customerContextList.add(customerContext);
		cTOfferRequest.setCustomerContext(customerContextList);
		cTOfferRequest.setOfferType(offerType);
		offerRequestWrapper.setCtOfferRequest(cTOfferRequest);

		return offerRequestWrapper;
	}


	@Test
	public void testCouponValidateEmptyOffer() throws Exception {

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();

		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");

		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = new CustomerCouponsResults();
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",
				CTCouponResponse.class);
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponEmptyOfferResponse.json",
				CTOfferResponse.class);
		List<CustomerCoupons> customerCouponsList = new ArrayList<CustomerCoupons>();
		CustomerCoupons customerCoupons = new DataReader().readFileToObj("UCCValidateCustomerCouponsUsed.json",
				CustomerCoupons.class);
		customerCouponsList.add(customerCoupons);
		CustomerCoupons customerCouponsTwo = new CustomerCoupons("TEST_CAM_DISCOUNT_002", "A", "Available",
				"Enabler/STMS", "", "", "", "", "", "", "", "", "", "");
		customerCouponsList.add(customerCouponsTwo);
		customerCouponsResults.setCoupons(customerCouponsList);

		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons(Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers(Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());

		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers,  couponValidationRequest);
		//CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals("Coupon does not exists",
				couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());
	}

	@Test
	public void testCouponValidateCTcouponResponseIsNull() throws Exception {

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();

		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");

		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);
		CTCouponResponse couponResponseData = null;

		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers,  couponValidationRequest);
		//CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals("Coupon does not exists",
				couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());
	}

	// write a test method for getCurrentTime method
	@Test
	public void testGetCurrentTime() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		// Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_CG);

		String currentTime = couponValidationResourceImpl.getCurrentTime();
		assertNotNull(dateFormat.format(calendar.getTime()), currentTime);
	}

	@Test
	public void testCouponValidateUsedCoupon() throws Exception {

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();

		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");

		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = new CustomerCouponsResults();
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",
				CTCouponResponse.class);
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",
				CTOfferResponse.class);
		List<CustomerCoupons> customerCouponsList = new ArrayList<CustomerCoupons>();
		CustomerCoupons customerCoupons = new DataReader().readFileToObj("UCCValidateCustomerCouponsUsed.json",
				CustomerCoupons.class);
		customerCouponsList.add(customerCoupons);

		CustomerCoupons customerCouponsTwo = new CustomerCoupons("TEST_CAM_DISCOUNT_002", "A", "Used", "Enabler/STMS",
				"", "2020-08-05 12:01:00.000+0000", "", "", "", "", "", "", "2000000", "");
		customerCouponsList.add(customerCouponsTwo);

		customerCouponsResults.setCoupons(customerCouponsList);

		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons(Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers(Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());
		doReturn(offerRequestWrapper).when(couponValidationService).generateOfferRequest(Mockito.any());
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		doReturn(true).when(couponValidationService).ValidateCampaignEligibility(Mockito.any(), Mockito.any());

		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers,  couponValidationRequest);
		//CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals(Constants.SUCCESS,
				couponValidationResponse.getContent().getCoupons().get(0).getResults().getStatus());
	}

	@Test
	public void testCouponValidateEmptyOfferCouponType() throws Exception {

		when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
		when(headers.getFirst("uuid")).thenReturn("123456");
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(false);
		mUriInfo = mock(UriInfo.class);
		when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
		when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

		CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

		AccountContext accountContext = new AccountContext();
		PurchaseContext purchaseContext = new PurchaseContext();

		accountContext.setBAN("111111");
		purchaseContext.setPurchaseType("Rent");
		purchaseContext.setPurchaseAmount("10$");
		purchaseContext.setContentCategory("Adult");
		purchaseContext.setFormat("Blu-Ray");
		purchaseContext.setTitle("Superman");
		purchaseContext.setGenre("Comedy");
		purchaseContext.setRating("PG13");
		purchaseContext.setStudio("CAMPAIGN");
		purchaseContext.setNewRelease(true);
		purchaseContext.setReleaseDate("22/12/2020");
		purchaseContext.setTmsProgramID("22/12/2020");

		couponValidationRequest.setCouponcode("TEST_CAM_DISCOUNT_002");
		couponValidationRequest.setAccountContext(accountContext);
		couponValidationRequest.setPurchaseContext(purchaseContext);

		CustomerCouponsResults customerCouponsResults = null;
		CTCouponResponse couponResponseData = new DataReader().readFileToObj("CTCouponResponse.json",
				CTCouponResponse.class);
		CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponseSingleUser.json",
				CTOfferResponse.class);
		List<CustomerCoupons> customerCouponsList = new ArrayList<CustomerCoupons>();
		CustomerCoupons customerCoupons = new DataReader().readFileToObj("UCCValidateCustomerCouponsUsed.json",
				CustomerCoupons.class);
		customerCouponsList.add(customerCoupons);
		CustomerCoupons customerCouponsTwo = new CustomerCoupons("TEST_CAM_DISCOUNT_002", "A", "Available",
				"Enabler/STMS", "", "", "", "", "", "", "", "", "", "");
		customerCouponsList.add(customerCouponsTwo);
		// customerCouponsResults.setCoupons(customerCouponsList);

		doReturn(couponResponseData).when(satelliteCouponsService).getCoupons(Mockito.any());
		doReturn(offerResponse).when(couponValidationService).getOffers(Mockito.any());
		OfferRequestWrapper offerRequestWrapper = generateOfferRequest(offerResponse.getProducts().get(0).getCode());
		doReturn(offerRequestWrapper).when(couponValidationService).generateOfferRequest(Mockito.any());
		doReturn(customerCouponsResults).when(customerGraphService).getUverseCustomerCoupons(Mockito.any(),
				Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
		doReturn(true).when(featureManagerHelper).isEnabled(Mockito.any());
		doReturn(true).when(couponValidationService).ValidateCampaignEligibility(Mockito.any(), Mockito.any());

		ResponseEntity responseEntity = couponValidationResourceImpl.couponValidation(headers, couponValidationRequest);
		CouponValidationResponse couponValidationResponse = (CouponValidationResponse) responseEntity.getBody();
		
		//CouponValidationResponse couponValidationResponse = couponValidationResourceImpl.couponValidation(headers,  couponValidationRequest);
		//CouponValidationResponse couponValidationResponse = (CouponValidationResponse) response.getEntity();
		System.out.print(couponValidationResponse.getContent().getCoupons().toString());
		System.out.print(couponValidationResponse.getContent().getCoupons().get(0).getResults().getErrorMessage());

		assertEquals(Constants.SUCCESS,
				couponValidationResponse.getContent().getCoupons().get(0).getResults().getStatus());
	}


}