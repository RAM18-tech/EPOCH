package com.dtv.dcp.epoch.resource;


import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.DisplayTypeByKey;
import com.dtv.dcp.epoch.model.common.SubCategoryByKey;
import com.dtv.dcp.epoch.model.common.request.Account;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.service.ott.OttProductsImpl;
import com.dtv.dcp.epoch.service.satellite.SatelliteProductService;
import com.dtv.dcp.epoch.service.watchtv.WatchTVProductsService;
import com.dtv.dcp.epoch.util.CTOfferRequestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dtv.dcp.epoch.representation.Error;

public class ProductsResourceImplTest {

    /** The OffersResource resource. */
    @InjectMocks
    private ProductsResourceImpl productsResourceImpl;

    @Mock
    private OttProductsImpl ottProductsService;

    @Mock
    private CTOfferRequestHelper ctOfferRequestHelper;

	@Mock
	private SatelliteProductService setelliteProductService;
	@Mock
	private WatchTVProductsService watchTVProductService;

    @Mock
    private RedisCacheHelper redisCacheHelper;

    @Mock
    private OffersUtils offersUtils;

    @Mock
    private FeatureManagerHelper featureManagerHelper;
    
    /** The headers. */
    HttpHeaders headers;

    ProductRequest productRequest;

    /** The uri info. */
    @Mock
    UriInfo mUriInfo;

    private static String PRODUCT_REQUEST = "{ \n" +
            "    \"offerIds\": [ \n" +
            "        \"string\" \n" +
            "    ], \n" +
            "    \"offerCodes\": [ \n" +
            "        \"string\" \n" +
            "    ], \n" +
            "    \"offerActionType\": [ \n" +
            "        \"Acquisition\" \n" +
            "    ], \n" +
            "    \"salesChannel\": [ \n" +
            "        \"opus\", \n" +
            "        \"online\" \n" +
            "    ], \n" +
            "    \"heartValue\": \"3\", \n" +
            "    \"accountTypes\": [ \n" +
            "        \"CONS\" \n" +
            "    ], \n" +
            "    \"addOnType\": [ \n" +
            "        \"bolt-on\", \n" +
            "        \"programming-bolt-on\", \n" +
            "        \"standalone\" \n" +
            "    ], \n" +
            "    \"customerSegments\": [ \n" +
            "        \"Employee\", \n" +
            "        \"Residential\" \n" +
            "    ], \n" +
            "    \"offerProductTypes\": [ \n" +
            "        \"base\", \n" +
            "        \"video-plan\" \n" +
            "    ], \n" +
            "    \"offerTypes\": [ \n" +
            "        \"free-trial\", \n" +
            "        \"x-off\", \n" +
            "        \"percent-off\", \n" +
            "        \"prepay\", \n" +
            "        \"free-promo\", \n" +
            "        \"multi-benefit\" \n" +
            "    ], \n" +
            "    \"productFamily\": [ \n" +
            "        \"OTT\", \n" +
            "        \"Wireless\", \n" +
            "        \"IPTV\", \n" +
            "        \"BB\", \n" +
            "        \"VOIP\" \n" +
            "    ], \n" +
            "    \"offerStatus\": [ \n" +
            "        \"Active\", \n" +
            "        \"Grandfathered\", \n" +
            "        \"Closed\" \n" +
            "    ], \n" +
            "    \"bundleProductCodes\": [ \n" +
            "        \"abc-123\", \n" +
            "        \"def-456\" \n" +
            "    ], \n" +
            "    \"bundleProductIds\": [ \n" +
            "        \"def-456\" \n" +
            "    ], \n" +
            "    \"contractApplicable\": [ \n" +
            "        \"contract\", \n" +
            "        \"non-contract\" \n" +
            "    ], \n" +
            "    \"benefitCodes\": [ \n" +
            "        \"string\" \n" +
            "    ], \n" +
            "    \"customerEligibility\": { \n" +
            "        \"state\": [ \n" +
            "            \"TX\" \n" +
            "        ], \n" +
            "        \"dma\": [ \n" +
            "            \"TX\" \n" +
            "        ], \n" +
            "        \"county\": [ \n" +
            "            \"plano\" \n" +
            "        ], \n" +
            "        \"city\": [ \n" +
            "            \"plano\" \n" +
            "        ], \n" +
            "        \"zipCode\": [ \n" +
            "            \"string\" \n" +
            "        ], \n" +
            "        \"zipCode_4\": [ \n" +
            "            \"75202-4206\" \n" +
            "        ], \n" +
            "        \"customerCallIntent\": [ \n" +
            "            \"Retail,\" \n" +
            "        ], \n" +
            "        \"custClassification\": [ \n" +
            "            \"existingCustomer\", \n" +
            "            \"newCustomer\" \n" +
            "        ] \n" +
            "    }, \n" +
            "    \"channelEligibility\": { \n" +
            "        \"opusGroup\": [ \n" +
            "            \"string\" \n" +
            "        ], \n" +
            "        \"opusChannels\": \"Retail\", \n" +
            "        \"opusSubChannels\": \"Retail\", \n" +
            "        \"regions\": [ \n" +
            "            \"Retail,\" \n" +
            "        ], \n" +
            "        \"market\": [ \n" +
            "            \"Retail,\" \n" +
            "        ], \n" +
            "        \"opusState\": [ \n" +
            "            \"TX,\" \n" +
            "        ], \n" +
            "        \"storeIds\": \"string\", \n" +
            "        \"dealerIds\": [ \n" +
            "            \"string\" \n" +
            "        ], \n" +
            "        \"callCenterIds\": [ \n" +
            "            \"string\" \n" +
            "        ], \n" +
            "        \"kioskIds\": [ \n" +
            "            \"string\" \n" +
            "        ], \n" +
            "        \"repProfile\": [ \n" +
            "            \"manager,\" \n" +
            "        ] \n" +
            "    }, \n" +
            "    \"cartContext\": { \n" +
            "        \"cpopProductIds\": [ \n" +
            "            \"12245abcd\" \n" +
            "        ], \n" +
            "        \"cpopOfferId\": [ \n" +
            "            \"455445abcd\" \n" +
            "        ] \n" +
            "    }, \n" +
            "    \"userType\": \"agent\", \n" +
            "    \"account\": { \n" +
            "        \"accountType\": \"nonmobility\", \n" +
            "        \"contractIndicator\": true, \n" +
            "        \"contractVersion\": \"v1\", \n" +
            "        \"zipCode\": \"32957\" \n" +
            "    } \n" +
            "}";
    private static String PRODUCT_RESPONSE = "{\n" +
            // "    \"content\": {\n" +
            "        \"limit\": 50,\n" +
            "        \"offset\": 0,\n" +
            "        \"count\": 40,\n" +
            "        \"total\": 40,\n" +
            "        \"products\": [\n" +
            "            {\n" +
            "                \"id\": \"a5269dc4-b6f5-4ab1-a71f-badfdceca1d3\",\n" +
            "                \"code\": \"VIDEOPLAN_TESTE2E\",\n" +
            "                \"productType\": {\n" +
            "                    \"typeId\": \"product-type\",\n" +
            "                    \"id\": \"eddcf245-874d-4cca-b069-8ada224b1d1a\",\n" +
            "                    \"key\": \"video-plan\"\n" +
            "                },\n" +
            "                \"name\": {\n" +
            "                    \"en\": \"VIDEOPLAN_TESTE2E\"\n" +
            "                },\n" +
            "                \"description\": {\n" +
            "                    \"en\": \"VIDEOPLAN_TESTE2E\"\n" +
            "                },\n" +
            "                \"categories\": [],\n" +
            "                \"variants\": [\n" +
            "                    {\n" +
            "                        \"id\": \"1\",\n" +
            "                        \"sku\": \"VIDEOPLAN_TESTE2E_SKU\",\n" +
            "                        \"key\": \"VIDEOPLAN_TESTE2E-1\",\n" +
            "                        \"prices\": [\n" +
            "                            {\n" +
            "                                \"value\": {\n" +
            "                                    \"centAmount\": 250000,\n" +
            "                                    \"currencyCode\": \"USD\",\n" +
            "                                    \"percentage\": 0,\n" +
            "                                    \"fractionDigits\": 2,\n" +
            "                                    \"dollarAmount\": 2500\n" +
            "                                },\n" +
            "                                \"id\": \"ecb370de-1eb0-4fb1-aa47-11c25c124df3\",\n" +
            "                                \"startDate\": \"2019-09-20T00:00:00.000Z\",\n" +
            "                                \"endDate\": \"9999-12-31T00:00:00.000Z\",\n" +
            "                                \"recurrenceIndicator\": true,\n" +
            "                                \"duration\": 0,\n" +
            "                                \"period\": \"day\",\n" +
            "                                \"proRateFrequency\": \"daily\",\n" +
            "                                \"contractIndicator\": \"contract\",\n" +
            "                                \"grandfathered\": false,\n" +
            "                                \"contracted\": false\n" +
            "                            }\n" +
            "                   	     ],\n" +
            "                        \"attributes\": {\n" +
            "                            \"displayName\": {\n" +
            "                                \"en\": \"VIDEOPLAN_TESTE2E\"\n" +
            "                            },\n" +
            "                            \"descriptionsByKey\": {\n" +
            "                                \"short-opus\": \"VIDEOPLAN_TESTE2E\"\n" +
            "                            },\n" +
            "                            \"billingProductCode\": \"VIDEOPLAN_TESTE2E\",\n" +
            "                            \"billingProductId\": \"32996\",\n" +
            "                            \"startDate\": \"2019-09-21T00:00:00.000Z\",\n" +
            "                            \"endDate\": \"2020-09-26T00:00:00.000Z\",\n" +
            "                            \"ranking\": 0,\n" +
            "                            \"productFamily\": \"OTT\",\n" +
            "                            \"capacityLimit\": 0,\n" +
            "                            \"videoBasePackageRequired\": false,\n" +
            "                            \"skipAdsIndicator\": false,\n" +
            "                            \"serviceFlag\": false,\n" +
            "                            \"concurrentStreamCount\": 0,\n" +
            "                            \"preOrder\": false,\n" +
            "                            \"thirdPartyOTT\": false,\n" +
            "                            \"dependentPromoIsFreeTrail\": false,\n" +
            "                            \"productStatus\": \"Active\",\n" +
            "                            \"contractApplicable\": \"contract\",\n" +
            "                            \"rsncapable\": \"false\",\n" +
            "                \"subCategoryByKey\": [],\n" +
            "                \"displayTypeBykey\": []\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "            ]\n" +
           // "    }\n" +
            "}";

    /**
     * Setup.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        headers = Mockito.mock(HttpHeaders.class);
        String[] str = "test1,test2".split(",");
  	  	ReflectionTestUtils.setField(productsResourceImpl, "attributesToRemoveInResponse", str);
    }

    @Test
    public void testGetProducts() throws Exception {

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("OTT");
        productRequest.setProductFamily(offerProductFamily);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setCount(20);

        when(ctOfferRequestHelper.preProcessProductRequest(any(), any())).thenReturn(productRequestWrapper);
        doReturn(ctProductResponse).when(ottProductsService).getProducts(headers, productRequestWrapper);
        
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        CTProductResponse response = (CTProductResponse) responseEntity.getBody();
        
       //CTProductResponse response = productsResourceImpl.getProducts(headers,  productRequest);

        assertNotNull(response);
        // assertEquals(20, response.get.getCount());
    }
    
    @Test
    public void testGetProducts_satellite() throws Exception {

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("satellite");
        productRequest.setProductFamily(offerProductFamily);

        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setCount(20);

        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        doReturn(productRequestWrapper).when(ctOfferRequestHelper).preProcessProductRequest(headers, productRequest);
     
        doReturn(ctProductResponse).when(ottProductsService).getProducts(headers, productRequestWrapper);
        doReturn(ctProductResponse).when(setelliteProductService).getProducts(headers, productRequestWrapper.getProductRequest());
        when(ctOfferRequestHelper.preProcessProductRequest(any(), any())).thenReturn(productRequestWrapper);
        
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        CTProductResponse response = (CTProductResponse) responseEntity.getBody();
        
       //CTProductResponse response = productsResourceImpl.getProducts(headers,  productRequest);

        assertNotNull(response);
        // assertEquals(20, response.get.getCount());
    }
    
    @Test
    public void testGetProductsWithAccount() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ProductRequest productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);

        ObjectMapper objectMapperRes = new ObjectMapper();
        CTProductResponse ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        //ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("OTT");

        Account account = new Account();
        account.setAccountType("mobility");
        account.setContractIndicator(true);
        account.setContractVersion("v1");
        account.setZipCode("32957");
        // productRequest.setAccount(account);
        productRequest.setProductFamily(offerProductFamily);

//        CTProductResponse ctProductResponse = new CTProductResponse();
//        ctProductResponse.setCount(20);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        doReturn(ctProductResponse).when(ottProductsService).getProducts(headers, productRequestWrapper);
        doReturn(productRequestWrapper).when(ctOfferRequestHelper).preProcessProductRequest(any(), any());

        CTProductResponse ctProductResponse1 = ottProductsService.getProducts(headers, productRequestWrapper);
        
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        CTProductResponse response = (CTProductResponse) responseEntity.getBody();
        
       //CTProductResponse response = productsResourceImpl.getProducts(headers,  productRequest);

        assertNotNull(response);
        assertNotNull(ctProductResponse1);
        // assertEquals(20, response.get.getCount());
    }
    @Test
    public void testGetProductsNullResponse() throws Exception {

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("OTT");

        Account account = new Account();
        account.setAccountType("mobility");
        account.setContractIndicator(true);
        account.setContractVersion("v1");
        account.setZipCode("32957");
       // productRequest.setAccount(account);
        productRequest.setProductFamily(offerProductFamily);

        CTProductResponse ctProductResponse = new CTProductResponse();
         ctProductResponse.setCount(20);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        doReturn(productRequestWrapper).when(ctOfferRequestHelper).preProcessProductRequest(headers, productRequest);
        doReturn(ctProductResponse).when(ottProductsService).getProducts(headers, productRequestWrapper);
        
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        CTProductResponse response = (CTProductResponse) responseEntity.getBody();
        
       //CTProductResponse response = productsResourceImpl.getProducts(headers,  productRequest);
       assertNotNull(response);
    }
    @Test
    public void testGetProductsServiceException() throws ServiceException {

        mUriInfo = mock(UriInfo.class);
        // when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        // when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = new ProductRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("OTT");
        productRequest.setProductFamily(offerProductFamily);

        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setCount(20);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);

        // doThrow(new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001)).when(ottProductsService.getProducts(any(), any()));
                //.getProducts(headers, productRequestWrapper);
        when(ottProductsService.getProducts(any(), any())).thenThrow(new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));

        ServiceException ex = catchThrowableOfType(() ->productsResourceImpl.getProducts(headers,  productRequest),
				ServiceException.class);
        
        
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        Error response = (Error) responseEntity.getBody();
        
        assertNotNull(response);
    }

    @Test
    public void testGetProductsService() throws ServiceException {

        mUriInfo = mock(UriInfo.class);
        // when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        // when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = new ProductRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("satellite");
        productRequest.setProductFamily(offerProductFamily);

        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setCount(20);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        when(setelliteProductService.getProducts(any(), any())).thenReturn(ctProductResponse);
        when(ctOfferRequestHelper.preProcessProductRequest(any(), any())).thenReturn(productRequestWrapper);
        assertNotNull(productsResourceImpl.getProducts(headers,  productRequest));
    }
    
    @Test
    public void testGetProductsService_exception() {

        mUriInfo = mock(UriInfo.class);
        // when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        // when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = new ProductRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("satellite");
        productRequest.setProductFamily(offerProductFamily);

        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setCount(20);

        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);

        doThrow(new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001)).when(ottProductsService)
                .getProducts(headers, productRequestWrapper);

        
        NullPointerException ex = catchThrowableOfType(() ->productsResourceImpl.getProducts(headers,  null),
        		NullPointerException.class);
        
        assertNotNull(ex);

        
    }
    
    @Test
    public void testGetProductsTAZ() throws Exception { 

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class); 
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ObjectMapper objectMapperRes = new ObjectMapper();
        CTProductResponse ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
        
        ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("OTT");
        productRequest.setProductFamily(offerProductFamily);
        List<String> contractIndicator=new ArrayList<String>();
        contractIndicator.add("TAZ");
        productRequest.setContractIndicator(contractIndicator);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        productRequestWrapper.setEmployeeAccount(true);

        when(ctOfferRequestHelper.preProcessProductRequest(any(), any())).thenReturn(productRequestWrapper);
        doReturn(ctProductResponse).when(ottProductsService).getProducts(headers, productRequestWrapper);
        
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        CTProductResponse response = (CTProductResponse) responseEntity.getBody();
        
       //CTProductResponse response = productsResourceImpl.getProducts(headers,  productRequest);

        assertNotNull(response);
        // assertEquals(20, response.get.getCount());
    }
    
    @Test
    public void testGetProductsTAZBYOD() throws Exception { 

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class); 
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ObjectMapper objectMapperRes = new ObjectMapper();
        CTProductResponse ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
        
        ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("OTT");
        productRequest.setProductFamily(offerProductFamily);
        List<String> contractIndicator=new ArrayList<String>();
        contractIndicator.add("TAZBYOD");
        productRequest.setContractIndicator(contractIndicator);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        productRequestWrapper.setEmployeeAccount(true);

        when(ctOfferRequestHelper.preProcessProductRequest(any(), any())).thenReturn(productRequestWrapper);
        doReturn(ctProductResponse).when(ottProductsService).getProducts(headers, productRequestWrapper);
        
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        CTProductResponse response = (CTProductResponse) responseEntity.getBody();
        
       //CTProductResponse response = productsResourceImpl.getProducts(headers,  productRequest);

        assertNotNull(response);
        // assertEquals(20, response.get.getCount());
    }
    
    @Test
    public void testGetProductsTAZCONTRACT() throws Exception { 

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class); 
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ObjectMapper objectMapperRes = new ObjectMapper();

        CTProductResponse ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
        
        ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("OTT");
        productRequest.setProductFamily(offerProductFamily);
        List<String> contractIndicator=new ArrayList<String>();
        contractIndicator.add("TAZCONTRACT");
        productRequest.setContractIndicator(contractIndicator);
        productRequest.setSalesChannel(Arrays.asList("directvOnline"));
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        productRequestWrapper.setEmployeeAccount(true);
        SubCategoryByKey category =new SubCategoryByKey();
        category.setKey("directvOnlineServices");
        category.setValue("directvOnlineServices");
        
        DisplayTypeByKey displayTypeByKey =new DisplayTypeByKey();
        displayTypeByKey.setDisplayTypeKey("directvOnlineServices");
        displayTypeByKey.setDisplayTypeValue("directvOnlineServices");	

        ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().setSubCategoryByKey(Arrays.asList(category));
        ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().setDisplayTypeByKey(Arrays.asList(displayTypeByKey));
        when(ctOfferRequestHelper.preProcessProductRequest(any(), any())).thenReturn(productRequestWrapper);
        doReturn(ctProductResponse).when(ottProductsService).getProducts(headers, productRequestWrapper);
        
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        CTProductResponse response = (CTProductResponse) responseEntity.getBody();
        
      // CTProductResponse response = productsResourceImpl.getProducts(headers,  productRequest);

        assertNotNull(response);
    }
    
    @Test
    public void testGetProducts_watchtv() throws Exception {

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("watchtv");
        productRequest.setProductFamily(offerProductFamily);

		/*
		 * CTProductResponse ctProductResponse = new CTProductResponse();
		 * ctProductResponse.setCount(20);
		 */
        ObjectMapper objectMapperRes = new ObjectMapper();
        CTProductResponse ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);

        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        doReturn(ctProductResponse).when(ottProductsService).getProducts(headers, productRequestWrapper);
        when(ctOfferRequestHelper.preProcessProductRequest(any(), any())).thenReturn(productRequestWrapper);
        when(watchTVProductService.getProducts(headers, productRequestWrapper)).thenReturn(ctProductResponse);
        
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        CTProductResponse response = (CTProductResponse) responseEntity.getBody();
        
       // CTProductResponse response = productsResourceImpl.getProducts(headers,  productRequest);

        assertNotNull(response);
        // assertEquals(20, response.get.getCount());
    }
    
    @Test
    public void testGetProducts_watchtv_Exception() throws Exception {

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("watchtv");
        productRequest.setProductFamily(offerProductFamily);

        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        when(ctOfferRequestHelper.preProcessProductRequest(any(), any())).thenReturn(productRequestWrapper);
        doThrow(new RuntimeException()).when(watchTVProductService).getProducts(any(),any());
        
//		ServiceException exception = assertThrows(ServiceException.class, () -> {
//			productsResourceImpl.getProducts(headers,  productRequest);
//		    });
		
		ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        Error response = (Error) responseEntity.getBody();
        
		assertEquals("CPOP_OFFER_ERROR_ON_GETEOFFER_10001", response.getError().getErrorId());
		   
    }    

    @Test
    public void testGetProducts_OTT_Exception() throws Exception {

        when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        when(headers.getFirst("uuid")).thenReturn("123456");

        mUriInfo = mock(UriInfo.class);
        when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = new ProductRequest();
        List<String> actionType = new ArrayList<String>();
        actionType.add("Acquisition");
        productRequest.setOfferActionType(actionType);
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("OTT");
        productRequest.setProductFamily(offerProductFamily);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setCount(20);

        when(ctOfferRequestHelper.preProcessProductRequest(any(), any())).thenReturn(productRequestWrapper);
        doThrow(new RuntimeException()).when(ottProductsService).getProducts(any(), any());
        
		/*
		 * ServiceException exception = assertThrows(ServiceException.class, () -> {
		 * productsResourceImpl.getProducts(headers, productRequest); });
		 */
        ResponseEntity responseEntity = productsResourceImpl.getProducts(headers,  productRequest);
        Error response = (Error) responseEntity.getBody();
        
		assertEquals("CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001", response.getError().getErrorId());
    }

}
