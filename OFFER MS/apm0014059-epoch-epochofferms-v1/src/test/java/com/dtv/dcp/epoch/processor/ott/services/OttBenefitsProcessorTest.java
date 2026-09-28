package com.dtv.dcp.epoch.processor.ott.services;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.BenefitRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPBenefitsHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OttBenefitsProcessorTest {

// *//** The OttSalesOffersProcessor . *//*

	@InjectMocks
	private OttServicesBenefitsProcessor ottServicesBenefitsProcessor;

	@Mock
	CpopClient cpopClient;

	@Mock
	CPOPBenefitsHelper cpopBenefitsHelper;


	private static String BENEFIT_REQUEST = "{\n" +
			"    \"productTypes\": [\n" +
			"        \"video-addon\"\n" +
			"    ],\n" +
			"    \"benefitIds\": [\n" +
			"       \"cc6b7b22-27b8-4863-b5d8-47a893fafd9a\"\n" +
			"    ],\n" +
			"    \"benefitCodes\": [\n" +
			"    \t\"BC_CPOP_flatoff_data1\"\n" +
			"    \t],\n" +
			"    \"userType\": \"agent\",\n" +
			"    \"account\": {\n" +
			"        \"accountType\": \"mobility\",\n" +
			"        \"contractIndicator\": true,\n" +
			"        \"contractVersion\": \"v1\",\n" +
			"        \"zipCode\": \"32957\"\n" +
			"    },\n" +
			"    \"state\": \"staged\"\n" +
			"}";
	private static String BENEFIT_RESPONSE = "{\n" +
			"    \"limit\": 200,\n" +
			"    \"offset\": 0,\n" +
			"    \"count\": 1,\n" +
			"    \"total\": 1,\n" +
			"    \"benefits\": [\n" +
			"        {\n" +
			"            \"duration\": 25,\n" +
			"            \"billingBenefitCode\": \"BC_Cpop_percoff_1\",\n" +
			"            \"promoSourceSkuProductType\": \"video-plan\",\n" +
			"            \"beneficiaryProductFamily\": \"OTT\",\n" +
			"            \"billAfterPeriod\": true,\n" +
			"            \"benefitType\": \"percent-off\",\n" +
			"            \"promoSourceSkuProductFamily\": \"OTT\",\n" +
			"            \"contractIndicator\": \"non-contract\",\n" +
			"            \"promoPriority\": 4,\n" +
			"            \"benefitDisplayName\": {\n" +
			"                \"en\": \"BDNCpop_percoff_1\"\n" +
			"            },\n" +
			"            \"beneficiaryProductType\": [\n" +
			"                \"video-plan\"\n" +
			"            ],\n" +
			"            \"benefitLevel\": \"account\",\n" +
			"            \"freeTrialPromo\": false,\n" +
			"            \"period\": \"NoOfDays\",\n" +
			"            \"benefitCategory\": \"Product\",\n" +
			"            \"applicableProducts\": [\n" +
			"                {\n" +
			"                    \"products\": [\n" +
			"                        {\n" +
			"                            \"typeId\": \"product\",\n" +
			"                            \"id\": \"30087085-93a0-472d-a3ff-1ab543c6c348\",\n" +
			"                            \"key\": \"BASE-PLUS-2018\",\n" +
			"                            \"productType\": \"video-plan\"\n" +
			"                        }\n" +
			"                    ]\n" +
			"                }\n" +
			"            ],\n" +
			"            \"id\": \"cc6b7b22-27b8-4863-b5d8-47a893fafd9a\",\n" +
			"            \"version\": 2,\n" +
			"            \"code\": \"BC_Cpop_percoff_1\",\n" +
			"            \"benefitName\": {\n" +
			"                \"en\": \"BN_Cpop_percoff_1\"\n" +
			"            },\n" +
			"            \"description\": {\n" +
			"                \"en\": \"\"\n" +
			"            },\n" +
			"            \"startDate\": \"2019-10-16T00:00:00.000Z\",\n" +
			"            \"endDate\": \"2019-11-01T00:00:00.000Z\",\n" +
			"            \"value\": {\n" +
			"                \"percentage\": 25\n" +
			"            },\n" +
			"            \"isQuotaBased\": false\n" +
			"        }\n" +
			"    ]\n" +
			"}";



	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGetBenefits() throws IOException {

		// Temp  need to update late once logic updated

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		BenefitRequest benefitRequest = null;
		CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
		CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
		JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);

		try {

			benefitRequest =  objectMapper.readValue(BENEFIT_REQUEST, BenefitRequest.class);
			BeanUtils.copyProperties(benefitRequest, ctBenefitsRequest);

			ctBenefitsResponse =  objectMapperRes.readValue(BENEFIT_RESPONSE, CTBenefitsResponse.class);
			// JsonNode jsonNode =  objectMapperRes.readTree(BENEFIT_RESPONSE);//objectMapperRes.readValue(BENEFIT_RESPONSE, JsonNode.class);
		} catch(Exception e) {
			//do nothing
		}

		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);

		BenefitRequestWrapper benefitRequestWrapper = new BenefitRequestWrapper();
		benefitRequestWrapper.setBenefitRequest(benefitRequest);
		doReturn(ctBenefitsResponse).when(cpopClient).getBenefits(ctBenefitsRequest);

		when(cpopClient.getBenefits(Mockito.any())).thenReturn(ctBenefitsResponse);

		when(cpopBenefitsHelper.filterByDate(Mockito.any())).thenReturn(ctBenefitsResponse.getBenefits());


		CTBenefitsResponse benefits = ottServicesBenefitsProcessor.getBenefits(benefitRequestWrapper);


		assertNotNull(benefits);
	}

}
