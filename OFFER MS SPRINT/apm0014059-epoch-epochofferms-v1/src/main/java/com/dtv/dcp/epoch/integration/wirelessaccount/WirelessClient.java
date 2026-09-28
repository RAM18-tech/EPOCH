package com.dtv.dcp.epoch.integration.wirelessaccount;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.httpclient.RestApiClient;
import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.exception.ServiceError;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.wireless.WirelessResponse;
import com.dtv.dcp.epoch.util.JsonService;
import com.jayway.jsonpath.JsonPath;

/*import com.att.idp.http.client.config.RestTemplateBeanFactory;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import java.security.cert.X509Certificate;
*/
/**
 * This Class retrieves wireless API response and convert json to
 * WirelessResponse. Offer Response
 * 
 * @author nk3077
 */

@Service
public class WirelessClient implements RestApiClient {

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(WirelessClient.class);

	/** The Constant WIRELESS. */
	private static final String WIRELESS = "wireless";

	/** The rest template. */
	private RestTemplate restTemplate;

	/** The wireless base URL. */
	@Value("${apiclient.rest.wireless.baseUrl}")
	private String wirelessBaseURL;

	/**
	 * Instantiates a new wireless client.
	 *
	 * @param restTemplateFactory
	 *            the rest template factory
	 * @throws Exception
	 *             the exception
	 */
	@Autowired
	public WirelessClient(RestTemplateBeanFactory restTemplateFactory) throws Exception {
		this.restTemplate = restTemplateFactory.getObject(WIRELESS);
		/*TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
		SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setHttpClient(httpClient);
		this.restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
		*/
	}

	

	/**
	 * This method will return the Wireless Response by calling the Wireless Micro
	 * Service.
	 *
	 * @param accountNumber
	 *            the account number
	 * 
	 * @return WirelessResponse
	 * @throws ClientException
	 */
	@Cacheable(value = Constants.CPOPOFFERSMS_PROFILE_NEAR_CACHE_MAP_NAME, key = "{ 'cpopWirelessGetByAccountNumber', #accountNumber }" , unless="#result==null", cacheManager="redisCacheManagerFirstHolder")
	@Retryable(maxAttemptsExpression = "${apiclient.rest.default.maxAttempts}", value = {
			HttpServerErrorException.class })
	public WirelessResponse getByAccountNumber(String accountNumber) throws ClientException {
		//log.info("Start of WirelessClient.getByAccountNumber() method..{}", ESAPI.encoder().encodeForHTML(accountNumber));
		if (!Optional.ofNullable(accountNumber).isPresent() || accountNumber.isEmpty()) {
			throw new ServiceException(ErrorMessages.MISSING_DATA_FROM_REQUEST_HEADER)
					.addDetail(ErrorMessages.MISSING_DATA_FROM_REQUEST_HEADER_DETAILS002);
		}
		Map<String, Object> params = new HashMap<>();
		params.put("account", accountNumber);
		log.info("cpopWirelessGetByAccountNumber wirelessBaseURL:[{}] ", wirelessBaseURL + "/details/" + accountNumber);
		long startTimeMillis = System.currentTimeMillis();			
		ResponseEntity<String> response = restTemplate.exchange(wirelessBaseURL + "/details/{account}", HttpMethod.GET,
				null, String.class, params);
		log.info("TOTAL_TIME_TAKEN_FROM_cpopWirelessGetByAccountNumber-[{}]", System.currentTimeMillis() - startTimeMillis);
		WirelessResponse wirelessResponse = JsonService.getObjectFromJson(response.getBody(), WirelessResponse.class);
		log.info("End of WirelessClient.getByAccountNumber() method..");
		return wirelessResponse;
	}

	/**
	 * Recover getByAccountNumber.
	 *
	 * @param re
	 *            the re
	 * @return the WirelessResponse object
	 */
	@Recover
	public WirelessResponse recoverGetByAccountNumber(RestClientException re) {
		if (re instanceof HttpServerErrorException) {
			throw new ServiceException(com.dtv.dcp.epoch.message.ErrorMessages.CTLG_WIRELESS_ERROR_UNKNOWN, re).addDetail(
					ErrorMessages.CTLG_WIRELESS_UNHANDLED_EXCEPTION_DETAILS, "Account by account no",
					"WirelessClient.getByAccountNumber");
		}
		if (re instanceof HttpStatusCodeException) {
			HttpStatusCodeException hse = (HttpStatusCodeException) re;
			String errors = hse.getResponseBodyAsString();
			ServiceError serviceError = JsonPath.parse(errors).read("$.error", ServiceError.class);
			ServiceException se = new ServiceException(ErrorMessages.ERROR_VALIDATION_FAILED);
			se.addDetails(serviceError.getDetails());
			throw se;
		}
		throw new ServiceException(ErrorMessages.CTLG_WIRELESS_ERROR_UNKNOWN, re).addDetail(
				ErrorMessages.CTLG_WIRELESS_UNHANDLED_EXCEPTION_DETAILS, "Account by account no",
				"WirelessClient.getByAccountNumber");
	}

	/**
	 * Recover RPP.
	 *
	 * @param re
	 *            the re
	 * @return the rate plan price response
	 */
	
}
