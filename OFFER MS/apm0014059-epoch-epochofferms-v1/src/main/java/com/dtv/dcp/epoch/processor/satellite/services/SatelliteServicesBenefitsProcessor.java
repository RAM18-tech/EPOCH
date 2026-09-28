package com.dtv.dcp.epoch.processor.satellite.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.Agreement;
import com.dtv.dcp.epoch.model.common.request.BenefitRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;


@Component
public class SatelliteServicesBenefitsProcessor {

	private static final Logger log = LoggerFactory.getLogger(SatelliteServicesBenefitsProcessor.class);

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
	 * @param benefitRequestWrapper the wrapper containing benefit request details
	 * @return CTBenefitsResponse containing the list of benefits and related metadata,
	 *         or null if no valid request is present
	 */
	public CTBenefitsResponse getBenefits(BenefitRequestWrapper benefitRequestWrapper) {

		CTBenefitsResponse benefitsResponse = null;
		CTBenefitsRequest ctBenefitsRequest;

		if ( (Optional.ofNullable(benefitRequestWrapper.getBenefitRequest().getBenefitIds()).isPresent() ||
				Optional.ofNullable(benefitRequestWrapper.getBenefitRequest().getBenefitCodes()).isPresent())
				|| Optional.ofNullable(benefitRequestWrapper.getBenefitRequest().getAgreements()).isPresent()) {
			log.debug("getBenefitIds or getBenefitCodes or getAgreementIds is Present");

			ctBenefitsRequest = new CTBenefitsRequest();
			BeanUtils.copyProperties(benefitRequestWrapper.getBenefitRequest(), ctBenefitsRequest);
			// Setting from properties file to call CT
			ctBenefitsRequest.setState(ctstate);

			long startTimeInMillis = System.currentTimeMillis();

			if(Optional.ofNullable(benefitRequestWrapper.getBenefitRequest().getAgreements()).isPresent()) {
				List<Agreement> agreements = benefitRequestWrapper.getBenefitRequest().getAgreements();
				List<String> agreementIds = agreements.stream().filter(Objects::nonNull)
						.map(Agreement::getAgreementId)
						.collect(Collectors.toList());
				ctBenefitsRequest.setAgreementIds(agreementIds);
				benefitsResponse = cpopClient.getSatelliteBenefits(ctBenefitsRequest);
				benefitsResponse.getBenefits().removeIf(benefit -> (!agreementPriceCodeMatching(benefit.getAgreementID(), benefit.getAgreementPriceCode(), agreements)));
				benefitsResponse.setCount(benefitsResponse.getBenefits().size());
				benefitsResponse.setTotal(benefitsResponse.getBenefits().size());
			} else {
				benefitsResponse = cpopClient.getSatelliteBenefits(ctBenefitsRequest);
			}

			log.info("EPOCH_CT_BENEFITS_EXECUTION_TIME-[{}]", (System.currentTimeMillis() - startTimeInMillis));
		}


		return benefitsResponse;
	}

	private boolean agreementPriceCodeMatching(String agreementId, String agreementPriceCode, List<Agreement> agreements) {
		if (Objects.nonNull(agreements)) {
            Optional<Agreement> matchingObject = agreements.stream().filter(Objects::nonNull).
                    filter(p -> (agreementId.equals(p.getAgreementId())) && (p.getAgreementPriceCode() == null || agreementPriceCode.equals(p.getAgreementPriceCode()))).
                    findFirst();
            if (matchingObject.isPresent()) {
                return true;
            }
        }

		return false;
	}

}
