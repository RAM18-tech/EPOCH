/**
 * 
 */
package com.dtv.dcp.epoch.integration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.Constraints;
import com.dtv.dcp.epoch.model.ct.product.DeliveryMethod;
import com.dtv.dcp.epoch.model.ct.product.DeliveryMethodAttributes;
import com.dtv.dcp.epoch.model.ct.product.InCompatibleProductValue;
import com.dtv.dcp.epoch.model.ct.product.IncludedProduct;
import com.dtv.dcp.epoch.model.ct.product.IncludeProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.InstallmentInfo;
import com.dtv.dcp.epoch.model.ct.product.Price;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.MinMaxQuantity;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericNameValueBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.DelayProvisioningMessagesByKey;
import com.dtv.dcp.epoch.model.ct.offer.MessageEntry;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.offer.OnlinePartnerDetails;
import com.dtv.dcp.epoch.model.ct.request.BenefitCodesToSuppressTheOffer;
import com.dtv.dcp.epoch.model.ct.request.CTChannelEligibility;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonFilterService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Component
public class BuildMiddlewareCTFilters {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	OffersUtils offersUtils;

	@Autowired
	private SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	/**
	 * @param ctResponse
	 * @param ctOfferRequest
	 * @return
	 */
	public CTOfferResponse applyMiddlewareFilters(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> offers = ctResponse.getOffers();

		if (CollectionUtils.isEmpty(offers)) {
			return ctResponse;
		}
		List<String> qualifyingProductBillingCodes = ctResponse.getSpecialOfferCartModeCodes();
		offers.stream().filter(Objects::nonNull).forEach(offer -> {
			if (null !=  ctOfferRequest.getCartContext()
				      && ((null !=  ctOfferRequest.getCartContext().getOfferIds() && !ctOfferRequest.getCartContext().getOfferIds().isEmpty())
				        || (null !=  ctOfferRequest.getCartContext().getOfferCodes() && !ctOfferRequest.getCartContext().getOfferCodes().isEmpty()))) {
				populateReconnectOffers(ctResponse, ctOfferRequest, qualifyingProductBillingCodes,offer);
			}
			populateInstallmentCreditRisk(ctOfferRequest, offer);
			populateInstallmentAndMinMaxQuantityEDSP(ctOfferRequest, offer);
			populateOEMDescriptionsByKey(ctOfferRequest, offer);
			removeComplatibleProductsFromResponse(ctOfferRequest, offer);
			removeinstallmentListForEmployee(ctOfferRequest, offer);
		});
		if (ctResponse != null && ctOfferRequest != null) {
			Optional.ofNullable(ctOfferRequest.getOfferProductFamily())
					.filter(CollectionUtils::isNotEmpty)
					.map(family -> family.get(0))
					.filter(Constants.OTT_PRODUCT_FAMILY::equalsIgnoreCase)
					.ifPresent(family -> offersUtils.filterOffersBasedOnCustomerType(ctResponse, ctOfferRequest));
		}
		filterResponseBasedOnRequest(ctResponse,ctOfferRequest);
		evaluateRewardCards(ctResponse,ctOfferRequest);
		filterOffersForDirectIntegrationPartners(ctResponse, ctOfferRequest);
		evaluateBenefitsCodestoSuppressTheOffer(ctResponse, ctOfferRequest);
		priceProtect(ctResponse,ctOfferRequest);
		returnValidOpusStoreOffers(ctResponse,ctOfferRequest);
		returnValidAgentOffers(ctResponse,ctOfferRequest);
		returnValidOnlinePartnerOffers(ctResponse,ctOfferRequest);
		returnValidPartnerDealersOffers(ctResponse,ctOfferRequest);
		removeIneligiblePartnerOffers(ctResponse,ctOfferRequest);
		removeIneligibleAccountStatus(ctResponse,ctOfferRequest);
		filterDeviceOffersBasedOnMigrationIndicator(ctResponse,ctOfferRequest);
		returnValidPartnerTypeOffers(ctResponse,ctOfferRequest);
		productsOnAccountToSupressOffer(ctResponse,ctOfferRequest);
		applyCartContextFilter(ctResponse,ctOfferRequest);
		filterCorrectPrice(ctResponse,ctOfferRequest);
		applySatelliteCartOffersFilter(ctResponse,ctOfferRequest);
		deliveryMethodByChannelFilterOffer(ctResponse,ctOfferRequest);
		// Future mode when we remove all filters from nodejs
		//filterChannelEligibilityOffersForSatellite(ctResponse,ctOfferRequest);

		if(CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& ctOfferRequest.getOfferProductFamily().contains(Constants.SATELLITE_PRODUCT_FAMILY)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType())
				&& (Objects.isNull(ctOfferRequest.getAdeRequest()) || ctOfferRequest.getAdeRequest() == false)) {
			OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
			offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
			satelliteCTOffersProcessor.filterOffersBasedOnDMA(offerRequestWrapper,ctResponse);
		}

		if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_MDU_INCLUDED_PRODUCT_EXCLUSIONS)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& ctOfferRequest.getOfferProductFamily().stream()
				.anyMatch(Constants.OTT::equalsIgnoreCase)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType())
				&& ctOfferRequest.getOfferProductType().stream()
				.anyMatch(Constants.VIDEO_PLAN::equalsIgnoreCase)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getBusinessSegment())
				&& ctOfferRequest.getBusinessSegment().stream()
				.anyMatch(Constants.MDU::equalsIgnoreCase)) {
			filterIncludedProductsFromOffer(ctResponse);
		}
        if(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
            updateMessagesByKey(ctResponse, ctOfferRequest);
        }
        filterIncludeProductsBasedOnSalesChannelORIapPartnerAccountType(ctResponse, ctOfferRequest);
		filterConflictingProductsBasedOnSalesChannelORIapPartnerAccountType(ctResponse, ctOfferRequest);
		return ctResponse;
	}

	public void updateMessagesByKey(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {

		final String flowType = (ctOfferRequest.getOfferActionType() == null)
				? Constants.SALES
				: (ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE) || ctOfferRequest.getOfferActionType().contains(Constants.UPSELL_ACTION_TYPE))
				? Constants.SALES
				: Constants.SERVICES;

		ctResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
			final var attributes = offer.getAttributes();
			if (null != attributes.isDelayProvisioning()) {
				final Map<String, String> offerAllMessagesMap = getOfferAllMessagesMap(attributes.getGlobalMessagesByKey(), ctOfferRequest);
				processAssociatedProductsForMessages(
						attributes,
						ctOfferRequest,
						flowType,
						offerAllMessagesMap,
						true
				);
			} else if(null == attributes.isDelayProvisioning()) {
				final Map<String, String> offerAllMessagesMap = new HashMap<>();
				processAssociatedProductsForMessages(
						attributes,
						ctOfferRequest,
						flowType,
						offerAllMessagesMap,
						false
				);
			}
			offer.getAttributes().setGlobalMessagesByKey(null);
		});
	}

	/**
	 * Helper to modularize the common logic for iterating associated products and variants for message processing.
	 */
	private void processAssociatedProductsForMessages(OfferAttributes attributes, CTOfferRequest ctOfferRequest,
													  String flowType, Map<String, String> offerAllMessagesMap,
													  boolean alwaysUpdate) {
		if (attributes.getAssociatedProducts() == null)
			return;
		attributes.getAssociatedProducts().stream().filter(ap -> CollectionUtils.isNotEmpty(ap.getBundleProducts()))
				.forEach(ap -> ap.getBundleProducts().forEach(bundleProduct -> {
					if (CollectionUtils.isEmpty(bundleProduct.getProducts())
							|| bundleProduct.getProducts().get(0) == null)
						return;
					bundleProduct.getProducts().forEach(product -> {
						if (product.getObj() == null || CollectionUtils.isEmpty(product.getObj().getVariants()))
							return;
						product.getObj().getVariants().forEach(variant -> {
							final var variantAttributes = variant.getAttributes();
							if (variantAttributes == null)
								return;
							final Map<String, String> productAllMessagesMap = getProductAllMessagesMap(
									variantAttributes.getGlobalMessagesByKey(), ctOfferRequest);
							if (alwaysUpdate || (null != variantAttributes.isDelayProvisioning() && variantAttributes.isDelayProvisioning()
									&& null != variantAttributes.getDelayProvisioningReasons()
									&& !variantAttributes.getDelayProvisioningReasons().isEmpty())) {
								updateOfferResponse(Constants.DELAY_PROVISIONING, variantAttributes, offerAllMessagesMap,
										productAllMessagesMap, ctOfferRequest,
										flowType,alwaysUpdate,attributes);
							}
							variant.getAttributes().setGlobalMessagesByKey(null);
						});
					});

				}));
	}

	private Map<String, String> getOfferAllMessagesMap(List<List<MessageEntry>> offerLevelMessages, CTOfferRequest ctOfferRequest) {
		Map<String, String> offerAllMessagesMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(offerLevelMessages) && CollectionUtils.isNotEmpty(ctOfferRequest.getSalesChannel())) {
			ObjectMapper mapper = MAPPER;
			for (var messageGroup : offerLevelMessages) {
				var offerMessagesMap = new HashMap<String, String>();
				for (var message : messageGroup) {
					if (Constants.MESSAGES_BY_KEY_MESSAGE_TYPE.equals(message.getName())) {
						var value = message.getValue();
						if (value instanceof Map) {
							var keyObj = ((Map<?, ?>) value).get(Constants.KEY);
							if (keyObj != null) offerMessagesMap.put(message.getName(), keyObj.toString());
						}
					}
					if (Constants.MESSAGES_BY_KEY_ALL_MESSAGES.equals(message.getName())) {
						for (var msgArr : (List<?>) message.getValue()) {
							for (var entry : (List<?>) msgArr) {
								var allMessage = mapper.convertValue(entry, MessageEntry.class);
								var value = allMessage.getValue();
								if (value instanceof Map) {
									var keyObj = ((Map<?, ?>) value).get(Constants.KEY);
									if (keyObj != null) offerMessagesMap.put(allMessage.getName(), keyObj.toString());
								} else {
									offerMessagesMap.put(allMessage.getName(), value != null ? value.toString() : null);
								}
							}
							offerAllMessagesMap.put(
									offerMessagesMap.get(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE)
											+ offerMessagesMap.get(Constants.ALL_MESSAGES_SALES_CHANNEL)
											+ offerMessagesMap.get(Constants.ALL_MESSAGES_FLOW_TYPE)
											+ offerMessagesMap.get(Constants.KEY),
									offerMessagesMap.get(Constants.VALUE)
							);
						}
					}
				}
			}
		}
		return offerAllMessagesMap;
	}

	private Map<String, String> getProductAllMessagesMap(List<List<MessageEntry>> productLevelMessages, CTOfferRequest ctOfferRequest) {
		Map<String, String> productAllMessagesMap = new HashMap<>();
		if (CollectionUtils.isNotEmpty(productLevelMessages) && CollectionUtils.isNotEmpty(ctOfferRequest.getSalesChannel())) {
			ObjectMapper mapper = MAPPER;
			for (var messageGroup : productLevelMessages) {
				var productMessagesMap = new HashMap<String, String>();
				for (var message : messageGroup) {
					if (Constants.MESSAGES_BY_KEY_MESSAGE_TYPE.equals(message.getName())) {
						var value = message.getValue();
						if (value instanceof Map) {
							var keyObj = ((Map<?, ?>) value).get(Constants.KEY);
							if (keyObj != null) productMessagesMap.put(message.getName(), keyObj.toString());
						}
					}
					if (Constants.MESSAGES_BY_KEY_ALL_MESSAGES.equals(message.getName())) {
						for (var msgArr : (List<?>) message.getValue()) {
							for (var entry : (List<?>) msgArr) {
								var allMessage = mapper.convertValue(entry, MessageEntry.class);
								var value = allMessage.getValue();
								if (value instanceof Map) {
									var keyObj = ((Map<?, ?>) value).get(Constants.KEY);
									if (keyObj != null) productMessagesMap.put(allMessage.getName(), keyObj.toString());
								} else {
									productMessagesMap.put(allMessage.getName(), value != null ? value.toString() : null);
								}
							}
							productAllMessagesMap.put(
									productMessagesMap.get(Constants.MESSAGES_BY_KEY_MESSAGE_TYPE)
											+ productMessagesMap.get(Constants.ALL_MESSAGES_SALES_CHANNEL)
											+ productMessagesMap.get(Constants.ALL_MESSAGES_FLOW_TYPE)
											+ productMessagesMap.get(Constants.KEY),
									productMessagesMap.get(Constants.VALUE)
							);
						}
					}
				}
			}
		}
		return productAllMessagesMap;
	}

	private void updateOfferResponse(String messageKey, com.dtv.dcp.epoch.model.ct.product.Attributes variantAttributes,
									 Map<String, String> offerAllMessagesMap, Map<String, String> productAllMessagesMap,
									 CTOfferRequest ctOfferRequest, String flowType, boolean alwaysUpdate,OfferAttributes attributes) {
		final String salesChannel = ctOfferRequest.getSalesChannel().get(0);
		final String offerShortMsgKey = messageKey + salesChannel + flowType + Constants.SHORT_MESSAGE;
		final String offerLongMsgKey = messageKey + salesChannel + flowType + Constants.LONG_MESSAGE;
		final String offerShortMsgKeyDefault = messageKey + Constants.DEFAULT + flowType + Constants.SHORT_MESSAGE;
		final String offerLongMsgKeyDefault = messageKey + Constants.DEFAULT + flowType + Constants.LONG_MESSAGE;

		final String offerShortMsg = offerAllMessagesMap.getOrDefault(offerShortMsgKey, offerAllMessagesMap.get(offerShortMsgKeyDefault));
		final String offerLongMsg = offerAllMessagesMap.getOrDefault(offerLongMsgKey, offerAllMessagesMap.get(offerLongMsgKeyDefault));

		final String productShortMsgKey = messageKey + salesChannel + flowType + Constants.SHORT_MESSAGE;
		final String productLongMsgKey = messageKey + salesChannel + flowType + Constants.LONG_MESSAGE;
		final String productShortMsgKeyDefault = messageKey + Constants.DEFAULT + flowType + Constants.SHORT_MESSAGE;
		final String productLongMsgKeyDefault = messageKey + Constants.DEFAULT + flowType + Constants.LONG_MESSAGE;

		final String productShortMsg = productAllMessagesMap.getOrDefault(productShortMsgKey, productAllMessagesMap.get(productShortMsgKeyDefault));
		final String productLongMsg = productAllMessagesMap.getOrDefault(productLongMsgKey, productAllMessagesMap.get(productLongMsgKeyDefault));

		if (Constants.DELAY_PROVISIONING.equalsIgnoreCase(messageKey)) {
			var delayProvisioningMsg = new DelayProvisioningMessagesByKey();
			delayProvisioningMsg.setShortMessage(
					offerShortMsg != null || alwaysUpdate ? offerShortMsg : productShortMsg);
			delayProvisioningMsg.setLongMessage(
					offerLongMsg != null || alwaysUpdate ? offerLongMsg : productLongMsg);
			variantAttributes.setDelayProvisioningMessagesByKey((null != delayProvisioningMsg.getShortMessage() || null !=  delayProvisioningMsg.getLongMessage()) ?
					delayProvisioningMsg : null);
			if(variantAttributes.getDelayProvisioningMessagesByKey() != null) {
				variantAttributes.setDelayProvisioning(alwaysUpdate ? attributes.isDelayProvisioning():variantAttributes.isDelayProvisioning());
				variantAttributes.setDelayProvisioningReasons(alwaysUpdate ? attributes.getDelayProvisioningReasons():variantAttributes.getDelayProvisioningReasons());
			}else {
				variantAttributes.setDelayProvisioning(null);
				variantAttributes.setDelayProvisioningReasons(null);
			}
		}
	}

	public void populateInstallmentCreditRisk(CTOfferRequest ctOfferRequest, CTOffer offer) {
		if (ctOfferRequest.getCreditRisk() != null) {
			List<Variant> extractedVariant = extractVariantFromBundleProduct(offer);
			if (CollectionUtils.isNotEmpty(extractedVariant) && extractedVariant.size() > 0) {
				List<InstallmentInfo> installmentList = extractedVariant.get(0).getAttributes().getInstallmentList();
				if (installmentList != null && installmentList.size() > 0) {
					List<InstallmentInfo> filteredInstallment = new ArrayList<>();
					for (InstallmentInfo installment : installmentList) {
						if (installment.getCreditRiskEligibility() != null) {
							List<String> creditRiskEligibility = installment.getCreditRiskEligibility().stream().map(InCompatibleProductValue::getKey).collect(Collectors.toList());
							for (String creditRisk : creditRiskEligibility) {
								if (creditRisk.equalsIgnoreCase(ctOfferRequest.getCreditRisk())) {
									filteredInstallment.add(installment);
									break;
								}
							}
						}
					}
					extractedVariant.get(0).getAttributes().setInstallmentList(filteredInstallment);
				}
			}
		}
	}

	public void populateInstallmentAndMinMaxQuantityEDSP(CTOfferRequest ctOfferRequest, CTOffer offer) {
		if (CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& ctOfferRequest.getOfferProductFamily().size() > 0
				&& !ctOfferRequest.getOfferProductFamily().contains("satellite")) {
			List<Variant> extractedVariant = extractVariantFromBundleProduct(offer);
			if (CollectionUtils.isNotEmpty(extractedVariant) && extractedVariant.size() > 0) {
				for (Variant variant : extractedVariant) {
					List<InstallmentInfo> installmentArray = new ArrayList<>();
					List<MinMaxQuantity> minMaxArray = new ArrayList<>();
					if (CollectionUtils.isNotEmpty(variant.getAttributes().getInstallmentList())
							&& variant.getAttributes().getInstallmentList().size() > 0) {
						for (InstallmentInfo installment : variant.getAttributes().getInstallmentList()) {
							if (CollectionUtils.isNotEmpty(installment.getContractApplicable()) && CollectionUtils.isNotEmpty(ctOfferRequest.getContractIndicator())) {
								Boolean found = installment.getContractApplicable().stream()
										.anyMatch(s -> ctOfferRequest.getContractIndicator().contains(s));
								if (found) {
									installmentArray.add(installment);
								}
							}
						}
						variant.getAttributes().setInstallmentList(installmentArray);
					}
					if (CollectionUtils.isNotEmpty(variant.getAttributes().getMinMaxQuantity())
							&& variant.getAttributes().getMinMaxQuantity().size() > 0) {
						for (MinMaxQuantity minmaxQuantity : variant.getAttributes().getMinMaxQuantity()) {
							if (CollectionUtils.isNotEmpty(minmaxQuantity.getContractApplicable()) && CollectionUtils.isNotEmpty(ctOfferRequest.getContractIndicator())) {
								Boolean found = minmaxQuantity.getContractApplicable().stream()
										.anyMatch(s -> ctOfferRequest.getContractIndicator().contains(s));
								if (found) {
									minMaxArray.add(minmaxQuantity);
								}
							}
						}
						variant.getAttributes().setMinMaxQuantity(minMaxArray);
					}
				}
			}
		}
	}

	public void filterOffersForDirectIntegrationPartners(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> offers = ctResponse.getOffers();
		if ((CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& ctOfferRequest.getOfferProductFamily().get(0).equalsIgnoreCase("OTT")
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getSalesChannel())
				&& ctOfferRequest.getSalesChannel().get(0).equalsIgnoreCase("directIntegrationPartner"))
				|| (CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
						&& !ctOfferRequest.getOfferProductFamily().get(0).equalsIgnoreCase("OTT")
						&& ctOfferRequest.getChannelEligibility() != null
						&& !Collections.disjoint(ctOfferRequest.getSalesChannel(),
								new HashSet<>(Arrays.asList("dpp", "ccap", "salesCRM", "directIntegrationPartner", Constants.ASSISTED_SALES))))) {
			List<CTOffer> nonMatchingOffers = new ArrayList<>();
			offers.stream().filter(Objects::nonNull).forEach(offer -> {
				OfferAttributes attributes = offer.getAttributes();
				CTChannelEligibility channelEligibility = ctOfferRequest.getChannelEligibility();
				if (isOfferChannelEligibilityNotMatching(attributes, channelEligibility)) {
					nonMatchingOffers.add(offer);
				}
			});
			if (!nonMatchingOffers.isEmpty()) {
				List<CTOffer> nonMatchingOffersTemp = nonMatchingOffers.stream()
						.filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
				for (CTOffer nonMatchingOffer : nonMatchingOffersTemp) {
					ctResponse.setOffers(ctResponse.getOffers().stream()
							.filter(of -> !of.getId().equals(nonMatchingOffer.getId())).collect(Collectors.toList()));
					ctResponse.setCount(ctResponse.getOffers().size());
					ctResponse.setTotal(ctResponse.getTotal() - nonMatchingOffers.size());
				}
			}

			if(CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
					&& ctOfferRequest.getOfferProductFamily().get(0).equalsIgnoreCase("OTT")
					&& CollectionUtils.isNotEmpty(ctOfferRequest.getSalesChannel())
					&& ctOfferRequest.getSalesChannel().get(0).equalsIgnoreCase("directIntegrationPartner")
					&& CollectionUtils.isNotEmpty(ctResponse.getOffers())) {
				for (CTOffer finalOffer : ctResponse.getOffers()) {
					if(null != finalOffer)
						checkConflictsEligibility(ctResponse,  ctResponse.getOffers(),finalOffer.getAttributes(), ctOfferRequest);
				}
				ctResponse.setOffers(ctResponse.getOffers().stream().filter(Objects::nonNull).collect(Collectors.toList()));
				ctResponse.setCount(ctResponse.getOffers().size());
				ctResponse.setTotal(ctResponse.getTotal() - nonMatchingOffers.size());
			}
		}
	}


	private boolean isOfferChannelEligibilityNotMatching(OfferAttributes attributes,
			CTChannelEligibility channelEligibility) {
		return (attributes.getSalesChannel() != null
				&& channelEligibility.getSalesChannel() != null
				&& (!attributes.getSalesChannel()
				.stream().anyMatch(type -> type.equalsIgnoreCase(channelEligibility.getSalesChannel()))))
		|| (attributes.getSalesSubChannel() != null
				&& channelEligibility.getSalesSubChannel() != null
				&& !attributes.getSalesSubChannel()
						.stream().anyMatch(type -> type.equalsIgnoreCase(channelEligibility.getSalesSubChannel())))
		|| (attributes.getDirectIntegrationPartnerName() != null
				&& channelEligibility.getDirectIntegrationPartnerName() != null
				&& !attributes.getDirectIntegrationPartnerName().stream().anyMatch(type -> type.equalsIgnoreCase(
						channelEligibility.getDirectIntegrationPartnerName())))
		|| (attributes.getMasterDealerId() != null
				&& channelEligibility.getMasterDealerId() != null
				&& (!attributes.getMasterDealerId()
						.stream().anyMatch(type -> type.equalsIgnoreCase(channelEligibility.getMasterDealerId()))))
		|| (attributes.getDealerIds() != null
				&& channelEligibility.getDealerId() != null
				&& (!attributes.getDealerIds()
						.stream().anyMatch(type -> type.equalsIgnoreCase(channelEligibility.getDealerId()))))
		|| (attributes.getDealerCode() != null
				&& channelEligibility.getDealerCode() != null
				&& (!attributes.getDealerCode()
						.stream().anyMatch(type -> type.equalsIgnoreCase(channelEligibility.getDealerCode()))))
		|| (attributes.getLocationId() != null
				&& channelEligibility.getLocationId() != null
				&& (! List.of(attributes.getLocationId().split(","))
				.stream().anyMatch(type -> type.equalsIgnoreCase(channelEligibility.getLocationId()))))
		|| (attributes.getLocationTypeId() != null
				&& channelEligibility.getLocationTypeId() != null
				&& (! List.of(attributes.getLocationTypeId().split(","))
				.stream().anyMatch(type -> type.equalsIgnoreCase(channelEligibility.getLocationTypeId()))));
	}

	public void filterChannelEligibilityOffersForSatellite(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> offers = ctResponse.getOffers();
		if (CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& !ctOfferRequest.getOfferProductFamily().get(0).equalsIgnoreCase("OTT")
				&& ctOfferRequest.getChannelEligibility()!=null) {
			List<CTOffer> matchingOffers = new ArrayList<>();
			offers.stream().filter(Objects::nonNull).forEach(offer -> {
				OfferAttributes attributes = offer.getAttributes();
				boolean filteredOffer = true;
				if ((attributes.getSalesChannel() != null && ctOfferRequest.getChannelEligibility().getSalesChannel() != null
						&& !attributes.getSalesChannel()
						.contains(ctOfferRequest.getChannelEligibility().getSalesChannel()))) {
					filteredOffer = false;
				}
				if((ctOfferRequest.getChannelEligibility().getDirectIntegrationPartnerName() != null
						&& attributes.getDirectIntegrationPartnerName()!=null
						&& !attributes.getDirectIntegrationPartnerName().contains(
								ctOfferRequest.getChannelEligibility().getDirectIntegrationPartnerName()))) {
					filteredOffer = false;
				}
				if ((attributes.getDealerCode() != null && ctOfferRequest.getChannelEligibility().getDealerCode() != null
						&& !attributes.getDealerCode()
						.contains(ctOfferRequest.getChannelEligibility().getDealerCode()))) {
					filteredOffer = false;
				}
				if ((CollectionUtils.isNotEmpty(attributes.getDealerIds())
						&&attributes.getDealerIds().size()>0 && ctOfferRequest.getChannelEligibility().getDealerId() != null
						&& !attributes.getDealerIds().contains(ctOfferRequest.getChannelEligibility().getDealerId()))) {
					filteredOffer = false;
				}
				if ((attributes.getMasterDealerId() != null && ctOfferRequest.getChannelEligibility().getMasterDealerId() != null
						&& !attributes.getMasterDealerId()
						.contains(ctOfferRequest.getChannelEligibility().getMasterDealerId()))) {
					filteredOffer = false;
				}
				if ((attributes.getSalesSubChannel() != null && ctOfferRequest.getChannelEligibility().getSalesSubChannel() != null
						&& !attributes.getSalesSubChannel()
						.contains(ctOfferRequest.getChannelEligibility().getSalesSubChannel()))) {
					filteredOffer = false;
				}
				if(filteredOffer) {
					matchingOffers.add(offer);
				}

			});
			if (CollectionUtils.isNotEmpty(matchingOffers) && matchingOffers.size() > 0) {
				ctResponse.setOffers(matchingOffers.stream().filter(x -> x != null).collect(Collectors.toList()));
				ctResponse.setCount(ctResponse.getOffers().size());
				ctResponse.setTotal(ctResponse.getOffers().size());
			}
		}
	}

	public void evaluateBenefitsCodestoSuppressTheOffer(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (CollectionUtils.isNotEmpty(ctOfferRequest.getBenefitCodesToSuppressTheOffer())) {
			List<CTOffer> coolOffPeriodOfferResult = new ArrayList<>();
			List<CTOffer> removeOfferFromResult = new ArrayList<>();
			List<CTOffer> validStackableOffers = new ArrayList<>();
			Map<String, String> benefitMap = new HashMap<>();
			for (BenefitCodesToSuppressTheOffer suppressBenefit : ctOfferRequest.getBenefitCodesToSuppressTheOffer()) {
				// startDAte -1 means the benefit is not active in customer yet
				if (!"-1".equals(suppressBenefit.getStartDate())) {
					benefitMap.put(suppressBenefit.getBenefitCode(), suppressBenefit.getEndDate());
				}
			}
			if (!benefitMap.isEmpty()) {
				for (CTOffer localOffer : ctResponse.getOffers()) {
					OfferAttributes attributes = localOffer.getAttributes();
					Map<String, Integer> suppressMap = buildBenefitCodestoSuppressObject(localOffer);
					if (attributes != null && !suppressMap.isEmpty()) {
						for (String benefitCode : suppressMap.keySet()) {
							if (benefitMap.containsKey(benefitCode)) {
								if (99999 == suppressMap.get(benefitCode) || "-1".equals(benefitMap.get(benefitCode))
										|| OffersUtils.isFutureDate(benefitMap.get(benefitCode))) {
									removeOfferFromResult.add(localOffer);
								} else {
									long diffDays = returnNumberOfDays(benefitMap.get(benefitCode));
									if (diffDays > suppressMap.get(benefitCode)) {
										coolOffPeriodOfferResult.add(localOffer);
									} else {
										removeOfferFromResult.add(localOffer);
									}
								}
							} else {
								// if customer does nt hv that benefit to suppress the offer return the offer
							}
							// Code to check if Retention offers are stackable
							if (localOffer.getAttributes() != null
									&& localOffer.getAttributes().getStackabilityGroup() != null) {
								if (benefitMap.get(benefitCode)!=null && OffersUtils.isFutureDate(benefitMap.get(benefitCode))) {
									validStackableOffers.add(localOffer);
								}
							}
						}
					}
				}
				if (!removeOfferFromResult.isEmpty()) {
					List<CTOffer> removeOfferFromResultTemp = removeOfferFromResult.stream()
							.filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
					ctResponse.setOffers(ctResponse.getOffers().stream().filter(
							of -> !removeOfferFromResultTemp.stream().anyMatch(s -> of.getId().contains(s.getId())))
							.collect(Collectors.toList()));
				}
				if (!validStackableOffers.isEmpty()) {
					List<String> stackabilityGroupList = new ArrayList<>();
					for (CTOffer stackabilityOffer : validStackableOffers) {
						if (stackabilityOffer != null && stackabilityOffer.getAttributes() != null
								&& stackabilityOffer.getAttributes().getStackabilityGroup() != null) {
							stackabilityGroupList.add(stackabilityOffer.getAttributes().getStackabilityGroup());
						}
					}
					if (CollectionUtils.isNotEmpty(stackabilityGroupList)) {
						for (String stackabilityGroup : stackabilityGroupList) {
							ctResponse.setOffers(ctResponse.getOffers().stream()
									.filter(of -> (Optional.ofNullable(of.getAttributes().getStackabilityGroup()).isEmpty()
											|| (Optional.ofNullable(of.getAttributes().getStackabilityGroup()).isPresent()
											&& of.getAttributes().getStackabilityGroup() != null
											&& !of.getAttributes().getStackabilityGroup().equalsIgnoreCase(stackabilityGroup))))
									.collect(Collectors.toList()));
						}
					}

				}
				ctResponse.setCount(ctResponse.getOffers().size());
				ctResponse.setTotal(ctResponse.getOffers().size());
			}
		}
	}

	public Map<String, Integer> buildBenefitCodestoSuppressObject(CTOffer localOffer) {
		JsonNode benefitCodesToSuppressTheOffer = localOffer.getAttributes().getBenefitCodesToSuppressTheOffer();
		Map<String, Integer> suppressMap = new HashMap<>();
		ObjectMapper mapper = MAPPER;
		if(null != benefitCodesToSuppressTheOffer) {
			for(JsonNode x : benefitCodesToSuppressTheOffer) {
				List<String> values = new ArrayList<>();
				Integer coolOffValue = 0;
				for(JsonNode y : x) {
					String name = null != y.get("name")?y.get("name").asText():"";
					if(name.equalsIgnoreCase("benefitCodes")) {
						values = mapper.convertValue(y.get("value"), List.class);
					}
					if(name.equalsIgnoreCase("coolOffPeriod")) {
						coolOffValue = y.get("value").intValue();
					}

				}
				if (CollectionUtils.isNotEmpty(values)) {
					for(String value:values) {
						suppressMap.put(value,coolOffValue);
					}
				}
			}
		}
		return suppressMap;
	}

	public int returnNumberOfDays(String endDate) {

		if(endDate!=null && endDate.contains("/")) {
			endDate = endDate.replaceAll("/", "-");
		}
		LocalDate localCurrentDate = LocalDate.parse(LocalDate.now().toString(),DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYY_MM_DD));
		LocalDate localEndDate = LocalDate.parse(endDate,DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_M_D_YYYY));
		return Math.toIntExact(ChronoUnit.DAYS.between(localEndDate,localCurrentDate));
	}

	public void populateReconnectOffers(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest,
			List<String> qualifyingProductBillingCodes, CTOffer offer) {

		List<String> deleteSpecialOffer = new ArrayList<>();
		if (offersUtils.isCustomerTypeReconnect(offer)) {
			if (ctOfferRequest.isReconnectOfferFlag()) {
				if (ctOfferRequest.getSalesChannel().get(0).equalsIgnoreCase("online")) {
					filterSpecialOfferForReconnect(ctResponse, offer);
				} else {
					populateDeleteSpecialOffer(qualifyingProductBillingCodes, offer,
							 deleteSpecialOffer);
					filterSpecialOffer(ctResponse, deleteSpecialOffer);
				}
			} else {
				filterSpecialOfferForReconnect(ctResponse, offer);
			}
		}
		if (!offersUtils.isCustomerTypeReconnect(offer)) {
			populateDeleteSpecialOffer(qualifyingProductBillingCodes, offer,
					 deleteSpecialOffer);
			filterSpecialOffer(ctResponse, deleteSpecialOffer);
		}
	}

	public void populateDeleteSpecialOffer(List<String> qualifyingProductBillingCodes, CTOffer offer,
			 List<String> deleteSpecialOffer) {
		List<String> qualifyingProductIds = new ArrayList<>();
		if (offer.getAttributes() != null && offer.getAttributes().isSpecialOffer()) {
			if (CollectionUtils.isNotEmpty(offer.getAttributes().getQualifyingProductIds())
					&& offer.getAttributes().getQualifyingProductIds().size() > 0) {
				qualifyingProductIds = offer.getAttributes().getQualifyingProductIds().stream()
						.map(Product::getKey).collect(Collectors.toList());
			}
			String specialOfferTobeChecked = offer.getAttributes().getBillingCode();
			if (CollectionUtils.isNotEmpty(qualifyingProductIds) && qualifyingProductIds.size() > 0) {
				Boolean found = qualifyingProductIds.stream()
						.anyMatch(s -> qualifyingProductBillingCodes.contains(s));
				if (!found) {
					if (specialOfferTobeChecked != null) {
						deleteSpecialOffer.add(specialOfferTobeChecked);
					}
				}
			}
		}
	}

	public void filterSpecialOfferForReconnect(CTOfferResponse ctResponse, CTOffer offer) {
		List<CTOffer> filteredOffers = new ArrayList<>();
		if (offer.getAttributes() != null && !offer.getAttributes().isSpecialOffer()) {
			List<Variant> variants = extractVariantFromBundleProduct(offer);
			for (Variant variant : variants) {
				JsonFilterService
				.filterAttributesAndGetObjectFromJson(
						variant, Variant.class, Stream
						.of("dependentPromoCode", "dependentBillerPromoId", "dependentPromoStartDate",
								"dependentPromoName", "dependentPromoDisplayName")
						.toArray(String[]::new));
			}
			filteredOffers.add(offer);
		}
		ctResponse.setOffers(filteredOffers);
		ctResponse.setCount(ctResponse.getOffers().size());
		ctResponse.setTotal(ctResponse.getOffers().size());
	}

	public void filterSpecialOffer(CTOfferResponse ctResponse, List<String> deleteSpecialOffer) {
		List<CTOffer> filteredOffers = new ArrayList<>();
		for (CTOffer offer : ctResponse.getOffers()) {
			if ((offer.getAttributes() != null && !offer.getAttributes().isSpecialOffer())
					|| (offer.getAttributes() != null && offer.getAttributes().isSpecialOffer()
					&& !deleteSpecialOffer.contains(offer.getAttributes().getBillingCode()))) {
				filteredOffers.add(offer);
			}
		}
		ctResponse.setOffers(filteredOffers);
		ctResponse.setCount(ctResponse.getOffers().size());
		ctResponse.setTotal(ctResponse.getOffers().size());
	}

	public List<Variant> extractVariantFromBundleProduct(CTOffer offer) {
		List<Variant> extractedVariant = new ArrayList<>();
		if (offer.getAttributes().getAssociatedProducts() != null) {
			for (AssociatedProduct associatedProduct : offer.getAttributes().getAssociatedProducts()) {
				// collecting unique bundle products from the offer
				if (associatedProduct.getBundleProducts() != null) {
					for (ProductWrapper bundleProduct : associatedProduct.getBundleProducts()) {
						if (bundleProduct.getProducts() != null) {
							for (Product qualiProd : bundleProduct.getProducts()) {
								if (qualiProd.getObj() != null && qualiProd.getObj().getVariants() != null
										&& qualiProd.getObj().getVariants().size() > 0) {
									extractedVariant.addAll(qualiProd.getObj().getVariants());
								}
							}
						}
					}
				}
			}
		}
		return extractedVariant;
	}

	public List<Variant> extractVariantFromQualifyingProduct(CTOffer offer) {
		List<Variant> extractedVariant = new ArrayList<>();
		if (offer.getAttributes().getAssociatedProducts() != null) {
			for (AssociatedProduct associatedProduct : offer.getAttributes().getAssociatedProducts()) {
				// collecting unique Qualifying products from the offer
				if (associatedProduct.getQualifyingProducts() != null) {
					for (ProductWrapper qualProduct : associatedProduct.getQualifyingProducts()) {
						if (qualProduct.getProducts() != null) {
							for (Product qualiProd : qualProduct.getProducts()) {
								if (qualiProd.getObj() != null && qualiProd.getObj().getVariants() != null
										&& qualiProd.getObj().getVariants().size() > 0) {
									extractedVariant.addAll(qualiProd.getObj().getVariants());
								}
							}
						}
					}
				}
			}
		}
		return extractedVariant;
	}

	public void removeOfferWithKey(List<CTOffer> results, String key) {
		if (results != null && !results.isEmpty()) {
			int indx = findIndexByKey(results, res -> res != null && res.getCode().equals(key));
			if (indx >= 0) {
				results.set(indx, null);
			}
		}
	}

	public void checkConflictsEligibility(CTOfferResponse ctResult, List<CTOffer> tempCtResultsOffers,
			OfferAttributes attributes, CTOfferRequest ctOfferRequest) {
		if (ctOfferRequest.getOfferProductType() != null
				&& (ctOfferRequest.getOfferProductType().contains("video-device")
						|| ctOfferRequest.getOfferProductType().contains("video-plan")
						|| ctOfferRequest.getOfferProductType().contains("fee"))) {
			checkConflicts(ctResult, tempCtResultsOffers, attributes);
		}
	}

	public void checkConflicts(CTOfferResponse ctResult, List<CTOffer> tempCtResultsOffers,
			OfferAttributes attributes) {
		if (attributes.getConflictingOffers() != null && !attributes.getConflictingOffers().isEmpty()) {
			for (GenericTypeIdBase obj : attributes.getConflictingOffers()) {
				if (obj.getKey() != null) {
					removeOfferWithKey(ctResult.getOffers(), obj.getKey());
					removeOfferWithKey(tempCtResultsOffers, obj.getKey());
				}
			}
			attributes.setConflictingOffers(null);
		}
	}

	public int findIndexByKey(List<CTOffer> list, Predicate<CTOffer> predicate) {
		for (int i = 0; i < list.size(); i++) {
			if (predicate.test(list.get(i))) {
				return i;
			}
		}
		return -1;
	}

	public void iterateAssociatedProduct(CTOfferRequest ctOfferRequest, List<ProductWrapper> associatedProducts) {
		for (ProductWrapper associatedProduct : associatedProducts) {
			if (associatedProduct.getProducts() != null) {
				for (Product qualiProd : associatedProduct.getProducts()) {
					extractOfferWithBusinessSegment(ctOfferRequest, qualiProd);
				}
			}
		}
	}

	public void extractOfferWithBusinessSegment(CTOfferRequest ctOfferRequest, Product prod) {
		String businessSegment;
		if (ctOfferRequest.getBusinessSegment() != null) {
			businessSegment = ctOfferRequest.getBusinessSegment().get(0);
		} else {
			businessSegment = "none";
		}
		List<Variant> tempVariant = new ArrayList<>();
		if (prod.getObj() != null && prod.getObj().getVariants() != null && prod.getObj().getVariants().size() > 0) {
			if (!"none".equals(businessSegment)) {
				for (Variant variant : prod.getObj().getVariants()) {
					if (variant.getAttributes() != null && variant.getAttributes().getBusinessSegment() != null
							&& variant.getAttributes().getBusinessSegment().contains(businessSegment)) {
						tempVariant.add(variant);
					}
				}
			}
			if ("none".equals(businessSegment) || tempVariant.size() < 1) {
				tempVariant.add(prod.getObj().getVariants().get(0));
			}
			prod.getObj().setVariants(tempVariant);
		}
	}

	public void evaluateRewardCards(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (ctOfferRequest.getAdditionalOfferType() != null
				&& ctOfferRequest.getAdditionalOfferType().equalsIgnoreCase("reward")) {
			List<CTOffer> offers = ctResponse.getOffers();
			List<CTOffer> rewardOfferResult = new ArrayList<>();
			offers.stream().filter(Objects::nonNull).forEach(offer -> {
				OfferAttributes attributes = offer.getAttributes();
				if (null != attributes.getOfferType() && attributes.getOfferType().equals("reward")) {
					if (OffersUtils.isEndDateIsGreater(offer.getEndDate())) {
						for (Benefit benefit : attributes.getBenefits()) {
							if (benefit.isQuotaBased()) {
								if (benefit.getAvailableQuota() != null && benefit.getAvailableQuota() > 0) {
									rewardOfferResult.add(offer);
									break;
								}
							} else {
								rewardOfferResult.add(offer);
								break;
							}
						}
					}
				} else {
					rewardOfferResult.add(offer);
				}
			});
			if (CollectionUtils.isNotEmpty(rewardOfferResult) && rewardOfferResult.size() > 0) {
				ctResponse.setOffers(rewardOfferResult.stream().filter(OffersUtils.distinctByKey(p -> p.getId()))
						.collect(Collectors.toList()));
				ctResponse.setTotal(ctResponse.getOffers().size());
			}
		}
	}

	public void extractBusinessSegment(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> offers = ctResponse.getOffers();
		offers.stream().filter(Objects::nonNull).forEach(offer -> {
			if (CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts())) {
				offer.getAttributes().getAssociatedProducts().stream().forEach(associatedProduct -> {
					if (associatedProduct.getBundleProducts() != null) {
						iterateAssociatedProduct(ctOfferRequest, associatedProduct.getBundleProducts());
					}
					if (associatedProduct.getQualifyingProducts() != null) {
						iterateAssociatedProduct(ctOfferRequest, associatedProduct.getQualifyingProducts());
					}
				});
			}
		});
	}

	public void populateOEMDescriptionsByKey(CTOfferRequest ctOfferRequest, CTOffer offer) {
		String salesChannel = getOEMchannel(ctOfferRequest);
		if (offer.getAttributes().getDescriptionsByKey() != null) {
			GenericByKey descriptionsByKey = offer.getAttributes().getDescriptionsByKey();
			if (descriptionsByKey.getOemLongDescriptionRokuTv() != null) {
				if (salesChannel != null && salesChannel.equals("oemIAPROKUTV")) {
					offer.getAttributes().getDescriptionsByKey()
					.setOemLongDescription(descriptionsByKey.getOemLongDescriptionRokuTv());
					offer.getAttributes().getDescriptionsByKey().setOemLongDescriptionFireTv(null);
					offer.getAttributes().getDescriptionsByKey().setOemLongDescriptionRokuTv(null);
				} else {
					descriptionsByKey.setOemLongDescriptionRokuTv(null);
				}
			} else if (offer.getAttributes().getAssociatedProducts() != null
					&& offer.getAttributes().getAssociatedProducts().size() > 0) {
				if (salesChannel != null && salesChannel.equals("oemIAPROKUTV")) {
					List<Variant> variants = extractVariantFromBundleProduct(offer);
					if (CollectionUtils.isNotEmpty(variants) && variants.size() > 0) {
						for (Variant variant : variants) {
							if (variant.getAttributes().getDescriptionsByKey()!=null &&
									variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionFireTv()!=null &&
									variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionFireTv()!=null) {
								offer.getAttributes().getDescriptionsByKey().setOemLongDescription(
										variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionRokuTv());
							}
						}
					}
				}
			}
			if (descriptionsByKey.getOemShortDescriptionRokuTv() != null) {
				if (salesChannel != null && salesChannel.equals("oemIAPROKUTV")) {
					offer.getAttributes().getDescriptionsByKey()
					.setOemShortDescription(descriptionsByKey.getOemShortDescriptionRokuTv());
					offer.getAttributes().getDescriptionsByKey().setOemShortDescriptionFireTv(null);
					offer.getAttributes().getDescriptionsByKey().setOemShortDescriptionRokuTv(null);
				} else {
					descriptionsByKey.setOemShortDescriptionRokuTv(null);
				}
			} else if (offer.getAttributes().getAssociatedProducts() != null
					&& offer.getAttributes().getAssociatedProducts().size() > 0) {
				if (salesChannel != null && salesChannel.equals("oemIAPROKUTV")) {
					List<Variant> variants = extractVariantFromBundleProduct(offer);
					if (CollectionUtils.isNotEmpty(variants) && variants.size() > 0) {
						for (Variant variant : variants) {
							if (variant.getAttributes().getDescriptionsByKey()!=null &&
									variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionFireTv()!=null &&
									variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionFireTv()!=null) {
								offer.getAttributes().getDescriptionsByKey().setOemShortDescription(
										variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionRokuTv());
							}
						}
					}
				}
			}

			if (descriptionsByKey.getOemLongDescriptionFireTv() != null) {
				if (salesChannel != null && salesChannel.equals("oemIAPFIRETV")) {
					offer.getAttributes().getDescriptionsByKey()
					.setOemLongDescription(descriptionsByKey.getOemLongDescriptionFireTv());
					offer.getAttributes().getDescriptionsByKey().setOemLongDescriptionRokuTv(null);
					offer.getAttributes().getDescriptionsByKey().setOemLongDescriptionFireTv(null);
				} else {
					descriptionsByKey.setOemLongDescriptionFireTv(null);
				}
			} else if (offer.getAttributes().getAssociatedProducts() != null
					&& offer.getAttributes().getAssociatedProducts().size() > 0) {
				if (salesChannel != null && salesChannel.equals("oemIAPFIRETV")) {
					List<Variant> variants = extractVariantFromBundleProduct(offer);
					if (CollectionUtils.isNotEmpty(variants) && variants.size() > 0) {
						for (Variant variant : variants) {
							if (variant.getAttributes().getDescriptionsByKey()!=null &&
									variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionFireTv()!=null &&
									variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionFireTv()!=null) {
								offer.getAttributes().getDescriptionsByKey().setOemLongDescription(
										variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionFireTv());
							}
						}
					}
				}
			}
			if (descriptionsByKey.getOemShortDescriptionFireTv() != null) {
				if (salesChannel != null && salesChannel.equals("oemIAPFIRETV")) {
					offer.getAttributes().getDescriptionsByKey()
					.setOemShortDescription(descriptionsByKey.getOemShortDescriptionFireTv());
					offer.getAttributes().getDescriptionsByKey().setOemShortDescriptionRokuTv(null);
					offer.getAttributes().getDescriptionsByKey().setOemShortDescriptionFireTv(null);
				} else {
					descriptionsByKey.setOemShortDescriptionFireTv(null);
				}
			} else if (offer.getAttributes().getAssociatedProducts() != null
					&& offer.getAttributes().getAssociatedProducts().size() > 0) {
				if (salesChannel != null && salesChannel.equals("oemIAPFIRETV")) {
					List<Variant> variants = extractVariantFromBundleProduct(offer);
					if (CollectionUtils.isNotEmpty(variants) && variants.size() > 0) {
						for (Variant variant : variants) {
							if (variant.getAttributes().getDescriptionsByKey()!=null &&
									variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionFireTv()!=null &&
									variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionFireTv()!=null) {
								offer.getAttributes().getDescriptionsByKey().setOemShortDescription(
										variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionFireTv());
							}
						}
					}
				}
			}
		} else if (offer.getAttributes().getAssociatedProducts() != null
				&& offer.getAttributes().getAssociatedProducts().size() > 0) {
			if (salesChannel != null && salesChannel.equals("oemIAPFIRETV")) {
				List<Variant> variants = extractVariantFromBundleProduct(offer);
				if (CollectionUtils.isNotEmpty(variants) && variants.size() > 0) {
					for (Variant variant : variants) {
						if (variant.getAttributes().getDescriptionsByKey()!=null &&
								variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionFireTv()!=null &&
								variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionFireTv()!=null) {
							setOEMDescriptionByKey(offer,
									variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionFireTv(),
									variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionFireTv());
						}
					}
				}
			} else if (salesChannel != null && salesChannel.equals("oemIAPROKUTV")) {
				List<Variant> variants = extractVariantFromBundleProduct(offer);
				if (CollectionUtils.isNotEmpty(variants) && variants.size() > 0) {
					for (Variant variant : variants) {
						if (variant.getAttributes().getDescriptionsByKey()!=null &&
								variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionRokuTv()!=null
								&& variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionRokuTv()!=null) {
							setOEMDescriptionByKey(offer,
									variant.getAttributes().getDescriptionsByKey().getOemShortDescriptionRokuTv(),
									variant.getAttributes().getDescriptionsByKey().getOemLongDescriptionRokuTv());
						}
					}
				}
			}
		}
	}

	public void setOEMDescriptionByKey(CTOffer offer, String shortDecription, String longDescription) {
		GenericByKey descriptionsByKey = new GenericByKey();
		offer.getAttributes().setDescriptionsByKey(descriptionsByKey);
		offer.getAttributes().getDescriptionsByKey().setOemShortDescription(shortDecription);
		offer.getAttributes().getDescriptionsByKey().setOemLongDescription(longDescription);
	}

	public void returnValidOpusStoreOffers(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (ctResponse.getOffers() != null && ctResponse.getOffers().size() > 0
				&& ctOfferRequest.getSalesChannel() != null
				&& (ctOfferRequest.getSalesChannel().contains("opus") || (ctOfferRequest.getSalesChannel().size() == 2
				&& ctOfferRequest.getSalesChannel().contains("directvOnline")))
				&& (ctOfferRequest.getOpusChannel() != null && ctOfferRequest.getOpusSubChannel() != null
				&& ctOfferRequest.getOpusStoreId() != null)) {
			List<CTOffer> tempCtResultsOffers = new ArrayList<>();
			if (ctResponse.getOffers() != null && ctResponse.getOffers().size() > 0) {
				for (CTOffer offer : ctResponse.getOffers()) {
					if (offer != null) {
						if (offer.getAttributes().getOpusGlobalSubChannels() != null && offer.getAttributes()
								.getOpusGlobalSubChannels().contains(ctOfferRequest.getOpusSubChannel())) {
							tempCtResultsOffers.add(offer);
							checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
						} else if (offer.getAttributes().getOpusGlobalChannels() != null && offer.getAttributes()
								.getOpusGlobalChannels().contains(ctOfferRequest.getOpusChannel())) {
							tempCtResultsOffers.add(offer);
							checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
						} else if (offer.getAttributes().getOpusStoreIds() != null) {
							if (offer.getAttributes().getOpusStoreIds().contains(ctOfferRequest.getOpusStoreId())) {
								tempCtResultsOffers.add(offer);
								checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
							}
						} else if (offer.getAttributes().getOpusSubChannels() != null) {
							if (offer.getAttributes().getOpusSubChannels()
									.contains(ctOfferRequest.getOpusSubChannel())) {
								tempCtResultsOffers.add(offer);
								checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
							}
						} else if (offer.getAttributes().getOpusChannels() != null) {
							if (offer.getAttributes().getOpusChannels().contains(ctOfferRequest.getOpusChannel())) {
								tempCtResultsOffers.add(offer);
								checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
							}
						} else {
							tempCtResultsOffers.add(offer);
						}
					}
				}
			}
			ctResponse.setOffers(tempCtResultsOffers.stream().filter(Objects::nonNull).collect(Collectors.toList()));
			ctResponse.setTotal(ctResponse.getOffers().size());
		} else if (ctOfferRequest.getSalesChannel() != null && ctOfferRequest.getSalesChannel().contains("opus")
				&& ctOfferRequest.getOpusChannel() == null && ctOfferRequest.getOpusSubChannel() == null
				&& ctOfferRequest.getOpusStoreId() == null && ctOfferRequest.getOfferProductFamily() != null
				&& ctOfferRequest.getOfferProductFamily().contains("OTT")) {
			ctResponse.setOffers(ctResponse.getOffers().stream()
					.filter(offer -> offer.getAttributes().getOpusStoreIds() == null
					|| offer.getAttributes().getOpusChannels() == null
					|| offer.getAttributes().getOpusSubChannels() == null)
					.collect(Collectors.toList()));
			ctResponse.setTotal(ctResponse.getOffers().size());
			ctResponse.setCount(ctResponse.getOffers().size());
		}
	}

	public void returnValidAgentOffers(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> tempCtResultsOffers = new ArrayList<>();
		if (ctOfferRequest.getSalesChannel() != null && ctOfferRequest.getSalesChannel().contains("opus")
				&& ctOfferRequest.getAgentDealerDetails() != null) {
			for (CTOffer offer : ctResponse.getOffers()) {
				if (offer != null) {
					if (offer.getAttributes().getOpusDealerID1() != null
							|| offer.getAttributes().getOpusDealerID2() != null) {
						if (offer.getAttributes().getOpusDealerID1() != null && offer.getAttributes().getOpusDealerID1()
								.contains(ctOfferRequest.getAgentDealerDetails().getDealerId1())) {
							tempCtResultsOffers.add(offer);
							checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
						} else if (offer.getAttributes().getOpusDealerID2() != null && offer.getAttributes()
								.getOpusDealerID2().contains(ctOfferRequest.getAgentDealerDetails().getDealerId2())) {
							tempCtResultsOffers.add(offer);
							checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
						}
					} else {
						tempCtResultsOffers.add(offer);
					}
				}
			}
			ctResponse.setOffers(tempCtResultsOffers.stream().filter(x -> x != null).collect(Collectors.toList()));
			ctResponse.setTotal(ctResponse.getOffers().size());

		} else if (ctOfferRequest.getSalesChannel() != null && ctOfferRequest.getSalesChannel().contains("opus")
				&& ctOfferRequest.getAgentDealerDetails() == null && ctOfferRequest.getOfferProductFamily() != null
				&& ctOfferRequest.getOfferProductFamily().contains("OTT")) {
			ctResponse
			.setOffers(
					ctResponse.getOffers().stream()
					.filter(offer -> offer.getAttributes().getOpusDealerID1() == null
					|| offer.getAttributes().getOpusDealerID2() == null)
					.collect(Collectors.toList()));
			ctResponse.setTotal(ctResponse.getOffers().size());
			ctResponse.setCount(ctResponse.getOffers().size());
		}
	}

	// filter the partnerDealerDetails
	public void returnValidPartnerDealersOffers(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (ctResponse.getOffers() != null && ctResponse.getOffers().size() > 0) {
			List<CTOffer> tempCtResultsOffers = new ArrayList<>();
			if (ctOfferRequest.getSalesChannel() != null && ctOfferRequest.getSalesChannel().contains("partner")
					&& ctOfferRequest.getPartnerDealerDetails() != null) {
				for (CTOffer offer : ctResponse.getOffers()) {
					if (offer != null) {
						if (offer.getAttributes().getPartnerDealerCode1() != null
								|| offer.getAttributes().getPartnerDealerCode2() != null) {
							if ((offer.getAttributes().getPartnerDealerCode1() != null
									&& offer.getAttributes().getPartnerDealerCode1().contains(
											ctOfferRequest.getPartnerDealerDetails().getPartnerDealerCode1()))) {
								tempCtResultsOffers.add(offer);
								checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
							} else if (offer.getAttributes().getPartnerDealerCode2() != null
									&& offer.getAttributes().getPartnerDealerCode2().contains(
											ctOfferRequest.getPartnerDealerDetails().getPartnerDealerCode2())) {
								tempCtResultsOffers.add(offer);
								checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
							}
						} else {
							tempCtResultsOffers.add(offer);
						}
					}
				}
				ctResponse
				.setOffers(tempCtResultsOffers.stream().filter(Objects::nonNull).collect(Collectors.toList()));
				ctResponse.setTotal(ctResponse.getOffers().size());
			}
		}
	}

	// Filter for OnlinePartnerDetails
	public void returnValidOnlinePartnerOffers(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (ctResponse.getOffers() != null && ctResponse.getOffers().size() > 0) {
			List<CTOffer> tempCtResultsOffers = new ArrayList<>();
			if (ctOfferRequest.getOnlinePartnerDetails() != null) {
				for (CTOffer offer : ctResponse.getOffers()) {
					if (offer != null) {
						if (offer.getAttributes().getOnlinePartnerDetails() != null
								&& offer.getAttributes().getOnlinePartnerDetails().size() > 0) {
							for (OnlinePartnerDetails onlinePartnerDetail : offer.getAttributes()
									.getOnlinePartnerDetails()) {
								if (ctOfferRequest.getOnlinePartnerDetails().getPartnerName() != null
										&& onlinePartnerDetail.getPartnerName() != null
										&& onlinePartnerDetail.getPartnerName().toLowerCase().equals(ctOfferRequest
												.getOnlinePartnerDetails().getPartnerName().toLowerCase())) {
									if (onlinePartnerDetail.getDealerCode1() != null) {
										if (onlinePartnerDetail.getDealerCode1()
												.contains(ctOfferRequest.getOnlinePartnerDetails().getDealerCode1())) {
											tempCtResultsOffers.add(offer);
											checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
										}
									} else {
										tempCtResultsOffers.add(offer);
										checkConflicts(ctResponse, tempCtResultsOffers, offer.getAttributes());
									}
								}
							}
						} else {
							tempCtResultsOffers.add(offer);
						}
					}
				}
				ctResponse
				.setOffers(tempCtResultsOffers.stream().filter(Objects::nonNull).collect(Collectors.toList()));
				ctResponse.setTotal(ctResponse.getOffers().size());
			} else if (ctOfferRequest.getSalesChannel() != null
					&& ctOfferRequest.getSalesChannel().contains("directvOnline")
					&& ctOfferRequest.getOfferProductFamily() != null
					&& ctOfferRequest.getOfferProductFamily().contains("OTT")) {
				ctResponse.setOffers(ctResponse.getOffers().stream()
						.filter(offer -> offer.getAttributes().getOnlinePartnerDetails() == null)
						.collect(Collectors.toList()));
				ctResponse.setTotal(ctResponse.getOffers().size());
				ctResponse.setCount(ctResponse.getOffers().size());
			}
		}
	}

	// Filter for Ineligible Partners
	public void removeIneligiblePartnerOffers(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (ctResponse.getOffers() != null && ctResponse.getOffers().size() > 0) {
			List<CTOffer> tempCtResultsOffers = new ArrayList<>();
			if (ctOfferRequest.getOnlinePartnerDetails() != null) {
				for (CTOffer offer : ctResponse.getOffers()) {
					boolean filterOffer = true;
					if (offer.getAttributes().getIneligiblePartners() != null && !offer.getAttributes().getIneligiblePartners().isEmpty()) {
						if (offer.getAttributes().getIneligiblePartners().contains("ALL")) {
							filterOffer = false;
						} else {
							for (String ineligiblePartnerName : offer.getAttributes().getIneligiblePartners()) {
								if (ctOfferRequest.getOnlinePartnerDetails().getPartnerName() != null && ineligiblePartnerName != null
										&& ineligiblePartnerName.toLowerCase().equals(ctOfferRequest.getOnlinePartnerDetails().getPartnerName().toLowerCase())) {
									filterOffer = false;
									break;
								}
							}
						}
					}
					if (filterOffer) {
						tempCtResultsOffers.add(offer);
					}
				}
				ctResponse.setOffers(tempCtResultsOffers);
				ctResponse.setTotal(ctResponse.getOffers().size());
			}
		}
	}

	// filter the ineligibleAccountStatus
	public void removeIneligibleAccountStatus(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (ctResponse.getOffers() != null && ctResponse.getOffers().size() > 0) {
			List<CTOffer> tempCtResultsOffers = new ArrayList<>();
			if (ctOfferRequest.getAdditionalAccountDetails() != null && ctOfferRequest.getAdditionalAccountDetails().getStatus() != null) {
				for (CTOffer offer : ctResponse.getOffers()) {
					boolean filterOffer = true;
					if (offer != null) {
						if (offer.getAttributes().getIneligibleAccountStatus() != null
								&& offer.getAttributes().getIneligibleAccountStatus().contains(ctOfferRequest.getAdditionalAccountDetails().getStatus())) {
							filterOffer = false;
						}
					}
					if (filterOffer) {
						tempCtResultsOffers.add(offer);
					}
				}
				ctResponse.setOffers(tempCtResultsOffers.stream().filter(Objects::nonNull).collect(Collectors.toList()));
				ctResponse.setTotal(ctResponse.getOffers().size());
			}
		}
	}

	// Filter migration Indicator for Devices
	public void filterDeviceOffersBasedOnMigrationIndicator(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if ((ctOfferRequest.isMigrationIndicator()== true || ctOfferRequest.isMigrationIndicator() == false ) && ctResponse.getOffers() != null
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType())&& ctOfferRequest.getOfferProductType().size()>0
				&& ctOfferRequest.getOfferProductType().contains("video-device")
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())&& ctOfferRequest.getOfferProductFamily().size()>0
				&& ctOfferRequest.getOfferProductFamily().contains("OTT")){
			String migrationInd;
			if (ctOfferRequest.isMigrationIndicator()) {
				migrationInd = "migration";
			} else {
				migrationInd = "non-migration";
			}
			List<CTOffer> filteredOffers = new ArrayList<>();

			List<CTOffer> offers = ctResponse.getOffers();
			offers.stream().filter(Objects::nonNull).forEach(offer -> {
				if(CollectionUtils.isNotEmpty(offer.getAttributes().getOfferIntents())&&offer.getAttributes().getOfferIntents().size()>0 ) {
					if(offer.getAttributes().getOfferIntents().contains(migrationInd)) {
						filteredOffers.add(offer);
					}
				}
			});

			ctResponse.setOffers(filteredOffers.stream().filter(Objects::nonNull).collect(Collectors.toList()));
			ctResponse.setTotal(ctResponse.getOffers().size());
		}
	}

	public void removeComplatibleProductsFromResponse(CTOfferRequest ctOfferRequest, CTOffer offer) {
		if (ctOfferRequest.getCustomerSegments() != null && ctOfferRequest.getCustomerSegments().size() == 1) {
			if (ctOfferRequest.getCustomerSegments().contains("Mobility")) {
				if (offer.getAttributes().getAssociatedProducts() != null && offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null) {
					List<Variant> variants = extractVariantFromBundleProduct(offer);
					for (Variant variant : variants) {
						variant.getAttributes().setCompatibleProducts(null);
					}
				} else if (offer.getAttributes().getAssociatedProducts() != null && offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts() != null) {
					List<Variant> variants = extractVariantFromQualifyingProduct(offer);
					for (Variant variant : variants) {
						variant.getAttributes().setCompatibleProducts(null);
					}
				}
			} else if (ctOfferRequest.getCustomerSegments().contains("Residential") || ctOfferRequest.getCustomerSegments().contains("Employee")) {
				List<Variant> variants = extractVariantFromBundleProduct(offer);
				for (Variant variant : variants) {
					variant.getAttributes().setCompatibleMobilityProducts(null);
				}
			} else if (offer.getAttributes().getAssociatedProducts() != null && offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts() != null) {
				List<Variant> variants = extractVariantFromQualifyingProduct(offer);
				for (Variant variant : variants) {
					variant.getAttributes().setCompatibleMobilityProducts(null);
				}
			}
		}
	}

	public void removeinstallmentListForEmployee(CTOfferRequest ctOfferRequest, CTOffer offer) {
		if(CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())&& ctOfferRequest.getOfferProductFamily().size()>0
				&& ctOfferRequest.getOfferProductFamily().contains("OTT") && CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerSegments())
				&& ctOfferRequest.getCustomerSegments().size()>0 && ctOfferRequest.getCustomerSegments().contains("Employee")  ) {
			List<Variant> variants = extractVariantFromBundleProduct(offer);
			for (Variant variant : variants) {
				variant.getAttributes().setInstallmentList(null);
			}
		}
	}

	// Filter to return the offer only if partnerType matches the value in request
	public void returnValidPartnerTypeOffers(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (ctOfferRequest != null && ctOfferRequest.getOfferProductType() != null && ctOfferRequest.getOfferProductType().contains("video-device")
				&& ctOfferRequest.getPartnerType() != null) {
			List<CTOffer> filteredOffers = new ArrayList<>();
			for (CTOffer offer : ctResponse.getOffers()) {
				List<Variant> variants = extractVariantFromBundleProduct(offer);
				for (Variant varobj : variants) {
					if (varobj.getAttributes() != null && varobj.getAttributes().getDeliveryMethodByChannel() != null) {
						DeliveryMethod deliveryMethodByChannel = varobj.getAttributes().getDeliveryMethodByChannel();
						if(CollectionUtils.isNotEmpty(deliveryMethodByChannel.getDeliveryMethod()) && deliveryMethodByChannel.getDeliveryMethod().size()>0) {
							for (DeliveryMethodAttributes deliveryMethod : deliveryMethodByChannel.getDeliveryMethod()) {
								if (StringUtils.isNotEmpty(deliveryMethod.getPartnerType())) {
									if (ctOfferRequest.getPartnerType().contains(deliveryMethod.getPartnerType())) {
										filteredOffers.add(offer);
									} else if(deliveryMethod.getPartnerType().equalsIgnoreCase("Default")) {
										filteredOffers.add(offer);
									}
								}
							}
						}
					}
				}
			}
			ctResponse.setOffers(filteredOffers.stream().filter(Objects::nonNull).collect(Collectors.toList()));
			ctResponse.setTotal(ctResponse.getOffers().size());
		}
	}

	public void productsOnAccountToSupressOffer(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> filteredOffers = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerContext()) && ctOfferRequest.getCustomerContext().size()>0
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getProductsOnAccountToSuppressOffer())&& ctOfferRequest.getProductsOnAccountToSuppressOffer().size()>0) {
			ctOfferRequest.getCustomerContext().stream().filter(Objects::nonNull).forEach(custContext ->{
				if(custContext!=null && custContext.getProductFamily().equalsIgnoreCase("OTT")
						&& CollectionUtils.isNotEmpty(custContext.getProducts())&& custContext.getProducts().size()>0 ) {
					List<CTOffer> offers = ctResponse.getOffers();
					offers.stream().filter(Objects::nonNull).forEach(offer -> {
						if(offer.getAttributes()!=null && CollectionUtils.isEmpty(offer.getAttributes().getProductsOnAccountToSuppressOffer())) {
							filteredOffers.add(offer);
						}
					});
				}
			});
		}
		if(filteredOffers.size()>0) {
			ctResponse.setOffers(filteredOffers.stream().filter(Objects::nonNull).collect(Collectors.toList()));
			ctResponse.setTotal(ctResponse.getOffers().size());
		}

	}

	public void applyCartContextFilter(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<String> requestCustProductFamily = new ArrayList<>();
		List<com.dtv.dcp.epoch.model.ct.request.Product> requestCustProducts = new ArrayList<>();
		List<String> requestCartProductFamily = new ArrayList<>();
		List<com.dtv.dcp.epoch.model.ct.request.Product> requestCartProducts = new ArrayList<>();
		List<String> offersToDeleteRewards = new ArrayList<>();
		//Populating cart and customer context products
		populateCartAndCustomerContextProducts(ctOfferRequest,requestCustProductFamily,
				requestCustProducts,requestCartProductFamily,requestCartProducts);
		if (((CollectionUtils.isNotEmpty(requestCartProductFamily) && requestCartProductFamily.size() > 0)
				|| (CollectionUtils.isNotEmpty(requestCartProducts) && requestCartProducts.size() > 0))
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferIntent()) && ctOfferRequest.getOfferIntent().size()>0
				&& !ctOfferRequest.getOfferIntent().contains("migration")) {
			ctResponse.getOffers().replaceAll(offer -> {
				boolean validRewardsOffer = false;
				if (offer.getAttributes().getAssociatedProducts() != null
						&& offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts() != null) {
					boolean validQualifyingProduct = false;
					boolean alreadyUpdated = false;
					for (ProductWrapper qualProduct : offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts()) {
						if (qualProduct.getConstraints()!=null && qualProduct.getConstraints().getProductState()!=null
								&& qualProduct.getConstraints().getProductState().equalsIgnoreCase("new")) {
							/**
							 * if both cartContextProductFamily && cartContextProductIds are in the request then
							 * offer to be return in the response if both productFamily in the offer qualifying products constraints is matching with the productFamily in request of cartContext
							 * and if all the productIds in the offer qualifying products is matching with the productIds in request of cartContext
							 */
							if (requestCartProductFamily != null && requestCartProductFamily.size() > 0
									&& requestCartProducts != null && requestCartProducts.size() > 0) {
								boolean requestProdFamilyMatched = false;
								if (qualProduct.getConstraints().getProductFamily() != null
										&& requestCartProductFamily.contains(qualProduct.getConstraints().getProductFamily())) {
									requestProdFamilyMatched = true;
								}
								/**
								 * offer is a valid offer for cart context filtering
								 * if all of the offer qualifying products is matching with the products in request
								 */
								List<com.dtv.dcp.epoch.model.ct.request.Product> requestProductsExist = new ArrayList<>();
								if (requestProdFamilyMatched && CollectionUtils.isNotEmpty(qualProduct.getProducts())&& qualProduct.getProducts().size() > 0) {
									for (Product eachProduct : qualProduct.getProducts()) {
										String eachProdBillingCode = eachProduct.getObj().getVariants().get(0).getAttributes().getBillingCode();
										String eachProdBillingProductCode = eachProduct.getObj().getVariants().get(0).getAttributes().getBillingProductCode();
										for (com.dtv.dcp.epoch.model.ct.request.Product eachRequestCartProduct : requestCartProducts) {
											if (eachRequestCartProduct.getProductId() != null && !eachRequestCartProduct.getProductId().equals(eachProduct.getId())) {
												continue;
											} else if (eachRequestCartProduct.getProductCode() != null && !eachRequestCartProduct.getProductCode().equals(eachProduct.getKey())) {
												continue;
											} else if (eachRequestCartProduct.getProductType() != null && !eachRequestCartProduct.getProductType().equals(eachProduct.getProductType())) {
												continue;
											} else if (eachRequestCartProduct.getBillingCode() != null && eachRequestCartProduct.getBillingCode() != null
													&& eachProdBillingCode != null && eachProdBillingProductCode != null
													&& !eachRequestCartProduct.getBillingCode().equals(eachProdBillingCode)
													&& !eachRequestCartProduct.getBillingProductCode().equals(eachProdBillingProductCode)) {
												continue;
											} else if (eachRequestCartProduct.getBillingCode() != null && eachProdBillingCode != null
													&& !eachRequestCartProduct.getBillingCode().equals(eachProdBillingCode)) {
												continue;
											} else if (eachRequestCartProduct.getBillingProductCode() != null && eachProdBillingProductCode != null
													&& !eachRequestCartProduct.getBillingProductCode().equals(eachProdBillingProductCode)) {
												continue;
											}
											requestProductsExist.add(eachRequestCartProduct);
										}
									}
								}
								/**
								 * if both Qualifying product family and one of the product ids in the request
								 * matched with the qualifying product in the offer
								 * */
								System.out.println("requestProdFamilyMatched matched :: " + requestProdFamilyMatched + "\n"
										+ "requestProductsExist :: " + requestProductsExist);
								if (!alreadyUpdated && requestProdFamilyMatched && requestProductsExist != null
										&& requestProductsExist.size() > 0) {
									validQualifyingProduct = true;
								} else if (requestProdFamilyMatched && (requestProductsExist == null
										|| (requestProductsExist != null && requestProductsExist.size() == 0))) {
									validQualifyingProduct = false;
									alreadyUpdated = true;
								} else if (!requestProdFamilyMatched) {
									validQualifyingProduct = false;
									alreadyUpdated = true;
								}
							}
							else if (requestCartProductFamily != null && requestCartProductFamily.size() > 0) {
								/**
								 * if only cartContextProductFamily is in the request then
								 * offer to be return in the response if productFamily in the offer qualifying products constraints is matching with the productFamily in request of cartContext
								 */
								if (qualProduct.getConstraints().getProductFamily() != null
										&& requestCartProductFamily.contains(qualProduct.getConstraints().getProductFamily().toLowerCase())) {
									validRewardsOffer = true;
								}
							}
							else if (requestCartProducts != null && requestCartProducts.size() > 0) {
								/**
								 * if only cartContextProductIds is in the request then
								 * offer to be return in the response if atleast one productId in the offer qualifying products is matching with the productId in request of cartContext
								 */
								if (qualProduct.getProducts() != null && qualProduct.getProducts().size() > 0) {
									for (Product eachProduct : qualProduct.getProducts()) {
										validRewardsOffer = false;
										for (com.dtv.dcp.epoch.model.ct.request.Product eachRequestCartProduct  : requestCartProducts) {
											boolean productMatched = true;
											if (eachRequestCartProduct.getProductId() != null && !eachRequestCartProduct.getProductId().equals(eachProduct.getId())) {
												productMatched = false;
											}
											if (eachRequestCartProduct.getProductCode() != null && !eachRequestCartProduct.getProductCode().equals(eachProduct.getKey())) {
												productMatched = false;
											}
											if (eachRequestCartProduct.getProductType() != null && !eachRequestCartProduct.getProductType().equals(eachProduct.getProductType())) {
												productMatched = false;
											}
											if (productMatched) {
												validRewardsOffer = true;
												break;
											}
										}
									}
								}
							}
						}
					}
					if (validQualifyingProduct) {
						validRewardsOffer = true;
					}
				}
				if (validRewardsOffer) {
					return offer;
				} else {
					/**
					 * if none of the qualifying products in the offer satisfied the cartcontext filtering condition
					 * then remove the offer from response
					 */
					offersToDeleteRewards.add(offer.getId());
					return null;
				}
			});
			//ctResponse.getOffers().removeIf(offer -> offersToDeleteRewards.contains(offer.getId()));
			ctResponse.setOffers(ctResponse.getOffers().stream().filter(offer -> offersToDeleteRewards.contains(offer.getId())).collect(Collectors.
					toList
					()));
		}
	}


	public void applySatelliteCartOffersFilter(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if(CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& ctOfferRequest.getOfferProductFamily().size() > 0
				&& ctOfferRequest.getOfferProductFamily().contains("satellite")
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getCartProducts())
				&& ctOfferRequest.getCartProducts().size()>0 ) {
			filterOffersByPolicy(ctResponse,ctOfferRequest);
			filterOffersByPPCBillingRefId(ctResponse,ctOfferRequest);
			filterOffersByBillingRefId(ctResponse,ctOfferRequest);
			filterOffersByComponentCode(ctResponse,ctOfferRequest);
			filterOffersByModelNumber(ctResponse,ctOfferRequest);
		}
	}

	public void filterOffersByPolicy(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> nonMatchingOffers = new ArrayList<>();
		ctOfferRequest.getCartProducts().forEach(code -> {
			if (code != null && code.getPricePlanCode() != null && code.getPriceCode() != null && code.getProductType().equals("video-plan")) {
				if (ctResponse.getOffers() != null) {
					ctResponse.getOffers().forEach(offer -> {
						if (offer != null && offer.getAttributes() != null && offer.getAttributes().getOfferProductType().equals("video-plan") && offer.getAttributes().getAssociatedProducts() != null) {
							List<Variant> variant = extractVariantFromBundleProduct(offer);
							if (variant.get(0).getAttributes() != null
									&& variant.get(0).getAttributes().getEnablerPricePlanCode().equals(code.getPricePlanCode())) {
								boolean isMatching = false;
								if (offer.getAttributes().getOmsFreeStbPolicy() != null) {
									for(Product policy:offer.getAttributes().getOmsFreeStbPolicy() ) {
										if (code.getPriceCode().equals("NA")) {
											if (policy != null && policy.getAttributes() != null
													&& (policy.getAttributes().getName().equals("DVANA1") || policy.getAttributes().getName().equals("DVANA2"))) {
												isMatching = true;
											}
										} else if (policy != null && policy.getAttributes() != null && policy.getAttributes().getName().equals(code.getPriceCode())) {
											isMatching = true;
										}
									}
								}
								if (!isMatching) {
									nonMatchingOffers.add(offer);
								}
							}
						}
					});
					if (nonMatchingOffers != null && nonMatchingOffers.size() > 0) {
						List<CTOffer> nonMatchingOffersTemp = nonMatchingOffers.stream()
								.filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
						nonMatchingOffersTemp.forEach(nonMatchingOffer -> {
							ctResponse.setOffers(ctResponse.getOffers().stream().filter(offer -> !offer.getId().equals(nonMatchingOffer.getId())).collect(Collectors.toList()));
							ctResponse.setTotal(ctResponse.getOffers().size());
						});
					}
				}
			}
		});
	}

	public void filterOffersByPPCBillingRefId(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> nonMatchingOffers = new ArrayList<>();
		ctOfferRequest.getCartProducts().forEach(code -> {
			if (code != null && code.getPricePlanCode() != null && code.getPriceCode() != null
					&& (code.getProductType().equals("video-addon") || code.getProductType().equals("insurance"))) {
				if (ctResponse.getOffers() != null) {
					ctResponse.getOffers().forEach(offer -> {
						if (offer != null && offer.getAttributes() != null
								&& (offer.getAttributes().getOfferProductType().equals("video-addon")
										|| offer.getAttributes().getOfferProductType().equals("insurance"))
								&& offer.getAttributes().getAssociatedProducts() != null) {
							List<Variant> variant = extractVariantFromBundleProduct(offer);
							if (variant.size() > 0) {
								if (variant.get(0).getAttributes() != null
										&& variant.get(0).getAttributes().getEnablerPricePlanCode().equals(code.getPricePlanCode())) {
									if (!offer.getAttributes().getBillingId().equals(code.getPriceCode())) {
										nonMatchingOffers.add(offer);
									}
								}
							}
						}
					});
					if (nonMatchingOffers != null && nonMatchingOffers.size() > 0) {
						List<CTOffer> nonMatchingOffersTemp = nonMatchingOffers.stream()
								.filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
						nonMatchingOffersTemp.forEach(nonMatchingOffer -> {
							ctResponse.setOffers(ctResponse.getOffers().stream().filter(offer -> !offer.getId().equals(nonMatchingOffer.getId())).collect(Collectors.toList()));
							ctResponse.setTotal(ctResponse.getOffers().size());
						});
					}
				}
			}
		});
	}

	public void filterOffersByBillingRefId(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		
		List<String> includedOfferCodes=new ArrayList<>();
		ctResponse.getOffers().forEach(offer->{
			if(CollectionUtils.isNotEmpty(offer.getAttributes().getIncludedOffers()) && CollectionUtils.isNotEmpty(ctOfferRequest.getCartOffers())
					&& ctOfferRequest.getCartOffers().contains(offer.getCode())) {
				offer.getAttributes().getIncludedOffers().forEach(includedOffer->{
					includedOfferCodes.add(includedOffer.getObj().getCode());
				});
			}
		});
		
		List<CTOffer> nonMatchingOffers = new ArrayList<>();
		ctOfferRequest.getCartProducts().forEach(code -> {
			if (code != null && code.getBillingProductCode() != null && code.getPriceCode() != null) {
				if (ctResponse.getOffers() != null) {
					ctResponse.getOffers().forEach(offer -> {
						if (offer != null && offer.getAttributes() != null && offer.getAttributes().getBillingCode() != null 
								&& offer.getAttributes().getBillingCode().equals(code.getBillingProductCode())
								&& (CollectionUtils.isEmpty(includedOfferCodes) || (CollectionUtils.isNotEmpty(includedOfferCodes) 
										&& !includedOfferCodes.contains(offer.getCode())))) { 
							if (offer.getAttributes().getBillingId() != null && !offer.getAttributes().getBillingId().equals(code.getPriceCode())) {
								nonMatchingOffers.add(offer);
							}
						}
					});
					if (nonMatchingOffers != null && nonMatchingOffers.size() > 0) {
						List<CTOffer> nonMatchingOffersTemp = nonMatchingOffers.stream()
								.filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
						if (ctOfferRequest.getCartOffers() != null && ctOfferRequest.getCartOffers().size() > 0) {
							// changes for insurance offers in cartOffers in request
							for(String cartOffer: ctOfferRequest.getCartOffers()) {
								nonMatchingOffersTemp = nonMatchingOffersTemp.stream().filter(offer -> !offer.getCode().equals(cartOffer)).collect(Collectors.toList());
							}
						}
						nonMatchingOffersTemp.forEach(nonMatchingOffer -> {
							ctResponse.setOffers(ctResponse.getOffers().stream()
									.filter(offer -> !offer.getId().equals(nonMatchingOffer.getId()))
									.collect(Collectors.toList()));
							ctResponse.setTotal(ctResponse.getOffers().size());
						});
					}
				}
			}
		});
		List<CTOffer> distinctOffers = ctResponse.getOffers().stream().filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
		ctResponse.setOffers(distinctOffers);
		ctResponse.setTotal(distinctOffers.size());
	}

	public void filterOffersByComponentCode(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> nonMatchingOffers = new ArrayList<>();
		List<CTOffer> matchingOffers = new ArrayList<>();
		for(ProductInfo code:ctOfferRequest.getCartProducts()) {
			if (code != null && code.getComponentCode() != null && code.getProductType().equals("video-device")) {
				if (ctResponse.getOffers() != null) {
					for(CTOffer offer:ctResponse.getOffers() ) {
						if (offer != null && offer.getAttributes() != null && offer.getAttributes().getOfferProductType().equals("video-device")
								&& offer.getAttributes().getAssociatedProducts() != null) {
							List<Variant> variant= extractVariantFromBundleProduct(offer);
							if (variant.size() > 0) {
								if (variant.get(0).getAttributes() != null && null != getComponentCodeFromBillingParams(variant.get(0).getAttributes())
										&& getComponentCodeFromBillingParams(variant.get(0).getAttributes()).equals(code.getComponentCode())) {
									matchingOffers.add(offer);
								} else {
									nonMatchingOffers.add(offer);
								}
							}
						}
					}
				}
			}
		}
		if (nonMatchingOffers != null && nonMatchingOffers.size() > 0) {
			nonMatchingOffers = nonMatchingOffers.stream()
					.filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
			if (matchingOffers != null && matchingOffers.size() > 0) {
				// changes for more than one component code in request
				matchingOffers =matchingOffers.stream()
						.filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
				for(CTOffer matchingOffer:matchingOffers) {
					nonMatchingOffers = nonMatchingOffers.stream().filter(offer -> !offer.getId().equals(matchingOffer.getId())).collect(Collectors.toList());
				}
			}
			if (ctOfferRequest.getCartOffers() != null && ctOfferRequest.getCartOffers().size() > 0) {
				// changes for video-device offers in cartOffers in request
				for(String cartOffer:ctOfferRequest.getCartOffers()){
					nonMatchingOffers = nonMatchingOffers.stream().filter(offer -> !offer.getCode().equals(cartOffer)).collect(Collectors.toList());
				}
			}
			nonMatchingOffers.forEach(nonMatchingOffer -> {
				ctResponse.setOffers(ctResponse.getOffers().stream().filter(offer -> !offer.getId().equals(nonMatchingOffer.getId())).collect(Collectors.toList()));
				ctResponse.setTotal(ctResponse.getOffers().size());
			});
		}
	}

	public void filterOffersByModelNumber(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> nonMatchingOffers = new ArrayList<>();
		List<CTOffer> matchingOffers = new ArrayList<>();
		for(ProductInfo code:ctOfferRequest.getCartProducts()) {
			if (code != null && code.getManufacturer() != null && code.getModelNumber() != null) {
				if (ctResponse.getOffers() != null) {
					if (ctResponse.getOffers() != null) {
						for(CTOffer offer:ctResponse.getOffers() ) {
							if (offer != null && offer.getAttributes() != null && offer.getAttributes().getOfferProductType().equals("video-device")
									&& offer.getAttributes().getAssociatedProducts() != null) {
								List<Variant> variant= extractVariantFromBundleProduct(offer);
								if (CollectionUtils.isNotEmpty(variant) && variant.size() > 0) {
									Map<String, List<String>> models = getModels(variant.get(0));
									if (models != null && models.size() > 0 && models.keySet().contains(code.getManufacturer())
											&& models.get(code.getManufacturer()).contains(code.getModelNumber())) {
										matchingOffers.add(offer);
									} else {
										nonMatchingOffers.add(offer);
									}
								}
							}
						}
					}
				}
			}
		}
		if (nonMatchingOffers != null && nonMatchingOffers.size() > 0) {
			nonMatchingOffers = nonMatchingOffers.stream()
					.filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
			if (matchingOffers != null && matchingOffers.size() > 0) {
				matchingOffers =matchingOffers.stream()
						.filter(OffersUtils.distinctByKey(p -> p.getId())).collect(Collectors.toList());
				for(CTOffer matchingOffer:matchingOffers) {
					nonMatchingOffers = nonMatchingOffers.stream().filter(offer -> offer.getId() != matchingOffer.getId()).collect(Collectors.toList());
				}
			}
			if (ctOfferRequest.getCartOffers() != null && ctOfferRequest.getCartOffers().size() > 0) {
				for(String cartOffer:ctOfferRequest.getCartOffers()){
					nonMatchingOffers = nonMatchingOffers.stream().filter(offer -> !offer.getCode().equals(cartOffer)).collect(Collectors.toList());
				}
			}
			nonMatchingOffers.forEach(nonMatchingOffer -> {
				ctResponse.setOffers(ctResponse.getOffers().stream().filter(offer -> offer.getId() != nonMatchingOffer.getId()).collect(Collectors.toList()));
				ctResponse.setTotal(ctResponse.getOffers().size());
			});
		}
	}

	public Map<String, List<String>> getModels(Variant variant) {
		Map<String, List<String>> models = new HashMap<>();
		GenericLocaleBase modelNumbers = variant.getAttributes().getModelNumbers();
		if(Objects.nonNull(modelNumbers) && Objects.nonNull(modelNumbers.getEn())) {
			List<String> allModels = Stream.of(modelNumbers.getEn().split(Pattern.quote("||"))).map(String::trim).collect(Collectors.toList());
			allModels.stream().filter(Objects::nonNull).forEach(model -> {
				List<String> manModel = Stream.of(model.split(":")).map(String::trim).collect(Collectors.toList());
				List<String> modelNos = Stream.of(manModel.get(1).split(",")).map(String::trim).collect(Collectors.toList());
				models.put(manModel.get(0), modelNos);
			});
		}
		return models;
	}

	public String getComponentCodeFromBillingParams(Attributes attributes) {
		String componentCode = null;
		if (attributes != null && CollectionUtils.isNotEmpty(attributes.getBillingParams())
				&& attributes.getBillingParams().size()>0) {
			for(GenericNameValueBase billingParams: attributes.getBillingParams()) {
				if(billingParams.getName().equalsIgnoreCase("STBName")) {
					componentCode=billingParams.getValue();
				}
			}
		}
		return componentCode;
	}

	public String getOEMchannel(CTOfferRequest ctOfferRequest) {
		String oemChannel = null;
		if (ctOfferRequest.getIapPartnerAccountType() != null) {
			String iapPartnerAccountType = ctOfferRequest.getIapPartnerAccountType();
			if (iapPartnerAccountType.equals("FIRETV")) {
				oemChannel = "oemIAPFIRETV";
			} else if (iapPartnerAccountType.equals("ROKUTV")) {
				oemChannel = "oemIAPROKUTV";
			}
		} else {
			if (CollectionUtils.isNotEmpty(ctOfferRequest.getSalesChannel())
					&& ctOfferRequest.getSalesChannel().size() > 0) {
				String salesChannel = ctOfferRequest.getSalesChannel().get(0);
				if (salesChannel.contains("oemIAPFIRETV")) {
					oemChannel = "oemIAPFIRETV";
				} else if (salesChannel.contains("oemIAPROKUTV")) {
					oemChannel = "oemIAPROKUTV";
				}
			}
		}
		return oemChannel;
	}
	//26.1.5 - HOG - Added GENRE to the list of valid channels for price protection as per requirement
	//User Story 4531, 4533
	public static boolean isValidChannel(Price price) {
		List<String> validChannels = Arrays.asList("PP-Contract", "PP-EDSP", "PP-Non-Contract", "PP-TAZ",
				"PP-TAZCONTRACT", "PP-TAZBYOD", "PP-FRONTPORCH", "PP-RR", "PP-GENRE");
		return validChannels.contains(price.getChannel());
	}

	public void priceProtect(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (ctOfferRequest.getPriceProtection() != null) {
			if (CollectionUtils.isNotEmpty(ctOfferRequest.getContractIndicator())
					&& ctOfferRequest.getContractIndicator().size() > 0) {
				List<CTOffer> offers = ctResponse.getOffers();
				offers.stream().filter(Objects::nonNull).forEach(offer -> {
					if (offer != null && offer.getAttributes() != null
							&& offer.getAttributes().getAssociatedProducts() != null) {
						offer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
							if (associatedProduct.getBundleProducts() != null) {
								associatedProduct.getBundleProducts().forEach(bundleProduct -> {
									if (bundleProduct.getProducts() != null && bundleProduct.getProducts().size() > 0) {
										bundleProduct.getProducts().forEach(product -> {
											List<Price> filteredPrices = new ArrayList<>();
											priceProtectQualAndBundleProducts(ctOfferRequest, offer, product,
													filteredPrices);
										});
									}
								});
							}
							if (associatedProduct.getQualifyingProducts() != null) {
								associatedProduct.getQualifyingProducts().forEach(qualifyingProduct -> {
									if (qualifyingProduct.getProducts() != null
											&& qualifyingProduct.getProducts().size() > 0) {
										qualifyingProduct.getProducts().forEach(product -> {
											List<Price> filteredPrices = new ArrayList<>();
											priceProtectQualAndBundleProducts(ctOfferRequest, offer, product,
													filteredPrices);
										});
									}
								});
							}
						});
					}
				});
			}
		} else if (CollectionUtils.isEmpty(ctOfferRequest.getBusinessSegment())
			||  ((ctOfferRequest.getBusinessSegment().size() > 0)
			&& !ctOfferRequest.getBusinessSegment().get(0).equalsIgnoreCase("MDU"))) {
			List<CTOffer> offers = ctResponse.getOffers();
			offers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (offer != null && offer.getAttributes() != null
						&& offer.getAttributes().getAssociatedProducts() != null) {
					offer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
						if (associatedProduct.getBundleProducts() != null) {
							associatedProduct.getBundleProducts().forEach(bundleProduct -> {
								if (bundleProduct.getProducts() != null && bundleProduct.getProducts().size() > 0) {
									bundleProduct.getProducts().forEach(product -> {
										if(null != product.getObj()) {
										product.getObj().getVariants().stream().filter(Objects::nonNull)
										.forEach(variant -> {
											if (CollectionUtils.isNotEmpty(variant.getPrices())
													&& variant.getPrices().size() > 0) {
												extractCorrectPrice(ctOfferRequest, variant.getPrices(),
														product);
											}
										});
									}

									});
								}
							});
						}
						if (associatedProduct.getQualifyingProducts() != null) {
							associatedProduct.getQualifyingProducts().forEach(qualifyingProduct -> {
								if (qualifyingProduct.getProducts() != null
										&& qualifyingProduct.getProducts().size() > 0) {
									qualifyingProduct.getProducts().forEach(product -> {
										if(null != product.getObj()) {
										product.getObj().getVariants().stream().filter(Objects::nonNull)
										.forEach(variant -> {
											if (CollectionUtils.isNotEmpty(variant.getPrices())
													&& variant.getPrices().size() > 0) {
												extractCorrectPrice(ctOfferRequest, variant.getPrices(),
														product);
											}
										});
										}
									});
								}
							});
						}
					});
				}
			});
		}
	}

	public List<Price> extractCorrectPrice(CTOfferRequest ctOfferRequest, List<Price> listPrices, Product product) {
		List<Price> filteredPrices = new ArrayList<>();
		if (ctOfferRequest.getNextBillingDate() != null) {
			if (ctOfferRequest.getContractIndicator() != null) {
				List<Price> contractIndicatorPrices = listPrices.stream()
						.filter(item -> item.getContractIndicator() != null
						&& ctOfferRequest.getContractIndicator().contains(item.getContractIndicator()))
						.collect(Collectors.toList());
				List<Price> activePrices = getActivePrices(contractIndicatorPrices,
						ctOfferRequest.getNextBillingDate());
				filteredPrices = activePrices.stream().filter(item -> !isValidChannel(item))
						.collect(Collectors.toList());
			} else {
				filteredPrices = listPrices.stream().filter(item -> !isValidChannel(item)).collect(Collectors.toList());
			}
		} else {
			filteredPrices = listPrices.stream().filter(item -> !isValidChannel(item)).collect(Collectors.toList());
		}
		product.getObj().getVariants().get(0).setPrices(filteredPrices);
		return filteredPrices;
	}

	public void priceProtectQualAndBundleProducts(CTOfferRequest ctOfferRequest, CTOffer offer, Product product,
			List<Price> filteredPrices) {
		if (product.getObj() != null && product.getObj().getVariants().size() > 0) {
			if (product.getObj().getVariants().get(0).getPrices() != null
					&& product.getObj().getVariants().get(0).getPrices().size() > 0) {
				List<Price> listPrices = product.getObj().getVariants().get(0).getPrices();
				// Filtering the prices with matching contract indicator
				if (CollectionUtils.isNotEmpty(ctOfferRequest.getContractIndicator())
						&& ctOfferRequest.getContractIndicator().size() > 0) {
					List<Price> contractIndicatorPrices = new ArrayList<>();
					if (!(CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
							&& ctOfferRequest.getOfferProductFamily().size() > 0
							&& ctOfferRequest.getOfferProductFamily().contains("satellite"))) {
						// Filtering the prices with matching contract indicator
						contractIndicatorPrices = listPrices.stream()
								.filter(item -> item.getContractIndicator() != null
								&& ctOfferRequest.getContractIndicator().contains(item.getContractIndicator()))
								.collect(Collectors.toList());
					} else {
						contractIndicatorPrices = product.getObj().getVariants().get(0).getPrices();
					}

					if (ctOfferRequest.getPriceProtection() != null) {
						String nextBillingDate = ctOfferRequest.getPriceProtection().getNextBillingDate();
						List<Price> activePrices = getActivePrices(contractIndicatorPrices, nextBillingDate);
						String protectEtDt = ctOfferRequest.getPriceProtection().getEndDate();
						String protectStDt = ctOfferRequest.getPriceProtection().getStartDate();
						boolean  isValidPPEndDate = OffersUtils.isDateAfter(nextBillingDate, protectEtDt);
						if (!isValidPPEndDate) {
							boolean isValidNextBillingDate = OffersUtils.isDateSameOrAfter(protectEtDt, nextBillingDate);
							if (isValidNextBillingDate && offer.getAttributes().getContractIndicator() != null) {
								List<Price> filteredHogPrices = activePrices.stream().filter(item -> {
									if (offer.getAttributes().getContractIndicator()
											.contains(item.getContractIndicator())) {
										String hogStartDate = OffersUtils.getFormattedDate(item.getStartDate());
										if (ctOfferRequest.getOfferProductFamily() != null
												&& ctOfferRequest.getOfferProductFamily().contains("OTT")) {
											return isValidChannel(item)
													&& OffersUtils.isDateSameOrAfter(hogStartDate, protectStDt);
										}
										return isValidChannel(item)
												&& OffersUtils.isDateAfter(hogStartDate, protectStDt);
									} else {
										return !isValidChannel(item);
									}
								}).collect(Collectors.toList());

								if (filteredHogPrices.size() > 0) {
									filteredPrices = filteredHogPrices;
								} else {
									filteredPrices = activePrices.stream().filter(item -> !isValidChannel(item))
											.collect(Collectors.toList());
								}
							} else {
								filteredPrices = activePrices.stream().filter(item -> !isValidChannel(item))
										.collect(Collectors.toList());
							}
						} else {
							filteredPrices = activePrices.stream().filter(item -> !isValidChannel(item))
									.collect(Collectors.toList());
						}
					} else {
						filteredPrices = contractIndicatorPrices.stream().filter(item -> !isValidChannel(item))
								.collect(Collectors.toList());
					}
				} else {
					filteredPrices = listPrices.stream().filter(item -> !isValidChannel(item))
							.collect(Collectors.toList());
				}
				product.getObj().getVariants().get(0).setPrices(filteredPrices);
			}
		}
	}
	public List<Price> getActivePrices(List<Price> contractIndicatorPrices, String currentDate) {
		return contractIndicatorPrices.stream().filter(item -> {
			String startDate = OffersUtils.getFormattedDate(item.getStartDate());
			String endDate = OffersUtils.getFormattedDate(item.getEndDate());
			return OffersUtils.validateActiveNBCDDates(startDate, endDate, currentDate);
		}).collect(Collectors.toList());

	}

	public void filterCorrectPrice(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		if (ctOfferRequest.getContractIndicator()!=null && ctOfferRequest.getContractIndicator().size() == 1) {
			String reqIndicator = ctOfferRequest.getContractIndicator().get(0);
			ctResponse.getOffers().forEach(offer -> {
				if (offer.getAttributes().getAssociatedProducts()!=null && offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()!=null) {
					if (offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()!=null) {
						List<Product> listProduct = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts();
						removeContractNoncontractPrice(listProduct, reqIndicator);
					}
				} else if (offer.getAttributes().getAssociatedProducts()!=null && offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts()!=null) {
					List<Product> listProduct = OffersUtils.getQualifyingProductsFromAssociatedProducts(offer.getAttributes().getAssociatedProducts());
						if (Objects.nonNull(listProduct) && !listProduct.isEmpty()){
							removeContractNoncontractPrice(listProduct, reqIndicator);
						}
				}
			});
		}
	}

	public void removeContractNoncontractPrice(List<Product> listProduct, String reqIndicator) {
		List<Variant> listPriceVariant;
		List<Price> listPrices;
		for(Product product : listProduct) {
			if(product.getObj() != null && product.getObj().getVariants() != null) {
				listPriceVariant = product.getObj().getVariants();
				for(Variant variant : listPriceVariant) {
					listPrices = variant.getPrices();
					if (null != listPrices) {
						for (int i = 0; i < listPrices.size(); i++) {
							String priceIndicator = listPrices.get(i).getContractIndicator();
							if (reqIndicator.equals("non-contract") && priceIndicator.equals("contract")) {
								listPrices.remove(i);
								i--;
							} else if (reqIndicator.equals("contract") && priceIndicator.equals("non-contract")) {
								listPrices.remove(i);
								i--;
							}
						}
					}
				}
			}
		}
	}

	public void populateCartAndCustomerContextProducts(CTOfferRequest ctOfferRequest,List<String> requestCustProductFamily,
			List<com.dtv.dcp.epoch.model.ct.request.Product> requestCustProducts,List<String> requestCartProductFamily,
			List<com.dtv.dcp.epoch.model.ct.request.Product> requestCartProducts) {
		if (ctOfferRequest.getCustomerContext() != null) {
			ctOfferRequest.getCustomerContext().forEach(eachCustContext -> {
				if (eachCustContext.getProductFamily() != null) {
					requestCustProductFamily.add(eachCustContext.getProductFamily().toLowerCase());
				}
				if (eachCustContext.getProducts() != null) {
					eachCustContext.getProducts().forEach(product -> {
						if (product.getProductId() != null ||
								product.getProductCode() != null ||
								product.getProductType() != null ||
								product.getBillingCode() != null ||
								product.getBillingProductCode() != null) {
							requestCustProducts.add(product);
						}
					});
				}
			});
		}
		if (ctOfferRequest.getCartContext() != null && ctOfferRequest.getCartContext().getLineItems() != null) {
			ctOfferRequest.getCartContext().getLineItems().forEach(eachlineItem -> {
				if (eachlineItem.getProductFamily() != null) {
					requestCartProductFamily.add(eachlineItem.getProductFamily().toLowerCase());
				}
				if (eachlineItem.getProducts() != null) {
					eachlineItem.getProducts().forEach(product -> {
						if (product.getProductId() != null ||
								product.getProductCode() != null ||
								product.getProductType() != null ||
								product.getBillingCode() != null ||
								product.getBillingProductCode() != null) {
							requestCartProducts.add(product);
						}
					});
				}
			});
		}
	}

	/*
	 * Can be refactored. Copied over as is from node js
	 */
	public void deliveryMethodByChannelFilterOffer(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<String> defaultCustomerSegment = new ArrayList<>(Arrays.asList("Residential"));
		List<String> defaultPartnerType = new ArrayList<>(Arrays.asList("Default"));
		List<String> reqCustomerSegment = defaultCustomerSegment;
		List<String> reqPartnerType = defaultPartnerType;
		if (CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerSegments())
				&& ctOfferRequest.getCustomerSegments().size()>0) {
			reqCustomerSegment = ctOfferRequest.getCustomerSegments();
		}
		if (ctOfferRequest.getPartnerType()!=null) {
			reqPartnerType = ctOfferRequest.getPartnerType();
		}
		for (CTOffer offer : ctResponse.getOffers()) {
			List<Variant> variants = extractVariantFromBundleProduct(offer);
			if (CollectionUtils.isNotEmpty(variants) && variants.size() > 0) {
				Variant variant = variants.get(0);
				if (variant.getAttributes() != null && variant.getAttributes().getDeliveryMethodByChannel() != null) {
					List<DeliveryMethodAttributes> filteredDeliveryMethodAttributes = new ArrayList<>();
					DeliveryMethod deliveryMethodByChannel = variant.getAttributes().getDeliveryMethodByChannel();
					if(CollectionUtils.isNotEmpty(deliveryMethodByChannel.getDeliveryMethod())
							&& deliveryMethodByChannel.getDeliveryMethod().size()>0) {
						for (DeliveryMethodAttributes deliveryMethod : deliveryMethodByChannel.getDeliveryMethod()) {
							if(deliveryMethod.getSalesChannel().equalsIgnoreCase(ctOfferRequest.getSalesChannel().get(0))) {
								filteredDeliveryMethodAttributes.add(deliveryMethod);
							}
						}
						if (filteredDeliveryMethodAttributes.size() > 0) {
							variant.getAttributes().getDeliveryMethodByChannel().setDeliveryMethod(filteredDeliveryMethodAttributes);
						}
						filteredDeliveryMethodAttributes = new ArrayList<>();
						for (DeliveryMethodAttributes deliveryMethod : deliveryMethodByChannel.getDeliveryMethod()) {
							if( CollectionUtils.isNotEmpty(reqCustomerSegment) && reqCustomerSegment.size()>0
									&& deliveryMethod.getCustomerSegments().equals(reqCustomerSegment.get(0))) {
								filteredDeliveryMethodAttributes.add(deliveryMethod);
							}
						}
						if (filteredDeliveryMethodAttributes.size() > 0) {
							variant.getAttributes().getDeliveryMethodByChannel().setDeliveryMethod(filteredDeliveryMethodAttributes);
						}
						filteredDeliveryMethodAttributes = new ArrayList<>();
						for (DeliveryMethodAttributes deliveryMethod : deliveryMethodByChannel.getDeliveryMethod()) {
							if( CollectionUtils.isNotEmpty(reqPartnerType) && reqPartnerType.size()>0
									&& StringUtils.isNotEmpty(deliveryMethod.getPartnerType())  &&  deliveryMethod.getPartnerType().contains(reqPartnerType.get(0))) {
								filteredDeliveryMethodAttributes.add(deliveryMethod);
							}
						}
						if (CollectionUtils.isEmpty(filteredDeliveryMethodAttributes) && filteredDeliveryMethodAttributes.size() == 0) {
							for (DeliveryMethodAttributes deliveryMethod : deliveryMethodByChannel.getDeliveryMethod()) {
								if( CollectionUtils.isNotEmpty(defaultPartnerType) && defaultPartnerType.size()>0
										&& StringUtils.isNotEmpty(deliveryMethod.getPartnerType()) 	&& deliveryMethod.getPartnerType().contains("Default")) {
									filteredDeliveryMethodAttributes.add(deliveryMethod);
								}
							}
						}
						if (filteredDeliveryMethodAttributes.size() > 0) {
							variant.getAttributes().getDeliveryMethodByChannel().setDeliveryMethod(filteredDeliveryMethodAttributes);
						}
					}
				}
			}
		}
	}

	public void filterResponseBasedOnRequest(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> offers = ctResponse.getOffers();
		Map<String, List<String>> requestAttr = new HashMap<>();
		Map<String, List<String>> requestMandatoryAttr = new HashMap<>();
		List<CTOffer> nonMatchingOffers = new ArrayList<>();
		collectRequestAttributes(ctOfferRequest, requestAttr);
		collectMandatoryRequestAttributes(ctOfferRequest, requestMandatoryAttr);
		if(!requestAttr.isEmpty() || !requestMandatoryAttr.isEmpty()) {
			offers.forEach(offer -> {
				if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()) {
					if(!requestAttr.isEmpty()) {
						for(Map.Entry<String, List<String>> attr : requestAttr.entrySet()) {
							checkCollectionAttributesNew(attr, nonMatchingOffers, offer,false);
						}
					}
					if(!requestMandatoryAttr.isEmpty()) {
						for(Map.Entry<String, List<String>> attr : requestMandatoryAttr.entrySet()) {
							checkCollectionAttributesNew(attr, nonMatchingOffers, offer,true);
						}
					}
				}
			});
		}
		if (CollectionUtils.isNotEmpty(nonMatchingOffers)) {
			List<CTOffer> nonMatchingOffersTemp = nonMatchingOffers.stream()
					.filter(OffersUtils.distinctByKey(CTOffer::getId)).collect(Collectors.toList());
			for (CTOffer nonMatchingOffer : nonMatchingOffersTemp) {
				ctResponse.setOffers(ctResponse.getOffers().stream()
						.filter(of -> !of.getId().equals(nonMatchingOffer.getId())).collect(Collectors.toList()));
				ctResponse.setCount(ctResponse.getOffers().size());
				ctResponse.setTotal(ctResponse.getTotal() - nonMatchingOffers.size());
			}
		}

	}

	private void checkCollectionAttributesNew(Map.Entry<String, List<String>>attrValues, List<CTOffer> nonMatchingOffers, CTOffer offer, boolean isMandatoryCheck) {
		List<String> attributeList = new ArrayList<>();
		switch(attrValues.getKey()) {
		case "billingProductCodes":
			attributeList = offer.getAttributes().getQualifyingProductsBillingProductCode();
			break;
		case "migrationServiceType":
			attributeList = offer.getAttributes().getMigrationServiceType();
			break;
		case "planSubType":
			attributeList = offer.getAttributes().getQualifyingProductsPlanSubTypes();
			break;
		case "creditRisk":
			attributeList = offer.getAttributes().getCreditRisk();
			break;
		case "customerSubType":
			attributeList = offer.getAttributes().getCustomerSubtypes();
			break;
		case "offerClassificationType":
			attributeList = offer.getAttributes().getOfferClassificationType()!=null ?
						Arrays.asList(offer.getAttributes().getOfferClassificationType()):null;
			break;
		case "offerIds":
			attributeList = offer.getId()!=null ? Arrays.asList(offer.getId()):null;
			break;
		case "offerType":
			attributeList = offer.getAttributes().getOfferType()!=null ?
						Arrays.asList(offer.getAttributes().getOfferType()):null;
			break;
		case "qualifyingProducts":
			if(CollectionUtils.isNotEmpty(offer.getAttributes().getQualifyingProductIds())){
				attributeList =offer.getAttributes().getQualifyingProductIds().stream()
						.map(Product::getKey).collect(Collectors.toList());
			}
			break;
		case "bundleProducts":
			if(CollectionUtils.isNotEmpty(offer.getAttributes().getBundleProductIds())){
				attributeList =offer.getAttributes().getBundleProductIds().stream()
						.map(Product::getKey).collect(Collectors.toList());
			}
			break;
		case "customerType":
			attributeList = offer.getAttributes().getCustomerTypes()!=null ?
					offer.getAttributes().getCustomerTypes():null;
			break;
		}

		if (isNonMatchingOffers(attrValues, isMandatoryCheck, attributeList)) {
			nonMatchingOffers.add(offer);
		}
	}

	private boolean isNonMatchingOffers(Map.Entry<String, List<String>> attrValues, boolean isMandatoryCheck,
			List<String> attributeList) {
		return (isMandatoryCheck && CollectionUtils.isNotEmpty(attrValues.getValue())
				&& (CollectionUtils.isEmpty(attributeList)
						|| attrValues.getValue().stream().noneMatch(value ->
				attributeList.stream().anyMatch(attr -> attr.equalsIgnoreCase(value)))))
				|| (CollectionUtils.isNotEmpty(attributeList)
						&& CollectionUtils.isNotEmpty(attrValues.getValue())
						&& attrValues.getValue().stream().noneMatch(value ->
				attributeList.stream().anyMatch(attr -> attr.equalsIgnoreCase(value))));
	}

	public void collectMandatoryRequestAttributes(CTOfferRequest ctOfferRequest, Map<String, List<String>> mandatoryRequestAttr) {
		Map<String, Supplier<List<String>>> attributeSuppliers = new HashMap<>();
	    attributeSuppliers.put("customerType", ctOfferRequest::getCustomerType);
	    for (Map.Entry<String, Supplier<List<String>>> entry : attributeSuppliers.entrySet()) {
	        List<String> values = entry.getValue().get();
	        if (CollectionUtils.isNotEmpty(values)) {
	        	mandatoryRequestAttr.put(entry.getKey(), values);
	        }
	    }
	}

	public void collectRequestAttributes(CTOfferRequest ctOfferRequest, Map<String, List<String>> requestAttr) {
		Map<String, Supplier<List<String>>> attributeSuppliers = new HashMap<>();

	    attributeSuppliers.put("billingProductCodes", ctOfferRequest::getBillingProductCodes);
	    attributeSuppliers.put("offerClassificationType", ctOfferRequest::getOfferClassificationType);
	    attributeSuppliers.put("offerIds", ctOfferRequest::getOfferIds);
	    attributeSuppliers.put("offerType", ctOfferRequest::getOfferType);
	    attributeSuppliers.put("migrationServiceType", ctOfferRequest::getMigrationServiceType);
	    attributeSuppliers.put("qualifyingProducts", ctOfferRequest::getQualifyingProducts);
	    attributeSuppliers.put("planSubType", ctOfferRequest::getPlanSubType);
	    attributeSuppliers.put("bundleProducts", ctOfferRequest::getBundleProducts);
	    attributeSuppliers.put("offerStatus", ctOfferRequest::getOfferStatus);
	    attributeSuppliers.put("customerSubType", ctOfferRequest::getCustomerSubType);
	    if (ctOfferRequest.getCreditRisk() != null) {
	        attributeSuppliers.put("creditRisk", () -> Arrays.asList(ctOfferRequest.getCreditRisk()));
	    }
	    for (Map.Entry<String, Supplier<List<String>>> entry : attributeSuppliers.entrySet()) {
	        List<String> values = entry.getValue().get();
	        if (CollectionUtils.isNotEmpty(values)) {
	            requestAttr.put(entry.getKey(), values);
	        }
	    }

	}

	public CTOfferResponse applyVCMiddlewareFilters(CTOfferResponse ctResponse, CTOfferRequest ctOfferRequest) {
		List<CTOffer> offers = ctResponse.getOffers();

		if (CollectionUtils.isEmpty(offers)) {
			return ctResponse;
		}
		List<String> qualifyingProductBillingCodes = ctResponse.getSpecialOfferCartModeCodes();
		offers.stream().filter(Objects::nonNull).forEach(offer -> {
			if (null !=  ctOfferRequest.getCartContext()
					&& ((null !=  ctOfferRequest.getCartContext().getOfferIds() && !ctOfferRequest.getCartContext().getOfferIds().isEmpty())
					|| (null !=  ctOfferRequest.getCartContext().getOfferCodes() && !ctOfferRequest.getCartContext().getOfferCodes().isEmpty()))) {
				populateReconnectOffers(ctResponse, ctOfferRequest, qualifyingProductBillingCodes,offer);
			}
			/*
			populateInstallmentCreditRisk(ctOfferRequest, offer);
			populateInstallmentAndMinMaxQuantityEDSP(ctOfferRequest, offer);
			populateOEMDescriptionsByKey(ctOfferRequest, offer);
			removeComplatibleProductsFromResponse(ctOfferRequest, offer);
			removeinstallmentListForEmployee(ctOfferRequest, offer);
			*/
		});
		if (ctResponse != null && ctOfferRequest != null) {
			Optional.ofNullable(ctOfferRequest.getOfferProductFamily())
					.filter(CollectionUtils::isNotEmpty)
					.map(family -> family.get(0))
					.filter(Constants.OTT_PRODUCT_FAMILY::equalsIgnoreCase)
					.ifPresent(family -> offersUtils.filterOffersBasedOnCustomerType(ctResponse, ctOfferRequest));
		}
		filterResponseBasedOnRequest(ctResponse,ctOfferRequest);
		evaluateRewardCards(ctResponse,ctOfferRequest);
		filterOffersForDirectIntegrationPartners(ctResponse, ctOfferRequest);
		evaluateBenefitsCodestoSuppressTheOffer(ctResponse, ctOfferRequest);
		//priceProtect(ctResponse,ctOfferRequest);
		returnValidOpusStoreOffers(ctResponse,ctOfferRequest);
		returnValidAgentOffers(ctResponse,ctOfferRequest);
		returnValidOnlinePartnerOffers(ctResponse,ctOfferRequest);
		returnValidPartnerDealersOffers(ctResponse,ctOfferRequest);
		removeIneligiblePartnerOffers(ctResponse,ctOfferRequest);
		removeIneligibleAccountStatus(ctResponse,ctOfferRequest);
		//filterDeviceOffersBasedOnMigrationIndicator(ctResponse,ctOfferRequest);
		returnValidPartnerTypeOffers(ctResponse,ctOfferRequest);
		productsOnAccountToSupressOffer(ctResponse,ctOfferRequest);
		applyCartContextFilter(ctResponse,ctOfferRequest);
		//filterCorrectPrice(ctResponse,ctOfferRequest);
		//applySatelliteCartOffersFilter(ctResponse,ctOfferRequest);
		//deliveryMethodByChannelFilterOffer(ctResponse,ctOfferRequest);
		/*
		if(CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& ctOfferRequest.getOfferProductFamily().contains(Constants.SATELLITE_PRODUCT_FAMILY)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType())
				&& (Objects.isNull(ctOfferRequest.getAdeRequest()) || ctOfferRequest.getAdeRequest() == false)) {
			OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
			offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
			satelliteCTOffersProcessor.filterOffersBasedOnDMA(offerRequestWrapper,ctResponse);
		}
		 */

        filterIncludeProductsBasedOnSalesChannelORIapPartnerAccountType(ctResponse, ctOfferRequest);
		filterConflictingProductsBasedOnSalesChannelORIapPartnerAccountType(ctResponse, ctOfferRequest);

		if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_MDU_INCLUDED_PRODUCT_EXCLUSIONS)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
				&& ctOfferRequest.getOfferProductFamily().stream()
				.anyMatch(Constants.OTT::equalsIgnoreCase)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType())
				&& ctOfferRequest.getOfferProductType().stream()
				.anyMatch(Constants.VIDEO_PLAN::equalsIgnoreCase)
				&& CollectionUtils.isNotEmpty(ctOfferRequest.getBusinessSegment())
				&& ctOfferRequest.getBusinessSegment().stream()
				.anyMatch(Constants.MDU::equalsIgnoreCase)) {
			filterIncludedProductsFromOffer(ctResponse);
		}
		return ctResponse;
	}

	public void filterIncludedProductsFromOffer(CTOfferResponse ctOfferResponse) {
		List<String> mduIncludedProductExclusions = offersUtils.getConfigList(Constants.MDU_INCLUDED_PRODUCT_EXCLUSIONS);

		ctOfferResponse.getOffers().stream()
				.filter(this::hasVideoPlanAssociatedProducts)
				.forEach(offer -> processAssociatedProducts(offer, mduIncludedProductExclusions));
	}

	private boolean hasVideoPlanAssociatedProducts(CTOffer offer) {
		return Optional.ofNullable(offer.getAttributes())
				.map(OfferAttributes::getAssociatedProducts)
				.filter(CollectionUtils::isNotEmpty)
				.map(associatedProducts -> associatedProducts.stream().anyMatch(this::isVideoPlanProduct))
				.orElse(false);
	}

	private boolean isVideoPlanProduct(AssociatedProduct associatedProduct) {
		return hasVideoPlan(associatedProduct.getQualifyingProducts()) || hasVideoPlan(associatedProduct.getBundleProducts());
	}

	private boolean hasVideoPlan(List<ProductWrapper> productWrappers) {
		return Optional.ofNullable(productWrappers)
				.filter(CollectionUtils::isNotEmpty)
				.map(wrappers -> wrappers.stream().anyMatch(wrapper -> Optional.ofNullable(wrapper.getProducts())
						.filter(CollectionUtils::isNotEmpty)
						.map(products -> products.stream().anyMatch(product -> Optional.ofNullable(product.getObj())
								.map(obj -> obj.getProductType())
								.map(type -> Constants.VIDEO_PLAN.equalsIgnoreCase(type.getKey()))
								.orElse(false)))
						.orElse(false)))
				.orElse(false);
	}

	private void processAssociatedProducts(CTOffer offer, List<String> mduIncludedProductExclusions) {
		Optional.ofNullable(offer.getAttributes())
				.map(OfferAttributes::getAssociatedProducts)
				.filter(CollectionUtils::isNotEmpty)
				.ifPresent(associatedProducts -> associatedProducts.forEach(associatedProduct -> {
					processIncludedProducts(associatedProduct.getQualifyingProducts(), mduIncludedProductExclusions);
					processIncludedProducts(associatedProduct.getBundleProducts(), mduIncludedProductExclusions);
				}));
	}

	private void processIncludedProducts(List<ProductWrapper> productWrappers, List<String> mduIncludedProductExclusions) {
		Optional.ofNullable(productWrappers)
				.filter(CollectionUtils::isNotEmpty)
				.ifPresent(wrappers -> wrappers.forEach(wrapper -> Optional.ofNullable(wrapper.getProducts())
						.filter(CollectionUtils::isNotEmpty)
						.ifPresent(products -> products.forEach(product -> processProductAttributes(product, mduIncludedProductExclusions)))));
	}

	private void processProductAttributes(Product product, List<String> mduIncludedProductExclusions) {
		Optional.ofNullable(product.getObj())
				.map(ProductObj::getVariants)
				.filter(CollectionUtils::isNotEmpty)
				.ifPresent(variants -> Optional.ofNullable(variants.get(0).getAttributes())
						.ifPresent(attributes -> {
							if (isMDUBusinessSegment(attributes)) {
								cleanIncludedProducts(attributes, mduIncludedProductExclusions);
							}
						}));
	}

	private boolean isMDUBusinessSegment(Attributes attributes) {
		return Constants.MDU.equalsIgnoreCase(Optional.ofNullable(attributes.getBusinessSegment())
				.filter(CollectionUtils::isNotEmpty)
				.map(segment -> segment.get(0))
				.orElse(null));
	}

	private void cleanIncludedProducts(Attributes attributes, List<String> mduIncludedProductExclusions) {
		List<IncludeProductWrapper> includedProducts = attributes.getIncludedProducts();
		if (CollectionUtils.isNotEmpty(includedProducts)) {
			includedProducts.removeIf(includedProduct -> {
				if (includedProduct.getProducts() != null) {
					includedProduct.getProducts().removeIf(productObj ->
							mduIncludedProductExclusions.stream()
									.anyMatch(exclusion -> exclusion.equalsIgnoreCase(productObj.getKey())));
					if (CollectionUtils.isEmpty(includedProduct.getProducts())) {
						includedProduct.setProducts(null);
					}
				}
				return includedProduct.getProducts() == null;
			});
		}
		if (CollectionUtils.isEmpty(includedProducts)) {
			attributes.setIncludedProducts(null);
		}
	}

    private void filterIncludeProductsBasedOnSalesChannelORIapPartnerAccountType(CTOfferResponse ctOfferResponse, CTOfferRequest ctOfferRequest) {
        if (CollectionUtils.isNotEmpty(ctOfferResponse.getOffers()) && featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_INCLUDE_PRODUCT_FILTERS)) {
            ctOfferResponse.getOffers().forEach(offer -> Optional.ofNullable(offer.getAttributes())
                    .map(OfferAttributes::getAssociatedProducts)
                    .filter(CollectionUtils::isNotEmpty)
                    .ifPresent(associatedProducts -> associatedProducts.forEach(associatedProduct -> {
                        filterIncludedProductsBySalesChannel(associatedProduct.getQualifyingProducts(), ctOfferRequest.getSalesChannel(), ctOfferRequest.getIapPartnerAccountType());
                        filterIncludedProductsBySalesChannel(associatedProduct.getBundleProducts(), ctOfferRequest.getSalesChannel(), ctOfferRequest.getIapPartnerAccountType());
                    })));
        }
    }

    private void filterIncludedProductsBySalesChannel(List<ProductWrapper> productWrappers, List<String> salesChannels, String iapPartnerAccountType) {
        if (CollectionUtils.isEmpty(productWrappers)) {
            return;
        }
        productWrappers.stream()
                .filter(pw -> CollectionUtils.isNotEmpty(pw.getProducts()))
                .flatMap(pw -> pw.getProducts().stream())
                .filter(product -> Objects.nonNull(product.getObj()) && CollectionUtils.isNotEmpty(product.getObj().getVariants()))
                .flatMap(product -> product.getObj().getVariants().stream())
                .filter(variant -> Objects.nonNull(variant.getAttributes()) && CollectionUtils.isNotEmpty(variant.getAttributes().getIncludedProducts()))
                .forEach(variant -> OffersUtils.filterAndSetIncludedProducts(variant, salesChannels, iapPartnerAccountType));
    }
	private void filterConflictingProductsBySalesChannel(List<ProductWrapper> productWrappers, List<String> salesChannels, String iapPartnerAccountType) {
		if (CollectionUtils.isEmpty(productWrappers)) {
			return;
		}
		productWrappers.stream()
				.filter(pw -> CollectionUtils.isNotEmpty(pw.getProducts()))
				.flatMap(pw -> pw.getProducts().stream())
				.filter(product -> Objects.nonNull(product.getObj()) && CollectionUtils.isNotEmpty(product.getObj().getVariants()))
				.flatMap(product -> product.getObj().getVariants().stream())
				.filter(variant -> Objects.nonNull(variant.getAttributes()) && CollectionUtils.isNotEmpty(variant.getAttributes().getConflictingProductsReference()))
				.forEach(variant -> {
					OffersUtils.filterAndSetConflictingProducts(variant,salesChannels,iapPartnerAccountType);
				});

	}
	private void filterConflictingProductsBasedOnSalesChannelORIapPartnerAccountType(CTOfferResponse ctOfferResponse, CTOfferRequest ctOfferRequest) {
		if (CollectionUtils.isNotEmpty(ctOfferResponse.getOffers()) && featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS)) {
			ctOfferResponse.getOffers().forEach(offer -> Optional.ofNullable(offer.getAttributes())
					.map(OfferAttributes::getAssociatedProducts)
					.filter(CollectionUtils::isNotEmpty)
					.ifPresent(associatedProducts -> associatedProducts.forEach(associatedProduct -> {
						filterConflictingProductsBySalesChannel(associatedProduct.getQualifyingProducts(), ctOfferRequest.getSalesChannel(), ctOfferRequest.getIapPartnerAccountType());
						filterConflictingProductsBySalesChannel(associatedProduct.getBundleProducts(), ctOfferRequest.getSalesChannel(), ctOfferRequest.getIapPartnerAccountType());
					})));
		}
	}

}
