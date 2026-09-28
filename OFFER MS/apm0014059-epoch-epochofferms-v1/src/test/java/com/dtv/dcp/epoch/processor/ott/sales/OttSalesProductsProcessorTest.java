package com.dtv.dcp.epoch.processor.ott.sales;
//package com.dtv.dcp.epoch.processor.ott.sales;/*package com.dtv.dcp.epoch.processor.ott.sales;*/
//
//
//import static org.junit.Assert.assertNotNull;
//import static org.mockito.Mockito.doReturn;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
//import com.dtv.dcp.epoch.model.common.request.ProductRequest;
//import com.dtv.dcp.epoch.model.ct.product.Product;
//import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
//import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//
//import com.dtv.dcp.epoch.common.Constants;
//import com.dtv.dcp.epoch.integration.CpopClient;
//import com.dtv.dcp.epoch.model.common.request.OfferRequest;
//import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
//import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
//import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
//import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
//import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
//
//public class OttSalesProductsProcessorTest {
//
//// *//** The OttSalesOffersProcessor . *//*
//
//	@Mock
//	private OttSalesProductsProcessor OttSalesProductsProcessor;
//
//	@Mock
//	CpopClient cpopClient;
//
//
//
//	@Before
//	public void setup() {
//		MockitoAnnotations.openMocks(this);
//
//	}
//
//	@SuppressWarnings("deprecation")
//	@Test
//	public void testGetProducts(){
//
//		// Temp  need to update late once logic updated
//
//		CTProductRequest productRequest = new CTProductRequest();
//		CTProductRequest ctProductRequest = new CTProductRequest();
//		ctProductRequest.setOfferActionType(productRequest.getOfferActionType());
////		ctProductRequest.setOfferProductSubType(offerRequest.getOfferProductTypes());
////		ctProductRequest.setState("staged");
//		CTProductResponse ctProductResponse = new CTProductResponse();
//		List<Product> responseList= new ArrayList<>();
//		Product ctProduct = new Product();
//		OfferAttributes attributes=new OfferAttributes();
//		attributes.setOfferProductSubtype(Constants.BASE);
//		// ctProduct.set(attributes);
//		// responseList.add(ctOffer);
//		// ctProductResponse.setOffers(responseList);
//
//
//		doReturn(ctProductResponse).when(cpopClient).getProducts(Mockito.any());
//
//		ctProductResponse = cpopClient.getProducts(productRequest);
//		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
//		CTProductResponse offersResponse = OttSalesProductsProcessor.getProducts(productRequestWrapper);
//
//		// assertNotNull(offersResponse);
//	}
//
//}
