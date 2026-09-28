/**
 * 
 */
package com.dtv.dcp.epoch.integration.common;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;

/**
 * @author nf2008
 * This is single instance which will point to P10 only
 */
@Component
public abstract class EpochUCCPrimaryBaseClient extends BaseRestClient {
	@Autowired
	@Qualifier("EpochUCCPrimaryAuthRequestHeaderInterceptor")
	ClientHttpRequestInterceptor epochUCCPrimaryAuthRequestHeaderInterceptor;

	@Override
	@PostConstruct
	protected void init() throws Exception {
		super.init();
		List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();		
		List<ClientHttpRequestInterceptor> interceptorsCustom = getCustomRestTemplate().getInterceptors();

		interceptors.add(epochUCCPrimaryAuthRequestHeaderInterceptor);
		interceptorsCustom.add(epochUCCPrimaryAuthRequestHeaderInterceptor);
	}

	public abstract CTOfferResponse getOffers(CTOfferRequest ctOffersRequest);

	public abstract CTBenefitsResponse getBenefits(CTBenefitsRequest ctBenefitsRequest);

}
