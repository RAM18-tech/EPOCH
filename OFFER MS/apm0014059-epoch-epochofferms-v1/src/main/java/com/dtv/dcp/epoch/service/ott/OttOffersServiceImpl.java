package com.dtv.dcp.epoch.service.ott;

import java.util.*;
import java.util.stream.Collectors;

import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.ct.product.IncludeProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.common.request.ReplacementDevices;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.processor.ott.services.OttServicesOffersProcessor;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class BaseOffersServiceImpl.
 */
@Component
public class OttOffersServiceImpl implements OttOffersService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    OttSalesOffersProcessor ottSalesOffersProcessor;

    @Autowired
    OttServicesOffersProcessor ottServicesOffersProcessor;

    @Autowired
    OttCTOffersProcessor ottCTOffersProcessor;
    @Autowired
    OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;

    @Autowired
    private FeatureManagerHelper featureHelper;
    
	@Autowired
	OffersUtils offersUtils;

	@Autowired
	CpopClient cpopClient;

    /**
     * The log.
     */
    private static Logger log = LoggerFactory.getLogger(OttOffersServiceImpl.class);

    @Override
    public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {
        log.debug("Start of OttOffersServiceImpl.getOffers() method..");

        OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();


        CTOfferResponse offersResponse = null;
        /**
         * On the bases of the getOfferActionType this class will call the OttSalesOffersProcessor or OttServicesOffersProcessor
         *   "Acquisition","Retention","Other","Upgrade","Downgrade","Cross-sell"
         *
         */
        if (Optional.ofNullable(offerRequest).isPresent()){

            if(Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()) {

                List<String> offerActionType = offerRequest.getOfferActionType();

                if ((offerActionType.contains(Constants.ACQUISITION_ACTION_TYPE)) ||
						(offerActionType.contains(Constants.UPSELL_ACTION_TYPE)) ||
						(offerActionType.contains(Constants.CLOSING_ACTION_TYPE))) {
                	log.info("EPOCH_GETOFFERS_ATTTV_ACQUISITIONFLOW");
                	log.info("{}:EPOCH_GETOFFERS_ACTIONTYPEFLOW_WITH_CHANNEL- [{}]","API_NAME:EPOCH_GETOFFERS", OffersUtils.sanitizeData(offerRequest.getSalesChannel().toString()));
                    offersResponse = ottSalesOffersProcessor.getOffers(offerRequestWrapper);

                } else {
                	 log.info("EPOCH_GETOFFERS_ATTTV_SERVICESFLOW");
                	 log.info("{}:EPOCH_GETOFFERS_ACTIONTYPEFLOW_WITH_CHANNEL- [{}]","API_NAME:EPOCH_GETOFFERS",OffersUtils.sanitizeData(offerRequest.getSalesChannel().toString()));
                    offersResponse = ottServicesOffersProcessor.getOffers(offerRequestWrapper);
                }

            }else {

                /** Considering this is cart mode request from SPO (online sales),
                 * sending additionalOfferType as rewards to get associated OTT reward offer from CT.
                 */
                if(offerRequest.getSalesChannel().stream().anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s)))
                {
                    offerRequestWrapper.getCtOfferRequest().setAdditionalOfferType(Constants.REWARD_OFFER_TYPE);
                }

                /** To GET Offers based on offerId, offerCode , bundleProducts**/
                offersResponse = ottCTOffersProcessor.getOfferByIds(offerRequestWrapper);

				if (featureHelper.isEnabled(Constants.SVC_EPOCH_MOBILITY_REWARD) && offerRequestWrapper.isCartModeMobility() && offersResponse != null && CollectionUtils.isNotEmpty(offersResponse.getOffers())) {
					boolean videoPlancall = false;
					for (CTOffer offer : offersResponse.getOffers()) {
						if (Objects.nonNull(offer) && Objects.nonNull(offer.getAttributes().getOfferProductType())
								&& offer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)
								&& Objects.nonNull(offer.getAttributes().getEligibility())
								&& Objects.nonNull(offer.getAttributes().getEligibility().getConstraints())
								&& Objects.nonNull(offer.getAttributes().getEligibility().getConstraints().get(0))
								&& CollectionUtils.isNotEmpty(offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments())
								&& offer.getAttributes().getEligibility().getConstraints().get(0).getCustomerSegments().contains(Constants.RESIDENTIAL)
								&& (CollectionUtils.isEmpty(offer.getAttributes().getOfferIntents())
										|| offer.getAttributes().getOfferIntents().contains("non-migration"))) {

							videoPlancall = true;
							break;
						}
					}

					if (videoPlancall) {
						ottSalesOffersProcessorHelper.getMobilityRewards(offerRequestWrapper, offersResponse);
						List<CTOffer> newOffers = new ArrayList<>();
						List<CTOffer> rewardOffers = new ArrayList<>();
						offersResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
							if (offer.getAttributes().getOfferType().equalsIgnoreCase(Constants.REWARD)) {
								rewardOffers.add(offer);
							} else {
								newOffers.add(offer);
							}
						});

						String billingProductCode = null;
						List<String> basePackageProductCodes = new ArrayList<>();
						if (offersResponse.getOffers() != null && !offersResponse.getOffers().isEmpty()) {
							CTOffer offer = newOffers.get(0);
							if (offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null
									&& offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null
									&& offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null
									&& offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null
									&& offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null) {
								billingProductCode = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0)
										.getObj().getCode();
							}
						}

						rewardOffers.stream().filter(Objects::nonNull).forEach(offer -> {
							if (Objects.nonNull(offer.getAttributes()) && Objects.nonNull(offer.getAttributes().getAssociatedProducts())
									&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0))
									&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts())) {
								List<ProductWrapper> qualifyingProducts = offer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts();
								if (Objects.nonNull(qualifyingProducts) && !qualifyingProducts.isEmpty()) {
									qualifyingProducts.stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
										List<Product> products = qualifyingProduct.getProducts();
										if (Objects.nonNull(products) && CollectionUtils.isNotEmpty(products)) {
											products.stream().filter(Objects::nonNull).forEach(product -> {
												basePackageProductCodes.add(product.getKey());
											});
										}
									});
								}
							}
						});

						if (billingProductCode != null && basePackageProductCodes.contains(billingProductCode) && CollectionUtils.isNotEmpty(rewardOffers)) {
							newOffers.addAll(rewardOffers);
							offersResponse.setOffers(newOffers);
						}else{
							offersResponse.setOffers(newOffers);
						}
					}
				}
                ottSalesOffersProcessor.processOffersFetchedBySearchIds(offersResponse, offerRequestWrapper);

            }
			if (null != offerRequestWrapper.getFlow()
					&& !offerRequestWrapper.getFlow().equals("validateCart")) {
			if (featureHelper.isEnabled(Constants.FEATURE_TOGGLE_REPLACEMENT_DEVICE_BASED_ON_PP)) {
				if (featureHelper.isEnabled(Constants.FEATURE_TOGGLE_REPLACEMENT_DEVICE_BASED_ON_PP_SECOND)) {
					offersResponse = getReplacementDevicesBasedOnProtectionPlanOnAccount(offerRequest, offersResponse);
				}else {
					offersResponse = getReplacementDevicesBasedOnBundledProductAccount(offerRequest, offersResponse);
				}
			} else {
				offersResponse = getReplacementDevices(offerRequest, offersResponse);
			}
            //Added code to filter out the incorrect InstallmentPlan in Equipment Cart Mode Request
            offersResponse=ottCTOffersProcessor.filterInstallmentProvider(offersResponse);

			// subCategoryByKey changes at product level variant
			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSalesChannel()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getSalesChannel().isEmpty()
					&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferActionType()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getOfferActionType().isEmpty()) {
				String modifiedSalesChannel = getSalesChannelForDisplayType(
						offerRequestWrapper.getOfferRequest().getSalesChannel(),
						offerRequestWrapper.getOfferRequest().getOfferActionType());
				offersResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
					if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
							&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts()).isPresent()
							&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)).isPresent()
							&& Optional
									.ofNullable(
											offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
									.isPresent()
							&& Optional.ofNullable(
									offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))
									.isPresent()
							&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
									.getBundleProducts().get(0).getProducts()).isPresent()
							&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
									.getBundleProducts().get(0).getProducts().get(0)).isPresent()
							&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
									.getBundleProducts().get(0).getProducts().get(0).getObj()).isPresent()
							&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
									.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants()).isPresent()
							&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
									.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0))
									.isPresent()
							&& Optional
									.ofNullable(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()
											.get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes())
									.isPresent()
							&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
									.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0)
									.getAttributes().getSubCategoryByKey()).isPresent()
							&& (offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
									.getProducts().get(0).getObj().getVariants().get(0).getAttributes()
									.getSubCategoryByKey().size() > 0)) {
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
						.getProducts().get(0).getObj().getVariants().get(0).getAttributes()
						.getSubCategoryByKey().stream().filter(Objects::nonNull).forEach(subCategory -> {
							if (subCategory.getKey().equalsIgnoreCase(modifiedSalesChannel)) {
								offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()
										.get(0).getProducts().get(0).getObj().getVariants().get(0)
										.getAttributes().setSubCategory(subCategory.getValue());
							}
						});
					}
				});
			}
            
			// displayTypeByChannel changes at product level variant
						if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSalesChannel()).isPresent()
								&& !offerRequestWrapper.getOfferRequest().getSalesChannel().isEmpty()
								&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferActionType()).isPresent()
								&& !offerRequestWrapper.getOfferRequest().getOfferActionType().isEmpty()
								&& Objects.nonNull(offersResponse)
								&& Objects.nonNull(offersResponse.getOffers())) {
							String modifiedSalesChannel = getModifiedSalesChannel(
									offerRequestWrapper.getOfferRequest().getSalesChannel(),
									offerRequestWrapper.getOfferRequest().getOfferActionType());
							String displayTypesalesChannel=	getSalesChannelForDisplayType(
									offerRequestWrapper.getOfferRequest().getSalesChannel(),
									offerRequestWrapper.getOfferRequest().getOfferActionType());
							offersResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
								if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
								&& Optional.ofNullable(offer.getAttributes().getDisplayTypeByKey()).isPresent()) {
							offer.getAttributes().getDisplayTypeByKey().stream().filter(Objects::nonNull).forEach(
									displayType -> {
										if (displayType.getDisplayTypeKey().equalsIgnoreCase(modifiedSalesChannel)) {
											offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()
													.get(0).getProducts().get(0).getObj().getVariants().get(0)
													.getAttributes().setDisplayType(displayType.getDisplayTypeValue());
										} else if (displayType.getDisplayTypeKey().equalsIgnoreCase(displayTypesalesChannel)) {
											offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()
													.get(0).getProducts().get(0).getObj().getVariants().get(0)
													.getAttributes().setDisplayType(displayType.getDisplayTypeValue());
										}
									});
						} else if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
										&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts()).isPresent()
										&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)).isPresent()
										&& Optional
												.ofNullable(
														offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
												.isPresent()
										&& Optional.ofNullable(
												offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))
												.isPresent()
										&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
												.getBundleProducts().get(0).getProducts()).isPresent()
										&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
												.getBundleProducts().get(0).getProducts().get(0)).isPresent()
										&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
												.getBundleProducts().get(0).getProducts().get(0).getObj()).isPresent()
										&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
												.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants()).isPresent()
										&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
												.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0))
												.isPresent()
										&& Optional
												.ofNullable(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()
														.get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes())
												.isPresent()
										&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)
												.getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0)
												.getAttributes().getDisplayTypeByKey()).isPresent()
										&& (offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
												.getProducts().get(0).getObj().getVariants().get(0).getAttributes()
												.getDisplayTypeByKey().size() > 0)) {
									offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)
									.getProducts().get(0).getObj().getVariants().get(0).getAttributes()
									.getDisplayTypeByKey().stream().filter(Objects::nonNull).forEach(displayType -> {
										if (displayType.getDisplayTypeKey().equalsIgnoreCase(modifiedSalesChannel)) {
											offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()
													.get(0).getProducts().get(0).getObj().getVariants().get(0)
													.getAttributes().setDisplayType(displayType.getDisplayTypeValue());
												}else if (displayType.getDisplayTypeKey().equalsIgnoreCase(displayTypesalesChannel))
												{
													offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()
															.get(0).getProducts().get(0).getObj().getVariants().get(0)
															.getAttributes().setDisplayType(displayType.getDisplayTypeValue());
										}
									});
								}
							});
						}
						
			// Filter ChoiceGroup based on the sales channel. if offer having choice group sales
			// channel and IDP requests sales channel is then return the choice group other
			// wise choice group is empty
			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSalesChannel()).isPresent()) {
				ottCTOffersProcessor.filterChoiceGroupBasedOnSalesChannel(offersResponse, offerRequestWrapper);
			}

			// Filter Product level Min Max values  based on the offer level min max values. 
			//if offer having Min Max values then replace these values in product level min max values.
			if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSalesChannel()).isPresent()) {
				ottCTOffersProcessor.replaceProductMinMaxValueWithOfferValue(offersResponse, offerRequestWrapper);
			}
			}

			//iap-remove-included-products-enabled changes start
			if (featureHelper.isEnabled(Constants.FEATURE_IAP_REMOVE_INCLUDED_PRODUCTS_ENABLED) &&
					Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSalesChannel()).isPresent()
							&& offerRequestWrapper.getOfferRequest().getSalesChannel() != null
							&& !offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.OEM_IAPFIRETV)
							&& !offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.OEM_IAP_GOOGLE)
							&& !offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.OEM_IAPROKUTV)) {
				if (offersResponse != null && offersResponse.getOffers() != null) {
					Map<String, List<String>> productsToRemoveMap = offersUtils.getIAPRemoveProductsFromIncludedProductsFromConfig();

					offersResponse.getOffers().forEach(offer -> {
						if (offer.getAttributes() != null
								&& offer.getAttributes().getAssociatedProducts() != null
								&& !offer.getAttributes().getAssociatedProducts().isEmpty()
								&& offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null
								&& !offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().isEmpty()
								&& offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null
								&& !offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().isEmpty()
								&& offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null) {
							offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().stream()
									.filter(Objects::nonNull)
									.forEach(productObj -> {
										if (productsToRemoveMap.keySet().stream().anyMatch(key -> key.equalsIgnoreCase(productObj.getObj().getCode()))
												&& productObj.getObj().getVariants() != null
												&& !productObj.getObj().getVariants().isEmpty()
												&& productObj.getObj().getVariants().get(0).getAttributes() != null
												&& productObj.getObj().getVariants().get(0).getAttributes().getIncludedProducts() != null) {

											List<IncludeProductWrapper> includedProducts = productObj.getObj().getVariants().get(0).getAttributes().getIncludedProducts();
											includedProducts.removeIf(includedProduct -> {
												if (includedProduct.getProducts() != null) {
													if (includedProduct.getProducts().size() == 1) {
														String singleProductKey = includedProduct.getProducts().get(0).getKey();
														return productsToRemoveMap.values().stream()
																.flatMap(Collection::stream)
																.anyMatch(removedProduct -> removedProduct.equalsIgnoreCase(singleProductKey));
													} else {
														// Remove matching products but keep the includedProduct in the response
														includedProduct.getProducts().removeIf(product ->
																productsToRemoveMap.values().stream()
																		.flatMap(Collection::stream)
																		.anyMatch(removedProduct -> removedProduct.equalsIgnoreCase(product.getKey()))
														);
													}
												}
												return false;
											});

											if (includedProducts.isEmpty()) {
												productObj.getObj().getVariants().get(0).getAttributes().setIncludedProducts(null);
											}
										}
									});
						}
					});
				}
			}
            if (Objects.nonNull(offersResponse) && CollectionUtils.isNotEmpty(offersResponse.getOffers())) {
                offersUtils.setDepenentOfferObject(offersResponse.getOffers());
            }
			//iap-remove-included-products-enabled changes end
			if (featureHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
				updateDelayedProvisioningMessages(offersResponse, offerRequest);

				// For Debugging purpose, Will be Deleted
				if (offersResponse != null && CollectionUtils.isNotEmpty(offersResponse.getOffers())) {
					offersResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
						if (Objects.nonNull(offer.getAttributes())
								&& Objects.nonNull(offer.getAttributes().getAssociatedProducts())
								&& CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts())
								&& Objects.nonNull(offer.getAttributes().getAssociatedProducts().get(0))
								&& Objects.nonNull(
										offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())) {
							List<ProductWrapper> bundleProducts = offer.getAttributes().getAssociatedProducts().get(0)
									.getBundleProducts();
							if (Objects.nonNull(bundleProducts) && !bundleProducts.isEmpty()) {
								bundleProducts.stream().filter(Objects::nonNull).forEach(bundleProduct -> {
									List<Product> products = bundleProduct.getProducts();
									if (Objects.nonNull(products) && !products.isEmpty()) {
										products.stream().filter(Objects::nonNull).forEach(product -> {
											if (product.getObj() == null
													|| CollectionUtils.isEmpty(product.getObj().getVariants()))
												return;
											product.getObj().getVariants().forEach(variant -> {
												if (null != variant.getAttributes()) {
													log.info("offer " + offer.getCode() + " Attributes - "
															+ variant.getAttributes().isDelayProvisioning() + ":::::"
															+ variant.getAttributes().getDelayProvisioningReasons()
															+ ":::::" + variant.getAttributes()
																	.getDelayProvisioningMessagesByKey());
												}
											});
										});
									}
								});
							}
						}
					});
				}
			}
			return offersResponse;
        }
        
        
		/*
		 * long startTimeInMillis = System.currentTimeMillis(); offersResponse =
		 * JsonFilterService.filterAttributesAndGetObjectFromJson(offersResponse,
		 * CTOfferResponse.class,Stream.of("benefitsOnCustomersAccount").toArray(String[
		 * ]::new));
		 * log.info("::::::::Time taken for filtering benefitsOnCustomersAccount::::::"+
		 * (System.currentTimeMillis() - startTimeInMillis));
		 */
		/*
		 * if (offerRequestWrapper.isChannelEligiblity() ||
		 * offerRequestWrapper.isDirectvOnline()) { AtomicBoolean offerAdded = new
		 * AtomicBoolean(false);
		 * offersResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer ->
		 * { if (Objects.nonNull(offer.getAttributes()) &&
		 * Objects.nonNull(offer.getAttributes().getOfferType()) &&
		 * offer.getAttributes().getOfferType().equalsIgnoreCase(Constants.
		 * FREETRIALWITHHYPHEN)) { offerAdded.set(true); } }); if (offerAdded.get() ==
		 * true) { offersResponse =
		 * JsonFilterService.filterAttributesAndGetObjectFromJson(offersResponse,
		 * CTOfferResponse.class, offersUtils.getIgnoreAttributes().split(",")); } }
		 */
        log.debug("End of OttOffersServiceImpl.getOffers() method..");
        return offersResponse;

    }

	/**
	 * This will be called in all the scenarios to check if delayed provisioning messages should be sent out or not
	 * Based on the free trial eligibility of the user and delayed provisioning conditions
	 * This should not be executed for cartMode calls since cartMode will not have freeTrail Base plan in the response.
	 * For CartMode calls the logic is executed in handleIneligibleOffersAndDelayedMessages in OTTSalesOffersProcessorHelper class
	 * @param offersResponse
	 * @param finalOfferList
	 * @param offerRequest
	 */
	private void updateDelayedProvisioningMessages(CTOfferResponse offersResponse, OfferRequest offerRequest) {
		if(null == offersResponse )
			return;
		List<String> benefitsCustomerHasReceivedList = null;
		List<CTOffer> finalOfferList = offersResponse.getOffers();
		// Check if customer has free trial benefit already
		final List<String> customerBenefitExtPromoTypes = new ArrayList<String>();
		// Check if OTT Service flow
		if (offersUtils.isOTTService(offerRequest)) {
			// Get all the benefits customer has received for video plan products
			benefitsCustomerHasReceivedList = offerRequest.getCustomerContext().getOtt().getProducts().stream()
					.filter(Objects::nonNull)
					.filter(productInfo -> Constants.VIDEO_PLAN.equalsIgnoreCase(productInfo.getProductType())
							&& CollectionUtils.isNotEmpty(productInfo.getPromotions()))
					.flatMap(productInfo -> productInfo.getPromotions().stream()).filter(Objects::nonNull)
					.map(promotion -> promotion.getPromotionId()).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(benefitsCustomerHasReceivedList)) {
				CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
				ctBenefitsRequest.setBenefitCodes(benefitsCustomerHasReceivedList);
				CTBenefitsResponse ctBenefitsResponse = cpopClient.getBenefitsFromGraphlQL(ctBenefitsRequest,
						Arrays.asList(Constants.EXT_PROMO_TYPE));
				if (ctBenefitsResponse != null && ctBenefitsResponse.getBenefits() != null) {
					customerBenefitExtPromoTypes.addAll(ctBenefitsResponse.getBenefits().stream()
							.filter(Objects::nonNull).map(Benefit::getExtPromoType).filter(Objects::nonNull)
							.collect(Collectors.toList()));
				}
			}
		}else {
			for (CTOffer ctOffer : finalOfferList) {
				if (null != ctOffer.getAttributes().getOfferProductType()
						&& ctOffer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)
						&& Objects.nonNull(ctOffer.getAttributes().getBenefits())) {
					customerBenefitExtPromoTypes.addAll(ctOffer.getAttributes().getBenefits().stream()
							.filter(Objects::nonNull).map(Benefit::getExtPromoType).filter(Objects::nonNull)
							.collect(Collectors.toList()));
					break;
				}
			}
		}

		// If no offer is eligible for free trial, then remove delayed provisioning messages from all the offers
		if (null == offerRequest.getCartContext() || CollectionUtils.isEmpty(offerRequest.getCartContext().getCpopOfferCodes())) {
			finalOfferList.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes())
						&& Objects.nonNull(offer.getAttributes().getAssociatedProducts()) && Objects.nonNull(
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())) {
					List<ProductWrapper> bundleProducts = offer.getAttributes().getAssociatedProducts().get(0)
							.getBundleProducts();
					if (Objects.nonNull(bundleProducts) && !bundleProducts.isEmpty()) {
						bundleProducts.stream().filter(Objects::nonNull).forEach(bundleProduct -> {
							List<Product> products = bundleProduct.getProducts();
							if (Objects.nonNull(products) && !products.isEmpty()) {
								products.stream().filter(Objects::nonNull).forEach(product -> {
									if (product.getObj() == null || CollectionUtils.isEmpty(product.getObj().getVariants()))
										return;
									product.getObj().getVariants().forEach(variant -> {
										if (( null != variant.getAttributes().getDelayProvisioningReasons() &&
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
				//offer.getAttributes().setDelayProvisioning(null);
				offer.getAttributes().setDelayProvisioningReasons(null);
			});
		}
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

	public CTOfferResponse getReplacementDevices(OfferRequest offerRequest, CTOfferResponse offersResponse) {
		if(Optional.ofNullable(offerRequest.getOfferClassificationType()).isPresent()) {
			List<String> offerClassificationType = offerRequest.getOfferClassificationType();
			if (CollectionUtils.isNotEmpty(getReplacementDevices(offerRequest))) {
				List<String> replacementDevices = getReplacementDevices(offerRequest);
				if (offerClassificationType.contains(Constants.REPLACEMENT_CLASSIFICATION_TYPE)) {
					List<CTOffer> removeList = new ArrayList<CTOffer>();
					offersResponse.getOffers().forEach(offer -> {
			        	if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
			        			&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts()).isPresent()
			        			&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0)).isPresent()
			        			&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts()).isPresent()
			        			&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0)).isPresent()
			        			&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts()).isPresent()
			        			&& !offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getKey().isEmpty()) {
			        		String productSKU = offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getKey();
			        		if (!replacementDevices.contains(productSKU)) {
			        			removeList.add(offer);
			        		}
			        	}
			        });
					
					if (!removeList.isEmpty()) {
						offersResponse.getOffers().removeAll(removeList);
					}
					offersResponse.setCount(offersResponse.getOffers().size());
					offersResponse.setTotal(offersResponse.getOffers().size());
		    	}
			}
		}
		
		return offersResponse;
	}
	
	/**
	 * Below Method returns Device Replacement offers based on Protection Plan on Account
	 * 
	 * @param offerRequest
	 * @param offersResponse
	 * @return
	 */
	public CTOfferResponse getReplacementDevicesBasedOnProtectionPlanOnAccount(OfferRequest offerRequest,
			CTOfferResponse offersResponse) {
		List<ReplacementDevices> replacementDevices = getReplacementDevicesDetail(offerRequest);
		if (CollectionUtils.isNotEmpty(offerRequest.getOfferClassificationType())
				&& offerRequest.getOfferClassificationType().contains(Constants.REPLACEMENT_CLASSIFICATION_TYPE)
				&& ObjectUtils.allNotNull(offerRequest.getCustomerContext(), offerRequest.getCustomerContext().getOtt())
				&& CollectionUtils.isNotEmpty(offerRequest.getCustomerContext().getOtt().getProducts())
				&& CollectionUtils.isNotEmpty(replacementDevices)) {
			List<CTOffer> replacementOffers = new ArrayList<>();
			ProductInfo productInfo = offerRequest.getCustomerContext().getOtt().getProducts().stream()
					.filter(Objects::nonNull)
					.filter(p -> p.getProductType().equalsIgnoreCase(Constants.PROTECTION_PLAN)).findAny().orElse(null);
			replacementDevices.stream().forEach(rd -> {
				List<CTOffer> offers = filterReplacementDevicesForProtectionPlan(offerRequest, rd, offersResponse,
						productInfo);
				if (CollectionUtils.isNotEmpty(offers)) {
					replacementOffers.addAll(offers);
				}
			});
			if (!replacementOffers.isEmpty()) {
				offersResponse.getOffers().clear();
				List<CTOffer> distinctOffers = replacementOffers.stream().distinct().collect(Collectors.toList());
				offersResponse.getOffers().addAll(distinctOffers);
			} else {
				offersResponse.getOffers().clear();
			}
			
			offersResponse.setCount(offersResponse.getOffers().size());
			offersResponse.setTotal(offersResponse.getOffers().size());
		}
		return offersResponse;
	}
	
	/**
	 * Below Method Filter Replacement Offers for customer outside BRE and within BRE
	 * @param offerRequest
	 * @param replacementDevices
	 * @param offersResponse
	 * @param productInfo
	 * @return
	 */
	private List<CTOffer> filterReplacementDevicesForProtectionPlan(OfferRequest offerRequest, ReplacementDevices replacementDevices,
			CTOfferResponse offersResponse, ProductInfo productInfo) {
		List<CTOffer> replacementOffers = null;
		if (Objects.nonNull(offerRequest.getCustomerContext().getOtt().getIsProjectedBillDate())
				&& Boolean.FALSE.equals(offerRequest.getCustomerContext().getOtt().getIsProjectedBillDate())) {

			// outside BRE
			if (replacementDevices.getWarrantyEndDate() != null
					&& Util.isDateBeforeCurrentDate(replacementDevices.getWarrantyEndDate())
					&& Objects.nonNull(productInfo)) {
				List<CTOffer> offers = new ArrayList<>();
				offersResponse.getOffers().forEach(offer -> {
					if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
							&& StringUtils.isNoneEmpty(offer.getAttributes().getReplacementDeviceType())
							&& StringUtils.equalsAnyIgnoreCase(offer.getAttributes().getReplacementDeviceType(),
									Constants.PROTECTION_PLAN)
							&& checkIfSkuPresentInQualifyingProducts(offer, productInfo.getProductCode())
							&& checkIfPrdCodePresentInBundleProducts(offer, replacementDevices.getProductCode())) {
						offers.add(offer);
					}
				});
				if (CollectionUtils.isNotEmpty(offers)) {
					replacementOffers = new ArrayList<>();
					replacementOffers.addAll(offers);
				}
			}else {
				return replacementOffers;
			}
		} else {
			// within BRE
			List<CTOffer> offers = new ArrayList<>();
			offersResponse.getOffers().forEach(offer -> {
				if (Optional.ofNullable(offer).isPresent() && Optional.ofNullable(offer.getAttributes()).isPresent()
						&& (StringUtils.isEmpty(offer.getAttributes().getReplacementDeviceType())
								|| !StringUtils.equalsAnyIgnoreCase(offer.getAttributes().getReplacementDeviceType(),
										Constants.PROTECTION_PLAN))
						&& checkIfPrdCodePresentInBundleProducts(offer, replacementDevices.getProductCode())) {
					offers.add(offer);
				}
			});
			if (CollectionUtils.isNotEmpty(offers)) {
				replacementOffers = new ArrayList<>();
				replacementOffers.addAll(offers);
			}
		}
		return replacementOffers;

	}
	
	/**
	 * Returns Boolean by checking if productCode is present inside Bundle Products of CT Offers
	 * @param offer
	 * @param productCode
	 * @return
	 */
	private boolean checkIfPrdCodePresentInBundleProducts(CTOffer offer, String productCode) {
		return offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
				.filter(ap -> CollectionUtils.isNotEmpty(ap.getBundleProducts()))
				.flatMap(bp -> bp.getBundleProducts().stream().filter(Objects::nonNull)
						.filter(pr -> CollectionUtils.isNotEmpty(pr.getProducts()))
						.flatMap(pr -> pr.getProducts().stream().filter(Objects::nonNull))
						.filter(pr -> StringUtils.isNotEmpty(pr.getKey()))
						.filter(pr -> StringUtils.equalsAnyIgnoreCase(pr.getKey(), productCode)))
				.findFirst().isPresent();
	}
	
	/**
	 * Returns Boolean by checking if sku is present inside Qualifying Products of CT Offers
	 * @param offer
	 * @param sku
	 * @return
	 */
	private boolean checkIfSkuPresentInQualifyingProducts(CTOffer offer, String sku) {
		return offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
				.filter(ap -> CollectionUtils.isNotEmpty(ap.getQualifyingProducts()))
				.flatMap(qp -> qp.getQualifyingProducts().stream().filter(Objects::nonNull)
						.filter(pr -> CollectionUtils.isNotEmpty(pr.getProducts()))
						.flatMap(pr -> pr.getProducts().stream().filter(Objects::nonNull))
						.filter(pr -> StringUtils.isNotEmpty(pr.getKey()))
						.filter(pr -> StringUtils.equalsAnyIgnoreCase(pr.getKey(), sku)))
				.findFirst().isPresent();
	}
	
	/**
	 * Checking the type of List<?> replacmentDevices and returning same using objectmaper
	 * @param offerRequest
	 * @return
	 */
	
	private List<ReplacementDevices> getReplacementDevicesDetail(OfferRequest offerRequest) {
		List<ReplacementDevices> replacementDevices = null;
		try {
			if (offerRequest.getCustomerContext() != null && offerRequest.getCustomerContext().getOtt() != null
					&& offerRequest.getCustomerContext().getOtt().getReplacementDevices() != null
					&& !offerRequest.getCustomerContext().getOtt().getReplacementDevices().isEmpty()) {
				ObjectMapper mapper = MAPPER;
				replacementDevices = mapper.convertValue(
						offerRequest.getCustomerContext().getOtt().getReplacementDevices(),
						new TypeReference<List<ReplacementDevices>>() {
						});
			}
		} catch (Exception e) {
			log.error("Exception while parsing List<ReplacementDevices>: replacmentDevices : {} ", e);
			if (CollectionUtils.isNotEmpty(getReplacementDevices(offerRequest))) {
				throw new ServiceException(ErrorMessages.ERROR_PARSING_REPLACEMENT_DEVICES);
			}
		}

		return replacementDevices;

	}
	
	/**
	 * Checking the type of List<?> replacmentDevices and returning same using objectmaper
	 * @param offerRequest
	 * @return
	 */
	public List<String> getReplacementDevices(OfferRequest offerRequest) {
		List<String> replacementDevices = null;
		try {
			if (offerRequest.getCustomerContext() != null && offerRequest.getCustomerContext().getOtt() != null
					&& offerRequest.getCustomerContext().getOtt().getReplacementDevices() != null
					&& !offerRequest.getCustomerContext().getOtt().getReplacementDevices().isEmpty()) {
				ObjectMapper mapper = MAPPER;
				replacementDevices = mapper.convertValue(
						offerRequest.getCustomerContext().getOtt().getReplacementDevices(),
						new TypeReference<List<String>>() {
						});
			}
		} catch (Exception e) {
			log.error("Exception while parsing List<String> : replacmentDevices : {}",e);
		}

		return replacementDevices;

	}
	
	public String getModifiedSalesChannel(List<String> salesChannel, List<String> offerActionType) {
		String modifiedSalesChannel = salesChannel.get(0);
		if (salesChannel.contains(Constants.DIRECTV_ONLINE)
				&& CollectionUtils.containsAny(offerActionType, Arrays.asList(Constants.OTHER_ACTION_TYPE,
				Constants.UPGRADE_ACTION_TYPE, Constants.DOWNGRADE_ACTION_TYPE, Constants.CROSS_SELL_ACTION_TYPE))) {
			modifiedSalesChannel = Constants.DIRECTV_ONLINE_SERVICES;
		} else if (salesChannel.contains(Constants.DIRECTV_ONLINE)
				&& CollectionUtils.containsAny(offerActionType, Arrays.asList(Constants.ACQUISITION_ACTION_TYPE,
				Constants.UPSELL))) {
			modifiedSalesChannel = Constants.DIRECTV_ONLINE_SALES;
		} else if (salesChannel.contains(Constants.ASSISTED_SALES)
				&& offerActionType.contains(Constants.ACQUISITION_ACTION_TYPE)) {
			modifiedSalesChannel = Constants.ASSISTED_SALES_SALES;
		}
		return modifiedSalesChannel;
	}
	

	public String getSalesChannelForDisplayType(List<String> salesChannel, List<String> offerActionType) {
		String salesChnl = salesChannel.get(0);
		String flowType = offerActionType.contains("Acquisition") || offerActionType.contains("Upsell") || offerActionType.contains("Closing") ? "Sales" : "Services";
		return salesChnl + flowType;
	}
	/**
	 * 
	 * @param offerRequest
	 * @param offersResponse
	 * @return
	 * Get Offers based on productCode filtering in bundledProduct[]
	 */
	public CTOfferResponse getReplacementDevicesBasedOnBundledProductAccount(OfferRequest offerRequest,
			CTOfferResponse offersResponse) {
		List<ReplacementDevices> replacementDevices = getReplacementDevicesDetail(offerRequest);
		if (CollectionUtils.isNotEmpty(offerRequest.getOfferClassificationType())
				&& offerRequest.getOfferClassificationType().contains(Constants.REPLACEMENT_CLASSIFICATION_TYPE)
				&& CollectionUtils.isNotEmpty(replacementDevices)) {
			List<CTOffer> filteredOffers = new ArrayList<>();
			replacementDevices.stream().forEach(rd -> {
				offersResponse.getOffers().forEach(offers -> {
					if (checkIfPrdCodePresentInBundleProducts(offers, rd.getProductCode())) {
						filteredOffers.add(offers);
					}
				});
			});
			if (!filteredOffers.isEmpty()) {
				offersResponse.getOffers().clear();
				List<CTOffer> distinctOffers = filteredOffers.stream().distinct().collect(Collectors.toList());
				offersResponse.getOffers().addAll(distinctOffers);
			} else {
				offersResponse.getOffers().clear();
			}

			offersResponse.setCount(offersResponse.getOffers().size());
			offersResponse.setTotal(offersResponse.getOffers().size());
		}
		return offersResponse;
	}
	
}