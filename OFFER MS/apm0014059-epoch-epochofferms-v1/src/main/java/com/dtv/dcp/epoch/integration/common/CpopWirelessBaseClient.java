package com.dtv.dcp.epoch.integration.common;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;

public abstract class CpopWirelessBaseClient extends BaseRestClient {

	@Autowired
	@Qualifier("CpopWirelessAuthRequestHeaderInterceptor")
	ClientHttpRequestInterceptor cpopWirelessAuthRequestHeaderInterceptor;

	@Override
	@PostConstruct
	protected void init() throws Exception {
		super.init();
		List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();

		interceptors.add(cpopWirelessAuthRequestHeaderInterceptor);
	}

	public abstract CTOfferResponse getOffers(CTOfferRequest ctOffersRequest);

	// public abstract CTOfferResponse getOffers(String ctOffersRequest,String
	// idpEnv);
	public abstract CTProductResponse getProducts(CTProductRequest ctProductRequest);

}
