package com.dtv.dcp.epoch.processor.ott.services;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.BenefitRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;


@Component
public class OttServicesBenefitsProcessor {

	private static final Logger log = LoggerFactory.getLogger(OttServicesBenefitsProcessor.class);

	/** The CpopClient. */
	@Autowired
	CpopClient cpopClient;

	/** The iptvMigrationEnabled. */
	@Value("${page}")
	private int page;
	/** The pageLimit. */
	@Value("${pageLimit}")
	private int pageLimit;
	/** The state. */
	@Value("${apiclient.rest.cpopofferms.ctstate}")
	private String ctstate;

	/**
	 *
	 * @param benefitRequestWrapper
	 * @return
	 */
	public CTBenefitsResponse getBenefits(BenefitRequestWrapper benefitRequestWrapper) {

		CTBenefitsResponse benefitsResponse = null;
		CTBenefitsRequest ctBenefitsRequest;

		if ( (Optional.ofNullable(benefitRequestWrapper.getBenefitRequest().getBenefitIds()).isPresent() ||
				Optional.ofNullable(benefitRequestWrapper.getBenefitRequest().getBenefitCodes()).isPresent())
		) {
			log.debug("getBenefitIds or getBenefitCodes is Present");

			ctBenefitsRequest = new CTBenefitsRequest();
			BeanUtils.copyProperties(benefitRequestWrapper.getBenefitRequest(), ctBenefitsRequest);
			// Setting from properties file to call CT
			ctBenefitsRequest.setState(ctstate);

			// as of now is a GET request, it should be a POST when CT is ready this needs to be revisited
			long startTimeInMillis = System.currentTimeMillis();
			benefitsResponse = cpopClient.getBenefits(ctBenefitsRequest);
			log.info("EPOCH_CT_BENEFITS_EXECUTION_TIME-[{}]", (System.currentTimeMillis() - startTimeInMillis));
		}


		return benefitsResponse;
	}

}
