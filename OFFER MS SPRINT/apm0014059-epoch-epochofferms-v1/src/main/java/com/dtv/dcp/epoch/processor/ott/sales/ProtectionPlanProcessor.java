package com.dtv.dcp.epoch.processor.ott.sales;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class ProtectionPlanProcessor {

	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;

	@Autowired
	OffersUtils offersUtils;

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(ProtectionPlanProcessor.class);

	public CTOfferResponse getProtectionPlanOffers(OfferRequestWrapper offerRequestWrapper) {
		log.info("ProtectionPlanProcessor.getProtecionPlanOffers");
		CTOfferResponse offerResponse = new CTOfferResponse();
		offerResponse = ottCTOffersProcessor.getProtectionPlanOffers(offerRequestWrapper);
		return offerResponse;
	}

}
