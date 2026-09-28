/**
 *
 */
package com.dtv.dcp.epoch.service.satellite;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;

/**
 * @author ap778g
 *
 */

/**
 * The Interface IntegratedOffersService for IO Offers.
 */
public interface SatelliteOffersService {


	/**
	 *
	 * @param headers
	 * @param offerRequestWrapper
	 * @return
	 * @throws ServiceException
	 */
    public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException;


}
