package com.dtv.dcp.epoch.service;

import javax.validation.constraints.NotNull;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.CouponValidationRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;

public interface CouponValidationService {

    public CTOfferResponse getBundleOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException;

    public CTProductResponse getProducts(ProductRequestWrapper productRequestWrapper) throws ServiceException;

	public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException;
	
	public OfferRequestWrapper generateOfferRequest(String campaignCode) throws ServiceException;

	public Boolean ValidateCampaignEligibility(CTOfferResponse offerResponse,
			@NotNull CouponValidationRequest couponValidationRequest) throws ServiceException;


}
