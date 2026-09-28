package com.dtv.dcp.epoch.util;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;



/*
 * Unit tests for JsonService
 */
@ExtendWith(MockitoExtension.class)
public class JsonServiceTest{

    @SuppressWarnings("unchecked")
    @Test
    public void testGetObjectFromJson() throws Exception {

        CTOfferResponse offerResponse = null;
        String response = "{\"offers\":[]}";
        offerResponse = JsonService.getObjectFromJson(response, CTOfferResponse.class);

        assertNotNull("Successfully covered the getObjectFromJson test", offerResponse);
    }

    @SuppressWarnings("unchecked")
    @Test
	public void testGetObjectFromJsonFailure() throws Exception {

		CTOfferResponse offerResponse = null;
		String response = "offers";
		ServiceException ex = catchThrowableOfType(() -> JsonService.getObjectFromJson(response, CTOfferResponse.class),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_MOCK_JSON_SERIALIZATION_ERROR));

		assertNull(offerResponse);
	}

    @SuppressWarnings("unchecked")
    @Test
    public void testGetObjectFromJsonFailureWithNull() throws Exception {

        CTOfferResponse offerResponse = null;
        offerResponse = JsonService.getObjectFromJson(offerResponse, CTOfferResponse.class);

        assertNull(offerResponse);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testGetJsonFromObject() throws Exception {

        CTOfferResponse offerResponse = null;
        String actualContent = "{\"offers\":[]}";
        offerResponse = JsonService.getObjectFromJson(actualContent, CTOfferResponse.class);
        String convertedContetfromObject = JsonService.getJsonFromObject(offerResponse);
        // assertEquals(actualContent,convertedContetfromObject);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetJsonFromObjectFailureWithNull() throws Exception {

        CTOfferResponse offerResponse = null;
        String convertedContetfromObject = JsonService.getJsonFromObject(offerResponse);
        assertEquals("null",convertedContetfromObject);
    }
}