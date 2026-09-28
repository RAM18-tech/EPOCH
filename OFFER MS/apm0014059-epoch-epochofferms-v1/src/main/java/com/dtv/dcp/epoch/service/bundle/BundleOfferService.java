package com.dtv.dcp.epoch.service.bundle;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;

public interface BundleOfferService {

    public CTOfferResponse getBundleOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException;
}
