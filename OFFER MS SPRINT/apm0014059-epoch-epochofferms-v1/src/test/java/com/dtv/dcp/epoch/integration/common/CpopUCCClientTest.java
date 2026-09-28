package com.dtv.dcp.epoch.integration.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.integration.CpopUCCClient;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;

public class CpopUCCClientTest {
	
    @InjectMocks
    CpopUCCClient cpopClient;

    @Mock
    private FeatureManagerHelper featureManagerHelper;
	
    @Mock
	CpopUCCClientHelper cpopClientHelper;

    @Mock
    BaseRestClient baseRestClient;

    RestTemplate restTemplate;
    RestTemplate restCustomTemplate;
    RestTemplateBeanFactory restTemplateBeanFactory;

    @Value("${pageLimit}")
    private int pageLimit;

    @Mock
    ResponseEntity responseEntity;


  
    /**
     * Setup.
     */
    @BeforeEach
    public void init() throws Exception {
        restTemplateBeanFactory=Mockito.mock(RestTemplateBeanFactory.class);
        restTemplate=Mockito.mock(RestTemplate.class);
        restCustomTemplate=Mockito.mock(RestTemplate.class);
        Mockito.when(restTemplateBeanFactory.getObject(ArgumentMatchers.anyString())).thenReturn(restTemplate);

        MockitoAnnotations.openMocks(this);
        try {
            ReflectionTestUtils.setField(cpopClient, "pageLimit", 5);
        } catch (Exception e) {
        }
    }
   
    @Test
    public void testGetOffers() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        Mockito.when(restCustomTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.<HttpEntity<?>>any(),
                ArgumentMatchers.<Class<String>>any())).thenReturn(returnOfferResponse());
        Mockito.when(restCustomTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any())).thenReturn(returnOfferResponse());
        cpopClient.getOffers(ctOfferRequest);

    }

    public ResponseEntity returnOfferResponse() {

        CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferWirelessResponse.json",CTOfferResponse.class);

        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity response = new ResponseEntity<CTOfferResponse>(
                cTOfferResponse,
                responseHeaders, HttpStatus.OK);
        return response;
    }
    
    public ResponseEntity returnCouponResponse() {

    	CTCouponResponse cTOfferResponse = new DataReader().readFileToObj("CTCouponResponse.json.json",CTCouponResponse.class);

        HttpHeaders responseHeaders = new HttpHeaders();
        ResponseEntity response = new ResponseEntity<CTCouponResponse>(
                cTOfferResponse,
                responseHeaders, HttpStatus.OK);
        return response;
    }
    
  /**
    @Test
    public void testGetCoupons() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        CTCouponsRequest ctOfferRequest = new CTCouponsRequest();
        CTCouponResponse ctOfferResponse = new CTCouponResponse();
        Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.<HttpEntity<?>>any(),
                ArgumentMatchers.<Class<String>>any())).thenReturn(returnOfferResponse());
        Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any())).thenReturn(returnOfferResponse());
        cpopClient.getCoupons(ctOfferRequest);

    }
    **/
    
}
