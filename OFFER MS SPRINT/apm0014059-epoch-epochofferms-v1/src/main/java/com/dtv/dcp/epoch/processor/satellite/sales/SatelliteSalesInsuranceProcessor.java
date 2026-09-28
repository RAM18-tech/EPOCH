package com.dtv.dcp.epoch.processor.satellite.sales;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;

@Component
public class SatelliteSalesInsuranceProcessor {

	@Autowired
	SatelliteCTOffersProcessor satelliteCTOffersProcessor;

	/**
	 * getSatelliteInsuranceOffers.
	 *
	 * @param offerRequestWrapper 
	 */
	public CTOfferResponse getSatelliteInsuranceOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffers) {
		
		return satelliteCTOffersProcessor.getInsuranceOffers(offerRequestWrapper, ctSelectedOffers);
	}
}
