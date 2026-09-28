package com.dtv.dcp.epoch.processor.satellite.services;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.CouponResponseData;
import com.dtv.dcp.epoch.model.common.EligibilityContext;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.validatecoupons.Coupon;
import com.dtv.dcp.epoch.model.ct.coupon.PurchaseDetails;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;


@Component
public class SatelliteServicesOffersProcessorUCCHelper {
	
	private static final Logger log = LoggerFactory.getLogger(SatelliteServicesOffersProcessorUCCHelper.class);

	CTOfferResponse getEmptyCTResponse() {
		CTOfferResponse offerResponse = new CTOfferResponse();
		List<Coupon> c = new ArrayList<>();
		List<CTOffer> o = new ArrayList<>();
		List<ProductObj> p = new ArrayList<>();
		offerResponse.setCoupons(c);
		offerResponse.setOffers(o);
		offerResponse.setProducts(p);
		return offerResponse;
	}

	/**
	 * @param satelliteServicesOffersProcessor TODO
	 * @param offerRequestWrapper TODO
	 * @param purchaseDetailsList
	 * @param offerResponse
	 * @return 
	 */
	void updateUCCCTOfferResponse(SatelliteServicesOffersProcessor satelliteServicesOffersProcessor, OfferRequestWrapper offerRequestWrapper, Map<String, PurchaseDetails> purchaseDetailsList, CTOfferResponse offerResponse) {
		
		getUpdatedCouponUCCResponse(satelliteServicesOffersProcessor, offerRequestWrapper, purchaseDetailsList,
				offerResponse);
		
		getUpdatedOffersUCCResponse(offerResponse);
		
		getUpdatedProductsUCCResponse(offerResponse);
	}

	private void getUpdatedCouponUCCResponse(SatelliteServicesOffersProcessor satelliteServicesOffersProcessor,
			OfferRequestWrapper offerRequestWrapper, Map<String, PurchaseDetails> purchaseDetailsList,
			CTOfferResponse offerResponse) {
		if (offerResponse != null && Optional.ofNullable(offerResponse.getCoupons()).isPresent()) {
			offerResponse.getCoupons().stream().filter(Objects::nonNull).forEach(coupon -> {
				coupon.setCouponStatus(satelliteServicesOffersProcessor.couponStatusList.get(coupon.getCode()));
				List<PurchaseDetails> purchaseDetailsResponseList = new ArrayList<>();
				if (purchaseDetailsList.size() > 0 && purchaseDetailsList.containsKey(coupon.getCode())) {
					PurchaseDetails purchaseDetails = purchaseDetailsList.get(coupon.getCode());
					purchaseDetailsUpdateForResponse(offerRequestWrapper, coupon, purchaseDetailsResponseList,
							purchaseDetails);
				}
		    });
		}
	}

	private void getUpdatedProductsUCCResponse(CTOfferResponse offerResponse) {
		if (offerResponse != null && Optional.ofNullable(offerResponse.getProducts()).isPresent() 
				&& offerResponse.getProducts().size() > 0) {
			offerResponse.getProducts().stream().forEach(ctproduct -> {
	    		if(Objects.nonNull(ctproduct))
	    		{    		
	    			ctproduct.getVariants().stream().forEach(ctvariants -> {
	    				if (null != ctvariants.getAttributes().getCampaignEligibility()) {
	    					ctvariants.getAttributes().getCampaignEligibility().forEach(campEligibility -> {
	    						if (null != campEligibility.getContentCategory() 
	    								&& (Constants.CONTENT_CATEGORY_YES).equalsIgnoreCase(campEligibility.getContentCategory())) {
	    							campEligibility.setContentCategory(Constants.CONTENT_CATEGORY_NONADULT);
	    						} else if (null != campEligibility.getContentCategory() 
	    								&& (Constants.CONTENT_CATEGORY_NO).equalsIgnoreCase(campEligibility.getContentCategory())) {
	    							campEligibility.setContentCategory(Constants.CONTENT_CATEGORY_ADULT);
	    						}
	    					});
	    				}
	    				
	    				if (null == ctvariants.getAttributes().getRedemptionLimit()) {
	    					ctvariants.getAttributes().setRedemptionLimit("");
	    				} else {
	    					ctvariants.getAttributes().setRedemptionLimit(getDecimalPoint(ctvariants.getAttributes().getRedemptionLimit()));
					}
	    			});
	    		}
	    	});
		}
	}

	private void getUpdatedOffersUCCResponse(CTOfferResponse offerResponse) {
		if (offerResponse != null && Optional.ofNullable(offerResponse.getOffers()).isPresent() 
				&& offerResponse.getOffers().size() > 0) {
			offerResponse.getOffers().forEach(offer -> {
				if (Optional.ofNullable(offer.getAttributes()).isPresent() 
						&& Optional.ofNullable(offer.getAttributes().getAssociatedProducts()).isPresent()) {
					offer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
						if (associatedProduct != null 
								&& Optional.ofNullable(associatedProduct.getQualifyingProducts()).isPresent()
								&& associatedProduct.getQualifyingProducts().size() > 0) {
							associatedProduct.getQualifyingProducts().forEach(qualifyingproduct->{
								if (null!=qualifyingproduct && CollectionUtils.isNotEmpty(qualifyingproduct.getProducts())
										&& qualifyingproduct.getProducts().size() > 0) {
									qualifyingproduct.getProducts().forEach(product -> {
										if (null != product.getObj() && CollectionUtils.isNotEmpty(product.getObj().getVariants())) {
											product.getObj().getVariants().forEach(variant -> {
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
												
												if (null != variant.getAttributes() && null == variant.getAttributes().getRedemptionLimit()) {
												  variant.getAttributes().setRedemptionLimit("");
												} else {
												  variant.getAttributes().setRedemptionLimit(getDecimalPoint(variant.getAttributes().getRedemptionLimit()));
												}
												if(null != variant.getAttributes().getCampaignEligibility())
													getEligibiity(variant.getAttributes().getCampaignEligibility());
												
											});
										}
									});
								}
							});
						}
						
					});
				}
			});
		}
	}
	public List<EligibilityContext> getEligibiity(List<EligibilityContext> campaignEligibility) {
		List<EligibilityContext> newEligib=new ArrayList<>(campaignEligibility);
		newEligib.forEach(campaignElig->{
			Object campList=null;
			if(null==campaignElig.getTitles()){
				campList=campaignElig.getTitle();
			}else{
				campList=campaignElig.getTitles();
			}
			if(null!=campList) {
				Set<String> titeleSet = new HashSet<>();
				//Boolean isEnabled =true;// featureManagerHelper.isEnabled(Constants.SVC_UCC_FS_ENABLED);
				if (campList instanceof Collection ) {
					List<String> campTitles = (List<String>) campList;
					campTitles.forEach(tit -> {
						String[] titleDetails = tit.split("\\|");
						if(titleDetails.length > 1){
						titeleSet.add(titleDetails[1].trim());
						}else{
							titeleSet.add(titleDetails[0].trim());
						}
					});
					String titleJoin = String.join(" | ", titeleSet);
					campaignElig.setTitle(titleJoin);
				} else if (campList instanceof String) {
					String[] titleDetails = campList.toString().split("\\|");
					if(titleDetails.length > 1) {
						titeleSet.add(titleDetails[1].trim());
					}else{
						titeleSet.add(titleDetails[0].trim());
					}
						String titleJoin = String.join(" | ", titeleSet);
						log.debug("titlejoin" + titleJoin);
						campaignElig.setTitle(titleJoin);
				}
			}
		});
		return newEligib;
	}


	void purchaseDetailsUpdateForResponse(OfferRequestWrapper offerRequestWrapper, Coupon coupon, List<PurchaseDetails> purchaseDetailsResponseList, PurchaseDetails purchaseDetails) {
		if (!Objects.isNull(purchaseDetails) ) {
			if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
		            .anyMatch(s -> s.equalsIgnoreCase(Constants.DTV_RIO_SERVICE))) {
				coupon.setPurchaseDetailsObj(purchaseDetails);
				
			} else {
				purchaseDetailsResponseList.add(purchaseDetails);
				coupon.setPurchaseDetails(purchaseDetailsResponseList);
			}
		}
	}

	/**
	 * gives current time in string format
	 */
	public String getCurrentTime() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		// Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_CG);
		log.debug("getCurrentTime returned.....{}",dateFormat.format(calendar.getTime()));
	    return dateFormat.format(calendar.getTime());
	}

	/**
	 * @param coupon
	 * @return PurchaseDetails
	 */
	PurchaseDetails getCouponPurchaseDetails(CustomerCoupons coupon) {
		PurchaseDetails purchaseDetails = new PurchaseDetails();
		if (coupon.getDiscountAmount() != null) {
			purchaseDetails.setDiscountAmount(coupon.getDiscountAmount());
		}
		if (coupon.getNetAmount() != null) {
			purchaseDetails.setNetAmount(coupon.getNetAmount());
		}
		if (coupon.getRetailPrice() != null) {
			purchaseDetails.setRetailPrice(coupon.getRetailPrice());
		}
		if (coupon.getTitlePurchased() != null) {
			purchaseDetails.setTitlePurchased(coupon.getTitlePurchased());
		}
		if (coupon.getTmsProgramId() != null) {
			purchaseDetails.setTmsProgramID(coupon.getTmsProgramId());
		}
		if (coupon.getUsedDate() != null 
				&& coupon.getStatusDescription().equalsIgnoreCase(Constants.USED_COUPON)) {
			purchaseDetails.setUsedDate(convertToCST(coupon.getUsedDate()));
		} else if(coupon.getRedemptionDate() != null 
				&& coupon.getStatusDescription().equalsIgnoreCase(Constants.REDEEMED_COUPON)) {
			purchaseDetails.setUsedDate(convertToCST(coupon.getRedemptionDate()));
		}
		return purchaseDetails;
	}
	
	private String convertToCST(String usedDate) {
		String cstDateStr = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_CG);
			TimeZone timeZone = TimeZone.getTimeZone("GMT");
			sdf.setTimeZone(timeZone);
			Date date1 = new Date();
			date1 = sdf.parse(usedDate); 
			String gmtDateStr = sdf.format(date1);
			timeZone = TimeZone.getTimeZone("CST");
			sdf.setTimeZone(timeZone);
			date1 = sdf.parse(gmtDateStr);
			cstDateStr = sdf.format(date1);
		} catch (ParseException e) {
			log.error("cannot parse used date",e);
		} 
		return cstDateStr;
		
	}

	String getDifferenceTime(String usedDateTime) {
		String currentTime = getCurrentTime();		
		try {
			Date date1 = new SimpleDateFormat(Constants.DATE_FORMAT_CG).parse(currentTime);
			Date date2 = new SimpleDateFormat(Constants.DATE_FORMAT_CG).parse(usedDateTime);
			log.debug("foramted date1.....{}",date1);
			log.debug("foramted date2.....{}",date2);
			long seconds = (date1.getTime()-date2.getTime())/Constants.ONE_THUSAND;
			log.debug("difference date.....{}",String.valueOf(seconds));
			return String.valueOf(seconds);
		} catch (ParseException e) {
			log.error(currentTime, e);
		}
		return null;
	}

	String getDecimalPoint(String redemptionLimit) {
		SatelliteServicesOffersProcessor.log.debug("redemptionLimit in CT...{}",redemptionLimit);
		NumberFormat formatter = new DecimalFormat("0.00");
		String formmatedredemptionLimitValue = "";
		if (redemptionLimit != null && redemptionLimit.length() > 0) {
			double redemptionLimitDouble = Double.parseDouble(redemptionLimit);
			formmatedredemptionLimitValue = formatter.format(redemptionLimitDouble);
		}
		SatelliteServicesOffersProcessor.log.debug("converted redemptionLimit...{}",formmatedredemptionLimitValue);
		return formmatedredemptionLimitValue;
	}

	public void updateTitlesResponse(CTOfferResponse offersResponse) {
		if(null!=offersResponse && CollectionUtils.isNotEmpty(offersResponse.getOffers())) {
			offersResponse.getOffers().forEach(offer -> {
				offer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
					setTitleResponse(associatedProduct);
				});
			});
		}
	}
	public void updateTitlesResponse(CouponResponseData offersResponse) {
		if(null!=offersResponse && CollectionUtils.isNotEmpty(offersResponse.getOffers())) {
			offersResponse.getOffers().forEach(offer -> {
				offer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
					setTitleResponse(associatedProduct);
				});
			});
		}
	}

	private void setTitleResponse(AssociatedProduct associatedProduct) {
		associatedProduct.getQualifyingProducts().forEach(qualifyingproduct -> {
			if(null!=qualifyingproduct && CollectionUtils.isNotEmpty(qualifyingproduct.getProducts())) {
				qualifyingproduct.getProducts().forEach(product -> {
					if (null != product.getObj() && CollectionUtils.isNotEmpty(product.getObj().getVariants())) {
						product.getObj().getVariants().forEach(variant -> {
							if (null != variant.getAttributes() && null != variant.getAttributes().getCampaignEligibility()) {
								variant.getAttributes().getCampaignEligibility().forEach(camp->{
									camp.setTitles(null);
								});
							}
						});
					}
				});
			}
		});
	}
	
}