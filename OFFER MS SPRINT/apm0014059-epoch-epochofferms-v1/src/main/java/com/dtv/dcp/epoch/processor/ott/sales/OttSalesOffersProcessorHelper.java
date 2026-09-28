package com.dtv.dcp.epoch.processor.ott.sales;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.DtvnMidasRule;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.DependentOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferPrice;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.InstallmentInfo;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTErrorResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;


/**
 * The Class OttSalesOffersProcessorHelper.
 */
/**
 * @author vc402t
 *
 */
@Component
public class OttSalesOffersProcessorHelper {

	/** The offers utils. */
	@Autowired
	OffersUtils offersUtils;

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(OttSalesOffersProcessorHelper.class);
	@Autowired
	private FeatureManagerHelper featureHelper;

	@Autowired
	private CustomerGraphService customerGraphService;

	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;

	@Autowired
	private DMALookUpService dmaLookUpService;
	/**
	 * Exclude virtual offers.
	 *
	 * @param offers the offers
	 * @return the list
	 */
	public List<CTOffer> excludeVirtualOffers(List<CTOffer> offers) {
		List<CTOffer> finalOfferList = offers;
		if (Optional.ofNullable(finalOfferList).isPresent()) {
			finalOfferList = finalOfferList.stream().filter(Objects::nonNull).filter(offer -> !offer.getAttributes().isVirtualOffer()).collect(Collectors.toList());
		}
		return finalOfferList;
	}
	/**
	 * Filter virtual qualifier promos.
	 *
	 * @param offers the offers
	 */
	public void filterVirtualQualifierPromos(List<CTOffer> offers) {

		LinkedHashSet<String> virtualPromo = new LinkedHashSet<>();
		try {
			if (Optional.ofNullable(offers).isPresent()) {
				offers.stream().filter(Objects::nonNull).forEach(offer -> {

					if (offer.getAttributes().isVirtualOffer()) {
						if (Objects.nonNull(offer.getAttributes().getBenefits())) {
							offer.getAttributes().getBenefits().stream().forEach(promo ->
							{
								if (StringUtils.isNotBlank(promo.getBillingBenefitCode())) {
									virtualPromo.add(promo.getBillingBenefitCode());
								}

							});
						}
					}
				});

				if (!virtualPromo.isEmpty()) {
					// Filter Virtual Add on Promo from non-virtual add on offers.
					offers.stream().filter(Objects::nonNull).forEach(offer -> {

						if (!offer.getAttributes().isVirtualOffer()) {
							if (Objects.nonNull(offer.getAttributes().getBenefits())) {
								offer.getAttributes().setBenefits(
										offer.getAttributes().getBenefits().stream().filter(promo ->
												!virtualPromo.contains(promo.getBillingBenefitCode())
										).collect(Collectors.toList())
								);
							}
						}
					});
				}
			}
		} catch (Exception e) {
			log.error(String.format("filterVirtualQualifierPromos: %s", e.getMessage()));
		}
	}

	/**
	 * Filter expired installment options.
	 *
	 * @param offers the offers
	 */
	public void filterExpiredInstallmentOptions(List<CTOffer> offers) {

		try {

			if (Optional.ofNullable(offers).isPresent()) {
				offers.stream().filter(Objects::nonNull).forEach(offer -> {
					if (Objects.nonNull(offer.getAttributes().getAssociatedProducts())
							&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0))
							&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
							&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))) {
						List<Product> products = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts();
						products.stream().filter(Objects::nonNull).forEach(product ->
								product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {

									if (Objects.nonNull(variant.getAttributes()) && Objects.nonNull(variant.getAttributes().getInstallmentList())) {
										List<InstallmentInfo> installmentInfo = variant.getAttributes().getInstallmentList();
										installmentInfo = installmentInfo.stream().filter(list ->
												(list.getInstallmentStartDate() != null && list.getInstallmentEndDate() != null
														&& OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(list.getInstallmentStartDate()),
														OffersUtils.getFormattedDate(list.getInstallmentEndDate())))).collect(Collectors.toList());
										variant.getAttributes().setInstallmentList(installmentInfo);
									}
								})
						);
					}
				});
			}
		} catch (Exception e) {
			log.error(String.format("filterExpiredInstallmentOptions: %s", e.getMessage()));
		}

	}

	/**
	 * Filter max plus for opus.
	 *
	 * @param offers the offers
	 * @param offerRequestWrapper the offer request wrapper
	 * @return the list
	 */
	public List<CTOffer> filterMaxPlusForOpus(List<CTOffer> offers,  OfferRequestWrapper offerRequestWrapper) {

		if(offerRequestWrapper.getOfferRequest().getSalesChannel().stream().anyMatch(s -> s.equalsIgnoreCase("opus")))
		{
			List<CTOffer> filteredList = new ArrayList<>();
			try {

				if (Optional.ofNullable(offers).isPresent()) {
					offers.stream().filter(Objects::nonNull).forEach(offer -> {
						if (Objects.nonNull(offer.getAttributes().getAssociatedProducts())
								&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0))
								&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
								&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)))
						{
							List<Product> products = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts();
							products.stream().filter(Objects::nonNull).forEach(product -> {
								if (!(product.getKey().equalsIgnoreCase("BASE-PLUS-2018") || product.getKey().equalsIgnoreCase("BASE-MAX-2018"))) {
									filteredList.add(offer);
								}
							});

						}
					});
				}
			} catch (Exception e) {
				log.error(String.format("filterMaxPlusForOpus: %s", e.getMessage()));
				log.debug(String.format("Offers:: %s", offersUtils.sanitizeData(offers)));
			}
			return filteredList;
		}else{
			return offers;
		}
	}

	/**
	 * Populate compatible products.
	 *
	 * @param offers the offers
	 */
	public void populateCompatibleProducts(List<CTOffer> offers) {

		try {
			if (Optional.ofNullable(offers).isPresent()) {

				offers.stream().filter(Objects::nonNull).forEach(offer -> {

					if (Objects.nonNull(offer.getAttributes().getAssociatedProducts())
							&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0))
							&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
							&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))
					) {
						List<Product> products = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts();

						products.stream().filter(Objects::nonNull).forEach(product ->
								product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
									if (Objects.nonNull(offer.getAttributes().getEligibility())
											&& Objects.nonNull(offer.getAttributes().getEligibility().getConstraints())
											&& Objects.nonNull(
											offer.getAttributes().getEligibility().getConstraints().get(0))) {
										if (Constants.RESIDENTIAL_ACCOUNT_TYPE.equalsIgnoreCase(offer.getAttributes()
												.getEligibility().getConstraints().get(0).getCustomerSegment())) {

											// Residential offer: Setting CompatibleMobilityProducts to Null
											variant.getAttributes().setCompatibleMobilityProducts(null);
											variant.getAttributes().setIncludedMobilityProducts(null);

											variant.getAttributes().setCompatibleEmployeeProducts(null);
										} else if (Constants.MOBILITY.equalsIgnoreCase(offer.getAttributes()
												.getEligibility().getConstraints().get(0).getCustomerSegment())) {

											// Mobility offer: Setting CompatibleProducts to CompatibleMobilityProducts
											variant.getAttributes().setCompatibleProducts(variant.getAttributes().getCompatibleMobilityProducts());

											// Mobility offer: Setting CompatibleMobilityProducts to Null (After CompatibleProducts are updated)
											variant.getAttributes().setCompatibleMobilityProducts(null);
											variant.getAttributes().setCompatibleEmployeeProducts(null);
											variant.getAttributes().setIncludedProducts(variant.getAttributes().getIncludedMobilityProducts());
											variant.getAttributes().setIncludedMobilityProducts(null);
										}else if (Constants.EMPLOYEE.equalsIgnoreCase(offer.getAttributes()
												.getEligibility().getConstraints().get(0).getCustomerSegment())) {

											// Employee offer: Setting CompatibleEmployeeProducts to CompatibleProducts
											variant.getAttributes().setCompatibleProducts(variant.getAttributes().getCompatibleEmployeeProducts());

											// Employee offer: Setting CompatibleEmployeeProducts to Null (After CompatibleProducts are updated)
											variant.getAttributes().setCompatibleEmployeeProducts(null);
											variant.getAttributes().setIncludedMobilityProducts(null);
											variant.getAttributes().setCompatibleMobilityProducts(null);
											variant.getAttributes().setIncludedProducts(variant.getAttributes().getIncludedEmployeeProducts());
											variant.getAttributes().setIncludedEmployeeProducts(null);
										}
									}
								})
						);
					}
				});
			}
		} catch (Exception e) {
			log.error(String.format("populateCompatibleProducts: %s" ,OffersUtils.sanitizeData(e.getMessage())));
			log.debug(String.format("Offers:: %s" ,OffersUtils.sanitizeData(offers)));
		}
	}

	/**
	 * Process the rules on CT offers.
	 *
	 * @param offerList the offer list
	 * @param offerRequestWrapper the offer request wrapper
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public List<CTOffer> dtvNowRulesProcessing(List<CTOffer> offerList, OfferRequestWrapper offerRequestWrapper) {
		long startTimeMillis = System.currentTimeMillis();
		log.info("Start of OttSalesOffersProcessorHelper.dtvNowRulesProcessing() method..");

		setLeadOffers(offerList, offerRequestWrapper);
		List<CTOffer> offerResponse = new ArrayList<>();


		try {
			List<DtvnMidasRule> dtvnMidasRuleInfoList = offersUtils.fetchDtvnMidasRules();
			if (Optional.ofNullable(dtvnMidasRuleInfoList).isPresent() && !dtvnMidasRuleInfoList.isEmpty()) {
				dtvnMidasRuleInfoList.stream().forEach(dtvnMidasRule -> {
					String salesChannel = dtvnMidasRule.getSalesChannel();
					boolean allMatch = findOpusChannel(salesChannel, offerRequestWrapper);
					String existingGroup = dtvnMidasRule.getExistingGroup();
					if (allMatch && !Optional.ofNullable(existingGroup).isPresent()) {
						offerList.stream().filter(Objects::nonNull).forEach(offer -> {

							if (offer != null && offer.getAttributes() != null && offer.getAttributes().getEligibility() != null && offer.getAttributes().getEligibility().getConstraints() != null
									&& offer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel() != null) {
								List<String> salesChannelList = offer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel();
								String offerActionType = offer.getAttributes().getOfferActionType();
								String derivedSalesChannel = deriveSalesChannel(salesChannelList, offerActionType);
								boolean salesChMatch = findOpusChannel(derivedSalesChannel, offerRequestWrapper);
								if (salesChMatch) {
									String contractIntent = dtvnMidasRule.getContractIntent();
									// Filter out offers that have a contract indicator other than the specified values
									if (Optional.ofNullable(contractIntent).isPresent() && !contractIntent.isEmpty()
											&& !contractIntent.equalsIgnoreCase(Constants.CONTRACT) // Check if contractIntent is not equal to "CONTRACT"
											&& !contractIntent.equalsIgnoreCase("Retail") // Check if contractIntent is not equal to "Retail"
											&& !contractIntent.equalsIgnoreCase(Constants.EDSP_STRING) // Check if contractIntent is not equal to the value of Constants.EDSP
											&& !contractIntent.equalsIgnoreCase(Constants.TAZ_STRING) // Check if contractIntent is not equal to the value of Constants.TAZ
											&& !contractIntent.equalsIgnoreCase(Constants.TAZBYOD_STRING) // Check if contractIntent is not equal to the value of Constants.TAZBYOD
											&& !contractIntent.equalsIgnoreCase(Constants.TAZCONTRACT_STRING) // Check if contractIntent is not equal to the value of Constants.TAZCONTRACT
											&& !contractIntent.equalsIgnoreCase(Constants.FRONTPORCH_STRING)) { // Check if contractIntent is not equal to the value of Constants.FRONTPORCH
										log.error(":::Issue..Midas Rules Contract Intent invalid");
										throw new ServiceException(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR)
												.addDetail(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR_DETAILS001);
									}

									/*boolean zipcodeAvailable = false;
									if (featureHelper.isEnabled(Constants.SVC_ATTTV_NATIONAL_LAUNCH)) {
										zipcodeAvailable=true;
									}
									if(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerEligibility()).isPresent() &&
											Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerEligibility().getZipCode()).isPresent()) {
										if (featureHelper.isEnabled(Constants.SVC_ATTTV_NATIONAL_LAUNCH)) {
											zipcodeAvailable=true;
										}else{
											List<String> allZipcodes = offersUtils.fetchZipcodes();
											zipcodeAvailable = allZipcodes.containsAll(offerRequestWrapper.getOfferRequest().getCustomerEligibility().getZipCode());
										}
									}*/
									zipCodeAndContractChecks(offerResponse, offer, offerRequestWrapper, true, contractIntent);
								}

							}


						});

					}

				});
			}

		} catch (Exception e) {
			log.error(" Exception Occured Midas Rules Sales:", e);
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
		log.info("End of OttSalesOffersProcessorHelper.dtvNowRulesProcessing() method..");
		log.info("TOTAL_TIME_TAKEN_FOR_dtvNowRulesProcessing-[{}]", System.currentTimeMillis()-startTimeMillis);

		return offerResponse;
	}


	/**
	 *
	 * @param offerList
	 * @param offerRequestWrapper
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void setLeadOffers(List<CTOffer> offerList, OfferRequestWrapper offerRequestWrapper) {

		log.debug("Start of OttSalesOffersProcessorHelper.setLeadOffers() method..");
		List<String> contractIndicator = offerRequestWrapper.getOfferRequest().getContractIndicator();
		try {
			List<DtvnMidasRule> dtvnMidasRuleInfoList = offersUtils.fetchDtvnMidasRules();
			if (Optional.ofNullable(dtvnMidasRuleInfoList).isPresent() && !dtvnMidasRuleInfoList.isEmpty()) {
				dtvnMidasRuleInfoList.stream().forEach(dtvnMidasRule -> {
					String salesChannel = dtvnMidasRule.getSalesChannel();
					boolean allMatch = findOpusChannel(salesChannel, offerRequestWrapper);
					String existingGroup = dtvnMidasRule.getExistingGroup();
					if (allMatch && !Optional.ofNullable(existingGroup).isPresent() &&
							("Contract".equalsIgnoreCase(dtvnMidasRule.getContractIntent()) || "EDSP".equalsIgnoreCase(dtvnMidasRule.getContractIntent())
									|| "TAZ".equalsIgnoreCase(dtvnMidasRule.getContractIntent())
									|| "TAZBYOD".equalsIgnoreCase(dtvnMidasRule.getContractIntent())
									|| "TAZCONTRACT".equalsIgnoreCase(dtvnMidasRule.getContractIntent()))) {
						Long leadGroup = dtvnMidasRule.getLeadGroup();
						offerList.stream().filter(Objects::nonNull).forEach(offer -> {

							if(("Contract".equalsIgnoreCase(dtvnMidasRule.getContractIntent()) && "Contract".equalsIgnoreCase(offer.getAttributes().getContractIndicator())) ||
									("EDSP".equalsIgnoreCase(dtvnMidasRule.getContractIntent()) && "EDSP".equalsIgnoreCase(offer.getAttributes().getContractIndicator()))||
									("TAZ".equalsIgnoreCase(dtvnMidasRule.getContractIntent()) && "TAZ".equalsIgnoreCase(offer.getAttributes().getContractIndicator()))||
									("TAZBYOD".equalsIgnoreCase(dtvnMidasRule.getContractIntent()) && "TAZBYOD".equalsIgnoreCase(offer.getAttributes().getContractIndicator())) ||
									("TAZCONTRACT".equalsIgnoreCase(dtvnMidasRule.getContractIntent()) && "TAZCONTRACT".equalsIgnoreCase(offer.getAttributes().getContractIndicator()))) {
								if (offer != null && offer.getAttributes() != null && offer.getAttributes().getEligibility() != null && offer.getAttributes().getEligibility().getConstraints() != null
										&& offer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel() != null) {
									List<String> salesChannelList = offer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel();
									String offerActionType = offer.getAttributes().getOfferActionType();
									String derivedSalesChannel = deriveSalesChannel(salesChannelList, offerActionType);
									boolean salesChMatch = findOpusChannel(derivedSalesChannel, offerRequestWrapper);
									if (salesChMatch) {

										Long packageGroup = 0L;
										if (null != offer.getAttributes().getAssociatedProducts()) {
											AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
											if (Optional.ofNullable(associatedProduct).isPresent()) {
												List<ProductWrapper> bundleProducts = associatedProduct.getBundleProducts();
												if (Optional.ofNullable(bundleProducts).isPresent()
														&& !bundleProducts.isEmpty()) {
													ProductWrapper bundleProduct = bundleProducts.get(0);
													List<Product> products = bundleProduct.getProducts();
													if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
														Product prod = products.get(0);
														if (Optional.ofNullable(prod).isPresent()
																&& Optional.ofNullable(prod.getObj()).isPresent()) {
															Attributes attributes = prod.getObj().getVariants().get(0)
																	.getAttributes();
															if (Optional.ofNullable(attributes).isPresent())
															{
																if (Optional.ofNullable(attributes.getPackageGroup())
																		.isPresent()
																		&& !attributes.getPackageGroup().isEmpty()) {
																	packageGroup = Long
																			.valueOf(attributes.getPackageGroup());
																}
															}
														}
													}
												}
											}
										}
										if (leadGroup.equals(packageGroup)) {
											offer.getAttributes().setLeadOffer(true);
										} else {
											offer.getAttributes().setLeadOffer(false);
										}
									}
								}
							}
						});
					}
				});
			}

		} catch (Exception e) {
			log.error(" Exception Occured :", e);
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
		log.debug("End of OttSalesOffersProcessorHelper.setLeadOffers() method..");
	}




	/**
	 * Find sales channel.
	 *
	 * @param salesChannel            the sales channel
	 * @param offerRequestWrapper the offer request wrapper
	 * @return true, if successful
	 */
	public boolean findOpusChannel(String salesChannel, OfferRequestWrapper offerRequestWrapper) {
		List<String> salesCh = salesChannel != null ? Arrays.asList(salesChannel.split("\\|")) : new ArrayList<>();
		boolean matched = false;
		if(StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains("opus")) {
			matched = salesCh.stream().anyMatch(s -> s.equalsIgnoreCase(Constants.AGENT_NEW) || s.equalsIgnoreCase(Constants.ALL));
		}else if(StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains("online") ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains(Constants.DIRECTV_ONLINE) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains(Constants.INDIRECT_PARTNER)||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains((Constants.EVERGENT_CRM).toLowerCase()) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains((Constants.OEM_IAPFIRETV).toLowerCase()) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains((Constants.OEM_IAPROKUTV).toLowerCase()) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains((Constants.OEM_APPLE).toLowerCase()) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains((Constants.OEM_GOOGLE).toLowerCase()) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains((Constants.OEM_TIZEN).toLowerCase()) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains(Constants.OSPREY) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains((Constants.OEM_IAP_GOOGLE).toLowerCase()) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains((Constants.ISUROKUTV).toLowerCase()) ||
				StringUtils.join(offerRequestWrapper.getOfferRequest().getSalesChannel(), "\\|").toLowerCase().contains(Constants.DIRECTV_STREAM_ONLINE)) {
			matched = salesCh.stream().anyMatch(s -> s.equalsIgnoreCase(Constants.SELF_SERVICE_NEW) || s.equalsIgnoreCase(Constants.ALL));
		}
		return matched;
	}

	/**
	 * Find sales channel.
	 *
	 * @param salesChannel            the sales channel
	 * @param offerActionType the offer action type
	 * @return true, if successful
	 */
	public String deriveSalesChannel(List<String> salesChannel,String offerActionType) {

		String dtvnowSalesChannel=null;
		//BYODOPC-4674 remove usage of dotcomAgent
		if (salesChannel != null && !salesChannel.isEmpty() ){
			if (salesChannel.stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s)) && salesChannel.contains(Constants.OPUS)) {
				dtvnowSalesChannel = Constants.ALL;
			} else if (salesChannel.stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s)) &&  offerActionType != null && !offerActionType.equalsIgnoreCase(Constants.ACQUISITION)
					&& !offerActionType.equalsIgnoreCase(Constants.UPSELL_ACTION_TYPE)) {
				dtvnowSalesChannel = Constants.SELF_SERVICE_EXISTING;

			} else if ((salesChannel.stream().anyMatch(channel -> channel.equalsIgnoreCase(Constants.OPUS)
					//||channel.equalsIgnoreCase(Constants.DOT_COM_AGENT)
				))
					&& offerActionType != null && !offerActionType.equalsIgnoreCase(Constants.ACQUISITION)
					&& !offerActionType.equalsIgnoreCase(Constants.UPSELL_ACTION_TYPE)) {
				dtvnowSalesChannel = Constants.AGENT_EXISTING;
			}else if (salesChannel.stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s)) && offerActionType != null
					&& ( offerActionType.equalsIgnoreCase(Constants.ACQUISITION) || offerActionType.equalsIgnoreCase(Constants.UPSELL_ACTION_TYPE))) {
				dtvnowSalesChannel = Constants.SELF_SERVICE_NEW;
			}
			else if (salesChannel.contains(Constants.OPUS) && offerActionType != null
					&& (offerActionType.equalsIgnoreCase(Constants.ACQUISITION)|| offerActionType.equalsIgnoreCase(Constants.UPSELL_ACTION_TYPE)) ){
				dtvnowSalesChannel = Constants.AGENT_NEW;
			}
		}
		return dtvnowSalesChannel;
	}

	/**
	 * Calculate best price.
	 *
	 * @param offerList the offer list
	 */
	public void calculateBestPrice(OfferRequestWrapper offerRequestWrapper, List<CTOffer> offerList) {
		boolean isBYODFlowStatus = Optional.ofNullable(offerRequestWrapper)
				.map(OfferRequestWrapper::getOfferRequest)
				.map(OfferRequest::getCustomerEligibility)
				.map(CustomerEligibility::isBYODFlow)
				.orElse(false);
		offerList.stream().filter(Objects::nonNull).forEach(offer -> {
			offersUtils.setBasePriceBasedOnPriceTier(offer);
			AtomicReference<List<String>> choiceGrpOnSelection = new AtomicReference<>(new ArrayList<>());
			AtomicReference<List<String>> choiceGrpOnDeselection = new AtomicReference<>(new ArrayList<>());
			if (!(offerRequestWrapper.isNoDeviceFrameworkEnabled() && isBYODFlowStatus)
					&& offerRequestWrapper.getOfferRequest().getCartOffers() != null
					&& OffersUtils.checkOfferCodeInChoiceGrp(offer.getAttributes().getOfferChoiceGroup(), offerRequestWrapper)) {
				OffersUtils.updateBenefitsBasedOnSelection(offerRequestWrapper, offer);
			}

			AtomicBoolean isChoicGrpSalesChannelPresentInRequest = new AtomicBoolean(false);
			if (Optional.ofNullable(offer.getAttributes().getOfferChoiceGroup()).isPresent()) {
				if (Optional.ofNullable(offer.getAttributes().getOfferChoiceGroup()).isPresent() && !offer.getAttributes().getOfferChoiceGroup().isEmpty()) {
					offer.getAttributes().getOfferChoiceGroup().stream().filter(choice -> (choice != null && choice.getChoiceGroupSalesChannel() != null && !choice.getChoiceGroupSalesChannel().isEmpty() && (choice.getPromosOnSelection() != null && !choice.getPromosOnSelection().isEmpty()) || (choice.getPromosOnDeselection() != null && !choice.getPromosOnDeselection().isEmpty()))).forEach(choice -> {

						if (choice.getChoiceGroupSalesChannel().contains(offerRequestWrapper.getOfferRequest().getSalesChannel().get(0))) {
							if (choice.getPromosOnSelection() != null && !choice.getPromosOnSelection().isEmpty()) {
								choiceGrpOnSelection.set(choice.getPromosOnSelection());
							}
							if (choice.getPromosOnDeselection() != null && !choice.getPromosOnDeselection().isEmpty()) {
								choiceGrpOnDeselection.set(choice.getPromosOnDeselection());
							}
							choice.setOfferCodes(null);
							if (choice.getPromosOnDeselection() == null && choice.getPromosOnSelection() != null) {
								choice.setPromosOnDeselection(new ArrayList<>());
							}
							if (choice.getPromosOnDeselection() != null && choice.getPromosOnSelection() == null) {
								choice.setPromosOnSelection(new ArrayList<>());
							}

							isChoicGrpSalesChannelPresentInRequest.set(true);
						}

					});
				}
			}

			if (Constants.CREDIT.equalsIgnoreCase(offer.getAttributes().getOfferProductType())) {
				OffersUtils.calculateBestPriceForCredit(offer, choiceGrpOnDeselection.get(), choiceGrpOnSelection.get(), offerRequestWrapper, isChoicGrpSalesChannelPresentInRequest.get());
			} else {
				OfferPrice offerPrice = new OfferPrice();

				// Fetch the baseprice and set the offerprice at offer level with the base price. If there is no benfit to subtract, this is the best price
				List<ProductWrapper> bundleProducts = null;

				if (null != offer.getAttributes().getAssociatedProducts()) {
					AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
					if (Optional.ofNullable(associatedProduct).isPresent()) {
						bundleProducts = associatedProduct.getBundleProducts();
						if (Optional.ofNullable(bundleProducts).isPresent() && !bundleProducts.isEmpty()) {
							bundleProducts.stream().filter(Objects::nonNull).forEach(bundleProduct -> {
								List<Product> products = bundleProduct.getProducts();
								if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
									products.stream().filter(Objects::nonNull).forEach(prod -> {
										if (Optional.ofNullable(prod).isPresent() && Optional.ofNullable(prod.getObj()).isPresent()) {
											if (Optional.ofNullable(prod.getObj().getVariants()).isPresent() && !prod.getObj().getVariants().isEmpty()) {
												if (Optional.ofNullable(prod.getObj().getVariants().get(0).getPrices()).isPresent() && !prod.getObj().getVariants().get(0).getPrices().isEmpty()) {
													prod.getObj().getVariants().get(0).getPrices().stream().filter(Objects::nonNull).forEach(price -> {
														if (Optional.ofNullable(price.getValue()).isPresent() && Optional.ofNullable(price.getValue().getDollarAmount()).isPresent()) {
															offerPrice.setDollarAmount(offerPrice.getDollarAmount() + price.getValue().getDollarAmount());
															if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && !choiceGrpOnSelection.get().isEmpty()) {
																if (offerPrice.getPriceOnCGSelection() != null) {
																	offerPrice.setPriceOnCGSelection(offerPrice.getPriceOnCGSelection() + price.getValue().getDollarAmount());

																} else {
																	offerPrice.setPriceOnCGSelection(price.getValue().getDollarAmount());
																}
															}
															if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && !choiceGrpOnDeselection.get().isEmpty()) {
																if (offerPrice.getPriceOnCGDeselection() != null) {
																	offerPrice.setPriceOnCGDeselection(offerPrice.getPriceOnCGDeselection() + price.getValue().getDollarAmount());

																} else {
																	offerPrice.setPriceOnCGDeselection(price.getValue().getDollarAmount());

																}
															}
															if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && choiceGrpOnDeselection.get().isEmpty() && !choiceGrpOnSelection.get().isEmpty()) {

																if (offerPrice.getPriceOnCGDeselection() != null) {
																	offerPrice.setPriceOnCGDeselection(offerPrice.getPriceOnCGDeselection() + price.getValue().getDollarAmount());

																} else {
																	offerPrice.setPriceOnCGDeselection(price.getValue().getDollarAmount());

																}

															}
															if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && !choiceGrpOnDeselection.get().isEmpty() && choiceGrpOnSelection.get().isEmpty()) {
																if (offerPrice.getPriceOnCGSelection() != null) {
																	offerPrice.setPriceOnCGSelection(offerPrice.getPriceOnCGSelection() + price.getValue().getDollarAmount());

																} else {
																	offerPrice.setPriceOnCGSelection(price.getValue().getDollarAmount());

																}
															}
														}
													});
												}
											}
										}
									});
								}
							});
						}
					}
				}

				// Now if there is a relevant benefit value, subtract it from the base price to
				// arrive at BEST PRICE!
				List<Benefit> activeBenefit = new ArrayList<>();
				List<Benefit> freePromoBenefit = new ArrayList<>();
				offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
					if (Optional.ofNullable(benefit).isPresent() && !benefit.isIoOffer() && !(benefit.isAgentOffer() && !benefit.isFreeTrialPromo())) {
						if (Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent() && !CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints()) && !CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments()) && offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.EMPLOYEE) && benefit.getBenefitType().equalsIgnoreCase("free-promo") && !offer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_DEVICE)) {
							freePromoBenefit.add(benefit);
						} else if (Optional.ofNullable(benefit.getBenefitType()).isPresent() && benefit.getBenefitType().equalsIgnoreCase("free-promo") && Optional.ofNullable(benefit.getPeriod()).isPresent() && benefit.getPeriod().equalsIgnoreCase("LifeTime") && (Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent() && !CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints()) && !Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments()).isPresent()) && Optional.ofNullable(offer.getAttributes().getOfferProductType()).isPresent() && !offer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_DEVICE)) {
							freePromoBenefit.add(benefit);
						} else {
							activeBenefit.add(benefit);
						}
					}
				});

				//Call OfferUtils SaveNow method to exclude the benefit from offer price calculation
				OffersUtils.filterSaveNowBenefits(offerRequestWrapper, activeBenefit);

				if (freePromoBenefit.size() > 0) {
					offerPrice.setDollarAmount(0);
					if (isChoicGrpSalesChannelPresentInRequest.get()) {
						offerPrice.setPriceOnCGDeselection(0.0);
						offerPrice.setPriceOnCGSelection(0.0);
					}
				} else if (activeBenefit.size() > 0) {
					activeBenefit.sort(Comparator.comparing(Benefit::getPromoPriority));
					activeBenefit.stream().filter(Objects::nonNull).forEach(benefit -> {
						if (Optional.ofNullable(benefit).isPresent() && Optional.ofNullable(benefit.getValue()).isPresent() && Optional.ofNullable(benefit.getBenefitType()).isPresent() && benefit.getBenefitType().equalsIgnoreCase("flat-off")) {
							offerPrice.setDollarAmount(offerPrice.getDollarAmount() - benefit.getValue().getDollarAmount());
							if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && !choiceGrpOnSelection.get().isEmpty() && choiceGrpOnSelection.get().contains(benefit.getCode())) {
								offerPrice.setPriceOnCGSelection(offerPrice.getPriceOnCGSelection() - benefit.getValue().getDollarAmount());
							}
							if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && !choiceGrpOnDeselection.get().isEmpty() && choiceGrpOnDeselection.get().contains(benefit.getCode())) {
								offerPrice.setPriceOnCGDeselection(offerPrice.getPriceOnCGDeselection() - benefit.getValue().getDollarAmount());
							}
						} else if (Optional.ofNullable(benefit).isPresent() && Optional.ofNullable(benefit.getValue()).isPresent() && Optional.ofNullable(benefit.getBenefitType()).isPresent() && benefit.getBenefitType().equalsIgnoreCase("percent-off") && benefit.getValue().getPercentage() != null && benefit.getValue().getPercentage() != 0) {
							Double percentageOff = ((offerPrice.getDollarAmount()) * (Double.valueOf(benefit.getValue().getPercentage()) / 100));
							offerPrice.setDollarAmount(offerPrice.getDollarAmount() - percentageOff);
							if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && !choiceGrpOnSelection.get().isEmpty() && choiceGrpOnSelection.get().contains(benefit.getCode())) {
								offerPrice.setPriceOnCGSelection(offerPrice.getPriceOnCGSelection() - percentageOff);
							}
							if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && !choiceGrpOnDeselection.get().isEmpty() && choiceGrpOnDeselection.get().contains(benefit.getCode())) {
								offerPrice.setPriceOnCGDeselection(offerPrice.getPriceOnCGDeselection() - percentageOff);
							}
						} else if (Optional.ofNullable(benefit).isPresent() && Optional.ofNullable(benefit.getValue()).isPresent() && Optional.ofNullable(benefit.getBenefitType()).isPresent() && benefit.getBenefitType().equalsIgnoreCase("flat-rate")) {
							offerPrice.setDollarAmount(benefit.getValue().getDollarAmount());
							if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && !choiceGrpOnSelection.get().isEmpty() && choiceGrpOnSelection.get().contains(benefit.getCode())) {
								offerPrice.setPriceOnCGSelection(benefit.getValue().getDollarAmount());
							}
							if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && !choiceGrpOnDeselection.get().isEmpty() && choiceGrpOnDeselection.get().contains(benefit.getCode())) {
								offerPrice.setPriceOnCGDeselection(benefit.getValue().getDollarAmount());
							}
						}

					});
				}

/*			offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {

				if (!CollectionUtils.isEmpty(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments())
						&& offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.EMPLOYEE) && Optional.ofNullable(benefit).isPresent()
						&& Optional.ofNullable(benefit.getBenefitType()).isPresent()
						&& benefit.getBenefitType().equalsIgnoreCase("free-promo")
						&& !benefit.isIoOffer() && !benefit.isFreeTrialPromo()
						&& !(benefit.isAgentOffer())) {
					offerPrice.setDollarAmount(0);
				}
				if (Optional.ofNullable(benefit).isPresent() && Optional.ofNullable(benefit.getValue()).isPresent()
						&& Optional.ofNullable(benefit.getBenefitType()).isPresent()
						&& benefit.getBenefitType().equalsIgnoreCase("flat-off")
						&& !benefit.isIoOffer()
						&& !(benefit.isAgentOffer())) {
					offerPrice.setDollarAmount(offerPrice.getDollarAmount() - benefit.getValue().getDollarAmount());
				}

			});*/
				BigDecimal bd = new BigDecimal(Double.toString(offerPrice.getDollarAmount()));
				bd = bd.setScale(2, RoundingMode.HALF_UP);
				offerPrice.setDollarAmount(bd.doubleValue());
				if (offerRequestWrapper.getOfferRequest().getCartOffers() == null && isChoicGrpSalesChannelPresentInRequest.get() && (!choiceGrpOnSelection.get().isEmpty() || !choiceGrpOnDeselection.get().isEmpty())) {
					BigDecimal bd1 = new BigDecimal(Double.toString(offerPrice.getPriceOnCGSelection()));
					bd1 = bd1.setScale(2, RoundingMode.HALF_UP);
					offerPrice.setPriceOnCGSelection(bd1.doubleValue());
					BigDecimal bd2 = new BigDecimal(Double.toString(offerPrice.getPriceOnCGDeselection()));
					bd2 = bd2.setScale(2, RoundingMode.HALF_UP);
					offerPrice.setPriceOnCGDeselection(bd2.doubleValue());
				}
				offer.getAttributes().setOfferPrice(offerPrice);
			}
		});
	}

	/**
	 * Retrieve dtvn offers.
	 *
	 * @param offers the offers
	 * @param offer the offer
	 * @param offerRequestWrapper the offer request wrapper
	 * @param zipcodeAvailable the zipcode available
	 * @param contractIntent the contract intent
	 * @return the list
	 */
	public List<CTOffer> zipCodeAndContractChecks(List<CTOffer> offers, CTOffer offer, OfferRequestWrapper offerRequestWrapper, boolean zipcodeAvailable, String contractIntent) {

		log.debug("Start of OttSalesOffersProcessorHelper.zipCodeAndContractChecks() method..");

		// Contract Offer Check
		if (Objects.nonNull(offerRequestWrapper) &&
				Objects.nonNull(offer.getAttributes()))   {

			OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
			if (((Objects.nonNull(offerRequest.getContractIndicator())
					&& offerRequest.getContractIndicator().contains(Constants.CONTRACT))
					|| CollectionUtils.isEmpty(offerRequest.getContractIndicator()))
					&& (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.CONTRACT)
					|| offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.ALL))
					&& contractIntent.equalsIgnoreCase("Contract")) {

				if (Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains(Constants.VIDEO_PLAN))
						|| Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains("standalone"))) {
					if (zipcodeAvailable) {
						offers.add(offer);
					}
				} else {
					offers.add(offer);
				}
			}
			// Non-Contract Offer Check
			if (((Objects.nonNull(offerRequest.getContractIndicator())
					&& offerRequest.getContractIndicator().contains(Constants.NONCONTRACT))
					|| CollectionUtils.isEmpty(offerRequest.getContractIndicator()))
					&& (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.NONCONTRACT)
					|| offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.ALL))
					&& contractIntent.equalsIgnoreCase("Retail")) {
				offers.add(offer);
			}
			// EDSP Check
			if (((Objects.nonNull(offerRequest.getContractIndicator())
					&& offerRequest.getContractIndicator().contains(Constants.EDSP_STRING))
					|| CollectionUtils.isEmpty(offerRequest.getContractIndicator()))
					&& (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.EDSP_STRING))
					&& contractIntent.equalsIgnoreCase(Constants.EDSP_STRING)) {

				if (Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains(Constants.VIDEO_PLAN))
						|| Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains("standalone"))) {
					if (zipcodeAvailable) {
						offers.add(offer);
					}
				} else {
					offers.add(offer);
				}
			}

			// TAZ Check
			if (((Objects.nonNull(offerRequest.getContractIndicator())
					&& offerRequest.getContractIndicator().contains(Constants.TAZ_STRING))
					|| CollectionUtils.isEmpty(offerRequest.getContractIndicator()))
					&& (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.TAZ_STRING))
					&& contractIntent.equalsIgnoreCase(Constants.TAZ_STRING)) {

				if (Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains(Constants.VIDEO_PLAN))
						|| Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains("standalone"))) {
					if (zipcodeAvailable) {
						offers.add(offer);
					}
				} else {
					offers.add(offer);
				}
			}

			// TAZ BYOD Check
			if (((Objects.nonNull(offerRequest.getContractIndicator())
					&& offerRequest.getContractIndicator().contains(Constants.TAZBYOD_STRING))
					|| CollectionUtils.isEmpty(offerRequest.getContractIndicator()))
					&& (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.TAZBYOD_STRING))
					&& contractIntent.equalsIgnoreCase(Constants.TAZBYOD_STRING)) {

				if (Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains(Constants.VIDEO_PLAN))
						|| Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains("standalone"))) {
					if (zipcodeAvailable) {
						offers.add(offer);
					}
				} else {
					offers.add(offer);
				}
			}

			// TAZ CONTRACT Check
			if (((Objects.nonNull(offerRequest.getContractIndicator())
					&& offerRequest.getContractIndicator().contains(Constants.TAZCONTRACT_STRING))
					|| CollectionUtils.isEmpty(offerRequest.getContractIndicator()))
					&& (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.TAZCONTRACT_STRING))
					&& contractIntent.equalsIgnoreCase(Constants.TAZCONTRACT_STRING)) {

				if (Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains(Constants.VIDEO_PLAN))
						|| Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains("standalone"))) {
					if (zipcodeAvailable) {
						offers.add(offer);
					}
				} else {
					offers.add(offer);
				}
			}
			// FRONTPORCH Check
			if (((Objects.nonNull(offerRequest.getContractIndicator())
					&& offerRequest.getContractIndicator().contains(Constants.FRONTPORCH_STRING))
					|| CollectionUtils.isEmpty(offerRequest.getContractIndicator()))
					&& (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.FRONTPORCH_STRING))
					&& contractIntent.equalsIgnoreCase(Constants.FRONTPORCH_STRING)) {

				if (Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains(Constants.VIDEO_PLAN))
						|| Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains("standalone"))) {
					if (zipcodeAvailable) {
						offers.add(offer);
					}
				} else {
					offers.add(offer);
				}
			}
			// Road Runner Check
			if (((Objects.nonNull(offerRequest.getContractIndicator())
					&& offerRequest.getContractIndicator().contains(Constants.ROAD_RUNNER))
					|| CollectionUtils.isEmpty(offerRequest.getContractIndicator()))
					&& (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.ROAD_RUNNER))
					&& contractIntent.equalsIgnoreCase(Constants.ROAD_RUNNER)) {

				if (Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains(Constants.VIDEO_PLAN))
						|| Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains("standalone"))) {
					if (zipcodeAvailable) {
						offers.add(offer);
					}
				} else {
					offers.add(offer);
				}
			}
			// GENRE Check
			if (((Objects.nonNull(offerRequest.getContractIndicator())
					&& offerRequest.getContractIndicator().contains(Constants.GENRE))
					|| CollectionUtils.isEmpty(offerRequest.getContractIndicator()))
					&& (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.GENRE))
					&& contractIntent.equalsIgnoreCase(Constants.GENRE)) {

				if (Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains(Constants.VIDEO_PLAN))
						|| Objects.nonNull(offerRequest.getOfferProductType()) && ((offerRequest.getOfferProductType()).contains("standalone"))) {
					if (zipcodeAvailable) {
						offers.add(offer);
					}
				} else {
					offers.add(offer);
				}
			}
		}

		log.debug("End of OttSalesOffersProcessorHelper.zipCodeAndContractChecks() method..");
		return offers;
	}

	public <R> R getUverseCustomerAccounts(String accountId, String accountType, Class<R> responseClass) {
		try {
			return customerGraphService.getUverseCustomerAccounts(accountId, accountId, accountType, responseClass, featureHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP) ? "backup" : "primary");
		} catch (ServiceException e) {
			log.error(String.format("Exception while getUverseAccountProducts %s", e.getMessage()));
		}
		return null;
	}
	
	/**
	 * @param offerRequestWrapper
	 * @return
	 */
	private CTOfferResponse getOfferIdResponse(OfferRequestWrapper offerRequestWrapper) {
		CTOfferResponse ctOfferResponse = null;
		OfferRequestWrapper offerReqWrapper = new OfferRequestWrapper();
		offerReqWrapper.setFlow(offerRequestWrapper.getFlow());
		OfferRequest offerRequest = new OfferRequest();

		if( Objects.nonNull(offerRequestWrapper.getOfferRequest()) &&
				Objects.nonNull(offerRequestWrapper.getCtOfferRequest()) && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCartContext())) {
			offerRequest.setOfferProductFamily(offerRequestWrapper.getOfferRequest().getOfferProductFamily());
			offerRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
			offerRequest.setOfferIds(offerRequestWrapper.getCtOfferRequest().getCartContext().getOfferIds());
			offerRequest.setOfferCodes(offerRequestWrapper.getCtOfferRequest().getCartContext().getOfferCodes());
			offerRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
			offerRequest.setChannelEligibility(offerRequestWrapper.getOfferRequest().getChannelEligibility());
			offerRequest.setOnlinePartnerDetails(offerRequestWrapper.getOfferRequest().getOnlinePartnerDetails());
			offerReqWrapper.setOfferRequest(offerRequest);
			ctOfferResponse = ottCTOffersProcessor.getOfferByIds(offerReqWrapper);
		}		
		return ctOfferResponse;
	}

	/**
	 * @param offerRequestWrapper
	 * @return
	 */
	private String getBillingProductCode(OfferRequestWrapper offerRequestWrapper) {
		String billingProductCode = null;
		CTOfferResponse ctOfferResponse = null;
		OfferRequestWrapper offerReqWrapper = new OfferRequestWrapper();
		offerReqWrapper.setFlow(offerRequestWrapper.getFlow());
		OfferRequest offerRequest = new OfferRequest();

		if( Objects.nonNull(offerRequestWrapper.getOfferRequest()) &&
				Objects.nonNull(offerRequestWrapper.getCtOfferRequest()) && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCartContext())) {
			offerRequest.setOfferProductFamily(offerRequestWrapper.getOfferRequest().getOfferProductFamily());
			offerRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
			offerRequest.setOfferIds(offerRequestWrapper.getCtOfferRequest().getCartContext().getOfferIds());
			offerRequest.setOfferCodes(offerRequestWrapper.getCtOfferRequest().getCartContext().getOfferCodes());
			offerRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
			offerRequest.setChannelEligibility(offerRequestWrapper.getOfferRequest().getChannelEligibility());
			offerRequest.setOnlinePartnerDetails(offerRequestWrapper.getOfferRequest().getOnlinePartnerDetails());
			offerReqWrapper.setOfferRequest(offerRequest);
			ctOfferResponse = ottCTOffersProcessor.getOfferByIds(offerReqWrapper);
		}
		if(Objects.nonNull(ctOfferResponse) && Objects.nonNull(ctOfferResponse.getOffers()) &&
				!ctOfferResponse.getOffers().isEmpty()) {
			CTOffer offer = ctOfferResponse.getOffers().get(0);
			if(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null &&
					offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null &&
					offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null &&
					offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null &&
					offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null ) {
				billingProductCode = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getCode();
			}
		}
		return billingProductCode;
	}
	
	/**
	 * This Method will be called in CartModeScenario to check if the video-addon delayed attributes will be returned is for a freeTrailEligbible plan.
	 * @param offerRequestWrapper
	 * @param processedCTOffers
	 * @return
	 */
	public List<CTOffer> handleIneligibleOffersAndDelayedMessages(
			List<CTOffer> processedCTOffers, OfferRequestWrapper offerRequestWrapper) {

		String isValidateCartFlow = offerRequestWrapper.getFlow();
		if (featureHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED) && 
				"validateCart".equalsIgnoreCase(isValidateCartFlow)) {
			offerRequestWrapper.setFlow("");
		}
		
		CTOfferResponse ctOfferResponse = getOfferIdResponse(offerRequestWrapper);		
		offerRequestWrapper.setFlow(isValidateCartFlow);
		
        boolean isReconnectAndLessThanEligibleMonthsFlag = offersUtils.isEligibleToReconnectLessThanEligibleMonths(offerRequestWrapper.getCtOfferRequest());        
        
        java.util.concurrent.atomic.AtomicReference<String> basePackageProd = new java.util.concurrent.atomic.AtomicReference<>(null);
		final List<String> customerBenefitExtPromoTypes = new ArrayList<String>();
		if (Objects.nonNull(ctOfferResponse) && Objects.nonNull(ctOfferResponse.getOffers()) &&
				!ctOfferResponse.getOffers().isEmpty()) {
			CTOffer offer = ctOfferResponse.getOffers().get(0);
			
			if (offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null &&
					offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null &&
					offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null &&
					offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null &&
					offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null) {
				basePackageProd.set(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getCode());
			}
			
			if (CollectionUtils.isNotEmpty(offer.getAttributes().getBenefits())) {
                customerBenefitExtPromoTypes.addAll(offer.getAttributes().getBenefits().stream()
                        .filter(Objects::nonNull).map(Benefit::getExtPromoType).filter(Objects::nonNull)
                        .collect(Collectors.toList()));
            }
		}

		List<CTOffer> offers = new ArrayList<>();
		if (Objects.nonNull(processedCTOffers) && basePackageProd.get() != null) {
			processedCTOffers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())) {
					List<ProductWrapper> bundleProducts = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts();
					if (Objects.nonNull(bundleProducts) && !bundleProducts.isEmpty()) {
						bundleProducts.stream().filter(Objects::nonNull).forEach(bundleProduct -> {
							List<Product> products = bundleProduct.getProducts();
							if (Objects.nonNull(products) && !products.isEmpty()) {
								products.stream().filter(Objects::nonNull).forEach(product -> {
									//if (isReconnectAndLessThanEligibleMonthsFlag) {										 
	                                	if (product.getObj() == null || CollectionUtils.isEmpty(product.getObj().getVariants()))
	        								return;
	        							product.getObj().getVariants().forEach(variant -> {
	        								if (isReconnectAndLessThanEligibleMonthsFlag || (!isReconnectAndLessThanEligibleMonthsFlag && null != variant.getAttributes().isDelayProvisioning() && variant.getAttributes().isDelayProvisioning() && 
	        										!CollectionUtils.containsAny(variant.getAttributes().getDelayProvisioningReasons(), customerBenefitExtPromoTypes))) {
	        									variant.getAttributes().setDelayProvisioningMessagesByKey(null);
												variant.getAttributes().setDelayProvisioning(null);
												variant.getAttributes().setDelayProvisioningReasons(null);
	        								}else {
	        									variant.getAttributes().setDelayProvisioningReason(getFirstMatchedValue(variant.getAttributes().getDelayProvisioningReasons(), customerBenefitExtPromoTypes));
	        								}
	        							});		                                    
									
								});
							} 
						});
					}					
				}
				if (Objects.nonNull(offer.getAttributes())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts())) {
					List<ProductWrapper> qualifyingProducts = offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts();
					if (Objects.nonNull(qualifyingProducts) && !qualifyingProducts.isEmpty()) {
						qualifyingProducts.stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
							List<Product> products = qualifyingProduct.getProducts();
							if (Objects.nonNull(products) && !products.isEmpty()) {
								products.stream().filter(Objects::nonNull).forEach(product -> {
									if (basePackageProd.get().contains(product.getKey())) {
										offers.add(offer);
									}
								});
							} else {
								offers.add(offer);
							}
						});
					} else {
						offers.add(offer);
					}
				} else {
					offers.add(offer);
				}
				//offer.getAttributes().setDelayProvisioning(null);
    			offer.getAttributes().setDelayProvisioningReasons(null);
			});
		} else {
			return processedCTOffers;
		}
		return offers;
	}
	
	public static String getFirstMatchedValue(Collection<String> a, Collection<String> b) {
	    if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
	        return null;
	    }
	    Set<String> bSet = new HashSet<>(b);
	    for (String value : a) {
	        if (value != null && bSet.contains(value)) {
	            return value;
	        }
	    }
	    return null;
	}
	
	/**
	 * @param offerRequestWrapper
	 * @param processedCTOffers
	 * @return
	 */
	public List<CTOffer> filterInEligibleOffers(
			List<CTOffer> processedCTOffers, OfferRequestWrapper offerRequestWrapper) {

		String basePackageProd=getBillingProductCode(offerRequestWrapper);
		List<CTOffer> offers = new ArrayList<>();
		if (Objects.nonNull(processedCTOffers) && basePackageProd !=null) {
			processedCTOffers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts())) {
					List<ProductWrapper> qualifyingProducts = offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts();
					if (Objects.nonNull(qualifyingProducts) && !qualifyingProducts.isEmpty()) {
						qualifyingProducts.stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
							List<Product> products = qualifyingProduct.getProducts();
							if (Objects.nonNull(products) && !products.isEmpty()) {
								products.stream().filter(Objects::nonNull).forEach(product -> {
									if (basePackageProd.contains(product.getKey())) {
										offers.add(offer);
									}
								});
							} else {
								offers.add(offer);
							}
						});
					}else{
						offers.add(offer);
					}
				}else {
					offers.add(offer);
				}
			});
		}else{
			return processedCTOffers;
		}
		return offers;
	}
	public void migrationOffersErrorHandler(CTOfferRequest ctOfferRequest,CTErrorResponse ctErrorResponse)
	{
		if(Objects.nonNull(ctErrorResponse)
				&& StringUtils.isNotEmpty(ctErrorResponse.getMessage())
				&& ctErrorResponse.getMessage().contains(Constants.MIGRATION_CT_VALIDATION_TEXT)) {
			throw (new ServiceException(ErrorMessages.MIGRATION_OFFERS_INVALID_REQUEST))
					.addDetail(ErrorMessages.MIGRATION_OFFERS_INVALID_REQUEST_DETAILS_1001);
		} else if(Objects.nonNull(ctErrorResponse)
				&& StringUtils.isNotEmpty(ctErrorResponse.getMessage())
				&& ctErrorResponse.getMessage().contains(Constants.EDSP_CART_CONTEXT_VALIDATION)) {
			throw (new ServiceException(ErrorMessages.EDSP_OFFERS_INVALID_REQUEST))
					.addDetail(ErrorMessages.EDSP_OFFERS_INVALID_REQUEST_DETAILS_1001);
		} else {
			if (Objects.nonNull(ctOfferRequest.getOfferProductFamily())
					&& ctOfferRequest.getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
						.addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);
			} else {
				throw new ServiceException(ErrorMessages.CT_ERROR)
						.addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		}
	}

	/**
	 * @param offerRequestWrapper
	 * @param offersResponse
	 * @return
	 */
	public void getMobilityRewards(OfferRequestWrapper offerRequestWrapper, CTOfferResponse offersResponse) {
		CTOfferResponse rewardOffersResponse = null;
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		OfferRequest offerRequest =offerRequestWrapper.getOfferRequest();
		ctOfferRequest.setCustomerSegments(Stream.of("Mobility").collect(Collectors.toList()));
		ctOfferRequest.setAdditionalOfferType(Constants.REWARD_OFFER_TYPE);
		ctOfferRequest.setSalesChannel(offerRequest.getSalesChannel());
		ctOfferRequest.setOfferProductFamily(offerRequest.getOfferProductFamily());
		for (CTOffer offer : offersResponse.getOffers()) {
			if (offer != null && offer.getAttributes() != null && offer.getAttributes().getContractIndicator() != null) {
				ctOfferRequest.setContractIndicator(Stream.of(offer.getAttributes().getContractIndicator()).collect(Collectors.toList()));
				break;
			}
		}
		OfferRequestWrapper offerRequestWrapperReward = new OfferRequestWrapper();
		offerRequestWrapperReward.setCtOfferRequest(ctOfferRequest);
		OfferRequest offerRequestReward = new OfferRequest();
		offerRequestReward.setSalesChannel(offerRequest.getSalesChannel());
		offerRequestReward.setCustomerSegments(ctOfferRequest.getCustomerSegments());
		offerRequestWrapperReward.setOfferRequest(offerRequestReward);
		rewardOffersResponse = ottCTOffersProcessor.getOfferByIds(offerRequestWrapperReward);
		List<CTOffer> newOffers = new ArrayList<>();
		offersResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
			if (!offer.getAttributes().getOfferType().equalsIgnoreCase(Constants.REWARD)) {
				newOffers.add(offer);
			}
		});

		if (rewardOffersResponse != null && !CollectionUtils.isEmpty(rewardOffersResponse.getOffers())) {
			newOffers.addAll(rewardOffersResponse.getOffers());
			offersResponse.setOffers(newOffers);
		}
	}

	/**
	 * This method filters the offers based on the cart offer quantity.
	 * It iterates through each offer in the provided list, checks for additional eligibility criteria,
	 * and if the criteria type is "dependentOffer", it creates a new DependentOffer object with the specified
	 * attributes (product type, minimum count, maximum count, and offer IDs). These dependent offer objects
	 * are then added to their respective CTOffer objects.
	 *
	 * @param offerList The list of CTOffer objects to be processed for dependent offers.
	 */

	public List<CTOffer> filterOfferBasedOnCartOfferQuantity(List<CTOffer> offerList, OfferRequestWrapper offerRequestWrapper) {
		Set<CTOffer> filterOffer = new HashSet<>();
        Set<String> offerCodes;
		if (CollectionUtils.isNotEmpty(offerList)) {
            offerCodes = offerList.stream().filter(Objects::nonNull).map(CTOffer::getCode).collect(Collectors.toSet());
			offerList.forEach(offer -> {
				if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCartOffers()) &&
						CollectionUtils.isNotEmpty(offer.getAttributes().getAdditionalEligibility())) {
					offer.getAttributes().getAdditionalEligibility().forEach(additionalEligibility -> {
						if (Constants.DEPENDENT_OFFER.equalsIgnoreCase(additionalEligibility.getEligibilityType()) && Constants.VIDEO_DEVICE.equalsIgnoreCase(additionalEligibility.getEligibleProductType())) {
							additionalEligibility.getOfferIds().forEach(id -> {
								if (offerRequestWrapper.getOfferRequest().getCartOffers().stream().filter(Objects::nonNull).anyMatch(of -> of.getOfferCode().equalsIgnoreCase(id))
										&& StringUtils.isEmpty(additionalEligibility.getMaxCount()) && StringUtils.isEmpty(additionalEligibility.getMinCount())) {
									filterOffer.add(offer);
								} else if (offerRequestWrapper.getOfferRequest().getCartOffers().stream().filter(Objects::nonNull).anyMatch(of -> of.getOfferCode().equalsIgnoreCase(id))) {
									CartOffer cartOffer = offerRequestWrapper.getOfferRequest().getCartOffers().stream().filter(Objects::nonNull).filter(of -> of.getOfferCode().equalsIgnoreCase(id)).findAny().get();
									if (StringUtils.isNotEmpty(additionalEligibility.getMaxCount()) && StringUtils.isNotEmpty(additionalEligibility.getMinCount())
											&& cartOffer.getQuantity() >= Integer.parseInt(additionalEligibility.getMinCount())
											&& cartOffer.getQuantity() <= Integer.parseInt(additionalEligibility.getMaxCount())) {
										filterOffer.add(offer);
									}
								}
							});
						} else if (Constants.DEPENDENT_OFFER.equalsIgnoreCase(additionalEligibility.getEligibilityType()) && CollectionUtils.isNotEmpty(additionalEligibility.getOfferIds()) && Constants.CREDIT.equalsIgnoreCase(offer.getAttributes().getOfferProductType())) {
                            List<CartOffer> cartOffers = offerRequestWrapper.getOfferRequest().getCartOffers().stream().filter(Objects::nonNull).filter(cartOffer -> additionalEligibility.getOfferIds().contains(cartOffer.getOfferCode())).collect(Collectors.toList());
                            boolean eligibleOfferPresent = cartOffers.stream().allMatch(cartOffer -> offerCodes.contains(cartOffer.getOfferCode()));
                            if (StringUtils.isNotEmpty(additionalEligibility.getMinCount()) && StringUtils.isNotEmpty(additionalEligibility.getMaxCount())
                                    && cartOffers.size() >= Integer.parseInt(additionalEligibility.getMinCount())
                                    && cartOffers.size() <= Integer.parseInt(additionalEligibility.getMaxCount()) && eligibleOfferPresent) {
                                filterOffer.add(offer);
                            }
                        } else if (Constants.DEPENDENT_OFFER.equalsIgnoreCase(additionalEligibility.getEligibilityType()) && CollectionUtils.isNotEmpty(additionalEligibility.getOfferIds())) {
							List<CartOffer> cartOffers = offerRequestWrapper.getOfferRequest().getCartOffers().stream().filter(Objects::nonNull).filter(cartOffer -> additionalEligibility.getOfferIds().contains(cartOffer.getOfferCode())).collect(Collectors.toList());
							if (StringUtils.isNotEmpty(additionalEligibility.getMinCount()) && StringUtils.isNotEmpty(additionalEligibility.getMaxCount())
									&& cartOffers.size() >= Integer.parseInt(additionalEligibility.getMinCount())
									&& cartOffers.size() <= Integer.parseInt(additionalEligibility.getMaxCount())) {
								filterOffer.add(offer);
							}
						} else if (!Constants.DEPENDENT_OFFER.equalsIgnoreCase(additionalEligibility.getEligibilityType())) {
							filterOffer.add(offer);
						}
					});
				} else {
					filterOffer.add(offer);
				}
			});
		}
		return new ArrayList<>(filterOffer);
	}

}
