package com.dtv.dcp.epoch.service.watchtv;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.watchtv.WatchTVCTOffersProcessor;
import com.dtv.dcp.epoch.processor.watchtv.sales.WatchTVSalesOffersProcessor;
import com.dtv.dcp.epoch.processor.watchtv.services.WatchTVServicesOffersProcessor;

/**
 * The Class BaseOffersServiceImpl.
 */
@Component
public class WatchTVOffersServiceImpl implements WatchTVOffersService {

    @Autowired
    WatchTVSalesOffersProcessor watchTvSalesOffersProcessor;

    @Autowired
    WatchTVServicesOffersProcessor watchTvServicesOffersProcessor;

    @Autowired
    WatchTVCTOffersProcessor watchTvCTOffersProcessor;

    /**
     * The log.
     */
    private static Logger log = LoggerFactory.getLogger(WatchTVOffersServiceImpl.class);

    @Override
    public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {
        log.debug("Start of OttOffersServiceImpl.getOffers() method..");

        OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();


        CTOfferResponse offersResponse = null;
        /**
         * On the bases of the getOfferActionType this class will call the OttSalesOffersProcessor or OttServicesOffersProcessor
         *   "Acquisition","Retention","Other","Upgrade","Downgrade","Cross-sell"
         *
         */
        if (Optional.ofNullable(offerRequest).isPresent()){

            if(Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()) {

                List<String> offerActionType = offerRequest.getOfferActionType();

                if (offerActionType.contains(Constants.ACQUISITION_ACTION_TYPE)) {

                    offersResponse = watchTvSalesOffersProcessor.getOffers(offerRequestWrapper);

                } else { // services case
                    offersResponse = watchTvServicesOffersProcessor.getOffers(offerRequestWrapper);

                }

            }else {
                /** To GET Offers based on offerId, offerCode , bundleProducts**/
                offersResponse = watchTvCTOffersProcessor.getOfferByIds(offerRequestWrapper);
                watchTvSalesOffersProcessor.processOffersFetchedBySearchIds(offersResponse, offerRequestWrapper);

            }

        }


        log.debug("End of OttOffersServiceImpl.getOffers() method..");
        return offersResponse;

    }
}