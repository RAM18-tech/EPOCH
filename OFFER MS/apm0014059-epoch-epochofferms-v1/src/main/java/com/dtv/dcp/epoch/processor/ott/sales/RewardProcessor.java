package com.dtv.dcp.epoch.processor.ott.sales;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;

/**
 * The RewardProcessor class is responsible for processing reward offers.
 * It filters the marketing source codes for Direct Integration Partner (DIP) offers.
 */
@Component
public class RewardProcessor {

	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;

	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(RewardProcessor.class);

	/**
	 * Retrieves reward offers based on the provided offer request.
	 *
	 * @param offerRequestWrapper The offer request wrapper containing the offer request.
	 * @return The offer response containing the reward offers.
	 */
	public CTOfferResponse getRewardOffers(OfferRequestWrapper offerRequestWrapper) {
		log.info("RewardProcessor.getRewardOffers");
		CTOfferResponse offerResponse = new CTOfferResponse();
		offerResponse = ottCTOffersProcessor.getRewardOffers(offerRequestWrapper);
		// Filter marketingSourceCode for DIP
		filterOffersbyMSC(offerRequestWrapper, offerResponse);
		return offerResponse;
	}

	/**
	 * Filters the marketing source codes for Direct Integration Partner (DIP) offers.
	 *
	 * @param offerRequestWrapper The offer request wrapper containing the offer request.
	 * @param offersResponse The offer response containing the offers to be filtered.
	 */
	public void filterOffersbyMSC(OfferRequestWrapper offerRequestWrapper, CTOfferResponse offersResponse) {
		// Extract the offer request from the wrapper
		OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
		// Extract the marketing source codes from the offer request
		List<String> requestMarketingSourceCodes = offerRequest.getMarketingSourceCode();

		// Check if sales channels contain DIRECT_INTEGRATION_PARTNER
		boolean isDirectIntegrationPartner = offerRequest.getSalesChannel().stream()
				.anyMatch(Constants.DIRECT_INTEGRATION_PARTNER::equalsIgnoreCase);
		boolean isAssistedSales = offerRequest.getSalesChannel().stream()
				.anyMatch(Constants.ASSISTED_SALES::equalsIgnoreCase);

		if (offersResponse != null && (isDirectIntegrationPartner || isAssistedSales)) {
			offersResponse.getOffers().removeIf(offer -> removeNonMatchingOffers(offer, requestMarketingSourceCodes)
					|| removeWhenMSCMissingInRequest(offer, requestMarketingSourceCodes));
			int offerSize = offersResponse.getOffers().size();
			offersResponse.setCount(offerSize);
			offersResponse.setTotal(offerSize);
		}
	}

	/**
	 * Determines whether an offer should be removed based on the marketing source codes not matching with the request.
	 *
	 * @param offer The offer to be evaluated.
	 * @param requestMarketingSourceCodes The marketing source codes from the offer request.
	 * @return true if the offer should be removed, false otherwise.
	 */
	private boolean removeNonMatchingOffers(CTOffer offer, List<String> requestMarketingSourceCodes) {
		return Objects.nonNull(offer.getAttributes()) && Objects.nonNull(offer.getAttributes().getMarketingSrcCode())
				&& Objects.nonNull(requestMarketingSourceCodes) && offer.getAttributes().getMarketingSrcCode().stream()
						.noneMatch(requestMarketingSourceCodes::contains);
	}
	
	/**
	 * Determines whether an offer should be removed when marketing source code is missing in the request.
	 *
	 * @param offer The offer to be evaluated.
	 * @param requestMarketingSourceCodes The marketing source codes from the offer request.
	 * @return true if the offer should be removed, false otherwise.
	 */
	private boolean removeWhenMSCMissingInRequest(CTOffer offer, List<String> requestMarketingSourceCodes) {
		return Objects.nonNull(offer.getAttributes()) && Objects.nonNull(offer.getAttributes().getMarketingSrcCode())
				&& !Objects.nonNull(requestMarketingSourceCodes);
	}
}