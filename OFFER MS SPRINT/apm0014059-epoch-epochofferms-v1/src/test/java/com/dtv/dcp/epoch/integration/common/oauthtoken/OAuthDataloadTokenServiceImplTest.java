package com.dtv.dcp.epoch.integration.common.oauthtoken;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.JsonNode;

@ExtendWith(MockitoExtension.class)
public class OAuthDataloadTokenServiceImplTest {

	@Mock
	private OAuthDataloadTokenClient oAuthDataloadTokenClient;

	@InjectMocks
	private OAuthDataloadTokenServiceImpl oauthDataloadTokenServiceImpl;
	
	Map<String, String> accessTokenMap = new ConcurrentHashMap<>();
	
	/**
     * Setup.
     */
	@BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        try {
            ReflectionTestUtils.setField(oauthDataloadTokenServiceImpl, "request", "request");
            ReflectionTestUtils.setField(oauthDataloadTokenServiceImpl, "accessTokenMap", accessTokenMap);
        } catch (Exception e) {
        }
    }

	@Test
	public void testGetAccessToken() throws Exception {

		JsonNode response = Mockito.mock(JsonNode.class);
		JsonNode accessTokenNode = Mockito.mock(JsonNode.class);
		String accessToken = "accessToken";

		when(response.get("access_token")).thenReturn(accessTokenNode);
		when(accessTokenNode.asText()).thenReturn(accessToken);
		when(oAuthDataloadTokenClient.getAccessToken(ArgumentMatchers.anyString())).thenReturn(response);

		String serverToken = oauthDataloadTokenServiceImpl.getAccessToken();
		assertEquals(accessToken, serverToken);

		String cachedToken = oauthDataloadTokenServiceImpl.getAccessToken();

		assertEquals(accessToken, cachedToken);
		
	}

}
