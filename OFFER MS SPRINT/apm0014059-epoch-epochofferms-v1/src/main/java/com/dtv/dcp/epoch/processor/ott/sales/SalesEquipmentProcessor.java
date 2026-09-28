package com.dtv.dcp.epoch.processor.ott.sales;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;


@Component
public class SalesEquipmentProcessor {
   
	
	@Autowired
	OttCTOffersProcessor ottCTOffersProcessor;
	
	@Autowired
	DtvnSalesRSNFeeOffersProcessor dtvnSalesRSNFeeOffersProcessor;
	
	@Autowired
    OffersUtils offersUtils;
	
    /** The Constant log. */
    private static final Logger log = LoggerFactory.getLogger(SalesEquipmentProcessor.class);
    
	public CTOfferResponse getEquipmentOffers(OfferRequestWrapper offerRequestWrapper) {
			return ottCTOffersProcessor.getEquipmentOffers(offerRequestWrapper,null);
	}

	public CTOfferResponse getFeeOffers(OfferRequestWrapper offerRequestWrapper) {
		log.info("SalesEquipmentProcessor.getFeeOffers");
		CTOfferResponse offerResponse = new CTOfferResponse();

		// SLS-IXP-FLAG changes
		if (offersUtils.isSlsGetOfferSalesEnabled() && offersUtils.isOTTSales(offerRequestWrapper.getOfferRequest())) {
			// SLS is enabled and it's a sales flow
			if (offersUtils.isUniversalCohort(offerRequestWrapper.getOfferRequest().getContractIndicator())) {
				// Contract indicator is universal cohort
				if (StringUtils.hasText(offerRequestWrapper.getOfferRequest().getCustomerSubscriptionType())
						&& !Constants.ROAD_RUNNER.equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getCustomerSubscriptionType())) {
					// Customer subscription type is NOT null AND NOT RR - Do NOT call AMES for RSN
					if (!offersUtils.isEDSPOnlyRequest(offerRequestWrapper.getOfferRequest())) {
						offerResponse = ottCTOffersProcessor.getFeeOffers(offerRequestWrapper);
						// Note: NOT calling dtvnSalesRSNFeeOffersProcessor.filterRSNFeeOffers
					}
				} else {
					// Customer subscription type is null OR equals RR - Call AMES for RSN (BAU)
					if (!offersUtils.isEDSPOnlyRequest(offerRequestWrapper.getOfferRequest())) {
						offerResponse = ottCTOffersProcessor.getFeeOffers(offerRequestWrapper);
						if (Objects.nonNull(offerResponse)) {
							dtvnSalesRSNFeeOffersProcessor.filterRSNFeeOffers(offerRequestWrapper, offerResponse.getOffers());
						}
					}
				}
			} else {
				// Contract indicator is NOT universal cohort - BAU
				if (!offersUtils.isEDSPOnlyRequest(offerRequestWrapper.getOfferRequest())) {
					offerResponse = ottCTOffersProcessor.getFeeOffers(offerRequestWrapper);
					if (Objects.nonNull(offerResponse)) {
						dtvnSalesRSNFeeOffersProcessor.filterRSNFeeOffers(offerRequestWrapper, offerResponse.getOffers());
					}
				}
			}
		} else {
			// SLS is NOT enabled OR not a sales flow - BAU
			if (!offersUtils.isEDSPOnlyRequest(offerRequestWrapper.getOfferRequest())) {
				offerResponse = ottCTOffersProcessor.getFeeOffers(offerRequestWrapper);
				if (Objects.nonNull(offerResponse)) {
					dtvnSalesRSNFeeOffersProcessor.filterRSNFeeOffers(offerRequestWrapper, offerResponse.getOffers());
				}
			}
		}
		return offerResponse;
	}
}
