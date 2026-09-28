package com.dtv.dcp.epoch.processor.ott.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class DtvnServicesFeeOffersProcessor {


    @Autowired
    OttCTOffersProcessor ottCTOffersProcessor;

    @Autowired
    OffersUtils offersUtils;
    
    /**
     * @param offerRequestWrapper
     * @return
     */
	public List<CTOffer> retrieveFeeOffers(OfferRequestWrapper offerRequestWrapper) {

		OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
		CTOfferResponse offerResponse = new CTOfferResponse();

		if (!offersUtils.isEDSPOnlyRequest(offerRequest)) {
			offerResponse = ottCTOffersProcessor.getFeeOffers(offerRequestWrapper);
			filterActiveOffers(offerResponse);
		}
		return offerResponse.getOffers();
	}

    /**
     * Filter active offers.
     *
     * @param offerResponse the offer response
     */
    public void filterActiveOffers(CTOfferResponse offerResponse) {
        List<CTOffer> offers = new ArrayList<>();
        if (Optional.ofNullable(offerResponse).isPresent() && Optional.ofNullable(offerResponse.getOffers()).isPresent()) {
            offerResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                if (ctOffer != null && ctOffer.getStartDate() != null && ctOffer.getEndDate() != null
                        && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(ctOffer.getStartDate()),
                        OffersUtils.getFormattedDate(ctOffer.getEndDate()))) {
                    offers.add(ctOffer);
                }
            });
            offerResponse.setOffers(offers);
        }
    }
}