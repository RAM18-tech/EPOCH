/**
 * 
 */
package com.dtv.dcp.epoch.processor.satellite.sales;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.IneligibleOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.processor.ott.sales.SalesEquipmentProcessor;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author ap778g
 *
 */
@Component
public class SatelliteSalesOffersProcessor {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	CpopClient cpopClient;

	@Autowired
	SatelliteSalesVideoPlanProcessor salesVideoPlanProcessor;

	@Autowired
	SalesVideoAddonProcessor salesVideoAddonProcessor;
	
	@Autowired
	SatelliteSalesVideoDeviceProcessor salesVideoDeviceProcessor;
	
	@Autowired
	SatelliteSalesInsuranceProcessor salesInsuranceProcessor;

	@Autowired
	SalesEquipmentProcessor salesEquipmentProcessor;

	@Autowired
	OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;
	
	@Autowired
	SatelliteSalesOffersProcessorHelper satelliteSalesOffersProcessorHelper;
	
	@Autowired
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;
	
	@Autowired
	SatelliteSalesFeeProcessor satelliteSalesFeeProcessor;
	
	@Autowired
	RedisCacheHelper redisCacheHelper;
	
	@Autowired
    private FeatureManagerHelper featureHelper;

	/** The log. */
	private static final Logger logger = LoggerFactory.getLogger(SatelliteSalesOffersProcessor.class);

	public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {
		logger.info("SatelliteSalesOffersProcessor.getOffersForAcquisition::::Start");
		
		CTOfferResponse videoPlanOffers = null;
		CTOfferResponse videoAddonOffer = null;
		CTOfferResponse videoDeviceOffers = null;
		CTOfferResponse insuranceOffers = null;
		CTOfferResponse feeOffers = null;
		CTOfferResponse rewardCardOffers = null;
		CTOfferResponse closingOffers = null;

		List<CTOffer> finalOfferList = new ArrayList<>();
		List<IneligibleOffer> ineligibleOfferList = new ArrayList<>();
		CTOfferResponse finalOfferResponse = new CTOfferResponse();	
		
		List<CTOffer> ctSelectedOffer = null;
		
		filterOffersForEligibleDealerCode(offerRequestWrapper);
		filterOffersForBlockedDealers(offerRequestWrapper);
		
		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductType()).isPresent()
				&& ((offerRequestWrapper.getOfferRequest().getOfferProductType()).contains(Constants.VIDEO_PLAN))) {
			videoPlanOffers = salesVideoPlanProcessor.getSatelliteVideoPlanOffers(offerRequestWrapper);
		}
		
		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductType()).isPresent()
				&& (offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_ADDON)
				|| offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_DEVICE)
				|| offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.REWARD)
				|| offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.CREDIT)
				|| offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.FEE)
				|| offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.INSURANCE))) {
			CTOfferResponse cTOfferResponse = satelliteCTOffersProcessor.getOfferByIds(offerRequestWrapper,false,false);
			if(Optional.ofNullable(cTOfferResponse).isPresent()) {
				ctSelectedOffer = cTOfferResponse.getOffers();
			}
		}
		
		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductType()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_ADDON)) {
			videoAddonOffer = salesVideoAddonProcessor.getSatelliteVideoAddonOffersForAcquisition(offerRequestWrapper, ctSelectedOffer);
		}
		
		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductType()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.INSURANCE)) {
			insuranceOffers = salesInsuranceProcessor.getSatelliteInsuranceOffers(offerRequestWrapper, ctSelectedOffer);
		}
		
		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductType()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.FEE)) {
			feeOffers = satelliteSalesFeeProcessor.getSatelliteFeeOffers(offerRequestWrapper,ctSelectedOffer,new HashMap<>());
		}

		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductType()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_DEVICE)) {
			videoDeviceOffers = salesVideoDeviceProcessor.getSatelliteVideoDeviceOffers(offerRequestWrapper, ctSelectedOffer);
			//CTOfferResponse allOffers = objectMapper.convertValue(videoDeviceOffers, CTOfferResponse.class);
			CTOfferResponse allOffers = null;
	        if (featureHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)) {
	        	allOffers = objectMapper.convertValue(videoDeviceOffers, CTOfferResponse.class);
	        }else {
	        	allOffers =  SerializationUtils.clone(videoDeviceOffers);
	        }
	        
			if(offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.SALES_CRM)
					|| offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.CCAP)
					|| offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DPP)
					|| offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DIRECT_INTEGRATION_PARTNER)
					|| offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.ASSISTED_SALES)) {
				videoDeviceOffers.setOffers(satelliteCTOffersProcessor.getHardwareInstantRebateOffers(offerRequestWrapper, videoDeviceOffers.getOffers(), ctSelectedOffer));
			} else {
				videoDeviceOffers.setOffers(satelliteCTOffersProcessor.associateFeeWithDevice(videoDeviceOffers.getOffers(), feeOffers, ctSelectedOffer));
			}
			
			satelliteCTOffersProcessor.applyOrderModChanges(videoDeviceOffers, offerRequestWrapper, allOffers, Constants.VIDEO_DEVICE);
		}
		
		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductType()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.REWARD)) {
			rewardCardOffers = satelliteCTOffersProcessor.getSatelliteRewardCardOffers(offerRequestWrapper,ctSelectedOffer);
		}
		
		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductType()).isPresent()
				&& offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.CREDIT)) {
			closingOffers = satelliteCTOffersProcessor.getSatelliteClosingOffers(offerRequestWrapper,ctSelectedOffer);
		}
		
		if (videoPlanOffers != null && CollectionUtils.isNotEmpty(videoPlanOffers.getOffers())) {
			finalOfferList.addAll(videoPlanOffers.getOffers());
		}
		if (videoAddonOffer != null && CollectionUtils.isNotEmpty(videoAddonOffer.getOffers())) {
				finalOfferList.addAll(videoAddonOffer.getOffers());
		}
		if (videoDeviceOffers != null && CollectionUtils.isNotEmpty(videoDeviceOffers.getOffers())) {
				finalOfferList.addAll(videoDeviceOffers.getOffers());
		}
		if (insuranceOffers != null && CollectionUtils.isNotEmpty(insuranceOffers.getOffers())) {
			finalOfferList.addAll(insuranceOffers.getOffers());
		}
		if (feeOffers != null && CollectionUtils.isNotEmpty(feeOffers.getOffers())) {
				finalOfferList.addAll(feeOffers.getOffers());
		}
		if (rewardCardOffers != null && CollectionUtils.isNotEmpty(rewardCardOffers.getOffers())) {
			finalOfferList.addAll(rewardCardOffers.getOffers());
		}
		if (closingOffers != null && CollectionUtils.isNotEmpty(closingOffers.getOffers())) {
			finalOfferList.addAll(closingOffers.getOffers());
		}
	   
		if (!finalOfferList.isEmpty()) {
			satelliteCTOffersProcessor.calculateBestPrice(finalOfferList, offerRequestWrapper, true);
			satelliteCTOffersProcessor.calculateDiscountAmount(finalOfferList, offerRequestWrapper);
			finalOfferResponse.setOffers(finalOfferList);
			finalOfferResponse.setCount(finalOfferList.size());
			finalOfferResponse.setTotal(finalOfferList.size());
		}
		
		if(videoAddonOffer != null && CollectionUtils.isNotEmpty(videoAddonOffer.getIncludedProducts())) {
			finalOfferResponse.setIncludedProducts(videoAddonOffer.getIncludedProducts());
		}
		
		if (videoPlanOffers != null && CollectionUtils.isNotEmpty(videoPlanOffers.getIneligibleOffers())) {
			ineligibleOfferList.addAll(videoPlanOffers.getIneligibleOffers());
		}
		if (videoAddonOffer != null && CollectionUtils.isNotEmpty(videoAddonOffer.getIneligibleOffers())) {
			ineligibleOfferList.addAll(videoAddonOffer.getIneligibleOffers());
		}
		if (videoDeviceOffers != null && CollectionUtils.isNotEmpty(videoDeviceOffers.getIneligibleOffers())) {
			ineligibleOfferList.addAll(videoDeviceOffers.getIneligibleOffers());
		}
		if (insuranceOffers != null && CollectionUtils.isNotEmpty(insuranceOffers.getIneligibleOffers())) {
			ineligibleOfferList.addAll(insuranceOffers.getIneligibleOffers());
		}
		if(!ineligibleOfferList.isEmpty()) {
			finalOfferResponse.setIneligibleOffers(ineligibleOfferList);
		}
		logger.info("SatelliteSalesOffersProcessor.getOffersForAcquisition::::Ends");

		return finalOfferResponse;
	}
	
private void filterOffersForEligibleDealerCode(OfferRequestWrapper offerRequestWrapper) {
		
		if(offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DIRECT_INTEGRATION_PARTNER)
				&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility())
				&& Constants.INDIRECT_SALES_CHANNEL.equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getChannelEligibility().getSalesChannel())
				&& Constants.MDU_DTH.equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getChannelEligibility().getSalesSubChannel())) {
			
			List<String> dealerCodes = redisCacheHelper.getValues(Constants.MDUDTH_PILOT_DEALERCODES,
					offerRequestWrapper.getOfferRequest().getOfferProductFamily().get(0));

			if(StringUtils.isEmpty(offerRequestWrapper.getOfferRequest().getChannelEligibility().getDealerCode()) ||
					(CollectionUtils.isNotEmpty(dealerCodes) 
							&& !dealerCodes.contains(offerRequestWrapper.getOfferRequest().getChannelEligibility().getDealerCode()))) {
				throw new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_DEALERCODE);
			}
		}
	}
	
	private void filterOffersForBlockedDealers(OfferRequestWrapper offerRequestWrapper) {
		
		if(offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DIRECT_INTEGRATION_PARTNER)
				&& Objects.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility())) {
		
			List<String> salesChannels = redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESCHANNEL,
					offerRequestWrapper.getOfferRequest().getOfferProductFamily().get(0));
			
			List<String> salesSubChannels = redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_SALESSUBCHANNEL,
					offerRequestWrapper.getOfferRequest().getOfferProductFamily().get(0));
			
			List<String> dealerCodes = redisCacheHelper.getValues(Constants.EXCLUSION_DIRECTINTEGRATIONPARTNER_DEALERCODE,
					offerRequestWrapper.getOfferRequest().getOfferProductFamily().get(0));
			
			if(CollectionUtils.isEmpty(salesChannels) 
					|| (CollectionUtils.isNotEmpty(salesChannels) 
							&& !salesChannels.contains(offerRequestWrapper.getOfferRequest().getChannelEligibility().getSalesChannel()))) {
				if(CollectionUtils.isEmpty(salesSubChannels)
						|| (CollectionUtils.isNotEmpty(salesSubChannels) 
						    && !salesSubChannels.contains(offerRequestWrapper.getOfferRequest().getChannelEligibility().getSalesSubChannel()))) {
					if(CollectionUtils.isNotEmpty(dealerCodes) 
							&& StringUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getChannelEligibility().getDealerCode())
							&& dealerCodes.contains(offerRequestWrapper.getOfferRequest().getChannelEligibility().getDealerCode())) {
						throw new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_BLOCKED_DEALER);
					}
				}else {
					throw new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_BLOCKED_DEALER);
				}
			}else {
				throw new ServiceException(ErrorMessages.EPOCH_GETOFFERS_INVALID_REQUEST_BLOCKED_DEALER);
			}
		}
	}
	
}

