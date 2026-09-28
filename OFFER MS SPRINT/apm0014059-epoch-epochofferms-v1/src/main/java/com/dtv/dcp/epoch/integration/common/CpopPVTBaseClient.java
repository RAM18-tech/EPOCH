package com.dtv.dcp.epoch.integration.common;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.commercetools.graphql.api.types.Product;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTCouponsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;


/**
 * 
 * @author sn611j
 *
 */

public abstract class CpopPVTBaseClient extends BaseRestClient {

	@Autowired
	@Qualifier("CpopAuthPVTRequestHeaderInterceptor")
	ClientHttpRequestInterceptor CpopAuthPVTRequestHeaderInterceptor;

	@Override
	@PostConstruct
	protected void init() throws Exception {
		super.init();
		List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();

		interceptors.add(CpopAuthPVTRequestHeaderInterceptor);

		List<ClientHttpRequestInterceptor> customInterceptors = getCustomRestTemplate().getInterceptors();
		customInterceptors.add(CpopAuthPVTRequestHeaderInterceptor);
	}

	public abstract CTOfferResponse getOffers(CTOfferRequest ctOffersRequest);

	public abstract CTProductResponse getProducts(CTProductRequest ctProductRequest);

	public abstract CTProductResponse getProductsByType(String productType);

	public abstract CTBenefitsResponse getBenefits(CTBenefitsRequest ctBenefitsRequest);

	public abstract CTOfferResponse getOffers(String ctOffersRequest,String idpEnv);

	public abstract CTCouponResponse getCoupons(CTCouponsRequest cTCouponsRequest);

	public abstract CTProductResponse getProducts(String ctProductsRequest,String idpEnv);

	public abstract CTProductResponse getProductsByType(String productType, String idpConfigEnv);

	public abstract CTBenefitsResponse getBenefits(String ctBenefitsRequest,String idpEnv);

	public abstract CTOfferResponse executeGraphQLQuery(String ctOffersRequest, String idpEnv);

	public abstract Map<String, CTOffer> getOfferDataFromCache(CTOfferRequest ctOfferRequest, String idpConfigEnv, String cacheKey);

	public abstract List<Product> executeGraphQLQueryProducts(String ctOfferRequest, String idpConfigEnv);

}
