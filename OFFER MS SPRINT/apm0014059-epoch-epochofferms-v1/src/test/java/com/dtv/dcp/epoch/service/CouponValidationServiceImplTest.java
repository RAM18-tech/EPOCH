package com.dtv.dcp.epoch.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.common.AccountContext;
import com.dtv.dcp.epoch.model.common.EligibilityContext;
import com.dtv.dcp.epoch.model.common.PurchaseContext;
import com.dtv.dcp.epoch.model.common.request.CouponValidationRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.bundle.BundleOfferProcessor;
import com.dtv.dcp.epoch.processor.satellite.SatelliteProductsProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;


public class CouponValidationServiceImplTest {
	
	  @Mock
	  BundleOfferProcessor bundleOfferProcessor;
	   
	  @Mock
	  SatelliteProductsProcessor productProcessor;
	   
	  @Mock
	  SatelliteServicesOffersProcessor offerProcessor;
	  
	  @InjectMocks
	  CouponValidationServiceImpl couponValidationServiceImpl;

	  @Mock
	FeatureManagerHelper featureManagerHelper;
	  
	  /** The headers. */
	    HttpHeaders headers;
	  
	    /**
	     * Setup.
	     */
	    @BeforeEach
	    public void setup() {
	        MockitoAnnotations.openMocks(this);
	        headers = Mockito.mock(HttpHeaders.class);
	    }
	    
		@Test
		public void testBundleOfferProcessor() throws Exception {
			
			OfferRequest offerRequest = new OfferRequest();
			OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
			CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);

			List<String> actionType = new ArrayList(); 
			offerRequest.setOfferActionType(actionType);
			offerRequestWrapper.setOfferRequest(offerRequest);
			// mock Service
			doReturn(offerResponse).when(bundleOfferProcessor).getBundleOffers(Mockito.any());

			CTOfferResponse response = couponValidationServiceImpl.getBundleOffers(offerRequestWrapper);
			
			System.out.println("response"+response.toString());

			assertNotNull(response);

		}
		
		@Test
		public void testGetProducts() throws Exception {
			
			ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
			
			CTProductResponse productResponse = new DataReader().readFileToObj("CTCouponProductResponse.json",CTProductResponse.class);

			// mock Service
			doReturn(productResponse).when(productProcessor).getProducts(Mockito.any());

			CTProductResponse response = couponValidationServiceImpl.getProducts(productRequestWrapper);
			System.out.println("response"+response.toString());

			assertNotNull(response);

		}
		
		@Test
		public void testGetOffers() throws Exception {
			
			OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
			
			CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);

			// mock Service
			doReturn(offerResponse).when(offerProcessor).getOffersFromCT(Mockito.any());

			CTOfferResponse response = couponValidationServiceImpl.getOffers(offerRequestWrapper);
			System.out.println("response"+response.toString());

			assertNotNull(response);

		}
		
		@Test
		public void testGenerateOfferRequest() throws Exception {
			
			String CampaignCode = "Test";
			OfferRequestWrapper response = couponValidationServiceImpl.generateOfferRequest(CampaignCode);
			assertNotNull(response);

		}	  
		
		@Test
		public void testValidateCampaignEligibility() throws Exception {

			CTOfferResponse offerResponse = new DataReader().readFileToObj("CTCouponOfferResponse.json",CTOfferResponse.class);

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("Superman");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

			Boolean response = couponValidationServiceImpl.ValidateCampaignEligibility(offerResponse , couponValidationRequest);

			assertNotNull(response);

			couponValidationServiceImpl.ValidateCampaignEligibility(null , null);

		}
		
		@Test
		public void testMatchEligibility() throws Exception {
			
			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();
	        
	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("A Star is Born Part2");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");
	        purchaseContext.setEventCode("A Star is Born Part2");

	        
	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");
	        
//	        PurchaseContext eligibility = new PurchaseContext();
            EligibilityContext eligibility = new EligibilityContext();
	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
            List<String> title = new ArrayList<>();
            title.add("A Star is Born Part2 | <short description for 2018 version> |  EST |  Blu-Ray| Blu-Ray |  2020");
            title.add("A Star is Born | <short description for 2018 version> |  EST |  Blu-Ray |  Blu-Ray | 2019");
	        eligibility.setTitle(title);
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio("CAMPAIGN");
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");
			//doReturn(false).when(featureManagerHelper).isEnabled(any());
			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null );
		
			assertNotNull(response);
		}
		
		@Test
		public void testMatchEligibilityFailure() throws Exception {

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("Superman");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

			EligibilityContext eligibility = new EligibilityContext();

	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
	        eligibility.setTitle("Superman");
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio(null);
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");

			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null);

			assertNotNull(response);
		}

		@Test
		public void testMatchEligibilityFailurePurchaseType() throws Exception {

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("mismatch");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("Superman");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

			EligibilityContext eligibility = new EligibilityContext();

	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
	        eligibility.setTitle("Superman");
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio(null);
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");

			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null);

			assertNotNull(response);
		}

		@Test
		public void testMatchEligibilityFailureContentCategory() throws Exception {

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("sadsgfg");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("Superman");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

			EligibilityContext eligibility = new EligibilityContext();

	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
	        eligibility.setTitle("Superman");
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio(null);
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");

			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null);

			assertNotNull(response);
		}

		@Test
		public void testMatchEligibilityFailureFormat() throws Exception {

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("mismatch");
	        purchaseContext.setTitle("Superman");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

			EligibilityContext eligibility = new EligibilityContext();

	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
	        eligibility.setTitle("Superman");
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio("CAMPAIGN");
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");

			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null);

			assertNotNull(response);
		}

		@Test
		public void testMatchEligibilityFailureTitle() throws Exception {

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("hsgdaj");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

			EligibilityContext eligibility = new EligibilityContext();

	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
	        eligibility.setTitle("Superman");
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio(null);
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");

			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null);

			assertNotNull(response);
		}

		@Test
		public void testMatchEligibilityFailureGenre() throws Exception {

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("Superman");
	        purchaseContext.setGenre("hfgahsdgf");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

	        EligibilityContext eligibility = new EligibilityContext();

	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
	        eligibility.setTitle("Superman");
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio(null);
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");

			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null);

			assertNotNull(response);
		}

		@Test
		public void testMatchEligibilityFailureRating() throws Exception {

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("Superman");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("ashdshg");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

	        EligibilityContext eligibility = new EligibilityContext();

	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
	        eligibility.setTitle("Superman");
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio("CAMPAIGN");
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");

			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null);

			assertNotNull(response);
		}

		@Test
		public void testMatchEligibilityFailureStudio() throws Exception {

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("Superman");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("gajhf");
	        purchaseContext.setNewRelease(true);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

	        EligibilityContext eligibility = new EligibilityContext();

	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
	        eligibility.setTitle("Superman");
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio("CAMPAIGN");
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");

			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null);

			assertNotNull(response);

			// Check content category condition
			eligibility.setPurchaseType(null);
			assertNotNull(couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null));

			eligibility.setContentCategory(null);
			assertNotNull(couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null));

			eligibility.setFormat(null);
			assertNotNull(couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null));

			eligibility.setTitle(null);
			assertNotNull(couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null));

			eligibility.setGenre(null);
			assertNotNull(couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null));

			eligibility.setStudio(null);
			assertNotNull(couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null));

			eligibility.setNewRelease(null);
			assertNotNull(couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null));


			assertNotNull(couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null));

		}


		@Test
		public void testMatchEligibilityFailureRelease() throws Exception {

			CouponValidationRequest couponValidationRequest = new CouponValidationRequest();

	        AccountContext accountContext = new AccountContext();
	        PurchaseContext purchaseContext = new PurchaseContext();

	        accountContext.setBAN("111111");
	        purchaseContext.setPurchaseType("Rent");
	        purchaseContext.setPurchaseAmount("10");
	        purchaseContext.setContentCategory("Adult");
	        purchaseContext.setFormat("Blu-Ray");
	        purchaseContext.setTitle("Superman");
	        purchaseContext.setGenre("Comedy");
	        purchaseContext.setRating("PG13");
	        purchaseContext.setStudio("CAMPAIGN");
	        purchaseContext.setNewRelease(false);
	        purchaseContext.setReleaseDate("22/12/2020");
	        purchaseContext.setTmsProgramID("22/12/2020");

	        couponValidationRequest.setPurchaseContext(purchaseContext);
	        couponValidationRequest.setAccountContext(accountContext);
	        couponValidationRequest.setCouponcode("TESTCOUPON");

			EligibilityContext eligibility = new EligibilityContext();

	        eligibility.setPurchaseType("Rent");
	        eligibility.setPurchaseAmount("10");
	        eligibility.setContentCategory("Adult");
	        eligibility.setFormat("Blu-Ray");
	        eligibility.setTitle("Superman");
	        eligibility.setGenre("Comedy");
	        eligibility.setRating("PG13");
	        eligibility.setStudio("CAMPAIGN");
	        eligibility.setNewRelease(true);
	        eligibility.setReleaseDate("22/12/2020");
	        eligibility.setTmsProgramID("22/12/2020");

			Boolean response = couponValidationServiceImpl.matchEligibility(eligibility ,Constants.BENEFIT_FREE_PROMO, couponValidationRequest, null);

			assertNotNull(response);
		}


		@Test
		public void testValidatePurchaseAmountFreePromo() throws Exception {

			Boolean response = couponValidationServiceImpl.validatePurchaseAmount("10","12");
			assertNotNull(response);
		}

		@Test
		public void testValidatePurchaseAmountPercentOffer() throws Exception {

			Boolean response = couponValidationServiceImpl.validatePurchaseAmount("10","12");
			assertNotNull(response);
		}
}
