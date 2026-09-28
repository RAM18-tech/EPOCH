package com.dtv.dcp.epoch.processor.ott.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.OffersUtils;

/**
 * The Class DtvnServicesStandAloneProcessor.
 */
@Component
public class DtvnServicesStandAloneProcessor {

	/** The epoch client helper. */
	@Autowired
	CpopClientHelper cpopClientHelper;
	
    /** The offers utils. */
    @Autowired
    OffersUtils offersUtils;
    
    private static final Logger log = LoggerFactory.getLogger(DtvnServicesStandAloneProcessor.class);
    /**
     * Populate package group stand alone.
     * @param subscriptionsContext the subscriptions context
     * @param currentServiceInfo the current service info
     */
    public void populatePackageGroupStandAlone(OfferRequestWrapper offerRequestWrapper,CustomerSubscriptionDetail subscriptionsContext,Map<String, String> currentServiceInfo,List<String> basePackageCompatibleList) {
		
		try {
			if (Objects.nonNull(subscriptionsContext)) {
				List<String> basePackageServiceIdList = subscriptionsContext.getBasePackageServiceIdList();
				List<String> addOnServiceIdList = subscriptionsContext.getAddOnServiceIdList();
				// STANDALONE (No Video-Plan Subscription)
				if (CollectionUtils.isEmpty(basePackageServiceIdList) && !CollectionUtils.isEmpty(addOnServiceIdList)) {
					// Get StandAlone Product
					ProductObj productObj = retrieveProduct(addOnServiceIdList);
					if (Objects.nonNull(productObj)) {
						// Populating packageGroup, productStatus
						pupulateCurrentServiceInfo(productObj, currentServiceInfo);
						List<String> offerProductType = offerRequestWrapper.getOfferRequest().getOfferProductType();
						if(offerProductType.contains(Constants.VIDEO_DEVICE) || offerProductType.contains(Constants.VIDEO_ADDON)){
							// Updating CompatibleList for STANDALONE Scenario
							updateCompatibleList(offerRequestWrapper,productObj,basePackageCompatibleList);
							
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Exception handleAsyncResponsesServices...{}", e);
		}
	}
    
    private void updateCompatibleList(OfferRequestWrapper offerRequestWrapper,ProductObj productObj, List<String> basePackageCompatibleList) {
        if (Objects.nonNull(productObj) && !CollectionUtils.isEmpty(productObj.getVariants()) && Objects.nonNull(productObj.getVariants().get(0).getAttributes()))
        {
        	List<ProductWrapper> productWrappers = null;
        	if (offerRequestWrapper.isMobility()) {
        		productWrappers = productObj.getVariants().get(0).getAttributes().getCompatibleMobilityProducts();

			} else if (offerRequestWrapper.isEmployeeAccount()) {
				productWrappers = productObj.getVariants().get(0).getAttributes().getCompatibleEmployeeProducts();
			} else {
                productWrappers = productObj.getVariants().get(0).getAttributes().getCompatibleProducts();
            }
            if (!CollectionUtils.isEmpty(productWrappers)) {
            	productWrappers.stream().filter(Objects::nonNull).forEach(prods -> {
                    if (Objects.nonNull(prods.getProducts())) {
                        prods.getProducts().forEach(prod -> basePackageCompatibleList.add(prod.getKey())
                        );
                    }
                });
            }
        }
    }
    /**
     * Pupulate current service info.
     *
     * @param productObj the product obj
     * @param currentServiceInfo the current service info
     */
    private void pupulateCurrentServiceInfo(ProductObj productObj, Map<String, String>  currentServiceInfo) {
        currentServiceInfo.put("productStatus", productObj.getVariants().get(0).getAttributes().getProductStatus());
        currentServiceInfo.put("packageGroup", productObj.getVariants().get(0).getAttributes().getPackageGroup());
    }
	/**
	 * Retrieve product.
	 *
	 * @param productCodes the product codes
	 * @return the product obj
	 */
	public ProductObj retrieveProduct(List<String> productCodes) {
		CTProductResponse ctProductResponse = null;
		ProductObj filteredProduct = null;
		CTProductRequest ctProductRequest = new CTProductRequest();
		if (!CollectionUtils.isEmpty(productCodes)) {
			ctProductRequest.setProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
			ctProductRequest.setAddOnType(Arrays.asList(Constants.STANDALONE_WITH_CAPITAL_S));
			ctProductRequest.setProductCodes(productCodes);
			ctProductResponse = cpopClientHelper.getProducts(ctProductRequest);
			if(Objects.nonNull(ctProductResponse) && !CollectionUtils.isEmpty(ctProductResponse.getProducts())) {
				filteredProduct = ctProductResponse.getProducts().get(0);
			}
		}
		return filteredProduct;
	}  
}