package com.dtv.dcp.epoch.processor.satellite.sales;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;

@Component
public class SatelliteSalesVideoDeviceProcessor {

	@Autowired
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	/**
	 * getSatelliteVideoDeviceOffers.
	 *
	 * @param offerRequestWrapper 
	 */
	public CTOfferResponse getSatelliteVideoDeviceOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffer) {
		
		return satelliteCTOffersProcessor.getVideoDeviceOffers(offerRequestWrapper, ctSelectedOffer);
	}
}
