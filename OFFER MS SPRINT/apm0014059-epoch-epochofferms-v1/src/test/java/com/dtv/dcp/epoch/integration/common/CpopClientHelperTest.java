package com.dtv.dcp.epoch.integration.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.BuildMiddlewareCTFilters;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CpopClientHelperTest {
    @InjectMocks
    CpopClientHelper cpopClientHelper;
    
    @Mock
    RedisCacheHelper redisCacheHelper;
    
    @Mock
    OffersUtils offersUtils;
    
    @Mock
    CpopClient cpopClient;
    @Mock
    private FeatureManagerHelper featureManagerHelper;
    
    @Mock
    private BuildMiddlewareCTFilters middlewareFilters;

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
 
    
    
    /**
     * Setup.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetOffers() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        ObjectMapper objectMapperCusRes = new ObjectMapper();
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CGResponse cgResponse = new CGResponse();
        try {


            offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);

            // ctOfferResponse =  objectMapperRes.readValue(ctOfferedPromoResponse, CTOfferResponse.class);

            // ctOfferResponseProcessed =  objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
            ctOfferResponseProcessed = new DataReader().readFileToObj("offers/offeredPromotions.json",CTOfferResponse.class);;

        } catch(Exception e) {
            //do nothing
            // System.out.println("Error : "+e);
        }
        when(cpopClient.getOffers(any())).thenReturn(ctOfferResponseProcessed);
        cpopClientHelper.getOffers(ctOfferRequest);
    }
    @Test
	public void testGetOffersWithException() {
		when(cpopClient.getOffers(any())).thenThrow(new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR));
		CTOfferRequest ctOfferRequest = null;
		ServiceException ex = catchThrowableOfType(() -> cpopClient.getOffers(ctOfferRequest),
				ServiceException.class);

		assertThat(ex.getError().getErrorId()).isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR));
	}
    @Test
    public void testGetProducts() {
        CTProductRequest ctProductRequest = new CTProductRequest();
        CTProductResponse ctProductResponse = new CTProductResponse();

        when(cpopClient.getProducts(any())).thenReturn(ctProductResponse);
        cpopClientHelper.getProducts(ctProductRequest);
    }
    @Test
    public void testGetProductsWithException() {
        CTProductRequest ctProductRequest = new CTProductRequest();
        when(cpopClient.getProducts(any())).thenThrow(new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR));
        ServiceException ex = catchThrowableOfType(() -> cpopClientHelper.getProducts(ctProductRequest),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.OTT_CT_ERROR));
    }
    
    @Test
	public void testGetOffers_NullOfferResponse() throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		OfferRequest offerRequest = null;
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		offerRequest = objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
		BeanUtils.copyProperties(offerRequest, ctOfferRequest);
		when(cpopClient.getOffers(any())).thenReturn(null);
		ServiceException ex = catchThrowableOfType(() -> cpopClientHelper.getOffers(ctOfferRequest),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.OTT_CT_ERROR));

	}
    
    
    @Test
    public void testGetProducts_NullCtProductRes() {
        CTProductRequest ctProductRequest = new CTProductRequest();
        when(cpopClient.getProducts(any())).thenReturn(null);
        ServiceException ex = catchThrowableOfType(() -> cpopClientHelper.getProducts(ctProductRequest),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.OTT_CT_ERROR));
    }
    
    @Test
    public void testGetOffersWithCT400Error() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectMapper objectMapperRes = new ObjectMapper();
        ObjectMapper objectMapperCusRes = new ObjectMapper();
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CGResponse cgResponse = new CGResponse();
        try {


            offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);

            ctOfferResponseProcessed = new DataReader().readFileToObj("offers/offeredPromotions.json",CTOfferResponse.class);;

        } catch(Exception e) {
            //do nothing
            // System.out.println("Error : "+e);
        }
        
        try {
        String ErrorString = "{\n\"message\":\"edsp-validation-error\",\n\"code\":\"400\",\n\"error\":\"Validation Error\"\n}";
        HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST,ErrorString);
        when(cpopClient.getOffers(any())).thenThrow(httpClientErrorException);
        cpopClientHelper.getOffers(ctOfferRequest);
        } catch (Exception e)
        {
        	
        }
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
    
    @Test
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
        when(cpopClient.getOffers(any())).thenReturn(ctOfferResponse);
        cpopClientHelper.getOffers(ctOfferRequest);
    }
    
    @Test
    public void testGetProductsByType() {
    	
    	CTProductResponse segmentProductResponse = new CTProductResponse();
        try {
            segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response.json", CTProductResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        Mockito.when(cpopClient.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
        CTProductResponse ctProductResponse = cpopClientHelper.getProductsByType(Constants.SEGMENT);
        assertNotNull(ctProductResponse);
    }
  
	@Test
	public void testFilterProductsOnAccountToSuppressOffer() {
		CTOfferRequest offerRequest = new CTOfferRequest();
		offerRequest.setState("staged");
		offerRequest.setProductsOnAccountToSuppressOffer(Stream.of("u300").collect(Collectors.toList()));

		CTOfferResponse offerResponse = new CTOfferResponse();
		List<CTOffer> responseList = new ArrayList<>();
		CTOffer ctOffer = new CTOffer();
		OfferAttributes attributes = new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		attributes.setProductsOnAccountToSuppressOffer(Stream.of("u300").collect(Collectors.toList()));
		ctOffer.setAttributes(attributes);
		responseList.add(ctOffer);
		offerResponse.setOffers(responseList);
		CTProductResponse segmentProductResponse = new CTProductResponse();
		
		Mockito.when(cpopClient.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
		Mockito.when(cpopClient.getOffers(any())).thenReturn(offerResponse);
	
		CTOfferResponse filteredOfferResponse = cpopClientHelper.filterProductsOnAccountToSuppressOffer(offerResponse,
				offerRequest);
		assertNotNull(filteredOfferResponse);
		
	}

	@Test
	public void testCleanCTOfferRequest() {
		CTOfferRequest offerRequest = new CTOfferRequest();
	when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_MIDDLEWARECACHE_ENABLED)).thenReturn(true);
	
	Map<String, List<String>> epochGloablConfigMap = new LinkedHashMap<>();
    epochGloablConfigMap.put(Constants.OTT, Arrays.asList("2"));
    Mockito.when(cpopClient.loadGlobalConfigurations(any())).thenReturn(epochGloablConfigMap);
	//when(PropertyAccessorFactory.forBeanPropertyAccess(offerRequest).getPropertyValue("customerContext")).thenReturn(epochGloablConfigMap);
		CTOfferRequest cleanedOfferRequest = cpopClientHelper.cleanCTOfferRequest(offerRequest);
		assertNotNull(cleanedOfferRequest);
	}
	
	@Test
    public void testSetOfferLevel3DAttributesDIP() {
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
            offerRequest = new DataReader().readFileToObj("stms/validate_3d_attributes_request.json", OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);

            ctOfferResponse = new DataReader().readFileToObj("stms/validate_3d_attributes_response.json", CTOfferResponse.class);
            

        } catch (Exception e) {
            //do nothing
        }

        cpopClientHelper.setOfferLevel3DAttributes(ctOfferResponse, ctOfferRequest);
        assertNotNull(ctOfferResponse);

        Optional<CTOffer> ctOffer = ctOfferResponse.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-DISCOVERY-PLUS_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());
        
        Attributes attr = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes();
        assertEquals("directIntegrationPartnerSalesDisclosure-discovery" , attr.getDisclosureMessagesByKey().getDisclosure());
        assertEquals("directIntegrationPartnerSalesSelectAllDisclosure-discovery" , attr.getDisclosureMessagesByKey().getSelectAllDisclosure());
        assertEquals("directIntegrationPartnerSalesShortDisclosure-discovery" , attr.getDisclosureMessagesByKey().getShortDisclosure());
        assertEquals("directIntegrationPartnerSalesOptIn-discovery" , attr.getDisclosureMessagesByKey().getOptIn());
        assertEquals("directIntegrationPartnerSalesOptOut-discovery" , attr.getDisclosureMessagesByKey().getOptOut());
        assertEquals("directIntegrationPartnerSalesAutoRenew-discovery" , attr.getDisclosureMessagesByKey().getAutoRenew());
        assertEquals("directIntegrationPartnerSalesShortDescription-discovery" , attr.getDescriptionsByKey().getShortDesc());
        assertEquals("directIntegrationPartnerSalesLongDescription-discovery" , attr.getDescriptionsByKey().getLongDesc());
        assertEquals("directIntegrationPartnerSalesShortDisplayName-discovery" , attr.getDisplayNamesByKey().getShortDisplayName());
        assertEquals("directIntegrationPartnerSalesLongDisplayName-discovery" , attr.getDisplayNamesByKey().getLongDisplayName());
        
        ctOffer = ctOfferResponse.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-ALLBLK_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());
        
        attr = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes();
        assertEquals("defaultSalesDisclosure-allblk" , attr.getDisclosureMessagesByKey().getDisclosure());
        assertEquals("defaultSalesShortDescription-allblk" , attr.getDescriptionsByKey().getShortDesc());
        assertEquals("defaultSalesLongDescription-allblk" , attr.getDescriptionsByKey().getLongDesc());
        assertEquals("defaultSalesShortDisplayName-allblk" , attr.getDisplayNamesByKey().getShortDisplayName());
        assertEquals("defaultSalesLongDisplayName-allblk" , attr.getDisplayNamesByKey().getLongDisplayName());

    }
	
	@Test
    public void testSetOfferLevel3DAttributesOnline() {
        OfferRequest offerRequest = null;
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        try {
            offerRequest = new DataReader().readFileToObj("stms/validate_3d_attributes_request.json", OfferRequest.class);
            BeanUtils.copyProperties(offerRequest, ctOfferRequest);
            
            ctOfferRequest.getSalesChannel().clear();
            ctOfferRequest.setSalesChannel(Arrays.asList("online"));
            ctOfferRequest.setChannelEligibility(null);

            ctOfferResponse = new DataReader().readFileToObj("stms/validate_3d_attributes_response.json", CTOfferResponse.class);
            

        } catch (Exception e) {
            //do nothing
        }

        cpopClientHelper.setOfferLevel3DAttributes(ctOfferResponse, ctOfferRequest);
        assertNotNull(ctOfferResponse);
        
        Optional<CTOffer> ctOffer = ctOfferResponse.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-DISCOVERY-PLUS_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());
        
        Attributes attr = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes();
        assertEquals("defaultSalesDisclosure-discovery" , attr.getDisclosureMessagesByKey().getDisclosure());
        assertEquals("defaultSalesSelectAllDisclosure-discovery" , attr.getDisclosureMessagesByKey().getSelectAllDisclosure());
        assertEquals("defaultSalesShortDisclosure-discovery" , attr.getDisclosureMessagesByKey().getShortDisclosure());
        assertEquals("defaultSalesOptIn-discovery" , attr.getDisclosureMessagesByKey().getOptIn());
        assertEquals("defaultSalesOptOut-discovery" , attr.getDisclosureMessagesByKey().getOptOut());
        assertEquals("defaultSalesAutoRenew-discovery" , attr.getDisclosureMessagesByKey().getAutoRenew());
        assertEquals("defaultSalesShortDescription-discovery" , attr.getDescriptionsByKey().getShortDesc());
        assertEquals("defaultSalesLongDescription-discovery" , attr.getDescriptionsByKey().getLongDesc());
        assertEquals("defaultSalesShortDisplayName-discovery" , attr.getDisplayNamesByKey().getShortDisplayName());
        assertEquals("defaultSalesLongDisplayName-discovery" , attr.getDisplayNamesByKey().getLongDisplayName());
        
        ctOffer = ctOfferResponse.getOffers().stream().filter(Objects::nonNull)
                .filter(offer -> "OF_BOLTON-ALLBLK_satellite".equals(offer.getCode()))
                .findAny();
        assertTrue(ctOffer.isPresent());
        
        attr = ctOffer.get().getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes();
        assertEquals("defaultSalesDisclosure-allblk" , attr.getDisclosureMessagesByKey().getDisclosure());
        assertEquals("defaultSalesShortDescription-allblk" , attr.getDescriptionsByKey().getShortDesc());
        assertEquals("defaultSalesLongDescription-allblk" , attr.getDescriptionsByKey().getLongDesc());
        assertEquals("defaultSalesShortDisplayName-allblk" , attr.getDisplayNamesByKey().getShortDisplayName());
        assertEquals("defaultSalesLongDisplayName-allblk" , attr.getDisplayNamesByKey().getLongDisplayName());

    }

}
