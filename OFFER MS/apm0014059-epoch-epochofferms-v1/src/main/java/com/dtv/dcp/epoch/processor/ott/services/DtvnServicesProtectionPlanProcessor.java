/**
 * 
 */
package com.dtv.dcp.epoch.processor.ott.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;

/**
 * @author nf2008
 *
 */
@Component
public class DtvnServicesProtectionPlanProcessor {

	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;
	
	/**
	 * retrieveProtectionPlanOffers
	 * @param offerRequestWrapper
	 * @return
	 */
	public List<CTOffer> retrieveProtectionPlanOffers(OfferRequestWrapper offerRequestWrapper) {
		CTOfferResponse offerResponse = new CTOfferResponse();
		offerResponse = ottCTOffersProcessor.getProtectionPlanOffers(offerRequestWrapper);
		return offerResponse.getOffers();
	}
}
