package com.dtv.dcp.epoch.integration.common.oauthtoken;

import com.fasterxml.jackson.databind.JsonNode;

public interface OAuthWirelessTokenClient {
	public JsonNode getAccessToken(final String request);
}
