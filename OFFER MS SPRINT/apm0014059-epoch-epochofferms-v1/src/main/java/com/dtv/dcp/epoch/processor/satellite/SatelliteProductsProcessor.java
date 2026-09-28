package com.dtv.dcp.epoch.processor.satellite;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.Code;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;

@Component
public class SatelliteProductsProcessor {

    /** The log. */
    private static Logger log = LoggerFactory.getLogger(SatelliteProductsProcessor.class);

    
	@Autowired
    CpopClientHelper cpopClientHelper;	
	
	@Autowired
	OffersUtils utils;
	
	@Autowired
	RedisCacheHelper redisCacheHelper;
	
	public CTProductResponse getProducts(ProductRequestWrapper productRequestWrapper) {
        CTProductRequest ctProductRequest = new CTProductRequest();
        List<String> pricePlanCodes = new ArrayList<>();
        try {
            BeanUtils.copyProperties(ctProductRequest, productRequestWrapper.getProductRequest());
            
            List<Code> codes = productRequestWrapper.getProductRequest().getCodes();
            
            if (null != codes && !codes.isEmpty()) {
            	
            	List<String> billingProductCodes = new ArrayList<>();
            	
    			codes.stream().filter(Objects::nonNull).forEach(code -> {
    				if(code.getPricePlanCode() != null) {
    					pricePlanCodes.add(code.getPricePlanCode());
    				} else {
    					billingProductCodes.add(code.getBillingProductCode());
    				}
    			});
    			
    			if(CollectionUtils.isNotEmpty(pricePlanCodes)) {
    				ctProductRequest.setEnablerPricePlanCode(pricePlanCodes);
    			} else {
    				ctProductRequest.setBillingProductCodes(billingProductCodes);
    			}
    			
    			// Removing Codes from the request as AWS service is not accepting both codes and billingProductCodes at the same time
    			ctProductRequest.setCodes(null);
    		}
            
        } catch (Exception e) {
        	log.error(String.format("Error in BeanUtils.copyProperties: %s", e.getMessage()));
        }

        log.debug("CTRequest: {}", ctProductRequest);
        
        
        String offerProductFamily = Objects.nonNull(ctProductRequest.getProductFamily()) && !ctProductRequest.getProductFamily().isEmpty()?ctProductRequest.getProductFamily().get(0):null;
        boolean isCouponFlow = Optional.ofNullable(ctProductRequest.getOfferTypes()).orElse(Collections.emptyList()).contains("coupon");
        // Called Only for the satellite family
        if(Constants.SATELLITE_PRODUCT_FAMILY.equalsIgnoreCase(offerProductFamily)  && !isCouponFlow) {
        	if(CollectionUtils.isNotEmpty(ctProductRequest.getSalesChannel())) {
        		ctProductRequest.setProductSalesChannel(new ArrayList<>(ctProductRequest.getSalesChannel()));	
        	}  
			ctProductRequest.setSalesChannel(null);
        	CTProductResponse ctProductResponse = cpopClientHelper.getProducts(ctProductRequest);

			if (null != productRequestWrapper.getProductRequest().getSalesChannel()) {
				String modifiedSalesChannel = utils.getModifiedSalesChannel(
						productRequestWrapper.getProductRequest().getSalesChannel(),
						productRequestWrapper.getProductRequest().getOfferActionType());
				utils.processProducts(ctProductResponse,modifiedSalesChannel);
			}
        	applyFilterOnPickCode(ctProductResponse, productRequestWrapper);
        	applyPriceFilter(ctProductResponse, productRequestWrapper);
        	setInlineProducts(ctProductRequest, ctProductResponse);
        	return ctProductResponse;
        }else {
        	 return cpopClientHelper.getProducts(ctProductRequest);
        }
    }
	
	private void applyPriceFilter(CTProductResponse ctProductResponse,
			ProductRequestWrapper productRequestWrapper) {
		if(Objects.nonNull(ctProductResponse) && CollectionUtils.isNotEmpty(ctProductResponse.getProducts())) {
			if (Objects.nonNull(productRequestWrapper) && Objects.nonNull(productRequestWrapper.getProductRequest())
					&& CollectionUtils.isNotEmpty(productRequestWrapper.getProductRequest().getCodes())) {
				productRequestWrapper.getProductRequest().getCodes().stream().filter(Objects::nonNull).forEach(code -> {
					List<ProductObj> productObjList = getProduct(code, ctProductResponse.getProducts());
					if(Objects.nonNull(productObjList)) {
						productObjList.stream().filter(Objects::nonNull).forEach(productObj -> {
							if(Objects.nonNull(productObj.getVariants())) {
								productObj.getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
									if(Constants.NA.equalsIgnoreCase(productRequestWrapper.getProductRequest().getPolicy())) {
										variant.getPrices().removeIf(price -> (Objects.nonNull(productRequestWrapper.getProductRequest().getPolicy())
												&& Constants.VIDEO_PLAN.equals(productObj.getProductType().getKey())
												&& (Objects.isNull(price.getOmsFreeSTBPolicyId())
												|| !(Arrays.asList(price.getOmsFreeSTBPolicyId().split(Constants.COMMA)).contains(Constants.DVANA1) 
												|| Arrays.asList(price.getOmsFreeSTBPolicyId().split(Constants.COMMA)).contains(Constants.DVANA2)))));
									} else {
 									    String billingReferenceId= Objects.nonNull(code.getBillingReferenceId())? code.getBillingReferenceId().replaceFirst(Constants.BILLING_REFERENEID_PREFIX, ""):null;
										variant.getPrices().removeIf(price -> (Objects.nonNull(billingReferenceId)
					        					&& !billingReferenceId.equals(price.getBillingReferenceId()))
												|| (Objects.nonNull(productRequestWrapper.getProductRequest().getPolicy())
												&& Constants.VIDEO_PLAN.equals(productObj.getProductType().getKey())
												&& (Objects.isNull(price.getOmsFreeSTBPolicyId())
												|| !Arrays.asList(price.getOmsFreeSTBPolicyId().split(Constants.COMMA))
												.contains(productRequestWrapper.getProductRequest().getPolicy()))));
									}
									if(Constants.VIDEO_PLAN.equals(productObj.getProductType().getKey())) {
										variant.getPrices().removeIf(price ->  Objects.nonNull(price.getCustomerGroup()) && price.getCustomerGroup().contains("-PNP"));
									}
									if(Objects.nonNull(productObj)
											&& !CollectionUtils.isEmpty(productObj.getVariants())
											&& Objects.nonNull(productObj.getVariants().get(0))
											&& Objects.nonNull(productObj.getVariants().get(0).getAttributes())
											&& Constants.ROADRUNNER.equalsIgnoreCase(productObj.getVariants().get(0).getAttributes().getBpType())) {
										List<String> localsBoltonPrices = redisCacheHelper.getValues(Constants.LOCALS_BOLTON_PRICE,
												Constants.SATELLITE_PRODUCT_FAMILY);
										if (CollectionUtils.isNotEmpty(localsBoltonPrices)) {
											double localsPrice = Double.valueOf(localsBoltonPrices.get(0));
											
											variant.getPrices().stream().filter(Objects::nonNull).forEach(price -> {
												double bpPrice = price.getValue().getDollarAmount();
												price.setPlanPricewithLocals(utils.getTwoDigitRoundOffValue(bpPrice + localsPrice));

											});											
										}
									}
									variant.getPrices().stream().filter(Objects::nonNull).forEach(price -> {
										if(Objects.nonNull(price.getSubCategory())) {
											variant.getAttributes().setSubCategory(price.getSubCategory());
										}
									});
								});
							}
						});
					}
				});
			}
			ctProductResponse.getProducts().removeIf(product-> !Constants.VIDEO_PLAN.equals(product.getProductType().getKey())
					&& Objects.nonNull(product.getVariants().get(0).getPrices())
					&& product.getVariants().get(0).getPrices().isEmpty());
		
		}
				
	}
	
	private List<ProductObj> getProduct(Code code, List<ProductObj> products) {
		List<ProductObj> matchingObject = null;
		if (Objects.nonNull(products)) {
            matchingObject = products.stream().filter(Objects::nonNull)
                    .filter(p -> (Objects.nonNull(p.getVariants().get(0).getAttributes().getBillingProductCode()) 
                    && p.getVariants().get(0).getAttributes().getBillingProductCode().equals(code.getBillingProductCode()))
                    || (Objects.nonNull(p.getVariants().get(0).getAttributes().getEnablerPricePlanCode()) 
                    && p.getVariants().get(0).getAttributes().getEnablerPricePlanCode().equals(code.getPricePlanCode())))
                    .collect(Collectors.toList());
		}
		return matchingObject;
	}

	/**
	 * This method is used to associate the product and objects 'obj'
	 * @param ctProductRequest
	 * @param ctProductResponse
	 * @return ctProductResponse
	 */
	private CTProductResponse setInlineProducts(CTProductRequest ctProductRequest,	CTProductResponse ctProductResponse) {

		if (null != ctProductResponse) {
			
			//Get list included product ids
			List<String> includedProductIdList = getIncludedProductIds(ctProductResponse);
			
			if (!includedProductIdList.isEmpty()) {
				
				//Removing the parent product id from the request
				ctProductRequest.setBillingProductCodes(null);
				ctProductRequest.setEnablerPricePlanCode(null);
				//setting  list of productIds to the request
				ctProductRequest.setProductIds(includedProductIdList);
				
				// Get CTProductResponse for all the Included products
				CTProductResponse includedProductObjects = cpopClientHelper.getProducts(ctProductRequest);
				
				//Associate the product object 'obj' to Included projects
				if(Objects.nonNull(includedProductObjects)){
					associateProductObject(ctProductResponse, includedProductObjects);
				}
			}
			
		}
		setAvailableAttribute(ctProductResponse);
		return ctProductResponse;
	}
	
	//setting isAvailable attribute for GNF base packages
	private void setAvailableAttribute(CTProductResponse ctProductResponse) {
		if(Objects.nonNull(ctProductResponse) && CollectionUtils.isNotEmpty(ctProductResponse.getProducts())) {
			ctProductResponse.getProducts().stream().filter(Objects::nonNull).forEach(product->{
				if(Objects.nonNull(product.getProductType()) && StringUtils.isNotEmpty(product.getProductType().getKey()) 
						&& Constants.VIDEO_PLAN.equalsIgnoreCase(product.getProductType().getKey())
						&& CollectionUtils.isNotEmpty(product.getVariants())) {
					product.getVariants().stream().filter(Objects::nonNull).forEach(variant->{
						if(Objects.nonNull(variant.getAttributes()) && Objects.nonNull(variant.getAttributes().getEndDate())) {
							try {
								SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
						        sdf.setTimeZone(TimeZone.getTimeZone(Constants.PST));
						        Date currentDate=new SimpleDateFormat(Constants.DATE_TIME_FORMAT).parse(sdf.format(new Date()));
						        String currentDateStr = sdf.format(currentDate);
						        boolean value=OffersUtils.validateActiveDates(currentDateStr,OffersUtils.getFormattedDate(variant.getAttributes().getEndDate()));
						        if(!value) {
						        	variant.getAttributes().setIsAvailable(value);
						        }
							} catch (ParseException e) {
								log.error("Invalid date present in the request - isActive() ", e);
							}	
						}	
					});
				}
			});
		}
	}
	
	/**
	 * This method is used to associate product object to Included projects
	 * @param ctProductResponse
	 * @param includedProductObjects
	 */
	private void associateProductObject(CTProductResponse ctProductResponse, CTProductResponse includedProductObjects) {
		ctProductResponse.getProducts().stream().filter(Objects::nonNull).forEach(products -> {
			if(hasIncludedProducts(products)) {
					products.getVariants().get(0).getAttributes()
							.getIncludedProducts().get(0).getProducts().stream().filter(Objects::nonNull).forEach(includedProduct -> {
					includedProduct.setObj(getProductObjByBillingProductId(includedProductObjects, includedProduct.getId()));
				});
			}
			
		});
	}
	
	/** This method is used to get the list product ids from Included products
	 * @param ctProductResponse
	 * @return list
	 */
	private List<String> getIncludedProductIds(CTProductResponse ctProductResponse) {
		List<String> inlcudedProductIdList = new ArrayList<String>();
		ctProductResponse.getProducts().stream().filter(Objects::nonNull).forEach(products -> {
			if(hasIncludedProducts(products)) {
					products.getVariants().get(0).getAttributes()
							.getIncludedProducts().get(0).getProducts().stream().filter(Objects::nonNull).forEach(includedProduct -> {
					inlcudedProductIdList.add(includedProduct.getId());
				});
			}
		});
		return inlcudedProductIdList;
	}
	
	/**
	 * This method is used to find the ProductObj from CTProductResponse using the attribute 'id'
	 * @param includedProductObjects
	 * @param id
	 * @return ProductObj
	 */
    private ProductObj getProductObjByBillingProductId(CTProductResponse includedProductObjects, String id) {

    	if (Objects.nonNull(includedProductObjects.getProducts())) {
            Optional<ProductObj> matchingObject = includedProductObjects.getProducts().stream().
                    filter(p -> Objects.nonNull(p.getId()) 
                    		&& p.getId().equalsIgnoreCase(id)).
                    findFirst();
            if (matchingObject.isPresent()) {
                return matchingObject.get();
            } 
        } 
        return null;
   }
   /**
    * This method is used check if the ProductObj has includedProducts.
    * @param productObj
    * @return boolean
    */
   private boolean hasIncludedProducts(ProductObj productObj) {
		if(Objects.nonNull(productObj)
				&& Objects.nonNull(productObj.getVariants())
				&& !productObj.getVariants().isEmpty()
				&& Objects.nonNull(productObj.getVariants().get(0))
				&& Objects.nonNull(productObj.getVariants().get(0).getAttributes())
				&& Objects.nonNull(productObj.getVariants().get(0).getAttributes().getIncludedProducts())
				&& !productObj.getVariants().get(0).getAttributes().getIncludedProducts().isEmpty()
				&& Objects.nonNull(productObj.getVariants().get(0).getAttributes().getIncludedProducts().get(0))
				&& Objects.nonNull(productObj.getVariants().get(0).getAttributes().getIncludedProducts().get(0).getProducts())){
			return true;
		}
		return false;
   }
   
   /**
	 * Remove products if pick code is not matching
	 * 
	 * @param ctProductResponse
	 * @param productRequestWrapper
	 */
	private void applyFilterOnPickCode(CTProductResponse ctProductResponse,
			ProductRequestWrapper productRequestWrapper) {
		if (productRequestWrapper != null && productRequestWrapper.getProductRequest() != null) {
			List<Code> codes = productRequestWrapper.getProductRequest().getCodes();

			if (codes != null && CollectionUtils.isNotEmpty(codes)) {
				codes.stream().filter(Objects::nonNull).forEach(code -> {
					if (code.getPricePlanCode() != null && !code.getPricePlanCode().isEmpty()
							&& code.getPickCode() != null && !code.getPickCode().isEmpty() && ctProductResponse != null
							&& CollectionUtils.isNotEmpty(ctProductResponse.getProducts())) {
						//Remove the product if pick code in request is not matching with the pick code of the product
						ctProductResponse.getProducts().removeIf(product -> isPickCodeNotMatching(product, code));
						ctProductResponse.setTotal(ctProductResponse.getProducts().size());
						ctProductResponse.setCount(ctProductResponse.getProducts().size());
					}
				});
			}
		}
	}
	
	/**
	 * Returns true if pick code in request is not matching with the pick code of the product
	 * 
	 * @param product
	 * @param code
	 * @return
	 */
	private Boolean isPickCodeNotMatching(ProductObj product, Code code) {
		return (Objects.nonNull(product) && Objects.nonNull(product.getProductType())
				&& Constants.VIDEO_ADDON.equals(product.getProductType().getKey())
				&& CollectionUtils.isNotEmpty(product.getVariants()) && Objects.nonNull(product.getVariants().get(0))
				&& Objects.nonNull(product.getVariants().get(0).getAttributes()) && code.getPricePlanCode() != null
				&& code.getPricePlanCode()
						.equals(product.getVariants().get(0).getAttributes().getEnablerPricePlanCode())
				&& code.getPickCode() != null
				&& !code.getPickCode().equals(getPickCodeFromBillingParams(product.getVariants().get(0).getAttributes())));
	}
  
	/**
	 * Get the pick code from billing params
	 * 
	 * @param attributes
	 * @return
	 */
	private String getPickCodeFromBillingParams(Attributes attributes) {
		List<String> pickCode = new ArrayList<>();
		if (Objects.nonNull(attributes) && CollectionUtils.isNotEmpty(attributes.getBillingParams())) {
			attributes.getBillingParams().stream().filter(Objects::nonNull).forEach(billingParam -> {
				if (Constants.PICK_CODE.equals(billingParam.getName())) {
					pickCode.add(billingParam.getValue());
				}
			});
			if (CollectionUtils.isNotEmpty(pickCode)) {
				return pickCode.get(0);
			}
		}
		return null;
	}
	
	/**
	 * Get all products based on product type
	 * 
	 * @param productType
	 * @return
	 */
	public CTProductResponse getProductsByType(String productType) {        
       return cpopClientHelper.getProductsByType(productType);
    }

}
