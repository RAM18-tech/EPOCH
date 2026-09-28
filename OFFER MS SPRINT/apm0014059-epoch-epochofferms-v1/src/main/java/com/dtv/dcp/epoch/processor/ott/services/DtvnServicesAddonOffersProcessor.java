package com.dtv.dcp.epoch.processor.ott.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.ProductPromotionData;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class DtvnServicesAddonOffersProcessor {

	@Autowired
	OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;
	
	@Autowired
	OffersUtils offersUtils;
	
	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;
	
	Map<String, ProductPromotionData> productPromotions= new HashMap<>();

	public List<CTOffer> retrieveAvailableAddOns(OfferRequestWrapper offerRequestWrapper) {
	
		CTOfferResponse offerResponseAddOn = ottCTOffersProcessor.getVideoAddonOffers(offerRequestWrapper);
		filterActiveOffers(offerResponseAddOn);
		return offerResponseAddOn.getOffers();
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