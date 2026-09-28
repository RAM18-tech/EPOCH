package com.dtv.dcp.epoch.processor.ott.sales;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class CreditProcessor
{
    @Autowired
    OttCTOffersProcessor ottCTOffersProcessor;

    @Autowired
    OffersUtils offersUtils;

    private static final Logger log = LoggerFactory.getLogger(CreditProcessor.class);

    public CTOfferResponse getClosingOffers(OfferRequestWrapper offerRequestWrapper) {

        boolean isReconnectFlag = false;
        log.info("CreditProcessor.getCreditOffers");
        CTOfferResponse offerResponse = new CTOfferResponse();

        offerResponse = ottCTOffersProcessor.getClosingOffers(offerRequestWrapper);



        return offerResponse;
    }


}
