package com.dtv.dcp.epoch.processor.watchtv;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

public class WatchTVProductsProcessorTest {

	@InjectMocks
	private WatchTVProductsProcessor watchTVProductsProcessor;

	@Mock
	CpopClientHelper cpopClient;

	@Mock
	FeatureManagerHelper featureManagerHelper;

	@Mock
	OffersUtils offersUtils;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		when(featureManagerHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(false);
	}

	@Test
	public void testGetVideoAddonOffers() throws Exception {

		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ProductRequest productRequest = new ProductRequest();
		productRequest.setProductCodes(Stream.of("WATCHTV").collect(Collectors.toList()));
		CTProductRequest ctProductRequest = new CTProductRequest();
		ctProductRequest.setProductCodes(Stream.of("WATCHTV").collect(Collectors.toList()));
		CTProductResponse ctProductResponse = new CTProductResponse();
		productRequestWrapper.setProductRequest(productRequest);
		productRequestWrapper.setCtProductRequest(ctProductRequest);
		doReturn(ctProductResponse).when(cpopClient).getProducts(Mockito.any());
		// doReturn(ctOfferResponse).when(salesVideoPlanProcessor).getVideoPlanOffers(offerRequestWrapper);

		CTProductResponse productResponse = watchTVProductsProcessor.getProducts(productRequestWrapper);
		assertNotNull(productResponse);
	}

}
