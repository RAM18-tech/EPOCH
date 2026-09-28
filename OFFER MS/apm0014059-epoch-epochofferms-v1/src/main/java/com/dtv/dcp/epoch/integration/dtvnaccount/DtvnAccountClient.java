package com.dtv.dcp.epoch.integration.dtvnaccount;

import java.net.SocketTimeoutException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.httpclient.RestApiClient;
import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.dtvnaccount.Account;
import com.dtv.dcp.epoch.model.common.dtvnaccount.DtvnAccount;
import com.dtv.dcp.epoch.model.common.dtvnaccount.DtvnAccountResponse;
import com.dtv.dcp.epoch.util.JsonService;

/**
 * The Class DtvnAccountClient.
 */
@Service
public class DtvnAccountClient implements RestApiClient {

	private static final String CONTENT = "content";

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(DtvnAccountClient.class);

	/** The Constant DTVNACCOUNT. */
	private static final String DTVNACCOUNT = "dtvnow";

	/** The rest template. */
	private RestTemplate restTemplate;

	/** The dtvn account base URL. */
	@Value("${apiclient.rest.dtvnow.dtvnAccountBaseUrl}")
	private String dtvnAccountBaseURL;

	/** The readTimeout. */
	@Value("${apiclient.rest.dtvnow.readTimeout:10000}")
	private int readTimeout;
	
	/**
	 * Instantiates a new dtvn account client.
	 */
	public DtvnAccountClient() {
	}

	/**
	 * Instantiates a new dtvn account client.
	 *
	 * @param restTemplateFactory
	 *            the rest template factory
	 * @throws Exception
	 *             the exception
	 */
	@Autowired
	private DtvnAccountClient(RestTemplateBeanFactory restTemplateFactory) throws Exception {
		this.restTemplate = restTemplateFactory.getObject(DTVNACCOUNT);
	}

	/**
	 * Gets the dtvn customer account status. If customer is Subscribable then
	 * offersMs shall return the offers.
	 * 
	 * @param customerId
	 *            the customer id
	 * @return the dtvn customer account status
	 * @throws ClientException
	 *             the client exception
	 */
	@SuppressWarnings({ "squid:S2259" })
	@Retryable(maxAttemptsExpression = "${apiclient.unified.rest.default.maxAttempts}", value = {
			RestClientException.class })
	public Boolean getDtvnCustomerAccountStatus(String customerId) {
		log.debug("Start of DtvnAccountClient.getDtvnCustomerAccountStatus() method..");

		ResponseEntity<String> responseEntity = null;
		Boolean dtvnAccountStatus = false;
		DtvnAccountResponse dtvnAccountContent = null;

		// build the base offer request to send it to CPC post request.
		HttpEntity<DtvnAccount> dtvnAccountRequest = buildDtvnAccountRequest(customerId);
		responseEntity = invokeDtvnAccountService(dtvnAccountRequest);
		if (Optional.ofNullable(responseEntity).isPresent()) {

			dtvnAccountContent = JsonService.getObjectFromJsonTree(responseEntity.getBody(), CONTENT,
					DtvnAccountResponse.class);
		}
		if (Optional.ofNullable(dtvnAccountContent).isPresent()
				&& Optional.ofNullable(dtvnAccountContent.getAccountResponse()).isPresent()) {
			dtvnAccountStatus = dtvnAccountContent.getAccountResponse().getSubscribable();
		}
		log.debug("End of DtvnAccountClient.getDtvnCustomerAccountStatus() method..");
		return dtvnAccountStatus;
	}

	/**
	 * Invoke dtvn account service.
	 
	 * @param dtvnAccountRequest
	 *            the dtvn account request
	 * @return the response entity
	 * @throws ClientException
	 *             the client exception
	 */
	public ResponseEntity<String> invokeDtvnAccountService(HttpEntity<DtvnAccount> dtvnAccountRequest) {
		log.info("Start of DtvnAccountClient.invokeDtvnAccountService() method..");
		ResponseEntity<String> response = null;
		log.info("dtvnAccountBaseURL:[{}] ", dtvnAccountBaseURL);
		long startTimeMillis = System.currentTimeMillis();
		response = restTemplate.exchange(dtvnAccountBaseURL, HttpMethod.POST, dtvnAccountRequest, String.class);
		log.info("TOTAL_TIME_TAKEN_FROM_DtvnAccountService-[{}]", System.currentTimeMillis() - startTimeMillis);
		log.info("End of DtvnAccountClient.invokeDtvnAccountService() method..");
		return response;

	}

	/**
	 * Builds the dtvn account request.
	 *
	 * @param customerId
	 *            the customer id
	 * @return the http entity
	 */
	public HttpEntity<DtvnAccount> buildDtvnAccountRequest(String customerId) {
		log.debug("Start of DtvnAccountClient.buildDtvnAccountRequest() method..");

		DtvnAccount dtvnAccountRequest = new DtvnAccount();
		Account account = new Account();
		account.setCustomerID(customerId);
		dtvnAccountRequest.setAccount(account);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("idpctx-user-type", "registered");

		HttpEntity<DtvnAccount> entity = new HttpEntity<>(dtvnAccountRequest, headers);
		log.debug("End of DtvnAccountClient.buildDtvnAccountRequest() method..");
		return entity;
	}

	/**
	 * Recover get dtvn customer account status.
	 *
	 * @param re
	 *            the re
	 * @return the dtvn account
	 */
	@Recover
	public Boolean recoverGetDtvnCustomerAccountStatus(RestClientException re) {
		log.error("Exception occured for DTVN account API::", re);

		if (re != null && re.getRootCause() != null && re.getRootCause() instanceof SocketTimeoutException) {
			throw new ServiceException(ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN, re).addDetail(
					ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN_DETAILS, Constants.DTVNOW_ACCOUNT_TYPE,
					Integer.toString(readTimeout));
		} else {
			throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, re).addDetail(
					ErrorMessages.DTVN_ACCOUNT_MS_BACKEND_ERROR, "Invoke DirecTVNowOrderMs For AccountInfo",
					"DtvnAccountClient.getDtvnCustomerAccountStatus");
		}
	}

}
