package com.dtv.dcp.epoch.processor.bundle;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPAdditionalOfferHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

import org.springframework.web.util.HtmlUtils;

@Component
public class BundleOfferProcessor {

    /**
     * The CpopClient.
     */
    @Autowired
	CpopClientHelper cpopClientHelper;
    
    @Autowired
    CPOPAdditionalOfferHelper cpopAdditionalOfferHelper;

  
    
	@Autowired
	private FeatureManagerHelper featureManagerHelper;


    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(BundleOfferProcessor.class);
    private static final String PRIMARY="primary";
    private static final String BACKUP="backup";

    /** The eligibilities processor. */
	@Autowired
	private BundleShoppingCartProcessor dtvNowRewardCouponProcessor;

	public CTOfferResponse getBundleOffers(OfferRequestWrapper offerRequestWrapper) {
		CTOfferResponse ctOfferResponse = null;
		Map<String, Boolean> quotaBaseBenefitCache = null;
		CTOfferRequest ctOfferRequest = null;
		if(Objects.nonNull(offerRequestWrapper.getCtOfferRequest()))
		{
			ctOfferRequest = offerRequestWrapper.getCtOfferRequest();
		}else{
			ctOfferRequest = new CTOfferRequest();
		}

		// Bundle Product start ....
		ctOfferRequest.setOfferProductFamily(offerRequestWrapper.getOfferRequest().getOfferProductFamily());
		ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
		log.info("API_NAME:EPOCH_GETOFFERS OFFERACTIONTYPE:{}",OffersUtils.sanitizeData(ctOfferRequest.getOfferActionType()));
		ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
		ctOfferRequest.setAccountTypes(offerRequestWrapper.getOfferRequest().getBusinessSegment());
		ctOfferRequest.setOfferProductType(offerRequestWrapper.getOfferRequest().getOfferProductType());
		ctOfferRequest.setOfferType(offerRequestWrapper.getOfferRequest().getOfferTypes());
		ctOfferRequest.setOfferStatus(offerRequestWrapper.getOfferRequest().getOfferStatus());
		ctOfferRequest.setCustomerSegments(offerRequestWrapper.isMobility() ? Stream.of("Mobility").collect(Collectors.toList()) :
						(Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerSegments()).isPresent() ? 
						offerRequestWrapper.getOfferRequest().getCustomerSegments() : Stream.of("Residential").collect(Collectors.toList())));
		 log.info("API_NAME:EPOCH_GETOFFERS CUSTOMERSEGMENT:{}",OffersUtils.sanitizeData(ctOfferRequest.getCustomerSegments()));
	   
		// Bundle Product end ....
		ctOfferResponse = getOffersFromCT(ctOfferRequest);
		// Store quotabase offers card in cache
		Map<String, Boolean> quotaBaseBenefits = cpopAdditionalOfferHelper.createQuotaBaseBenefitMap(ctOfferResponse);
		if (Optional.ofNullable(quotaBaseBenefits).isPresent() && !quotaBaseBenefits.isEmpty()) {
			quotaBaseBenefitCache = cpopAdditionalOfferHelper
					.getQuotaBaseOfferMapFromCache(offerRequestWrapper.getSessionId(),featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)?BACKUP:PRIMARY);
			if (Optional.ofNullable(quotaBaseBenefitCache).isPresent() && !quotaBaseBenefitCache.isEmpty()) {
				Map<String, Boolean> benefitsMap = quotaBaseBenefitCache;
				quotaBaseBenefits.forEach((key, value) -> 
					benefitsMap.put(key, value)
				);
				cpopAdditionalOfferHelper.putQuotaBaseOfferMapInCache(offerRequestWrapper.getSessionId(), benefitsMap,featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)?BACKUP:PRIMARY);
			} else {
				cpopAdditionalOfferHelper.putQuotaBaseOfferMapInCache(offerRequestWrapper.getSessionId(),
						quotaBaseBenefits,featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)?BACKUP:PRIMARY);
			}
		}
		return ctOfferResponse;
	}








    
    public OfferValidationResponse offerValidation(OfferValidationRequest offerValidationRequest) {
		log.debug("Start of OfferValidationProcessor.offerValidation() ...");
		OfferValidationResponse offerValidationResponse = new OfferValidationResponse();
		return dtvNowRewardCouponProcessor.validateShoppingCart(offerValidationRequest, offerValidationResponse);
	}

    /**
     * @param ctOfferRequest
     * @return
     * @throws ServiceException
     */
	private CTOfferResponse getOffersFromCT(CTOfferRequest ctOfferRequest) {
		CTOfferResponse offerResponse = null;

		offerResponse = cpopClientHelper.getOffers(ctOfferRequest);
		if (offerResponse == null) {
			log.info("In Method getOffersFromCT.OttCTOffersProcessor() :: Getting the null response from the CTMS");
			throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
		}
		log.info("In Method getOffersFromCT.OttCTOffersProcessor() :: Getting response from the CTMS");
		return offerResponse;

	}



	
}
