package com.dtv.dcp.epoch.integration.common;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.common.oauthtoken.OAuthPVTTokenService;


/**
 * 
 * @author sn611j
 *
 */

@Component("CpopAuthPVTRequestHeaderInterceptor")
public class CpopAuthPVTRequestHeaderInterceptor implements ClientHttpRequestInterceptor {
	
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(CpopAuthPVTRequestHeaderInterceptor.class);
	
	/** The OAuthTokenService. */
	@Autowired
	private OAuthPVTTokenService oAuthTokenService;

	/** The AUTHORIZATION_HEADER_KEY. */
	private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
	
	
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
		
		log.debug("Start of CpopAuthPVTRequestHeaderInterceptor.intercept method..");
		String accessToken = oAuthTokenService.getAccessToken();
		String authorizationHeader = String.format("Bearer %s", accessToken);

		HttpHeaders oncomingHeaders = request.getHeaders();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("idp-trace-id", oncomingHeaders.getFirst("idp-trace-id"));
		oncomingHeaders.clear();
		oncomingHeaders.add(AUTHORIZATION_HEADER_KEY, authorizationHeader);
		oncomingHeaders.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
		oncomingHeaders.add(HttpHeaders.ACCEPT_ENCODING, "deflate");
		oncomingHeaders.add("idp-trace-id", httpHeaders.getFirst("idp-trace-id"));

		ClientHttpResponse response = null;

		response = execution.execute(request, body);

		if (HttpStatus.UNAUTHORIZED.equals(response.getStatusCode())
				|| HttpStatus.FORBIDDEN.equals(response.getStatusCode())) {
			log.info("EPOCH_AUTH_TOKEN_CALL_FAILED");
			log.warn("EPOCH_API_AUTH_TOKEN_CALL_FAILURE=\"EPOCH API Token Failure Summary\"");
			log.info("EPOCH CpopAuthPVTRequestHeaderInterceptor  - Auth failed so getting the new token from the server");
				accessToken = oAuthTokenService.getAccessTokenFromServer();
				authorizationHeader = String.format("Bearer %s", accessToken);

				oncomingHeaders.remove(AUTHORIZATION_HEADER_KEY);
				oncomingHeaders.add(AUTHORIZATION_HEADER_KEY, authorizationHeader);

				response = execution.execute(request, body);
				}
		
		log.debug("End of CpopAuthPVTRequestHeaderInterceptor.intercept method..");

		return response;
	}

}
