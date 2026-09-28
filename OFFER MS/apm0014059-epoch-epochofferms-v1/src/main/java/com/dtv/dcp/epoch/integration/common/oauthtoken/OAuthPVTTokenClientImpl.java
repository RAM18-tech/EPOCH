package com.dtv.dcp.epoch.integration.common.oauthtoken;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.common.BaseRestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author sn611j
 *
 */
@Component
public class OAuthPVTTokenClientImpl extends BaseRestClient implements OAuthPVTTokenClient {
	
	/** The log. */

	/** The rest baseUrl. */
	@Value("${apiclient.rest.pvtcttokenservice.baseUrl}")
	private String baseUrl;

	/** The  proxyEnabled. */
	@Value("${apiclient.rest.pvtcttokenservice.proxyEnabled}")
	private String proxyEnabled;

	/** The  sslEnabled. */
	@Value("${apiclient.rest.pvtcttokenservice.sslEnabled:false}")
	private boolean sslEnabled;

	/** The connectTimeout. */
	@Value("${apiclient.rest.pvtcttokenservice.connectTimeout:4000}")
	private int connectTimeout;

	/** The rest readTimeout. */
	@Value("${apiclient.rest.pvtcttokenservice.readTimeout:4000}")
	private int readTimeout;

	/** The Constant CPOPOAUTH. */
	private static final String CTTOKENSERVICE = "pvtcttokenservice";
	

	/** The the abstract method  getting value of APIKEY. */
	@Override
	protected String getApiKey() {
		return CTTOKENSERVICE;
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
	protected int connectTimeout() {
		return connectTimeout;
	}

	 /** The the abstract method  getting value of readTimeout. */
	protected int readTimeout() {
		return readTimeout;
	}

	 /** The the abstract method  getting value of noOfConnections. */
	@Override
	protected int noOfConnections() {
		return 0;
	}
	
	@Override
	public JsonNode getAccessToken(final String request) {
		String response = makePostCall(request, String.class, baseUrl, "");

		return retrieveResponseJsonNode(response);
	}

	/**
	 * init method.
	 *
	 */
	
	@Override
	@PostConstruct
	protected void init() throws Exception {
		super.init();
		List<ClientHttpRequestInterceptor> interceptors = getRestTemplate().getInterceptors();

		ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {

			@Override
			public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
					throws IOException {
				HttpHeaders httpHeaders = request.getHeaders();

				httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

				return execution.execute(request, body);
			}

		};

		interceptors.add(interceptor);
	}

	

	/**
	 * retrieveResponseJsonNode method.
	 * @param response
	 * @return JsonNode object
	 *
	 */
	private JsonNode retrieveResponseJsonNode(String response) {
		JsonNode jnode = null;

		if (response != null) {
			ObjectMapper mapper = new ObjectMapper();

			try {
				jnode = mapper.readTree(response);
			} catch (IOException e) {
				jnode = null;
			}
		}

		return jnode;
	}

}
