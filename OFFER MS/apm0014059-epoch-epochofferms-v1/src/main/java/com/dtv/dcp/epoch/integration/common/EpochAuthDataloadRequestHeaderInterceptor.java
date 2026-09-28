package com.dtv.dcp.epoch.integration.common;

import java.io.IOException;

import org.slf4j.MDC;
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

import com.dtv.dcp.epoch.integration.common.oauthtoken.OAuthDataloadTokenService;

import io.opentracing.util.GlobalTracer;

/**
 * 
 * @author sx4928
 *
 */
@Component("EpochAuthDataloadRequestHeaderInterceptor")
public class EpochAuthDataloadRequestHeaderInterceptor implements ClientHttpRequestInterceptor {
	
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(EpochAuthDataloadRequestHeaderInterceptor.class);
	
	/** The oAuthDataloadTokenService. */
	@Autowired
	private OAuthDataloadTokenService oAuthDataloadTokenService;

	/** The AUTHORIZATION_HEADER_KEY. */
	private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
	
	/** The OPENTRACING_TRACE_ID. */
	private static final String OPENTRACING_TRACE_ID = "idp-trace-id";
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
		
		log.debug("Start of EpochAuthDataloadRequestHeaderInterceptor.intercept method..");

		String accessToken = oAuthDataloadTokenService.getAccessToken();
		String authorizationHeader = String.format("Bearer %s", accessToken);

		HttpHeaders oncomingHeaders = request.getHeaders();
		HttpHeaders httpHeaders = new HttpHeaders();
		String parentTraceId = (String) MDC.get((String) OPENTRACING_TRACE_ID);
		log.info("OPENTRACING_TRACE_ID:::",parentTraceId);
		if(null != GlobalTracer.get().activeSpan()) {
			String currentTraceId = GlobalTracer.get().activeSpan().toString();
			MDC.put((String) OPENTRACING_TRACE_ID, currentTraceId);
			log.info("current active span:::", currentTraceId);
		}
		httpHeaders.add("idp-trace-id", oncomingHeaders.getFirst(OPENTRACING_TRACE_ID));
		oncomingHeaders.clear();
		oncomingHeaders.add(AUTHORIZATION_HEADER_KEY, authorizationHeader);
		oncomingHeaders.add(HttpHeaders.ACCEPT_ENCODING, "gzip");
		oncomingHeaders.add(HttpHeaders.ACCEPT_ENCODING, "deflate");
		oncomingHeaders.add(OPENTRACING_TRACE_ID, httpHeaders.getFirst(OPENTRACING_TRACE_ID));

		ClientHttpResponse response = null;

		response = execution.execute(request, body);

		if (HttpStatus.UNAUTHORIZED.equals(response.getStatusCode())
				|| HttpStatus.FORBIDDEN.equals(response.getStatusCode())) {
			log.info("EPOCH_AUTH_TOKEN_CALL_FAILED");
			log.warn("EPOCH_API_AUTH_TOKEN_CALL_FAILURE=\"EPOCH API Token Failure Summary\"");
			log.info("EPOCH EpochAuthDataloadRequestHeaderInterceptor  - Auth failed so getting the new token from the server");
				accessToken = oAuthDataloadTokenService.getAccessTokenFromServer();
				authorizationHeader = String.format("Bearer %s", accessToken);

				oncomingHeaders.remove(AUTHORIZATION_HEADER_KEY);
				oncomingHeaders.add(AUTHORIZATION_HEADER_KEY, authorizationHeader);

				response = execution.execute(request, body);
				}
		
		log.debug("End of EpochAuthDataloadRequestHeaderInterceptor.intercept method..");

		return response;
	}

}
