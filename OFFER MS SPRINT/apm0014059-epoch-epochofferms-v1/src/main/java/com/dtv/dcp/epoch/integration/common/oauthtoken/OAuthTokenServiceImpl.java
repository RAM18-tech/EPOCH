package com.dtv.dcp.epoch.integration.common.oauthtoken;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by nk3077 on 03/21/2019.
 * 
 * This class is responsible to get the token and have that in to JVM cache
 */

@Component
public class OAuthTokenServiceImpl implements OAuthTokenService {
	
	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(OAuthTokenServiceImpl.class);

	private static final String TOKEN_KEY = "CT_ACCESS_TOKEN";
	
	
	/** The rest request. */
	@Value("${apiclient.rest.cttokenservice.request}")
	private String request;
	
	/** The oAuthTokenClient. */
	@Autowired
	private OAuthTokenClient oAuthTokenClient;

	/** The map to have token as local. */
	private Map<String, String> accessTokenMap = new ConcurrentHashMap<>();

	
	

	/**
	 * accessToken(). if not in cache then make a call to server
	 *
	 * @return  String accessToken
	 * @throws ServiceException
	 */
	@Override
	public String getAccessToken() {
		log.debug("Start of OAuthTokenServiceImpl.getAccessToken() method..");
		String accessToken;

		if (isTokenAvailableInCache()) {
			accessToken = accessTokenMap.get(TOKEN_KEY);
		} else {
			accessToken = getAccessTokenFromServer();
		}
		log.debug("End of OAuthTokenServiceImpl.getAccessToken() method..");
		return accessToken;
	}

	@Override
	public String getAccessTokenFromServer() {
		
		log.debug("Start of OAuthTokenServiceImpl.getAccessTokenFromServer() method..");
		String accessToken;

		JsonNode response = oAuthTokenClient.getAccessToken(request);

		accessToken = Optional.ofNullable(response).map(j -> j.get("access_token")).map(JsonNode::asText).orElse(null);

		accessTokenMap.put(TOKEN_KEY, accessToken);
		log.debug("End of OAuthTokenServiceImpl.getAccessTokenFromServer() method..");
		return accessToken;
	}

	private boolean isTokenAvailableInCache() {
		return accessTokenMap.containsKey(TOKEN_KEY);
	}

}
