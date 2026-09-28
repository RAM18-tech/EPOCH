package com.dtv.dcp.epoch.service.ott;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.BenefitRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.processor.ott.services.OttServicesBenefitsProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesBenefitsProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

/**
 * The Class BaseOffersServiceImpl.
 */
@Component
public class BenefitsServiceImpl implements BenefitsService {

	@Autowired
	OttServicesBenefitsProcessor ottServicesBenefitsProcessor;

	@Autowired
	SatelliteServicesBenefitsProcessor satelliteServicesBenefitsProcessor;
	
	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	
	/** The log. */
	private static Logger log = LoggerFactory.getLogger(BenefitsServiceImpl.class);

	@Override
	public CTBenefitsResponse getBenefits(HttpHeaders headers, BenefitRequest benefitRequest) throws ServiceException {
		log.debug("Start of OttOffersServiceImpl.getResults() method..");
		BenefitRequestWrapper benefitRequestWrapper = new BenefitRequestWrapper();
		benefitRequestWrapper.setLoggedInId(headers.getFirst(Constants.IDPCTX_LOGGEDINID));
		benefitRequestWrapper.setAuthAccountsWireless(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_AUTHORIZEDACCOUNTS_WIRELESS)));
		benefitRequestWrapper.setDtvnAccount(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_DTVN_ACCOUNT)));
		benefitRequestWrapper.setSessionId(headers.getFirst(Constants.IDPCTX_SESSION_ID));
		benefitRequestWrapper.setLinkedUverseAccountNums(OffersUtils.getFirstAccount(headers.getFirst(Constants.IDPCTX_LINKEDUVERSEACCNUMS)));
		if(!featureManagerHelper.isEnabled(Constants.FEATURE_FTC_SALES_CHANNEL)) {
			benefitRequest.setSalesChannel(null);
		}
		benefitRequestWrapper.setBenefitRequest(benefitRequest);

		CTBenefitsResponse benefitsResponse = null;
		if (Optional.ofNullable(benefitRequest).isPresent()
				&& ( Optional.ofNullable(benefitRequest.getBenefitIds()).isPresent() ||
						Optional.ofNullable(benefitRequest.getBenefitCodes()).isPresent()
						|| Optional.ofNullable(benefitRequest.getAgreements()).isPresent()
				) ) {
			if(Optional.ofNullable(benefitRequestWrapper.getBenefitRequest().getProductFamily()).isPresent()
					&& benefitRequestWrapper.getBenefitRequest().getProductFamily().contains(Constants.SATELLITE_PRODUCT_FAMILY)) {
				log.debug("Satellite getBenefits flow");
				benefitsResponse = satelliteServicesBenefitsProcessor.getBenefits(benefitRequestWrapper);
			} else {
				benefitsResponse = ottServicesBenefitsProcessor.getBenefits(benefitRequestWrapper);
			}

		log.debug("End of OttOffersServiceImpl.getResults() method..");
		}
		return benefitsResponse;
	
	}
}