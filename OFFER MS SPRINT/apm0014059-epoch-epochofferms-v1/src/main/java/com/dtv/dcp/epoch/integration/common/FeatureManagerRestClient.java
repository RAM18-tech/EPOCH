package com.dtv.dcp.epoch.integration.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class FeatureManagerRestClient extends BaseRestClient {

	@Value("${ixp.svc.url}")
	private String ixpUrl;
	
	/**
	 * The rest noOfConnections.
	 */
	@Value("${apiclient.rest.ixp.noOfConnections:20}")
	private int noOfConnections;
	
	/**
	 * The  proxyEnabled.
	 */
	@Value("${apiclient.rest.ixp.proxyEnabled:false}")
	private boolean proxyEnabled;

	/**
	 * The  sslEnabled.
	 */
	@Value("${apiclient.rest.ixp.sslEnabled:false}")
	private boolean sslEnabled;

	/**
	 * The connectTimeout.
	 */
	@Value("${apiclient.rest.ixp.connectTimeout:4000}")
	private int connectTimeout;

	/**
	 * The rest readTimeout.
	 */
	@Value("${apiclient.rest.ixp.readTimeout:4000}")
	private int readTimeout;

	public static final String EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME = "epochOffersMsIXPCache1";

	private static final Logger log = LoggerFactory.getLogger(FeatureManagerRestClient.class);

	@Cacheable(value = EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME, key = "{ 'EpochOffersMsIXP',#idpConfigEnv}" , unless = "#result == null", cacheManager = "redisCacheManagerSecondHolder")
	public String invokeIxpEndpoint(String idpConfigEnv) throws RestClientException {
		log.debug(" inside invokeixpEndpoint() idpConfigEnv: {} ", idpConfigEnv);
		
		return makeGetCall(String.class, ixpUrl, null);
		
	}

	@CacheEvict(value = EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME)
	public void removeIxpFlagsCache(String cacheKey) {
		log.info(" cache evict for  invokeIxpEndpoint()");
	}
	
	@Override
	protected String getApiKey() {
		return "ixp";
	}

	/**
	 * The the abstract method  getting value of isProxyEnabled.
	 */
	@Override
	protected boolean isProxyEnabled() {
		return proxyEnabled;

	}

	/**
	 * The the abstract method  getting value of isSSLEnabled.
	 */
	@Override
	protected boolean isSSLEnabled() {
		return sslEnabled;

	}

	/**
	 * The the abstract method  getting value of connectTimeout.
	 */
	protected int connectTimeout() {
		return connectTimeout;
	}

	/**
	 * The the abstract method  getting value of readTimeout.
	 */
	protected int readTimeout() {
		return readTimeout;
	}

	/**
	 * The the abstract method getting value of connections.
	 */
	protected int noOfConnections() {
		return noOfConnections;
	}
}
