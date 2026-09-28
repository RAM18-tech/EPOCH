package com.dtv.dcp.epoch.resource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.AccountContext;
import com.dtv.dcp.epoch.model.common.CouponResponseData;
import com.dtv.dcp.epoch.model.common.CustomCouponBase;
import com.dtv.dcp.epoch.model.common.CustomCouponFields;
import com.dtv.dcp.epoch.model.common.VaildateCouponBase;
import com.dtv.dcp.epoch.model.common.request.CouponValidationRequest;
import com.dtv.dcp.epoch.model.common.request.CouponsRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.response.CouponValidationResponse;
import com.dtv.dcp.epoch.model.common.validatecoupons.CouponResponse;
import com.dtv.dcp.epoch.model.ct.coupon.Content;
import com.dtv.dcp.epoch.model.ct.coupon.Coupon;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.Messages;
import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;
import com.dtv.dcp.epoch.model.customergraph.CustomerCouponsResults;
import com.dtv.dcp.epoch.processor.helper.CPOPValidateCouponHelper;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessorUCCHelper;
import com.dtv.dcp.epoch.representation.Error;
import com.dtv.dcp.epoch.service.CouponValidationService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphClientService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.service.satellite.SatelliteCouponsService;
import com.dtv.dcp.epoch.util.CouponValidateUtils;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;


/**
 * @author nk3077
 */

/**
 * This Class has the information of the end point related  to validate the offer..
 *
 */

@Controller
public class CouponValidationResourceImpl  implements CouponValidationResource{

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(CouponValidationResourceImpl.class);

	private static final String ERROR_MESSAGE = "Unknown error ocuured in CouponValidationResourceImpl.getResults() method.";

	private static final String ENDPOINT = "API_NAME:EPOCH_VALIDATECOUPONOFFERS";

	/** reference of service class. */
	@Autowired
	private CouponValidationService couponValidationService;

	@Autowired
	private CustomerGraphService customerGraphService;

	@Autowired
	private SatelliteCouponsService satelliteCouponsService;
	
	@Autowired
	private CustomerGraphClientService customerGraphClientService;
	
	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	@Autowired
	SatelliteServicesOffersProcessorUCCHelper satelliteServicesOffersProcessorUCCHelper;

	/**
	 * This API is to validate the coupon
	 *
	 * @param headers , information of the header - session id , BAN etc 
	 * @param uriInfo , information about the url
	 * @param offerValidationRequest , this is actually a purchase context
	 * @return response - bundle offer result with meta-info
	 */
	/** The Constant SOURCE. */
	private static final String SOURCE = "CouponValidationResourceImpl";

	@Override
	public ResponseEntity couponValidation(@RequestHeader HttpHeaders headers, 
			@NotNull @RequestBody CouponValidationRequest couponValidationRequest) {
		MDC.put(Constants.TRACE_ID, null != headers ? headers.getFirst(Constants.TRACE_ID):"");
		try {

			log.info("{} CLIENT_REQUEST:[{}] TRACE_ID:[{}]", ENDPOINT, JsonService.getJsonFromObject(couponValidationRequest),
					headers.getFirst(Constants.TRACE_ID));
			FeatureManagerHelper.httpHeaders = headers;
			CPOPValidateCouponHelper cPOPValidateCouponHelper = new CPOPValidateCouponHelper();
			AccountContext accountContext = couponValidationRequest.getAccountContext();
			String customerId = accountContext.getBAN();
			String accountType= Constants.UVERSE_CG_ACCOUNT_TYPE;
			String accountId = "1w";
			String couponCode = couponValidationRequest.getCouponcode();
			String campaignCode = null;
			CTOfferResponse offerResponse = null;
			Boolean isCampaignExpired = false;
			Boolean isCampaignInactive = false;
			Integer maxApplications = 0;
			Boolean isPurchaseContextEligible = false;
			String couponType = null;
			Link link = null;
			Boolean isCouponAvailable = true;
			Boolean isCouponAvailableInAccount = true;
			Boolean isCouponAvailableInCT = true;
			Boolean isMultiUse2Coupon = false;
			CouponValidationResponse response =  new CouponValidationResponse();
			Content content = new Content(new ArrayList<>());
			content.setCoupons(new ArrayList<>());
			CouponResponseData couponResponseData = new CouponResponseData();

			log.debug("Start of CouponValidationResourceImpl ...");
			
			CustomerCouponsResults customerCouponsResults = null;
			CouponsRequestWrapper couponsRequestWrapper = new CouponsRequestWrapper();
			
			// Get customer coupon data from CG
			try {
				log.info("getUverseCustomerCoupons...");
				customerCouponsResults = customerGraphService.getUverseCustomerCoupons(accountId, customerId, accountType, CustomerCouponsResults.class, "primary");
			} catch (ServiceException e) {
				log.error(String.format("Exception while getUverseAccountCoupon %s", e.getMessage()));
			}
			
			// Get coupon details from CT
			couponsRequestWrapper.setCode(couponCode.toUpperCase());
			CTCouponResponse cTCouponResponse = satelliteCouponsService.getCoupons(couponsRequestWrapper);
			
			if(Objects.isNull(cTCouponResponse) || (Objects.nonNull(cTCouponResponse) && cTCouponResponse.getCount() == 0))
			{
				isCouponAvailableInCT = false;
			}

			// Get offers from CT for the campaign
			if(Objects.nonNull(cTCouponResponse))
			{
				log.info("cTCouponResponse getCount.....{}",cTCouponResponse.getCount());
				log.info("cTCouponResponse getTotal.....{}",cTCouponResponse.getTotal());
				log.info("cTCouponResponse getResults.....{}",cTCouponResponse.getResults());
				log.info("cTCouponResponse size.....{}",cTCouponResponse.getResults().size());
				
				List<Coupon> coupon = cTCouponResponse.getResults();

				for (Coupon value :  coupon)
				{ 
					maxApplications = value.getMaxApplications();
					if(!Optional.ofNullable(maxApplications).isPresent())
					{
						maxApplications = 0;
					}
					CustomCouponBase customCouponBase  = value.getCustom();
					CustomCouponFields customCouponFields  = customCouponBase.getFields();
					campaignCode = customCouponFields.getCampaignCode();
				}
				
				if(campaignCode != null && !campaignCode.isEmpty())
				{
					OfferRequestWrapper offerRequestWrapper = couponValidationService.generateOfferRequest(campaignCode);
					offerResponse = couponValidationService.getOffers(offerRequestWrapper);

					if(Objects.nonNull(offerResponse))
					{   
						log.info("offerResponse.getOffers() {}",offerResponse.getOffers().size());

						log.info("productResponse.getProducts() {}",offerResponse.getProducts().size());
						
						if(offerResponse.getOffers().size() == 0 || offerResponse.getProducts().size() == 0)
						{
							log.info("association of product and offer is invalid");
							Messages CouponResults = new Messages();
							CouponResults.setStatus("Error");
							CouponResults.setErrorCode("COUPON_INVALID_ERROR");
							CouponResults.setErrorMessage(Constants.COUPON_NOT_EXISTS);
							content.getCoupons().add(new VaildateCouponBase(couponCode, couponResponseData , CouponResults));
							response.setContent(content);
							return ResponseEntity.ok(response);
						}
						// Get coupon type
						couponType = getCouponType(offerResponse);
					}
				}
				
			}
			
			if (Objects.nonNull(customerCouponsResults)) {

				List<CustomerCoupons> customerCouponsList= customerCouponsResults.getCoupons();
				final String searchCode = couponCode;
				isCouponAvailableInAccount = customerCouponsList.stream().anyMatch(o -> o.getCouponCode().equalsIgnoreCase(searchCode));
				log.info("isCouponAvailableInAccount "+isCouponAvailableInAccount);

				for (CustomerCoupons CustomerCoupons :  customerCouponsList)
				{
					if(CustomerCoupons.getCouponCode().equalsIgnoreCase(couponCode))
					{
						log.info("CG COUPON INFO : [{}]", JsonService.getJsonFromObject(CustomerCoupons));
						if (Optional.ofNullable(CustomerCoupons.getStatusDescription()).isPresent()
								&& CustomerCoupons.getStatusDescription().equalsIgnoreCase(Constants.USED_COUPON)) {
							log.info("Updating the couponStatus to Available for coupon [{}]", OffersUtils.sanitizeData(couponCode));
							CustomerCoupons = updateCouponStatusToCG(customerId, CustomerCoupons);
						}
						if(!CustomerCoupons.getStatus().equalsIgnoreCase(Constants.AVAILABLE) &&
								!CustomerCoupons.getStatus().equalsIgnoreCase(Constants.EXPIRED) )
						{
							isCouponAvailable = false;
							log.info("isCouponAvailable set {}",isCouponAvailable);
						}
						
						if(CustomerCoupons.getStatus().equalsIgnoreCase(Constants.EXPIRED)) {
							isCampaignExpired = true;
							log.info("isCampaignExpired set:  {}", isCampaignExpired);
						} else {
							isCampaignExpired = false;
							log.info("isCampaignExpired set:  {}", isCampaignExpired);
						}
					}
				}
			}
			else
			{
				log.info("Scenerio - Coupon Customer Account is not available....");
				isCouponAvailableInAccount = false;
				
			}
			
			
			if(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_B_ENABLED) &&
				isCouponAvailableInCT && !isCouponAvailableInAccount &&
				couponType != null && (Constants.MULTI_USE_TYPE_2_COUPON).equalsIgnoreCase(couponType))
			{
				List<Coupon> couponsList = cTCouponResponse.getResults();
				for (Coupon coupon :  couponsList)
				{
					if(Optional.ofNullable(maxApplications).isPresent()
						&& Optional.ofNullable(coupon.getCustom()).isPresent()
						&& Optional.ofNullable(coupon.getCustom().getFields()).isPresent()
						&& Optional.ofNullable(coupon.getCustom().getFields().getCouponStatus()).isPresent()
						&& ((maxApplications == 0 && coupon.getCustom().getFields().getCouponStatus().equalsIgnoreCase(Constants.QUOTA_REACHED_COUPON))
								|| coupon.getCustom().getFields().getCouponStatus().equalsIgnoreCase(Constants.EXPIRED_COUPON)))
					{
						isCampaignExpired = true;
						log.info("isCampaignExpired set:  {}", isCampaignExpired);
						
					} else {
						isCampaignExpired = false;
						log.info("isCampaignExpired set:  {}", isCampaignExpired);
					}
				}
				
				if (couponType != null && (Constants.MULTI_USE_TYPE_2_COUPON).equalsIgnoreCase(couponType)) {
					isMultiUse2Coupon = true;
				} else {
					isMultiUse2Coupon = false;
				}	
			}
			
			
			log.info("isCouponAvailableInCT...{}",isCouponAvailableInCT);
			log.info("isCouponAvailableInAccount......{}",isCouponAvailableInAccount);
			log.info("isMultiUse2Coupon......{}",isMultiUse2Coupon);
			
			if(!isCouponAvailableInCT && !isCouponAvailableInAccount && !isMultiUse2Coupon)
			{
				log.info("isCouponAvailableInCT-false && isCouponAvailableInAccount-false");
				Messages CouponResults = new Messages();
				CouponResults.setStatus("Error");
				CouponResults.setErrorCode("COUPON_INVALID_ERROR");
				CouponResults.setErrorMessage(Constants.COUPON_NOT_EXISTS);
				content.getCoupons().add(new VaildateCouponBase(couponCode, null , CouponResults));
				response.setContent(content);
				return ResponseEntity.ok(response);
			}
			else if (isCouponAvailableInCT && !isCouponAvailableInAccount && !isMultiUse2Coupon)
			{
				log.info("isCouponAvailableInCT-true && isCouponAvailableInAccount-false");
				Messages CouponResults = new Messages();
				CouponResults.setStatus("Error");
				CouponResults.setErrorCode("COUPON_ACCOUNT_INELGIBLE_ERROR");
				CouponResults.setErrorMessage(Constants.COUPON_ACCOUNT_INELIGIBLE);
				content.getCoupons().add(new VaildateCouponBase(couponCode, null, CouponResults));
				response.setContent(content);
				return ResponseEntity.ok(response);
			}
			else if (!isCouponAvailableInCT && isCouponAvailableInAccount && !isMultiUse2Coupon)
			{
				log.info("isCouponAvailableInCT-false && isCouponAvailableInAccount-true");
				Messages CouponResults = new Messages();
				CouponResults.setStatus("Error");
				CouponResults.setErrorCode("COUPON_INVALID_ERROR");
				CouponResults.setErrorMessage(Constants.COUPON_NOT_EXISTS);
				content.getCoupons().add(new VaildateCouponBase(couponCode, null , CouponResults));
				response.setContent(content);
				return ResponseEntity.ok(response);
			}
			
			if(Objects.nonNull(cTCouponResponse))
			{	
				CouponResponse updateCTCouponResponse = cPOPValidateCouponHelper.mergeCustomerGraphCouponResponse(customerCouponsResults, 
						cTCouponResponse, isMultiUse2Coupon, couponValidationRequest.getCouponcode());
				 List<ProductObj> inActiveCampaigns = new ArrayList<>();
				if(campaignCode != null && !campaignCode.isEmpty())
				{
//					 OfferRequestWrapper offerRequestWrapper = couponValidationService.generateOfferRequest(campaignCode);
//					 offerResponse = couponValidationService.getOffers(offerRequestWrapper);
					
					if(Objects.nonNull(offerResponse))
					{   
						isPurchaseContextEligible = couponValidationService.ValidateCampaignEligibility(offerResponse,couponValidationRequest);

						log.info("isPurchaseContextEligible {}",isPurchaseContextEligible);

						couponResponseData.setProducts(cPOPValidateCouponHelper.processProductsResponse(offerResponse.getProducts()));
						couponResponseData.setOffers(cPOPValidateCouponHelper.processOffersResponse(offerResponse.getOffers(), 
								featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_B_ENABLED)));
						couponResponseData.setCoupons(updateCTCouponResponse.getResults());
						satelliteServicesOffersProcessorUCCHelper.updateTitlesResponse(couponResponseData);


						if(Objects.nonNull(offerResponse.getProducts()))
						{
							inActiveCampaigns = offerResponse.getProducts().stream().filter(Objects::nonNull).filter(ctOffer -> {
								log.info("validating inActive campaigns---check...");
								if (ctOffer.getVariants() != null && ctOffer.getVariants().get(0).getAttributes() != null && ctOffer.getVariants().get(0).getAttributes().getCampaignStartDate() != null) {
									log.info("validating inActive campaigns");
											return CouponValidateUtils.validateActiveDate(ctOffer.getVariants().get(0).getAttributes().getCampaignStartDate());
										} else {
											return false;
										}
							}).collect(Collectors.toList());
						}
					}
					
					//validate eligibility

					 log.info("inActiveCampaigns.size() {}",inActiveCampaigns.size());

					if(inActiveCampaigns.size() > 0)	{
						log.info("CampaignInactive status is true");
						isCampaignInactive = true;
					} else {
						log.info("CampaignInactive status is false");
					}

					log.info("ProductResponse-attributeList-isPurchaseContextEligible {}",isPurchaseContextEligible);
					log.info("isCouponAvailable {}",isCouponAvailable);
					log.info("isCampaignInactive = {}",isCampaignInactive);
					
					if(!isCouponAvailable && !isPurchaseContextEligible) {
						log.info("Scenerio - Validate PurchaseContext....");
						Messages CouponResults = new Messages();
						CouponResults.setStatus("Error");
						CouponResults.setErrorCode("COUPON_PURCHASE_INELGIBLE_ERROR");
						CouponResults.setErrorMessage(Constants.COUPON_INVALID_PURCHASE);
						content.getCoupons().add(new VaildateCouponBase(couponCode, couponResponseData, CouponResults));

					} else if(!isCouponAvailable && isPurchaseContextEligible) {
						log.info("Scenerio - Validate Coupon is availabe or not");
						Messages CouponResults = new Messages();
						CouponResults.setStatus("Error");
						CouponResults.setErrorCode("COUPON_REDEEMED_ERROR");
						CouponResults.setErrorMessage(Constants.COUPON_ALREADY_REDEEMED);
						content.getCoupons().add(new VaildateCouponBase(couponCode, couponResponseData, CouponResults));
						response.setContent(content);
						return ResponseEntity.ok(response);
					} else if(isCampaignExpired) {  
						
						log.info("Scenerio-Campaign Expired...");
						Messages CouponResults = new Messages();
						CouponResults.setStatus("Error");
						CouponResults.setErrorCode("COUPON_EXPIRED_ERROR");
						CouponResults.setErrorMessage(Constants.COUPON_EXPIRED);
						content.getCoupons().add(new VaildateCouponBase(couponCode, couponResponseData , CouponResults));

					} else if (isCampaignInactive) {
						log.info("Scenerio-Campaign Inactive...");
						Messages CouponResults = new Messages();
						CouponResults.setStatus("Error");
						CouponResults.setErrorCode("COUPON_NOTACTIVE_ERROR");
						CouponResults.setErrorMessage(Constants.COUPON_INACTIVE);
						content.getCoupons().add(new VaildateCouponBase(couponCode, couponResponseData, CouponResults));

					} else if (!isPurchaseContextEligible) {
						log.info("Scenerio - Validate PurchaseContext....");
						Messages CouponResults = new Messages();
						CouponResults.setStatus("Error");
						CouponResults.setErrorCode("COUPON_PURCHASE_INELGIBLE_ERROR");
						CouponResults.setErrorMessage(Constants.COUPON_INVALID_PURCHASE);
						content.getCoupons().add(new VaildateCouponBase(couponCode, couponResponseData, CouponResults));

					} else if(maxApplications == 0 && (StringUtils.isNotEmpty(couponType)
							&& !couponType.equalsIgnoreCase(Constants.SINGLE_USE_COUPON))) {
						log.info("Scenerio - Validate Quota limit for Multiuse Token...");
						Messages CouponResults = new Messages();
						CouponResults.setStatus("Error");
						CouponResults.setErrorCode("COUPON_REDEEMED_ERROR");
						CouponResults.setErrorMessage(Constants.COUPON_ALREADY_REDEEMED);
						content.getCoupons().add(new VaildateCouponBase(couponCode, couponResponseData, CouponResults));
					}
					else {
						log.info("Scenerio - Success...");
						Messages CouponResults = new Messages();
						CouponResults.setStatus(Constants.SUCCESS);
						CouponResults.setErrorCode("");
						CouponResults.setErrorMessage("");
						content.getCoupons().add(new VaildateCouponBase(couponCode, couponResponseData, CouponResults));
					}
				}
				else 
				{
					log.info("Scenerio - campaign code empty");
					Messages CouponResults = new Messages();
					CouponResults.setStatus("Error");
					CouponResults.setErrorCode("COUPON_INVALID_ERROR");
					CouponResults.setErrorMessage(Constants.COUPON_NOT_EXISTS);
					content.getCoupons().add(new VaildateCouponBase(couponCode, couponResponseData , CouponResults));
				}
			}
			log.debug("End of CouponValidationResourceImpl ...");
			
			response.setContent(content);
			
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			log.error(ERROR_MESSAGE, ex);
			log.error("{} STATUS:FAILED SYSTEM ERROR-[{}]", ex.getMessage());
			ServiceException serviceException =	new ServiceException(ErrorMessages.SYSTEM_ERROR, ex);
			Error error =new Error(serviceException.getError());
			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));

			/*
			 * throw ((new ServiceException(ErrorMessages.SYSTEM_ERROR, ex)).addDetail(
			 * ErrorMessages.SYSTEM_ERROR, SOURCE));
			 */
		}

	}
	
	private String getCouponType(CTOfferResponse offerResponse) {
		if (offerResponse != null && Optional.ofNullable(offerResponse.getProducts()).isPresent()
				&& offerResponse.getProducts().size() > 0
				&& Objects.nonNull(offerResponse.getProducts().get(0).getVariants())
				&& Objects.nonNull(offerResponse.getProducts().get(0).getVariants().get(0).getAttributes())
				&& Objects.nonNull(offerResponse.getProducts().get(0).getVariants().get(0).getAttributes()
								.getCouponType()))
	    		{  		
	    			return offerResponse.getProducts().get(0).getVariants().get(0).getAttributes()
							.getCouponType();
	    		}
		return null;
	}

	/**
	 * gives current time in string format
	 */
	public String getCurrentTime() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		// Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_CG);
		log.info("getCurrentTime returned.....{}",dateFormat.format(calendar.getTime()));
        return dateFormat.format(calendar.getTime());
	}
	
	private CustomerCoupons updateCouponStatusToCG(String accountId, CustomerCoupons coupon) {
		if (Optional.ofNullable(coupon.getLockDuration()).isPresent() && Optional.ofNullable(coupon.getUsedDate()).isPresent()) {
			log.info("Used Date.....{}",coupon.getUsedDate());
			log.info("LockDuration.....{}",coupon.getLockDuration());
			String differenceDateTime = getDifferenceTime(coupon.getUsedDate());
			log.info("differenceDateTime.....{}",differenceDateTime);
			int lockDuration = Integer.parseInt(coupon.getLockDuration());
			int diffDateTime = Integer.parseInt(differenceDateTime);
			if (diffDateTime >= lockDuration) {
				log.info("Change in CG Used to avaible call publishUpdateCouponMessage()");
				customerGraphClientService.publishUpdateCouponMessage(coupon, accountId);
				log.info("Change in CG Used to avaible call publishUpdateCouponMessage() : sucess");
				coupon.setStatus(Constants.AVAILABLE);
				coupon.setStatusDescription(Constants.AVAILABLE_COUPON);
				return coupon;
			}
		}
		log.info("No Change in CG Used to avaible, no call to publishUpdateCouponMessage()");
		return coupon;
	}
	
	private String getDifferenceTime(String usedDateTime) {
		String currentTime = getCurrentTime();
		log.info("currentTime.....{}",currentTime);
		log.info("usedDateTime.....{}",usedDateTime);
		try {
			
			Date date1 = new SimpleDateFormat(Constants.DATE_FORMAT_CG).parse(currentTime);
			Date date2 = new SimpleDateFormat(Constants.DATE_FORMAT_CG).parse(usedDateTime);
			log.info("formated currentTime.....{}",date1);
			log.info("formated usedDateTime.....{}",date2);
			long seconds = (date1.getTime()-date2.getTime())/Constants.ONE_THUSAND;
			log.info("difference time.....{}",String.valueOf(seconds));
			return String.valueOf(seconds);
		} catch (ParseException e) {
			log.error(currentTime, e);
		}
		return null;
	}

}