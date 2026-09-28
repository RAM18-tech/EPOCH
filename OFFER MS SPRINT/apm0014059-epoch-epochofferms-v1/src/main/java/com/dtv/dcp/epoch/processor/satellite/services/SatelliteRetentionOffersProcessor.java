/**
 * 
 */
package com.dtv.dcp.epoch.processor.satellite.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.processor.ott.sales.SalesEquipmentProcessor;
import com.dtv.dcp.epoch.processor.ott.sales.SalesVideoPlanProcessor;
import com.dtv.dcp.epoch.processor.satellite.sales.SatelliteSalesOffersProcessor;
import com.dtv.dcp.epoch.processor.satellite.sales.SatelliteSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.processor.satellite.sales.SatelliteSalesVideoPlanProcessor;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;

/**
 * @author ap778g
 *
 */
@Component
public class SatelliteRetentionOffersProcessor {

	@Autowired
	CpopClient cpopClient;

	@Autowired
	SalesVideoPlanProcessor salesVideoPlanProcessor;

	@Autowired
	SalesVideoAddonProcessor salesVideoAddonProcessor;

	@Autowired
	SalesEquipmentProcessor salesEquipmentProcessor;

	@Autowired
	OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;
	
	@Autowired
	SatelliteSalesOffersProcessorHelper satelliteSalesOffersProcessorHelper;
	
	@Autowired
	SatelliteSalesVideoPlanProcessor satelliteSalesVideoPlanProcessor;

	/** The log. */
	private static final Logger logger = LoggerFactory.getLogger(SatelliteRetentionOffersProcessor.class);
	
	

	public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {
		logger.info("SatelliteRetentionOffersProcessor.getOffers::::Start");
		
	
		CTOfferResponse videoAddonOffer = null;
		CTOfferResponse videoPlanOffers = null;

		List<CTOffer> finalOfferList = new ArrayList<>();
		CTOfferResponse finalOfferResponse = new CTOfferResponse();		
		
		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& ((offerRequestWrapper.getOfferRequest().getOfferProductType()).contains(Constants.VIDEO_ADDON))) {
			videoAddonOffer = salesVideoAddonProcessor.getSatelliteVideoAddonOffers(offerRequestWrapper);
		}

		if (Optional.ofNullable(offerRequestWrapper).isPresent()
				&& Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
				&& ((offerRequestWrapper.getOfferRequest().getOfferProductType()).contains(Constants.VIDEO_PLAN))) {
			videoPlanOffers = satelliteSalesVideoPlanProcessor.getSatelliteVideoPlanOffersForRetention(offerRequestWrapper);
		}
		
	   if (videoAddonOffer != null && CollectionUtils.isNotEmpty(videoAddonOffer.getOffers())) {
			finalOfferList.addAll(videoAddonOffer.getOffers());
		}
		
	   	if (videoPlanOffers != null && CollectionUtils.isNotEmpty(videoPlanOffers.getOffers())) {
			finalOfferList.addAll(videoPlanOffers.getOffers());
		}
	
		if (!finalOfferList.isEmpty()) {
			finalOfferList = satelliteSalesOffersProcessorHelper.retentionDowngradeSaveOfferProcessing(finalOfferList,
					offerRequestWrapper);
			finalOfferResponse.setOffers(finalOfferList);
			finalOfferResponse.setLimit(300);
			finalOfferResponse.setTotal(finalOfferList.size());
			finalOfferResponse.setCount(finalOfferList.size());
		}
		logger.info("SatelliteRetentionOffersProcessor.getOffers::::Ends");

		return finalOfferResponse;
	}
	

}

