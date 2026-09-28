package com.dtv.dcp.epoch.service.satellite;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteProductsProcessor;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.TestUtility;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class SatelliteProductsServiceImplTest {
	
	@Mock
	SatelliteProductsProcessor productProcessor;
	
	@InjectMocks
	SatelliteProductServiceImpl prodService;
	
	@Test
	public void testGetProductDetails() throws JsonGenerationException, JsonMappingException, IOException {
		HttpHeaders headers = Mockito.mock(HttpHeaders.class);
		
		ObjectMapper mapper = new ObjectMapper();
		
		String prodReq = readFileAsString("src/test/resources/products/satellite/satellite_product_request.json");
		
		CTProductResponse ctProductResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/ProductResponse.json"),CTProductResponse.class);
		
		ProductRequest productRequest = mapper.readValue(prodReq, ProductRequest.class);
		
		when(headers.getFirst(Constants.IDPCTX_LOGGEDINID)).thenReturn("test_idpctx-loggedInId");
		when(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)).thenReturn("test_idpctx-linkedWirelessAccNums");
		when(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)).thenReturn("test_idpctx-linkedDirectvNowAccNums");
		when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("test_idpctx-session-id");
		when(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)).thenReturn("test_Idpctx-linkeduverseaccnums");
		
		when(productProcessor.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		CTProductResponse response = prodService.getProducts(headers, productRequest);
		assertNotNull(response.getProducts());
	}
	
	@Test
	public void testGetIncludedProductDetails() throws JsonGenerationException, JsonMappingException, IOException {
		HttpHeaders headers = Mockito.mock(HttpHeaders.class);
		
		ObjectMapper mapper = new ObjectMapper();
		
		String prodReq = readFileAsString("src/test/resources/products/satellite/satellite_product_request2.json");
		
		CTProductResponse ctProductResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/ProductResponseWithObjects.json"),CTProductResponse.class);
		
		ProductRequest productRequest = mapper.readValue(prodReq, ProductRequest.class);
		
		when(headers.getFirst(Constants.IDPCTX_LOGGEDINID)).thenReturn("test_idpctx-loggedInId");
		when(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)).thenReturn("test_idpctx-linkedWirelessAccNums");
		when(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)).thenReturn("test_idpctx-linkedDirectvNowAccNums");
		when(headers.getFirst(Constants.IDPCTX_SESSION_ID)).thenReturn("test_idpctx-session-id");
		when(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)).thenReturn("test_Idpctx-linkeduverseaccnums");
		
		when(productProcessor.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		CTProductResponse response = prodService.getProducts(headers, productRequest);
		assertNotNull(response.getProducts().get(0).getVariants().get(0).getAttributes().getIncludedProducts().get(0).getProducts().get(0).getObj());
	}


	private String readFileAsString(String path) throws IOException {
		byte[] inputReqByte = Files.readAllBytes(Paths.get(path));
		
		return new String(inputReqByte, StandardCharsets.UTF_8);
	}
	
}
