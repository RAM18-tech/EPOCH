package com.dtv.dcp.epoch.integration.common;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.dtv.dcp.epoch.model.ct.request.CTCouponsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;


/**
 * Created by nk3077 on 03/21/2019.
 * This class add the CpopUCCAuthRequestHeaderInterceptor in the request for rest client
 */

public abstract class CpopUCCBaseClient extends BaseRestClient {

	@Autowired
	@Qualifier("CpopAuthUCCRequestHeaderInterceptor")
	ClientHttpRequestInterceptor cpopAuthUCCRequestHeaderInterceptor;

	@Override
	@PostConstruct
	protected void init() throws Exception {
		super.init();
		List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();
		interceptors.add(cpopAuthUCCRequestHeaderInterceptor);
		
		List<ClientHttpRequestInterceptor> customInterceptors = getCustomRestTemplate().getInterceptors();
		customInterceptors.add(cpopAuthUCCRequestHeaderInterceptor);
	}

	public abstract CTOfferResponse getOffers(CTOfferRequest ctOffersRequest);

	public abstract CTCouponResponse getCoupons(CTCouponsRequest cTCouponsRequest);


}
