package com.dtv.dcp.epoch.integration.common.oauthtoken;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * @author sn611j
 *
 */
public interface OAuthBackUpTokenClient {
	public JsonNode getAccessToken(final String request);
}
