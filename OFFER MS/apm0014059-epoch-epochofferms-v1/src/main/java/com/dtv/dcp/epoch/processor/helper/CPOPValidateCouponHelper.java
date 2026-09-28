package com.dtv.dcp.epoch.processor.helper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.validatecoupons.Coupon;
import com.dtv.dcp.epoch.model.common.validatecoupons.CouponOfferAttributes;
import com.dtv.dcp.epoch.model.common.validatecoupons.CouponProductAttributes;
import com.dtv.dcp.epoch.model.common.validatecoupons.CouponResponse;
import com.dtv.dcp.epoch.model.common.validatecoupons.Offers;
import com.dtv.dcp.epoch.model.common.validatecoupons.Products;
import com.dtv.dcp.epoch.model.common.validatecoupons.Variant;
import com.dtv.dcp.epoch.model.ct.eligibility.Eligibility;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.customergraph.CustomerCouponsResults;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessorUCCHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;

public class CPOPValidateCouponHelper {

	@Autowired
	FeatureManagerHelper featureManagerHelper;

	SatelliteServicesOffersProcessorUCCHelper satelliteServicesOffersProcessorUCCHelper = new SatelliteServicesOffersProcessorUCCHelper();
	private static final Logger log = LoggerFactory.getLogger(CPOPValidateCouponHelper.class);

    public List<Products> processProductsResponse(List<ProductObj> products) {
    	
    	List<Products> processedProducts =   new ArrayList<Products>();           
    	products.stream().forEach(ctproduct -> {
    		if(Objects.nonNull(ctproduct))
    		{    		
    			Products product = new Products();

    			product.setCode(ctproduct.getCode());
    			product.setId(ctproduct.getId());
    			product.setTypeId(ctproduct.getProductType().getTypeId());
    			product.setProductType(ctproduct.getProductType());
    			product.setBenefitContinuation(ctproduct.getBenefitContinuation());
    			product.setName(ctproduct.getName());
    			product.setDescription(ctproduct.getDescription());
    			//product.setCategories(ctproduct.getCategories());
    			product.setDescription(ctproduct.getDescription());
    			List<Variant> variantList = new ArrayList<Variant>(); 
    			ctproduct.getVariants().stream().forEach(ctvariants -> {
    				
    				Variant variant = new Variant();
    				
    				variant.setId(ctvariants.getId());
    				variant.setSku(ctvariants.getSku());
    				variant.setKey(ctvariants.getKey());
    				variant.setPrices(ctvariants.getPrices());
    				variant.setContentImages(ctvariants.getContentImages());
    				variant.setProductImages(ctvariants.getProductImages());
    				
    				CouponProductAttributes attributes = new CouponProductAttributes();
    				attributes.setDisplayName(ctvariants.getAttributes().getDisplayName());
    				attributes.setDisplayNamesByKey(ctvariants.getAttributes().getDisplayNamesByKey());
    				attributes.setDescriptionsByKey(ctvariants.getAttributes().getDescriptionsByKey());
    				attributes.setBillingProductCode(ctvariants.getAttributes().getBillingProductCode());
    				attributes.setBillingCode(ctvariants.getAttributes().getBillingCode());
    				attributes.setBillingProductId(ctvariants.getAttributes().getBillingProductId());
    				attributes.setStartDate(ctvariants.getAttributes().getStartDate());
    				attributes.setEndDate(ctvariants.getAttributes().getEndDate());
    				attributes.setCreatedAt(ctvariants.getAttributes().getCreationDate());
    				attributes.setCampaignStartDate(ctvariants.getAttributes().getCampaignStartDate());
    				attributes.setCampaignEndDate(ctvariants.getAttributes().getCampaignEndDate());
    				attributes.setCampaignName(ctvariants.getAttributes().getCampaignName());
    				attributes.setCampaignDesc(ctvariants.getAttributes().getCampaignDesc());
    				attributes.setCouponType(ctvariants.getAttributes().getCouponType());
    				attributes.setCampaignCategory(ctvariants.getAttributes().getCampaignCategory());
    				attributes.setAutoRegister(ctvariants.getAttributes().getAutoRegister());
    				attributes.setLockDuration(ctvariants.getAttributes().getLockDuration());
    				attributes.setAutoRegister(ctvariants.getAttributes().getAutoRegister());
    				attributes.setNumberOfCoupons(ctvariants.getAttributes().getNumberOfCoupons());
    				attributes.setStudioSponsorByATT(ctvariants.getAttributes().getStudioSponsorByATT());
    				attributes.setCampaignMessaging(ctvariants.getAttributes().getCampaignMessaging());
     				attributes.setCampaignEligibility(satelliteServicesOffersProcessorUCCHelper.getEligibiity(ctvariants.getAttributes().getCampaignEligibility()));
    				if (null != attributes.getCampaignEligibility()) {
    					attributes.getCampaignEligibility().forEach(campEligibility -> {
    						if (null != campEligibility.getContentCategory() 
    								&& (Constants.CONTENT_CATEGORY_YES).equalsIgnoreCase(campEligibility.getContentCategory())) {
    							campEligibility.setContentCategory(Constants.CONTENT_CATEGORY_NONADULT);
    						} else if (null != campEligibility.getContentCategory() 
    								&& (Constants.CONTENT_CATEGORY_NO).equalsIgnoreCase(campEligibility.getContentCategory())) {
    							campEligibility.setContentCategory(Constants.CONTENT_CATEGORY_ADULT);
    						}
    					});
    				}
    				if(null == ctvariants.getAttributes().getRedemptionLimit()) {
						attributes.setRedemptionLimit("");
					}else{
						attributes.setRedemptionLimit(getDecimalPoint(ctvariants.getAttributes().getRedemptionLimit()));
					}
    				variant.setAttributes(attributes);

//    				attributes.setBusinessSegment(ctvariants.getAttributes().getBusinessSegment());
//    				attributes.setProvisioningCode(ctvariants.getAttributes().getProvisioningCode());
//    				attributes.setCongratsMessage(ctvariants.getAttributes().getCongratsMessage());
//    				attributes.setSalesChannel(ctvariants.getAttributes().getSalesChannel());
//    				attributes.setRanking(ctvariants.getAttributes().getRanking());
//    				attributes.setAttProject(ctvariants.getAttributes().getAttProject());
//    				attributes.setProductFamily(ctvariants.getAttributes().getProductFamily());
//    				attributes.setPackageGroup(ctvariants.getAttributes().getPackageGroup());
//    				attributes.setFufillmentAttribute1(ctvariants.getAttributes().getFufillmentAttribute1());
//    				attributes.setFufillmentAttribute2(ctvariants.getAttributes().getFufillmentAttribute2());
//    				attributes.setRemovalRule(ctvariants.getAttributes().getRemovalRule());
//    				attributes.setServiceType(ctvariants.getAttributes().getServiceType());
//    				attributes.setPreReqProducts(ctvariants.getAttributes().getPreReqProducts());
//    				attributes.setUpgradablePlans(ctvariants.getAttributes().getUpgradablePlans());
//    				attributes.setDowngradablePlans(ctvariants.getAttributes().getDowngradablePlans());
//    				attributes.setIsRSNCapable(ctvariants.getAttributes().getIsRSNCapable());
//    				attributes.setMarketingDescLink(ctvariants.getAttributes().getMarketingDescLink());
//    				attributes.setNumberOfChannels(ctvariants.getAttributes().getNumberOfChannels());
//    				attributes.setEnglishChannelList(ctvariants.getAttributes().getEnglishChannelList());
//    				attributes.setSpanishChannelList(ctvariants.getAttributes().getSpanishChannelList());
//    				attributes.setFeaturedChannels(ctvariants.getAttributes().getFeaturedChannels());
//    				attributes.setAddOnType(ctvariants.getAttributes().getAddOnType());
//    				attributes.setPlanSubType(ctvariants.getAttributes().getPlanSubType());
//    				attributes.setCapacityLimit(ctvariants.getAttributes().getCapacityLimit());
//    				attributes.setOpusDisclosureMessage(ctvariants.getAttributes().getOpusDisclosureMessage());
//    				attributes.setOpusDisclosureMessage(ctvariants.getAttributes().getOpusDisclosureMessage());
//    				attributes.setOpusDisclosureMessage(ctvariants.getAttributes().getOpusDisclosureMessage());

        			
        			variantList.add(variant);

    			});
    			product.setVariants(variantList);
    			product.setLeadOffer(ctproduct.isLeadOffer());
    			processedProducts.add(product);
    		}
    	});
    	return processedProducts;
    }


    
   private String getDecimalPoint(String value) {
	   log.info("redemptionLimit in CT...{}",value);
	   double valueDouble = Double.parseDouble(value);
	   NumberFormat formatter = new DecimalFormat("0.00");
	   String formmatedValue = formatter.format(valueDouble);
	   log.info("converted redemptionLimit...{}",formmatedValue);
		return formmatedValue;
	}

public List<Offers> processOffersResponse(List<CTOffer> offers, boolean uccBFlag) {

    	List<Offers> processedOffers =   new ArrayList<Offers>();           
    	offers.stream().forEach(ctoffer -> {
    		if(Objects.nonNull(ctoffer))
    		{   
    			Offers offer = new Offers();
    			offer.setCode(ctoffer.getCode());
    			offer.setId(ctoffer.getId());
    			offer.setStatus(ctoffer.getStatus());
    			offer.setName(ctoffer.getName());
    			offer.setDescription(ctoffer.getDescription());
    			offer.setStartDate(ctoffer.getStartDate());
    			offer.setEndDate(ctoffer.getEndDate());
    			
    			CouponOfferAttributes attributes = new CouponOfferAttributes();
    			attributes.setDisplayName(ctoffer.getAttributes().getDisplayName());
    			attributes.setDisplayNamesByKey(ctoffer.getAttributes().getDisplayNamesByKey());
    			attributes.setDescriptionsByKey(ctoffer.getAttributes().getDescriptionsByKey());
    			attributes.setBillingCode(ctoffer.getAttributes().getBillingCode());
    			attributes.setOfferProductType(ctoffer.getAttributes().getOfferProductType());
    			attributes.setOfferProductTypes(ctoffer.getAttributes().getOfferProductTypes());
    			attributes.setOfferProductFamily(ctoffer.getAttributes().getOfferProductFamily());
    			attributes.setOfferProductFamilies(ctoffer.getAttributes().getOfferProductFamilies());
    			
    			attributes.setOfferStatus(ctoffer.getAttributes().getOfferStatus());
    			attributes.setCpcOfferId(ctoffer.getAttributes().getCpcOfferId());
    			attributes.setPredicateRule(ctoffer.getAttributes().getPredicateRule());
    			attributes.setOfferProductSubtype(ctoffer.getAttributes().getOfferProductSubtype());
    			
    			attributes.setOfferType(ctoffer.getAttributes().getOfferType());
    			attributes.setOfferActionType(ctoffer.getAttributes().getOfferActionType());
    			attributes.setOfferActionTypes(ctoffer.getAttributes().getOfferActionTypes());
    			attributes.setFilterOffersWhenEligible(ctoffer.getAttributes().getFilterOffersWhenEligible());
    			attributes.setEnvironment(ctoffer.getAttributes().getEnvironment());
    			attributes.setOfferCategory(ctoffer.getAttributes().getOfferCategory());
    			attributes.setRank(ctoffer.getAttributes().getRank());
    			attributes.setDisplayName(ctoffer.getAttributes().getDisplayName());
    			attributes.setDisplayNamesByKey(ctoffer.getAttributes().getDisplayNamesByKey());
    			attributes.setDescriptionsByKey(ctoffer.getAttributes().getDescriptionsByKey());
    			attributes.setOpusDisclosureMessage(ctoffer.getAttributes().getOpusDisclosureMessage());
    			attributes.setMyAttDisclosureMessage(ctoffer.getAttributes().getMyAttDisclosureMessage());
    			attributes.setBillingId(ctoffer.getAttributes().getBillingId());
    			attributes.setBillingCode(ctoffer.getAttributes().getBillingCode());
    			attributes.setEligibleSOCs(ctoffer.getAttributes().getEligibleSOCs());
    			attributes.setDisqualifyingSOCs(ctoffer.getAttributes().getDisqualifyingSOCs());
    			attributes.setMigratedOffer(ctoffer.getAttributes().isMigratedOffer());
    			attributes.setProvisioningCodes(ctoffer.getAttributes().getProvisioningCodes());
				ctoffer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
					associatedProduct.getQualifyingProducts().forEach(qualifyingproduct->{
						if(null!=qualifyingproduct && CollectionUtils.isNotEmpty(qualifyingproduct.getProducts())) {
							qualifyingproduct.getProducts().forEach(product -> {
								if (null != product.getObj() && CollectionUtils.isNotEmpty(product.getObj().getVariants())) {
									product.getObj().getVariants().forEach(variant -> {
										if (null != variant.getAttributes() && null == variant.getAttributes().getRedemptionLimit()) {
											variant.getAttributes().setRedemptionLimit("");
										}else{
											variant.getAttributes().setRedemptionLimit(getDecimalPoint(variant.getAttributes().getRedemptionLimit()));
										}
										variant.getAttributes().setCampaignEligibility(satelliteServicesOffersProcessorUCCHelper.getEligibiity(variant.getAttributes().getCampaignEligibility()));

										if (null != variant.getAttributes().getCampaignEligibility()) {
											variant.getAttributes().getCampaignEligibility().forEach(campEligibility -> {
					    						if (null != campEligibility.getContentCategory() 
					    								&& (Constants.CONTENT_CATEGORY_YES).equalsIgnoreCase(campEligibility.getContentCategory())) {
					    							campEligibility.setContentCategory(Constants.CONTENT_CATEGORY_NONADULT);
					    						} else if (null != campEligibility.getContentCategory() 
					    								&& (Constants.CONTENT_CATEGORY_NO).equalsIgnoreCase(campEligibility.getContentCategory())) {
					    							campEligibility.setContentCategory(Constants.CONTENT_CATEGORY_ADULT);
					    						}
					    					});
					    				}
									});
								}
							});
						}
					});
				});
    			attributes.setAssociatedProducts(ctoffer.getAttributes().getAssociatedProducts());
    			attributes.setBenefits(ctoffer.getAttributes().getBenefits());
    			attributes.setStackableOffers(ctoffer.getAttributes().getStackableOffers());
    			attributes.setStackableOffersCategories(ctoffer.getAttributes().getStackableOffersCategories());
    			attributes.setConflictingOffers(ctoffer.getAttributes().getConflictingOffers());
    			attributes.setConflictingOffersCategories(ctoffer.getAttributes().getConflictingOffersCategories());
    			attributes.setIptvCustomerType(ctoffer.getAttributes().getIptvCustomerType());
    			attributes.setOfferPromos(ctoffer.getAttributes().getOfferPromos());
    			attributes.setRetIoApplicableProducts(ctoffer.getAttributes().getRetIoApplicableProducts());
    			attributes.setVirtualOffer(ctoffer.getAttributes().isVirtualOffer());
    			attributes.setLeadOffer(ctoffer.getAttributes().isLeadOffer());
    			attributes.setOfferPrice(ctoffer.getAttributes().getOfferPrice());
    			attributes.setContractIndicator(ctoffer.getAttributes().getContractIndicator());
    			attributes.setExternalConflictOfferIds(ctoffer.getAttributes().getExternalConflictOfferIds());
    			attributes.setExternalConflictOfferCategories(ctoffer.getAttributes().getExternalConflictOfferCategories());
    			attributes.setCompatibleProducts(ctoffer.getAttributes().getCompatibleProducts());
    			attributes.setFlashSaleOffer(ctoffer.getAttributes().isFlashSaleOffer());
    			attributes.setMobilityConflictingProducts(ctoffer.getAttributes().getMobilityConflictingProducts());
    			attributes.setResidentialConflictingProducts(ctoffer.getAttributes().getResidentialConflictingProducts());
    			attributes.setEmployeeConflictingProducts(ctoffer.getAttributes().getEmployeeConflictingProducts());
    			//attributes.setOfferIntent(ctoffer.getAttributes().getOfferIntent());
    			attributes.setOfferIntents(ctoffer.getAttributes().getOfferIntents());
    			attributes.setMigrationServiceType(ctoffer.getAttributes().getMigrationServiceType());
    			attributes.setMigrationServiceOfferType(ctoffer.getAttributes().getMigrationServiceOfferType());
    			attributes.setOfferClassificationType(ctoffer.getAttributes().getOfferClassificationType());
    			attributes.setOfferPreselectDesignation(ctoffer.getAttributes().getOfferPreselectDesignation());
    			attributes.setBenefitCodesToSuppressOffer(ctoffer.getAttributes().getBenefitCodesToSuppressOffer());
    			attributes.setPaymentType(ctoffer.getAttributes().getPaymentType());
    			attributes.setPortIn(ctoffer.getAttributes().getPortIn());
    			attributes.setFanCategories(ctoffer.getAttributes().getFanCategories());
    			attributes.setCustomerSubtypes(ctoffer.getAttributes().getCustomerSubtypes());
    			attributes.setQualifyingSku(ctoffer.getAttributes().getQualifyingSku());
    			attributes.setExcludedCustomerSubTypes(ctoffer.getAttributes().getExcludedCustomerSubTypes());
    			attributes.setEnrollmentType(ctoffer.getAttributes().getEnrollmentType());
    			attributes.setUpsellOfferATGid(ctoffer.getAttributes().getUpsellOfferATGid());
    			attributes.setDiscountRolledUp(ctoffer.getAttributes().isDiscountRolledUp());
    			attributes.setOfferTrayEnabled(ctoffer.getAttributes().isOfferTrayEnabled());
    			attributes.setVisible(ctoffer.getAttributes().getVisible());
    			attributes.setUseForPriceCalculation(ctoffer.getAttributes().getUseForPriceCalculation());
    			
    			if (uccBFlag) {
					// minimum purchase amount with 2 decimal for UCC B pid flow
    				attributes.setEligibility(getMinPurchaseAmount(ctoffer.getAttributes().getEligibility()));
				} else {
					attributes.setEligibility(ctoffer.getAttributes().getEligibility());
				}
    			
    			if(Objects.nonNull(ctoffer.getAttributes().getTermsAndConditions()) 
    					&& ctoffer.getAttributes().getTermsAndConditions().getEn() != null){
    				attributes.setTermsAndConditions(ctoffer.getAttributes().getTermsAndConditions().getEn());
    			}
    			offer.setAttributes(attributes);

    			offer.setDuplicatedCTOfferIds(ctoffer.getDuplicatedCTOfferIds());
    			offer.setAdditionals(ctoffer.getAdditionals());
    			
    			processedOffers.add(offer);
    		}
    	});
    	return processedOffers;
    }


   
   private Eligibility getMinPurchaseAmount(Eligibility eligibility) {
	   NumberFormat formatter = new DecimalFormat("0.00");
	   if (Optional.ofNullable(eligibility.getConstraints()).isPresent()) {
		   eligibility.getConstraints().forEach(constraints ->{
				double minimumPurchaseAmount = 0;
				if (constraints.getMinimumPurchaseAmount() != null) {
					minimumPurchaseAmount= constraints.getMinimumPurchaseAmount().doubleValue();
				}
				String formmatedMinimumPurchaseAmount = formatter.format(minimumPurchaseAmount);
				BigDecimal minPurchaseAmount = new BigDecimal(formmatedMinimumPurchaseAmount);
				constraints.setMinimumPurchaseAmount(minPurchaseAmount);
				log.info("MinimumPurchaseAmount set : [{}]",constraints.getMinimumPurchaseAmount());
			}); 
	   }
	return eligibility;
}

public List<Coupon> processCouponResponse(List<Coupon> coupons) {
   	
   	List<Coupon> processedCoupons =   new ArrayList<Coupon>();           
   	coupons.stream().forEach(coupon -> {
   		
    });
   	return processedCoupons;
   }
   
   public CouponResponse mergeCustomerGraphCouponResponse(CustomerCouponsResults customerCouponsResults ,CTCouponResponse cTCouponResponse,
		   boolean isMultiUse2Coupon, String couponCodeFromRequest) {
	   	   
	   CouponResponse updateCTCouponResponse = new CouponResponse();
	   List<Coupon> updateCoupons = new ArrayList<>();
	   
	   if(Objects.nonNull(customerCouponsResults) && Objects.nonNull(cTCouponResponse))
	   {
		   
		   customerCouponsResults.getCoupons().stream().forEach(customerCoupons -> {          
		       cTCouponResponse.getResults().stream().forEach(ctcoupons -> {  
		    	   Coupon coupons = new Coupon();
		           if(updateCoupons.isEmpty() && ctcoupons.getCode().equals(customerCoupons.getCouponCode()))
		           {      
		               updateCtCouponDetails(coupons,ctcoupons);
		               coupons.setCouponStatus(customerCoupons.getStatusDescription());
		               updateCoupons.add(coupons);
		           } else if(updateCoupons.isEmpty() && isMultiUse2Coupon 
		        		   && ctcoupons.getCode().equals(couponCodeFromRequest))
		           {      
		        	   updateCtCouponDetails(coupons,ctcoupons);
		               coupons.setCouponStatus(ctcoupons.getCustom().getFields().getCouponStatus());
		               updateCoupons.add(coupons);
		           }
		       });
		   });
	   } else if (isMultiUse2Coupon && Objects.nonNull(cTCouponResponse)) {
		   cTCouponResponse.getResults().stream().forEach(ctcoupons -> {  
	    	   Coupon coupons = new Coupon();
	           if(updateCoupons.isEmpty() && ctcoupons.getCode().equals(couponCodeFromRequest))
	           {      
	        	   updateCtCouponDetails(coupons,ctcoupons);
	               coupons.setCouponStatus(ctcoupons.getCustom().getFields().getCouponStatus());
	               updateCoupons.add(coupons);
	           }
	       });
	   }
	   
	   updateCTCouponResponse.setResults(updateCoupons);
	   
	   return updateCTCouponResponse;
	   	
	}



private void updateCtCouponDetails(Coupon coupons, com.dtv.dcp.epoch.model.ct.coupon.Coupon ctcoupons) {
	coupons.setCampaignCode(ctcoupons.getCustom().getFields().getCampaignCode());
    coupons.setLastModifiedBy(ctcoupons.getLastModifiedBy().getClientId());
    coupons.setCreatedBy(ctcoupons.getCreatedBy().getClientId());
    coupons.setName(ctcoupons.getName().getEn());
    coupons.setDescription(ctcoupons.getDescription().getEn());
    coupons.setId(ctcoupons.getId());
    coupons.setCode(ctcoupons.getCode());
    coupons.setStartDate(ctcoupons.getValidFrom());
    coupons.setEndDate(ctcoupons.getValidUntil());
    coupons.setCreatedAt(ctcoupons.getCreatedAt());
    coupons.setLastModifiedAt(ctcoupons.getLastModifiedAt());
    List<String> CartDiscountList =  new ArrayList<>();
    ctcoupons.getCartDiscounts().stream().forEach(cartDiscounts -> {
 	   CartDiscountList.add(cartDiscounts.getId());
    });
    coupons.setCartDiscount(CartDiscountList);
    coupons.setMaxApplications(ctcoupons.getMaxApplications());
    coupons.setMaxApplicationsPerCustomer(ctcoupons.getMaxApplicationsPerCustomer());
    coupons.setIsActive(ctcoupons.getIsActive());
}
   
   
}
