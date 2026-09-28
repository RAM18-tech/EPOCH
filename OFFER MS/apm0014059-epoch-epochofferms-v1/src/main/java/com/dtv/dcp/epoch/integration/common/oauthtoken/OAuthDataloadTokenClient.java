package com.dtv.dcp.epoch.integration.common.oauthtoken;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * @author sx4928
 *
 */
public interface OAuthDataloadTokenClient {
	public JsonNode getAccessToken(final String request);
}
