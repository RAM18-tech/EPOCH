package com.dtv.dcp.epoch.integration.common;


import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class CustomerGraphBaseClient extends BaseRestClient {

	@Autowired
	@Qualifier("CustomerGraphAuthRequestHeaderInterceptor")
	ClientHttpRequestInterceptor customerGraphAuthRequestHeaderInterceptor;

	@Override
	@PostConstruct
	protected void init() throws Exception {
		super.init();
		List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();

		interceptors.add(customerGraphAuthRequestHeaderInterceptor);
	}

	public abstract JsonNode getAccountById(String accountId);

	public abstract <T> T getUverseAccountProducts(String customerId, String accountId, String accountType,
			Boolean includeAssignedProductDetails, Class<T> responseClass);

	public abstract <T> T getUverseCustomerAccounts(String customerId, String accountId, String accountType,
			Class<T> responseClass);
	
	public abstract <T> T getUverseCustomerCoupons(String customerId, String accountId, String accountType,
			Class<T> responseClass);


}
