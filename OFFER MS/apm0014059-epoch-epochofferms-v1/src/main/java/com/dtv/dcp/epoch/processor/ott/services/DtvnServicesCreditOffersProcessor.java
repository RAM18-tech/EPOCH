package com.dtv.dcp.epoch.processor.ott.services;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DtvnServicesCreditOffersProcessor {

	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;
	
	public List<CTOffer> retrieveCreditOffers(OfferRequestWrapper offerRequestWrapper) {
		CTOfferResponse offerResponse = new CTOfferResponse();
		offerResponse = ottCTOffersProcessor.getCreditOffers(offerRequestWrapper);
		return offerResponse.getOffers();
	}
}
