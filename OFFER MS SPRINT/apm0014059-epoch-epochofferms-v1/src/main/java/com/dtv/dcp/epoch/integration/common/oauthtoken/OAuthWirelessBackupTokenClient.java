package com.dtv.dcp.epoch.integration.common.oauthtoken;

import com.fasterxml.jackson.databind.JsonNode;

public interface OAuthWirelessBackupTokenClient {
	public JsonNode getAccessToken(final String request);
}
