package com.dtv.dcp.epoch.integration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CpopUCCClientHelper {
	private static final Logger log = LoggerFactory.getLogger(CpopUCCClientHelper.class);
	private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

	@Autowired
	CpopUCCClient cpopUccClient;

	/** The iptvMigrationEnabled. */
	@Value("${page}")
	private int page;
	/** The pageLimit. */
	@Value("${pageLimit}")
	private int pageLimit;
	
	   /** The pvtflags. */
    @Value("${pvtflags}")
    private String pvtFlags;
    
	@Autowired
	BuildMiddlewareCTFilters middlewareFilters;

	@Autowired
	OfferFilterService offerFilterService;

	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	@Value("${apiclient.rest.cpopofferms.ctstate}")
	private String ctstate;

	@Autowired
	CpopClient cpopClient;
	
	@Autowired
	CpopClientHelper cpopClientHelper;
	
	/**
	 * 
	 * @return
	 */
	public boolean isPVTEnabled() {		
		if(featureManagerHelper.isEnabled(Constants.FEATURE_IDP_FEATURES_SVC_PVTENABLED)) {
			String[] pvtSupportedFlags = null;
			if(null != pvtFlags && pvtFlags.length() > 0) {
				pvtSupportedFlags = pvtFlags.split(",");
			}
			 // If one of the header flag is true it will be true.
			for(String pvtSupportedFlag : pvtSupportedFlags) {
				if(featureManagerHelper.isEnabled(pvtSupportedFlag)) {
					return true;
				}
			}			
		}
		return false;
	}

	/**
	 *
	 * @return
	 */
	private Pagination getPagination(CTOfferRequest ctOfferRequest) {
		Pagination pagination = new Pagination();
		
		if (featureManagerHelper.isEnabled("svc-epoch-ade-perf-change")
				&& ((Optional.ofNullable(ctOfferRequest.getOfferActionType()).isPresent()
						&& !ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE))
						&& (Optional.ofNullable(ctOfferRequest.getSalesChannel()).isPresent()
								&& ctOfferRequest.getSalesChannel().contains("online"))
						&& (Optional.ofNullable(ctOfferRequest.getOfferProductFamily()).isPresent()
								&& ctOfferRequest.getOfferProductFamily().contains("satellite")))) {
			List<String> adePageLimitConfig = cpopClient.loadGlobalConfigurations("satellite").get("adePageLimit");
			if (CollectionUtils.isNotEmpty(adePageLimitConfig)) {
				int adePagelimitCtConfig = Integer.parseInt(adePageLimitConfig.get(0));
				pagination.setLimit(adePagelimitCtConfig);
			} else {
				pagination.setLimit(150);
			}
		} else {
			pagination.setLimit(pageLimit);
		}

		pagination.setPage(page);
		return pagination;
	}

	/**
	 *
	 * @param ctOfferRequest
	 * @return
	 * @throws ServiceException
	 */
	public CTOfferResponse getOffers(CTOfferRequest ctOfferRequest) throws ServiceException {
		CTOfferResponse offerResponse = null;

		ctOfferRequest.setState(ctstate);
		ctOfferRequest.setPagination(this.getPagination(ctOfferRequest));
		ctOfferRequest.setExpandProductRefs(true);
		ctOfferRequest.setExpiredOffers(true);
		String json = null;
		try {
			long startTimeInMillis = System.currentTimeMillis();
			json = MAPPER.writeValueAsString(ctOfferRequest);
			log.info("CT-JSON-REQUEST {}", json);

			boolean isCouponFlow = Optional.ofNullable(ctOfferRequest.getOfferType()).orElse(Collections.emptyList())
					.contains("coupon");

			if (!isCouponFlow) {
				offerResponse = cpopClient.getOffers(ctOfferRequest);
			} else if (featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_ENABLED)) {
				offerResponse = cpopUccClient.getOffers(ctOfferRequest);
			}

			long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
			log.info("EPOCH_CT_OFFERS_EXECUTION_TIME-[{}]", (endTimeMillis));
			if (endTimeMillis > 1000) {
				log.info("EPOCH_CT_OFFERS_EXECUTION_TIME_REQUEST-[{},{}]", (endTimeMillis),
						JsonService.getJsonFromObject(ctOfferRequest));
			}
		} catch (Exception e) {
			log.debug(String.format("getOffersFromSource::: %s", json));
			log.error(String.format("getOffersFromSource::: %s", e.getMessage()));
			throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
		}
		if (offerResponse == null) {
			log.info("In Method getOffers.CpopUCCClientHelper() :: Getting  null response from the CT");
			throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
		} else {
			setInlineProducts(offerResponse);
			log.info("Call as part of the 3D framework");
			cpopClientHelper.setOfferLevel3DAttributes(offerResponse,ctOfferRequest);
			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_GLOBALELIGIBLITYRULES_ENABLED)) {
      			 log.info("Call Global Eligbility Rule Filtering");
      			 long startTimes = System.currentTimeMillis();
      			 offerFilterService.applyFilters(ctOfferRequest.getOfferRequest(), offerResponse);
      			 log.info("TOTAL_TIME_FOR_IDP_FILTERING-[{}]",System.currentTimeMillis()- startTimes );
			}
			long startTimeMillis = System.currentTimeMillis();
			if (null != ctOfferRequest.getFlow() && ctOfferRequest.getFlow().equalsIgnoreCase("validateCart")) {
				middlewareFilters.applyVCMiddlewareFilters(offerResponse, ctOfferRequest);
			} else {
				middlewareFilters.applyMiddlewareFilters(offerResponse, ctOfferRequest);
			}
			log.info("TOTAL_TIME_TAKEN_APPLY_MIDDLEWARE_FILTER-[{}]", System.currentTimeMillis() - startTimeMillis);
		}
		log.info("In Method getOffers.CpopUCCClientHelper() :: Getting response from the CT");
		return offerResponse;

	}
	

	/**
	 * This method is used to find the product information based on the product id.
	 * 
	 * @param offerResponse
	 * @param id
	 * @return ProductObj
	 */
	public ProductObj getProductObjByid(CTOfferResponse offerResponse, String id) {

		if (Objects.nonNull(offerResponse.getProducts()) && StringUtils.isNotEmpty(id)) {
			Optional<ProductObj> matchingObject = offerResponse.getProducts().stream().filter(Objects::nonNull)
					.filter(p -> p.getId().equals(id)).findFirst();
			if (matchingObject.isPresent()) {
				ProductObj productObj = (ProductObj) SerializationUtils.clone(matchingObject.get());
				return productObj;
			} else {
				log.error("Inline Product not found at top level. Product Id: " + id);
				throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		} else {
			log.error("Inline Product not found at top level. Product Id: " + id);
			throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
		}
	}

	public void setInlineProducts(CTOfferResponse offerResponse) {
		if (featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)) {
			// New Implementation to set inline products which creates a map of productId
			// and ProductObj to reduce the time complexity to get the product details for
			// inline products, compared to old implementation which has multiple calls to
			// getProductObjByid.
			setInlineProductsV2(offerResponse);
		} else {
			// Old Implementation with multiple calls to getProductObjByid which is costly
			// when there are more number of inline products as it iterates through the list
			// of products to find the matching product for each inline product.
			setInlineProductsV1(offerResponse);
		}
	}
	
	public void setInlineProductsV1(CTOfferResponse offerResponse) {
		long startTimeInMillis = System.currentTimeMillis();
		try {
			offerResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
				if (Objects.nonNull(ctOffer.getAttributes())
						&& CollectionUtils.isNotEmpty(ctOffer.getAttributes().getAssociatedProducts())) {
					ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
							.forEach(associatedProduct -> {
								if (CollectionUtils.isNotEmpty(associatedProduct.getQualifyingProducts())) {
									associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull)
											.forEach(qualifyingProduct -> {
												if (CollectionUtils.isNotEmpty(qualifyingProduct.getProducts())) {
													qualifyingProduct.getProducts().stream().filter(Objects::nonNull)
															.forEach(product -> {
																product.setObj(getProductObjByid(offerResponse,
																		product.getId()));
															});
												}
 
											});
								}
								if (CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts())) {
									associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
											.forEach(bundleProduct -> {
												if (CollectionUtils.isNotEmpty(bundleProduct.getProducts())) {
													bundleProduct.getProducts().stream().filter(Objects::nonNull)
															.forEach(product -> {
																product.setObj(getProductObjByid(offerResponse,
																		product.getId()));
															});
												}
 
											});
								}
								if (CollectionUtils.isNotEmpty(associatedProduct.getAssociatedConditions())) {
									associatedProduct.getAssociatedConditions().stream().filter(Objects::nonNull)
											.forEach(condition -> {
												if (CollectionUtils.isNotEmpty(condition.getProducts())) {
													condition.getProducts().stream().filter(Objects::nonNull)
															.forEach(product -> {
																product.setObj(getProductObjByid(offerResponse,
																		product.getId()));
															});
												}
 
											});
								}
							});
				}
			});
		} catch (Exception ex) {
			log.error("EPOCH CpopClientHelper.setInlineProducts::" + ex);
 
		}
		long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
		log.info("EPOCH_CT_OFFERS_SET_InlineProducts-[{}]", (endTimeMillis));
	}
	
	public void setInlineProductsV2(CTOfferResponse offerResponse) {
        long startTimeInMillis = System.currentTimeMillis();
        try {
            // Build lookup map once — O(n) — instead of O(n) linear scan on every call
            Map<String, ProductObj> productMap = CollectionUtils.isEmpty(offerResponse.getProducts())
                    ? Collections.emptyMap()
                    : offerResponse.getProducts().stream()
                            .filter(Objects::nonNull)
                            .filter(p -> StringUtils.isNotEmpty(p.getId()))
                            .collect(Collectors.toMap(ProductObj::getId, p -> p, (a, b) -> a));
 
            offerResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                if (Objects.nonNull(ctOffer.getAttributes())
                        && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getAssociatedProducts())) {
                    ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
                            .forEach(associatedProduct -> {
                                if (CollectionUtils.isNotEmpty(associatedProduct.getQualifyingProducts())) {
                                    associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull)
                                            .forEach(qualifyingProduct -> {
                                                if (CollectionUtils.isNotEmpty(qualifyingProduct.getProducts())) {
                                                    qualifyingProduct.getProducts().stream().filter(Objects::nonNull)
                                                            .forEach(product -> product.setObj(
                                                                    getProductObjByid(productMap, product.getId())));
                                                }
                                            });
                                }
                                if (CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts())) {
                                    associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
                                            .forEach(bundleProduct -> {
                                                if (CollectionUtils.isNotEmpty(bundleProduct.getProducts())) {
                                                    bundleProduct.getProducts().stream().filter(Objects::nonNull)
                                                            .forEach(product -> product.setObj(
                                                                    getProductObjByid(productMap, product.getId())));
                                                }
                                            });
                                }
                                if (CollectionUtils.isNotEmpty(associatedProduct.getAssociatedConditions())) {
                                    associatedProduct.getAssociatedConditions().stream().filter(Objects::nonNull)
                                            .forEach(condition -> {
                                                if (CollectionUtils.isNotEmpty(condition.getProducts())) {
                                                    condition.getProducts().stream().filter(Objects::nonNull)
                                                            .forEach(product -> product.setObj(
                                                                    getProductObjByid(productMap, product.getId())));
                                                }
                                            });
                                }
                            });
                }
            });
        } catch (Exception ex) {
            log.error("EPOCH CpopClientHelper.setInlineProducts::" + ex);
        }
        log.info("EPOCH_CT_OFFERS_SET_InlineProducts-[{}]", System.currentTimeMillis() - startTimeInMillis);
    }
	
	/**
	 * This method is used to find the product information based on the product id.
	 * Uses a pre-built map for O(1) lookup instead of O(n) linear scan per call.
	 *
	 * @param productMap pre-built map of product id to ProductObj
	 * @param id
	 * @return ProductObj
	 */
	public ProductObj getProductObjByid(Map<String, ProductObj> productMap, String id) {
		if (productMap != null && StringUtils.isNotEmpty(id)) {
			ProductObj productObj = productMap.get(id);
			if (productObj != null) {
				return MAPPER.convertValue(productObj, ProductObj.class);
			}
		}
		log.error("Inline Product not found at top level. Product Id: " + id);
		throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
	}
}