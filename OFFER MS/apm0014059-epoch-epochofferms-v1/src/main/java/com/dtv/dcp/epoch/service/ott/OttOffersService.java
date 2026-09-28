package com.dtv.dcp.epoch.service.ott;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;

/**
 * The Interface IntegratedOffersService for IO Offers.
 */
public interface OttOffersService {

    /**
     *
     * @param offerRequestWrapper
     * @return
     * @throws ServiceException
     */
    CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException;

}
