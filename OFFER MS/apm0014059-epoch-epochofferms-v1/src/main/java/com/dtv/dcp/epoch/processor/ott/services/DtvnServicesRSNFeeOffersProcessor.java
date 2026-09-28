package com.dtv.dcp.epoch.processor.ott.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.rsn.BasePackage;
import com.dtv.dcp.epoch.integration.rsn.CustomerProfile;
import com.dtv.dcp.epoch.integration.rsn.RSNClient;
import com.dtv.dcp.epoch.integration.rsn.RSNRequest;
import com.dtv.dcp.epoch.integration.rsn.RSNResponse;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.RSNFeeDetails;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RxJavaHelper;

/**
 * The Class DtvnServicesRSNFeeOffersProcessor.
 */
@Component
public class DtvnServicesRSNFeeOffersProcessor {

	/** The Constant RSN_FEE. */
	private static final String RSN_FEE = "RSNFee";
	
	/** The Constant FREE_PROMO. */
	private static final String FREE_PROMO = "free-promo";
	
	/** The rsn client. */
	@Autowired
	RSNClient rsnClient;
	
	/** The offers utils. */
	@Autowired
	OffersUtils offersUtils;

    /** The Constant log. */
    private static final Logger log = LoggerFactory.getLogger(DtvnServicesRSNFeeOffersProcessor.class);
	
	/**
	 * Filter RSN fee offers.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param customerSubscriptionDetail the customer subscription detail
	 * @param feeOffers the fee offers
	 */
	public void filterRSNFeeOffers(OfferRequestWrapper offerRequestWrapper,CustomerSubscriptionDetail  customerSubscriptionDetail, List<CTOffer> feeOffers) {
		RSNRequest rsnRequest = new RSNRequest();
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest()) && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())) {
			RSNResponse rsnResponse =  null;
			try {
				buildRSNRequest(offerRequestWrapper, customerSubscriptionDetail, rsnRequest);
				if(!StringUtils.isEmpty(rsnRequest.getBasePackage().getSku()) && !StringUtils.isEmpty(rsnRequest.getZipCode())) {				
					rsnResponse = rsnClient.getRSNInfo(rsnRequest);
				}
				populateRSNFee(feeOffers,rsnResponse);
				
			} catch (Exception e) {
				log.error("Error in method filterRSNFeeOffers() method ", e);
			}
		}
	}
	
	/**
	 * Builds the RSN request.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param customerSubscriptionDetail the customer subscription detail
	 * @param rsnRequest the rsn request
	 */
	public void buildRSNRequest(OfferRequestWrapper offerRequestWrapper,CustomerSubscriptionDetail  customerSubscriptionDetail, RSNRequest rsnRequest) {
		BasePackage basePackage = new BasePackage();
		populateRequest(offerRequestWrapper, customerSubscriptionDetail, rsnRequest, basePackage);
		ProductInfo product = getVideoPlanProdcut(offerRequestWrapper);
		if (Objects.nonNull(product)) {
			basePackage.setSku(product.getProductCode());
		}
		rsnRequest.setBasePackage(basePackage);
	}
	
	/**
	 * Builds the RSN request.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param customerSubscriptionDetail the customer subscription detail
	 * @param rsnRequest the rsn request
	 * @param billingProductCode the billing product code
	 */
	private void buildRSNRequest(OfferRequestWrapper offerRequestWrapper,CustomerSubscriptionDetail  customerSubscriptionDetail, RSNRequest rsnRequest,String billingProductCode) {
		BasePackage basePackage = new BasePackage();
		populateRequest(offerRequestWrapper, customerSubscriptionDetail, rsnRequest, basePackage);
		if (Objects.nonNull(billingProductCode)) {
			basePackage.setSku(billingProductCode);
		}
		rsnRequest.setBasePackage(basePackage);
	}
	
	/**
	 * Gets the billing product code from offer.
	 *
	 * @param offer the offer
	 * @return the billing product code from offer
	 */
	private String getBillingProductCodeFromOffer(CTOffer offer) {
		String billingProductCode = "";
		AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
		List<ProductWrapper> cpopProducts = null;
		if(FREE_PROMO.equalsIgnoreCase(offer.getAttributes().getOfferType())) {
			cpopProducts = associatedProduct.getBundleProducts();
		}else {
			cpopProducts = associatedProduct.getQualifyingProducts();
		}
		if (Optional.ofNullable(cpopProducts).isPresent() && !cpopProducts.isEmpty()) {
			for (ProductWrapper product : cpopProducts) {
				List<Product> products = product.getProducts();
				if (Optional.ofNullable(products).isPresent() && !products.isEmpty()) {
					for (Product prod : products) {
						billingProductCode = prod.getObj().getVariants().get(0).getAttributes().getBillingProductCode();
					}
				}
			}
		}
      return billingProductCode;
	}

	/**
	 * Populate request.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param customerSubscriptionDetail the customer subscription detail
	 * @param rsnRequest the rsn request
	 * @param basePackage the base package
	 */
	private void populateRequest(OfferRequestWrapper offerRequestWrapper,
			CustomerSubscriptionDetail customerSubscriptionDetail, RSNRequest rsnRequest, BasePackage basePackage) {
		CustomerProfile customerProfile = new CustomerProfile();
		PriceProtection priceProtection = new PriceProtection();
		BasePackage custBasePackage = new BasePackage();
		
		CustomerEligibility customerEligibility = offerRequestWrapper.getOfferRequest().getCustomerEligibility();
		
		if (Objects.nonNull(customerEligibility) && CollectionUtils.isNotEmpty(customerEligibility.getZipCode())) {
			rsnRequest.setZipCode(customerEligibility.getZipCode().get(0));
			customerProfile.setZipCode(customerEligibility.getZipCode().get(0));
		}
		if(Objects.nonNull(customerSubscriptionDetail) && !StringUtils.isEmpty(customerSubscriptionDetail.getAccountType()) ) {
			rsnRequest.setAccountType(customerSubscriptionDetail.getAccountType());
		}
		if(Objects.nonNull(customerSubscriptionDetail) && !StringUtils.isEmpty(customerSubscriptionDetail.getContractIndicator()) ) {
			if(Constants.CONTRACT.equalsIgnoreCase(customerSubscriptionDetail.getContractIndicator())) {
				rsnRequest.setSubscriberType(Constants.BBTV_STRING);
				basePackage.setContracted(true);
				custBasePackage.setContracted(true);
			}else if(Constants.NONCONTRACT.equalsIgnoreCase(customerSubscriptionDetail.getContractIndicator())){
				basePackage.setContracted(false);
				custBasePackage.setContracted(false);
			}else if(Constants.TAZ_STRING.equalsIgnoreCase(customerSubscriptionDetail.getContractIndicator())) {
				rsnRequest.setSubscriberType(Constants.TAZ_STRING);
			}else if(Constants.TAZCONTRACT_STRING.equalsIgnoreCase(customerSubscriptionDetail.getContractIndicator())) {
				rsnRequest.setSubscriberType(Constants.TAZCONTRACT_STRING);
				basePackage.setContracted(true);
				custBasePackage.setContracted(true);
			}else if(Constants.ROAD_RUNNER.equalsIgnoreCase(customerSubscriptionDetail.getContractIndicator())) {
				rsnRequest.setSubscriberType(Constants.ROAD_RUNNER);
				basePackage.setContracted(true);
				custBasePackage.setContracted(true);
			}
		}
        
        if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
        		Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
        		Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection()) &&
        		Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection().getStartDate()) &&
        		Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection().getEndDate())) {
        	priceProtection.setStartDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection().getStartDate());
        	priceProtection.setEndDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getPriceProtection().getEndDate());
        	customerProfile.setPriceProtection(priceProtection);
        }

        if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
        		Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()) &&
        		Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate())) {
        	customerProfile.setNextBillCycleDate(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getNextBillingDate());
        }
		
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt())) {
			List<ProductInfo> customerProducts = offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts();
			String basepackageSku = getProductCodeByProductType(customerProducts,Constants.VIDEO_PLAN);
			String rsnSku = getProductCodeByProductType(customerProducts,Constants.FEE);
         	if (org.apache.commons.lang3.StringUtils.isNotBlank(rsnSku) && !rsnSku.contains(Constants.RSN_CAPS)) {
				rsnSku = Optional.ofNullable(customerProducts).orElseGet(Collections::emptyList).stream()
						.filter(Objects::nonNull).filter(cp -> cp.getProductCode().contains(Constants.RSN_CAPS))
						.map(ProductInfo::getProductCode).findFirst().orElseGet(null);
			}
			if(!StringUtils.isEmpty(basepackageSku) && !StringUtils.isEmpty(rsnSku)) {
				customerProfile.setRsnSku(rsnSku);
				custBasePackage.setSku(basepackageSku);
				customerProfile.setBasePackage(custBasePackage);
				rsnRequest.setCustomerProfile(customerProfile);				
			}
		}
	}

	private String getProductCodeByProductType(List<ProductInfo> customerProducts, String productType) {
		String productCode = null;
		if (CollectionUtils.isNotEmpty(customerProducts)) {
			Optional<ProductInfo> filteredPoduct = customerProducts.stream()
					.filter(product -> productType.equalsIgnoreCase(product.getProductType())).findFirst();
			if (filteredPoduct.isPresent()) {
				productCode = filteredPoduct.get().getProductCode();
			}
		}
		return productCode;
	}
	/**
	 * Gets the video plan prodcut.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @return the video plan prodcut
	 */
	private ProductInfo getVideoPlanProdcut(OfferRequestWrapper offerRequestWrapper) {
		ProductInfo prodct = null;
		CustomerContext customerContext = offerRequestWrapper.getOfferRequest().getCustomerContext();
		if (Objects.nonNull(customerContext)) {
			CartProduct cartProduct = customerContext.getOtt();
			if (Objects.nonNull(cartProduct) && Objects.nonNull(cartProduct.getProducts())) {
				Optional<ProductInfo> product = cartProduct.getProducts().stream().filter(productInfo -> productInfo.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)).findFirst();
				if (product.isPresent()) {
					prodct = product.get();
				}
			}
		}
		return prodct;
	}
	
	/**
	 * Populate RSN fee.
	 *
	 * @param offers the offers
	 * @param rsnResponse the rsn response
	 */
	public void populateRSNFee(List<CTOffer> offers,RSNResponse rsnResponse) {
		List<CTOffer> filteredOtherFeeOffers = null;
		List<CTOffer> filteredRSNFeeOffers = null;
		List<CTOffer> rsnCTOffers = new ArrayList<>();
		
		filteredOtherFeeOffers = offersUtils.filterOtherFeeOffersByFeeType(offers, RSN_FEE);
		filteredRSNFeeOffers = offersUtils.filterFeeOffersByFeeType(offers, RSN_FEE);
		try {
			if (Optional.ofNullable(filteredRSNFeeOffers).isPresent()) {
				
				filteredRSNFeeOffers.stream().filter(Objects::nonNull).forEach(rsnOffer -> {

					if (Objects.nonNull(rsnOffer.getAttributes().getAssociatedProducts())
							&& Objects.nonNull(rsnOffer.getAttributes().getAssociatedProducts().get(0))
							&& Objects.nonNull(rsnOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
							&& Objects.nonNull(rsnOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))
							) {
							List<Product> products = rsnOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts();
	
							if(Objects.nonNull(products)) {
								products.stream().filter(Objects::nonNull).forEach(product -> {
									if(Objects.nonNull(product.getObj()) && 
									   Objects.nonNull(product.getObj().getCode()) && 
									   Objects.nonNull(rsnResponse) && 
									   Objects.nonNull(rsnResponse.getFee()) && 
									   Objects.nonNull(rsnResponse.getFee().getSku()) &&
									   product.getObj().getCode().equalsIgnoreCase(rsnResponse.getFee().getSku()) ) {
										   populateRSNFee(product,rsnResponse.getFee().getAmount());
										   rsnOffer.getAttributes().setVisible(rsnResponse.getFee().getVisible());
										   rsnCTOffers.add(rsnOffer);	
									}
								});
							}
												
					}
				});
				
				if(Objects.nonNull(offers)){
					offers.clear();
					offers.addAll(filteredOtherFeeOffers);
					offers.addAll(rsnCTOffers);
				}
			}
		} catch (Exception e) {
			log.error("Exception populateRSNFee...{}", e);
		}
	}
	
	/**
	 * Populate RSN fee.
	 *
	 * @param product the product
	 * @param amount the amount
	 */
	public void populateRSNFee(Product product, Double amount) {

		product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
			if (Objects.nonNull(variant) && Objects.nonNull(variant.getPrices())) {
				variant.getPrices().stream().filter(Objects::nonNull).forEach(price -> {
					if (price != null && price.getEndDate() != null
							&& OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()),
									OffersUtils.getFormattedDate(price.getEndDate()))
							&& Optional.ofNullable(price.getValue()).isPresent()
							&& Optional.ofNullable(price.getValue().getDollarAmount()).isPresent()
							&& Optional.ofNullable(amount).isPresent()) {
						price.getValue().setDollarAmount(amount);
					}
				});
			}
		});
	}
	
    /**
     * Filter active offers.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param customerSubscriptionDetail the customer subscription detail
     * @param videoPlanOffers the video plan offers
     * @param feeOffers the fee offers
     */
    public void populateRSNFeeDetails(OfferRequestWrapper offerRequestWrapper,CustomerSubscriptionDetail  customerSubscriptionDetail, List<CTOffer> videoPlanOffers, List<CTOffer> feeOffers) {
		List<CTOffer> videoPlanCTOffers = videoPlanOffers.stream().filter(Objects::nonNull)
				.filter(offer -> Constants.VIDEO_PLAN.equalsIgnoreCase(offer.getAttributes().getOfferProductType())
						&& !"EDSP".equalsIgnoreCase(offer.getAttributes().getContractIndicator()))
				.collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(videoPlanCTOffers)) {
	    	getRSNFeeDetailsAsynchronously(offerRequestWrapper, customerSubscriptionDetail, videoPlanCTOffers, feeOffers);
	    }
    }
	
    public void getRSNFeeDetailsAsynchronously(OfferRequestWrapper offerRequestWrapper,CustomerSubscriptionDetail  customerSubscriptionDetail, List<CTOffer> videoPlanOffers, List<CTOffer> feeOffers) {
    	try {
			List<Callable<?>> callableObjList = new ArrayList<>();
			 // Capture MDC context
	        Map<String, String> capturedMdcContext = MDC.getCopyOfContextMap();
			videoPlanOffers.stream().filter(Objects::nonNull)
									.forEach(videoPlanOffer -> {
							    		if(Optional.ofNullable(videoPlanOffer.getAttributes()).isPresent() && Constants.VIDEO_PLAN.equalsIgnoreCase(videoPlanOffer.getAttributes().getOfferProductType()) ) {
							    			callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
							                        () -> getRSNFeeDetails(offerRequestWrapper,customerSubscriptionDetail, videoPlanOffer)));	
										//callableObjList.add(() ->  getRSNFeeDetails(offerRequestWrapper,customerSubscriptionDetail, videoPlanOffer));
							    		}
									});
			Object[] responses = null;
			long startTimeInMillis = System.currentTimeMillis();
			responses = RxJavaHelper.callConcurrentlyGetResult(callableObjList.toArray(new Callable[callableObjList.size()]));
			long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
			log.info("EPOCH_OTT_SERVICES_RSN_FEE_ASYNC-[{}]", (endTimeMillis));
			log.info("Getting RSN Fee Async response: {}", responses);
			handleAsyncResponsesServices(responses,videoPlanOffers, feeOffers);
		} catch (Exception e) {
			log.error("Exception getRSNFeeDetailsAsynchronously...{}", e);
		}
    }
    
    public void handleAsyncResponsesServices(Object[] responses,List<CTOffer> videoPlanOffers,List<CTOffer> feeOffers) {
    	int[] index = {0};
		videoPlanOffers.stream().filter(Objects::nonNull)
								.forEach(videoPlanOffer -> {
							    	if(responses[index[0]] instanceof RSNResponse) {
							    		// Populating RSN Fee Info for Video-plans
							    		populateRSNFeeInfo(videoPlanOffer, feeOffers, (RSNResponse)responses[index[0]]);
							    	}
							    	index[0]++;
								});
    }    
	public RSNResponse getRSNFeeDetails(OfferRequestWrapper offerRequestWrapper,CustomerSubscriptionDetail customerSubscriptionDetail, CTOffer ctOffer) {
		RSNResponse rsnResponse = null;
		RSNRequest rsnRequest = new RSNRequest();
		String billingProductCode = getBillingProductCodeFromOffer(ctOffer);

		try {
			buildRSNRequest(offerRequestWrapper, customerSubscriptionDetail, rsnRequest, billingProductCode);
			if(!StringUtils.isEmpty(rsnRequest.getBasePackage().getSku()) && !StringUtils.isEmpty(rsnRequest.getZipCode())) {
				rsnResponse = rsnClient.getRSNInfo(rsnRequest);
			}
		} catch (Exception e) {
			log.error("Exception getRSNFeeDetails...{}", e);
		}
		return rsnResponse;
	}

	/**
	 * Populate RSN fee info.
	 *
	 * @param rsnResponse the offer request wrapper
	 * @param ctOffer the ct offer
	 * @param feeOffers the fee offers
	 */
	private void populateRSNFeeInfo(CTOffer ctOffer, List<CTOffer> feeOffers, RSNResponse rsnResponse) {

		RSNRequest rsnRequest = new RSNRequest();
		CTOffer filteredRSNFeeOffer = null;
		try {
			
			RSNFeeDetails rsnFeeDetails = new RSNFeeDetails();
			
			if(Objects.nonNull(rsnResponse) && Objects.nonNull(rsnResponse.getFee()) && Objects.nonNull(rsnResponse.getFee().getSku())) {
				
				filteredRSNFeeOffer = filterRSNFeeOfferByFeeTypeSku(feeOffers, RSN_FEE,rsnResponse.getFee().getSku());
				rsnFeeDetails.setSku(rsnResponse.getFee().getSku());
				rsnFeeDetails.setAmount(rsnResponse.getFee().getAmount());
				rsnFeeDetails.setVisible(rsnResponse.getFee().getVisible());
				
				if(Objects.nonNull(filteredRSNFeeOffer)) {
					Attributes attributes = filteredRSNFeeOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes();
					if (Objects.nonNull(ctOffer.getAttributes().getContractIndicator())
							&& (!Constants.TAZ_STRING.equalsIgnoreCase(ctOffer.getAttributes().getContractIndicator())
									&& !Constants.TAZCONTRACT_STRING.equalsIgnoreCase(ctOffer.getAttributes().getContractIndicator()))) {
						rsnFeeDetails.setFeeType(attributes.getFeeType());
					}
					rsnFeeDetails.setOfferId(filteredRSNFeeOffer.getCode());
					if(Objects.nonNull(attributes.getDisplayName())) {
						rsnFeeDetails.setDisplayName(attributes.getDisplayName().getEn());
					}
				}
				ctOffer.getAttributes().setRsnFeeDetails(rsnFeeDetails);
			}
		} catch (Exception e) {
			log.error("Exception populateRSNFeeInfo...{}", e);
		}
	}

	/**
	 * Filter RSN fee offer by fee type sku.
	 *
	 * @param ctOffers the ct offers
	 * @param feeType the fee type
	 * @param sku the sku
	 * @return the CT offer
	 */
	public CTOffer filterRSNFeeOfferByFeeTypeSku(List<CTOffer> ctOffers, String feeType, String sku) {
		Optional<CTOffer> rsnFeeOffer = null;
        if (Optional.ofNullable(ctOffers).isPresent() ) {
        	rsnFeeOffer = ctOffers.stream().filter(Objects::nonNull)
				.filter(offer -> 
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null &&
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0) != null && 
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts() != null &&
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0) != null &&
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj() != null &&
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants() != null &&
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0) != null && 
						offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes() != null && 
						feeType.equalsIgnoreCase(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes().getFeeType()) && 
						sku.equalsIgnoreCase(offer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getCode())) 
						.findFirst();
        }
        return (Objects.nonNull(rsnFeeOffer) &&  rsnFeeOffer.isPresent()) ? rsnFeeOffer.get() : null;
    }

	/**
	 * Filter RSN fee offer based on RSN fee details.
	 *
	 * @param videoPlanOffers the video plan offers
	 * @param feeOffers the fee offers
	 */
	public  void filterRsnFeeOfferBasedOnRsnFeeDetails(List<CTOffer> videoPlanOffers, List<CTOffer> feeOffers){
		List<CTOffer> filteredOtherFeeOffers = null;

		filteredOtherFeeOffers = offersUtils.filterOtherFeeOffersByFeeType(feeOffers, RSN_FEE);

		List<String> feeOfferId = videoPlanOffers.stream()
				.filter(Objects::nonNull)
				.filter(offer -> Objects.nonNull(offer.getAttributes()) && Constants.VIDEO_PLAN.equalsIgnoreCase(offer.getAttributes().getOfferProductType()))
				.filter(offer -> Objects.nonNull(offer.getAttributes().getRsnFeeDetails()))
				.map(offer -> offer.getAttributes().getRsnFeeDetails().getOfferId())
				.distinct()
				.collect(Collectors.toList());

		List<CTOffer> filteredFeeOffers = feeOffers.stream().filter(Objects::nonNull)
				.filter(f -> feeOfferId.contains(f.getCode())).collect(Collectors.toList());

		if (Optional.ofNullable(filteredFeeOffers).isPresent() && Optional.ofNullable(filteredOtherFeeOffers).isPresent()) {
			feeOffers.clear();
			feeOffers.addAll(filteredOtherFeeOffers);
			feeOffers.addAll(filteredFeeOffers);
		}

	}

}
