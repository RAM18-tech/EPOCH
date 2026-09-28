package com.dtv.dcp.epoch.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesMobilityProcessorHelper;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.processor.watchtv.WatchTVCTOffersProcessor;

@Component
public class SalesVideoAddonProcessor {

	@Autowired
	OttSalesMobilityProcessorHelper ottSalesMobilityProcessorHelper; 

	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;
	
	@Autowired
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;
	
    @Autowired
    WatchTVCTOffersProcessor watchTVCTOffersProcessor;

	/**
	 * getVideoAddonOffers.
	 *
	 * @param offerRequestWrapper 
	 */
	public CTOfferResponse getVideoAddonOffers(OfferRequestWrapper offerRequestWrapper) {

		return ottCTOffersProcessor.getVideoAddonOffers(offerRequestWrapper);
	}

	/**
	 * getVideoAddonStandalonOffers.
	 *
	 * @param offerRequestWrapper 
	 */
	public CTOfferResponse getVideoAddonStandalonOffers(OfferRequestWrapper offerRequestWrapper) {

		return ottSalesMobilityProcessorHelper.getFilteredOffers(offerRequestWrapper, Constants.STANDALONE);

	}
	
	/**
	 * getSatelliteVideoAddonOffers.
	 *
	 * @param offerRequestWrapper 
	 */
	public CTOfferResponse getSatelliteVideoAddonOffers(OfferRequestWrapper offerRequestWrapper) {

		return satelliteCTOffersProcessor.getVideoAddonOffers(offerRequestWrapper);
	}
	
	/**
	 * getSatelliteVideoAddonOffers.
	 *
	 * @param offerRequestWrapper 
	 */
	public CTOfferResponse getSatelliteVideoAddonOffersForAcquisition(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffer) {

		return satelliteCTOffersProcessor.getVideoAddonOffersForAcquisition(offerRequestWrapper, ctSelectedOffer);
	}
	
    /**
     * getWatchTVVideoAddonOffers.
     *
     * @param offerRequestWrapper 
     */
    public CTOfferResponse getWatchTVVideoAddonOffers(OfferRequestWrapper offerRequestWrapper) {
        return watchTVCTOffersProcessor.getVideoAddonOffers(offerRequestWrapper);
    }

}
