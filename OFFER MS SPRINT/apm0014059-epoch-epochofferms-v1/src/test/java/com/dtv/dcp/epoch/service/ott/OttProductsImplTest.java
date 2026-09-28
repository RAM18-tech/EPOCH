package com.dtv.dcp.epoch.service.ott;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPDevicesHelper;
import com.dtv.dcp.epoch.processor.ott.OttProductsProcessor;
import com.dtv.dcp.epoch.util.CTOfferRequestHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.TestUtility;
import com.fasterxml.jackson.databind.ObjectMapper;
@ExtendWith(MockitoExtension.class)
public class OttProductsImplTest {




//    @Mock
//    private OttServicesOffersProcessor ottServicesOffersProcessor;

    @Mock
    OttProductsProcessor ottProductsProcessor;

    @Mock
    CTOfferRequestHelper ctOfferRequestHelper;



    @InjectMocks
    private OttProductsImpl ottProducts;

    @Mock
    CPOPDevicesHelper cpopDevicesHelper;

    @Mock
    CpopClient cpopClient;

    /** The headers. */
    HttpHeaders headers;


    private static String RETENTION_REQUEST = "{\n" +
            "    \"offerActionType\": [\n" +
            "        \"Retention\"\n" +
            "    ],\n" +
            "    \"offerProductFamily\": [\n" +
            "        \"OTT\"\n" +
            "    ],\n" +
            "    \"salesChannel\": [\n" +
            "        \"opus\", \"online\"\n" +
            "    ],\n" +
            "    \"state\": \"staged\",\n" +
            "    \"pagination\": {\n" +
            "        \"page\": 1,\n" +
            "        \"limit\": 300\n" +
            "    },\n" +
            "    \"heartValue\": \"TV 1\"\n" +
            "}";
    private static String BENEFIT_RESPONSE = "{\n" +
            "    \"limit\": 200,\n" +
            "    \"offset\": 0,\n" +
            "    \"count\": 1,\n" +
            "    \"total\": 1,\n" +
            "    \"benefits\": [\n" +
            "        {\n" +
            "            \"duration\": 25,\n" +
            "            \"billingBenefitCode\": \"BC_Cpop_percoff_1\",\n" +
            "            \"promoSourceSkuProductType\": \"video-plan\",\n" +
            "            \"beneficiaryProductFamily\": \"OTT\",\n" +
            "            \"billAfterPeriod\": true,\n" +
            "            \"benefitType\": \"percent-off\",\n" +
            "            \"promoSourceSkuProductFamily\": \"OTT\",\n" +
            "            \"contractIndicator\": \"non-contract\",\n" +
            "            \"promoPriority\": 4,\n" +
            "            \"benefitDisplayName\": {\n" +
            "                \"en\": \"BDNCpop_percoff_1\"\n" +
            "            },\n" +
            "            \"beneficiaryProductType\": [\n" +
            "                \"video-plan\"\n" +
            "            ],\n" +
            "            \"benefitLevel\": \"account\",\n" +
            "            \"freeTrialPromo\": false,\n" +
            "            \"period\": \"NoOfDays\",\n" +
            "            \"benefitCategory\": \"Product\",\n" +
            "            \"applicableProducts\": [\n" +
            "                {\n" +
            "                    \"products\": [\n" +
            "                        {\n" +
            "                            \"typeId\": \"product\",\n" +
            "                            \"id\": \"30087085-93a0-472d-a3ff-1ab543c6c348\",\n" +
            "                            \"key\": \"BASE-PLUS-2018\",\n" +
            "                            \"productType\": \"video-plan\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ],\n" +
            "            \"id\": \"cc6b7b22-27b8-4863-b5d8-47a893fafd9a\",\n" +
            "            \"version\": 2,\n" +
            "            \"code\": \"BC_Cpop_percoff_1\",\n" +
            "            \"benefitName\": {\n" +
            "                \"en\": \"BN_Cpop_percoff_1\"\n" +
            "            },\n" +
            "            \"description\": {\n" +
            "                \"en\": \"\"\n" +
            "            },\n" +
            "            \"startDate\": \"2019-10-16T00:00:00.000Z\",\n" +
            "            \"endDate\": \"2019-11-01T00:00:00.000Z\",\n" +
            "            \"value\": {\n" +
            "                \"percentage\": 25\n" +
            "            },\n" +
            "            \"isQuotaBased\": false\n" +
            "        }\n" +
            "    ]\n" +
            "}";


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        headers = Mockito.mock(HttpHeaders.class);

    }




    @Test
    public void testGetProducts() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        // ProductRequest productRequest = null;
       // CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
        CTProductRequest ctProductRequest = new CTProductRequest();
       // JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);

        ProductRequest productRequest = JsonService.getObjectFromJson(TestUtility.loadJson("/getProductRequest.json"), ProductRequest.class);
        try {

            // productRequest =  objectMapper.readValue(RETENTION_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(productRequest, ctProductRequest);

            // ctBenefitsResponse =  objectMapperRes.readValue(BENEFIT_RESPONSE, CTBenefitsResponse.class);
            // JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);//objectMapperRes.readValue(BENEFIT_RESPONSE, JsonNode.class);
        } catch(Exception e) {
            //do nothing
        }

        productRequest.setProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
        productRequest.setProductCodes(Arrays.asList("BASE-PLUS-2018"));

        Mockito.lenient().when(headers.getContentType()).thenReturn(MediaType.valueOf("application/json"));
        Mockito.lenient().when(headers.getFirst("uuid")).thenReturn("123456");

        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LOGGEDINID)).thenReturn("test_idpctx-loggedInId");
        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)).thenReturn("test_idpctx-linkedWirelessAccNums");
        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)).thenReturn("test_idpctx-linkedDirectvNowAccNums");
        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("test_idpctx-session-id");
        Mockito.lenient().when(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)).thenReturn("test_Idpctx-linkeduverseaccnums");

        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();

        CTProductResponse ctProductResponse = new CTProductResponse();
        List<ProductObj> responseList= new ArrayList<>();
        ProductObj ctProduct=new ProductObj();
        OfferAttributes attributes=new OfferAttributes();
        attributes.setOfferProductSubtype(Constants.BASE);
        // ctProduct.setA(attributes);
        responseList.add(ctProduct);
        ctProductResponse.setProducts(responseList);
        productRequestWrapper.setProductRequest(productRequest);

        Mockito.lenient().doReturn(ctProductResponse).when(cpopClient).getProducts(Mockito.any());
        Mockito.lenient().when(ctOfferRequestHelper.preProcessProductRequest(any(), any() )).thenReturn(productRequestWrapper);
        // doReturn(ctOfferResponse).when(ottServicesOffersProcessor).getOffers(Mockito.any());
        when(ottProductsProcessor.isValidDevicesRequest( any() )).thenReturn(true);
        when(ottProductsProcessor.isValidProductRequest( any() )).thenReturn(true);
        when(ottProductsProcessor.getProducts( any() )).thenReturn(ctProductResponse);
        Mockito.lenient(). when(cpopDevicesHelper.getDevices( any() )).thenReturn(any());
        ottProducts.getProducts( headers , productRequestWrapper);

    }

}
