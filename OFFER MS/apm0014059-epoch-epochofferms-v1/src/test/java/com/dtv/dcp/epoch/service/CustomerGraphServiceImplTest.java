package com.dtv.dcp.epoch.service;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.customergraph.CustomerGraphClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGServiceInfo;
import com.dtv.dcp.epoch.model.customergraph.response.UVAccountProductsResponse;
import com.dtv.dcp.epoch.model.customergraph.response.UVCustomerAccountResponse;
import com.dtv.dcp.epoch.resource.OffersResourceImpl;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphServiceImpl;
import com.dtv.dcp.epoch.service.ott.OttOffersServiceImpl;
import com.dtv.dcp.epoch.util.CustomerGraphProductsHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.TestUtility;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;


public class CustomerGraphServiceImplTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Mock
    private CustomerGraphClient customerGraphDTVNowClientImpl;

    @InjectMocks
    private CustomerGraphServiceImpl customerGraphDTVNowServiceImpl;

    @Mock
    private CustomerGraphServiceImpl customerGraphService;


    /**
     * The OffersResource resource.
     */
    @InjectMocks
    private OffersResourceImpl offersResourceImpl;

    @Mock
    private OttOffersServiceImpl ottOffersService;

    @Mock
    CustomerGraphProductsHelper customerGraphProductsHelper;

    /**
     * The headers.
     */
    HttpHeaders headers;

    OfferRequest offerRequest;

    /** The uri info. */
//    @Mock
//    UriInfo mUriInfo;

    /**
     * Setup.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        headers = Mockito.mock(HttpHeaders.class);
    }

    @Test
    public void testAccountInfo() {
        CGResponse cgresponse = new CGResponse();
        cgresponse.setAccountId("432432432432");

        when(customerGraphService.getActiveSubscriptions("432432432432", "")).thenReturn(cgresponse);
        CGResponse response = customerGraphService.getActiveSubscriptions("432432432432", "");
        assertNotNull(response);
    }

    @Test
    public void testGetOffersServiceException() throws ServiceException {
        OfferRequest offerRequest = new OfferRequest();
        List<String> offerProductFamily = new ArrayList<String>();
        offerProductFamily.add("OTT");
        offerRequest.setOfferProductFamily(offerProductFamily);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setCount(20);
        try {
            customerGraphDTVNowServiceImpl.getActiveSubscriptions("", "");
        } catch (Exception e) {

        }
    }

    @Test
    public void testGetActiveSubscriptions() throws Exception {
        String accountId = "accountId";
        JsonNode response = createAccountAndServicesJsonNode();

        when(customerGraphDTVNowClientImpl.getAccountById(accountId)).thenReturn(response);

        CGResponse accountServices = customerGraphDTVNowServiceImpl.getActiveSubscriptions(accountId, "");
        assertEquals(2, accountServices.getServiceInfo().length);
    }

    @Test
    public void testGetActiveSubscriptionsWithException() throws Exception {
        String accountId = "accountId";
        try {
            customerGraphDTVNowServiceImpl.getActiveSubscriptions(accountId, "");
        } catch (Exception e) {

        }
    }

    @Test
    public void testGetUverseAccountProducts() throws Exception {
        String accountId = "629011272";
        String accountType = "uverse";
        boolean includeAssignedProductDetails = true;
        UVAccountProductsResponse accountProductsResponse = JsonService.getObjectFromJson(TestUtility.loadJson(
                "/Uverse_Response_251461578.json"), UVAccountProductsResponse.class);

        when(customerGraphDTVNowClientImpl.getUverseAccountProducts(accountId, accountId, accountType, includeAssignedProductDetails, UVAccountProductsResponse.class)).thenReturn(accountProductsResponse);
        UVAccountProductsResponse uvAccountResp = customerGraphDTVNowServiceImpl.getUverseAccountProducts(accountId, accountId, accountType, includeAssignedProductDetails, UVAccountProductsResponse.class, "");
        assertNotNull(uvAccountResp);
    }

    @Test
    public void testGetUverseAccountProductsWithException() throws Exception {
        String accountId = "629011272";
        String accountType = "uverse";
        boolean includeAssignedProductDetails = true;
        ServiceException se = new ServiceException(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "GetUverseCustomerAccounts: ");
        when(customerGraphDTVNowClientImpl.getUverseAccountProducts(accountId, accountId, accountType, includeAssignedProductDetails, UVAccountProductsResponse.class)).thenThrow(se);
        try {
            customerGraphDTVNowServiceImpl.getUverseAccountProducts(accountId, accountId, accountType,
                    includeAssignedProductDetails, UVAccountProductsResponse.class, "");
        } catch (Exception e) {

        }
    }

    @Test
    public void testGetUverseCustomerAccounts() throws Exception {
        String accountId = "629011272";
        String accountType = "uverse";
        UVCustomerAccountResponse customerAccountsResponse = JsonService.getObjectFromJson(TestUtility.loadJson(
                "/Uverse_Response_IptvSunsetDate.json"), UVCustomerAccountResponse.class);

        when(customerGraphDTVNowClientImpl.getUverseCustomerAccounts(accountId, accountId, accountType, UVCustomerAccountResponse.class)).thenReturn(customerAccountsResponse);
        UVCustomerAccountResponse uvCustAccountResp = customerGraphDTVNowServiceImpl.getUverseCustomerAccounts(accountId, accountId, accountType, UVCustomerAccountResponse.class, "");
        assertNotNull(uvCustAccountResp);
    }

    @Test
    public void testGetUverseCustomerAccountsWithException() throws Exception {
        String accountId = "629011272";
        String accountType = "uverse";
        ServiceException se = new ServiceException(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "GetUverseCustomerAccounts: ");
        when(customerGraphDTVNowClientImpl.getUverseCustomerAccounts(accountId, accountId, accountType, UVCustomerAccountResponse.class)).thenThrow(se);
        try {
            customerGraphDTVNowServiceImpl.getUverseCustomerAccounts(accountId, accountId, accountType,
                    UVCustomerAccountResponse.class, "");
        } catch (Exception e) {

        }
    }

    private List<CGServiceInfo> createDtvNowAccountServices() {
        CGServiceInfo subscription1 = new CGServiceInfo();
        CGServiceInfo subscription2 = new CGServiceInfo();
        List<CGServiceInfo> accountServices = Arrays.asList(subscription1, subscription2);

//        DtvPromotion dtvPromotion = new DtvPromotion();
//        List<DtvPromotion> promotions = Arrays.asList(dtvPromotion);

        // subscription1.setPromotion(promotions);

        return accountServices;
    }

    private JsonNode createAccountAndServicesJsonNode() {
        JsonNode accountServicesNode = null;
        ObjectMapper mapper = new ObjectMapper();
        List<CGServiceInfo> accountServices = createDtvNowAccountServices();
        Map<String, Object> contentMap = new HashMap<>();
        Map<String, Object> serviceInfoMap = new HashMap<>();

        serviceInfoMap.put("serviceInfo", accountServices);
        contentMap.put("content", serviceInfoMap);

        try {
            String accountServicesString = mapper.writeValueAsString(contentMap);
            accountServicesNode = mapper.readTree(accountServicesString);
        } catch (IOException e) {

        }

        return accountServicesNode;
    }

}
