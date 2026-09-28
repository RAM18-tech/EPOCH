package com.dtv.dcp.epoch.processor.ott.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import com.dtv.dcp.epoch.model.ct.product.Variant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.AdditionalDetails;
import com.dtv.dcp.epoch.model.common.request.BenefitsCustomerHasReceived;
import com.dtv.dcp.epoch.model.common.request.CustomerPromotion;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.BenefitCodesToSuppressTheOffer;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGServiceInfo;
import com.dtv.dcp.epoch.processor.helper.CPOPBenefitsHelper;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.processor.ott.services.model.CustomerServiceDetail;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphServiceImpl;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class OttServicesOffersProcessor {

	/** The CpopUCCClientHelper */
	@Autowired
	private CpopUCCClientHelper cpopUCCClientHelper;

	@Autowired
	CustomerGraphService customerGraphService;

	@Autowired
	CPOPProductsHelper cpopProductsHelper;

	@Autowired
	CpopClientHelper cpopClientHelper;

	@Autowired
	@Qualifier("CustomerGraph")
	CustomerGraphServiceImpl customerGraphServiceImpl;

	@Autowired
	OttAvaialbleOffersProcessor ottAvaialbleOffersProcessor;

	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;

	@Autowired
	OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;

	@Autowired
	CPOPBenefitsHelper cpopBenefitsHelper;

	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	@Autowired
	OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;

	/** The iptvMigrationEnabled. */
	@Value("${page}")
	private int page;
	/** The pageLimit. */
	@Value("${pageLimit}")
	private int pageLimit;
	/** The state. */
	@Value("${apiclient.rest.cpopofferms.ctstate}")
	private String ctstate;

	@Autowired
	OffersUtils offersUtils;

	@Autowired
	private RedisCacheHelper redisCacheHelper;

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(OttServicesOffersProcessor.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final String OTTSERVICESOFFERSPROCESSOR_GETOFFERS = "OttServicesOffersProcessor getOffers -> : %s";

	/**
	 *
	 * @param offerRequestWrapper
	 * @return
	 * @throws ServiceException
	 */
	public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {

		CGResponse cgResponse = null;
		CTOfferResponse ctOfferResponse = null;

		try {

			cgResponse = new CGResponse();
			OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
			CTOfferRequest ctOfferRequest = new CTOfferRequest();

			if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
				BeanUtils.copyProperties(offerRequestWrapper.getCtOfferRequest(), ctOfferRequest);
			}
			offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
			String requestCustomerSubType = null;
			if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getCustomerSubType()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getCustomerSubType().isEmpty()){
				requestCustomerSubType = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getCustomerSubType();
			}
			List<String> partnerCatalogExclusiveSalesChannels = new ArrayList<>(Arrays.asList(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_EXCLUSIVE_SALES_CHANNEL, Constants.OTT).get(0).split("\\s*,\\s*")));
			List<String> partnerCatalogExclusiveIapPartnerType = new ArrayList<>(Arrays.asList(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_IAP_PARTNER_TYPE, Constants.OTT).get(0).split("\\s*,\\s*")));

			if (Objects.nonNull(OffersUtils.getIapPartnerAccountType(offerRequestWrapper.getOfferRequest()))
					&& (partnerCatalogExclusiveIapPartnerType.contains(OffersUtils.getIapPartnerAccountType(offerRequestWrapper.getOfferRequest()))
					&& !partnerCatalogExclusiveSalesChannels.stream().anyMatch(offerRequestWrapper.getOfferRequest().getSalesChannel()::contains))
			) {
				log.info("EPOCH_GETOFFERS_SERVICESFLOW_FAILED_INVALIDiapPartnerAccountType ");
				ctOfferResponse = new CTOfferResponse();
				ctOfferResponse.setOffers(new ArrayList<>());
				return ctOfferResponse;
			}

			/*
			 * Check if DecisionFlow Flag is passed in Request. If yes then Call Retention
			 * and all other Offer Action Types in this section
			 */
			if (offerRequestWrapper.getOfferRequest().isDecisioningFlow()) {
				long startDecisioningTimeInMillis = System.currentTimeMillis();
				ctOfferRequest.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
				ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType().stream()
						.filter(a -> a != null).collect(Collectors.toList()));
				ctOfferRequest.setOfferProductType(offerRequestWrapper.getOfferRequest().getOfferProductType().stream()
						.filter(a -> a != null).collect(Collectors.toList()));

				if (!validateRequestForService(offerRequestWrapper).isEmpty()) {
					log.info("EPOCH_GETOFFERS_SERVICESFLOW_FAILED_INVALIDREQUEST");
					throw new ServiceException(ErrorMessages.INVALID_REQUEST).addDetail(
							ErrorMessages.INVALID_REQUEST_FIELDS_MISSING,
							String.join(",", validateRequestForService(offerRequestWrapper)));
				}
				long startNoRetTimeInMillis = System.currentTimeMillis();
				boolean removedRetention = false;
				if (ctOfferRequest.getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE)
					&& ctOfferRequest.getOfferProductType() != null
						&& (ctOfferRequest.getOfferProductType().contains(Constants.VIDEO_PLAN)
						|| ctOfferRequest.getOfferProductType().contains(Constants.VIDEO_DEVICE))) {
					ctOfferRequest.getOfferActionType().remove(Constants.RETENTION_ACTION_TYPE);
					removedRetention = true;
				}
				if (!ctOfferRequest.getOfferActionType().isEmpty()) {
					ctOfferResponse = ottAvaialbleOffersProcessor.getAvailableCTOffers(offerRequestWrapper);
					cpopBenefitsHelper.filterInvalidBenefits(ctOfferResponse, offerRequestWrapper);
				}
				log.info("EPOCH_OFFER_DECISIONING_NO_RETENTION_EXECUTION_TIME-[{}]",
						(System.currentTimeMillis() - startNoRetTimeInMillis));
				//filter the offers based on CustomerSubType
				offersUtils.filterOffersBasedOnCustomerSubType(ctOfferResponse, requestCustomerSubType);

				//filter the offers based on the ineligible accountStatus as freeTrail
				filterFreeTrailOffers(offerRequestWrapper, ctOfferResponse);
				CTOfferResponse ctRetentionOfferResponse = null;

				if (removedRetention) {
					long startRetTimeInMillis = System.currentTimeMillis();
					ctOfferRequest.setOfferActionType(Arrays.asList(Constants.RETENTION_ACTION_TYPE));
					commonMethodForROFFlowAndRetention(offerRequestWrapper, ctOfferRequest, offerRequest, cgResponse);
                    ottCTOffersProcessor.setSubscriptionTypeAndFlowIntents(offerRequestWrapper, ctOfferRequest);
					ctRetentionOfferResponse = this.getOffersFromSource(ctOfferRequest);
					 // SLS-IXP-FLAG changes
                    if (offersUtils.isSlsGetOfferServicesEnabled()
                            && ctOfferResponse != null && CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())
                            && offerRequestWrapper.getSlsCombinationMap() != null
                            && null != offerRequestWrapper.getOfferRequest().getContractIndicator()
                            && offersUtils.isUniversalCohort(offerRequestWrapper.getOfferRequest().getContractIndicator())
                            && offersUtils.isService(offerRequestWrapper.getOfferRequest())) {
                        ctOfferResponse = offersUtils.filterProductsOnAccountToSuppressSLSOffer(offerRequestWrapper, ctOfferRequest, ctOfferResponse);
                    }
					//filter the offers based on CustomerSubType
					offersUtils.filterOffersBasedOnCustomerSubType(ctRetentionOfferResponse, requestCustomerSubType);
					offersUtils.filterOffersBasedOnEligibleIapPartners(ctOfferResponse, offerRequestWrapper);
					ctRetentionOfferResponse = applyFilters(cgResponse, ctRetentionOfferResponse, offerRequestWrapper);
					//filter the offer based on BAN lookup table, targeted offer in retention flow
					ottAvaialbleOffersProcessor.filterOffersBasedOnBANLookup(offerRequestWrapper, ctRetentionOfferResponse);
					if (ctRetentionOfferResponse != null && (ctRetentionOfferResponse.getOffers() != null
							&& !ctRetentionOfferResponse.getOffers().isEmpty())) {
						calculateOfferPriceIXPFlagEnabled(offerRequestWrapper, ctRetentionOfferResponse);
						if (ctOfferResponse != null) {
							ctOfferResponse.getOffers().addAll(ctRetentionOfferResponse.getOffers());
							ctOfferResponse.setCount(ctOfferResponse.getCount() + ctRetentionOfferResponse.getCount());
							ctOfferResponse.setTotal(ctOfferResponse.getTotal() + ctRetentionOfferResponse.getTotal());
							//logic to filter offers based on qualifying products with pre or post effective date
							if(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_TRUTV_TCM_ANE_ENABLED)) {
								ottServicesOffersProcessorHelper.filterOffersBasedOnQualifyingProducts(ctOfferResponse, offerRequestWrapper);
							}
						}else {
							//logic to filter offers based on qualifying products with pre or post effective date
							if(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_TRUTV_TCM_ANE_ENABLED)) {
								ottServicesOffersProcessorHelper.filterOffersBasedOnQualifyingProducts(ctOfferResponse, offerRequestWrapper);
							}
							return ctOfferResponse;
						}
					}
					log.info("EPOCH_OFFER_DECISIONING_RETENTION_EXECUTION_TIME-[{}]",
							(System.currentTimeMillis() - startRetTimeInMillis));
				}
				if (Objects.nonNull(ctOfferResponse) && CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())){
					OffersUtils.removeServiceSubscriptionTypeDEFAULT(ctOfferResponse.getOffers());
				}
				log.info("EPOCH_OFFER_DECISIONING_EXECUTION_TIME-[{}]", (System.currentTimeMillis() - startDecisioningTimeInMillis));
			}

			// For Offered Promotions -> RETENTION
			else if (offerRequestWrapper.getOfferRequest().getOfferActionType()
					.contains(Constants.RETENTION_ACTION_TYPE)) {
				long startRetTimeInMillis = System.currentTimeMillis();
				ctOfferRequest.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
				ctOfferRequest.setOfferActionType(Arrays.asList(Constants.RETENTION_ACTION_TYPE));

				commonMethodForROFFlowAndRetention(offerRequestWrapper, ctOfferRequest, offerRequest, cgResponse);

				ctOfferResponse = this.getOffersFromSource(ctOfferRequest);
				//filter the offers based on CustomerSubType
				offersUtils.filterOffersBasedOnCustomerSubType(ctOfferResponse, requestCustomerSubType);
				offersUtils.filterOffersBasedOnEligibleIapPartners(ctOfferResponse, offerRequestWrapper);
				//filter the offers based on the ineligible accountStatus as freeTrail
				filterFreeTrailOffers(offerRequestWrapper, ctOfferResponse);
				//filter offers based on Qualifying products in retention
		        ctOfferResponse = filterRetentionQualifyingProducts(ctOfferResponse, offerRequestWrapper);
				ctOfferResponse = applyFilters(cgResponse, ctOfferResponse, offerRequestWrapper);
				//filter the offer based on BAN lookup table, targeted offer in retention flow
				ottAvaialbleOffersProcessor.filterOffersBasedOnBANLookup(offerRequestWrapper, ctOfferResponse);
				if (ctOfferResponse != null
						&& (ctOfferResponse.getOffers() != null && !ctOfferResponse.getOffers().isEmpty())) {
					calculateOfferPriceIXPFlagEnabled(offerRequestWrapper, ctOfferResponse);
					OffersUtils.removeServiceSubscriptionTypeDEFAULT(ctOfferResponse.getOffers());
				}
				//logic to filter offers based on qualifying products with pre or post effective date
				if(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_TRUTV_TCM_ANE_ENABLED)) {
					ottServicesOffersProcessorHelper.filterOffersBasedOnQualifyingProducts(ctOfferResponse, offerRequestWrapper);
				}
				log.info("EPOCH_OFFER_ONLY_RETENTION_EXECUTION_TIME-[{}]", (System.currentTimeMillis() - startRetTimeInMillis));
				// For Available Packages, addon, devices & fees -> OTHER
			} else if (offersUtils.isService(offerRequest)) {

				if (!validateRequestForService(offerRequestWrapper).isEmpty()) {
					log.info("EPOCH_GETOFFERS_SERVICESFLOW_FAILED_INVALIDREQUEST");
					throw new ServiceException(ErrorMessages.INVALID_REQUEST).addDetail(
							ErrorMessages.INVALID_REQUEST_FIELDS_MISSING,
							String.join(",", validateRequestForService(offerRequestWrapper)));
				}
				long startTimeInMillis = System.currentTimeMillis();
				ctOfferResponse = ottAvaialbleOffersProcessor.getAvailableCTOffers(offerRequestWrapper);
				//filter the offers based on CustomerSubType
				offersUtils.filterOffersBasedOnCustomerSubType(ctOfferResponse, requestCustomerSubType);

				//filter the offers based on the ineligible accountStatus as freeTrail
				filterFreeTrailOffers(offerRequestWrapper, ctOfferResponse);
				//logic to filter offers based on qualifying products with pre or post effective date
				if(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_TRUTV_TCM_ANE_ENABLED)) {
					ottServicesOffersProcessorHelper.filterOffersBasedOnQualifyingProducts(ctOfferResponse, offerRequestWrapper);
				}
				if (Objects.nonNull(ctOfferResponse) && CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())){
					OffersUtils.removeServiceSubscriptionTypeDEFAULT(ctOfferResponse.getOffers());
				}
				supressDependentPromoAttributes(ctOfferResponse);
				log.info("EPOCH_AVAILABLE_OFFERS_SERVICES_EXECUTION_TIME-[{}]", (System.currentTimeMillis() - startTimeInMillis));
				return cpopBenefitsHelper.filterInvalidBenefits(ctOfferResponse, offerRequestWrapper);
			}

		} catch (ServiceException e) {
			log.debug(String.format(OTTSERVICESOFFERSPROCESSOR_GETOFFERS, e.getError()));
			log.error(String.format(OTTSERVICESOFFERSPROCESSOR_GETOFFERS, e.getError()));
			throw e;
		} catch (Exception ex) {
			log.error(String.format(OTTSERVICESOFFERSPROCESSOR_GETOFFERS, ex.getMessage()));
			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
					&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream()
							.allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR)
						.addDetail(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
			} else {
				throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
						.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
			}
		}
		return ctOfferResponse;
	}

	private void calculateOfferPriceIXPFlagEnabled(OfferRequestWrapper offerRequestWrapper,
			CTOfferResponse ctOfferResponse) {
//		if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_IXP_OFFERPRICE_ENABLED)) {
//			if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream().anyMatch(
//					s -> Constants.OEM_IAPFIRETV.equalsIgnoreCase(s) || Constants.OEM_IAPROKUTV.equalsIgnoreCase(s))) {
//				log.info("Get offers Based on the IXP flag is TRUE and sales channel is Fire TV or Roku TV");
//				OffersUtils.calculateBestPrice(offerRequestWrapper, ctOfferResponse.getOffers());
//			}
//		} else {
		log.info("Get offers Based on the IXP flag is FALSE Which returns new Offer prices");
		offersUtils.calculateBestPrice(offerRequestWrapper, ctOfferResponse.getOffers());
//		}
	}

	private List<String> validateRequestForService(OfferRequestWrapper offerRequestWrapper) {
		List<String> listMissingField = new ArrayList<>();
		OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
		if (offerRequest.getOfferProductType() == null && offerRequest.getCustomerContext() != null
				&& (offerRequest.getCustomerContext().getOtt().getProducts() == null
						|| offerRequest.getCustomerContext().getOtt().getProducts().isEmpty())) {
			listMissingField.add("OfferProductType");
		}
		if (offerRequest.getOfferActionType() == null) {
			listMissingField.add("OfferActionType");
		}
		if (offerRequest.getOfferProductFamily() == null) {
			listMissingField.add("OfferProductFamily");
		}
		if (offerRequest.getSalesChannel() == null) {
			listMissingField.add("SalesChannel");
		}
		return listMissingField;
	}

	/**
	 *
	 * @param ctOfferRequest
	 * @return
	 * @throws ServiceException
	 */
	private CTOfferResponse getOffersFromSource(CTOfferRequest ctOfferRequest) throws ServiceException {
		CTOfferResponse offerResponse = null;

		ctOfferRequest.setState(ctstate);
		Pagination pagination = new Pagination();
		pagination.setPage(page);
		pagination.setLimit(pageLimit);
		ctOfferRequest.setPagination(pagination);
		String json = null;
		try {
				json = MAPPER.writeValueAsString(ctOfferRequest);
			offerResponse = cpopClientHelper.getOffers(ctOfferRequest);
		} catch (Exception e) {
			log.debug(String.format("getOffersFromSource::: %s", json));
			log.debug(String.format("getOffersFromSource::: %s", e.getMessage()));
			if (Objects.nonNull(ctOfferRequest.getOfferProductFamily()) && ctOfferRequest.getOfferProductFamily()
					.stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CT_ERROR).addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);
			} else {
				throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		}
		if (offerResponse == null) {
			log.info(
					"In Method getOffersFromSource.OttServicesOffersProcessor() :: Getting  null response from the CT");
			if (Objects.nonNull(ctOfferRequest.getOfferProductFamily()) && ctOfferRequest.getOfferProductFamily()
					.stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CT_ERROR).addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);
			} else {
				throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		} 
		log.info("In Method getOffersFromSource.OttServicesOffersProcessor() :: Getting response from the CT");
		return offerResponse;
	}

	/**
	 *
	 * @param salesChannel
	 * @return
	 */

	/**
	 * Applying Filters -> Filtering Eligible offers as per Customer Graph;
	 * filtering by Mobility, Residential, Employee; filtering by heartScore ; and
	 * filter by expired Prices
	 * 
	 * @param cgResponse
	 * @param ctOfferResponse
	 * @param offerRequestWrapper
	 * @return
	 * @throws ServiceException
	 */
	public CTOfferResponse applyFilters(CGResponse cgResponse, CTOfferResponse ctOfferResponse,
			OfferRequestWrapper offerRequestWrapper) throws ServiceException {
		try {
			List<String> listOfServiceId = new ArrayList<>();
			CustomerSubscriptionDetail customerSubscriptionDetail = null;

			String incomingSalesChannel = offerRequestWrapper.getOfferRequest().getSalesChannel().get(0);

			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
					&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent()
					&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())
							.isPresent()
					&& Optional
							.ofNullable(
									offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts())
							.isPresent()) {
				long startGetStackabilityInMillis = System.currentTimeMillis();
				customerSubscriptionDetail = getListBaseBackageServiceDetail(offerRequestWrapper);
				if (!customerSubscriptionDetail.getListBaseBackageServiceDetail().isEmpty()
						&& Optional.ofNullable(ctOfferResponse).isPresent()
						&& Optional.ofNullable(ctOfferResponse.getOffers()).isPresent()) {
					ctOfferResponse = applyStackabilityRule(ctOfferResponse, customerSubscriptionDetail,
							offerRequestWrapper);
				}
				log.info("EPOCH_APPLY_STACKABILITY_RULE_EXECUTION_TIME-[{}]",
						(System.currentTimeMillis() - startGetStackabilityInMillis));
			}
			
			if(Objects.nonNull(offerRequestWrapper.getOfferRequest()) && Objects.nonNull(offerRequestWrapper.getOfferRequest().isDecisioningFlow())
					&& offerRequestWrapper.getOfferRequest().isDecisioningFlow()== false) {
				ctOfferResponse.setOffers(ctOfferResponse.getOffers().stream().filter(ctOffer -> Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes())
																		&& Objects.nonNull(ctOffer.getAttributes().getRetentionOfferSubType())
																		&& ctOffer.getAttributes().getRetentionOfferSubType().contains(Constants.RETENTION_OFFER_SUBTYPE_PREMIUM))
															.collect(Collectors.toList()));
			}

			ctOfferResponse = cpopBenefitsHelper.filterOffersBySalesChannelOfBenefits(ctOfferResponse,
					offerRequestWrapper);

			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent() && Optional
					.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()) {

				String accountType = "";
				if (offerRequestWrapper.isMobility()) {
					accountType = Constants.MOBILITY_WITH_CAPITAL_M;
				} else {
					String customerAccountType = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()
							.getAccountType();
					if (StringUtils.isNotBlank(customerAccountType)) {
						accountType = customerAccountType;
					} else {
						accountType = Constants.RESIDENTIAL_ACCOUNT_TYPE;
					}
				}

				Boolean RetentionPromoIndicator = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()
						.getRetentionPromotionIndicator();
				String coolOffPeriod = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()
						.getCoolOffPeriod();

				// Getting the list from eligible products
				if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())
						.isPresent()) {
					for (ProductInfo productInfo : offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()
							.getProducts()) {
						listOfServiceId.add(productInfo.getProductCode());
					}
				}

				// Search for premium save offers
//				List<CTOffer> listOfPremiumSaveOffers = new ArrayList<>();
//				if (offerRequestWrapper.getOfferRequest().getContractIndicator().contains(Constants.EDSP_STRING))
//				{
//					if(ctOfferResponse !=null && ctOfferResponse.getOffers() !=null) {
//		                ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
//		                	if(ctOffer.getAttributes().getRetentionOfferSubType()!= null && 
//		                			(ctOffer.getAttributes().getRetentionOfferSubType().contains(Constants.RETENTION_OFFER_SUBTYPE_PREMIUM)
//		                					|| ctOffer.getAttributes().getRetentionOfferSubType().contains(Constants.RETENTION_OFFER_SUBTYPE_PROGRAMMING_DISPUTE)))
//		                	{
//		                		listOfPremiumSaveOffers.add(ctOffer);
//		                	}
//		                });
//	                }
//				}
//				else if (offerRequestWrapper.getOfferRequest().getContractIndicator().contains(Constants.CONTRACT)){
//					if(ctOfferResponse !=null && ctOfferResponse.getOffers() !=null) {
//		                ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
//		                	if(ctOffer.getAttributes().getRetentionOfferSubType()!= null && 
//		                		 ctOffer.getAttributes().getRetentionOfferSubType().contains(Constants.RETENTION_OFFER_SUBTYPE_PROGRAMMING_DISPUTE))
//		                	{
//		                		listOfPremiumSaveOffers.add(ctOffer);
//		                	}
//		                });
//	                }
//				}
//				
//				//Remove the PremiumSaveOffers
//				for (CTOffer offer: listOfPremiumSaveOffers) {
//					if(ctOfferResponse.getOffers().contains(offer))
//					{
//						ctOfferResponse.getOffers().remove(offer);
//					}
//				}
//				
//				// Filtering By HeartScore
//				if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getSegmentDescription()).isPresent()) {
//					List<String> listOfHeartScore = Arrays.asList(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getSegmentDescription());
//					ctOfferResponse = cpopProductsHelper.filterOffersByHeartValue(ctOfferResponse, listOfHeartScore, RetentionPromoIndicator, coolOffPeriod, incomingSalesChannel, offerRequestWrapper.getOfferRequest().getContractIndicator().get(0));
//				} else {
//					ctOfferResponse = cpopProductsHelper.filterOffersByHeartValue(ctOfferResponse, new ArrayList<>(), RetentionPromoIndicator, coolOffPeriod, incomingSalesChannel, offerRequestWrapper.getOfferRequest().getContractIndicator().get(0));
//				}
//				
//				//Add back the PremiumSaveOffers for further filtering
//				for (CTOffer offer: listOfPremiumSaveOffers) {
//					if(!ctOfferResponse.getOffers().contains(offer))
//					{
//						ctOfferResponse.getOffers().add(offer);
//					}
//				}

				// Filtering By AccountType
				if (Optional
						.ofNullable(
								offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getAccountType())
						.isPresent()) {
					// Account Type: Mobility, Residential or Employee need to come from the request
					ctOfferResponse = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, accountType);
				}

			} else if ((StringUtils.isNotBlank(offerRequestWrapper.getDtvnAccount()))) {

				// Getting the list from eligible products
				CGServiceInfo[] cgServiceInfos = cgResponse.getServiceInfo();
				if (cgServiceInfos != null) {
					for (CGServiceInfo serviceInfo : cgServiceInfos) {
						listOfServiceId.add(serviceInfo.getServiceID());
					}
				}

				// Filtering By HeartScore
//				if (Optional.ofNullable(cgResponse).isPresent() && Optional.ofNullable(cgResponse.getContactInfo()).isPresent() &&
//						Optional.ofNullable(cgResponse.getContactInfo().getSegmentInfo()).isPresent()) {
//					List<String> listOfHeartScore = cgResponse.getContactInfo().getSegmentInfo().stream()
//							.map(CGSegmentInfo::getDescription)
//							.collect(Collectors.toList());
//					ctOfferResponse = cpopProductsHelper.filterOffersByHeartValue(ctOfferResponse, listOfHeartScore, false, "", incomingSalesChannel, offerRequestWrapper.getOfferRequest().getContractIndicator().get(0));
//				} else {
//					ctOfferResponse = cpopProductsHelper.filterOffersByHeartValue(ctOfferResponse, new ArrayList<>(), false, "", incomingSalesChannel, offerRequestWrapper.getOfferRequest().getContractIndicator().get(0));
//				}
				// Filtering By AccountType
				if (Optional.ofNullable(cgResponse).isPresent()
						&& Optional.ofNullable(cgResponse.getContactInfo()).isPresent()
						&& Optional.ofNullable(cgResponse.getContactInfo().getSegmentInfo()).isPresent()) {
					ctOfferResponse = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse,
							cgResponse.getAccountInfo().getAccountType());
				}
			}

			// Filtering by productCodes gotten from Customer Graph or customerContext
			if (ctOfferResponse != null && ctOfferResponse.getOffers() != null) {
				ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
					if (Objects.nonNull(ctOffer.getAttributes())
							&& ctOffer.getAttributes().getAssociatedProducts() != null) {
						ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
								.forEach(associatedProduct -> {
									if (Objects.nonNull(associatedProduct)
											&& CollectionUtils.isNotEmpty(associatedProduct.getQualifyingProducts())) {
										associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull)
												.forEach(qualifyingProduct -> {
													if (qualifyingProduct.getProducts() != null)
														qualifyingProduct.setProducts(cpopProductsHelper
																.filterByProducts(qualifyingProduct.getProducts(),
																		listOfServiceId));
												});
									}

								}

								);
					}
				});
			}

			// Filtering Prices
			ctOfferResponse = cpopProductsHelper.filterInvalidPrices(ctOfferResponse, offerRequestWrapper);
			ctOfferResponse = cpopBenefitsHelper.filterInvalidBenefits(ctOfferResponse, offerRequestWrapper);

			// Filtering Benefits for applicable products gotten from Customer Graph or
			// customerContext
			List<CTOffer> listOfOffers = new ArrayList<>();
			if (ctOfferResponse != null && ctOfferResponse.getOffers() != null) {
				ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
					AtomicBoolean offerAdded = new AtomicBoolean(false);
					if (ctOffer.getAttributes() != null && CollectionUtils.isEmpty(ctOffer.getAttributes().getBenefits())) {
						listOfOffers.add(ctOffer);
						offerAdded.set(true);
					} else if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getBenefits() != null) {
						ctOffer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
							if (benefit.getApplicableProducts() != null && !benefit.getApplicableProducts().isEmpty()) {
								benefit.getApplicableProducts().stream().forEach(productWrapper -> {
									if (!offerAdded.get() && Objects.nonNull(productWrapper.getProducts())
											&& CollectionUtils.isNotEmpty(productWrapper.getProducts())) {
										List<String> list = productWrapper.getProducts().stream()
												.map(product -> product.getKey()).collect(Collectors.toList());
										if (CollectionUtils.containsAny(list, listOfServiceId)
												&& benefit.getStartDate() != null && benefit.getEndDate() != null
												&& OffersUtils.validateActiveDates(
														OffersUtils.getFormattedDate(benefit.getStartDate()),
														OffersUtils.getFormattedDate(benefit.getEndDate()))) {
											listOfOffers.add(ctOffer);
											offerAdded.set(true);
										} else if (CollectionUtils.containsAny(list, listOfServiceId)
												&& benefit.getStartDate() != null && benefit.getEndDate() == null
												&& OffersUtils.isDateActive(
														OffersUtils.getFormattedDate(benefit.getStartDate()))) {
											listOfOffers.add(ctOffer);
											offerAdded.set(true);
										}
									}

								});

							}
						});
					}
				});

				ctOfferResponse.setOffers(listOfOffers);

				if (listOfOffers != null) {
					ctOfferResponse.setCount(listOfOffers.size());
					ctOfferResponse.setTotal(listOfOffers.size());
				}
			}

		} catch (Exception ex) {
			log.error(String.format("applyFilters : %s", ex.getMessage()));
			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
					&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream()
							.allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR)
						.addDetail(ErrorMessages.OTT_CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
			} else {
				throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
						.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
			}
		}
		return ctOfferResponse;
	}

	private CTOfferResponse filterRetentionQualifyingProducts(CTOfferResponse ctOfferResponse,
			OfferRequestWrapper offerRequestWrapper) {
		log.debug("filterRetentionQualifyingProducts method execution start");
		List<String> listOfServiceId = new ArrayList<>();
		if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent() &&
				Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()){
			for (ProductInfo productInfo : offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()
					.getProducts()) {
				listOfServiceId.add(productInfo.getProductCode());
			}
		}
		List<CTOffer> offers = new ArrayList<>();
		if (Objects.nonNull(ctOfferResponse)) {
			ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts())) {
					List<ProductWrapper> qualifyingProducts = offer.getAttributes().getAssociatedProducts().get(0)
							.getQualifyingProducts();
					if (Objects.nonNull(qualifyingProducts) && !qualifyingProducts.isEmpty()) {
						qualifyingProducts.stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
							List<Product> products = qualifyingProduct.getProducts();
							if (Objects.nonNull(products) && !products.isEmpty()) {
								products.stream().filter(Objects::nonNull).forEach(product -> {
									if (listOfServiceId.contains(product.getKey())) {
										offers.add(offer);
									}
								});
							} else {
								offers.add(offer);
							}
						});
					}
				} else {
					offers.add(offer);
				}

			});
			if (offers != null & offers.size() > 0) {
				ctOfferResponse.setOffers(offers);
			}
		}
		log.debug("filterRetentionQualifyingProducts method execution end");
		return ctOfferResponse;
	}

	private void commonMethodForROFFlowAndRetention(OfferRequestWrapper offerRequestWrapper,
			CTOfferRequest ctOfferRequest, OfferRequest offerRequest, CGResponse cgResponse) {
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
				.anyMatch(s -> s.equalsIgnoreCase(Constants.OPUS) || s.equalsIgnoreCase(Constants.DTV360) || s.equalsIgnoreCase(Constants.UVC))) {
			ctOfferRequest.setRetentionOfferUser(Arrays.asList(Constants.AGENT));

		} else if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
				.anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s))) {
			ctOfferRequest.setRetentionOfferUser(Arrays.asList(Constants.CUSTOMER));
		}

		if (Optional.ofNullable(offerRequest.getCustomerContext()).isPresent()
				&& Optional.ofNullable(offerRequest.getCustomerContext().getOtt()).isPresent()) {

			if (Optional.ofNullable(offerRequest.getCustomerContext().getOtt().getRetentionPromotionIndicator())
					.isPresent()
					&& Boolean.FALSE
							.equals(offerRequest.getCustomerContext().getOtt().getRetentionPromotionIndicator())) {
				ctOfferRequest.setOfferActionType(
						Arrays.asList(Constants.RETENTION_ACTION_TYPE, Constants.OTHER_ACTION_TYPE));
				ctOfferRequest.setContractIndicator(null);
			}

		} else if (offerRequestWrapper.getDtvnAccount() != null) {

			cgResponse = customerGraphServiceImpl.getActiveSubscriptions(offerRequestWrapper.getDtvnAccount(),
					featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP) ? "backup"
							: "primary");

			if (cgResponse.getAccountInfo().isIsPremiumCustomer()) {
				ctOfferRequest.setContractIndicator(Stream.of(Constants.CONTRACT).collect(Collectors.toList()));
			} else {
				ctOfferRequest.setContractIndicator(Stream.of(Constants.NONCONTRACT).collect(Collectors.toList()));
			}
		}
		if (ctOfferRequest.getContractIndicator() == null && offerRequest.getContractIndicator() != null) {
			ctOfferRequest.setContractIndicator(offerRequest.getContractIndicator());

		}
		// Set BenefitCodesToSuppressTheOffer in the Retention flow
		// Set BenefitCodesToSuppressTheOffer in the Retention flow
		if (Optional.ofNullable(offerRequest.getBenefitsCustomerHasReceived()).isPresent()) {
			List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived = new ArrayList<>();
			benefitsCustomerHasReceived = offerRequest.getBenefitsCustomerHasReceived();
			List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = new ArrayList<>();
			benefitCodesToSuppressTheOffer = ottCTOffersProcessor
					.convertToBenefitCodesToSuppressTheOffer(benefitsCustomerHasReceived);
			if (Optional.ofNullable(benefitCodesToSuppressTheOffer).isPresent()) {
				ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);
			}
		}
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",
				OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
	}

	private CustomerSubscriptionDetail getListBaseBackageServiceDetail(OfferRequestWrapper offerRequestWrapper) {

		CustomerSubscriptionDetail customerAccountDetail = new CustomerSubscriptionDetail();
		List<String> basePackageServiceIdList = new ArrayList<>();
		List<CustomerServiceDetail> listBaseBackageServiceDetail = new ArrayList<>();

		for (ProductInfo product : offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()) {
			if (Constants.VIDEO_PLAN.equals(product.getProductType())) {
				basePackageServiceIdList.add(product.getProductCode());
				CustomerServiceDetail customerServiceDetail = new CustomerServiceDetail();
				if (product.getPromotions() != null) {
					for (CustomerPromotion cPromo : product.getPromotions()) {
						Map<String, String> promoMap = new HashMap<>();

						promoMap.put(Constants.PROMO_START_DATE, cPromo.getPromotionStartDate());
						promoMap.put(Constants.PROMO_END_DATE, cPromo.getPromotionEndDate());
						customerServiceDetail.getAccountPromoStartEndDate().put(cPromo.getPromotionId(), promoMap);
					}
				}
				customerServiceDetail.setPromosId(customerServiceDetail.getAccountPromoStartEndDate().keySet().stream()
						.collect(Collectors.toList()));
				listBaseBackageServiceDetail.add(customerServiceDetail);
			}
		}
		customerAccountDetail.setListBaseBackageServiceDetail(listBaseBackageServiceDetail);
		log.info("END populateCustomerSubscriptionDetail");
		return customerAccountDetail;

	}

	public CTOfferResponse applyStackabilityRule(CTOfferResponse ctOfferResponse,
			CustomerSubscriptionDetail subscriptionsContext, OfferRequestWrapper offerRequestWrapper) {

		log.info("START getStackabilityGroupList ");

		Map<String, List<CTOffer>> sortedOffers = new HashMap<>();
		Map<String, List<String>> stackbilityValues = new HashMap<>();
		List<String> listOfBenefitCodes = new ArrayList<>();

		List<CTOffer> ottOffers = ctOfferResponse.getOffers();

		CustomerServiceDetail customerServiceDetail = subscriptionsContext.getListBaseBackageServiceDetail().get(0);

		if (featureManagerHelper.isEnabled(Constants.FEATURE_STACKABILITY_RULE_FOR_VIDEO_DEVICES_ENABLED)) {
			ctOfferResponse = applyNewStackabilityRuleForVideoDevice(ctOfferResponse, ottOffers, customerServiceDetail, sortedOffers, stackbilityValues, listOfBenefitCodes, offerRequestWrapper);
		} else {
			ctOfferResponse = applyExistingStackabilityRule(ctOfferResponse, customerServiceDetail, sortedOffers, stackbilityValues, listOfBenefitCodes);
		}
		log.info("END getStackabilityGroupList ");
		return ctOfferResponse;
	}
	private CTOfferResponse applyExistingStackabilityRule(CTOfferResponse ctOfferResponse, CustomerServiceDetail customerServiceDetail, Map<String, List<CTOffer>> sortedOffers, Map<String, List<String>> stackbilityValues, List<String> listOfBenefitCodes) {
		List<CTOffer> filteredOffers = Collections.synchronizedList(new ArrayList<>());
		List<CTOffer> ottOffers = ctOfferResponse.getOffers();
		ottOffers.forEach(offer -> {
			if ((offer.getAttributes().isSkipStackabilityGroupCheck() == null
					|| offer.getAttributes().isSkipStackabilityGroupCheck() == false)
					&& offer.getAttributes().getOfferProductType() != null
					&& !offer.getAttributes().getOfferProductTypes().contains(Constants.VIDEO_DEVICE)) {
			if (Optional.ofNullable(customerServiceDetail.getPromosId()).isPresent()) {
				if (offer.getAttributes().getStackabilityGroup() == null) {
					if (sortedOffers.isEmpty() || !sortedOffers.containsKey("noGroup")) {
						List<CTOffer> noGroup = new ArrayList<>();
						noGroup.add(offer);
						sortedOffers.put("noGroup", noGroup);
					} else if (sortedOffers.containsKey("noGroup")) {
						sortedOffers.get("noGroup").add(offer);
					}
				} else if (Optional.ofNullable(offer.getAttributes().getStackabilityGroup()).isPresent()) {
					String groupName = offer.getAttributes().getStackabilityGroup();
					if (sortedOffers.isEmpty() || !sortedOffers.containsKey(groupName)) {
						List<CTOffer> stackableOffers = new ArrayList<>();
						stackableOffers.add(offer);
						sortedOffers.put(groupName, stackableOffers);
					} else if (sortedOffers.containsKey(groupName)) {
						sortedOffers.get(groupName).add(offer);
					}
				}
				if (Optional.ofNullable(customerServiceDetail.getPromosId()).isPresent()
						&& Optional.ofNullable(offer.getAttributes().getStackabilityGroup()).isPresent()
						&& offer.getAttributes().getStackabilityGroup() != null
						&& Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent()) {
					listOfBenefitCodes.clear();
					offer.getAttributes().getBenefits().forEach(b -> {
						if (Optional.ofNullable(b.getBillingBenefitCode()).isPresent()) {
							listOfBenefitCodes.add(b.getBillingBenefitCode());
						}
					});
					if (anySameEbenefits(listOfBenefitCodes, customerServiceDetail.getPromosId())) {
						stackbilityValues.put(offer.getAttributes().getStackabilityGroup(),
								new ArrayList<>(listOfBenefitCodes));
					}
				}
			}
			} else {
				filteredOffers.add(offer);
			}
		});
		sortedOffers.entrySet().parallelStream().forEach(groupName -> {
			if (!groupName.getValue().isEmpty()) {
				if (stackbilityValues.containsKey(groupName.getKey())) {
					groupName.getValue().forEach(offer -> {
						if (Optional.ofNullable(offer.getAttributes().getBenefits()).isPresent()
								&& Optional.ofNullable(offer.getAttributes().getBenefits().get(0)).isPresent()) {
							if (stackbilityValues.get(groupName.getKey())
									.contains(offer.getAttributes().getBenefits().get(0).getBillingBenefitCode())) {
								filteredOffers.add(offer);
							}
						}
					});
				} else {
					groupName.getValue().forEach(offer -> {
						filteredOffers.add(offer);
					});
				}
			}
		});
		log.info("END getStackabilityGroupList ");

		ctOfferResponse.setOffers(filteredOffers);
		ctOfferResponse.setTotal(filteredOffers.size());
		ctOfferResponse.setCount(filteredOffers.size());
		return ctOfferResponse;
	}
	private CTOfferResponse applyNewStackabilityRuleForVideoDevice(CTOfferResponse ctOfferResponse, List<CTOffer> ctOffers, CustomerServiceDetail customerServiceDetail, Map<String, List<CTOffer>> sortedOffers, Map<String,
			List<String>> stackbilityValues, List<String> listOfBenefitCodes, OfferRequestWrapper offerRequestWrapper) {
		List<CTOffer> filteredOffers = new ArrayList<>();
		Map<String, List<String>> ctProductBenefitsOnAccount = new HashMap<>();//Product Benefits On Account


		List<String> reqProductBenefitsOnAccount = new ArrayList<>();

		List<ProductInfo> videoDeviceProductsInRequest = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts().stream()
				.filter(product -> Constants.VIDEO_DEVICE.equals(product.getProductType()))
				.collect(Collectors.toList());
		int numberOfDevicesOnAccountInReq = videoDeviceProductsInRequest.size();

		//Get the number of Benefits on Customer Context
		videoDeviceProductsInRequest.stream()
				.filter(Objects::nonNull).forEach(product -> {
					if (product.getPromotions() != null) {
						product.getPromotions().forEach(reqPromotion -> {
							reqProductBenefitsOnAccount.add(reqPromotion.getPromotionId());
						});
					}
				});
		//filter the Product Benefits On Account
		videoDeviceProductsInRequest.stream().distinct().filter(Objects::nonNull).forEach(product -> {
			String billingProductCode = product.getProductCode();
			ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
			CTProductRequest ctProductRequest = new CTProductRequest();
			ctProductRequest.setBusinessSegment(Collections.singletonList("CONS"));
			ctProductRequest.setProductCodes(Stream.of(billingProductCode).collect(Collectors.toList()));
			CTProductResponse ctProductResponse = cpopClientHelper.getProducts(ctProductRequest);
			ctProductResponse.getProducts().stream().filter(Objects::nonNull).forEach(ctProduct -> {
				if (Objects.nonNull(ctProduct.getVariants())) {
					ctProduct.getVariants().stream().filter(Objects::nonNull).forEach(variants -> {
						if (Objects.nonNull(variants.getAttributes()) && variants.getAttributes().getBenefitsOnCustomersAccount() != null)
							ctProductBenefitsOnAccount.put(variants.getAttributes().getBillingProductCode(), variants.getAttributes().getBenefitsOnCustomersAccount());
					});
				}
			});
		});

		ctOffers.forEach(ctOffer -> {
			Map<String, Integer> ctOfferBenefitMaxOccurance = new HashMap<>();
			Map<String, Integer> deviceBenefitsOnAccount = new HashMap<>();
			if (ctOffer.getAttributes().isSkipStackabilityGroupCheck() != null
					&& ctOffer.getAttributes().getOfferProductType() != null
					&& ctOffer.getAttributes().getOfferProductTypes().contains(Constants.VIDEO_DEVICE)) {

				//get the max occurance from the offer-benefit
				ctOffer.getAttributes().getBenefits().forEach(benefit -> {
					if (Optional.ofNullable(benefit.getMaxOccurrence()).isPresent()
							&& benefit.getMaxOccurrence() != 0) {
						ctOfferBenefitMaxOccurance.put(benefit.getBillingBenefitCode(), benefit.getMaxOccurrence());
					}
				});

				reqProductBenefitsOnAccount.stream().filter(Objects::nonNull).forEach(reqBenefit -> {
					ctProductBenefitsOnAccount.forEach((key, benefits) -> {
						if (benefits.contains(reqBenefit)) {
							deviceBenefitsOnAccount.put(key, deviceBenefitsOnAccount.getOrDefault(key, 0) + 1);
						}
					});
				});
				String productKey = getBenefitApplicableProduct(ctOffer);
				String billingBenefitCode = getBillingBenefitCode(ctOffer);
				Integer deviceBenefitCount = deviceBenefitsOnAccount.getOrDefault(productKey, 0);
				Integer ctBenefitmaxOccurrence = ctOfferBenefitMaxOccurance.getOrDefault(billingBenefitCode, 0);

				//retention offer-benefit in "Product Benefits On Account"
				boolean isBenefitPresent = false;
				isBenefitPresent = checkIfBenefitPresent(ctProductBenefitsOnAccount, ctOffer);

				if (ctOfferBenefitMaxOccurance.isEmpty()) {
					filteredOffers.add(ctOffer);
				} else {
					if (isBenefitPresent) {
						if (deviceBenefitCount < ctBenefitmaxOccurrence) {
							if (((numberOfDevicesOnAccountInReq - reqProductBenefitsOnAccount.size()) == 0)
									&& (ctOffer.getAttributes().getAssociatedProducts() != null
									&& !ctOffer.getAttributes().getAssociatedProducts().isEmpty()
									&& ctOffer.getAttributes().getAssociatedProducts().get(0) != null
									&& (ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() == null
									|| ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().size() == 0))) {
//							Do not return the offer
							} else {
								ctOffer.getAttributes().getBenefits().get(0).setMaxOccurrence(ctOfferBenefitMaxOccurance.get(ctOffer.getAttributes().getBenefits().get(0).getBillingBenefitCode()) - deviceBenefitsOnAccount.size());
								filteredOffers.add(ctOffer);
							}
						} else {
							//Do not return the offer
						}
					} else {
						filteredOffers.add(ctOffer);

					}

				}
			} else {
				filteredOffers.add(ctOffer);
			}
		});
		ctOfferResponse.setOffers(filteredOffers);
		ctOfferResponse.setTotal(filteredOffers.size());
		ctOfferResponse.setCount(filteredOffers.size());
		return ctOfferResponse;
	}

	private String getBillingBenefitCode(CTOffer ctOffer) {
		if (ctOffer != null
				&& ctOffer.getAttributes() != null
				&& ctOffer.getAttributes().getBenefits() != null
				&& !ctOffer.getAttributes().getBenefits().isEmpty()
				&& ctOffer.getAttributes().getBenefits().get(0) != null) {
			return ctOffer.getAttributes().getBenefits().get(0).getBillingBenefitCode();
		}
		return null;
	}
	private String getBenefitApplicableProduct(CTOffer ctOffer) {
		if (ctOffer != null
				&& ctOffer.getAttributes() != null
				&& ctOffer.getAttributes().getBenefits() != null
				&& !ctOffer.getAttributes().getBenefits().isEmpty()
				&& ctOffer.getAttributes().getBenefits().get(0) != null) {
			return ctOffer.getAttributes().getBenefits().get(0).getApplicableProducts().get(0).getProducts().get(0).getKey();
		}
		return null;
	}

	private boolean checkIfBenefitPresent(Map<String, List<String>> ctProductBenefitsOnAccount, CTOffer ctOffer) {
		for (Map.Entry<String, List<String>> entry : ctProductBenefitsOnAccount.entrySet()) {
			for (String benefitCode : entry.getValue()) {
				if (ctOffer.getAttributes().getBenefits().stream()
						.anyMatch(benefit -> benefitCode.equals(benefit.getBillingBenefitCode()))) {
					return true;
				}
			}
		}
		return false;
	}

	public Boolean anySameEbenefits(List<String> listOfBenefitCodes, List<String> customerContextPromos) {
		listOfBenefitCodes.retainAll(customerContextPromos);
		if (!listOfBenefitCodes.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * This method is added as part of CouponCartValidation implementation
	 * 
	 * @param ctOfferRequest
	 * @return CTOfferResponse
	 */
	public CTOfferResponse getOffersFromCT(CTOfferRequest ctOfferRequest) {
		CTOfferResponse offerResponse = null;
		log.info("Start In Method OttServicesOffersProcessor.getOffersFromCT() :: Getting response from the CTMS");
		offerResponse = cpopUCCClientHelper.getOffers(ctOfferRequest);
		if (offerResponse == null) {
			log.info(
					"In Method OttServicesOffersProcessor.getOffersFromCT() :: Getting the null response from the CTMS");
			throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
		}
		log.info("End In Method OttServicesOffersProcessor.getOffersFromCT() :: Getting response from the CTMS");
		return offerResponse;
	}

	// Method to filter offers for free trial accounts
    public void filterFreeTrailOffers(OfferRequestWrapper offerRequestWrapper, CTOfferResponse ctOfferResponse) {
		String freeTrialStatus = null;
		// Check if the account is in a free trial
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
				&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getAdditionalAccountDetails())
				&& CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCustomerContext().getAdditionalAccountDetails().getAdditonalInfo())
				&& checkForAdditionalInfo(offerRequestWrapper.getOfferRequest().getCustomerContext().getAdditionalAccountDetails().getAdditonalInfo())) {
			freeTrialStatus = Constants.FREE_TRIAL;
		}
		// If the account is in a free trial, filter the offers
		if (!ctOfferResponse.getOffers().isEmpty() && freeTrialStatus != null && freeTrialStatus.equalsIgnoreCase(Constants.FREE_TRIAL)) {
			filterOffersForIneligibleAccountStatus(ctOfferResponse);
		}
	}

	// Method to check if the account is in a free trial
    public boolean checkForAdditionalInfo(List<AdditionalDetails> additionalDetails) {
		if (CollectionUtils.isNotEmpty(additionalDetails)) {
			for (AdditionalDetails details : additionalDetails) {
				// Check if the additional details match the criteria for a free trial
				if ((details.getName().equalsIgnoreCase(Constants.FREE_TRIAL_DETAILS)
						|| details.getName().equalsIgnoreCase(Constants.FREE_TRIAL_DETAILS_UPPERCASE))
						&& CollectionUtils.isNotEmpty(details.getParams())
						&& (details.getParams().stream().filter(Objects::nonNull)
						.anyMatch(d -> (d.getParamValue().equalsIgnoreCase(Constants.ACTIVE))
								&& (d.getParamName().equalsIgnoreCase("status")))
				)) {
					return true;
				}

			}

		}
		return false;
	}

	// Method to filter the offers for ineligible account status
	public void filterOffersForIneligibleAccountStatus(CTOfferResponse ctOfferResponse) {
		// Check if ctOfferResponse is null to avoid NullPointerException
		if (null != ctOfferResponse) {
			// Filter the offers
			ctOfferResponse.getOffers().removeIf(ctOffer ->
					ctOffer.getAttributes().getIneligibleAccountStatus()!=null
					&& ctOffer.getAttributes().getIneligibleAccountStatus().contains(Constants.FREE_TRIAL));
			// Update the count and total of the offers
			ctOfferResponse.setCount(ctOfferResponse.getOffers().size());
			ctOfferResponse.setTotal(ctOfferResponse.getOffers().size());
		}
	}
	public void supressDependentPromoAttributes(CTOfferResponse ctOfferResponse) {
		if (ctOfferResponse != null && ctOfferResponse.getOffers() != null) {
			ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
				if (ctOffer.getAttributes() != null
						&& ctOffer.getAttributes().getAssociatedProducts() != null) {
					ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
							.forEach(associatedProduct -> {
								if (associatedProduct.getBundleProducts() != null
										&& CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts())) {
									associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
											.forEach(bundleProduct -> {
												processProducts(bundleProduct.getProducts());
											});
								}
							});
					ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
							.forEach(associatedProduct -> {
								if (associatedProduct.getQualifyingProducts() != null
										&& CollectionUtils.isNotEmpty(associatedProduct.getQualifyingProducts())) {
									associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull)
											.forEach(qualifyProduct -> {
												processProducts(qualifyProduct.getProducts());
											});
								}
							});
				}
			});
		}
	}

	private void processProducts(List<Product> products) {
		if (products != null) {
			products.stream()
					.filter(Objects::nonNull)
					.forEach(product -> product.getObj().getVariants()
							.stream()
							.filter(Objects::nonNull)
							.forEach(this::clearDependentPromoAttributes));
		}
	}
	private void clearDependentPromoAttributes(Variant variant) {
		if (variant != null && variant.getAttributes() != null) {
			variant.getAttributes().setDependentBillerPromoId(null);
			variant.getAttributes().setDependentPromoCode(null);
			variant.getAttributes().setDependentPromoName(null);
			variant.getAttributes().setDependentPromoDisplayName(null);
			variant.getAttributes().setDependentPromoStartDate(null);
		}
	}

}
