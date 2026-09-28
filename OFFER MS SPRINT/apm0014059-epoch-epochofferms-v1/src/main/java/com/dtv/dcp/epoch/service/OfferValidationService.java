package com.dtv.dcp.epoch.service;

import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;

/**
 * The Interface OfferValidationService.
 *  * @author nk3077
 */
public interface OfferValidationService {
	
	/**
	 * Offer Validation .
	 *
	 * @param offerValidationRequest
	 * @return OfferValidationResponse - the final response of bundle status
	 */
	public  OfferValidationResponse offerValidation(OfferValidationRequest offerValidationRequest, String sessionId) throws ClientException ;
}