package com.dtv.dcp.epoch.integration.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.BuildMiddlewareCTFilters;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopUCCClient;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.integration.OfferFilterService;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.Assert;

public class CpopUCCClientHelperTest {
    @InjectMocks
    CpopUCCClientHelper cpopClientHelper;

    @Mock
    CpopUCCClient cpopClient;
    
    @Mock
    CpopClient nonUCCClient;
    
    @Mock
    private FeatureManagerHelper featureManagerHelper;
    
    @Mock
    private BuildMiddlewareCTFilters middlewareFilters;
    
    @Mock
    private DMALookUpService dmaLookUpService;
    
    @Mock
	com.dtv.dcp.epoch.integration.CpopClientHelper clientHelper;
    
    @Mock
	OfferFilterService offerFilterService;
    
    private static final String OFFER_REQUEST_3D_SERVICES = "acquisition/offer_request_services.json";
    private static final String OFFER_RESPONSE_3D_SERVICES = "acquisition/offer_response_services.json";
    
    private static String OFFER_REQUEST = "{\n" +
            "    \"offerActionType\": [\n" +
            "        \"Retention\"\n" +
            "    ],\n" +
            "\"customerContext\": {\n" +
            "    \"existingProductFamily\": [\n" +
            "      \"wireless\",\n" +
            "      \"IPTV\",\n" +
            "      \"satellite\",\n" +
            "      \"OTT\"\n" +
            "    ],\n" +
            "    \"IPTV\": {\n" +
            "      \"accountNumber\": \"9999\",\n" +
            "      \"isActive\":true,\n" +
            "      \"sunsetDate\": \"02012020\",\n" +
            "      \"products\": [\n" +
            "        {\n" +
            "          \"productCode\": \"u300\",\n" +
            "          \"productType\": \"video-plan\"\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    \"satellite\": {\n" +
            "      \"accountNumber\": \"9999\",\n" +
            "      \"isActive\":true\n" +
            "    },\n" +
            "    \"wireless\": {\n" +
            "      \"accountNumber\": \"987665\",\n" +
            "      \"isActive\":true\n" +
            "\n" +
            "    },\n" +
            "    \"OTT\": {\n" +
            "      \"accountNumber\": \"9999\",\n" +
            "      \"isActive\":true,\n" +
            "      \"sunsetDate\": \"02012020\",\n" +
            "      \"products\": [\n" +
            "        {\n" +
            "          \"productCode\": \"u300\",\n" +
            "          \"productType\": \"video-plan\"\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  }," +
            "    \"offerProductFamily\": [\n" +
            "        \"OTT\"\n" +
            "    ],\n" +
            "    \"salesChannel\": [\n" +
            "        \"agent\"\n" +
            "    ],\n" +
            "    \"state\": \"staged\",\n" +
            "    \"pagination\": {\n" +
            "        \"page\": 1,\n" +
            "        \"limit\": 300\n" +
            "    },\n" +
            "    \"heartValue\": \"TV 1\"\n" +
            "}";

    private static String OFFER_REQUEST_SATELLITE_FILTER = "{\r\n" + 
    		"	\"contractIndicator\": [\r\n" + 
    		"		\"non-contract\"\r\n" + 
    		"	],\r\n" + 
    		"	\"customerContext\": {\r\n" + 
    		"		\"existingProductFamily\": [\r\n" + 
    		"			\"satellite\"\r\n" + 
    		"		],\r\n" + 
    		"		\"satellite\": {\r\n" + 
    		"			\"products\": [\r\n" + 
    		"				{\r\n" + 
    		"					\"billingProductCode\": \"P103351\",\r\n" + 
    		"					\"productType\": \"video-addon\"\r\n" + 
    		"				},\r\n" + 
    		"				{\r\n" + 
    		"					\"billingProductCode\": \"P2244\",\r\n" + 
    		"					\"productType\": \"video-addon\"\r\n" + 
    		"				}\r\n" + 
    		"				{\r\n" + 
    		"					\"billingProductCode\": \"B2072\",\r\n" + 
    		"                    \"billingReference ID\": \"206\",\r\n" +          
    		"					\"productType\": \"insurance\"\r\n" + 
    		"				}\r\n" + 
    		"			],\r\n" + 
    		"			\"accountNumber\": \"171202181454689\",\r\n" + 
    		"			\"isActive\": true,\r\n" + 
    		"			\"retentionPromotionIndicator\": false,\r\n" + 
    		"			\"freeTrialEligible\": false,\r\n" + 
    		"			\"accountType\": \"Residential\",\r\n" + 
    		"			\"nextBillingDate\": \"03/09/2020\",\r\n" + 
    		"			\"businessSegment\": \"REG\",\r\n" + 
    		"			\"employeeSegment\": \"\",\r\n" + 
    		"			\"nftvFlag\": \"\",\r\n" + 
    		"			\"ftvCount\": \"\"\r\n" + 
    		"		}\r\n" + 
    		"	},\r\n" + 
    		"	\"offerActionType\": [\r\n" + 
    		"		\"Upgrade\",\r\n" + 
    		"		\"Downgrade\",\r\n" + 
    		"		\"Cross-sell\",\r\n" + 
    		"		\"Other\",\r\n" + 
    		"		\"Acquisition\",\r\n" + 
    		"		\"retention\"\r\n" + 
    		"	],\r\n" + 
    		"	\"offerProductFamily\": [\r\n" + 
    		"		\"satellite\"\r\n" + 
    		"	],\r\n" + 
    		"	\"salesChannel\": [\r\n" + 
    		"		\"opus\",\r\n" + 
    		"		\"online\"\r\n" + 
    		"	],\r\n" + 
    		"	\"offerProductTypes\": [\r\n" + 
    		"		\"video-plan\",\r\n" + 
    		"		\"video-addon\"\r\n" +
    		"		\"insurance\"\r\n" +
    		"	]\r\n" + 
    		"}";
    
    private static String OFFER_REQUEST_ACCESSORY = "{\r\n" + 
    		"    \"contractIndicator\": [\r\n" + 
    		"        \"non-contract\"\r\n" + 
    		"    ],\r\n" + 
    		"    \"customerEligibility\": {\r\n" + 
    		"        \"zipCode\": [\r\n" + 
    		"            \"00058\"\r\n" + 
    		"        ],\r\n" + 
    		"        \"county\": [\r\n" + 
    		"            \"17000\"\r\n" + 
    		"        ]\r\n" + 
    		"    },\r\n" + 
    		"    \"customerContext\": {\r\n" + 
    		"        \"existingProductFamily\": [\r\n" + 
    		"            \"satellite\"\r\n" + 
    		"        ],\r\n" + 
    		"        \"satellite\": {\r\n" + 
    		"            \"products\": [\r\n" + 
    		"                {\r\n" + 
    		"                    \"billingProductCode\": \"P100626\",\r\n" + 
    		"                    \"billingReferenceID\": \"1\",\r\n" + 
    		"                    \"productType\": \"video-plan\"\r\n" + 
    		"                },\r\n" + 
    		"                {\r\n" + 
    		"                    \"billingProductCode\": \"P6688\",\r\n" + 
    		"                    \"billingReferenceID\": \"1\",\r\n" + 
    		"                    \"productType\": \"video-addon\"\r\n" + 
    		"                }\r\n" + 
    		"            ],\r\n" + 
    		"            \"accountNumber\": \"171202181454689\",\r\n" + 
    		"            \"isActive\": true,\r\n" + 
    		"            \"retentionPromotionIndicator\": false,\r\n" + 
    		"            \"freeTrialEligible\": false,\r\n" + 
    		"            \"accountType\": \"Residential\",\r\n" + 
    		"            \"nextBillingDate\": \"03/09/2020\",\r\n" + 
    		"            \"businessSegment\": \"REG\"\r\n" + 
    		"        }\r\n" + 
    		"    },\r\n" + 
    		"    \"offerActionType\": [\r\n" + 
    		"        \"Upgrade\",\r\n" + 
    		"        \"Downgrade\",\r\n" + 
    		"        \"Cross-sell\",\r\n" + 
    		"        \"Other\",\r\n" + 
    		"        \"Acquisition\",\r\n" + 
    		"        \"retention\"\r\n" + 
    		"    ],\r\n" + 
    		"    \"offerProductFamily\": [\r\n" + 
    		"        \"satellite\"\r\n" + 
    		"    ],\r\n" + 
    		"    \"salesChannel\": [       \r\n" + 
    		"        \"online\"\r\n" + 
    		"    ],\r\n" + 
    		"    \"offerProductTypes\": [        \r\n" + 
    		"        \"video-accessory\"\r\n" + 
    		"    ]\r\n" + 
    		"}";
    
    /**
     * Setup.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testApplyMiddlewareFilters() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        ObjectMapper objectMapperCusRes = new ObjectMapper();
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CGResponse cgResponse = new CGResponse();
        try {


            offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);
            ctOfferResponse = new DataReader().readFileToObj("CTSingleOffer.json",CTOfferResponse.class);

        } catch(Exception e) {
            //do nothing
        }
        //cpopClientHelper.applyMiddlewareFilters(ctOfferResponse,ctOfferRequest);
    }
    
    //@Test
    public void testGetOffers() {
        ObjectMapper objectMapper = new ObjectMapper();
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
            offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);
            ctOfferResponseProcessed = new DataReader().readFileToObj("offers/offeredPromotions.json",CTOfferResponse.class);;
        } catch(Exception e) {
            //do nothing
        }
        Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        when(cpopClient.getOffers(any())).thenReturn(ctOfferResponseProcessed);
        when(nonUCCClient.getOffers(any())).thenReturn(ctOfferResponseProcessed);
        
        cpopClientHelper.getOffers(ctOfferRequest);
    }
    
   // @Test
    public void testGetOffersSetInlineProducts() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        ObjectMapper objectMapperCusRes = new ObjectMapper();
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CGResponse cgResponse = new CGResponse();
        try {


            offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);
            ctOfferResponse = new DataReader().readFileToObj("CTSingleOffer.json",CTOfferResponse.class);

        } catch(Exception e) {
            //do nothing
        }
      Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        when(cpopClient.getOffers(any())).thenReturn(ctOfferResponse);
        when(nonUCCClient.getOffers(any())).thenReturn(ctOfferResponse);
        cpopClientHelper.getOffers(ctOfferRequest);
    }
    
    @Test
    public void testSetInlineProductsWithActiveCustomerFilter() {
    	
        ObjectMapper objectMapper = new ObjectMapper();
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        
        try {

            offerRequest =  objectMapper.readValue(OFFER_REQUEST_SATELLITE_FILTER, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);
            
            Mockito.when(dmaLookUpService.hasLocalChannels(Mockito.anyString(),Mockito.anyString())).thenReturn(true);
            Boolean hasLocalChannels = dmaLookUpService.hasLocalChannels("00086", "55000");
            offerRequest.setHasLocalChannels(hasLocalChannels);
            
            List<CTCustomerContext> customerContextList=new ArrayList<CTCustomerContext>();
            List<com.dtv.dcp.epoch.model.ct.request.Product> productList=new ArrayList<com.dtv.dcp.epoch.model.ct.request.Product>();
            List<ProductInfo> productInfoList=offerRequest.getCustomerContext().getSatellite().getProducts();
            for (ProductInfo productInfo : productInfoList) {
            	com.dtv.dcp.epoch.model.ct.request.Product product=new com.dtv.dcp.epoch.model.ct.request.Product();
            	product.setBillingProductCode(productInfo.getBillingProductCode());
            	productList.add(product);
			}
            
			CTCustomerContext ctCustomerContext = new CTCustomerContext();
			ctCustomerContext.setProducts(productList);
			customerContextList.add(ctCustomerContext);
            ctOfferRequest.setCustomerContext(customerContextList);
            ctOfferRequest.setHasLocalChannels(hasLocalChannels);
            
            CTOfferResponse ctOfferResponseMock = new DataReader().readFileToObj("CTSingleOfferWithPriceFilterAttributes.json",CTOfferResponse.class);

            Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
            when(cpopClient.getOffers(any())).thenReturn(ctOfferResponseMock);
            
            ctOfferResponse = cpopClient.getOffers(ctOfferRequest);
            
            cpopClientHelper.setInlineProducts(ctOfferResponse);
             
            List<CTOffer> ctOfferList  = ctOfferResponse.getOffers();
            Assert.assertEquals(1, ctOfferList.size());
            
        } catch(Exception e) {
            //do nothing
        }
       
    }
        
    @Test
	public void testGetOffersWithException() {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperCusRes = new ObjectMapper();
		OfferRequest offerRequest = null;
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CGResponse cgResponse = new CGResponse();
		try {

			offerRequest = objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);

			// ctOfferResponse = objectMapperRes.readValue(ctOfferedPromoResponse,
			// CTOfferResponse.class);

			// ctOfferResponseProcessed = objectMapperRes.readValue(OFFER_RESPONSE,
			// CTOfferResponse.class);
			ctOfferResponseProcessed = new DataReader().readFileToObj("offers/offeredPromotions.json",
					CTOfferResponse.class);
			;

		} catch (Exception e) {
			// do nothing
			// System.out.println("Error : "+e);
		}
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
		when(cpopClient.getOffers(any())).thenThrow(new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR));
		ServiceException ex = catchThrowableOfType(() -> cpopClientHelper.getOffers(ctOfferRequest),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.OTT_CT_ERROR));
	}
    
    @Test
	public void testSetOfferLevel3DAttributes() {
		OfferRequest offerRequest = new DataReader().readFileToObj(OFFER_REQUEST_3D_SERVICES, OfferRequest.class);
		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj(OFFER_RESPONSE_3D_SERVICES,
				CTOfferResponse.class);
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		BeanUtils.copyProperties(offerRequest, ctOfferRequest);
		when(nonUCCClient.getOffers(any())).thenReturn(ctOfferResponse);
		Mockito.doCallRealMethod().when(clientHelper).setOfferLevel3DAttributes(any(), any());
		when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_MIDDLEWARECACHE_ENABLED)).thenReturn(true);
		CTOfferResponse ctOfferResponse1= cpopClientHelper.getOffers(ctOfferRequest);
		
		Optional<CTOffer> ctOffer_3D = ctOfferResponse1.getOffers().stream().filter(Objects::nonNull)
				.filter(offer -> "OF_BOLTON-TV-GLOBO_satellite".equals(offer.getCode())
						&& Objects.nonNull(offer.getAttributes()))
				.findAny();
		assertThat(ctOffer_3D).isPresent();
		
		Assert.assertEquals("3D Disclosure for Services",
				ctOffer_3D.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()
						.get(0).getObj().getVariants().get(0).getAttributes().getDisclosureMessagesByKey()
						.getDisclosure());
		
	}
    
}
