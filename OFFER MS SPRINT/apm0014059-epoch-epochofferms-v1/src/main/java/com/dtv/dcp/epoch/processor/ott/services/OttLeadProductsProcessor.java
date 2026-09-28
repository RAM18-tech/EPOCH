package com.dtv.dcp.epoch.processor.ott.services;

import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.DtvnMidasRule;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class OttLeadProductsProcessor {

    private static final Logger log = LoggerFactory.getLogger(OttLeadProductsProcessor.class);

    @Autowired
    OffersUtils offersUtils;
    
    @Autowired 
    OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;
    /**
	 *
	 * @param availableCtOfferList
	 * @param productRequestWrapper
     * @param customerSubscriptionDetail 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void populateLeadAttribute(List<ProductObj> availableCtProductList,
			ProductRequestWrapper productRequestWrapper) {

		log.debug("Start of OttLeadProductsProcessor.populateLeadAttribute() method..");
		String usertype =ottServicesOffersProcessorHelper.findSalesChannel(productRequestWrapper.getProductRequest().getSalesChannel(), "");
		List<String> contractIndicator = productRequestWrapper.getProductRequest().getContractApplicable()!=null ? productRequestWrapper.getProductRequest().getContractApplicable():null;
		try {
			if (Optional.ofNullable(availableCtProductList).isPresent() && !availableCtProductList.isEmpty()
					&& CollectionUtils.isNotEmpty(contractIndicator)) {
				availableCtProductList.forEach(ctProduct -> processCTProduct(ctProduct,
						contractIndicator.get(0), usertype));
			}
		} catch (Exception e) {
			log.error(" Exception Occured while populating lead attribute OttLeadProductsProcessor.populateLeadAttribute(): ", e);
		}
		log.debug("End of OttLeadProductsProcessor.populateLeadAttribute() method..");
	}
	
	public void processCTProduct(ProductObj product, String contractIntent, String userType) {
		log.debug("Start of OttLeadProductsProcessor.processCTProduct() method..");
		List<DtvnMidasRule> dtvnMidasRuleInfoList = offersUtils.fetchDtvnMidasRules();
		if(product != null && product.getVariants() != null && product.getVariants().get(0) !=null) {
	        Attributes attributes =  product.getVariants().get(0).getAttributes() !=null ? product.getVariants().get(0).getAttributes() : null;
			if (Optional.ofNullable(dtvnMidasRuleInfoList).isPresent() && !dtvnMidasRuleInfoList.isEmpty()) {
				dtvnMidasRuleInfoList.stream().forEach(dtvnMidasRule -> {
					if (dtvnMidasRule != null && dtvnMidasRule.getSalesChannel() != null
							&& dtvnMidasRule.getSalesChannel().contains(userType)
							&& dtvnMidasRule.getContractIntent() != null
							&& ((dtvnMidasRule.getContractIntent().equalsIgnoreCase("Contract") && "contract".equalsIgnoreCase(contractIntent))
									|| (dtvnMidasRule.getContractIntent().equalsIgnoreCase("Retail") && "non-contract".equalsIgnoreCase(contractIntent))
									|| (dtvnMidasRule.getContractIntent().equalsIgnoreCase("EDSP") && "EDSP".equalsIgnoreCase(contractIntent)))
							&& ((attributes !=null && attributes.getPackageGroup() != null 
									&& !checkExistingGroupNumberValue(dtvnMidasRule.getExistingGroup()))
									&& Long.parseLong(attributes.getPackageGroup())==Long.parseLong(dtvnMidasRule.getExistingGroup())
											&& Long.parseLong(attributes.getPackageGroup())==dtvnMidasRule.getLeadGroup())) {
						product.setLeadOffer(true);
					}
				});
			}
			if(product.isLeadOffer() == null) {
				product.setLeadOffer(false);
			}
			if (log.isDebugEnabled()) {
				log.debug("Product Processed:::"+product.getCode()+"::is lead offer::"+product.isLeadOffer());
			}
		}
		log.debug("Start of OttLeadProductsProcessor.processCTProduct() method..");
	}
	/**
	 * @param existingGroup
	 * @return boolean
	 * Method to check whether the given String can be parsed to Long or not
	 */
	public static boolean checkExistingGroupNumberValue(String existingGroup) {
		boolean result = false;
		try {
			Long.parseLong(existingGroup);
		} catch (Exception e) {
			result = true;
		}
		return result;
	}
}
