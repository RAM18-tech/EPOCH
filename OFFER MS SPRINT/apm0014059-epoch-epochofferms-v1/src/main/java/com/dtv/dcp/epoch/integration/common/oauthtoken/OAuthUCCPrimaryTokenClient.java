/**
 * 
 */
package com.dtv.dcp.epoch.integration.common.oauthtoken;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author nf2008
 *
 */
public interface OAuthUCCPrimaryTokenClient {
	public JsonNode getAccessToken(final String request);
}
