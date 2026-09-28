package com.dtv.dcp.epoch.service.ott;

import org.springframework.http.HttpHeaders;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;

/**
 * The Interface IntegratedOffersService for IO Offers.
 */
public interface BenefitsService {
	

	CTBenefitsResponse getBenefits(HttpHeaders headers, BenefitRequest benefitRequest) throws ServiceException;

}
