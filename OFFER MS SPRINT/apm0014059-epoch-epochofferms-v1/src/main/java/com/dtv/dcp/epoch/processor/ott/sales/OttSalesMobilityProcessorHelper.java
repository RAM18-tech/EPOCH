package com.dtv.dcp.epoch.processor.ott.sales;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.integration.dtvnaccount.DtvnAccountClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.Additonals;
import com.dtv.dcp.epoch.model.common.Alert;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.OttMobilityOffersProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;

/**
 * The Class OttSalesMobilityProcessorHelper.
 *
 * @author vc402t
 */
@Component
public class OttSalesMobilityProcessorHelper {

	/** The ott CT offers processor. */
	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;
	
	/** The ott mobility offers processor. */
	@Autowired
	OttMobilityOffersProcessor ottMobilityOffersProcessor;
	
	/** The dtvn account client. */
	@Autowired
	private DtvnAccountClient dtvnAccountClient;
	
    @Autowired
    OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;
    @Autowired
    private FeatureManagerHelper featureHelper;

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(OttSalesMobilityProcessorHelper.class);

	/**
	 * Gets the filtered offers.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param offerType the offer type
	 * @return the filtered offers
	 */
	public CTOfferResponse getFilteredOffers(OfferRequestWrapper offerRequestWrapper , String offerType) {
		boolean mobilityBaseOffers = true;
		List<Alert> alerts = new ArrayList<>();
		Additonals additionals = new Additonals();
		Alert alert = new Alert();
		List<CTOffer> offersList = null;
		CTOfferResponse offerResponse = new CTOfferResponse();
		CTOfferResponse ctOfferResponse=null;
		Boolean dtvnAccountStatus = null;
		List<String> customerSegments = null;
		
		try {
			customerSegments = offerRequestWrapper.isMobility() ? Stream.of("Mobility").collect(Collectors.toList()) :
			(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent() ? 
			offerRequestWrapper.getOfferRequest().getCustomerSegments() : Stream.of("Residential").collect(Collectors.toList()));
			
			log.info("offerRequestWrapper.getOfferRequest().getCustomerSegments() {}",customerSegments);
			
			if(	Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent() 
					&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent()
					&& offerRequestWrapper.getOfferRequest().getCustomerSegments().contains(Constants.EMPLOYEE)) {
				customerSegments = Stream.of(Constants.EMPLOYEE).collect(Collectors.toList());
			}
			
			// check if loggedInId is present: for Mobility Scenario, else it's Stand alone
			// scenario
			if (isCustomerSegmentPresent(customerSegments, Constants.MOBILITY)
				|| Optional.ofNullable(offerRequestWrapper.getDtvnAccount()).isPresent()) {
				
				// do not return offers if customer is existing DTVNow customer and not
				// subscriable
				if (Optional.ofNullable(offerRequestWrapper.getDtvnAccount()).isPresent()
						&& !(offerRequestWrapper.getOfferRequest().getOfferActionType()).contains(Constants.UPSELL)) {
					
					// if sales channel is opus
					
					if(offerRequestWrapper.getOfferRequest().getSalesChannel() != null && offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.OPUS)) {
					
					throw new ServiceException(ErrorMessages.CTLG_DTVN_EXISTING_ACCOUNT_ERROR)
							.addDetail(ErrorMessages.CTLG_DTVN_EXISTING_ACCOUNT_ERROR_DETAILS001);
					}else {
						
						// if  sales channel is not opus
						try {
							// check if customer is Subscribable then give them the base offer
							
							dtvnAccountStatus = dtvnAccountClient
									.getDtvnCustomerAccountStatus(offerRequestWrapper.getDtvnAccount());
							if (Boolean.FALSE.equals(dtvnAccountStatus)) {
								throw new ServiceException(ErrorMessages.CTLG_DTVN_EXISTING_ACCOUNT_ERROR)
										.addDetail(ErrorMessages.CTLG_DTVN_EXISTING_ACCOUNT_ERROR_DETAILS001);
							}
						} catch (ServiceException serviceEx) {
							if (Optional.ofNullable(serviceEx.getError()).isPresent()
									&& (Constants.CTLG_E500005.equalsIgnoreCase(serviceEx.getError().getErrorId())
											|| Constants.CATLG_EXT_ERROR_50001.equalsIgnoreCase(serviceEx.getError().getErrorId()))) {

								throw (new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR)
										.addDetail(ErrorMessages.CTLG_DTVN_EXISTING_ACCOUNT_ERROR_DETAILS002));
							} else {
								throw serviceEx;
							}
						}
					}
				}
				// getting qualified mobility offers
				// using the online offer logic of IDSE
				
				// if request for base
				if(offerType.equalsIgnoreCase(Constants.BASE) && !isCustomerSegmentPresent(offerRequestWrapper.getOfferRequest().getCustomerSegments(), Constants.EMPLOYEE)) {
					ctOfferResponse = ottCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper, true);
				}
				// if request for standalone 
				if(offerType.equalsIgnoreCase(Constants.STANDALONE)  && !isCustomerSegmentPresent(offerRequestWrapper.getOfferRequest().getCustomerSegments(), Constants.EMPLOYEE)) {
					 ctOfferResponse = ottCTOffersProcessor.getVideoAddonStandalonOffers(offerRequestWrapper, true);
				}
				if (ctOfferResponse != null && ctOfferResponse.getOffers() != null) {

					// Below is OPUS logic but now using the online any if there will be any gap then will enable this
					
					/** offersList = ottMobilityOfferFilterProcessor.retrieveOffersResponse(ctOfferResponse.getOffers(),
							offerRequestWrapper.getAuthAccountsWireless(), mobilityBaseOffers,
							offerRequestWrapper.getSessionId(), offerRequestWrapper);
					*/
					
					offersList=ottMobilityOffersProcessor.retrieveOffersResponse(ctOfferResponse.getOffers(), offerRequestWrapper.getAuthAccountsWireless(), mobilityBaseOffers, offerRequestWrapper.getSessionId(),offerRequestWrapper);
					/*
					 * if(offerRequestWrapper.isMobility()) {
					 * updateMobilityProductsAsIncluded(offersList); }else {
					 * removeMobilityProductsIncluded(offersList); }
					 */
					if(CollectionUtils.isNotEmpty(offersList)) {
						offerResponse.setOffers(offersList);
					}else {
					// NON mobility case for base offers so sending the non mobility offers
						offerRequestWrapper.setMobility(false);
						offerResponse = getNonMobilityOffers(offerRequestWrapper, offerType);
						if (featureHelper.isEnabled(Constants.SVC_EPOCH_MOBILITY_REWARD) && offerType.equalsIgnoreCase(Constants.BASE)
								&& offerResponse != null && CollectionUtils.isNotEmpty(offerResponse.getOffers()) && offerResponse.getOffers().get(0) != null
								&& offerResponse.getOffers().get(0).getAttributes() != null
								&& (CollectionUtils.isEmpty(offerResponse.getOffers().get(0).getAttributes().getOfferIntents())
										|| offerResponse.getOffers().get(0).getAttributes().getOfferIntents().contains("non-migration"))) {
							ottSalesOffersProcessorHelper.getMobilityRewards(offerRequestWrapper, offerResponse);
						}
					}
				} else {
					// NON mobility case for base offers so sending the non mobility offers
					offerRequestWrapper.setMobility(false);
					offerResponse = getNonMobilityOffers(offerRequestWrapper, offerType);
					if(featureHelper.isEnabled(Constants.SVC_EPOCH_MOBILITY_REWARD) && offerType.equalsIgnoreCase(Constants.BASE) && offerResponse!=null&& CollectionUtils.isNotEmpty(offerResponse.getOffers()) && offerResponse.getOffers().get(0) != null
							&& offerResponse.getOffers().get(0).getAttributes() != null
							&& (CollectionUtils.isEmpty(offerResponse.getOffers().get(0).getAttributes().getOfferIntents())
									|| offerResponse.getOffers().get(0).getAttributes().getOfferIntents().contains("non-migration"))) {
						ottSalesOffersProcessorHelper.getMobilityRewards(offerRequestWrapper, offerResponse);
					}
				}
			} else {
				// NON mobility case for base offers so sending the non mobility offers
				if(isCustomerSegmentPresent(customerSegments, Constants.EMPLOYEE) || isCustomerSegmentPresent(customerSegments,Constants.RESIDENTIAL)||
				isCustomerSegmentPresent(customerSegments,Constants.DECA) || isCustomerSegmentPresent(customerSegments,Constants.SHOWROOM) ||
				isCustomerSegmentPresent(customerSegments,Constants.COURTESY) || isCustomerSegmentPresent(customerSegments,Constants.BCOMP)||
				isCustomerSegmentPresent(customerSegments,Constants.MDUTENANT) || isCustomerSegmentPresent(customerSegments,Constants.DEMO)) {
					offerResponse = getNonMobilityOffers(offerRequestWrapper, offerType);
					//removeMobilityProductsIncluded(offerResponse.getOffers());
				}
			}

			// If There is any exception during the
		} catch (ClientException ce) {
			log.error("Sales getFilteredOffers ."+Constants.GOT_CLIENT_EXCEPTION, ce);
			if (Optional.ofNullable(ce).isPresent() && Optional.ofNullable(ce.getSource()).isPresent()
					&& ce.getSource().equalsIgnoreCase(Constants.WIRELESS_CLIENT)) {
				try {

					alert.setAlertCode(Constants.ALERT_CODE);
					alert.setAlertDescription(Constants.ALERT_DESCRIPTION);
					alerts.add(alert);
					additionals.setAlerts(alerts);
					
					
					// sending the non mobility offer with the addition waring 
					offerRequestWrapper.setMobility(false);
					offerResponse = getNonMobilityOffers(offerRequestWrapper, offerType);
					if(offerResponse!= null) {
						offerResponse.setAdditionals(additionals);
						if(featureHelper.isEnabled(Constants.SVC_EPOCH_MOBILITY_REWARD) && offerType.equalsIgnoreCase(Constants.BASE)&& CollectionUtils.isNotEmpty(offerResponse.getOffers()) && offerResponse.getOffers().get(0) != null
								&& offerResponse.getOffers().get(0).getAttributes() != null
								&& (CollectionUtils.isEmpty(offerResponse.getOffers().get(0).getAttributes().getOfferIntents())
										|| offerResponse.getOffers().get(0).getAttributes().getOfferIntents().contains("non-migration"))) {
							ottSalesOffersProcessorHelper.getMobilityRewards(offerRequestWrapper, offerResponse);
						}
					}
					
					// need to set additional to bundleOfferResponse once AID gets updated and
					// circulated for alert code.
					log.info(
							"Unable to verify Authenticated customer wireless account information to return mobility benefit DirecTVNow offers, so returns Standalone DirecTVNow offers with Alert code..");
				} catch (ServiceException e) {
					log.error(Constants.GOT_CLIENT_EXCEPTION, e);
					if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getOfferProductFamily())
							&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
						 throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
		                 .addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);					
					} else {
						 throw new ServiceException(ErrorMessages.CT_ERROR)
		                 .addDetail(ErrorMessages.CT_ERROR_DETAILS);
					}
				}
			}
			if (Optional.ofNullable(ce).isPresent() && Optional.ofNullable(ce.getMessage()).isPresent()
					&& ce.getMessage().equalsIgnoreCase(Constants.BAN_NOT_FOUND)) {
				try {
					offerRequestWrapper.setMobility(false);
					offerResponse = getNonMobilityOffers(offerRequestWrapper, offerType);
					if(featureHelper.isEnabled(Constants.SVC_EPOCH_MOBILITY_REWARD) && offerType.equalsIgnoreCase(Constants.BASE) && offerResponse!=null && CollectionUtils.isNotEmpty(offerResponse.getOffers()) && offerResponse.getOffers().get(0) != null
							&& offerResponse.getOffers().get(0).getAttributes() != null
							&& (CollectionUtils.isEmpty(offerResponse.getOffers().get(0).getAttributes().getOfferIntents())
									|| offerResponse.getOffers().get(0).getAttributes().getOfferIntents().contains("non-migration"))) {
						ottSalesOffersProcessorHelper.getMobilityRewards(offerRequestWrapper, offerResponse);
					}
					
				} catch (ServiceException e) {
					log.error("Got ClientException While retrieving  Offers...", e);
					if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getOfferProductFamily())
							&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
						 throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
		                 .addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);					
					} else {
						 throw new ServiceException(ErrorMessages.CT_ERROR)
		                 .addDetail(ErrorMessages.CT_ERROR_DETAILS);
					}
				}
			}

			if (Optional.ofNullable(ce.getSource()).isPresent() && !ce.getSource().isEmpty()
					&& ce.getSource().equalsIgnoreCase(Constants.SUSPENDED_ACCOUNT)) {
				try {
					offerRequestWrapper.setMobility(false);
					offerResponse = getNonMobilityOffers(offerRequestWrapper, offerType);
					if(featureHelper.isEnabled(Constants.SVC_EPOCH_MOBILITY_REWARD) && offerType.equalsIgnoreCase(Constants.BASE) && offerResponse!=null&& CollectionUtils.isNotEmpty(offerResponse.getOffers()) && offerResponse.getOffers().get(0) != null
							&& offerResponse.getOffers().get(0).getAttributes() != null
							&& (CollectionUtils.isEmpty(offerResponse.getOffers().get(0).getAttributes().getOfferIntents())
									|| offerResponse.getOffers().get(0).getAttributes().getOfferIntents().contains("non-migration"))) {
						ottSalesOffersProcessorHelper.getMobilityRewards(offerRequestWrapper, offerResponse);
					}
				} catch (ServiceException e) {
					log.error(Constants.GOT_CLIENT_EXCEPTION, e);
					if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getOfferProductFamily())
							&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
						 throw new ServiceException(ErrorMessages.OTT_CT_ERROR)
		                 .addDetail(ErrorMessages.OTT_CT_ERROR_DETAILS);					
					} else {
						 throw new ServiceException(ErrorMessages.CT_ERROR)
		                 .addDetail(ErrorMessages.CT_ERROR_DETAILS);
					}
				}
			}
		} catch (ServiceException se) {
			log.error("Got ServiceException.... ", se);
			throw se;
		} catch (Exception e) {
			log.error(Constants.GOT_EXCEPTION, e);
			throw new ServiceException(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR)
					.addDetail(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR_DETAILS001);
		}
		
		return offerResponse;
	}
	
	/**
	 * 
	 * @param processedCTOffers
	 */
	private void removeMobilityProductsIncluded(List<CTOffer> processedCTOffers) {
		if (Objects.nonNull(processedCTOffers)) {
			processedCTOffers.stream().filter(Objects::nonNull).forEach(offer -> {
				if (Objects.nonNull(offer.getAttributes().getAssociatedProducts()))
				offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
						.forEach(associatedProduct -> {
							if (Objects.nonNull(associatedProduct.getBundleProducts()))
							associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
									.forEach(bundleProduct -> {
										if (Objects.nonNull(bundleProduct.getProducts()))
										bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
											if (Objects.nonNull(prod.getObj().getVariants()))
											prod.getObj().getVariants().stream().filter(Objects::nonNull)
													.forEach(variants -> {
														if (Objects.nonNull(variants.getAttributes()))														
														variants.getAttributes().setIncludedMobilityProducts(null);
													});
										});
									});

						});
			});
		}		
	}

	/**
	 * getNonMobilityOffers.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 * @param offerType the offer type
	 * @return the non mobility offers
	 */
	
	private CTOfferResponse getNonMobilityOffers(OfferRequestWrapper offerRequestWrapper,String offerType ) {
		
		if(offerType.equalsIgnoreCase(Constants.BASE)) {
			excludeCustomerContextWireless(offerRequestWrapper);
			return  ottCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper, false);
		}
		if(offerType.equalsIgnoreCase(Constants.STANDALONE)) {
			return ottCTOffersProcessor.getVideoAddonStandalonOffers(offerRequestWrapper, false);
		}
		return null;
	}
	
	/**
	 * Checks if is customer segment present.
	 *
	 * @param custSegments the cust segments
	 * @param matchCustSegment the match cust segment
	 * @return true, if is customer segment presents
	 */
	private boolean isCustomerSegmentPresent(List<String> custSegments, String matchCustSegment) {
		return custSegments != null ? custSegments.stream().anyMatch(custSegment -> custSegment.equals(matchCustSegment)): false;
	}
	
	/**
	 * Exclude customer context wireless.
	 *
	 * @param offerRequestWrapper the offer request wrapper
	 */
	private void excludeCustomerContextWireless(OfferRequestWrapper offerRequestWrapper) {
		if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
			List<CTCustomerContext> customerContext = offerRequestWrapper.getCtOfferRequest().getCustomerContext();
			if (Objects.nonNull(customerContext)) {
				List<CTCustomerContext> filteredCustomerContext = customerContext.stream().filter(Objects::nonNull)
																  .filter(existProduct -> (!"wireless".equalsIgnoreCase(existProduct.getProductFamily())))
																  .collect(Collectors.toList());
				
				if(CollectionUtils.isNotEmpty(filteredCustomerContext)) {
					offerRequestWrapper.getCtOfferRequest().setCustomerContext(filteredCustomerContext);
				}else {
					offerRequestWrapper.getCtOfferRequest().setCustomerContext(null);
				}
				
			}
		}
	}
}
