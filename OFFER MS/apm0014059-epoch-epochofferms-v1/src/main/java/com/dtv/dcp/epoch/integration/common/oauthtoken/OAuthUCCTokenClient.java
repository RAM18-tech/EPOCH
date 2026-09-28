package com.dtv.dcp.epoch.integration.common.oauthtoken;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by nk3077 on 03/21/2019.
 */

public interface OAuthUCCTokenClient {
	public JsonNode getAccessToken(final String request);
}
