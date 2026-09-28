package com.dtv.dcp.epoch.processor.satellite.sales;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;

@Component
public class SatelliteSalesVideoPlanProcessor {

	@Autowired
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	/**
	 * getSatelliteVideoPlanOffers.
	 *
	 * @param offerRequestWrapper 
	 */
	public CTOfferResponse getSatelliteVideoPlanOffers(OfferRequestWrapper offerRequestWrapper) {

		return satelliteCTOffersProcessor.getVideoPlanOffers(offerRequestWrapper);
	}
	
	/**
	 * getSatelliteVideoPlanOffersForRetention.
	 *
	 * @param offerRequestWrapper 
	 */
	public CTOfferResponse getSatelliteVideoPlanOffersForRetention(OfferRequestWrapper offerRequestWrapper) {

		return satelliteCTOffersProcessor.getVideoPlanOffersForRetention(offerRequestWrapper);
	}
}
