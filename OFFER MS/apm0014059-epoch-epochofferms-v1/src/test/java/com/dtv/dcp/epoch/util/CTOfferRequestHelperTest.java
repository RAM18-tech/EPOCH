package com.dtv.dcp.epoch.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.customergraph.response.CGAccountProducts;
import com.dtv.dcp.epoch.model.customergraph.response.Product;
import com.dtv.dcp.epoch.model.customergraph.response.UVAccountProductsResponse;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphServiceImpl;

@ExtendWith(MockitoExtension.class)
class CTOfferRequestHelperTest {


    @InjectMocks
    CTOfferRequestHelper ctOfferRequestHelper;
    
    @Mock
    CustomerGraphServiceImpl customerGraphService;
    
    @Mock
    CustomerGraphProductsHelper customerGraphHelper;

    @Mock
    OffersUtils offersUtils;

    @Mock
    FeatureManagerHelper featureHelper;
    /**
     * The headers.
     */
    HttpHeaders headers;

    /**
     * The uri info.
     */
    @Mock
    UriInfo mUriInfo;

    @Mock
    RedisCacheHelper redisCacheHelper;

    /**
     * Setup.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        headers = Mockito.mock(HttpHeaders.class);
    }

    @Test
    void preProcessOfferRequest() throws URISyntaxException {

    	Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
        doReturn(true).when(featureHelper).isEnabled(anyString());

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        OfferRequest offerRequest = JsonService.getObjectFromJson(TestUtility.loadJson("/getOfferRequest.json"), OfferRequest.class);


        OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offerRequest);

        assertNotNull(offerRequestWrapper);
    }
    @Test
    void preProcessProductRequest() throws URISyntaxException {

        Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
        Mockito.lenient().doReturn(true).when(featureHelper).isEnabled(anyString());

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

        ProductRequest productRequest = JsonService.getObjectFromJson(TestUtility.loadJson("/getProductRequest.json"), ProductRequest.class);


        ProductRequestWrapper productRequestWrapper = ctOfferRequestHelper.preProcessProductRequest(headers, productRequest);

        assertNotNull(productRequestWrapper);
    }
   @Test
    void testUversePreProcessOfferRequest() throws URISyntaxException {

	    Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
        Mockito.lenient().when(headers.getFirst("Idpctx-linkeduverseaccnums")).thenReturn("629011272");
        doReturn(true).when(featureHelper).isEnabled(anyString());

        mUriInfo = mock(UriInfo.class);
        Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
        Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));
        CGAccountProducts cgAccountProducts = new CGAccountProducts();
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        product.setLinesOfBusiness("HSIA");
        product.setProductBillingCode("HSIA123");
        product.setProductType("internate-plan");
        products.add(product);
        
        Product product1 = new Product();
        product1.setLinesOfBusiness("IPTV");
        product1.setProductBillingCode("IPTV123");
        product1.setProductType("vodeo-plan");
        products.add(product1);
        
        Product product2 = new Product();
        product2.setLinesOfBusiness("DTVS");
        product2.setProductBillingCode("Satellite123");
        product2.setProductType("video-plan");
        products.add(product2);
        
        cgAccountProducts.setProducts(products);
        
        String requestJson = "{\"pagination\":{\"page\":1,\"limit\":100},\"offerProductFamily\":[\"broadband\",\"IPTV\"],\"offerActionType\":[\"Acquisition\"],\"salesChannel\":[\"online\"],\"offerProductTypes\":[\"video-plan\"],\"contractIndicator\":[\"contract\"],\"customerContext\":{\"existingProductFamily\":[\"wireless\"],\"wireless\":{\"accountNumber\":\"298090749748\",\"isActive\":\"true\"}},\"customerEligibility\":{\"zipCode\":[\"32958\"]}}";
        OfferRequest offerRequest = JsonService.getObjectFromJson(requestJson, OfferRequest.class);
        UVAccountProductsResponse accountProductsResponse = JsonService.getObjectFromJson(TestUtility.loadJson(
				"/Uverse_Response_251461578.json"), UVAccountProductsResponse.class);
        when(customerGraphService.getUverseAccountProducts("629011272", "629011272", "uverse", true, UVAccountProductsResponse.class,"backup")).thenReturn(accountProductsResponse);
        when(customerGraphHelper.createUVerseAccountProducts(accountProductsResponse)).thenReturn(cgAccountProducts);
        when(offersUtils.isOTTService(any())).thenReturn(false);
        OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offerRequest);

        assertNotNull(offerRequestWrapper);
    }
   
   @Test
   void preProcessOfferRequest_ChannelEligibility() throws URISyntaxException {

	   Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
       Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
       doReturn(true).when(featureHelper).isEnabled(anyString());

       mUriInfo = mock(UriInfo.class);
       Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
       Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

       OfferRequest offerRequest = new DataReader().readFileToObj("stms/getOffers_channel_eligibility_req.json", OfferRequest.class);


       OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offerRequest);

       assertNotNull(offerRequestWrapper);
       assertNotNull(offerRequestWrapper.getCtOfferRequest());
       assertNotNull(offerRequestWrapper.getCtOfferRequest().getChannelEligibility());
       assertEquals("indirectSales", offerRequestWrapper.getCtOfferRequest().getChannelEligibility().getSalesChannel());
       assertEquals("DTVCALLCTR", offerRequestWrapper.getCtOfferRequest().getChannelEligibility().getSalesSubChannel());
       assertEquals("118724", offerRequestWrapper.getCtOfferRequest().getChannelEligibility().getLocationId());
       assertEquals("4", offerRequestWrapper.getCtOfferRequest().getChannelEligibility().getLocationTypeId());
       assertEquals("4335U", offerRequestWrapper.getCtOfferRequest().getChannelEligibility().getDealerCode());
       assertEquals("1234", offerRequestWrapper.getCtOfferRequest().getChannelEligibility().getDealerId());
       assertEquals("CHUZO", offerRequestWrapper.getCtOfferRequest().getChannelEligibility().getDirectIntegrationPartnerName());

   }
   
   @Test
   void preProcessOfferRequest_ServerDate() throws URISyntaxException {

	   Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
       Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
       Mockito.lenient().when(headers.getFirst(Constants.SERVER_DATE)).thenReturn("12/12/2023");
       doReturn(true).when(featureHelper).isEnabled(anyString());

       mUriInfo = mock(UriInfo.class);
       Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
       Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

       OfferRequest offerRequest = new DataReader().readFileToObj("stms/getOffers_channel_eligibility_req.json", OfferRequest.class);

       OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offerRequest);

       assertNotNull(offerRequestWrapper);
       assertNotNull(offerRequestWrapper.getCtOfferRequest());
       assertNotNull(offerRequestWrapper.getOfferRequest());
       assertNotNull(offerRequestWrapper.getOfferRequest().getServerDate());
       assertEquals("12/12/2023", offerRequestWrapper.getOfferRequest().getServerDate());

   }
   
   @Test
   void preProcessOfferRequest_ServerDateHeaderNoOverride() throws URISyntaxException {

	   Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
       Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
       Mockito.lenient().when(headers.getFirst(Constants.SERVER_DATE)).thenReturn("12/12/2023");
       doReturn(true).when(featureHelper).isEnabled(anyString());

       mUriInfo = mock(UriInfo.class);
       Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
       Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

       OfferRequest offerRequest = new DataReader().readFileToObj("stms/getOffers_channel_eligibility_req.json", OfferRequest.class);
       offerRequest.setServerDate("11/11/2023");

       OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offerRequest);

       assertNotNull(offerRequestWrapper);
       assertNotNull(offerRequestWrapper.getCtOfferRequest());
       assertNotNull(offerRequestWrapper.getOfferRequest());
       assertNotNull(offerRequestWrapper.getOfferRequest().getServerDate());
       assertEquals("11/11/2023", offerRequestWrapper.getOfferRequest().getServerDate());

   }
   
   @Test
   void preProcessOfferRequest_ServerDateGlobalConfig() throws URISyntaxException {

	   Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
       Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
       List<String> serverDate = new ArrayList<>();
       serverDate.add("10/10/2023");
       Mockito.when(redisCacheHelper.getValues(Constants.SERVER_DATE, Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(serverDate);
       doReturn(true).when(featureHelper).isEnabled(anyString());

       mUriInfo = mock(UriInfo.class);
       Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
       Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

       OfferRequest offerRequest = new DataReader().readFileToObj("stms/getOffers_channel_eligibility_req.json", OfferRequest.class);

       OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offerRequest);

       assertNotNull(offerRequestWrapper);
       assertNotNull(offerRequestWrapper.getCtOfferRequest());
       assertNotNull(offerRequestWrapper.getOfferRequest());
       assertNotNull(offerRequestWrapper.getOfferRequest().getServerDate());
       assertEquals("10/10/2023", offerRequestWrapper.getOfferRequest().getServerDate());

   }
   
   @Test
   void preProcessOfferRequest_ServerDateGlobalConfigNoOverride() throws URISyntaxException {

	   Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
       Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");
       List<String> serverDate = new ArrayList<>();
       serverDate.add("10/10/2023");
       Mockito.when(redisCacheHelper.getValues(Constants.SERVER_DATE, Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(serverDate);
       doReturn(true).when(featureHelper).isEnabled(anyString());

       mUriInfo = mock(UriInfo.class);
       Mockito.lenient().when(mUriInfo.getAbsolutePath()).thenReturn(new URI("www.att.com"));
       Mockito.lenient().when(mUriInfo.getRequestUri()).thenReturn(new URI("www.att.com"));

       OfferRequest offerRequest = new DataReader().readFileToObj("stms/getOffers_channel_eligibility_req.json", OfferRequest.class);
       offerRequest.setServerDate("11/11/2023");

       OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offerRequest);

       assertNotNull(offerRequestWrapper);
       assertNotNull(offerRequestWrapper.getCtOfferRequest());
       assertNotNull(offerRequestWrapper.getOfferRequest());
       assertNotNull(offerRequestWrapper.getOfferRequest().getServerDate());
       assertEquals("11/11/2023", offerRequestWrapper.getOfferRequest().getServerDate());

   }

}