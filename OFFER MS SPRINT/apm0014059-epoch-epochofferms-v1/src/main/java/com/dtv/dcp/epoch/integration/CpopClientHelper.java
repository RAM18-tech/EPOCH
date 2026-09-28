package com.dtv.dcp.epoch.integration;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.DisclosureMessagesByKey;
import com.dtv.dcp.epoch.model.ct.offer.RibbonTextsByKey;
import com.dtv.dcp.epoch.model.ct.product.ProductDescriptionsByKey;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.*;
import com.dtv.dcp.epoch.model.ct.response.CTErrorResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class CpopClientHelper {
	private static final Logger log = LoggerFactory.getLogger(CpopClientHelper.class);
	private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

	@Autowired
	CpopClient cpopClient;

	@Autowired
	EpochClient epochClient;

	@Autowired
	OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;

	@Autowired
	BuildMiddlewareCTFilters middlewareFilters;

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
	private FeatureManagerHelper featureManagerHelper;

	@Value("${apiclient.rest.cpopofferms.ctstate}")
	private String ctstate;

	@Autowired
	OfferFilterService offerFilterService;

	@Autowired
	RedisCacheHelper redisCacheHelper;

	@Autowired
	OffersUtils offersUtils;

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
	private Pagination getPagination() {
		Pagination pagination = new Pagination();
		pagination.setLimit(pageLimit);
		pagination.setPage(page);
		return pagination;
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
		List<CTCustomerContext> customerContext = null;
		if(CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerContext())) {
			customerContext = ctOfferRequest.getCustomerContext();
			ctOfferRequest.setCustomerContext(null);
		}
		CTOfferRequest newCTtOfferRequest = new CTOfferRequest();
		BeanUtils.copyProperties(ctOfferRequest, newCTtOfferRequest);
		ctOfferRequest.setState(ctstate);
		ctOfferRequest.setPagination(this.getPagination(ctOfferRequest));
		//Product details will be suppressed by passing expandProductRefs as false,
		//both within offers(Only qualifying products with limited product info) and outside Product[].
		if (Objects.nonNull(ctOfferRequest.getOfferProductFamily()) && ctOfferRequest.getOfferProductFamily().stream()
				.allMatch(Predicate.isEqual(Constants.WIRELESS_PRODUCT_FAMILY)) && Objects.isNull(ctOfferRequest.getOfferCodes())) {
			ctOfferRequest.setExpandProductRefs(false);
		} else {
			ctOfferRequest.setExpandProductRefs(true);
		}
		if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_PST_TIME_LOGIC_ENABLED)) {
			ctOfferRequest.setOldDateFormat(true);
		}
		String json = null;
		try {
			long startTimeInMillis = System.currentTimeMillis();
			json = MAPPER.writeValueAsString(ctOfferRequest);
			log.info("CT-JSON-REQUEST {}",json);

			if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL) && null != ctOfferRequest.getFlow() && ctOfferRequest.getFlow().equalsIgnoreCase("validateCart")) {
				offerResponse = epochClient.getOffers(ctOfferRequest);
			}
			// SLS-IXP-FLAG changes
			else if (offersUtils.isSlsGetOfferServicesEnabled()
					&& offersUtils.isServiceCT(ctOfferRequest)
					&& null != ctOfferRequest.getContractIndicator()
					&& offersUtils.isUniversalCohort(ctOfferRequest.getContractIndicator())) {
				offerResponse = epochClient.getOffersFromCache(ctOfferRequest);
			} else {
				offerResponse = cpopClient.getOffers(ctOfferRequest);
			}

			long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
			log.info("EPOCH_CT_OFFERS_EXECUTION_TIME-[{}]", (endTimeMillis));
			if(endTimeMillis > 1000) {
				log.info("EPOCH_CT_OFFERS_EXECUTION_TIME_REQUEST-[{},{}]", (endTimeMillis), JsonService.getJsonFromObject(ctOfferRequest));
			}
		} catch(HttpClientErrorException e){
			log.debug(String.format("getOffersFromSource::: %s",json));
			log.info("getOffersFromSource::: {}", e);
			log.error(String.format("getOffersFromSource::: %s", e.getMessage()));
			String body = e.getResponseBodyAsString();
			log.info("error response body {}",body);
			try {
				CTErrorResponse ctErrorResponse = MAPPER.readValue(body, CTErrorResponse.class);
				ottSalesOffersProcessorHelper.migrationOffersErrorHandler(ctOfferRequest,ctErrorResponse);
			} catch (IOException e1) {
				if (Objects.nonNull(ctOfferRequest.getOfferProductFamily())
						&& ctOfferRequest.getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
					throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
							.addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);
				} else {
					throw new ServiceException(ErrorMessages.CT_ERROR)
							.addDetail(ErrorMessages.CT_ERROR_DETAILS);
				}
			}

		} catch (Exception e ){
			log.debug(String.format("getOffersFromSource::: %s",json));
			log.info("getOffersFromSource::: {}", e);
			log.error(String.format("getOffersFromSource::: %s", e.getMessage()));
			if (Objects.nonNull(ctOfferRequest.getOfferProductFamily())
					&& ctOfferRequest.getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
						.addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);
			} else {
				throw new ServiceException(ErrorMessages.CT_ERROR)
						.addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		}
		if (offerResponse==null) {
			log.info("In Method getOffers.CpopClientHelper() :: Getting  null response from the CT");
			if (Objects.nonNull(ctOfferRequest.getOfferProductFamily())
					&& ctOfferRequest.getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
						.addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);
			} else {
				throw new ServiceException(ErrorMessages.CT_ERROR)
						.addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		} else {
			if(Objects.nonNull(ctOfferRequest.getOfferProductFamily()) && ctOfferRequest.getOfferProductFamily().stream()
					.allMatch(Predicate.isEqual(Constants.WIRELESS_PRODUCT_FAMILY)) && featureManagerHelper.isEnabled(CpopConstants.EPOCH_WIRELESS_OFFER_ENABLED)) {
				log.info("Wireless StatelessCart flag enabled");
			} else {
				boolean universalCohort = null != ctOfferRequest.getContractIndicator() && offersUtils.isUniversalCohort(ctOfferRequest.getContractIndicator());
				if (!offersUtils.isServiceCT(ctOfferRequest) && universalCohort) {
					universalCohort = false;
				}
				if (ctOfferRequest.isDecisioningFlow() && CollectionUtils.isNotEmpty(customerContext)) {
					if(!featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL)
							|| (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL)
							&& null != ctOfferRequest.getFlow() && !ctOfferRequest.getFlow().equalsIgnoreCase("validateCart")
							&& !universalCohort)
							|| null == ctOfferRequest.getFlow()) {
						setInlineForRetention(offerResponse, customerContext);
					}
				}else {
					// ctOfferRequest.getContractIndicator() is coming as null in few scenarios.
					if(!featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL)
							|| (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL)
							&& null != ctOfferRequest.getFlow() && !ctOfferRequest.getFlow().equalsIgnoreCase("validateCart")
							&& !universalCohort)
							|| null == ctOfferRequest.getFlow()) {
						setInlineProducts(offerResponse);
					}
					setOfferLevel3DAttributes(offerResponse,ctOfferRequest);
				}
				if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_GLOBALELIGIBLITYRULES_ENABLED)) {
					log.info("Call Global Eligbility Rule Filtering");
					long startTimes = System.currentTimeMillis();
					offerFilterService.applyFilters(ctOfferRequest.getOfferRequest(), offerResponse);
					log.info("TOTAL_TIME_FOR_IDP_FILTERING-[{} ms]",System.currentTimeMillis()- startTimes );
				}
				long startTimeMillis = System.currentTimeMillis();
				if (null != ctOfferRequest.getFlow() && ctOfferRequest.getFlow().equalsIgnoreCase("validateCart")) {
					middlewareFilters.applyVCMiddlewareFilters(offerResponse, newCTtOfferRequest);
				} else {
					middlewareFilters.applyMiddlewareFilters(offerResponse, newCTtOfferRequest);
				}
				log.info("TOTAL_TIME_TAKEN_APPLY_MIDDLEWARE_FILTER-[{}]", System.currentTimeMillis() - startTimeMillis);
			}
		}
		log.info("In Method getOffers.CpopClientHelper() :: Getting response from the CT");
		return offerResponse;

	}

	/**
	 *
	 * @param offerResponse
	 * @param id
	 * @return
	 */
	private ProductObj getProductObjByid(CTOfferResponse offerResponse, String id) {

		if (Objects.nonNull(offerResponse.getProducts())) {
			Optional<ProductObj> matchingObject = offerResponse.getProducts().stream().filter(Objects::nonNull).
					filter(p -> p.getId().equals(id)).
					findFirst();
			if (matchingObject.isPresent()) {
				ProductObj productObj = (ProductObj) SerializationUtils.clone(matchingObject.get());
				return productObj;
			} else {
				log.error("Inline Product not found at top level. Product Id: " + id);
				throw new ServiceException(ErrorMessages.CT_ERROR)
						.addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		} else {
			log.error("Inline Product not found at top level. Product Id: " + id);
			throw new ServiceException(ErrorMessages.CT_ERROR)
					.addDetail(ErrorMessages.CT_ERROR_DETAILS);
		}
	}

	private ProductObj getProductObjByid(Map<String, ProductObj> productMap, String id) {
		if (productMap != null && StringUtils.isNotEmpty(id)) {
			ProductObj productObj = productMap.get(id);
			if (productObj != null) {
				return MAPPER.convertValue(productObj, ProductObj.class);
			}
		}
		log.error("Inline Product not found at top level. Product Id: " + id);
		throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
	}
	/**
	 *
	 * @param ctProductRequest
	 * @return
	 * @throws ServiceException
	 */
	public CTProductResponse getProducts(CTProductRequest ctProductRequest) throws ServiceException{
		CTProductResponse ctProductResponse = null;

		ctProductRequest.setState(ctstate);
		ctProductRequest.setPagination(this.getPagination());
		String json = null;
		try {
			long startTimeInMillis = System.currentTimeMillis();
			json = MAPPER.writeValueAsString(ctProductRequest);
			ctProductResponse = cpopClient.getProducts(ctProductRequest);
			long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
			log.info("EPOCH_CT_PRODUCTS_EXECUTION_TIME-[{}]", (endTimeMillis));
			if(endTimeMillis > 1000) {
				log.info("EPOCH_CT_PRODUCTS_EXECUTION_TIME_REQUEST-[{},{}]", (endTimeMillis), JsonService.getJsonFromObject(ctProductRequest));
			}
		} catch (Exception e ){
			log.debug(String.format("getProducts::: %s", json));
			log.error(String.format("getProducts::: %s", e.getMessage()));
			if (Objects.nonNull(ctProductRequest.getProductFamily())
					&& ctProductRequest.getProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
						.addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);
			} else {
				throw new ServiceException(ErrorMessages.CT_ERROR)
						.addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		}
		if (ctProductResponse==null) {
			log.info("In Method getProducts.CpopClientHelper() :: Getting  null response from the CT");
			if (Objects.nonNull(ctProductRequest.getProductFamily())
					&& ctProductRequest.getProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
						.addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);
			} else {
				throw new ServiceException(ErrorMessages.CT_ERROR)
						.addDetail(ErrorMessages.CT_ERROR_DETAILS);
			}
		}
		log.info("In Method getProducts.CpopClientHelper() :: Getting response from the CT");
		return ctProductResponse;

	}
	/**
	 * @param productType
	 * @return
	 * @throws ServiceException
	 */
	public CTProductResponse getProductsByType(String productType) throws ServiceException {
		CTProductResponse ctProductResponse = null;
		try {
			long startTimeInMillis = System.currentTimeMillis();

			ctProductResponse = cpopClient.getProductsByType(productType);
			long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
			log.info("EPOCH_CT_PRODUCTS_EXECUTION_TIME-[{}]", (endTimeMillis));
			if (endTimeMillis > 1000) {
				log.info("EPOCH_CT_PRODUCTS_EXECUTION_TIME_REQUEST-[{},{}]", (endTimeMillis), productType);
			}

		} catch (Exception e) {
			log.debug(String.format("getProductsByType::: %s", productType));
			log.error(String.format("getProductsByType::: %s", e.getMessage()));

			throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
		}
		if (ctProductResponse == null) {
			log.info("In Method getProductsByType.CpopClientHelper() :: Getting  null response from the CT");

			throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
		}
		log.info("In Method getProductsByType.CpopClientHelper() :: Getting response from the CT");
		return ctProductResponse;

	}

	public void setInlineForRetention(CTOfferResponse offerResponse, List<CTCustomerContext> ctCustomerContexts) {
		if (featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)) {
			// New Implementation to set inline products which creates a map of productId
			// and ProductObj to reduce the time complexity to get the product details for
			// inline products, compared to old implementation which has multiple calls to
			// getProductObjByid.
			setInlineForRetentionV2(offerResponse, ctCustomerContexts);
		} else {
			// Old Implementation with multiple calls to getProductObjByid which is costly
			// when there are more number of inline products as it iterates through the list
			// of products to find the matching product for each inline product.
			setInlineForRetentionV1(offerResponse, ctCustomerContexts);
		}
	}
	
	public void setInlineForRetentionV1(CTOfferResponse offerResponse, List<CTCustomerContext> ctCustomerContexts) {
		List<String> productsOnAccount = ctCustomerContexts.stream().map(CTCustomerContext::getProducts).flatMap(List::stream)
				.map(Product::getProductCode).collect(Collectors.toList());
		long startTimeInMillis = System.currentTimeMillis();
		try {
			offerResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
				if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
					ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
						if (associatedProduct.getQualifyingProducts() != null) {
							associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
								if (Objects.nonNull(qualifyingProduct.getProducts())) {
									qualifyingProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
										if(productsOnAccount.contains(product.getKey())) {
											product.setObj(getProductObjByid(offerResponse, product.getId()));
										}
									});
								}
							});
						}
						if (associatedProduct.getBundleProducts() != null) {
							associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
								if (Objects.nonNull(bundleProduct.getProducts())) {
									bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
										product.setObj(getProductObjByid(offerResponse, product.getId()));
									});
								}
							});
						}
					});
				}
			});
		} catch (Exception ex) {
			log.error("EPOCH CpopClientHelper.setInlineForRetention::" + ex);
		}
		long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
		log.info("EPOCH_CT_OFFERS_Set_InlineForRetention-[{}]", (endTimeMillis));
	}
	
	public void setInlineForRetentionV2(CTOfferResponse offerResponse, List<CTCustomerContext> ctCustomerContexts) {
		Set<String> productsOnAccount = ctCustomerContexts.stream().map(CTCustomerContext::getProducts).flatMap(List::stream)
				.map(Product::getProductCode).collect(Collectors.toSet());
		long startTimeInMillis = System.currentTimeMillis();
		try {
			Map<String, ProductObj> productMap = CollectionUtils.isEmpty(offerResponse.getProducts())
					? Collections.emptyMap()
					: offerResponse.getProducts().stream()
							.filter(Objects::nonNull)
							.filter(p -> StringUtils.isNotEmpty(p.getId()))
							.collect(Collectors.toMap(ProductObj::getId, p -> p, (a, b) -> a));
			offerResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
				if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
					ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
						if (associatedProduct.getQualifyingProducts() != null) {
							associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
								if (Objects.nonNull(qualifyingProduct.getProducts())) {
									qualifyingProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
										if(productsOnAccount.contains(product.getKey())) {
											product.setObj(getProductObjByid(productMap, product.getId()));
										}
									});
								}
							});
						}
						if (associatedProduct.getBundleProducts() != null) {
							associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
								if (Objects.nonNull(bundleProduct.getProducts())) {
									bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
										product.setObj(getProductObjByid(productMap, product.getId()));
									});
								}
							});
						}
					});
				}
			});
		} catch (Exception ex) {
			log.error("EPOCH CpopClientHelper.setInlineForRetention::" + ex);
		}
		long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
		log.info("EPOCH_CT_OFFERS_Set_InlineForRetention-[{}]", (endTimeMillis));
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
	
	public void setInlineProductsV2(CTOfferResponse offerResponse) {
		long startTimeInMillis = System.currentTimeMillis();
		try {
			Map<String, ProductObj> productMap = CollectionUtils.isEmpty(offerResponse.getProducts())
					? Collections.emptyMap()
					: offerResponse.getProducts().stream()
							.filter(Objects::nonNull)
							.filter(p -> StringUtils.isNotEmpty(p.getId()))
							.collect(Collectors.toMap(ProductObj::getId, p -> p, (a, b) -> a));
			offerResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
				if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
					ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
						if (associatedProduct.getQualifyingProducts() != null) {
							associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
								if (Objects.nonNull(qualifyingProduct.getProducts())) {
									qualifyingProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
										product.setObj(getProductObjByid(productMap, product.getId()));
									});
								}
 
							});
						}
						if (associatedProduct.getBundleProducts() != null) {
							associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
								if (Objects.nonNull(bundleProduct.getProducts())) {
									bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
										product.setObj(getProductObjByid(productMap, product.getId()));
									});
								}
 
							});
						}
						if (associatedProduct.getAssociatedConditions() != null) {
							associatedProduct.getAssociatedConditions().stream().filter(Objects::nonNull).forEach(condition -> {
								if (Objects.nonNull(condition.getProducts())) {
									condition.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
										product.setObj(getProductObjByid(productMap, product.getId()));
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
	
	public void setInlineProductsV1(CTOfferResponse offerResponse) {
		long startTimeInMillis = System.currentTimeMillis();
		try {
			offerResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
				if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
					ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
							.forEach(associatedProduct -> {
								if (associatedProduct.getQualifyingProducts() != null) {
									associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull)
											.forEach(qualifyingProduct -> {
												if (Objects.nonNull(qualifyingProduct.getProducts())) {
													qualifyingProduct.getProducts().stream().filter(Objects::nonNull)
															.forEach(product -> {
																product.setObj(getProductObjByid(offerResponse,
																		product.getId()));
															});
												}
 
											});
								}
								if (associatedProduct.getBundleProducts() != null) {
									associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
											.forEach(bundleProduct -> {
												if (Objects.nonNull(bundleProduct.getProducts())) {
													bundleProduct.getProducts().stream().filter(Objects::nonNull)
															.forEach(product -> {
																product.setObj(getProductObjByid(offerResponse,
																		product.getId()));
															});
												}
 
											});
								}
								if (associatedProduct.getAssociatedConditions() != null) {
									associatedProduct.getAssociatedConditions().stream().filter(Objects::nonNull)
											.forEach(condition -> {
												if (Objects.nonNull(condition.getProducts())) {
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

	/**
	 *
	 *
	 * @param ctResponse
	 * @param ctOfferRequest
	 * @return
	 */
	public CTOfferResponse filterProductsOnAccountToSuppressOffer(CTOfferResponse ctResponse,
																  CTOfferRequest ctOfferRequest) {
		List<CTOffer> filterOffers = new ArrayList<>();
		List<CTOffer> offers = ctResponse.getOffers();
		if (Objects.nonNull(offers) && Objects.nonNull(ctOfferRequest.getProductsOnAccountToSuppressOffer())) {
			Set<String> suppressCodes = ctOfferRequest.getProductsOnAccountToSuppressOffer().stream()
					.map(String::toLowerCase).collect(Collectors.toSet());
			offers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes())
						&& Objects.nonNull(offer.getAttributes().getProductsOnAccountToSuppressOffer())) {
					boolean shouldSuppress = offer.getAttributes().getProductsOnAccountToSuppressOffer().stream()
							.anyMatch(code -> suppressCodes.contains(code.toLowerCase()));
					if (shouldSuppress) {
						filterOffers.add(offer);
					}
				}
			});
		}

		if (CollectionUtils.isNotEmpty(filterOffers)) {
			offers.removeAll(filterOffers);
			ctResponse.setOffers(offers);
			return ctResponse;
		} else {
			return ctResponse;
		}
	}

	/**
	 *
	 * @param srcRequest
	 * @return
	 */
	public CTOfferRequest cleanCTOfferRequest(CTOfferRequest srcRequest) {
		if (featureManagerHelper.isEnabled(Constants.FEATURE_SVC_MIDDLEWARECACHE_ENABLED)) {

			String[] requiredProperties;
			List<String> requestCT = cpopClient.loadGlobalConfigurations("OTT").get("requiredCTRequestProperties");
			if(Optional.ofNullable(requestCT).isPresent()) {
				requiredProperties = requestCT.get(0).split(",");
			}else {
				requiredProperties = new String[] { "addOnType", "businessSegment", "cartContext","offerIntent",
						"customerSegments", "expandProductRefs", "expiredOffers", "offerActionType", "offerProductFamily",
						"offerProductType", "offerCodes", "cartProducts", "pagination", "salesChannel", "state", "cartOffers" ,"contractIndicator" ,
						"isMigrationRequired","retentionOfferUser","reconnectOfferFlag", "decisioningFlow", "migrationIndicator", "serviceSubscriptionType"};
			}

			CTOfferRequest targetReq = new CTOfferRequest();
			BeanWrapper srcWrapper = PropertyAccessorFactory.forBeanPropertyAccess(srcRequest);
			BeanWrapper trgWrapper = PropertyAccessorFactory.forBeanPropertyAccess(targetReq);

			Arrays.stream(requiredProperties).iterator()
					.forEachRemaining(prop -> trgWrapper.setPropertyValue(prop, srcWrapper.getPropertyValue(prop)));

			return targetReq;
		} else {
			return srcRequest;
		}
	}

	/**
	 * Method to set the offerLevel Attribute in product
	 * @param baseOffersResponse
	 * @param offersRequest
	 */
	public void setOfferLevel3DAttributes(CTOfferResponse baseOffersResponse,CTOfferRequest offersRequest ) {
		if (Optional.ofNullable(baseOffersResponse).isPresent() && CollectionUtils.isNotEmpty(baseOffersResponse.getOffers())) {
			final String salesChannel = offersRequest.getSalesChannel() != null ? offersRequest.getSalesChannel().get(0) : null;
			final String flowType = offersRequest.getOfferActionType() != null
					? (offersRequest.getOfferActionType().contains("Acquisition") || offersRequest.getOfferActionType().contains("Upsell") ? "Sales" : "Services")
					: null;
			baseOffersResponse.getOffers().forEach(offer -> {
				if (offer.getAttributes() != null
						&& CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()) &&
						(CollectionUtils.isNotEmpty(offer.getAttributes().getDescriptionByKey()) || CollectionUtils.isNotEmpty(offer.getAttributes().getDisplayNameByKey())
								|| CollectionUtils.isNotEmpty(offer.getAttributes().getDisclosureMessagesByKey())
								|| CollectionUtils.isNotEmpty(offer.getAttributes().getRibbonTextsByKey()))) {
					offer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
						if (CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts())) {
							associatedProduct.getBundleProducts().forEach(bundleProduct -> {
								if (CollectionUtils.isNotEmpty(bundleProduct.getProducts()) && bundleProduct.getProducts().get(0) != null) {
									com.dtv.dcp.epoch.model.ct.product.Product product = bundleProduct.getProducts().get(0);
									if (product.getObj() != null
											&& CollectionUtils.isNotEmpty(product.getObj().getVariants())) {
										for (Variant variant : product.getObj().getVariants()) {
											if (variant.getAttributes() != null && salesChannel != null && flowType != null) {
												List<String> scMatchFlag = new ArrayList<>();
												if (offer.getAttributes().getDescriptionByKey() != null) {
													if (Optional.ofNullable(offersRequest.getSalesChannel()).isPresent()) {
														String shortKey = salesChannel + flowType + "ShortDescription";
														String defaultShortKey = "default" + flowType + "ShortDescription";
														String longKey = salesChannel + flowType + "LongDescription";
														String defaultLongKey = "default" + flowType + "LongDescription";
														offer.getAttributes().getDescriptionByKey().forEach(descriptionByKey -> {
															if(variant.getAttributes().getDescriptionsByKey()==null)
															{
																variant.getAttributes().setDescriptionsByKey(new ProductDescriptionsByKey());
															}
															if (descriptionByKey.getName().equalsIgnoreCase(shortKey)
															) {
																variant.getAttributes().getDescriptionsByKey().setShortDesc(descriptionByKey.getValue());
																scMatchFlag.add(shortKey);

															} else if (!scMatchFlag.contains(shortKey) && descriptionByKey.getName().equalsIgnoreCase(defaultShortKey)) {
																variant.getAttributes().getDescriptionsByKey().setShortDesc(descriptionByKey.getValue());

															}
															if (descriptionByKey.getName().equalsIgnoreCase(longKey)) {
																variant.getAttributes().getDescriptionsByKey().setLongDesc(descriptionByKey.getValue());
																scMatchFlag.add(longKey);

															} else if (!scMatchFlag.contains(longKey) && descriptionByKey.getName().equalsIgnoreCase(defaultLongKey)) {
																variant.getAttributes().getDescriptionsByKey().setLongDesc(descriptionByKey.getValue());

															}
															if (variant.getAttributes().getDescriptionsByKey() != null
																	&& variant.getAttributes().getDescriptionsByKey().isEmpty()){
																variant.getAttributes().setDescriptionsByKey(null);
															}
														});
													}
												}
												if (offer.getAttributes().getDisplayNameByKey() != null) {
													if (Optional.ofNullable(offersRequest.getSalesChannel()).isPresent()) {
														String shortKey = salesChannel + flowType + "ShortDisplayName";
														String defaultShortKey = "default" + flowType + "ShortDisplayName";
														String longKey = salesChannel + flowType + "LongDisplayName";
														String defaultLongKey = "default" + flowType + "LongDisplayName";
														offer.getAttributes().getDisplayNameByKey().forEach(displayNameByKey -> {
															if(variant.getAttributes().getDisplayNamesByKey()==null)
															{
																variant.getAttributes().setDisplayNamesByKey(new GenericByKey());
															}
															if (displayNameByKey.getName().equalsIgnoreCase(shortKey)
															) {
																variant.getAttributes().getDisplayNamesByKey().setShortDisplayName(displayNameByKey.getValue());
																scMatchFlag.add(shortKey);

															} else if (!scMatchFlag.contains(shortKey) && displayNameByKey.getName().equalsIgnoreCase(defaultShortKey)) {
																variant.getAttributes().getDisplayNamesByKey().setShortDisplayName(displayNameByKey.getValue());

															}
															if (displayNameByKey.getName().equalsIgnoreCase(longKey)) {
																variant.getAttributes().getDisplayNamesByKey().setLongDisplayName(displayNameByKey.getValue());
																scMatchFlag.add(longKey);

															} else if (!scMatchFlag.contains(longKey) && displayNameByKey.getName().equalsIgnoreCase(defaultLongKey)) {
																variant.getAttributes().getDisplayNamesByKey().setLongDisplayName(displayNameByKey.getValue());

															}

															if (variant.getAttributes().getDisplayNamesByKey() != null
																	&& variant.getAttributes().getDisplayNamesByKey().isEmpty()){
																variant.getAttributes().setDisplayNamesByKey(null);
															}
														});
													}
												}
												if (offer.getAttributes().getDisclosureMessagesByKey() != null) {
													if (Optional.ofNullable(offersRequest.getSalesChannel()).isPresent()) {
														String selectAllDisclosureKey = salesChannel + flowType + "SelectAllDisclosure";
														String selectAllDefaultDisKey = "default" + flowType + "SelectAllDisclosure";
														String disclosureKey = salesChannel + flowType + "Disclosure";
														String defaultDisKey = "default" + flowType + "Disclosure";
														String shortDisclosureKey = salesChannel + flowType + "ShortDisclosure";
														String defaultShortDisclosureKey = "default" + flowType + "ShortDisclosure";
														String optInKey = salesChannel + flowType + "OptIn";
														String defaultOptInKeyKey = "default" + flowType + "OptIn";
														String optOutKey = salesChannel + flowType + "OptOut";
														String defaultOptOutKeyKey = "default" + flowType + "OptOut";
														String autoRenewKey = salesChannel + flowType + "AutoRenew";
														String defaultAutoRenewKeyKey = "default" + flowType + "AutoRenew";
														offer.getAttributes().getDisclosureMessagesByKey().forEach(disclosureMessagesByKey -> {
															if(variant.getAttributes().getDisclosureMessagesByKey()==null)
															{
																variant.getAttributes().setDisclosureMessagesByKey(new DisclosureMessagesByKey());
															}
															if (disclosureMessagesByKey.getName().equalsIgnoreCase(selectAllDisclosureKey)
															) {
																variant.getAttributes().getDisclosureMessagesByKey().setSelectAllDisclosure(disclosureMessagesByKey.getValue());
																scMatchFlag.add(selectAllDisclosureKey);

															} else if (!scMatchFlag.contains(selectAllDisclosureKey) && disclosureMessagesByKey.getName().equalsIgnoreCase(selectAllDefaultDisKey)) {
																variant.getAttributes().getDisclosureMessagesByKey().setSelectAllDisclosure(disclosureMessagesByKey.getValue());

															}
															if (disclosureMessagesByKey.getName().equalsIgnoreCase(disclosureKey)
															) {
																variant.getAttributes().getDisclosureMessagesByKey().setDisclosure(disclosureMessagesByKey.getValue());
																scMatchFlag.add(disclosureKey);

															} else if (!scMatchFlag.contains(disclosureKey) && disclosureMessagesByKey.getName().equalsIgnoreCase(defaultDisKey)) {
																variant.getAttributes().getDisclosureMessagesByKey().setDisclosure(disclosureMessagesByKey.getValue());

															}
															if (disclosureMessagesByKey.getName().equalsIgnoreCase(shortDisclosureKey)
															) {
																variant.getAttributes().getDisclosureMessagesByKey().setShortDisclosure(disclosureMessagesByKey.getValue());
																scMatchFlag.add(shortDisclosureKey);
															}else if (!scMatchFlag.contains(shortDisclosureKey) && disclosureMessagesByKey.getName().equalsIgnoreCase(defaultShortDisclosureKey)) {
																variant.getAttributes().getDisclosureMessagesByKey().setShortDisclosure(disclosureMessagesByKey.getValue());
															}
															if (disclosureMessagesByKey.getName().equalsIgnoreCase(optInKey)
															) {
																variant.getAttributes().getDisclosureMessagesByKey().setOptIn(disclosureMessagesByKey.getValue());
																scMatchFlag.add(optInKey);

															} else if (!scMatchFlag.contains(optInKey) && disclosureMessagesByKey.getName().equalsIgnoreCase(defaultOptInKeyKey)) {
																variant.getAttributes().getDisclosureMessagesByKey().setOptIn(disclosureMessagesByKey.getValue());

															}
															if (disclosureMessagesByKey.getName().equalsIgnoreCase(optOutKey)
															) {
																variant.getAttributes().getDisclosureMessagesByKey().setOptOut(disclosureMessagesByKey.getValue());
																scMatchFlag.add(optOutKey);

															} else if (!scMatchFlag.contains(optOutKey) && disclosureMessagesByKey.getName().equalsIgnoreCase(defaultOptOutKeyKey)) {
																variant.getAttributes().getDisclosureMessagesByKey().setOptOut(disclosureMessagesByKey.getValue());

															}
															if (disclosureMessagesByKey.getName().equalsIgnoreCase(autoRenewKey)
															) {
																variant.getAttributes().getDisclosureMessagesByKey().setAutoRenew(disclosureMessagesByKey.getValue());
																scMatchFlag.add(autoRenewKey);

															} else if (!scMatchFlag.contains(autoRenewKey) && disclosureMessagesByKey.getName().equalsIgnoreCase(defaultAutoRenewKeyKey)) {
																variant.getAttributes().getDisclosureMessagesByKey().setAutoRenew(disclosureMessagesByKey.getValue());

															}

															if (variant.getAttributes().getDisclosureMessagesByKey() != null
																	&& variant.getAttributes().getDisclosureMessagesByKey().isEmpty()){
																variant.getAttributes().setDisclosureMessagesByKey(null);
															}
														});
													}
												}
												if (offer.getAttributes().getRibbonTextsByKey() != null) {
													if (Optional.ofNullable(offersRequest.getSalesChannel()).isPresent()) {

														String ribbonKey = salesChannel + flowType + "RibbonText";
														String defultRibbonKey = "default" + flowType + "RibbonText";

														offer.getAttributes().getRibbonTextsByKey().forEach(ribbonTextsBykey -> {
															if(variant.getAttributes().getRibbonTextsByKey()==null)
															{
																variant.getAttributes().setRibbonTextsByKey(new RibbonTextsByKey());
															}
															if (ribbonTextsBykey.getName().equalsIgnoreCase(ribbonKey)
															) {
																variant.getAttributes().getRibbonTextsByKey().setRibbonText(ribbonTextsBykey.getValue());
																scMatchFlag.add(ribbonKey);

															} else if (!scMatchFlag.contains(ribbonKey) && ribbonTextsBykey.getName().equalsIgnoreCase(defultRibbonKey)) {
																variant.getAttributes().getRibbonTextsByKey().setRibbonText(ribbonTextsBykey.getValue());

															}
															if (variant.getAttributes().getRibbonTextsByKey() != null
																	&& variant.getAttributes().getRibbonTextsByKey().isEmpty()){
																variant.getAttributes().setRibbonTextsByKey(null);
															}
														});
													}
												}
											}
										}
									}

								}
							});
						}

					});
					if (CollectionUtils.isNotEmpty(offer.getAttributes().getDisclosureMessagesByKey())) {
						offer.getAttributes().setDisclosureMessagesByKey(null);
					}
					if (CollectionUtils.isNotEmpty(offer.getAttributes().getDisplayNameByKey())) {
						offer.getAttributes().setDisplayNameByKey(null);
					}
					if (CollectionUtils.isNotEmpty(offer.getAttributes().getDescriptionByKey())) {
						offer.getAttributes().setDescriptionByKey(null);
					}
					if (CollectionUtils.isNotEmpty(offer.getAttributes().getRibbonTextsByKey())) {
						offer.getAttributes().setRibbonTextsByKey(null);
					}
				}
			});
		}
	}


	public Map<String, String> loadProductTypeAttrs(String productType) {
		return epochClient.loadProductTypeAttrs(productType);
	}

	public String getGraphQLRequest(CTOfferRequest srcRequest) {
		StringBuilder finalQuery = new StringBuilder();
		Map<String, String> types = loadProductTypeAttrs("offer");
		Map<String, String> idpCtMapKey = getIDPCTMapKeys();
		try {
			buildGraphRequestBasedOnTypes(srcRequest, finalQuery, types, idpCtMapKey);
		} catch (Exception e) {
			log.error(String.format("getGraphQLRequest::: %s", e.getMessage()));
		}

		return finalQuery.toString();
	}

	public String getGraphQLProductRequest(CTProductRequest srcRequest) {
		StringBuilder finalQuery = new StringBuilder();

		Map<String, String> videoPlanProductTypes = loadProductTypeAttrs("video-plan");
		Map<String, String> videoAddonProductTypes = loadProductTypeAttrs("video-addon");
		Map<String, String> videoDeviceProductTypes = loadProductTypeAttrs("video-device");
		Map<String, String> feeProductTypes = loadProductTypeAttrs("fee");

		Map<String, String> productTypes = new HashMap<>(videoPlanProductTypes);
		productTypes.putAll(videoAddonProductTypes);
		productTypes.putAll(videoDeviceProductTypes);
		productTypes.putAll(feeProductTypes);

		Map<String, String> idpCtMapKey = getIDPCTMapKeys();
		try {
			buildGraphRequestBasedOnTypes(srcRequest, finalQuery, productTypes, idpCtMapKey);
		} catch (Exception e) {
			log.error(String.format("getGraphQLProductRequest::: %s", e.getMessage()));
		}
		return finalQuery.toString();
	}

	private void buildGraphRequestBasedOnTypes(Object srcRequest, StringBuilder finalQuery,
											   Map<String, String> productTypes, Map<String, String> idpCtMapKey)
			throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		StringBuilder cartOfferQuery = new StringBuilder();
		StringBuilder bundleProductsQuery = new StringBuilder();
		StringBuilder offerProductTypesQuery = new StringBuilder();
		PropertyDescriptor[] pdDescriptors =  Introspector.getBeanInfo(srcRequest.getClass()).getPropertyDescriptors();
		boolean pdExist = checkOfferProductTypeExist(srcRequest,pdDescriptors);
		for (PropertyDescriptor pd : pdDescriptors) {
			Method readMethod = pd.getReadMethod();
			if (readMethod == null) {
				// If the standard getter is not found, try "is" prefix
				readMethod = Introspector.getBeanInfo(srcRequest.getClass()).getPropertyDescriptors()[0].getReadMethod();
			}
			Object propertyValue = readMethod.invoke(srcRequest);
			if (null != propertyValue) {
				if (pd.getName().equalsIgnoreCase("offerProductType")) {
					offerProductTypesQuery.append(buildEnumAttr(pd.getName(), (List<String>)propertyValue));
				} else if(pd.getName().equalsIgnoreCase("migrationIndicator")) {
					if(pdExist) {
						if (finalQuery.length() != 0) {
							finalQuery.append(" and ");
						}
						finalQuery.append(buildEnumAttr(idpCtMapKey.get(pd.getName()), (boolean) propertyValue? Arrays.asList("migration"): Arrays.asList("non-migration")));
					}
				}
				else {
					//System.out.println("NAME - " + pd.getName() + " TYPE- " + productTypes.get(pd.getName()));
					if (pd.getName().equalsIgnoreCase("cartOffers")) {
						String query = "attributeNameToBeReplaced in ( attributValueToBeReplaced )";
						query = query.toString().replaceAll("attributeNameToBeReplaced", "key");
						List<String> values = (List<String>) propertyValue;
						String valuesString = "\"" + StringUtils.join(values, "\",\"") + "\"";
						query = query.toString().replaceAll("attributValueToBeReplaced", valuesString);
						/*if (finalQuery.length() != 0) {
							finalQuery.append(" and ");
						}*/
						cartOfferQuery.append(query);
					}else if (pd.getName().equalsIgnoreCase("cartProducts")) {
						List<String> ppcValues = new ArrayList<String>();
						List<String> bpcValues = new ArrayList<String>();
						List<ProductInfo> cartProducts = (List<ProductInfo>)propertyValue;
						for(ProductInfo cartProduct : cartProducts) {
							if(null != cartProduct.getPricePlanCode()) {
								ppcValues.add(cartProduct.getPricePlanCode());
							} else if(null != cartProduct.getBillingProductCode()) {
								bpcValues.add(cartProduct.getBillingProductCode());
							}
						}

						if(ppcValues.size() > 0 ) {
							if (finalQuery.length() != 0) {
								finalQuery.append(" and ");
							}
							finalQuery.append(buildEnumAttr("enablerPricePlanCode",ppcValues));
						} else if(bpcValues.size() > 0 ) {
							if (finalQuery.length() != 0) {
								finalQuery.append(" and ");
							}
							finalQuery.append(buildListAttr("billingProductCode",bpcValues));
						}

					}  else if (pd.getName().equalsIgnoreCase("bundleProducts")) {
						List<String> bundProductIds = (List<String>) propertyValue;
						//finalQuery.append(buildIdListAttr("bundleProductIds",bundProductIds));
						bundleProductsQuery.append(buildIdListAttr("bundleProductIds",bundProductIds));

					}else if(pd.getName().equalsIgnoreCase("offerCodes")) {
						if (finalQuery.length() != 0) {
							finalQuery.append(" and ");
						}
						String valuesString = "\"" + StringUtils.join((List<String>)propertyValue , "\",\"") + "\"";
						finalQuery.append( "key in (" +valuesString+ ")");
					}else if(pd.getName().equalsIgnoreCase("offerIds")) {
						if (finalQuery.length() != 0) {
							finalQuery.append(" and ");
						}
						String valuesString = "\"" + StringUtils.join((List<String>)propertyValue , "\",\"") + "\"";
						finalQuery.append( "id in (" +valuesString+ ")");
					}else {
						String typeKey = null != idpCtMapKey.get(pd.getName()) ? idpCtMapKey.get(pd.getName())
								: pd.getName();
						if (null != productTypes.get(typeKey) && (productTypes.get(typeKey).equalsIgnoreCase("set_enum") || productTypes.get(typeKey).equalsIgnoreCase("enum"))) {
							if (finalQuery.length() != 0) {
								finalQuery.append(" and ");
							}
							finalQuery.append(buildEnumAttr(typeKey, (List<String>)propertyValue));
						} else if (null != productTypes.get(typeKey) && productTypes.get(typeKey).equalsIgnoreCase("set_list")) {
							if (finalQuery.length() != 0) {
								finalQuery.append(" and ");
							}
							finalQuery.append(buildListAttr(typeKey, (List<String>)propertyValue));
						} else if (null != productTypes.get(typeKey) && productTypes.get(typeKey).equalsIgnoreCase("boolean")) {
							if (finalQuery.length() != 0) {
								finalQuery.append(" and ");
							}
							finalQuery.append(buildBooleanAttr(pd, propertyValue));
						}
					}
				}
			}
		}
		StringBuilder orQuery = new StringBuilder("(");
		if(cartOfferQuery.length() > 0 && bundleProductsQuery.length() > 0) {
			if (finalQuery.length() != 0) {
				finalQuery.append(" and ");
			}
			orQuery.append(cartOfferQuery.toString());
			orQuery.append(" or ");
			orQuery.append(bundleProductsQuery.toString());
			if(offerProductTypesQuery.length()> 0) {
				orQuery.append(" or ");
				orQuery.append(offerProductTypesQuery.toString());
			}
			orQuery.append(")");
			finalQuery.append(orQuery.toString());
		}else {

			if(bundleProductsQuery.length() > 0) {
				if (finalQuery.length() != 0) {
					finalQuery.append(" and ");
				}
				//finalQuery.append(bundleProductsQuery);
				orQuery.append(bundleProductsQuery.toString());
				if(offerProductTypesQuery.length()> 0) {
					orQuery.append(" and ");
					orQuery.append(offerProductTypesQuery.toString());
				}
				orQuery.append(")");
				finalQuery.append(orQuery.toString());
			}else if(cartOfferQuery.length() > 0) {
				if (finalQuery.length() != 0) {
					finalQuery.append(" and ");
				}
				orQuery.append(cartOfferQuery.toString());
				if(offerProductTypesQuery.length()> 0) {
					orQuery.append(" or ");
					orQuery.append(offerProductTypesQuery.toString());
				}
				orQuery.append(")");
				finalQuery.append(orQuery.toString());
			} else if(offerProductTypesQuery.length()> 0) {
				if (finalQuery.length() != 0) {
					finalQuery.append(" and ");
				}
				finalQuery.append(offerProductTypesQuery);
			}

		}
	}

	private boolean checkOfferProductTypeExist(Object srcRequest,PropertyDescriptor[] pdDescriptors) {
		if(srcRequest instanceof CTOfferRequest) {
			CTOfferRequest offerRequest = (CTOfferRequest)srcRequest;
			if(CollectionUtils.isNotEmpty(offerRequest.getOfferProductType()) &&
					offerRequest.getOfferProductType().size()== 1 && !offerRequest.getOfferProductType().contains("fee")){
				return true;
			}
		}
		return false;
	}

	private String buildIdListAttr(String name, List<String> values) {
		String query = "masterData(current(masterVariant(attributes(name= \"attributeNameToBeReplaced\"  and value(id  in ( attributValueToBeReplaced ))))))";
		query = query.toString().replaceAll("attributeNameToBeReplaced", name);
		String valuesString = "\"" + StringUtils.join(values , "\",\"") + "\"";
		query = query.toString().replaceAll("attributValueToBeReplaced",valuesString );
		return query;
	}

	private String buildListAttr(String name, List<String> values) {
		String query = "masterData(current(masterVariant(attributes(name= \"attributeNameToBeReplaced\"  and value  in ( attributValueToBeReplaced )))))";
		query = query.toString().replaceAll("attributeNameToBeReplaced", name);
		String valuesString = "\"" + StringUtils.join(values , "\",\"") + "\"";
		query = query.toString().replaceAll("attributValueToBeReplaced",valuesString );
		return query;
	}

	private String buildBooleanAttr(PropertyDescriptor pd, Object propertyValue) {
		String query = "masterData(current(masterVariant(attributes(name= \"attributeNameToBeReplaced\"  and value(key in ( attributValueToBeReplaced ))))))";
		query = query.toString().replaceAll("attributeNameToBeReplaced", pd.getName());
		List<String> values = (List<String>)propertyValue;
		String valuesString = "\"" + StringUtils.join(values , "\",\"") + "\"";
		query = query.toString().replaceAll("attributValueToBeReplaced",valuesString );
		return query;
	}


	private String buildEnumAttr(String name, List<String> values) {
		String query = "masterData(current(masterVariant(attributes(name= \"attributeNameToBeReplaced\"  and value(key in ( attributValueToBeReplaced ))))))";
		query = query.toString().replaceAll("attributeNameToBeReplaced", name);
		String valuesString = "\"" + StringUtils.join(values , "\",\"") + "\"";
		query = query.toString().replaceAll("attributValueToBeReplaced",valuesString );
		return query;
	}



	private Map<String, String> getIDPCTMapKeys() {

		Map<String, String> ctQueryKeyTransformationMap = new HashMap<>();
		ctQueryKeyTransformationMap.put("addOnType", "qualifyingProductsAddonTypes");
		ctQueryKeyTransformationMap.put("businessSegment", "eligibilityBusinessSegment");
		ctQueryKeyTransformationMap.put("cartContext.offerIds", "id");
		ctQueryKeyTransformationMap.put("customerSegments", "eligibilityCustomerSegments");
		ctQueryKeyTransformationMap.put("salesChannel", "eligibilitySalesChannels");
		ctQueryKeyTransformationMap.put("migrationIndicator", "offerIntent");

		return ctQueryKeyTransformationMap;
	}

}
