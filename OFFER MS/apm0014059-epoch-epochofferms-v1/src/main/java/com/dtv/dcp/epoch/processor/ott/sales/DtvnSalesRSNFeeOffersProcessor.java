package com.dtv.dcp.epoch.processor.ott.sales;

import java.util.ArrayList;
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

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.rsn.BasePackage;
import com.dtv.dcp.epoch.integration.rsn.RSNClient;
import com.dtv.dcp.epoch.integration.rsn.RSNRequest;
import com.dtv.dcp.epoch.integration.rsn.RSNResponse;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.RSNFeeDetails;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.services.DtvnServicesRSNFeeOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RxJavaHelper;

/**
 * The Class DtvnServicesRSNFeeOffersProcessor.
 */
@Component
public class DtvnSalesRSNFeeOffersProcessor {
	
	@Autowired
	DtvnServicesRSNFeeOffersProcessor dtvnServicesRSNFeeOffersProcessor;

	private static final String CONTRACT = "contract";

	/** The Constant RSN_FEE. */
	private static final String RSN_FEE = "RSNFee";
	
	/** The rsn client. */
	@Autowired
	RSNClient rsnClient;
	
	@Autowired
	OffersUtils offersUtils;
	
    @Autowired
    OttCTOffersProcessor ottCTOffersProcessor;


    /** The Constant log. */
    private static final Logger log = LoggerFactory.getLogger(DtvnSalesRSNFeeOffersProcessor.class);
	
	/**
	 * Filter RSN fee offers.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param feeOffers the fee offers
	 */
	public void filterRSNFeeOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> feeOffers) {
		RSNRequest rsnRequest = new RSNRequest();
		log.info("DtvnSalesRSNFeeOffersProcessor.filterRSNFeeOffers started" );
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest()) && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCartContext())) {
			RSNResponse rsnResponse = null;
			String billingProductCode = null;
			try {
				billingProductCode = getBillingProductCode(offerRequestWrapper);
				buildRSNRequest(offerRequestWrapper, rsnRequest, billingProductCode);
				if(!StringUtils.isEmpty(rsnRequest.getBasePackage().getSku()) && !StringUtils.isEmpty(rsnRequest.getZipCode())) {				
					rsnResponse = rsnClient.getRSNInfo(rsnRequest);
				}
				populateRSNFee(feeOffers,rsnResponse);
				
			} catch (Exception e) {
				log.error("Error in method filterRSNFeeOffers() method ", e);
				//throw new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, e);
			}
			
		}
	}
	
	 public void populateRSNFeeDetails(OfferRequestWrapper offerRequestWrapper, List<CTOffer> videoPlanOffers, List<CTOffer> feeOffers) {
		 List<CTOffer> videoPlanCTOffers = videoPlanOffers.stream().filter(Objects::nonNull)
				 .filter(offer -> Constants.VIDEO_PLAN.equalsIgnoreCase(offer.getAttributes().getOfferProductType())
						 && (Constants.TAZ_STRING.equalsIgnoreCase(offer.getAttributes().getContractIndicator())
						 || Constants.TAZCONTRACT_STRING.equalsIgnoreCase(offer.getAttributes().getContractIndicator())
						 || Constants.ROAD_RUNNER.equalsIgnoreCase(offer.getAttributes().getContractIndicator())))
				 .collect(Collectors.toList());
		 if (CollectionUtils.isNotEmpty(videoPlanCTOffers)) {
			 getRSNFeeDetailsAsynchronously(offerRequestWrapper, videoPlanCTOffers, feeOffers);
		 }
	 }
	
	 public void getRSNFeeDetailsAsynchronously(OfferRequestWrapper offerRequestWrapper, List<CTOffer> videoPlanOffers, List<CTOffer> feeOffers) {
	    	try {
				List<Callable<?>> callableObjList = new ArrayList<>();
				 // Capture MDC context
		        Map<String, String> capturedMdcContext = MDC.getCopyOfContextMap();
				videoPlanOffers.stream().filter(Objects::nonNull)
										.forEach(videoPlanOffer -> {
								    		if(Optional.ofNullable(videoPlanOffer.getAttributes()).isPresent() && Constants.VIDEO_PLAN.equalsIgnoreCase(videoPlanOffer.getAttributes().getOfferProductType()) ) {
								    			 callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
								    	                    () -> getRSNFeeDetails(offerRequestWrapper, videoPlanOffer)));
								    			//callableObjList.add(() ->  getRSNFeeDetails(offerRequestWrapper, videoPlanOffer));
								    		}
										});
				Object[] responses = null;
				long startTimeInMillis = System.currentTimeMillis();
				responses = RxJavaHelper.callConcurrentlyGetResult(callableObjList.toArray(new Callable[callableObjList.size()]));
				long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
				log.info("EPOCH_OTT_SALES_RSN_FEE_ASYNC-[{}]", (endTimeMillis));
				log.info("Getting RSN Fee Async response: {}", responses);
				handleAsyncResponsesSales(responses, videoPlanOffers, feeOffers);
			} catch (Exception e) {
				log.error("Exception getRSNFeeDetailsAsynchronously...{}", e);
			}
	    }

	 public void handleAsyncResponsesSales(Object[] responses,List<CTOffer> videoPlanOffers, List<CTOffer> feeOffers) {
	    	int[] index = {0};
			videoPlanOffers.stream().filter(Objects::nonNull)
									.forEach(videoPlanOffer -> {
								    	if(responses[index[0]] instanceof RSNResponse) {
								    		// Populating RSN Fee Info for Video-plans
								    		populateRSNFeeInfo(videoPlanOffer, (RSNResponse)responses[index[0]], feeOffers);
								    	}
								    	index[0]++;
									});
	    }   
	private void populateRSNFeeInfo(CTOffer videoPlanOffer, RSNResponse rsnResponse, List<CTOffer> feeOffers) {

		CTOffer filteredRSNFeeOffer = null;
		try {
			
			RSNFeeDetails rsnFeeDetails = new RSNFeeDetails();
			
			if(Objects.nonNull(rsnResponse) && Objects.nonNull(rsnResponse.getFee()) && Objects.nonNull(rsnResponse.getFee().getSku())) {
				
				filteredRSNFeeOffer = dtvnServicesRSNFeeOffersProcessor.filterRSNFeeOfferByFeeTypeSku(feeOffers, RSN_FEE,rsnResponse.getFee().getSku());
				rsnFeeDetails.setSku(rsnResponse.getFee().getSku());
				rsnFeeDetails.setAmount(rsnResponse.getFee().getAmount());
				rsnFeeDetails.setVisible(rsnResponse.getFee().getVisible());
				
				if(Objects.nonNull(filteredRSNFeeOffer)) {
					Attributes attributes = filteredRSNFeeOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes();
					if (Objects.nonNull(videoPlanOffer.getAttributes().getContractIndicator())
							&&(!Constants.TAZ_STRING.equalsIgnoreCase(videoPlanOffer.getAttributes().getContractIndicator())
									&& !Constants.TAZCONTRACT_STRING.equalsIgnoreCase(videoPlanOffer.getAttributes().getContractIndicator()))) {
						rsnFeeDetails.setFeeType(attributes.getFeeType());
					}
					rsnFeeDetails.setOfferId(filteredRSNFeeOffer.getCode());
					if(Objects.nonNull(attributes.getDisplayName())) {
						rsnFeeDetails.setDisplayName(attributes.getDisplayName().getEn());
					}
				}
				videoPlanOffer.getAttributes().setRsnFeeDetails(rsnFeeDetails);
			}
		} catch (Exception e) {
			log.error("Exception populateRSNFeeInfo...{}", e);
		}
	}

	private RSNResponse getRSNFeeDetails(OfferRequestWrapper offerRequestWrapper, CTOffer videoPlanOffer) {
		RSNResponse rsnResponse = null;
		RSNRequest rsnRequest = new RSNRequest();
		String billingProductCode = getBillingProductCodeFromOffer(videoPlanOffer);

		try {
			buildRSNRequest(offerRequestWrapper, rsnRequest, billingProductCode);
			if(!StringUtils.isEmpty(rsnRequest.getBasePackage().getSku()) && !StringUtils.isEmpty(rsnRequest.getZipCode())) {
				rsnResponse = rsnClient.getRSNInfo(rsnRequest);
			}
		} catch (Exception e) {
			log.error("Exception getRSNFeeDetails...{}", e);
		}
		return rsnResponse;
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
		if (Optional.ofNullable(associatedProduct.getBundleProducts()).isPresent()
				&& !associatedProduct.getBundleProducts().isEmpty()) {
			for (ProductWrapper product : associatedProduct.getBundleProducts()) {
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

	private String getBillingProductCode(OfferRequestWrapper offerRequestWrapper) {
		String billingProductCode = null;
		CTOfferResponse ctOfferResponse = null;
		OfferRequestWrapper offerReqWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		offerReqWrapper.setFlow(offerRequestWrapper.getFlow());
		if( Objects.nonNull(offerRequestWrapper.getOfferRequest()) && 
			Objects.nonNull(offerRequestWrapper.getCtOfferRequest()) ) {
			offerRequest.setOfferProductFamily(offerRequestWrapper.getOfferRequest().getOfferProductFamily());
			offerRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
			offerRequest.setOfferIds(offerRequestWrapper.getCtOfferRequest().getCartContext().getOfferIds());
			offerRequest.setOfferCodes(offerRequestWrapper.getCtOfferRequest().getCartContext().getOfferCodes());
			offerRequest.setCustomerSegments(offerRequestWrapper.getCtOfferRequest().getCustomerSegments());
			offerRequest.setBusinessSegments(offerRequestWrapper.getCtOfferRequest().getBusinessSegments());
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
	 * Builds the RSN request.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param rsnRequest the rsn request
	 */
    public void buildRSNRequest(OfferRequestWrapper offerRequestWrapper, RSNRequest rsnRequest, String billingProductCode) {
		BasePackage basePackage = new BasePackage();
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerEligibility())
				&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerEligibility().getZipCode())
				&& !offerRequestWrapper.getOfferRequest().getCustomerEligibility().getZipCode().isEmpty()) {
			rsnRequest.setZipCode(offerRequestWrapper.getOfferRequest().getCustomerEligibility().getZipCode().get(0));
		}
		
		rsnRequest.setAccountType(offerRequestWrapper.isMobility() ? Constants.MOBILITY: Constants.RESIDENTIAL);
        if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerSegments()) 
        		&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE))
        {
        	rsnRequest.setAccountType(Constants.EMPLOYEE);
        }
		
		if(Objects.nonNull(offerRequestWrapper.getOfferRequest()) && 
		   Objects.nonNull(offerRequestWrapper.getOfferRequest().getContractIndicator()) ) {
			basePackage.setContracted(OffersUtils.isMatchFound(offerRequestWrapper.getOfferRequest().getContractIndicator(),CONTRACT));
		}
		
		if (Objects.nonNull(offerRequestWrapper.getOfferRequest())
				&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getContractIndicator())
				&& OffersUtils.isMatchFound(offerRequestWrapper.getOfferRequest().getContractIndicator(), Constants.TAZ_STRING)) {
			rsnRequest.setSubscriberType(Constants.TAZ_STRING);
		} else if (Objects.nonNull(offerRequestWrapper.getOfferRequest())
				&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getContractIndicator())
				&& OffersUtils.isMatchFound(offerRequestWrapper.getOfferRequest().getContractIndicator(), Constants.TAZCONTRACT_STRING)) {
			rsnRequest.setSubscriberType(Constants.TAZCONTRACT_STRING);
			basePackage.setContracted(true);
		} else if (Objects.nonNull(offerRequestWrapper.getOfferRequest())
				&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getContractIndicator())
				&& OffersUtils.isMatchFound(offerRequestWrapper.getOfferRequest().getContractIndicator(), Constants.ROAD_RUNNER)) {
			rsnRequest.setSubscriberType(Constants.ROAD_RUNNER);
			basePackage.setContracted(true);
		}
		basePackage.setSku(billingProductCode);
		ProductInfo product = getVideoPlanProdcut(offerRequestWrapper);
		if (Objects.nonNull(product)) {
			basePackage.setSku(product.getProductCode());
		}
		rsnRequest.setBasePackage(basePackage);
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
			log.error("populateRSNFee: " + e.getMessage());
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
}