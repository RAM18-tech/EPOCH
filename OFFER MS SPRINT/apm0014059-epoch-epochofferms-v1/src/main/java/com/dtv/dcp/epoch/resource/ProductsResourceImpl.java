package com.dtv.dcp.epoch.resource;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.product.IncludeProductWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.representation.Content;
import com.dtv.dcp.epoch.representation.Error;
import com.dtv.dcp.epoch.service.ott.OttProducts;
import com.dtv.dcp.epoch.service.satellite.SatelliteProductService;
import com.dtv.dcp.epoch.service.watchtv.WatchTVProductsService;
import com.dtv.dcp.epoch.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The Class ProductsResourceImpl.
 *
 * Created by ac2201 on 08/08/2019.
 */
@Controller
@PropertySource("classpath:remove-attributes-from-satellite-response.properties")
public class ProductsResourceImpl implements ProductsResource {

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(ProductsResourceImpl.class);

	/** The Constant for ERROR_MESSAGE. */
	private static final String ERROR_MESSAGE = "Unknown error occurred in ProductsResourceImpl.getProducts() method.";

	/** The Constant SOURCE. */
	private static final String SOURCE = "ProductsResourceImpl";
	
	private static final String ENDPOINT = "API_NAME:EPOCH_GETPRODUCTS";

	@Autowired
	private OttProducts ottProductsService;

	@Autowired
	private CTOfferRequestHelper ctOfferRequestHelper;

	@Autowired
	private SatelliteProductService setelliteProductService;

    @Autowired
    private WatchTVProductsService watchTVProductService;

	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	@Autowired
	RedisCacheHelper redisCacheHelper;

	@Autowired
	private OffersUtils offersUtils;

	@Autowired
	CPOPProductsHelper cpopProductsHelper;

    @Value("#{'${attributesToRemoveInResponse}'.split(',')}") 
    private String[] attributesToRemoveInResponse;
    	
	@Override
	public ResponseEntity getProducts(HttpHeaders headers,  @NotNull ProductRequest productsRequest) {
		MDC.put(Constants.TRACE_ID, null != headers ? headers.getFirst(Constants.TRACE_ID):"");
		log.info("getProducts headers {} ",headers);
		Content<CTProductResponse> content = null;
		CTProductResponse baseProductsResponse = null;
		long startTimeInMillies = System.currentTimeMillis();
		log.info("{} CLIENT_REQUEST:[{}] TRACE_ID:[{}]",ENDPOINT,JsonService.getJsonFromObject(productsRequest),headers.getFirst(Constants.TRACE_ID));
		try {
			FeatureManagerHelper.httpHeaders = headers;
			ProductRequestWrapper productRequestWrapper = ctOfferRequestHelper.preProcessProductRequest(headers, productsRequest);
			  if (Optional.ofNullable(productsRequest.getProductFamily()).isPresent() && !productsRequest.getProductFamily().isEmpty() && !productsRequest.getProductFamily().contains(null)) {
						  List<String> offerProductFamily = productsRequest.getProductFamily();
						  if (offerProductFamily.stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
							  log.info("EPOCH_GETPRODUCTS_OTT_INVOKED");
							  log.debug("Start of OTT GET PRODUCT method..");
							  baseProductsResponse = ottProductsService.getProducts(headers,productRequestWrapper);
							  log.debug("end of OTT GET PRODUCT method..");
							  if (productRequestWrapper!=null && productRequestWrapper.isEmployeeAccount()) {
								  // Remove AutoRenewable field and messages for Merlin
								  baseProductsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseProductsResponse,
										  CTProductResponse.class, Stream.of("autoRenewMessages","conflictingProductsReference","complianceRank","billingProductGroup").toArray(String[]::new));
								  // Set "isAutoRenewable" to false for Employee Service flow
								  if (Optional.ofNullable(baseProductsResponse.getProducts()).isPresent()) {
					                    baseProductsResponse.getProducts().stream().filter(Objects::nonNull)
					                            .forEach(productObj -> productObj.getVariants().stream().filter(Objects::nonNull)
					                                    .forEach(variant -> variant.getAttributes().setIsAutoRenewable(false)));
					                }
							  }else {
								  //Temporary fix to remove ribbonTextsByKey attribute from the response for sales channel directvOnline and IapPartnerAccountType FIRETV
								  if(productsRequest.getCustomerContext() != null && productsRequest.getCustomerContext().getOtt() != null
										  && "FIRETV".equalsIgnoreCase(productsRequest.getCustomerContext().getOtt().getIapPartnerAccountType())
										  && CollectionUtils.isNotEmpty(productsRequest.getSalesChannel()) && productsRequest.getSalesChannel().contains("directvOnline")) {
									  baseProductsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseProductsResponse,
												  CTProductResponse.class, Stream.of("opusSubChannels", "opusChannels", "opusStoreIds", "ribbonTextsByKey","complianceRank","billingProductGroup","conflictingProductsReference").toArray(String[]::new));
								  } else {
									  baseProductsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseProductsResponse,
												  CTProductResponse.class, Stream.of("opusSubChannels", "opusChannels", "opusStoreIds","complianceRank","billingProductGroup","conflictingProductsReference").toArray(String[]::new));
								  }

								  if (!featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
									  baseProductsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseProductsResponse,
											  CTProductResponse.class, Stream.of("delayProvisioningReason", "delayProvisioning", "delayProvisioningMessagesByKey").toArray(String[]::new));
								  }
							  }
							  
							//remove CompatibleProducts for Taz/tazbyod
							  
							  if(Optional.ofNullable(productsRequest.getContractIndicator()).isPresent() && (productsRequest.getContractIndicator().stream().allMatch(Predicate.isEqual(Constants.TAZ_STRING)) ||
									  (productsRequest.getContractIndicator().stream().allMatch(Predicate.isEqual(Constants.TAZBYOD_STRING))) ||
									  (productsRequest.getContractIndicator().stream().allMatch(Predicate.isEqual(Constants.TAZCONTRACT_STRING))))) {
								  baseProductsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseProductsResponse,
										  CTProductResponse.class, Stream.of("compatibleProducts").toArray(String[]::new));
							  }
							  
								// subCategoryByKey changes at product level variant
								if (Optional.ofNullable(productsRequest.getSalesChannel()).isPresent()
										&& !productsRequest.getSalesChannel().isEmpty()) {
									String modifiedSalesChannel = getSalesChannelForDisplayType(productsRequest.getSalesChannel(), productsRequest);
									if (Optional.ofNullable(baseProductsResponse.getProducts()).isPresent()) {
										baseProductsResponse.getProducts().stream().filter(Objects::nonNull)
												.forEach(productObj -> productObj.getVariants().stream()
														.filter(Objects::nonNull).forEach(variant -> {
															if (Objects.nonNull(variant.getAttributes())
																	&& Objects.nonNull(variant.getAttributes()
																			.getSubCategoryByKey())
																	&& (variant.getAttributes().getSubCategoryByKey()
																			.size() > 0)) {
																variant.getAttributes().getSubCategoryByKey().stream()
																		.filter(Objects::nonNull)
																		.forEach(subCategory -> {
																			if (subCategory.getKey().equalsIgnoreCase(modifiedSalesChannel)) {
																				variant.getAttributes().setSubCategory(subCategory.getValue());
																			}
																		});
															}
														}));
									}
								}
								
								// displayTypeByChannel changes at product level variant
								if (Optional.ofNullable(productsRequest.getSalesChannel()).isPresent()
										&& !productsRequest.getSalesChannel().isEmpty())
										{
									String modifiedSalesChannel = getModifiedSalesChannel(
											productsRequest.getSalesChannel(),
											productsRequest);
											String displayTypesalesChannel=getSalesChannelForDisplayType(productsRequest.getSalesChannel(),
													productsRequest);
									if (Objects.nonNull(baseProductsResponse)
											&& Optional.ofNullable(baseProductsResponse.getProducts()).isPresent()) {
										baseProductsResponse.getProducts().stream().filter(Objects::nonNull)
												.forEach(productObj -> productObj.getVariants().stream()
														.filter(Objects::nonNull).forEach(variant -> {
															if (Objects.nonNull(variant.getAttributes())
																	&& Objects.nonNull(variant.getAttributes()
																			.getDisplayTypeByKey())
																	&& (variant.getAttributes().getDisplayTypeByKey()
																			.size() > 0)) {
																variant.getAttributes().getDisplayTypeByKey().stream()
																		.filter(Objects::nonNull)
																		.forEach(displayType -> {
																				if (displayType.getDisplayTypeKey().equalsIgnoreCase(modifiedSalesChannel)) {
																					variant.getAttributes().setDisplayType(
																							displayType.getDisplayTypeValue());
																			}
																			if (displayType.getDisplayTypeKey().equalsIgnoreCase(displayTypesalesChannel)) {
																				variant.getAttributes().setDisplayType(
																						displayType.getDisplayTypeValue());
																			}
																		});
																variant.getAttributes().setDisplayTypeByKey(null);
															}
														}));
									}
								}
							  // SLS-IXP-FLAG changes
							  if(offersUtils.isSlsGetProductServicesEnabled()) {
								  baseProductsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseProductsResponse,
										  CTProductResponse.class, Stream.of("subCategoryByKey", "ineligibleAccountStatuses").toArray(String[]::new));
							  }
							  else
							  {
								  baseProductsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseProductsResponse,
										  CTProductResponse.class, Stream.of("subCategoryByKey").toArray(String[]::new));

							  }

							  if (baseProductsResponse != null
									  && productsRequest != null
									  && productsRequest.getProductTypes() != null
									  && productsRequest.getProductTypes().stream().anyMatch(type -> Constants.VIDEO_PLAN.equalsIgnoreCase(type))
									  && productsRequest.getBusinessSegment() != null
									  && productsRequest.getBusinessSegment().stream().anyMatch(segment -> Constants.MDU.equalsIgnoreCase(segment))
									  && featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_MDU_INCLUDED_PRODUCT_EXCLUSIONS)) {
								  offersUtils.filterMDUIncludedProducts(baseProductsResponse);
							  }
							  //iap-remove-included-products-enabled changes start
							  if (featureManagerHelper.isEnabled(Constants.FEATURE_IAP_REMOVE_INCLUDED_PRODUCTS_ENABLED) && productsRequest.getSalesChannel() != null
									  && !productsRequest.getSalesChannel().contains(Constants.OEM_IAPFIRETV)
									  && !productsRequest.getSalesChannel().contains(Constants.OEM_IAP_GOOGLE)
									  && !productsRequest.getSalesChannel().contains(Constants.OEM_IAPROKUTV)) {
								  if (baseProductsResponse != null && baseProductsResponse.getProducts() != null) {
									  Map<String, List<String>> productsToRemoveMap = offersUtils.getIAPRemoveProductsFromIncludedProductsFromConfig();
									  baseProductsResponse.getProducts().stream()
											  .filter(Objects::nonNull)
											  .forEach(productObj -> {
												  if (productsToRemoveMap.keySet().stream().anyMatch(key -> key.equalsIgnoreCase(productObj.getCode()))
														  && productObj.getVariants() != null
														  && !productObj.getVariants().isEmpty()
														  && productObj.getVariants().get(0).getAttributes() != null
														  && productObj.getVariants().get(0).getAttributes().getIncludedProducts() != null) {

													  List<IncludeProductWrapper> includedProducts = productObj.getVariants().get(0).getAttributes().getIncludedProducts();
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
														  productObj.getVariants().get(0).getAttributes().setIncludedProducts(null);
													  }
												  }
											  });
								  }
							  }
							  //iap-remove-included-products-enabled changes end
							  //delayProvisioning changes start
						  List<String> attributesToFilter = new ArrayList<>();
						  attributesToFilter.addAll(Arrays.asList("globalMessagesByKey","delayProvisioningReasons","attributePricingCriteria","conflictingPriceTiers"));
							  if(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
								  Map<String, Boolean> reqDelayProvisioningProductsMap = offersUtils.getDelayProvisioningProductsFromRequest(productsRequest);
								  String salesChannel = Optional.ofNullable(productsRequest)
										  .map(ProductRequest::getSalesChannel)
										  .filter(CollectionUtils::isNotEmpty)
										  .map(salesChannels -> salesChannels.get(0))
										  .orElse(null);
								  Optional.ofNullable(baseProductsResponse)
										  .map(CTProductResponse::getProducts)
										  .orElse(Collections.emptyList())
										  .stream()
										  .filter(Objects::nonNull)
										  .forEach(productObj -> offersUtils.processDelayProvisioningProducts(productObj, reqDelayProvisioningProductsMap, salesChannel, productsRequest, offersUtils));
							  } else {
								  // Remove delayProvisioning related attributes from response when feature flag is disabled
								  attributesToFilter.addAll(Arrays.asList("delayProvisioning", "delayProvisioningReason", "delayProvisioningMessagesByKey"));
							  }
							  baseProductsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseProductsResponse,
									  CTProductResponse.class, attributesToFilter.toArray(new String[0]));
							  //delayProvisioning changes end
						  }
				  if (offerProductFamily.stream().allMatch(Predicate.isEqual(Constants.SATELLITE_PRODUCT_FAMILY))) {
							  log.debug("EPOCH_GETPRODUCTS_SATELLITE_INVOKED");
							  log.debug("Start of Satellite GET PRODUCT method..");
							  baseProductsResponse = setelliteProductService.getProducts(headers,productRequestWrapper.getProductRequest());
							  if (OffersUtils.needsAttributeRemoval(productsRequest.getProductFamily(),productsRequest.getOfferTypes(),productsRequest.getProductTypes())) {
								  baseProductsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseProductsResponse,
									  CTProductResponse.class, Stream.of(attributesToRemoveInResponse).toArray(String[]::new));
							  }
							  log.debug("end of Satellite GET OFFER method..");
						  }
		                  if (offerProductFamily.stream().allMatch(Predicate.isEqual(Constants.WATCHTV_PRODUCT_FAMILY))) {
							  log.info("EPOCH_GETPRODUCTS_WATCHTV_INVOKED");
		                      log.debug("Start of watchTV GET PRODUCT method..");
		                      baseProductsResponse = watchTVProductService.getProducts(headers,productRequestWrapper); 
		                      log.debug("end of Satellite GET OFFER method..");
		                  }
			  }
			  
			if (baseProductsResponse == null) {
				log.debug("baseProductsResponse..");
				throw new ServiceException(ErrorMessages.CTLG_WIRELESS_ERROR_INVALID_REQUEST, "Unable to find resource");
				//return baseProductsResponse;
			}

			log.debug("end of OTT GET PRODUCT method..");
			log.info("EPOCH_GETPRODUCTS_SUCCESS");
			content = new Content<>(baseProductsResponse);

		} catch (ServiceException serviceException) {
			log.error(SOURCE + "getProducts() :  ServiceException= ", serviceException);
			log.info("EPOCH_GETPRODUCTS_FAILED");
			log.error("{} STATUS:FAILED EPOCH_GETPRODUCTS_FAILED- [{}]",ENDPOINT, serviceException.getError());
			Error error =new Error(serviceException.getError());
			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));
			//throw serviceEx;
		} catch (Exception ex) {
			log.error(ERROR_MESSAGE, ex);
			log.info("EPOCH_GETPRODUCTS_FAILED");
			log.error("{} STATUS:FAILED EPOCH_GETPRODUCTS_FAILED-[{}]",ENDPOINT, ex.getMessage());
			if (Optional.ofNullable(productsRequest.getProductFamily()).isPresent()
					&& !productsRequest.getProductFamily().isEmpty()
					&& !productsRequest.getProductFamily().contains(null)
					&& productsRequest.getProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				ServiceException serviceException =	new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001, ex);
				Error error =new Error(serviceException.getError());
				return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));
            } else {
            	ServiceException serviceException =	new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001, ex);
    			Error error =new Error(serviceException.getError());
    			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));          	
            }
		}
		log.debug("End of ProductsResourceImpl.getProducts() method..");
		log.info("{} EPOCH_GETPRODUCTS_EXECUTION_TIME-[{}] FOR SALES_CHANNEL-[{}]", ENDPOINT,
				(System.currentTimeMillis() - startTimeInMillies),OffersUtils.sanitizeData(productsRequest.getSalesChannel()));
		return ResponseEntity.ok(baseProductsResponse);
	}

		
		private String getModifiedSalesChannel(List<String> salesChannel,ProductRequest productRequest ) {
		String modifiedSalesChannel = salesChannel.get(0);
		if (salesChannel.contains(Constants.DIRECTV_ONLINE)
				&& Objects.nonNull( productRequest.getCustomerContext())
				&& Objects.nonNull(productRequest.getCustomerContext().getExistingProductFamily())
				) {
			modifiedSalesChannel = Constants.DIRECTV_ONLINE_SERVICES;
		} else if (salesChannel.contains(Constants.DIRECTV_ONLINE)
				&& Objects.isNull( productRequest.getCustomerContext())) {
			modifiedSalesChannel = Constants.DIRECTV_ONLINE_SALES;
		}else if (salesChannel.contains(Constants.ASSISTED_SALES)
				&& Objects.isNull( productRequest.getCustomerContext())) {
			modifiedSalesChannel = Constants.ASSISTED_SALES_SALES;
		}
		return modifiedSalesChannel;
	}
	public String getSalesChannelForDisplayType(List<String> salesChannel ,ProductRequest productRequest) {
		String salesChnl = salesChannel.get(0);
		String flowType="";
		if( Objects.nonNull(productRequest.getCustomerContext())
				&& Objects.nonNull(productRequest.getCustomerContext().getExistingProductFamily()))
		{
			flowType = "Services";

		} else if ( Objects.isNull( productRequest.getCustomerContext()))
		{
			flowType = "Sales";
		}
		return salesChnl + flowType;
	}

}
