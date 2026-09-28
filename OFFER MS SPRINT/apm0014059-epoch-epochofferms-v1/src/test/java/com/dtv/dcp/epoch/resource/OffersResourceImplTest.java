package com.dtv.dcp.epoch.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTCheckOfferEligibilityResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPAdditionalOfferHelper;
import com.dtv.dcp.epoch.service.bundle.BundleOfferService;
import com.dtv.dcp.epoch.service.ott.OttOffersServiceImpl;
import com.dtv.dcp.epoch.service.satellite.SatelliteOffersService;
import com.dtv.dcp.epoch.service.watchtv.WatchTVOffersService;
import com.dtv.dcp.epoch.util.CTOfferRequestHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dtv.dcp.epoch.representation.Error;

@ExtendWith(MockitoExtension.class)
public class OffersResourceImplTest {

    /**
     * The OffersResource resource.
     */
    @InjectMocks
    private OffersResourceImpl offersResourceImpl;

    @Mock
    private OttOffersServiceImpl ottOffersService;

    @Mock
    private SatelliteOffersService satelliteOffersService;

    @Mock
    CPOPAdditionalOfferHelper dtvNowCPOPAdditionalOfferHelper;

    @Mock
    private FeatureManagerHelper featureHelper;

    @Mock
    BundleOfferService BundleOfferService;

    @Mock
    CTOfferRequestHelper ctOfferRequestHelper;
    
    @Mock
    WatchTVOffersService watchTVOffersService;
    
    @Mock
    private OffersUtils offersUtils;
    
    @Mock
    private RedisCacheHelper redisCacheHelper;

    /**
     * The headers.
     */
    HttpHeaders headers;

    OfferRequest offerRequest;

    /**
     * The uri info.
     */
    @Mock
    UriInfo mUriInfo;

    /**
     * Setup.
     */
    @BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		headers = Mockito.mock(HttpHeaders.class);
		String[] str = "test1,test2".split(",");
		ReflectionTestUtils.setField(offersResourceImpl, "attributesToRemoveInResponse", str);
		Mockito.lenient()
				.when(redisCacheHelper.getValues(Constants.REDIS_CACHE_SALESCHANNEL, Constants.OTT_PRODUCT_FAMILY))
				.thenReturn(Arrays.asList(
						"online,opus,none,onlineSales,onlineServices,opusSales,opusServices,partnerSales,partnerServices,dtvSelfService,dtvRioService,dtvSales,directvOnline,oemIAPROKUTV,oemIAPFIRETV,partner,ivr,tactical,evCRM,osprey,ccap,salesCRM,dpp,directIntegrationPartner,directvStreamOnline,oemAPPLE,oemTIZEN,oemGOOGLE,dtv360,oemIAPGOOGLE,assistedSales,stb"));

	}

    @Test
    public void testGetOffers() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("OTT");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(20);
        Mockito.lenient().when(offersUtils.getIapSalesChannels(Mockito.any())).thenReturn("N");
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest(Mockito.any(), Mockito.any());
        Mockito.lenient().doReturn(ctOfferResponse).when(ottOffersService).getOffers(Mockito.any());
        Mockito.lenient().doReturn(false).when(dtvNowCPOPAdditionalOfferHelper).isRewardCapabilityEnabled();
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
		
       // CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        //assertNotNull(response);
         assertEquals(20, response.getCount());
    }

    @Test
    public void testRewardGetOffers() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("BB");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);

        List<String> offerTypes = new ArrayList<String>();
        offerTypes.add("reward");
        offerRequest.setOfferTypes(offerTypes);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(20);
        
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);
        Mockito.lenient().doReturn(ctOfferResponse).when(ottOffersService).getOffers( offerRequestWrapper);
        Mockito.lenient().doReturn(true).when(dtvNowCPOPAdditionalOfferHelper).isRewardCapabilityEnabled();
        doReturn(true).when(featureHelper).isEnabled(Mockito.anyString());
        Mockito.lenient().doReturn(ctOfferResponse).when(BundleOfferService).getBundleOffers( offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
       // CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNull(response);
    }


    @Test
    public void testSatelliteGetOffers() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setOfferActionType(Stream.of("Retention").collect(Collectors.toList()));
        offerRequest.setOfferProductType(Stream.of("video-addon").collect(Collectors.toList()));
        offerRequest.setBillingProductCodes(Stream.of("EPIXDV").collect(Collectors.toList()));
        offerRequest.setSalesChannel(salesChannel);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);
        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers( offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
       // CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);
        assertNotNull(response);

    }

    @Test
    public void testGetOffersServiceException() throws ServiceException {

        mUriInfo = mock(UriInfo.class);


        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        ;
        offerProductFamily.add("OTT");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(20);

		// Mocking ServiceException
        ServiceException serviceException = new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001);

        // Mocking the service to throw ServiceException
        Mockito.lenient().doThrow(serviceException).when(ottOffersService).getOffers(Mockito.any());

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        // Verifying the error
        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));

		
    }

    @Test
    public void testThrowSatelliteException() throws ServiceException {

        mUriInfo = mock(UriInfo.class);


        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        ;
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(20);

        // Mocking ServiceException
        ServiceException serviceException = new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001);

        // Mocking the service to throw ServiceException
        Mockito.lenient().doThrow(serviceException).when(satelliteOffersService).getOffers(Mockito.any());

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        // Verifying the error
        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
    }

    @Test
    public void testOffersServiceException() throws ServiceException, URISyntaxException {
        // Mocking UriInfo
        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("http://www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("http://www.att.com"));

        // Setting up OfferRequest
        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<>();
        List<String> salesChannel = new ArrayList<>();
        offerProductFamily.add("OTT");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);

        // Wrapping OfferRequest
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        // Mocking ServiceException
        ServiceException serviceException = new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001);

        // Mocking the service to throw ServiceException
        Mockito.lenient().doThrow(serviceException).when(ottOffersService).getOffers(Mockito.any());

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        // Verifying the error
        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
    }

    @Test
    public void testSatelliteException() throws ServiceException {

        mUriInfo = mock(UriInfo.class);


        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        ;
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(20);
        NullPointerException nullExp = new NullPointerException();
		/*
		 * doThrow(nullExp).when(satelliteOffersService) .getOffers( Mockito.any());
		 * ServiceException ex = catchThrowableOfType(() ->
		 * offersResourceImpl.getOffers(headers, offerRequest), ServiceException.class);
		 * 
		 * assertThat(ex.getError().getErrorId())
		 * .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.
		 * CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
		 */	
		 // Mocking ServiceException
        ServiceException serviceException = new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001);

        // Mocking the service to throw ServiceException
        Mockito.lenient().doThrow(nullExp).when(satelliteOffersService).getOffers(Mockito.any());

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        // Verifying the error
        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
		
		
    }

    @Test
    public void testSatelliteRewardOffers() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferTypes(Stream.of("reward").collect(Collectors.toList()));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.lenient().when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
        Mockito.lenient().doReturn(ctOfferResponse).when(BundleOfferService).getBundleOffers( offerRequestWrapper);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
       // CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);
        assertNotNull(response);

    }

    @Test
    public void testSatelliteRequestForGetOffers() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferActionType(Stream.of("Retention").collect(Collectors.toList()));
        offerRequest.setOfferProductType(Stream.of("video-addon").collect(Collectors.toList()));
        offerRequest.setBillingProductCodes(Stream.of("EPIXDV").collect(Collectors.toList()));
        offerRequest.setOfferTypes(Stream.of("reward").collect(Collectors.toList()));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.lenient().when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
        Mockito.lenient().doReturn(ctOfferResponse).when(BundleOfferService).getBundleOffers( offerRequestWrapper);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);
        assertNotNull(response);

    }

    @Test
    public void testRequestAttributeSalesChannel() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        ;
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add("OPUSONLINE");
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.lenient().when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
        Mockito.lenient().doReturn(ctOfferResponse).when(BundleOfferService).getBundleOffers( offerRequestWrapper);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

		try {
			  ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
		      CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
		        
			//CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);
			assertNotNull(response);
		} catch (Exception e) {

		}

	}

    @Test
    public void testRequestAttributeNoOfferProductFamily() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));
        OfferRequest offerRequest = new OfferRequest();
        List<String> salesChannel = new ArrayList<String>();
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        doReturn(true).when(featureHelper).isEnabled(Mockito.anyString());
        List<String> offertypes =new ArrayList<>();
        offertypes. add("coupon");
        offerRequest.setOfferTypes(offertypes);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.lenient().when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);
        Mockito.lenient().doReturn(ctOfferResponse).when(BundleOfferService).getBundleOffers( offerRequestWrapper);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);
        assertNull(response);

    }

    @Test
    public void testRequiredAttributeForGetOffers() throws Exception {

        Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferActionType(Stream.of("Retention").collect(Collectors.toList()));
        offerRequest.setOfferProductType(Stream.of("video-addon").collect(Collectors.toList()));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.lenient().when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
        Mockito.lenient().doReturn(ctOfferResponse).when(BundleOfferService).getBundleOffers( offerRequestWrapper);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

		try {
			//CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);
			ServiceException ex = catchThrowableOfType(() -> offersResourceImpl.getOffers(headers,  offerRequest),
					ServiceException.class);

			assertThat(ex.getError().getErrorId())
					.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CTLG_DTVN_INVALID_CHANNEL));
//			assertNotNull(response);
		} catch (Exception e) {

		}
        

    }

    @Test
    public void testExceptionForSatelliteProductFamily() throws Exception {

        mUriInfo = mock(UriInfo.class);

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        ;
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferActionType(Stream.of("Retention").collect(Collectors.toList()));
        offerRequest.setOfferProductType(Stream.of("video-addon").collect(Collectors.toList()));
        offerRequest.setBillingProductCodes(Stream.of("EPIXDV").collect(Collectors.toList()));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(10);

        NullPointerException npe = new NullPointerException();

		/*
		 * doThrow(npe).when(satelliteOffersService).getOffers( Mockito.any());
		 * 
		 * ServiceException ex = catchThrowableOfType(() ->
		 * offersResourceImpl.getOffers(headers, offerRequest), ServiceException.class);
		 * 
		 * assertThat(ex.getError().getErrorId())
		 * .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.
		 * CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
		 */
		
		 // Mocking ServiceException
        ServiceException serviceException = new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001);

        // Mocking the service to throw ServiceException
        Mockito.lenient().doThrow(npe).when(satelliteOffersService).getOffers(Mockito.any());

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        // Verifying the error
        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
    }
    
    @Test
    public void testExceptionForSatelliteAcquisition() throws Exception {

        mUriInfo = mock(UriInfo.class);

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        ;
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferActionType(Stream.of("Acquisition").collect(Collectors.toList()));
        offerRequest.setOfferProductType(Stream.of("video-addon").collect(Collectors.toList()));
        offerRequest.setBillingProductCodes(Stream.of("EPIXDV").collect(Collectors.toList()));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(10);

        NullPointerException npe = new NullPointerException();

		/*
		 * doThrow(npe).when(satelliteOffersService).getOffers( Mockito.any());
		 * 
		 * ServiceException ex = catchThrowableOfType(() ->
		 * offersResourceImpl.getOffers(headers, offerRequest), ServiceException.class);
		 * 
		 * assertThat(ex.getError().getErrorId())
		 * .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.
		 * CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
		 */
		
		// Mocking ServiceException
        ServiceException serviceException = new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001);

        // Mocking the service to throw ServiceException
        Mockito.lenient().doThrow(npe).when(satelliteOffersService).getOffers(Mockito.any());

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        // Verifying the error
        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
		
		
    }
    
    @Test
    public void testWatchTvOffers() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add(Constants.WATCHTV_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferTypes(Stream.of("reward").collect(Collectors.toList()));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.lenient().when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
        Mockito.lenient().doReturn(ctOfferResponse).when(watchTVOffersService).getOffers( offerRequestWrapper);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);
        assertNotNull(response);

    }
    
    @Test
    public void testOfferCodes() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerRequest.setOfferProductFamily(offerProductFamily);
        offerRequest.setOfferCodes(Stream.of("OFFER_CODES").collect(Collectors.toList()));
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferTypes(Stream.of("reward").collect(Collectors.toList()));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        Mockito.lenient().when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
        Mockito.lenient().doReturn(ctOfferResponse).when(BundleOfferService).getBundleOffers( offerRequestWrapper);
        Mockito.lenient().doReturn(ctOfferResponse).when(watchTVOffersService).getOffers( offerRequestWrapper);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);
        assertNotNull(response);

    }
    
    @Test
    public void testUpdateMinimumAmount() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerRequest.setOfferProductFamily(offerProductFamily);
        offerRequest.setOfferCodes(Stream.of("OFFER_CODES").collect(Collectors.toList()));
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferTypes(Stream.of("reward").collect(Collectors.toList()));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("CTOfferResponse.json",CTOfferResponse.class);;
        //when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        //when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        doReturn(ctOfferResponse).when(watchTVOffersService).getOffers(Mockito.any());
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);
        assertNotNull(response);

    }
    
    @Test
    public void testGetOffersIVR() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("satellite");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.IVR);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
    }

    @Test
    public void testGetOffersTactical() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("satellite");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.TACTICAL);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
    }
    
    @Test
    public void testGetOffersSalesCRM() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("satellite");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.SALES_CRM);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
    }
    
    @Test
    public void testGetOffersCCAP() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("satellite");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.CCAP);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
    }
    
    @Test
    public void testGetOffersDPP() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("satellite");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.DPP);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
    }
    
    @Test
    public void testGetOffersDirectIntegrationPartner() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("satellite");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.DIRECT_INTEGRATION_PARTNER);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
    }
    
    @Test
    public void testGetOffersSTB() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("satellite");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.STB);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
    }
    
    @Test
    public void testGetOffersAssistedSales() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("satellite");
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ASSISTED_SALES);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);

        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
    }
    
    @Test
    public void testGetOffersCombinedRetentionCreditWithOtherTypes() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        List<String> offerActionType = new ArrayList<String>();
        List<String> offerProductTypes=new ArrayList<String>();
        
        offerProductFamily.add("satellite");
        salesChannel.add(Constants.ONLINE);
        
        offerActionType.add(Constants.RETENTION_ACTION_TYPE);
        offerActionType.add(Constants.UPGRADE_ACTION_TYPE);
        offerActionType.add(Constants.DOWNGRADE_ACTION_TYPE);
        offerActionType.add(Constants.CROSS_SELL_ACTION_TYPE);
        offerActionType.add(Constants.OTHER_ACTION_TYPE);
        
        offerProductTypes.add(Constants.CREDIT);
        offerProductTypes.add(Constants.VIDEO_ACCESSORY);
        offerProductTypes.add(Constants.VIDEO_ADDON);
        offerProductTypes.add(Constants.VIDEO_PLAN);
        offerProductTypes.add(Constants.VIDEO_DEVICE);
        offerProductTypes.add(Constants.INSURANCE);
        offerProductTypes.add(Constants.FEE_TYPE);
        
        offerRequest.setOfferProductFamily(offerProductFamily);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferActionType(offerActionType);
        offerRequest.setOfferProductType(offerProductTypes);
        
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();

        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest( headers, offerRequest);
        Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(offerRequestWrapper);
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
    }
    
    @Test
    void testValidateRequest_InvalidAccountStatus() throws JsonMappingException, JsonProcessingException { 
    	ObjectMapper objectMapper = new ObjectMapper();
    	String offerRequestJson = "{\"offerProductTypes\":[\"video-plan\",\"video-addon\"],\"offerProductFamily\":[\"satellite\"],\"contractIndicator\":[\"non-contract\"],\"customerContext\":{\"existingProductFamily\":[\"satellite\"],\"satellite\":{\"commissionStartDate\":\"\",\"nextBillingDate\":\"\",\"businessSegment\":\"REG\",\"serviceAddress\":{\"zipCode\":\"30824\",\"streetAddress\":\"640PYLANTCROSSINGRD\",\"city\":\"THOMSON\",\"state\":\"GA\"},\"nftvFlag\":\"Y\",\"freeTrialEligible\":false,\"accountNumber\":\"22485018\",\"isActive\":false,\"ftvCount\":\"1\",\"retentionPromotionIndicator\":false,\"accountStatus\":\"PEND\",\"activationDate\":\"\",\"accountType\":\"REG\"}},\"offerActionType\":[\"Upgrade\",\"Downgrade\",\"Cross-sell\",\"Other\"],\"customerEligibility\":{\"zipCode\":[\"30824\"],\"county\":[\"13189\"]},\"salesChannel\":[\"online\"]}";
    	offerRequest = objectMapper.readValue(offerRequestJson, OfferRequest.class);
        assertThrows(ServiceException.class, () -> offersResourceImpl.validateRequest(offerRequest));
    }
      
    @Test
    void testValidateRequest_InvalidProgrammingPackage() throws JsonMappingException, JsonProcessingException { 
    	ObjectMapper objectMapper = new ObjectMapper();
    	String offerRequestJson = "{\"contractIndicator\":[\"non-contract\"],\"customerEligibility\":{\"zipCode\":[\"80302\"],\"county\":[\"08013\"]},\"customerContext\":{\"existingProductFamily\":[\"satellite\"],\"satellite\":{\"products\":[],\"serviceAddress\":{\"streetAddress\":\"2600CANYONBLVD\",\"city\":\"BOULDER\",\"state\":\"CO\",\"zipCode\":\"80302\"},\"accountNumber\":\"1234567890\",\"isActive\":true,\"retentionPromotionIndicator\":false,\"freeTrialEligible\":false,\"accountType\":\"Residential\",\"nextBillingDate\":\"03/03/2025\",\"businessSegment\":\"REG\",\"nftvFlag\":\"Y\",\"ftvCount\":\"4\",\"accountStatus\":\"ACTV\",\"customerValue\":\"2\",\"acceptPaperlessBillEnrollment\":true,\"acceptAutoBillPayEnrollment\":true,\"activationDate\":\"03/03/2023\",\"commissionStartDate\":\"03/03/2023\"}},\"offerActionType\":[\"Upgrade\",\"Downgrade\",\"Cross-sell\",\"Other\"],\"offerProductFamily\":[\"satellite\"],\"salesChannel\":[\"online\"],\"offerProductTypes\":[\"video-plan\",\"video-addon\",\"video-device\",\"video-accessory\",\"insurance\"]}";
    	offerRequest = objectMapper.readValue(offerRequestJson, OfferRequest.class);
        assertThrows(ServiceException.class, () -> offersResourceImpl.validateRequest(offerRequest));
    }

    @Test
    void testValidateRequest_ValidRequest() throws JsonMappingException, JsonProcessingException {
    	ObjectMapper objectMapper = new ObjectMapper();
    	String offerRequestJson = "{\"contractIndicator\":[\"non-contract\"],\"customerEligibility\":{\"zipCode\":[\"80302\"],\"county\":[\"08013\"]},\"customerContext\":{\"existingProductFamily\":[\"satellite\"],\"satellite\":{\"products\":[{\"billingProductCode\":\"P103351\",\"billingReferenceID\":\"1\",\"productType\":\"video-plan\"},{\"billingProductCode\":\"R0167\",\"billingReferenceID\":\"41\",\"productType\":\"video-addon\"},{\"billingProductCode\":\"R120110\",\"billingReferenceID\":\"41\",\"productType\":\"video-addon\"},{\"billingProductCode\":\"R120124\",\"billingReferenceID\":\"41\",\"productType\":\"video-addon\"},{\"manufacturer\":\"DIRECTV\",\"modelNumber\":\"HR44-200\",\"accessCardId\":\"002187890633\",\"accessCardStatus\":\"ACTV\",\"equipmentOwnership\":\"lease\"}],\"serviceAddress\":{\"streetAddress\":\"2600CANYONBLVD\",\"city\":\"BOULDER\",\"state\":\"CO\",\"zipCode\":\"80302\"},\"accountNumber\":\"1234567890\",\"isActive\":true,\"retentionPromotionIndicator\":false,\"freeTrialEligible\":false,\"accountType\":\"Residential\",\"nextBillingDate\":\"03/03/2025\",\"businessSegment\":\"REG\",\"nftvFlag\":\"Y\",\"ftvCount\":\"4\",\"accountStatus\":\"ACTV\",\"customerValue\":\"2\",\"acceptPaperlessBillEnrollment\":true,\"acceptAutoBillPayEnrollment\":true,\"activationDate\":\"03/03/2023\",\"commissionStartDate\":\"03/03/2023\"}},\"offerActionType\":[\"Upgrade\",\"Downgrade\",\"Cross-sell\",\"Other\"],\"offerProductFamily\":[\"satellite\"],\"salesChannel\":[\"online\"],\"offerProductTypes\":[\"video-plan\",\"video-addon\",\"video-device\",\"video-accessory\",\"insurance\"]}";
    	offerRequest = objectMapper.readValue(offerRequestJson, OfferRequest.class);
        Mockito.when(offersUtils.isSTMSRequest(Mockito.any())).thenReturn(true);
    	assertDoesNotThrow(() -> offersResourceImpl.validateRequest(offerRequest));
    }
    
 	@Test
 	public void testGetOffersException() throws Exception {
 		mUriInfo = mock(UriInfo.class);
 		OfferRequest offerRequest = new OfferRequest();
 		List<String> offerProductFamily = new ArrayList<String>();
 		List<String> salesChannel = new ArrayList<String>();
 		offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
 		offerRequest.setOfferProductFamily(offerProductFamily);
 		salesChannel.add(Constants.ONLINE);
 		offerRequest.setSalesChannel(salesChannel);
 		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
 		offerRequestWrapper.setOfferRequest(offerRequest);
 		CTOfferResponse ctOfferResponse = new CTOfferResponse();
 		NullPointerException nullExp = new NullPointerException();
		/*
		 * doThrow(nullExp).when(satelliteOffersService).getOffers(Mockito.any());
		 * ServiceException ex = catchThrowableOfType(() ->
		 * offersResourceImpl.getOffers(headers, offerRequest), ServiceException.class);
		 * assertThat(ex.getError().getErrorId())
		 * .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.
		 * CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
		 */
 		
 		ServiceException serviceException = new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001);

        // Mocking the service to throw ServiceException
        Mockito.lenient().doThrow(nullExp).when(satelliteOffersService).getOffers(Mockito.any());

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        // Verifying the error
        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
 	}

 	
 	@Test
 	public void testCheckOffersEligibility() throws Exception {
 		mUriInfo = mock(UriInfo.class);
 		OfferRequest offerRequest = new OfferRequest();
 		List<String> offerProductFamily = new ArrayList<String>();
 		List<String> salesChannel = new ArrayList<String>();
 		offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
 		offerRequest.setOfferProductFamily(offerProductFamily);
 		salesChannel.add(Constants.ONLINE);
 		offerRequest.setSalesChannel(salesChannel);

 		List<String> offerCodes = new ArrayList<>();
 		offerCodes.add("Test123");
		offerRequest.setOfferCodes(offerCodes);
 		
 		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
 		offerRequestWrapper.setOfferRequest(offerRequest);
 		CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest(Mockito.any(), Mockito.any());

 		Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(Mockito.any());

 		ResponseEntity responseEntity = offersResourceImpl.checkOffersEligibility(headers,  offerRequest);
 		CTCheckOfferEligibilityResponse response = (CTCheckOfferEligibilityResponse) responseEntity.getBody();
 	        
 		//CTCheckOfferEligibilityResponse response  = offersResourceImpl.checkOffersEligibility(headers,  offerRequest);
 		assertNotNull(response);
 	}

 	@Test
 	public void testCheckOffersEligibilityException() throws Exception {
 		mUriInfo = mock(UriInfo.class);
 		OfferRequest offerRequest = new OfferRequest();
 		List<String> offerProductFamily = new ArrayList<String>();
 		List<String> salesChannel = new ArrayList<String>();
 		offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
 		offerRequest.setOfferProductFamily(offerProductFamily);
 		salesChannel.add(Constants.ONLINE);
 		offerRequest.setSalesChannel(salesChannel);
 		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
 		offerRequestWrapper.setOfferRequest(offerRequest);
 		CTOfferResponse ctOfferResponse = new CTOfferResponse();
 		NullPointerException nullExp = new NullPointerException();

        // Mocking the service to throw NullPointerException
        Mockito.lenient().doThrow(nullExp).when(satelliteOffersService).getOffers(Mockito.any());

        ResponseEntity responseEntity = offersResourceImpl.checkOffersEligibility(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        // Verifying the error
        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
 	}

 	
 	@Test
 	public void testCheckOffersEligibilityForInvalidOfferCodes() throws Exception {
 		mUriInfo = mock(UriInfo.class);
 		OfferRequest offerRequest = new OfferRequest();
 		List<String> offerProductFamily = new ArrayList<String>();
 		List<String> salesChannel = new ArrayList<String>();
 		List<String> offerCodes = new ArrayList<String>();
 		offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
 		offerRequest.setOfferProductFamily(offerProductFamily);
 		salesChannel.add(Constants.ONLINE);
 		offerRequest.setSalesChannel(salesChannel);
 		offerCodes.add("OFFER_CODES");
 		offerRequest.setOfferCodes(offerCodes);
 		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
 		offerRequestWrapper.setOfferRequest(offerRequest);
 		CTOfferResponse ctOfferResponse = new CTOfferResponse();
 		Mockito.lenient().when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
 		Mockito.lenient().when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
 		Mockito.lenient().doReturn(ctOfferResponse).when(BundleOfferService).getBundleOffers(Mockito.any());
 		Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest(Mockito.any(), Mockito.any());
 		Mockito.lenient().doReturn(ctOfferResponse).when(satelliteOffersService).getOffers(Mockito.any());

 		ResponseEntity responseEntity = offersResourceImpl.checkOffersEligibility(headers,  offerRequest);
 		CTCheckOfferEligibilityResponse response = (CTCheckOfferEligibilityResponse) responseEntity.getBody();
 		
 		//CTCheckOfferEligibilityResponse response  = offersResourceImpl.checkOffersEligibility(headers,  offerRequest);
 		assertNotNull(response);

 	}

 	
 	@Test
 	public void testCheckOffersEligibilityForEmptyOfferProductFamily() throws Exception {
 		mUriInfo = mock(UriInfo.class);
 		OfferRequest offerRequest = new OfferRequest();
 		List<String> offerProductFamily = new ArrayList<String>(); // empty list — matches test intent
 		List<String> salesChannel = new ArrayList<String>();
 		offerRequest.setOfferProductFamily(offerProductFamily);
 		salesChannel.add(Constants.ONLINE);
 		offerRequest.setSalesChannel(salesChannel);
 		offerRequest.setOfferCodes(Stream.of("OFFER_CODES").collect(Collectors.toList()));
 		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
 		offerRequestWrapper.setOfferRequest(offerRequest);
 		CTOfferResponse ctOfferResponse = new CTOfferResponse();
 		when(featureHelper.isEnabled(Mockito.anyString())).thenReturn(true);
 		Mockito.lenient().when(dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled()).thenReturn(true);
 		Mockito.lenient().doReturn(ctOfferResponse).when(BundleOfferService).getBundleOffers(offerRequestWrapper);

 		ResponseEntity responseEntity = offersResourceImpl.checkOffersEligibility(headers,  offerRequest);
 		CTCheckOfferEligibilityResponse response = (CTCheckOfferEligibilityResponse) responseEntity.getBody();
 		
 		//CTCheckOfferEligibilityResponse response  = offersResourceImpl.checkOffersEligibility(headers,  offerRequest);
 		assertNotNull(response);
 	}
 	
 	
 	@Test
    public void testExceptionForOTTProductFamily() throws Exception {

        mUriInfo = mock(UriInfo.class);

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        ;
        offerProductFamily.add(Constants.SATELLITE_PRODUCT_FAMILY);
        offerProductFamily.add(Constants.OTT_PRODUCT_FAMILY);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setSalesChannel(salesChannel);
        offerRequest.setOfferActionType(Stream.of("Retention").collect(Collectors.toList()));
        offerRequest.setOfferProductType(Stream.of("video-addon").collect(Collectors.toList()));
        offerRequest.setBillingProductCodes(Stream.of("EPIXDV").collect(Collectors.toList()));
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(10);

        NullPointerException npe = new NullPointerException();

        // Mocking the service to throw NullPointerException
        Mockito.lenient().doThrow(npe).when(satelliteOffersService).getOffers(Mockito.any());

        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        // Verifying the error
        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001));
    }
 	
 	@Test
    public void testGetOffersContractIndicatorTAZ() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("OTT");
        List<String> contractIndicator=new ArrayList<String>();
        contractIndicator.add(Constants.TAZ_STRING);
        offerRequest.setContractIndicator(contractIndicator);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(20);

        Mockito.lenient().when(offersUtils.getIapSalesChannels(Mockito.any())).thenReturn("N");
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest(Mockito.any(), Mockito.any());
        Mockito.lenient().doReturn(ctOfferResponse).when(ottOffersService).getOffers(Mockito.any());
        Mockito.lenient().doReturn(false).when(dtvNowCPOPAdditionalOfferHelper).isRewardCapabilityEnabled();
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
 		
       //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        //assertNotNull(response);
         assertEquals(20, response.getCount());
    }
 	
 	 @Test
     public void testGetOffersContractIndicatorTAZBYOD() throws Exception {

     	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
         Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

         mUriInfo = mock(UriInfo.class);
         Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
         Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

         OfferRequest offerRequest = new OfferRequest();
         List<String> offerProductFamily = new ArrayList<String>();
         List<String> salesChannel = new ArrayList<String>();
         offerProductFamily.add("OTT");
         List<String> contractIndicator=new ArrayList<String>();
         contractIndicator.add(Constants.TAZBYOD_STRING);
         offerRequest.setContractIndicator(contractIndicator);
         offerRequest.setOfferProductFamily(offerProductFamily);
         salesChannel.add(Constants.ONLINE);
         offerRequest.setSalesChannel(salesChannel);
         OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
         offerRequestWrapper.setOfferRequest(offerRequest);

         CTOfferResponse ctOfferResponse = new CTOfferResponse();
         ctOfferResponse.setCount(20);
         Mockito.lenient().when(offersUtils.getIapSalesChannels(Mockito.any())).thenReturn("N");
         Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest(Mockito.any(), Mockito.any());

         Mockito.lenient().doReturn(ctOfferResponse).when(ottOffersService).getOffers(Mockito.any());
         Mockito.lenient().doReturn(false).when(dtvNowCPOPAdditionalOfferHelper).isRewardCapabilityEnabled();
         
         ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
         CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
         
        // CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

         assertNotNull(response);
         // assertEquals(20, response.get.getCount());
     }
 	@Test
    public void testGetOffersContractIndicatorRR() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("OTT");
        List<String> contractIndicator=new ArrayList<String>();
        contractIndicator.add(Constants.ROAD_RUNNER);
        offerRequest.setContractIndicator(contractIndicator);
        offerRequest.setOfferProductFamily(offerProductFamily);
        salesChannel.add(Constants.ONLINE);
        offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(20);
        Mockito.lenient().when(offersUtils.getIapSalesChannels(Mockito.any())).thenReturn("N");
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest(Mockito.any(), Mockito.any());

        Mockito.lenient().doReturn(ctOfferResponse).when(ottOffersService).getOffers(Mockito.any());
        Mockito.lenient().doReturn(false).when(dtvNowCPOPAdditionalOfferHelper).isRewardCapabilityEnabled();
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
       // CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
        // assertEquals(20, response.get.getCount());
    }

 	@Test
    public void testGetOffersContractIndicatorTAZCONTRACT() throws Exception {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        List<String> salesChannel = new ArrayList<String>();
        offerProductFamily.add("OTT");
        List<String> contractIndicator=new ArrayList<String>();
        contractIndicator.add(Constants.TAZCONTRACT_STRING);
        offerRequest.setContractIndicator(contractIndicator);
        offerRequest.setOfferProductFamily(offerProductFamily);
        offerRequest.setOfferActionType(Arrays.asList("OTT"));
        salesChannel.add(Constants.OPUS);
        //offerRequest.setSalesChannel(salesChannel);
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(20);
        Mockito.lenient().when(offersUtils.getIapSalesChannels(Mockito.any())).thenReturn("N");
        Mockito.lenient().doReturn(offerRequestWrapper).when(ctOfferRequestHelper).preProcessOfferRequest(Mockito.any(), Mockito.any());

        Mockito.lenient().doReturn(ctOfferResponse).when(ottOffersService).getOffers(Mockito.any());
        Mockito.lenient().doReturn(false).when(dtvNowCPOPAdditionalOfferHelper).isRewardCapabilityEnabled();
        
        ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        CTOfferResponse response = (CTOfferResponse) responseEntity.getBody();
        
        //CTOfferResponse response  = offersResourceImpl.getOffers(headers,  offerRequest);

        assertNotNull(response);
        // assertEquals(20, response.get.getCount());
    }
    
    @Test
    public void testIsNotActiveAccount() throws Exception {
      
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new DataReader().readFileToObj("enabler/offer_request_isActive_false.json", OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
        
		ResponseEntity responseEntity = offersResourceImpl.getOffers(headers,  offerRequest);
        Error error = (Error) responseEntity.getBody();

        assertThat(error.getError().getErrorId())
                .isEqualTo(ResourceManager.getIdentifier(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_ACCOUNT_STATUS));
      
 	}
           
}