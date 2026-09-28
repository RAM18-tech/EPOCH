package com.dtv.dcp.epoch.processor.ott;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.cache.CacheEntry;
import com.dtv.dcp.epoch.cache.CacheService;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.integration.dtvnsubscription.DtvnSubscriptionClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.Additonals;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.wireless.AdditionalOffering;
import com.dtv.dcp.epoch.model.common.wireless.Content;
import com.dtv.dcp.epoch.model.common.wireless.Subscriber;
import com.dtv.dcp.epoch.model.common.wireless.WirelessResponse;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.customergraph.EligibleWirelessAccount;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;

import io.reactivex.schedulers.Schedulers;


/**
 * This Class retrieves CSI response and process the CPC response into CatalogMs
 * Offer Response.
 *
 * @author vc402t
 */
@Component
public class OttMobilityOffersProcessor {

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(OttMobilityOffersProcessor.class);

	/** The wireless client. */
	@Autowired
	private com.dtv.dcp.epoch.integration.wirelessaccount.WirelessClient wirelessClient;

	/** The Cache Service. */
	@Autowired
	private CacheService cacheService;

	/** The Cache Service. */
	@Autowired
	private DtvnSubscriptionClient dtvnSubscriptionClient;

	/** The Constant ACTIVE. */
	private static final String ACTIVE = "ACTIVE";

	/** The Constant WIRELESS_CLIENT. */
	private static final String WIRELESS_CLIENT = "WIRELESS_CLIENT";

	/** The Constant SUSPENDED_ACCOUNT. */
	private static final String SUSPENDED_ACCOUNT = "Suspended Account";

	/** The Constant ACC_TYPE. */
	private static final String ACC_TYPE = "I";

	/** The Constant CUSTOMER_TYPE. */
	private static final String CUSTOMER_TYPE = "EMO|RMO|IMO|SMB|CRU";

	/** The Constant BAN_NOT_FOUND. */
	private static final String BAN_NOT_FOUND = "Mobility account not available";

	/** The cache name. */
	private static final String CACHE_NAME = "idseMsCatalogAccount";

	/** The object key. */
	private static final String OBJECT_KEY = "session_context_data";

	/** The Constant DTVNEW. */
	private static final String DTVNEW = "DTVNew";

	/** The Constant GOT_CLIENT_EXCEPTION. */
	private static final String GOT_CLIENT_EXCEPTION = "Got ClientException : {} ";

	/** The Constant GOT_EXCEPTION. */
	private static final String GOT_EXCEPTION = "Got Exception : {}";
	/** The feature manager helper. */

	/**
	 * Retrieve offers response.
	 *
	 * @param offerServiceType
	 *            the offer service type
	 * @param accountNumbers
	 *            the account numbers
	 * @param mobilityBaseOffers
	 *            the mobility base offers
	 * @param sessionId
	 *            the session id
	 * @return the list
	 * @throws ClientException
	 *             the client exception
	 */

	// NK first method to use
	public List<CTOffer> retrieveOffersResponse(List<CTOffer> offerList, String accountNumbers,
			boolean mobilityBaseOffers, String sessionId,OfferRequestWrapper offerRequestWrapper) throws ClientException {



		log.debug("Start of DtvnOffersProcessor.retrieveOfferResponse(offerServiceType) method.. {}");

		List<CTOffer> qualifiedOffers = null;
		try {
			if (Optional.ofNullable(accountNumbers).isPresent()) {
				/*if(featureManagerHelper.isEnabled("idp-dtvn-cassandraoffers")){
					listOfferResponse = dtvnBaseOffersProcessor.getCassandraOfferResponse(offerServiceType, true, baseOfferRequestWrapper);

				}else{
					listOfferResponse = dtvnBaseOffersProcessor.getOfferResponse(offerServiceType, true, baseOfferRequestWrapper);
				}*/

				//offerResponse = listOfferResponse!=null ? new Collection<Offer>(listOfferResponse) : null;
				qualifiedOffers = getWirelessInfo(accountNumbers, offerList, mobilityBaseOffers, null, sessionId, offerRequestWrapper);
			} else {
				throw new ClientException(null, BAN_NOT_FOUND, 0);
			}

		} catch (ClientException ce) {
			throw ce;
		}

		log.debug("End of DtvnOffersProcessor.retrieveOfferResponse(offerServiceType) method.. {}");

		return qualifiedOffers;
	}

	/**
	 * Retrieve offer id response.
	 *
	 * @param accountNumbers            the account numbers
	 * @param offer            the offer
	 * @param mobilityBaseOffers            the mobility base offers
	 * @param mode            the mode
	 * @param sessionId the session id
	 * @return the list
	 * @throws ClientException             the client exception
	 */
	/*public List<CTOffer> retrieveOfferIdResponse(String accountNumbers, List<CTOffer> offerList, boolean mobilityBaseOffers,
			String mode, String sessionId) throws ClientException {

		log.debug("Start of DtvnOffersProcessor.retrieveOfferIdResponse() method..");
		List<CTOffer> qualifiedOffers = null;
		try {
			offerList.add(offer);
			offerResponse = new Collection<>(offerList);
				// get the wireless response, perform the soc validation for mobility offers.
				qualifiedOffers = getWirelessInfo(accountNumbers, offerList, mobilityBaseOffers, mode, sessionId);

		} catch (ClientException ce) {
			throw ce;
		}

		log.debug("End of DtvnOffersProcessor.retrieveOfferIdResponse() method..");
		return qualifiedOffers;
	}*/

	/**
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
	 */
	@SuppressWarnings({ "squid:S2259" })
	public List<CTOffer> getWirelessInfo(String customerAccountNumbers, List<CTOffer> offerList,
			boolean mobilityBaseOffers, String mode, String sessionId, OfferRequestWrapper offerRequestWrapper) throws ClientException {

		log.debug("Start of DtvnOffersProcessor.getWirelessInfo() method..");
		WirelessResponse wirelessResponse = null;
		List<CTOffer> qualifiedOffers = new ArrayList<>();

		try {
			EligibleWirelessAccount eligibleWirelessAccount = null;

			if (Optional.ofNullable(sessionId).isPresent() && !sessionId.isEmpty()) {
				// Retrieving session Payload from Remote Cache
				eligibleWirelessAccount = getPrimaryCtnfromRemoteCache(sessionId, offerRequestWrapper);
			}

			if (Optional.ofNullable(eligibleWirelessAccount).isPresent()
					&& Optional.ofNullable(eligibleWirelessAccount.getAccountNumber()).isPresent()
					&& !eligibleWirelessAccount.getAccountNumber().isEmpty()) {
				try {

					// retrieve the Qualified Mobility Offer based on the soc codes eligibility
					qualifiedOffers = performMobilityOfferEligibilityRules(eligibleWirelessAccount, offerList,
							mobilityBaseOffers, mode);
					log.debug("QualifiedOffers ..", OffersUtils.sanitizeData(qualifiedOffers));

				} catch (ServiceException se) {
					throw se;
				} catch (Exception e) {
					log.error("Got Exception with the user associated Eligible Wireless Account Info :", e);
		            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
							&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
							&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
							&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
		    			throw new ServiceException(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR)
		    					.addDetail(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
		            } else {
		    			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
						.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
		            }
				}
			}

			if (!Optional.ofNullable(qualifiedOffers).isPresent() || qualifiedOffers.isEmpty()) {
				// Invoke wireless service and get the WirelessResponse
				wirelessResponse = retriveWirelessResponse(customerAccountNumbers, wirelessResponse);

				// retrieve the Qualified Mobility Offer based on the soc codes eligibility
				qualifiedOffers = retrieveQualifiedMobilityOffer(wirelessResponse, offerList, mobilityBaseOffers,
						mode, sessionId);
				log.debug("Filtered QualifiedOffers ..", OffersUtils.sanitizeData(qualifiedOffers));
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
            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
					&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
    			throw new ServiceException(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR)
    					.addDetail(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
            } else {
    			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
				.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
            }
		}
		log.debug("End of DtvnOffersProcessor.getWirelessInfo() method.. ");
		return qualifiedOffers;
	}


	/**
	 * Retrive wireless response.
	 *
	 * @param customerAccountNumbers the customer account numbers
	 * @param wirelessResponse the wireless response
	 * @return the wireless response
	 * @throws ClientException the client exception
	 */
	private WirelessResponse retriveWirelessResponse(String customerAccountNumbers, WirelessResponse wirelessResponse)
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

	/**
	 * Gets the customer SOC.
	 *
	 * @param wirelessResponse            the wireless response
	 * @param mobilityBaseOffers            the mobility base offers
	 * @return the customer SOC
	 * @throws ClientException the client exception
	 */
	public Map<String, Map<String, List<String>>> getCustomerSOC(WirelessResponse wirelessResponse, boolean mobilityBaseOffers)
			throws ClientException {
		log.debug("Start of DtvnOffersProcessor.processWirelessResponse() method..");
		Map<String, List<String>> socByGroupId = new HashMap<>();
		Map<String, Map<String, List<String>>> customerAccountGroupIdMap = new HashMap<>();
		List<Content> contentList = null;
		List<String> activeSubsriberSOCs=null;
		boolean bansAccountType = false;
		if (Optional.ofNullable(wirelessResponse).isPresent()
				&& Optional.ofNullable(wirelessResponse.getContent()).isPresent()) {

			contentList = wirelessResponse.getContent();
			for (Content content : contentList) {
				if (Optional.ofNullable(content.getStatus()).isPresent()
						&& !content.getStatus().equalsIgnoreCase(ACTIVE)) {
					throw new ClientException("", "", 0);
				}
				if (Optional.ofNullable(content.getType()).isPresent()
						&& content.getType().equalsIgnoreCase(ACC_TYPE)) {
					if (Optional.ofNullable(content.getCustomerType()).isPresent()
							&& (content.getCustomerType().toUpperCase().matches(CUSTOMER_TYPE))) {
						ClientException ce = new ClientException("", "", 0);
						ce.setSource(WIRELESS_CLIENT);
						throw ce;
					}
					bansAccountType = true;
					if (Optional.ofNullable(content.getSubscribers()).isPresent()) {
						for (Subscriber subscriber : content.getSubscribers()) {
							if (Optional.ofNullable(subscriber.getSubscriberStatus()).isPresent() && subscriber.getSubscriberStatus().equalsIgnoreCase(ACTIVE)
									&& Optional.ofNullable(subscriber.getAdditionalOfferings()).isPresent() && !subscriber.getAdditionalOfferings().isEmpty()) {
								activeSubsriberSOCs = socByGroupId.get(subscriber.getGroupId());
								if (!Optional.ofNullable(activeSubsriberSOCs).isPresent()) {
									activeSubsriberSOCs = new ArrayList<>();
								}
								for (AdditionalOffering additionalOffering : subscriber.getAdditionalOfferings()) {
									activeSubsriberSOCs.add(additionalOffering.getCode());
								}
								if (Optional.ofNullable(subscriber.getGroupId()).isPresent()) {
									socByGroupId.put(subscriber.getGroupId(), activeSubsriberSOCs);
								}
							}
						}
					}

					if (Optional.ofNullable(socByGroupId).isPresent() && !socByGroupId.isEmpty()) {
						customerAccountGroupIdMap.put(content.getAccountNumber(), socByGroupId);
					}

				}
			}
			// With wireless account(s), If accountType is not "I" return below error
			if (!bansAccountType) {
				ClientException ce = new ClientException("", "", 0);
				ce.setSource(WIRELESS_CLIENT);
				throw ce;
			}

		}
		log.debug("End of DtvnOffersProcessor.processWirelessResponse() method..");
		return customerAccountGroupIdMap;
	}

	/**
	 * This method will match the customer's soc code with the mobility soc codes
	 * and will retrieve the qualified mobility offer.
	 *
	 * @param wirelessRes            the wireless res
	 * @param offerResponse            the offer response
	 * @param mobilityBaseOffers            the mobility base offers
	 * @param mode            the mode
	 * @param sessionId            the session id
	 * @return the List<Offer>
	 * @throws ClientException the client exception
	 */
	@SuppressWarnings("unchecked")
	public List<CTOffer> retrieveQualifiedMobilityOffer(WirelessResponse wirelessRes, List<CTOffer> offerList,
			boolean mobilityBaseOffers, String mode, String sessionId) throws ClientException {

		log.debug("Start of DtvnOffersProcessor.retrieveQualifiedMobilityOffer() method..");
		List<CTOffer> qualifiedOffers = new ArrayList<>();
		List<CTOffer> rewardOffers = new ArrayList<>();
		List<CTOffer> offers = new ArrayList<>();
		Additonals additonals = new Additonals();
		EligibleWirelessAccount eligibleWirelessAccount = new EligibleWirelessAccount();
		Map<String, Map<String, List<String>>> customerAccountGroupIdMap;
		try {
			customerAccountGroupIdMap = getCustomerSOC(wirelessRes, mobilityBaseOffers);
		} catch (ClientException ce) {
			log.error(GOT_CLIENT_EXCEPTION, ce);
			ce.setSource(SUSPENDED_ACCOUNT);
			throw ce;
		}
		AtomicInteger disQualifyCount = new AtomicInteger(0);

		// Checking Customer SOC
		if (Optional.ofNullable(customerAccountGroupIdMap).isPresent() && !customerAccountGroupIdMap.isEmpty()
				&& Optional.ofNullable(offerList).isPresent())
				 {
			for (Map.Entry<String, Map<String, List<String>>> entry : customerAccountGroupIdMap.entrySet()) {
				Map<String, List<String>> customerSOCGroupMap = entry.getValue();
				for (Map.Entry<String, List<String>> entry1 : customerSOCGroupMap.entrySet()) {
					List<String> customerSOCList = entry1.getValue();
					customerSOCList.forEach(custSoc -> {
						offerList.forEach(offer -> {

							List<String> trimmedEligibleSocList=new ArrayList<>();
							List<String> trimmedDisqualSocList=new ArrayList<>();
							List<String> unMatchedSocList=new ArrayList<>();
						    if("reward".equalsIgnoreCase(offer.getAttributes().getOfferType())){
						    	rewardOffers.add(offer);
						    }
							if(offer.getAttributes().getEligibleSOCs()!= null){
							 trimmedEligibleSocList = offer.getAttributes().getEligibleSOCs().stream()

									.filter(Objects::nonNull).map(String::trim).collect(Collectors.toList());
							}
							if(offer.getAttributes().getDisqualifyingSOCs()!= null){
							 trimmedDisqualSocList = offer.getAttributes().getDisqualifyingSOCs().stream()
									.filter(Objects::nonNull).map(String::trim).collect(Collectors.toList());
							}
							List<String> matchedSocList = trimmedEligibleSocList.stream().filter(custSoc::contains)
									.collect(toList());
							 unMatchedSocList = trimmedDisqualSocList.stream().filter(custSoc::contains)
									.collect(toList());
							if (!unMatchedSocList.isEmpty()) {
								disQualifyCount.getAndIncrement();
							}
							if (!matchedSocList.isEmpty() && unMatchedSocList.isEmpty()) {
								offers.add(offer);
								qualifiedOffers.addAll(offers);
							}
						});
					});

					if (disQualifyCount.get() > 0 && mobilityBaseOffers) {
						qualifiedOffers.clear();
						break;
					}
					if ((disQualifyCount.get() > 0 && !mobilityBaseOffers)
							|| (qualifiedOffers.isEmpty() && !mobilityBaseOffers)) {
						throw new ServiceException(ErrorMessages.CTLG_DTVN_CUSTOMER_NOT_ELIGIBLE)
								.addDetail(ErrorMessages.CTLG_DTVN_CUSTOMER_NOT_ELIGIBLE_DETAILS001);
					}
					if (Optional.ofNullable(qualifiedOffers).isPresent() && !qualifiedOffers.isEmpty()) {
						wirelessRes.getContent().forEach(content -> {
							String accNum = content.getAccountNumber();
							if (accNum.equalsIgnoreCase(entry.getKey())) {
								eligibleWirelessAccount.setAccountNumber(entry.getKey());
								content.getSubscribers().forEach(subscriber -> {
									if (subscriber.getGroupId() != null
											&& subscriber.getGroupId().equalsIgnoreCase(entry1.getKey())
											&& subscriber.isPrimary()) {
										eligibleWirelessAccount.setGroupId(subscriber.getGroupId());
										eligibleWirelessAccount.setSubscriberNumber(subscriber.getSubscriberNumber());
										eligibleWirelessAccount.setAccountSocList(customerAccountGroupIdMap);
										sendMobilityDetailsToDtvNowClient(eligibleWirelessAccount.getAccountNumber(),eligibleWirelessAccount.getSubscriberNumber());
									}
								});
							}
						});

						if (Optional.ofNullable(sessionId).isPresent() && !sessionId.isEmpty()
								&& Optional.ofNullable(eligibleWirelessAccount.getAccountNumber()).isPresent()) {
							// Storing session data in remote cache
							saveAccountInfoToCache(sessionId, eligibleWirelessAccount);
						}

						// Implementation for Catalog to return the qualifier BAN in response for cart
						// mode.
						if (Optional.ofNullable(mode).isPresent() && !mode.isEmpty()) {

							additonals.setEligibleWirelessAccount(eligibleWirelessAccount);
							qualifiedOffers.get(0).setAdditionals(additonals);
						}
					}
				}
				if (!CollectionUtils.isEmpty(qualifiedOffers)) {
					break;
				}
			}
		}
		if (!CollectionUtils.isEmpty(qualifiedOffers)) {
			qualifiedOffers.addAll(rewardOffers);
		}
		Set<CTOffer> distinctOffers = new LinkedHashSet<>(qualifiedOffers);
		log.debug("End of DtvnOffersProcessor.retrieveQualifiedMobilityOffer() method..");
		return new ArrayList<>(distinctOffers);

	}


	/**
	 * Send mobility details to dtv now client.
	 *
	 * @param accountNumber the account number
	 * @param primarySubscriberNumber the primary subscriber number
	 */
	private void sendMobilityDetailsToDtvNowClient(String accountNumber, String primarySubscriberNumber) {
		try {
			dtvnSubscriptionClient.postMobilityDetailsAsyn(accountNumber, primarySubscriberNumber).subscribeOn(Schedulers.io()).subscribe();
		}catch(Exception e) {
			log.error("DtvnOffersProcessor.sendMobilityDetailsToDtvNowClient-Error"+e.getMessage(), e);
		}
	}


	/**
	 * Save account info to cache.
	 *
	 * @param sessionId
	 *            the session id
	 * @param eligibleWirelessAccount
	 *            the eligible wireless account
	 */
	private void saveAccountInfoToCache(String sessionId, EligibleWirelessAccount eligibleWirelessAccount) {

		try {
			String key = this.getKey(DTVNEW + sessionId);
			CacheEntry cacheEntry = new CacheEntry(OBJECT_KEY, eligibleWirelessAccount, "v1", CACHE_NAME);
			cacheService.putInCache(key, cacheEntry.getPayLoad());
			log.debug("Session payload stored in distributed cache");
		} catch (Exception e) {
			log.error("Got Exception while storing account number from session payload : {} ", e);
		}
	}

	/**
	 * Retrieve qualified offer.
	 *
	 * @param offerId
	 *            the offer id
	 * @param loggedInId
	 *            the logged in id
	 * @param accountNumbers
	 *            the account numbers
	 * @param mobilityBaseOffers
	 *            the mobility base offers
	 * @param mode
	 *            the mode
	 * @param sessionId
	 *            the session id
	 * @return the offer
	 * @throws ClientException
	 *             the client exception
	 */
	/*@SuppressWarnings({ "squid:S2259" })
	public Offer retrieveQualifiedOffer(String offerId, String loggedInId, String accountNumbers,
			boolean mobilityBaseOffers, String mode, String sessionId, BaseOfferRequestWrapper baseOfferRequestWrapper) throws ClientException {
		log.debug("Start of DtvnOffersProcessor.retrieveQualifiedOffer(offerServiceType) method..");

		Offer offer = null;
		List<Offer> offersList = null;
		try {
			offer = dtvnBaseOffersProcessor.getOfferIdResponse(offerId, baseOfferRequestWrapper);
			String isMobilityOffer = offer != null ? offer.getIsMobilityOffer() : null;
			if (Optional.ofNullable(isMobilityOffer).isPresent() && isMobilityOffer.equalsIgnoreCase(FALSE)) {

				//offer = JsonFilterService.filterAttributesAndGetObjectFromJson(offer, Offer.class, IGNORE_ATTRIBUTES);
			} else if (Optional.ofNullable(isMobilityOffer).isPresent()) {
				if (!Optional.ofNullable(loggedInId).isPresent() || loggedInId.isEmpty()) {
					throw new ServiceException(ErrorMessages.MISSING_DATA_FROM_REQUEST_HEADER).addDetail(ErrorMessages.MISSING_DATA_FROM_REQUEST_HEADER_DETAILS001);
				} else {
					offersList = retrieveOfferIdResponse(accountNumbers, offer, mobilityBaseOffers, mode, sessionId);
					if (Optional.ofNullable(offersList).isPresent() && !offersList.isEmpty()) {
						Offer qualifiedOffer = offersList.get(0);
					}
				}
			}

		} catch (ClientException ce) {
			throw ce;
		} catch (ServiceException se) {
			throw se;
		} catch (Exception ce) {
			log.error(GOT_EXCEPTION, ce);
			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR).addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS008);
		}

		log.debug("End of DtvnOffersProcessor.retrieveQualifiedOffer(offerServiceType) method..");
		return offer;
	}*/

	/**
	 * Gets the primary ctnfrom remote cache.
	 *
	 * @param sessionId
	 *            the session id
	 * @return the primary ctnfrom remote cache
	 */
	private EligibleWirelessAccount getPrimaryCtnfromRemoteCache(String sessionId, OfferRequestWrapper offerRequestWrapper) {

		EligibleWirelessAccount eligibleWirelessAccount = null;

		try {
			String key = this.getKey(DTVNEW + sessionId);
			String payload = cacheService.getFromCache(key);
			if (Optional.ofNullable(payload).isPresent()) {
				eligibleWirelessAccount = Optional.ofNullable(payload).isPresent() ? JsonService.getObjectFromJsonTree(
						payload, "value", EligibleWirelessAccount.class) : null;
			}
		} catch (Exception e) {
			log.error("Got Exception while getting account number from session payload : ", e);
            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
					&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
    			throw new ServiceException(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR)
    					.addDetail(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR_DETAILS007);
            } else {
    			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
				.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS007);
            }
		}
		return eligibleWirelessAccount;
	}

	/**
	 * Gets the key.
	 *
	 * @param primaryKey
	 *            the primary key
	 * @return the key
	 */
	public String getKey(String primaryKey) {
		return primaryKey+"_"+Constants.CACHE_NAME + "_" + Constants.OBJECT_KEY+"_"+Constants.CACHE_VERSION_ID+"_"+Constants.APPLICATION_ID ;
	}

	/**
	 * This method will match the customer's soc code with the mobility soc codes
	 * and will retrieve the qualified mobility offer.
	 *
	 * @param eligibleWirelessAccount the eligible wireless account
	 * @param offerResponse            the offer response
	 * @param multipleMobilityOffers            the mobility base offers
	 * @param mode            the mode
	 * @return the List<Offer>
	 */
	@SuppressWarnings("unchecked")
	public List<CTOffer> performMobilityOfferEligibilityRules(EligibleWirelessAccount eligibleWirelessAccount,
			List<CTOffer> offerList, boolean multipleMobilityOffers, String mode) {
		List<CTOffer> qualifiedOffers = new ArrayList<>();
		List<CTOffer> offers = new ArrayList<>();
		Additonals additonals = new Additonals();
		AtomicInteger disQualifyCount = new AtomicInteger(0);
		if (eligibleWirelessAccount != null
				&& Optional.ofNullable(eligibleWirelessAccount.getAccountSocList()).isPresent()
				&& !eligibleWirelessAccount.getAccountSocList().isEmpty()) {
			for (Map.Entry<String, Map<String, List<String>>> entry : eligibleWirelessAccount.getAccountSocList()
					.entrySet()) {
				Map<String, List<String>> customerSOCGroupMap = entry.getValue();
				for (Map.Entry<String, List<String>> entry1 : customerSOCGroupMap.entrySet()) {
					List<String> customerSOCList = entry1.getValue();
					customerSOCList.forEach(custSoc -> {
						offerList.forEach(offer -> {
							List<String> eligibleSocList=new ArrayList<>();
							List<String> disqualSocList=new ArrayList<>();
							List<String> unMatchedSocList=new ArrayList<>();

							if(offer.getAttributes().getEligibleSOCs()!= null){
							 eligibleSocList = offer.getAttributes().getEligibleSOCs();
							}

							List<String> matchedSocList = eligibleSocList.stream().filter(Objects::nonNull).map(String::trim).filter(custSoc::contains)
									.collect(toList());
							if(offer.getAttributes().getDisqualifyingSOCs() != null) {
							 unMatchedSocList = disqualSocList.stream().filter(Objects::nonNull).map(String::trim).filter(custSoc::contains)
									.collect(toList());
							}
							if (!unMatchedSocList.isEmpty()) {
								disQualifyCount.getAndIncrement();
							}
							if (!matchedSocList.isEmpty() && unMatchedSocList.isEmpty()) {
								offers.add(offer);
								qualifiedOffers.addAll(offers);
							}
						});
					});

					if (disQualifyCount.get() > 0 && multipleMobilityOffers) {
						qualifiedOffers.clear();
						break;
					} else if (disQualifyCount.get() <= 0 && !multipleMobilityOffers
							&& Optional.ofNullable(mode).isPresent() && !mode.isEmpty()) {
						additonals.setEligibleWirelessAccount(eligibleWirelessAccount);
						if (!CollectionUtils.isEmpty(qualifiedOffers)) {
							qualifiedOffers.get(0).setAdditionals(additonals);
						}
					}
					if ((disQualifyCount.get() > 0 && !multipleMobilityOffers)
							|| (qualifiedOffers.isEmpty() && !multipleMobilityOffers)) {
						throw new ServiceException(ErrorMessages.CTLG_DTVN_CUSTOMER_NOT_ELIGIBLE)
								.addDetail(ErrorMessages.CTLG_DTVN_CUSTOMER_NOT_ELIGIBLE_DETAILS001);
					}
				}
				if (!CollectionUtils.isEmpty(qualifiedOffers)) {
					break;
				}
			}
		}
		Set<CTOffer> distinctOffers = new LinkedHashSet<>(qualifiedOffers);
		return new ArrayList<>(distinctOffers);
	}


}
