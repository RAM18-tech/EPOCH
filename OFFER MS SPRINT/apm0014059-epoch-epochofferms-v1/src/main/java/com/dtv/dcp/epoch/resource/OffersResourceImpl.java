package com.dtv.dcp.epoch.resource;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.customergraph.CustomerGraphClient;
import com.dtv.dcp.epoch.integration.dtvnaccount.DtvnAccountClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTCheckOfferEligibilityResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.OfferEligibility;
import com.dtv.dcp.epoch.processor.helper.CPOPAdditionalOfferHelper;
import com.dtv.dcp.epoch.representation.Content;
import com.dtv.dcp.epoch.representation.Error;
import com.dtv.dcp.epoch.service.bundle.BundleOfferService;
import com.dtv.dcp.epoch.service.ott.OttOffersService;
import com.dtv.dcp.epoch.service.satellite.SatelliteOffersService;
import com.dtv.dcp.epoch.service.watchtv.WatchTVOffersService;
import com.dtv.dcp.epoch.util.CTOfferRequestHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonFilterService;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;


/**
 * The Class OffersResourceImpl.
 * <p>
 * Created by nk3077 on 07/30/2019.
 */
@RestController
@PropertySource("classpath:remove-attributes-from-satellite-response.properties")
public class OffersResourceImpl implements OffersResource {

    /**
     * The log.
     */
    private static Logger log = LoggerFactory.getLogger(OffersResourceImpl.class);

    /**
     * The Constant for ERROR_MESSAGE.
     */
    private static final String ERROR_MESSAGE = "Unknown error ocuured in OffersResourceImpl.getOffers() method.";
    
    /**
     * The Constant SOURCE.
     */
    private static final String SOURCE = "OffersResourceImpl";

    private static final String ENDPOINT = "API_NAME:EPOCH_GETOFFERS";

    private static final String ELIGIBILITY_ENDPOINT = "API_NAME:EPOCH_CHECKOFFERSELIGIBILITY";

    @Autowired
    private OttOffersService ottOffersService;

    @Autowired
    private SatelliteOffersService satelliteOffersService;

    @Autowired
    private WatchTVOffersService watchTVOffersService;

    @Autowired
    private BundleOfferService bundleOfferService;

    @Autowired
    CPOPAdditionalOfferHelper dtvNowCPOPAdditionalOfferHelper;

    @Autowired
    DtvnAccountClient dtvnAccountClient;

    @Autowired
    private CTOfferRequestHelper ctOfferRequestHelper;

    @Autowired
	private FeatureManagerHelper featureManagerHelper;

    @Value("#{'${attributesToRemoveInResponse}'.split(',')}")
    private String[] attributesToRemoveInResponse;

	@Autowired
	private CustomerGraphClient customerGraphClient;
	
	@Autowired
	private OffersUtils offersUtils;
	
	@Autowired
	RedisCacheHelper redisCacheHelper;

    @Override
    public ResponseEntity getOffers(HttpHeaders headers,  @NotNull OfferRequest offersRequest) {
		//validateRequest(offersRequest);
    	MDC.put(Constants.TRACE_ID, null != headers ? headers.getFirst(Constants.TRACE_ID):"");
		log.info("getOffers headers {} ",headers);
    
    	Content<CTOfferResponse> content = null;
        CTOfferResponse baseOffersResponse = null;
        long startTimeInMillies = System.currentTimeMillis();
		log.info("{} CLIENT_REQUEST:[{}] TRACE_ID:[{}]", ENDPOINT, JsonService.getJsonFromObject(offersRequest),
				headers.getFirst(Constants.TRACE_ID));
		
		if (isSatelliteExistCustUpgDwngrade(offersRequest)) {
			if (isInvalidProgrammingPackage(offersRequest)) {
				ServiceException serviceException =	new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_PROGRAMMING_PACKAGE, new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_PROGRAMMING_PACKAGE));
				Error error =new Error(serviceException.getError());
				return new ResponseEntity<>(error,HttpStatus.valueOf(400));
				//throw new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_PROGRAMMING_PACKAGE);
			} else if (isInvalidAccountStatus(offersRequest)) {
				ServiceException serviceException =	new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_ACCOUNT_STATUS, new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_ACCOUNT_STATUS));
				Error error =new Error(serviceException.getError());
				return new ResponseEntity<>(error,HttpStatus.valueOf(400));
				//throw new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_ACCOUNT_STATUS);
			} else if(isNotActiveAccount(offersRequest)) {
				ServiceException serviceException =	new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_ACCOUNT_STATUS, new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_ACCOUNT_STATUS));
				Error error =new Error(serviceException.getError());
				return new ResponseEntity<>(error,HttpStatus.valueOf(400));
			}
		}	
		
        try {
        	FeatureManagerHelper.httpHeaders = headers;
        	baseOffersResponse=getBaseOffersResponse(headers, offersRequest, ENDPOINT);
        	content = new Content<>(baseOffersResponse);
        } catch (ServiceException serviceException) {
            log.error(SOURCE + "getOffers() :  ServiceException= ", serviceException);
            log.error("{} STATUS:FAILED SERVICEEXCEPTION EPOCH_GETOFFERS_FAILED- [{}] OFFER_PRODUCT_FAMILY-[{}]",ENDPOINT,serviceException.getError(),OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
            Error error =new Error(serviceException.getError());
			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));
			//throw serviceEx;

        } catch (Exception ex) {
            log.error(ERROR_MESSAGE, ex);
            log.error("{} STATUS:FAILED EXCEPTION EPOCH_GETOFFERS_FAILED-[{}] OFFER_PRODUCT_FAMILY-[{}]",ENDPOINT, ex.getMessage(),OffersUtils.sanitizeData
(offersRequest.getOfferProductFamily()));
            if (!offersRequest.getOfferProductFamily().isEmpty() && offersRequest.getOfferProductFamily().contains("OTT")) {
    			ServiceException serviceException =	new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001, ex);
    			Error error =new Error(serviceException.getError());
    			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));
            } else {
            	ServiceException serviceException =	new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001, ex);
    			Error error =new Error(serviceException.getError());
    			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));
            }
			
        }
        log.debug("End of OffersResourceImpl.getOffers() method..");
        log.info("{} STATUS:SUCCESS EPOCH_GETOFFERS_SUCCESS OFFER_PRODUCT_FAMILY-[{}]",ENDPOINT,OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
		if (Objects.nonNull(baseOffersResponse) && CollectionUtils.isNotEmpty(baseOffersResponse.getOffers())) {
			log.info("{} OFFER_COUNT-[{}] FOR SALES_CHANNEL-[{}] OFFER_PRODUCT_FAMILY-[{}]", ENDPOINT, baseOffersResponse.getOffers().size(), OffersUtils.sanitizeData(offersRequest.getSalesChannel()), OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
		}else {
			log.info("{} OFFER_COUNT-[0] FOR SALES_CHANNEL-[{}] OFFER_PRODUCT_FAMILY-[{}]", ENDPOINT, OffersUtils.sanitizeData(offersRequest.getSalesChannel()), OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
		}
        if (featureManagerHelper.isEnabled("svc-epoch-ade-cache-perf-change")
				&& ((Optional.ofNullable(offersRequest.getOfferActionType()).isPresent()
						&& !offersRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
						&& !offersRequest.getOfferActionType().contains(Constants.SOS_ACTION_TYPE)
						&& !offersRequest.getOfferActionType().contains(Constants.CLOSING_ACTION_TYPE))
						&& (Optional.ofNullable(offersRequest.getSalesChannel()).isPresent()
								&& offersRequest.getSalesChannel().contains("online"))
						&& (Optional.ofNullable(offersRequest.getOfferProductFamily()).isPresent()
								&& offersRequest.getOfferProductFamily().contains("satellite")))) {
			log.info("{} EPOCH_GETADEOFFERS_EXECUTION_TIME-[{}] FOR SALES_CHANNEL-[{}] OFFER_PRODUCT_FAMILY-[{}]",
					ENDPOINT, (System.currentTimeMillis() - startTimeInMillies),
					OffersUtils.sanitizeData(offersRequest.getSalesChannel()),
					OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
		} else {
			log.info("{} EPOCH_GETOFFERS_EXECUTION_TIME-[{}] FOR SALES_CHANNEL-[{}] OFFER_PRODUCT_FAMILY-[{}]",ENDPOINT,
					(System.currentTimeMillis() - startTimeInMillies), OffersUtils.sanitizeData(offersRequest.getSalesChannel()),OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
		}
        return ResponseEntity.ok(baseOffersResponse);
    }


	/**
	 * Adding this method to do request validation only for Satellite flow and
	 * existing customer with invalid status or empty assets trying to perform
	 * Upgrade/ Downgrade
	 * 
	 * @param offersRequest Offer Request to Validate
	 */
	public void validateRequest(OfferRequest offersRequest) {
		if (!isSatelliteExistCustUpgDwngrade(offersRequest)) {
			return;
		}
		if (isInvalidProgrammingPackage(offersRequest)) {
			throw new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_PROGRAMMING_PACKAGE);
		} else if (isInvalidAccountStatus(offersRequest)) {
			throw new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_ACCOUNT_STATUS);

		}
	}

	/**
	 * Check for Satellite existing customer flow performing upgrade or downgrade
	 * 
	 * @param offersRequest Offer Request to Validate
	 * @return boolean
	 */
	public boolean isSatelliteExistCustUpgDwngrade(OfferRequest offersRequest) {
		return null != offersRequest.getOfferProductFamily()
				&& offersRequest.getOfferProductFamily().contains(Constants.SATELLITE_PRODUCT_FAMILY)
				&& null != offersRequest.getOfferActionType() && checkOperation(offersRequest.getOfferActionType())
				&& (null != offersRequest.getCustomerContext()
						&& null != offersRequest.getCustomerContext().getSatellite());
	}

	/**
	 * Check for Valid Account Status for STMS Flow
	 * 
	 * @param offersRequest Offer Request to Validate
	 * @return boolean
	 */
	public boolean isInvalidAccountStatus(OfferRequest offersRequest) {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offersRequest);
		return offersUtils.isSTMSRequest(offerRequestWrapper)
				&& null != offersRequest.getCustomerContext().getSatellite().getAccountStatus()
				&& Arrays.stream(Constants.VALID_ACCOUNT_STATUS)
						.noneMatch(offersRequest.getCustomerContext().getSatellite().getAccountStatus()::equals);
	}

	/**
	 * Check if Programming Package exists for the flow where customer is trying to
	 * upgrade or downgrade.
	 * 
	 * @param offersRequest Offer Request to Validate
	 * @return boolean
	 */
	private boolean isInvalidProgrammingPackage(OfferRequest offersRequest) {
		return CollectionUtils.isEmpty(offersRequest.getCustomerContext().getSatellite().getProducts());
	}

	/**
	 * Check for Upgrade Operation
	 * 
	 * @param offerActionType Offer Action from request
	 * @return boolean
	 */
	private boolean checkOperation(List<String> offerActionType) {
		return Arrays.asList(Constants.OFFER_ACTION_TYPES).stream()
				.anyMatch(element -> offerActionType.stream().anyMatch(element::equalsIgnoreCase));
	}

    @Override
	public ResponseEntity checkOffersEligibility(HttpHeaders headers,  @NotNull OfferRequest offersRequest) {
    	MDC.put(Constants.TRACE_ID, null != headers ? headers.getFirst(Constants.TRACE_ID):"");
    	log.info("checkOffersEligibility headers {} ",headers);
		Content<CTCheckOfferEligibilityResponse> content = null;
		CTOfferResponse baseOffersResponse = null;
		CTCheckOfferEligibilityResponse response=null;
		List<String> offerCodes=null;
		long startTimeInMillies = System.currentTimeMillis();
		log.info("{} CLIENT_REQUEST:[{}] TRACE_ID:[{}]", ELIGIBILITY_ENDPOINT, JsonService.getJsonFromObject(offersRequest),
				headers.getFirst(Constants.TRACE_ID));
		try {
			FeatureManagerHelper.httpHeaders = headers;
			offerCodes=offersRequest.getOfferCodes();
			offersRequest.setOfferCodes(new ArrayList<String>());
			baseOffersResponse=getBaseOffersResponse(headers, offersRequest, ELIGIBILITY_ENDPOINT);
			long eligibilityStartTimeInMillies = System.currentTimeMillis();
			
			if(offerCodes!=null && !offerCodes.isEmpty())
			{
				response=new CTCheckOfferEligibilityResponse();
				for(String offerCode: offerCodes)
				{
					if(baseOffersResponse!=null && null != baseOffersResponse.getOffers() && !baseOffersResponse.getOffers().isEmpty()) {
						response.getOfferStatuses().add(new OfferEligibility(offerCode, baseOffersResponse.getOffers().stream().filter(Objects::nonNull)
						  .anyMatch(offer -> (offer.getCode()!=null && offerCode.equalsIgnoreCase(offer.getCode())))));
					}
					else
					{
						response.getOfferStatuses().add(new OfferEligibility(offerCode, false));
					}
				}
			}

			log.info("{} EPOCH_CHECKOFFERSELIGIBILITY_ELIGIBILITY EXECUTION_TIME-[{}] FOR SALES_CHANNEL-[{}] OFFER_PRODUCT_FAMILY-[{}]",ELIGIBILITY_ENDPOINT,
					(System.currentTimeMillis() - eligibilityStartTimeInMillies), OffersUtils.sanitizeData(offersRequest.getSalesChannel()),OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
			content = new Content<>(response);

		} catch (ServiceException serviceException) {
			log.error(SOURCE + "getOffers() :  ServiceException= ", serviceException);
			log.error("{} STATUS:FAILED SERVICEEXCEPTION EPOCH_CHECKOFFERSELIGIBILITY_FAILED- [{}] OFFER_PRODUCT_FAMILY-[{}]",ELIGIBILITY_ENDPOINT,serviceException.getError(),OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
			Error error =new Error(serviceException.getError());
			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));
			//throw serviceEx;

		} catch (Exception ex) {
			log.error(ERROR_MESSAGE, ex);
			log.error("{} STATUS:FAILED EXCEPTION EPOCH_CHECKOFFERSELIGIBILITY_FAILED-[{}] OFFER_PRODUCT_FAMILY-[{}]",ELIGIBILITY_ENDPOINT, ex.getMessage(),OffersUtils.sanitizeData
					(offersRequest.getOfferProductFamily()));
			 if (!offersRequest.getOfferProductFamily().isEmpty() && offersRequest.getOfferProductFamily().contains("OTT")) {
				ServiceException serviceException =	new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001, ex);
				Error error =new Error(serviceException.getError());
				return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));
			} else {
				ServiceException serviceException =	new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001, ex);
				Error error =new Error(serviceException.getError());
				return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));
			}
		}
		log.debug("End of CheckOffersEligibilityResourceImpl.checkOffersEligibility() method..");
		log.info("{} STATUS:SUCCESS EPOCH_CHECKOFFERSELIGIBILITY_SUCCESS OFFER_PRODUCT_FAMILY-[{}]",ELIGIBILITY_ENDPOINT,OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
		log.info("{} EPOCH_CHECKOFFERSELIGIBILITY_EXECUTION_TIME-[{}] FOR SALES_CHANNEL-[{}] OFFER_PRODUCT_FAMILY-[{}]",ELIGIBILITY_ENDPOINT,
				(System.currentTimeMillis() - startTimeInMillies), OffersUtils.sanitizeData(offersRequest.getSalesChannel()),OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
		return ResponseEntity.ok(response);
	}

	/**
	 * @param offerProductFamily
	 * @param offerTypes
	 * @param baseOffersResponse
	 * @return
	 */
	private boolean isRewardOfferRequested(List<String> offerProductFamily, List<String> offerTypes,
			CTOfferResponse baseOffersResponse) {
		return ((offerTypes != null && offerTypes.stream()
				.anyMatch(Predicate.isEqual(Constants.REWARD).or(Predicate.isEqual(Constants.FLATOFF))))
				&& (offerProductFamily.stream()
						.anyMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY)
								.or(Predicate.isEqual(Constants.BB_PRODUCT_FAMILY)
										.or(Predicate.isEqual(Constants.IPTV_PRODUCT_FAMILY))
										.or(Predicate.isEqual(Constants.DTVS_PRODUCT_FAMILY))
										.or(Predicate.isEqual(Constants.WIRELESS_PRODUCT_FAMILY)))))
				&& ((baseOffersResponse != null && baseOffersResponse.getOffers() != null
						&& baseOffersResponse.getOffers().isEmpty()) || (baseOffersResponse == null))

		);
	}

	private CTOfferResponse getBaseOffersResponse(HttpHeaders headers,@NotNull OfferRequest offersRequest, String endPoint)
	{
		CTOfferResponse baseOffersResponse = null;

		OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offersRequest);
		log.info("{} STATUS:INVOKED EPOCH_GETOFFERS_INVOKED_WITH_CHANNEL- [{}] OFFER_PRODUCT_FAMILY-[{}]",ENDPOINT,OffersUtils.sanitizeData(offersRequest.getSalesChannel()),OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
        log.info("{} BUSINESSSEGMENT:{}",ENDPOINT,OffersUtils.sanitizeData(offersRequest.getBusinessSegment()));
		if (!(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_ENABLED)
				&& Optional.ofNullable(offersRequest.getOfferTypes()).isPresent()
				&& (StringUtils.equalsIgnoreCase(Constants.COUPON, offersRequest.getOfferTypes().get(0))))) {
            List<String> salesChannelFromConfig = Arrays.asList(redisCacheHelper.getValues(Constants.REDIS_CACHE_SALESCHANNEL, Constants.OTT).get(0).split("\\s*,\\s*"));
			if (CollectionUtils.isNotEmpty(offersRequest.getSalesChannel()) && StringUtils.isNotEmpty(offersRequest.getSalesChannel().get(0)) && !salesChannelFromConfig.contains(offersRequest.getSalesChannel().get(0))) {
				log.info("{} STATUS:FAILED EPOCH_GETOFFERS_FAILED_CHANNEL_INVALID", ENDPOINT);
				throw new ServiceException(ErrorMessages.CTLG_DTVN_INVALID_CHANNEL);
			}
		}

        if(ENDPOINT != null) {
        	log.info("{} ENDPOINT:{}",OffersUtils.sanitizeData(ENDPOINT));
        }

        log.info("{} OFFEPRODUCTFAMILY:{}",OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
		log.info("{} OFFERTYPE:{}", OffersUtils.sanitizeData(offersRequest.getOfferTypes()));
		log.info("{} OFFERCODES:{}",OffersUtils.sanitizeData(offersRequest.getOfferCodes()));
		log.info("{} OFFERIDS:{}", OffersUtils.sanitizeData(offersRequest.getOfferIds()));
		log.info("{} BENEFITCODES:{}",OffersUtils.sanitizeData(offersRequest.getBenefitCodes()));
		log.info("{} CLASSIFICATION:{}",OffersUtils.sanitizeData(offersRequest.getOfferClassificationType()));

        /**
         * On the bases of the offerProductFamily service class will be get
         * call "OTT","Wireless","IPTV","BB","Satellite","VOIP"
         *
         */
        if (Optional.ofNullable(offersRequest.getOfferProductFamily()).isPresent() && !offersRequest.getOfferProductFamily().isEmpty() && !offersRequest.getOfferProductFamily().contains(null)) {
            List<String> offerProductFamily = offersRequest.getOfferProductFamily();
            /**
             * checking the OTT family Only if not present then throw the
             * error
             *
             */

            if (offerProductFamily.stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
                log.debug("Start of OTT GET OFFER method..");
                baseOffersResponse = ottOffersService.getOffers(offerRequestWrapper);
                log.debug("end of OTT GET OFFER method..");
            }

            if (offerProductFamily.contains(Constants.SATELLITE_PRODUCT_FAMILY)) {
				log.info("EPOCH_GETOFFERS_SATELLITEFLOW");
                log.debug("Start of Satellite GET OFFER method..");
                baseOffersResponse = satelliteOffersService.getOffers(offerRequestWrapper);
                log.debug("end of OTT Satellite OFFER method..");
            }

            if (offerProductFamily.contains(Constants.WATCHTV_PRODUCT_FAMILY)) {
            	log.info("EPOCH_GETOFFERS_WATCHTVFLOW");
                log.debug("Start of watchTV GET OFFER method..");
                baseOffersResponse = watchTVOffersService.getOffers(offerRequestWrapper);
                log.debug("end of OTT watchTV OFFER method..");
            }
            if (isRewardOfferRequested(offerProductFamily, offersRequest.getOfferTypes(), baseOffersResponse)) {
            	log.info("EPOCH_GETOFFERS_VBBFLOW");
                log.debug("Start of VBB GET OFFER method..");
                baseOffersResponse = bundleOfferService.getBundleOffers(offerRequestWrapper);
                log.debug("end of VBB GET OFFER method..");
            }

        } else if (Optional.ofNullable(offersRequest.getOfferCodes()).isPresent()) {

            log.debug("Start of watchTV GET OFFER method..");
            baseOffersResponse = watchTVOffersService.getOffers(offerRequestWrapper);
            log.debug("end of OTT watchTV OFFER method..");

        } else {
            // TO DO through ERROR if no
            log.info("Validate request payload, seems no OfferProductFamily has been passed in request object...");
        }

		if (Optional.ofNullable(offersRequest.getOfferProductFamily()).isPresent() && !offersRequest
				.getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.WIRELESS_PRODUCT_FAMILY))) {
        // Add attributes here which are not needed by FE to consume.
        baseOffersResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseOffersResponse,
        		CTOfferResponse.class, Stream.of("computedRules", "offerPromos", "opusStoreIds",
						"compatibleProductsList","maxCustomerEligibilityTenureInDays", "onlinePartnerDetails",
						"opusDealerID1", "opusDealerID2", "opusSubChannels", "opusChannels", "parentOffers","ineligibleAccountStatus",
						"ineligiblePartners", "qualifyingProductCheckNOTRequired","choiceGroupSalesChannel",
                        "offerMinMaxQuantity","removalRuleByChannel","customerTypes","channelEligibilityByKey","skipProductsOnAccountToSuppress").toArray(String[]::new));
        log.info("{} ATTRIBUTE EXCLUSION FOR WIRELINE:{}",ENDPOINT,OffersUtils.sanitizeData(offersRequest.getOfferProductFamily()));
        }

		if (Optional.ofNullable(offersRequest.getOfferProductFamily()).isPresent()
				&& !offersRequest.getOfferProductFamily().isEmpty()
				&& !offersRequest.getOfferProductFamily().contains(null)
				&& offersRequest.getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))
				&& Objects.nonNull(baseOffersResponse)
				&& CollectionUtils.isNotEmpty(baseOffersResponse.getOffers())) {
			// Suppress AutoRenewable messages for employee where benefit type is free promo
			offersUtils.filterAutoRenewMessage(baseOffersResponse.getOffers(), offerRequestWrapper);
		}
		if (OffersUtils.needsAttributeRemoval(offersRequest.getOfferProductFamily(), offersRequest.getOfferTypes(), offersRequest.getOfferProductType())) {
			// Add attributes here which are not needed by FE to consume.
			baseOffersResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseOffersResponse,
					CTOfferResponse.class, Stream.of(attributesToRemoveInResponse).toArray(String[]::new));
		}


		if (Optional.ofNullable(offersRequest.getOfferProductFamily()).isPresent() && offersRequest
				.getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
				if(offerRequestWrapper!=null && offerRequestWrapper.isEmployeeAccount()) {
			// Remove AutoRenewable field and messages for Merlin
			baseOffersResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseOffersResponse,
					CTOfferResponse.class, Stream.of("isAutoRenewable","minAddonCount","maxAddonCount","addonCount","maxOccurrence_courtesy","maxOccurrence_showroom","maxOccurrence_bcomp",
							"maxOccurrence_deca","maxOccurrence_demo","channel","compliance","complianceRanking", "partnerDealerCode1", "partnerDealerCode2","additionalEligibility", "zip", "dma",
							"treatmentCode","creditRisk","eligibilityCreditRisk","customerGroup","subCategoryByKey","benefitCodesToSuppressTheOffer","benefitCodes","coolOffPeriod","supportedCohorts","displayTypeByKey", "skipProductsOnAccountToSuppress","complianceRank","billingProductGroup").toArray(String[]::new));
				}
				else {
					//Temporary fix to remove ribbonTextsByKey attribute from the response for sales channel directvOnline and IapPartnerAccountType FIRETV
					if(offersRequest.getCustomerContext() != null && offersRequest.getCustomerContext().getOtt() != null
						&& "FIRETV".equalsIgnoreCase(offersRequest.getCustomerContext().getOtt().getIapPartnerAccountType())
						&& CollectionUtils.isNotEmpty(offersRequest.getSalesChannel()) && offersRequest.getSalesChannel().contains("directvOnline")) {
						baseOffersResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseOffersResponse,
								CTOfferResponse.class, Stream.of("minAddonCount","maxAddonCount","addonCount","maxOccurrence_courtesy","maxOccurrence_showroom","maxOccurrence_bcomp",
										"maxOccurrence_deca","maxOccurrence_demo","channel","compliance","complianceRanking", "partnerDealerCode1", "partnerDealerCode2","additionalEligibility","zip", "dma",
										"treatmentCode","creditRisk","eligibilityCreditRisk","customerGroup","subCategoryByKey","benefitCodesToSuppressTheOffer","benefitCodes","coolOffPeriod","supportedCohorts","displayTypeByKey","ribbonTextsByKey", "skipProductsOnAccountToSuppress","complianceRank","billingProductGroup").toArray(String[]::new));

					} else {
						baseOffersResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseOffersResponse,
								CTOfferResponse.class, Stream.of("minAddonCount","maxAddonCount","addonCount","maxOccurrence_courtesy","maxOccurrence_showroom","maxOccurrence_bcomp",
										"maxOccurrence_deca","maxOccurrence_demo","channel","compliance","complianceRanking", "partnerDealerCode1", "partnerDealerCode2","additionalEligibility","zip", "dma",
										"treatmentCode","creditRisk","eligibilityCreditRisk","customerGroup","subCategoryByKey","benefitCodesToSuppressTheOffer","benefitCodes","coolOffPeriod","supportedCohorts","displayTypeByKey", "skipProductsOnAccountToSuppress","complianceRank","billingProductGroup").toArray(String[]::new));

					}

					if (!featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
						baseOffersResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseOffersResponse,
								CTOfferResponse.class, Stream.of("delayProvisioningReason", "delayProvisioning", "delayProvisioningMessagesByKey").toArray(String[]::new));
					}

				}

				//remove CompatibleProducts for TAZ/TAZBYOD/TAZCONTRACT/RR
				if (Optional.ofNullable(offersRequest.getContractIndicator()).isPresent() && (offersRequest
						.getContractIndicator().stream().allMatch(Predicate.isEqual(Constants.TAZ_STRING))
						|| offersRequest.getContractIndicator().stream()
								.allMatch(Predicate.isEqual(Constants.TAZBYOD_STRING))
						|| offersRequest.getContractIndicator().stream()
						.allMatch(Predicate.isEqual(Constants.ROAD_RUNNER))
						|| offersRequest.getContractIndicator().stream()
						.allMatch(Predicate.isEqual(Constants.GENRE))
						|| offersRequest.getContractIndicator().stream()
								.allMatch(Predicate.isEqual(Constants.TAZCONTRACT_STRING)))) {
					if (Optional.ofNullable(offersRequest.getSalesChannel()).isPresent()
							&& offersRequest.getSalesChannel().stream().noneMatch(ch -> ch.equals(Constants.OPUS) || ch.equals(Constants.DTV360) || ch.equals(Constants.UVC))) {
						baseOffersResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(baseOffersResponse,
								CTOfferResponse.class, Stream.of("compatibleProducts").toArray(String[]::new));
					} else {
						if (Optional.ofNullable(offersRequest.getOfferActionType()).isPresent() && (offersRequest
								.getOfferActionType().stream().noneMatch(Predicate.isEqual(Constants.ACQUISITION))
						&& offersRequest
								.getOfferActionType().stream().noneMatch(Predicate.isEqual(Constants.UPSELL_ACTION_TYPE))
								&& offersRequest
								.getOfferActionType().stream().noneMatch(Predicate.isEqual(Constants.CLOSING_ACTION_TYPE))
						)) {
							baseOffersResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(
									baseOffersResponse, CTOfferResponse.class,
									Stream.of("compatibleProducts").toArray(String[]::new));
						}
					}
				}
		}

		if (featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_B_ENABLED)
			&& Optional.ofNullable(offersRequest.getOfferProductFamily()).isPresent()
			&& offersRequest.getOfferProductFamily().contains(Constants.SATELLITE_PRODUCT_FAMILY)
			&& Optional.ofNullable(offersRequest.getOfferTypes()).isPresent()
	        && (StringUtils.equalsIgnoreCase(Constants.COUPON, offersRequest.getOfferTypes().get(0)))) {
			// call minimum purchase amount with decimal for UCC flow
			updateMinimumPurchaseAmount("0.00", baseOffersResponse);
		} else {
			// call minimum purchase amount with out decimal for non-UCC flow
			updateMinimumPurchaseAmount("0", baseOffersResponse);
		}
		//Removing attributes added for middlewareFiltering
		baseOffersResponse=JsonFilterService.filterAttributesAndGetObjectFromJson(baseOffersResponse, CTOfferResponse.class, Stream.of(
				"ineligibleAccountStatus","opusGlobalSubChannels","opusGlobalChannels;","directIntegrationPartnerName",
				"dealerCode","dealerIds","masterDealerId","salesSubChannel","locationId","locationTypeId",
				"qualifyingProductIds","qualifyingProductsBillingProductCode","oem-long-description-rokutv","oem-long-description-firetv",
				"oem-short-description-rokutv","oem-short-description-firetv","predicateTag","availableQuota","creditRiskEligibility","partnerType",
				"qualifyingProductsPlanSubTypes","bundleProductIds","retentionOfferUser", "skipProductsOnAccountToSuppress","globalMessagesByKey","delayProvisioningReasons","conflictingProductsReference",
				"attributePricingCriteria","conflictingPriceTiers").toArray(String[]::new));

		return baseOffersResponse;

	}

    /**
	 * @param format
     * @param offerResponse
	 * @return
	 */
	private void updateMinimumPurchaseAmount(String format, CTOfferResponse offerResponse) {
		log.info("Format : [{}]",format);
		NumberFormat formatter = new DecimalFormat(format);
		if (offerResponse != null && Optional.ofNullable(offerResponse.getOffers()).isPresent()
				&& offerResponse.getOffers().size() > 0) {
			offerResponse.getOffers().forEach(offer -> {
				if (Optional.ofNullable(offer).isPresent() &&
						Optional.ofNullable(offer.getAttributes()).isPresent()
						&& Optional.ofNullable(offer.getAttributes().getEligibility()).isPresent()
						&& Optional.ofNullable(offer.getAttributes().getEligibility().getConstraints()).isPresent()) {
					offer.getAttributes().getEligibility().getConstraints().forEach(constraints ->{
						double minimumPurchaseAmount = 0;
						if (constraints.getMinimumPurchaseAmount() != null) {
							minimumPurchaseAmount = constraints.getMinimumPurchaseAmount().doubleValue();
						}
						String formmatedMinimumPurchaseAmount = formatter.format(minimumPurchaseAmount);
						BigDecimal minPurchaseAmount = new BigDecimal(formmatedMinimumPurchaseAmount);
						constraints.setMinimumPurchaseAmount(minPurchaseAmount);
						// log.info("MinimumPurchaseAmount set : [{}]",constraints.getMinimumPurchaseAmount());
					});
				}
			});
		}
	}
	
	private boolean isNotActiveAccount(OfferRequest offersRequest) {
        return Boolean.FALSE.equals(offersRequest.getCustomerContext().getSatellite().getIsActive());
    }
}
