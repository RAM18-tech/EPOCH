package com.dtv.dcp.epoch.processor.ott.sales;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.services.CustomerSubscriptionDetail;
import com.dtv.dcp.epoch.util.OffersUtils;



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
		if(!offersUtils.isEDSPOnlyRequest(offerRequestWrapper.getOfferRequest())) {
			offerResponse =ottCTOffersProcessor.getFeeOffers(offerRequestWrapper);
				if(Objects.nonNull(offerResponse)) {
					dtvnSalesRSNFeeOffersProcessor.filterRSNFeeOffers(offerRequestWrapper, offerResponse.getOffers());
				}
			}
		return offerResponse;
	}
}
