package com.dtv.dcp.epoch.integration.dtvnsubscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.RestApiClient;
import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ServiceError;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.jayway.jsonpath.JsonPath;

import io.reactivex.Observable;

/**
 * The Class DtvnSubscriptionClient.
 */

@Service
public class DtvnSubscriptionClient implements RestApiClient {

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(DtvnSubscriptionClient.class);

	/** The rest template. */
	private RestTemplate restTemplate;

	/** The DTVNow base URL. */
	@Value("${apiclient.rest.dtvnow.baseUrl}")
	private String dtvNowBaseURL;

	/** The DTVNow subscription URL. */
	@Value("${apiclient.rest.dtvnow.subscriptionUrl}")
	private String dtvNowSubscriptionURL;

	/** The Constant DTVNOW. */
	private static final String DTVNOW = "dtvnow";
	
	public static final String MOBILITY = "mobility";
	
	/**
	 * Instantiates a new DTV now client.
	 *
	 * @param restTemplateFactory the rest template factory
	 * @throws Exception the exception
	 */
	@Autowired
	public DtvnSubscriptionClient(RestTemplateBeanFactory restTemplateFactory) throws Exception {
		this.restTemplate = restTemplateFactory.getObject(DTVNOW);
	}
	
	/**
	 * Post mobility details asyn.
	 *
	 * @param accountNumber the account number
	 * @param primarySubscriberNumber the primary subscriber number
	 * @return the observable
	 */
	@SuppressWarnings("rawtypes")
	public Observable<ResponseEntity> postMobilityDetailsAsyn(String accountNumber,String primarySubscriberNumber) {
		return Observable.<ResponseEntity>create(sub -> {
			WirelessAccountDetails sendWirelessAccDetailsInRequest = buildRequestWithWirelessAccountDetails(accountNumber,primarySubscriberNumber);
			try {				
				HttpEntity<?> entity = new HttpEntity<>(sendWirelessAccDetailsInRequest);
				log.info("DtvnSubscriptionClient BaseURL:[{}] ", dtvNowBaseURL + dtvNowSubscriptionURL);
				long startTimeMillis = System.currentTimeMillis();
				ResponseEntity<String> response = restTemplate.exchange(dtvNowBaseURL + dtvNowSubscriptionURL,HttpMethod.POST, entity, String.class);
				log.info("TOTAL_TIME_TAKEN_FROM_DtvnSubscriptionClient-[{}]", System.currentTimeMillis() - startTimeMillis);
				if (response.getStatusCodeValue() != HttpStatus.NO_CONTENT.value()) {
				  log.error(String.format("DtvnSubscriptionClient-postMobilityDetailsAsyn-Error-Response- %s", response.getStatusCodeValue()));
				}
				sub.onNext(response);
				sub.onComplete();
				
			}catch(Exception e) {
				log.error(String.format( "DTVNowClient-postMobilityDetailsAsyn-Error %s",e.getMessage()));
				throw e;
			}
		}).doOnNext(c -> log.info("DtvnSubscriptionClient - postMobilityDetailsAsyn-Successfull..."))
		  .doOnError(e -> log.info("DtvnSubscriptionClient - postMobilityDetailsAsyn-Failed...", e));
	}

	/**
	 * Recover send mobility details.
	 *
	 * @param re the re
	 */
	@Recover
	public void recoverSendMobilityDetails(RestClientException re) {
		if (re instanceof HttpServerErrorException) {
			log.error("Error sending Mobility details as HttpServerErrorException received");
			throw new ServiceException(ErrorMessages.CTLG_WIRELESS_ERROR_UNKNOWN, re).addDetail(
					ErrorMessages.CTLG_WIRELESS_UNHANDLED_EXCEPTION_DETAILS,
					"to send Mobility details as HttpServerErrorException received",
					"DtvnSubscriptionClient.sendMobilityDetails");
		}
		if (re instanceof HttpStatusCodeException) {
			HttpStatusCodeException hse = (HttpStatusCodeException) re;
			String errors = hse.getResponseBodyAsString();
			ServiceError serviceError = JsonPath.parse(errors).read("$.error", ServiceError.class);
			ServiceException se = new ServiceException(ErrorMessages.ERROR_VALIDATION_FAILED);
			se.addDetails(serviceError.getDetails());
			log.error("Error sending Mobility details as HttpStatusCodeException received");
			throw se;
		}
		log.error("Error sending Mobility details as RestClientException received");
		throw new ServiceException(ErrorMessages.CTLG_WIRELESS_ERROR_UNKNOWN, re).addDetail(
				ErrorMessages.CTLG_WIRELESS_UNHANDLED_EXCEPTION_DETAILS,
				"to send Mobility details as RestClientException received", "DtvnSubscriptionClient.sendMobilityDetails");
	}

	/**
	 * Builds the request with wireless account details.
	 *
	 * @param accountNumber the account number
	 * @param primarySubscriberNumber the primary subscriber number
	 * @return the http entity
	 */
	public WirelessAccountDetails buildRequestWithWirelessAccountDetails(String accountNumber,String primarySubscriberNumber) {
		WirelessAccountDetails wirelessAccountSubscriptionDetails = new WirelessAccountDetails();
		wirelessAccountSubscriptionDetails.setAccountNumber(accountNumber);
		wirelessAccountSubscriptionDetails.setSubscriberNumber(primarySubscriberNumber);
		wirelessAccountSubscriptionDetails.setAccountType(MOBILITY);
		return wirelessAccountSubscriptionDetails;
	}

}
