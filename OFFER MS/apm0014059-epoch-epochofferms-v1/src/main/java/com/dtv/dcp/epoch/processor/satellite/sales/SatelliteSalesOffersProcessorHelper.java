package com.dtv.dcp.epoch.processor.satellite.sales;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.util.OffersUtils;

/**
 * @author ap778g
 *
 */
@Component
public class SatelliteSalesOffersProcessorHelper {

	@Autowired
	OffersUtils offersUtils;



	/**
	 * Process the rules on CT offers.
	 *
	 * @param offerList
	 */
	

	public List<CTOffer> retentionDowngradeSaveOfferProcessing(List<CTOffer> ctFinalOfferList,
			OfferRequestWrapper offerRequestWrapper) {		
		List<CTOffer> offerResponse = new ArrayList<>();
		offerResponse = ctFinalOfferList;
		return !offerResponse.isEmpty() ? offerResponse : null;

	}
}
