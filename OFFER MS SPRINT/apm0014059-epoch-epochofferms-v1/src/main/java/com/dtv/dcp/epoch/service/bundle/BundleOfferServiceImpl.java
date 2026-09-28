package com.dtv.dcp.epoch.service.bundle;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.bundle.BundleOfferProcessor;

@Component
public class BundleOfferServiceImpl implements BundleOfferService {

    @Autowired
    BundleOfferProcessor bundleOfferProcessor;


    /**
     * The log.
     */
    private static Logger log = LoggerFactory.getLogger(BundleOfferServiceImpl.class);

    @Override
    public CTOfferResponse getBundleOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {
        log.debug("Start of BundleOfferServiceImpl.getBundleOffers() method..");

        OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();

        CTOfferResponse offersResponse = null;
        /**
         * VBB Offers are for Acquisition only.
         */
		if (Optional.ofNullable(offerRequest).isPresent()
				&& Optional.ofNullable(offerRequest.getOfferActionType()).isPresent()) {
			offersResponse = bundleOfferProcessor.getBundleOffers(offerRequestWrapper);
			log.debug("End of BundleOfferServiceImpl.getBundleOffers() method..");
		}
        return offersResponse;

    }
}