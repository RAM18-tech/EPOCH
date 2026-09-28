package com.dtv.dcp.epoch.integration;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.common.EpochDataloadBaseClient;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.util.JsonService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author sx4928
 *
 */
@Component("EpochDataloadClient")
public class EpochDataloadClient extends EpochDataloadBaseClient {

	/**
	 * The log.
	 */
	private static final Logger log = LoggerFactory.getLogger(EpochDataloadClient.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * The rest baseUrl.
	 */
	@Value("${apiclient.rest.dataload.baseUrl}")
	private String baseUrl;
	
	/**
	 * The  proxyEnabled.
	 */
	@Value("${apiclient.rest.dataload.proxyEnabled}")
	private String proxyEnabled;

	/**
	 * The  sslEnabled.
	 */
	@Value("${apiclient.rest.dataload.sslEnabled:false}")
	private boolean sslEnabled;

	/**
	 * The connectTimeout.
	 */
	@Value("${apiclient.rest.dataload.connectTimeout:4000}")
	private int connectTimeout;

	/**
	 * The rest readTimeout.
	 */
	@Value("${apiclient.rest.dataload.readTimeout:4000}")
	private int readTimeout;

	/**
	 * The Constant dataload.
	 */
	private static final String DATALOAD = "dataload";
	/**
	 * The pageLimit.
	 */
	@Value("${pageLimit}")
	private int pageLimit;
	/**
	 * The rest noOfConnections.
	 */
	@Value("${apiclient.rest.dataload.noOfConnections}")
	private int noOfConnections;
	
	/**
	 * The the abstract method  getting value of APIKEY.
	 */
	@Override
	protected String getApiKey() {
		return DATALOAD;
	}

	/**
	 * The the abstract method  getting value of isProxyEnabled.
	 */
	@Override
	protected boolean isProxyEnabled() {
		return "enabled".equalsIgnoreCase(proxyEnabled);

	}

	/**
	 * The the abstract method  getting value of isSSLEnabled.
	 */
	@Override
	protected boolean isSSLEnabled() {
		return sslEnabled;

	}

	/**
	 * The the abstract method  getting value of connectTimeout.
	 */
	protected int connectTimeout() {
		return connectTimeout;
	}

	/**
	 * The the abstract method  getting value of readTimeout.
	 */
	protected int readTimeout() {
		return readTimeout;
	}

	/**
	 * The the abstract method getting value of connections.
	 */
	protected int noOfConnections() {
		return noOfConnections;
	}
	
	@Override
	public CTBenefitsResponse getSatelliteBenefits(CTBenefitsRequest ctBenefitsRequest) {
		log.info("Start of EpochDataloadClient.getSatelliteBenefits method with baseUrl..[{}]",baseUrl);
		log.info("EPOCH_BENEFITS_CALL_CT_REQUEST: [{}]", JsonService.getJsonFromObject(ctBenefitsRequest));
		final String apiPath = "getbenefits";
		CTBenefitsResponse benefitsResponse = null;
		
		List<String> benefitIdsList =  ctBenefitsRequest.getBenefitIds() == null ? new ArrayList<>() : ctBenefitsRequest.getBenefitIds();
		List<String> benefitCodeList = ctBenefitsRequest.getBenefitCodes() == null ? new ArrayList<>() : ctBenefitsRequest.getBenefitCodes();
		List<String> agreementIdList = ctBenefitsRequest.getAgreementIds() == null ? new ArrayList<>() : ctBenefitsRequest.getAgreementIds();

		JsonNode jsonNode = null;
		try {
			if(CollectionUtils.isNotEmpty(agreementIdList)) {
				jsonNode = makeGetCall(JsonNode.class, baseUrl,
						apiPath+"?agreementIDs=".concat(String.join(",", agreementIdList))
								.concat("&state=").concat(ctBenefitsRequest.getState())
				);
			} else {
				jsonNode = makeGetCall(JsonNode.class, baseUrl,
						apiPath+"?benefitIds=".concat(String.join(",", benefitIdsList))
								.concat("&billingBenefitCodes=").concat(String.join(",", benefitCodeList))
								.concat("&state=").concat(ctBenefitsRequest.getState())
				);
			}
			
		} catch (Exception excep) {
			log.error("EPOCH_DATALOAD_CLIENT_MET_EXCEPTION_AS_URL_COMPROMISED" + excep);
		}

		benefitsResponse = MAPPER.convertValue(jsonNode, new TypeReference<CTBenefitsResponse>() {
		});
		log.info("EPOCH_BENEFITS_CALL_SUCCESS");
		log.debug("End of EpochDataloadClient.getSatelliteBenefits method..");
		return benefitsResponse;

	}
	
}
