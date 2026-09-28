package com.dtv.dcp.epoch.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.model.customergraph.response.CGAccountProducts;
import com.dtv.dcp.epoch.model.customergraph.response.UVAccountProductsResponse;


@ExtendWith(MockitoExtension.class)
public class CustomerGraphProductsHelperTest {


    @InjectMocks
    CustomerGraphProductsHelper customerGraphProductsHelper;
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
	public void testCreateCGAccountProducts() throws Exception {
		UVAccountProductsResponse accountProductsResponse = JsonService.getObjectFromJson(TestUtility.loadJson(
				"/Uverse_Response_251461578.json"), UVAccountProductsResponse.class);
		
		CGAccountProducts cgAccountProducts = customerGraphProductsHelper.createUVerseAccountProducts(accountProductsResponse);

		assertNotNull(cgAccountProducts);
		assertEquals(14, cgAccountProducts.getProducts().size());
	}
	
	
}