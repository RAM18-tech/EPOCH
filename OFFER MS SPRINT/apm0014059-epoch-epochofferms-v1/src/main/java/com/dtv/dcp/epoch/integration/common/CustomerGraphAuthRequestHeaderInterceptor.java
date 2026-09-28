package com.dtv.dcp.epoch.integration.common;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;


/**
 * Created by ac2201
 *
 */

@Component("CustomerGraphAuthRequestHeaderInterceptor")
public class CustomerGraphAuthRequestHeaderInterceptor implements ClientHttpRequestInterceptor {
	
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(CustomerGraphAuthRequestHeaderInterceptor.class);

	@Value("${apiclient.rest.customergraphdtvnow.username}")
	private String userName;
	@Value("${apiclient.rest.customergraphdtvnow.password}")
	private String password;


	/**
	 * this method add the token in the request .
	 *
	 * @param request
	 * @param body
	 * @param execution
	 * @return  ClientHttpResponse
	 */

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		log.debug("Start of CustomerGraphAuthRequestHeaderInterceptor.intercept method..");
		String notEncoded = userName + ":" + password;
		String encodedAuth = "Basic " + new String(Base64.encodeBase64(notEncoded.getBytes()));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", encodedAuth);
		ClientHttpResponse response = null;
		response = execution.execute(request, body);
		if (HttpStatus.UNAUTHORIZED.equals(response.getStatusCode())
				|| HttpStatus.FORBIDDEN.equals(response.getStatusCode())) {			
			log.error("EPOCH_AUTH_TOKEN_CALL_FAILED : EPOCH_BASIC_CALL_FAILURE=\"EPOCH API BASIC AUTH Failure Summary\"");
			log.debug("EPOCH_AUTH_TOKEN_CALL_FAILED :End of CustomerGraphAuthRequestHeaderInterceptor.intercept method..");
			return response;
		}
		return response;
	}
}
