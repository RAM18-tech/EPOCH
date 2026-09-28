/*package com.dtv.dcp.epoch.processor.ott;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.cache.CacheService;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.wirelessaccount.WirelessClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.wireless.WirelessResponse;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.customergraph.EligibleWirelessAccount;
import com.dtv.dcp.epoch.util.JsonService;



@Component
public class SocCodesEligibilityProcessor {

	*//** The log. *//*
	private static final Logger log = LoggerFactory.getLogger(SocCodesEligibilityProcessor.class);

	*//** The wireless client. *//*
	@Autowired
	private WirelessClient wirelessClient;

	*//** The Cache Service. *//*
	@Autowired
	private CacheService cacheService;

	*//** The dtvNow Opus Offers Processor. *//*
	//@Autowired
	//private SocCodesEligibilityProcessor socCodesEligibilityProcessor;
	
	@Autowired
	OttMobilityOfferFilterProcessor ottMobilityOfferFilterProcessor;

	*//** The Constant WIRELESS_CLIENT. *//*
	private static final String WIRELESS_CLIENT = "WIRELESS_CLIENT";

	*//** The Constant SUSPENDED_ACCOUNT. *//*
	private static final String SUSPENDED_ACCOUNT = "Suspended Account";

	*//** The Constant DTVNEW. *//*
	private static final String DTVNEW = "DTVNew";

	*//** The Constant GOT_CLIENT_EXCEPTION. *//*
	private static final String GOT_CLIENT_EXCEPTION = "Got ClientException : {} ";

	*//** The Constant GOT_EXCEPTION. *//*
	private static final String GOT_EXCEPTION = "Got Exception : {}";

	*//**
	 * Gets the wireless info.
	 *
	 * @param customerAccountNumbers
	 *            the customer account numbers
	 * @param offerResponse
	 *            the offer response
	 * @param mobilityBaseOffers
	 *            the mobility base offers
	 * @param mode
	 *            the mode
	 * @param sessionId
	 *            the session id
	 * @return the wireless info
	 * @throws ClientException
	 *             the client exception
	 *//*
	@SuppressWarnings({ "squid:S2259" })
	public List<CTOffer> getWirelessInfo(String customerAccountNumbers, List<CTOffer> offerResponseList,
			boolean mobilityBaseOffers, String mode, String sessionId) throws ClientException {
		log.debug("Start of DtvnOffersProcessor.getWirelessInfo() method..");
		WirelessResponse wirelessResponse = null;
		List<CTOffer> qualifiedOffers = new ArrayList<>();

		try {
			EligibleWirelessAccount eligibleWirelessAccount = null;

			if (Optional.ofNullable(sessionId).isPresent() && !sessionId.isEmpty()) {
				// Retrieving session Payload from Remote Cache
				eligibleWirelessAccount = getPrimaryCtnfromRemoteCache(sessionId);
			}

			if (Optional.ofNullable(eligibleWirelessAccount).isPresent()
					&& Optional.ofNullable(eligibleWirelessAccount.getAccountNumber()).isPresent()
					&& !eligibleWirelessAccount.getAccountNumber().isEmpty()) {
				try {

					// retrieve the Qualified Mobility Offer based on the soc codes eligibility
					qualifiedOffers = ottMobilityOfferFilterProcessor.performMobilityOfferEligibilityRules(
							eligibleWirelessAccount, offerResponseList, mobilityBaseOffers, mode);
					log.debug("QualifiedOffers ..", qualifiedOffers);

				} catch (ServiceException se) {
					throw se;
				} catch (Exception e) {
					log.error("Got Exception with the user associated Eligible Wireless Account Info :", e);
					throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
							.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
				}
			}

			if (!Optional.ofNullable(qualifiedOffers).isPresent() || qualifiedOffers.isEmpty()) {
				// Invoke wireless service and get the WirelessResponse
				wirelessResponse = retriveWirelessResponse(customerAccountNumbers, wirelessResponse);

				// retrieve the Qualified Mobility Offer based on the soc codes eligibility
				qualifiedOffers = ottMobilityOfferFilterProcessor.retrieveQualifiedMobilityOffer(wirelessResponse,
						offerResponseList, mobilityBaseOffers, mode, sessionId);
				log.debug("Filtered QualifiedOffers ..", qualifiedOffers);
			}

		} catch (ClientException ce) {
			if (Optional.ofNullable(ce.getSource()).isPresent() && !ce.getSource().isEmpty()
					&& ce.getSource().equalsIgnoreCase(SUSPENDED_ACCOUNT)) {
				throw ce;
			} else {
				log.error(GOT_CLIENT_EXCEPTION, ce);
				ce.setSource(WIRELESS_CLIENT);
				throw ce;
			}

		} catch (ServiceException se) {
			throw se;
		} catch (Exception ex) {
			log.error(GOT_EXCEPTION, ex);
			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
					.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
		}
		log.debug("End of DtvnOffersProcessor.getWirelessInfo() method.. ");
		return qualifiedOffers;
	}

	*//**
	 * Gets the primary ctnfrom remote cache.
	 *
	 * @param sessionId
	 *            the session id
	 * @return the primary ctnfrom remote cache
	 *//*
	private EligibleWirelessAccount getPrimaryCtnfromRemoteCache(String sessionId) {

		EligibleWirelessAccount eligibleWirelessAccount = null;

		try {
			String key = ottMobilityOfferFilterProcessor.getKey(DTVNEW + sessionId);
			String payload = cacheService.getFromCache(key);
			if (Optional.ofNullable(payload).isPresent()) {
				eligibleWirelessAccount = Optional.ofNullable(payload).isPresent()
						? JsonService.getObjectFromJsonTree(payload, "value", EligibleWirelessAccount.class)
						: null;
			}
		} catch (Exception e) {
			log.error("Got Exception while getting account number from session payload : ", e);
			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
					.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS007);
		}
		return eligibleWirelessAccount;
	}

	*//**
	 * Retrieve offer id response.
	 *
	 * @param accountNumbers
	 *            the account numbers
	 * @param offer
	 *            the offer
	 * @param mobilityBaseOffers
	 *            the mobility base offers
	 * @param mode
	 *            the mode
	 * @param sessionId
	 *            the session id
	 * @return the list
	 * @throws ClientException
	 *             the client exception
	 *//*
	public List<CTOffer> retrieveOfferIdResponse(String accountNumbers, CTOffer offer, boolean mobilityBaseOffers,
			String mode, String sessionId) throws ClientException {

		log.debug("Start of DtvnOffersProcessor.retrieveOfferIdResponse() method..");
		List<CTOffer> qualifiedOffers = null;
		List<CTOffer> offerList = new ArrayList<>();
		try {
			offerList.add(offer);
			// get the wireless response, perform the soc validation for mobility offers.
			qualifiedOffers = getWirelessInfo(accountNumbers, offerList, mobilityBaseOffers, mode, sessionId);

		} catch (ClientException ce) {
			throw ce;
		}

		log.debug("End of DtvnOffersProcessor.retrieveOfferIdResponse() method..");
		return qualifiedOffers;
	}

	*//**
	 * Retrive wireless response.
	 *
	 * @param customerAccountNumbers
	 *            the customer account numbers
	 * @param wirelessResponse
	 *            the wireless response
	 * @return the wireless response
	 * @throws ClientException
	 *             the client exception
	 *//*
	public WirelessResponse retriveWirelessResponse(String customerAccountNumbers, WirelessResponse wirelessResponse)
			throws ClientException {

		WirelessResponse wlsResponse = null;
		try {
			wlsResponse = wirelessClient.getByAccountNumber(customerAccountNumbers);
			log.debug("WirelessResponse ..", wirelessResponse);
		} catch (Exception se) {
			ClientException ce = new ClientException("", "", 0);
			ce.setSource(WIRELESS_CLIENT);
			log.error(GOT_EXCEPTION, se.getMessage(), se);
			throw ce;
		}
		return wlsResponse;
	}

}
*/