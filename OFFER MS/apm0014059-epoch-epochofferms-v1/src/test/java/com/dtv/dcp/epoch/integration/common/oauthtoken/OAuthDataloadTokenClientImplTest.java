package com.dtv.dcp.epoch.integration.common.oauthtoken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.integration.common.BaseRestClient;
import com.fasterxml.jackson.databind.JsonNode;

@ExtendWith(MockitoExtension.class)
public class OAuthDataloadTokenClientImplTest {

	@InjectMocks
	private OAuthDataloadTokenClientImpl oauthDataloadTokenClientImpl;
	
	@Mock
    BaseRestClient baseRestClient;
	
	RestTemplate restCustomTemplate;
	
	RestTemplateBeanFactory restTemplateBeanFactory;
	
	/**
     * Setup.
     */
	@BeforeEach
    public void init() throws Exception {
        restTemplateBeanFactory = Mockito.mock(RestTemplateBeanFactory.class);
        restCustomTemplate = Mockito.mock(RestTemplate.class);

        MockitoAnnotations.openMocks(this);
        try {
            ReflectionTestUtils.setField(oauthDataloadTokenClientImpl, "baseUrl", "url");
        } catch (Exception e) {
        }
    }

	@Test
	public void testGetAccessToken() throws Exception {
		String request = "request";
		String response = "{\"access_token\":\"abcd\",\"token_type\":\"Bearer\",\"expires_in\":172800,\"scope\":\"manage_project:epoch-dataload\"}";
		ResponseEntity<Object> responseEntity = new ResponseEntity<>(response, HttpStatus.OK);

		when(restCustomTemplate.postForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(responseEntity);

		JsonNode result = oauthDataloadTokenClientImpl.getAccessToken(request);

		assertNotNull(result);
		assertEquals("abcd", result.get("access_token").asText());
	}

}
