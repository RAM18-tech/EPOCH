package com.dtv.dcp.epoch.integration.unifiedaccount;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.RestApiClient;
import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.model.unifiedaccount.UnifiedProductDetailsRequest;
import com.dtv.dcp.epoch.model.unifiedaccount.UnifiedProductDetailsResponse;
import com.dtv.dcp.epoch.util.JsonService;

/**
 * 
 * @author ks5810
 */

@Service
public class UnifiedAccountClient implements RestApiClient {

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(UnifiedAccountClient.class);

	private static final String UNIFIEDACCOUNT = "unifiedAccount";

	/** The rest template. */
	private RestTemplate restTemplate;

	/** The UnifiedAccount service info base URL. */
	@Value("${apiclient.rest.unifiedaccount.productDetailsInfo.baseUrl}")
	private String unifiedAccountProductDetailsInfoBaseURL;

	/** The readTimeout. */
	@Value("${apiclient.rest.unifiedAccount.readTimeout:10000}")
	private int readTimeout;
	
	/** The is local. */
	@Value("${genericLocalConfigEnabled:false}")
	private boolean isLocal = false;
	
	/** The params. */
	private Map<String, Object> params = new HashMap<>();

	/**
	 * Instantiates a new UnifiedAccount client.
	 *
	 * @param restTemplateFactory
	 *            the rest template factory
	 * @throws Exception
	 *             the exception
	 */
	@Autowired
	public UnifiedAccountClient(RestTemplateBeanFactory restTemplateFactory) throws Exception {
		this.restTemplate = restTemplateFactory.getObject(UNIFIEDACCOUNT);
	}

	/**
	 * This method will return the UnifiedProductDetailsInfo Response by calling the UnifiedAccount
	 * Micro Service.
	 * 
	 * @param ban
	 * @param sessionId
	 * @return UnifiedServiceResponse
	 */
	@Retryable(maxAttemptsExpression = "${apiclient.unified.rest.default.maxAttempts}", value = {
			RestClientException.class })
	public UnifiedProductDetailsResponse getProductDetailsInfoByBAN(UnifiedProductDetailsRequest unifiedProductDetailsRequest) throws ClientException {
		log.debug("Start of UnifiedAccountClient.getProductDetailsInfoByBAN() method.. ");
		ResponseEntity<String> response = null;
		UnifiedProductDetailsResponse unifiedServiceResponse = null;
		try {
			if (isLocal) {
				initializeSSLLocal();
			}
		} catch (KeyManagementException|NoSuchAlgorithmException| KeyStoreException ex) {
			log.error("UnifiedAccountClient.getProductDetailsInfoByBAN() API ::", ex);
		}
		response = invokeUnifiedAccountMsForProductDetailsInfo(unifiedProductDetailsRequest);
		unifiedServiceResponse = JsonService.getObjectFromJson(response.getBody(), UnifiedProductDetailsResponse.class);
		log.debug("End of UnifiedAccountClient.getProductDetailsInfoByBAN() method.. ");
		return unifiedServiceResponse;
	}
	/**
	 * Invoke UnifiedAccount ms for product details info.
	 * 
	 * @param ban
	 * @param sessionId
	 * @return ResponseEntity
	 */
	public ResponseEntity<String> invokeUnifiedAccountMsForProductDetailsInfo(UnifiedProductDetailsRequest unifiedProductDetailsRequest) {
		log.info("Start of UnifiedServiceClient.invokeUnifiedAccountMsForProductDetailsInfo() method.. ");
		ResponseEntity<String> response = null;
		HttpEntity<UnifiedProductDetailsRequest> request = buildUnifiedProductDetailsRequest(unifiedProductDetailsRequest);
		log.info("UnifiedAccountMsForProductDetailsInfoBaseURL:[{}] ", unifiedAccountProductDetailsInfoBaseURL);
		long startTimeMillis = System.currentTimeMillis();		
		response = restTemplate.exchange(unifiedAccountProductDetailsInfoBaseURL, HttpMethod.POST, request, String.class);	
		log.info("TOTAL_TIME_TAKEN_FROM_UNIFIED_ACCOUNT_MS-[{}]", System.currentTimeMillis() - startTimeMillis);
		log.info("End of UnifiedServiceClient.invokeUnifiedAccountMsForProductDetailsInfo() method.. ");
		return response;
	}
	/**
	 * Build Unified product details info request 
	 * 
	 * @param unifiedProductDetailsRequest
	 * @return
	 */
	private HttpEntity<UnifiedProductDetailsRequest> buildUnifiedProductDetailsRequest(
			UnifiedProductDetailsRequest unifiedProductDetailsRequest) {
		return new HttpEntity<>(unifiedProductDetailsRequest);
	}
	/**
	 * Initialize SSL local.
	 *
	 * @throws Exception the exception
	 */
	private void initializeSSLLocal() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		this.restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));

	}
	
}
