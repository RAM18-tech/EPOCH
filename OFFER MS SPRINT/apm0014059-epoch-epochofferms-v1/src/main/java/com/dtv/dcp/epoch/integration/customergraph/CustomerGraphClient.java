package com.dtv.dcp.epoch.integration.customergraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.common.CustomerGraphBaseClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.fasterxml.jackson.databind.JsonNode;

@Component("CustomerGraphClient")
public class CustomerGraphClient extends CustomerGraphBaseClient {
    /** The log. */
    private static final Logger log = LoggerFactory.getLogger(CustomerGraphClient.class);

    /** The rest baseUrl. */
    @Value("${apiclient.rest.customergraphdtvnow.baseUrl}")
    private String baseUrl;
    
    /** The rest baseUrl. */
    @Value("${apiclient.rest.customergraphdtvnow.couponsBaseUrl}")
    private String couponsBaseUrl;
    
    /** The  proxyEnabled. */
    @Value("${apiclient.rest.customergraph.proxyEnabled}")
    private String proxyEnabled;

    /** The  sslEnabled. */
    @Value("${apiclient.rest.customergraph.sslEnabled:false}")
    private boolean sslEnabled;

    /** The connectTimeout. */
    @Value("${apiclient.rest.customergraph.connTimeout:4000}")
    private int connectTimeout;

    /** The rest readTimeout. */
    @Value("${apiclient.rest.customergraph.readTimeout:4000}")
    private int readTimeout;

    /** The Constant epoch. */
    private static final String CPOP_CUSTOMERGRAPH_DTVNOW = "customergraphdtvnow";
    /** The pageLimit. */
    @Value("${pageLimit}")
    private int pageLimit;
    /** The rest noOfConnections. */
    @Value("${apiclient.rest.epoch.noOfConnections}")
    private int noOfConnections;
    @Value("${apiclient.rest.epoch.burnUrl}")
    private String burnEndpoint;

    /** The the abstract method  getting value of APIKEY. */
    @Override
    protected String getApiKey() {
        return CPOP_CUSTOMERGRAPH_DTVNOW;
    }

    /** The the abstract method  getting value of isProxyEnabled. */
    @Override
    protected boolean isProxyEnabled() {
        return "enabled".equalsIgnoreCase(proxyEnabled);

    }

    /** The the abstract method  getting value of isSSLEnabled. */
    @Override
    protected boolean isSSLEnabled() {
        return sslEnabled;

    }

    /** The the abstract method  getting value of connectTimeout. */
    protected  int  connectTimeout() {
        return connectTimeout;
    }

    /** The the abstract method  getting value of readTimeout. */
    protected  int  readTimeout() {
        return readTimeout;
    }
    /** The the abstract method getting value of connections. */
    protected int noOfConnections() {
        return noOfConnections;
    }

    @Override
	public JsonNode getAccountById(String accountId) {
		log.info("Start of CustomerGraphClient.getAccountById method..");
		final String apiPath = "offers";
		// The line below only to test from local
		// JsonNode jsonNode = makeGetCallTrustEveryone(JsonNode.class, baseUrl,
		// "customers/accounts/dtvnow/".concat(accountId));
		JsonNode jsonNode = null;
		try {
			log.info("baseUrl: [{}]", baseUrl);
			long startTimeMillis = System.currentTimeMillis();
			jsonNode = makeGetCall(JsonNode.class, baseUrl, "customers/accounts/dtvnow/".concat(accountId));
			log.info("TOTAL_TIME_TAKEN_FOR_GETACCOUNTBYID_CALL-[{}]", System.currentTimeMillis() - startTimeMillis);
		} catch (Exception excep) {
			log.info("EPOCH_GETACCOUNTBYID_CALL_EXCEPTION" + excep);
		}
		log.info("End of CustomerGraphClient.getAccountById method..");
		log.info("EPOCH_GETACCOUNTBYID_CALL_SUCCESS");
		return jsonNode;
	}
    
    @Override
	public <T> T getUverseAccountProducts(String customerId, String accountId, String accountType,
			Boolean includeAssignedProductDetails, Class<T> responseClass) throws ServiceException, RestClientException {
		log.info("getUverseAccountProducts() start");
    	String contextPath = String.format("customer/%s/accountId/%s/products?accountType=%s", customerId, accountId,accountType);
		if (includeAssignedProductDetails != null) {
			contextPath = contextPath
					.concat(String.format("&includeAssignedProductDetails=%s", includeAssignedProductDetails));
		}
		try {
			log.info("getUverseAccountProducts() baseUrl: {}", baseUrl);
			log.info("getUverseAccountProducts() end");
			return makeGetCall(responseClass, baseUrl, contextPath);
		} catch ( Exception e) {
			ServiceException ex = new ServiceException(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND,"getUverseAccountProducts: " + e.getMessage());
			ex.addDetail(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "EPOCH", accountId);
			log.error("Exception occurred: {}", e);
			throw ex;
		}	
	}

	@Override
	public <T> T getUverseCustomerAccounts(String customerId, String accountId, String accountType,
			Class<T> responseClass) throws ServiceException, RestClientException {
		log.info("getUverseCustomerAccounts() start");
		String contextPath = String.format("customer/%s/accountId/%s?accountType=%s", customerId, accountId,accountType);
		try {
			log.info("getUverseCustomerAccounts() baseUrl: {}", baseUrl);
			log.info("getUverseCustomerAccounts() end");
			return makeGetCall(responseClass, baseUrl, contextPath,"true");
		} catch (Exception e) {
			ServiceException ex = new ServiceException(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND,"getUverseCustomerAccounts: " + e.getMessage());
			ex.addDetail(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "EPOCH", accountId);
			log.error("Exception occurred: {}", e);
			throw ex;
		}
	}
	
	@Override
	public <T> T getUverseCustomerCoupons(String customerId, String accountId, String accountType,
			Class<T> responseClass) throws ServiceException, RestClientException {
		log.info("getUverseCustomerCoupons() start");
		String contextPath = String.format("customer/%s/accountId/%s/coupons?accountType=%s", customerId, accountId,accountType);
		try {
			log.info("getUverseCustomerCoupons() couponsBaseUrl: {}", couponsBaseUrl);
			log.info("getUverseCustomerCoupons() end");
			return makeGetCallCoupon(responseClass, couponsBaseUrl, contextPath);
		} catch (Exception e) {
			ServiceException ex = new ServiceException(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND,"getUverseCustomerAccounts: " + e.getMessage());
			ex.addDetail(ErrorMessages.ACCOUNT_NUMBER_NOT_FOUND, "EPOCH", accountId);
			log.error("Exception occurred: {}", e);
			throw ex;
		}
	}

}
