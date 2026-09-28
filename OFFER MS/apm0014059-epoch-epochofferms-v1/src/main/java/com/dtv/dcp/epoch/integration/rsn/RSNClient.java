package com.dtv.dcp.epoch.integration.rsn;

import java.net.SocketTimeoutException;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.RestApiClient;
import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.util.JsonService;
import javax.annotation.PostConstruct;

/**
 * The Class RSNClient
 * 
 * @author ks5810
 */

@Service
public class RSNClient implements RestApiClient {

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(RSNClient.class);

	private static final String RSN = "rsn";

	/** The rest template. */
	private RestTemplate restTemplate;

	/** The RSN info base URL. */
	@Value("${apiclient.rest.rsn.baseUrl}")
	private String rsnInfoBaseURL;

	/** The readTimeout. */
	@Value("${apiclient.rest.rsn.readTimeout:25000}")
	private int readTimeout;

	@Value("${apiclient.rest.epoch.noOfConnections}")
	private int noOfConnections;

	@Value("${apiclient.rest.epoch.connectTimeout:4000}")
	private int connectTimeout;

	@Value("${apiclient.rest.epoch.sslEnabled:false}")
	private boolean sslEnabled;

	/** The proxy auth host. */
	@Value("${idp.com.att.proxyAuthHost}")
	private String proxyHost;

	/** The proxy port. */
	@Value("${idp.com.att.proxyAuthPort}")
	private int proxyPort;

	/** The Default max Connections For Localhost . */
	@Value("${idp.com.att.defaultMaxPerRoute}")
	private int defaultMaxPerRoute;
	
	/** The proxy enabled. */
	@Value("${dcp.rsn.proxyEnabled:false}")
	private String isProxyRequired;

	/** The rest template. */
	@Autowired
	private RestTemplateBeanFactory restTemplateFactory;

	/**
	 * Instantiates a new RSN client.
	 *
	 * @param restTemplateFactory the rest template factory
	 * @throws Exception the exception
	 */
	@PostConstruct
	protected void init() throws Exception {
		// this.restTemplate = restTemplateFactory.getObject(RSN);
		this.restTemplate = getRestTemplate("enabled".equalsIgnoreCase(isProxyRequired), RSN, sslEnabled, connectTimeout, readTimeout, noOfConnections);
	}

	public RestTemplate getRestTemplate(boolean requireProxy, String apiName, boolean isSSLEnabled, int connectTimeout,
			int readTimeout, int noOfConnections) throws Exception {
		log.info("Rest Client =" + apiName + ":: requireProxy= " + requireProxy + ":: isSSLEnabled= " + isSSLEnabled
				+ ":: connectTimeout= " + connectTimeout + ":: readTimeout=" + readTimeout + ":: noOfConnections="
				+ noOfConnections + ":: defaultMaxPerRoute= " + defaultMaxPerRoute);

		if (restTemplate == null) {
			restTemplate = restTemplateFactory.getObject(apiName);
			HttpClientBuilder httpClientBuilder = HttpClients.custom();
			if (noOfConnections != 0) {
				PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
				connManager.setMaxTotal(noOfConnections);
				connManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
				// HttpHost localhost = new HttpHost("locahost", maxConnectionsForLocalhost);
				// connManager.setMaxPerRoute(new HttpRoute(localhost), maxPerRoute);
				httpClientBuilder.setConnectionManager(connManager);
			}
			if (requireProxy) {
				httpClientBuilder.setProxy(new HttpHost(proxyHost, proxyPort));
			}

			if (!isSSLEnabled) {
				TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
				SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
						.loadTrustMaterial(null, acceptingTrustStrategy).build();
				SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
				httpClientBuilder.setSSLSocketFactory(csf);
			}
			CloseableHttpClient httpClient = httpClientBuilder.build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setConnectTimeout(connectTimeout);
			requestFactory.setReadTimeout(readTimeout);
			requestFactory.setHttpClient(httpClient);
			restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
		}
		return restTemplate;
	}

	/**
	 * This method will return the RSN Response by calling the RSN Micro Service.
	 *
	 * @param rsnRequest
	 * @return RSNResponse
	 */
	@Retryable(maxAttemptsExpression = "${apiclient.rsn.rest.default.maxAttempts}", value = {
			RestClientException.class })
	public RSNResponse getRSNInfo(RSNRequest rsnRequest) {
		log.debug("Start of RSNClient.getRSNInfo() method.. {}");
		ResponseEntity<String> response = null;
		RSNResponse rsnResponse = null;
		response = invokeRSNMs(rsnRequest);
		if (response != null) {
			rsnResponse = JsonService.getObjectFromJson(response.getBody(), RSNResponse.class);
		}

		log.debug("End of RSNClient.getRSNInfo() method.. {}");
		return rsnResponse;
	}

	@Recover
	public RSNResponse recoverGetRSNInfo(RestClientException re) {
		log.info("start -- RSN client recoverGetRSNInfo ");
		log.error("Got error from RSN Ms.", re);
		if (re != null && re.getRootCause() != null && re.getRootCause() instanceof SocketTimeoutException) {
			log.info("start -- RSN client recoverGetRSNInfo inside condition");
			throw new ServiceException(ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN, re)
					.addDetail(ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN_DETAILS, RSN, Integer.toString(readTimeout));

		}

		else {
			log.info("start -- RSN client recoverGetRSNInfo inside else condition");
			log.error("Got error from RSN Ms..", re);
			throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, re)
					.addDetail(ErrorMessages.BROADBAND_EQUIPMENT_IPUSAGE_OFFER_ERROR, "RSN Fee details");
		}

	}

	@Recover
	public RSNResponse recoverGetRSNInfo(ServiceException re) {
		log.info("start -- RSN client recoverGetRSNInfo ");
		log.error("Got error from RSN Ms..", re);
		throw (new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR)
				.addDetail(ErrorMessages.CTLG_DTVN_EXTERNAL_ERROR_DETAILS003));

	}

	/**
	 * Invoke RSNClient ms for rsn info.
	 * 
	 * @param rsnRequest
	 * @return ResponseEntity the response entity
	 */
	public ResponseEntity<String> invokeRSNMs(RSNRequest rsnRequest) {
		log.info("Start of RSNClient.invokeRSNMs() method.. {}");
		ResponseEntity<String> response = null;
		HttpEntity<RSNRequest> requestEntity = buildRSNRequest(rsnRequest);
		log.info("rsnInfoBaseURL:[{}] ", rsnInfoBaseURL);
		log.info("RSNMS_REQUEST-[{}]", JsonService.getJsonFromObject(requestEntity));
		long startTimeMillis = System.currentTimeMillis();
		response = restTemplate.exchange(rsnInfoBaseURL, HttpMethod.POST, requestEntity, String.class);
		log.info("TOTAL_TIME_TAKEN_FROM_RSNMS-[{}]", System.currentTimeMillis() - startTimeMillis);
		log.info("End of RSNClient.invokeRSNMs() method.. {}");
		return response;
	}

	/**
	 * Builds the rsn request.
	 *
	 * @param rsnRequest the rsnRequest
	 * @return the http entity
	 */
	private HttpEntity<RSNRequest> buildRSNRequest(RSNRequest rsnRequest) {
		log.debug("Start of RSNClient.buildRSNRequest() method..");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<RSNRequest> entity = new HttpEntity<>(rsnRequest, headers);
		log.debug("End of RSNClient.buildRSNRequest() method..");
		return entity;
	}
}
