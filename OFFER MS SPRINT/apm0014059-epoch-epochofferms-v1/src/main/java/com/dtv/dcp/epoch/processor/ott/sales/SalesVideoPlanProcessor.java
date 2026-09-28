package com.dtv.dcp.epoch.processor.ott.sales;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;

@Component
public class SalesVideoPlanProcessor {

	@Autowired
	OttSalesMobilityProcessorHelper ottSalesMobilityProcessorHelper; 
	


	public CTOfferResponse getVideoPlanOffers(OfferRequestWrapper offerRequestWrapper) {
		
		return ottSalesMobilityProcessorHelper.getFilteredOffers(offerRequestWrapper, Constants.BASE);
		
	}
}
