package com.dtv.dcp.epoch.integration.common;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;

/**
 * 
 * @author sx4928
 *
 */
public abstract class EpochDataloadBaseClient extends BaseRestClient {

	@Autowired
	@Qualifier("EpochAuthDataloadRequestHeaderInterceptor")
	ClientHttpRequestInterceptor epochAuthDataloadRequestHeaderInterceptor;

	@Override
	@PostConstruct
	protected void init() throws Exception {
		super.init();
		List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();
		List<ClientHttpRequestInterceptor> interceptorsCustom = getCustomRestTemplate().getInterceptors();

		interceptors.add(epochAuthDataloadRequestHeaderInterceptor);
		interceptorsCustom.add(epochAuthDataloadRequestHeaderInterceptor);
	}

	public abstract CTBenefitsResponse getSatelliteBenefits(CTBenefitsRequest ctBenefitsRequest);

}
