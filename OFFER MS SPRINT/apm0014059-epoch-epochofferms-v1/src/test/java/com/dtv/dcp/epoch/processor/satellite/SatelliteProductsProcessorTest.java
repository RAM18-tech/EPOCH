package com.dtv.dcp.epoch.processor.satellite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.product.Price;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class SatelliteProductsProcessorTest {

	@Mock
	CpopClientHelper cpopclient;
	
	@InjectMocks
	SatelliteProductsProcessor prodProcessor;

	@Mock
	OffersUtils offersUtils;
	
	@Mock
	RedisCacheHelper redisCacheHelper;
	
	private static String PRODUCT_REQUEST_BPC = "{\n" + 
			"    \"codes\": [\n" + 
			"        {\n" + 
			"            \"billingProductCode\": \"R107114\",\n" + 
			"            \"billingReferenceId\": \"1\",\n" + 
			"        }\n" + 
			"    ],\n" + 
			"    \"productFamily\": [\n" + 
			"        \"satellite\"\n" + 
			"    ]\n" + 
			"}";
	private static String PRODUCT_REQUEST_PPC = "{\n" + 
			"    \"codes\": [\n" + 
			"        {\n" + 
			"            \"pricePlanCode\": \"88952763\",\n" + 
			"            \"billingReferenceId\": \"1\",\n" + 
			"        }\n" + 
			"    ],\n" + 
			"    \"productFamily\": [\n" + 
			"        \"satellite\"\n" + 
			"    ]\n" + 
			"}";
	
	private static String PRODUCT_REQUEST_PPC_PC = "{\n" + 
			"    \"codes\": [\n" + 
			"        {\n" + 
			"            \"pricePlanCode\": \"88890395\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"pricePlanCode\": \"88952763\"\n" + 
			"        },\n" + 
			"        {\n" + 
			"            \"pricePlanCode\": \"88830604\",\n" + 
			"            \"pickCode\": \"P0030\"\n" + 
			"        }\n" + 
			"    ],\n" + 
			"    \"policy\": \"DVANA1\",\n" + 
			"    \"productFamily\": [\n" + 
			"        \"satellite\"\n" + 
			"    ]\n" + 
			"}";
	
	private static String stms_product_request = "products/satellite/gnfProduct_stms_request.json";
	private static String stms_product_response = "products/satellite/gnfProduct_stms_response.json";
	private static String enabler_product_request = "products/satellite/gnfProduct_enabler_request.json";
	private static String enabler_product_response = "products/satellite/gnfProduct_enabler_response.json";
	
	private static String PRODUCT_REQUEST_ENABLER = "enabler/product_request_enabler.json";
	private static String PRODUCT_RESPONSE_ENABLER = "enabler/product_response_enabler.json";
	private static String PRODUCT_REQUEST_SUBCATEGORY_FILTER = "enabler/product_request_subcategory_filter.json";
	private static String PRODUCT_RESPONSE_SUBCATEGORY_FILTER = "enabler/product_response_subcategory_filter.json";
	
	private static String PRODUCT_REQUEST_RR_STMS = "products/satellite/product_request_rr_stms.json";
	private static String PRODUCT_REQUEST_RR_ENABLER = "products/satellite/product_request_rr_enabler.json";
	private static String PRODUCT_RESPONSE_RR = "products/satellite/product_response_rr.json";
	
	private static String PRODUCT_REQUEST_SUBCATEGORY_PRICE_PEACOCK_1 = "stms/product_request_subcategory_price_peacock_1.json";
	private static String PRODUCT_REQUEST_SUBCATEGORY_PRICE_PEACOCK_15 = "stms/product_request_subcategory_price_peacock_15.json";
	private static String PRODUCT_RESPONSE_SUBCATEGORY_PRICE_PEACOCK = "stms/product_response_subcategory_price_peacock.json";
	
	
	@Test
	public void testGetProducts() {
		
		ProductRequestWrapper productRequestWrapper= new ProductRequestWrapper();
		assertNull(prodProcessor.getProducts(productRequestWrapper));
	}
	
	@Test
	public void testGetProductsBillingProductCode() {		
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ObjectMapper objectMapper = new ObjectMapper();
		ProductRequest productRequest = new ProductRequest();
		
		try {
			productRequest = objectMapper.readValue(PRODUCT_REQUEST_BPC, ProductRequest.class);
			productRequestWrapper.setProductRequest(productRequest);
		} catch (Exception e) {
		}
		
		CTProductResponse ctProductResponse = new DataReader().readFileToObj("products-ppc.json", CTProductResponse.class);
		
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		assertEquals(1, productResponse.getProducts().size());
	}
	
	@Test
	public void testGetProductsPricePlanCode() {		
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ObjectMapper objectMapper = new ObjectMapper();
		ProductRequest productRequest = new ProductRequest();
		
		try {
			productRequest = objectMapper.readValue(PRODUCT_REQUEST_PPC, ProductRequest.class);
			productRequestWrapper.setProductRequest(productRequest);
		} catch (Exception e) {
		}
		
		CTProductResponse ctProductResponse = new DataReader().readFileToObj("products-ppc.json", CTProductResponse.class);
		
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		assertEquals(1, productResponse.getProducts().size());
	}
	
	@Test
	public void testGetProductsPricePlanCodePickCode() {		
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ObjectMapper objectMapper = new ObjectMapper();
		ProductRequest productRequest = new ProductRequest();
		
		try {
			productRequest = objectMapper.readValue(PRODUCT_REQUEST_PPC_PC, ProductRequest.class);
			productRequestWrapper.setProductRequest(productRequest);
		} catch (Exception e) {
		}
		
		CTProductResponse ctProductResponse = new DataReader().readFileToObj("products-ppc-pc.json", CTProductResponse.class);
		
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		assertEquals(3, productResponse.getProducts().size());
	}
	
	@Test
	public void testIsAvailableAttributeForSTMS() {	
		
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ProductRequest productRequest =new DataReader().readFileToObj(stms_product_request, ProductRequest.class);
		productRequestWrapper.setProductRequest(productRequest);
		
		CTProductResponse ctProductResponse = new DataReader().readFileToObj(stms_product_response, CTProductResponse.class);
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		assertEquals(6, productResponse.getProducts().size());
		Optional<ProductObj> product = productResponse.getProducts().stream()
				.filter(Objects::nonNull).filter(p -> "BASE-FAMILY_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
		assertTrue(product.get().getVariants().get(0).getAttributes().getIsAvailable().equals(false));
	}
	
	@Test
	public void testIsAvailableAttributeForEnabler() {	
		
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ProductRequest productRequest =new DataReader().readFileToObj(enabler_product_request, ProductRequest.class);
		productRequestWrapper.setProductRequest(productRequest);
		
		CTProductResponse ctProductResponse = new DataReader().readFileToObj(enabler_product_response, CTProductResponse.class);
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		assertEquals(1, productResponse.getProducts().size());
		Optional<ProductObj> product = productResponse.getProducts().stream()
				.filter(Objects::nonNull).filter(p -> "BASE-FAMILY_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
		assertTrue(product.get().getVariants().get(0).getAttributes().getIsAvailable().equals(false));
	}
	
	@Test
    public void testGetProductsByType() {
    	
    	CTProductResponse segmentProductResponse = new CTProductResponse();
        try {
            segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response.json", CTProductResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        Mockito.when(cpopclient.getProductsByType(Constants.SEGMENT)).thenReturn(segmentProductResponse);
        CTProductResponse ctProductResponse = prodProcessor.getProductsByType(Constants.SEGMENT);
        assertNotNull(ctProductResponse);
    }
	
	@Test
	public void testGetProductsNoPP() {	
		
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ProductRequest productRequest =new DataReader().readFileToObj(PRODUCT_REQUEST_ENABLER, ProductRequest.class);
		productRequestWrapper.setProductRequest(productRequest);
		
		CTProductResponse ctProductResponse = new DataReader().readFileToObj(PRODUCT_RESPONSE_ENABLER, CTProductResponse.class);
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		assertEquals(1, productResponse.getProducts().size());
		Optional<ProductObj> product = productResponse.getProducts().stream()
				.filter(Objects::nonNull).filter(p -> "BASE-PREFERRED-CHOICE-ALL-INCLUDED_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
		assertNotNull(product.get());
		Optional<Price> price = product.get().getVariants().get(0).getPrices().stream().filter(Objects::nonNull)
				.filter(p -> Objects.nonNull(p.getCustomerGroup()) && p.getCustomerGroup().contains("-PNP"))
				.findFirst();
		assertFalse(price.isPresent());
	}

	@Test
	public void testGetProductsSubCategoryFilter() {

		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ProductRequest productRequest =new DataReader().readFileToObj(PRODUCT_REQUEST_SUBCATEGORY_FILTER, ProductRequest.class);
		productRequestWrapper.setProductRequest(productRequest);

		CTProductResponse ctProductResponse = new DataReader().readFileToObj(PRODUCT_RESPONSE_SUBCATEGORY_FILTER, CTProductResponse.class);
		doCallRealMethod().when(offersUtils).processProducts(any(),any());
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		Optional<ProductObj> product = productResponse.getProducts().stream()
				.filter(Objects::nonNull).filter(p -> "BOLTON-ACORN-TV_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
		assertNotNull(product);
		assertEquals("Popular",product.get().getVariants().get(0).getAttributes().getSubCategory());
	}
	@Test
	public void testGetProductsRoadrunnerSTMS() {

		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ProductRequest productRequest =new DataReader().readFileToObj(PRODUCT_REQUEST_RR_STMS, ProductRequest.class);
		productRequestWrapper.setProductRequest(productRequest);

		CTProductResponse ctProductResponse = new DataReader().readFileToObj(PRODUCT_RESPONSE_RR, CTProductResponse.class);
		Mockito.when(redisCacheHelper.getValues("localsBoltonPrice", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("12"));
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		doCallRealMethod().when(offersUtils).getTwoDigitRoundOffValue(any());
		
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		Optional<ProductObj> product = productResponse.getProducts().stream()
				.filter(Objects::nonNull).filter(p -> "BASE-ULTIMATE-RR_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
		assertNotNull(product);
		assertTrue(product.get().getVariants().get(0).getPrices().get(0).getPlanPricewithLocals().equals(185.0));
	}
	
	@Test
	public void testGetProductsRoadrunnerEnabler() {

		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ProductRequest productRequest =new DataReader().readFileToObj(PRODUCT_REQUEST_RR_ENABLER, ProductRequest.class);
		productRequestWrapper.setProductRequest(productRequest);

		CTProductResponse ctProductResponse = new DataReader().readFileToObj(PRODUCT_RESPONSE_RR, CTProductResponse.class);
		Mockito.when(redisCacheHelper.getValues("localsBoltonPrice", Constants.SATELLITE_PRODUCT_FAMILY)).thenReturn(Arrays.asList("12"));
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		doCallRealMethod().when(offersUtils).getTwoDigitRoundOffValue(any());
		
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		Optional<ProductObj> product = productResponse.getProducts().stream()
				.filter(Objects::nonNull).filter(p -> "BASE-ULTIMATE-RR_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
		assertNotNull(product);
		assertTrue(product.get().getVariants().get(0).getPrices().get(0).getPlanPricewithLocals().equals(185.0));
	}
	
	@Test
	public void testGetProductsSubCategoryFilterPrice1() {

		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ProductRequest productRequest =new DataReader().readFileToObj(PRODUCT_REQUEST_SUBCATEGORY_PRICE_PEACOCK_1, ProductRequest.class);
		productRequestWrapper.setProductRequest(productRequest);

		CTProductResponse ctProductResponse = new DataReader().readFileToObj(PRODUCT_RESPONSE_SUBCATEGORY_PRICE_PEACOCK, CTProductResponse.class);
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		Optional<ProductObj> product = productResponse.getProducts().stream()
				.filter(Objects::nonNull).filter(p -> "BOLTON-PEACOCK_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
		assertNotNull(product);
		assertEquals("Streaming",product.get().getVariants().get(0).getAttributes().getSubCategory());
	}
	
	@Test
	public void testGetProductsSubCategoryFilterPrice15() {

		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ProductRequest productRequest =new DataReader().readFileToObj(PRODUCT_REQUEST_SUBCATEGORY_PRICE_PEACOCK_15, ProductRequest.class);
		productRequestWrapper.setProductRequest(productRequest);

		CTProductResponse ctProductResponse = new DataReader().readFileToObj(PRODUCT_RESPONSE_SUBCATEGORY_PRICE_PEACOCK, CTProductResponse.class);
		when(cpopclient.getProducts(any())).thenReturn(ctProductResponse);
		CTProductResponse productResponse = prodProcessor.getProducts(productRequestWrapper);
		assertNotNull(productResponse);
		assertNotNull(productResponse.getProducts());
		Optional<ProductObj> product = productResponse.getProducts().stream()
				.filter(Objects::nonNull).filter(p -> "BOLTON-PEACOCK_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
		assertNotNull(product);
		assertEquals("EmbeddedSVOD",product.get().getVariants().get(0).getAttributes().getSubCategory());
	}
	
}