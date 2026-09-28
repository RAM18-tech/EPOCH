package com.dtv.dcp.epoch.resource;

import java.util.Optional;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.representation.Content;
import com.dtv.dcp.epoch.service.ott.BenefitsService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonFilterService;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;

/**
 * The Class OffersResourceImpl.
 *
 * Created by nk3077 on 07/30/2019.
 */
@Controller
public class BenefitsResourceImpl implements BenefitsResource {

	/** The log. */
	private static Logger log = LoggerFactory.getLogger(BenefitsResourceImpl.class);

	/** The Constant for ERROR_MESSAGE. */
	private static final String ERROR_MESSAGE = "Unknown error ocuured in BenefitsResourceImpl.getResults() method.";

	/** The Constant SOURCE. */
	private static final String SOURCE = "OffersResourceImpl";

	private static final String ENDPOINT = "API_NAME:EPOCH_GETBENEFITS";

	@Autowired
	private BenefitsService benefitsService;

	@Override
	public CTBenefitsResponse getBenefits(HttpHeaders headers,  @NotNull BenefitRequest benefitRequest) {
		MDC.put(Constants.TRACE_ID, null != headers ? headers.getFirst(Constants.TRACE_ID):"");
		Content<CTBenefitsResponse> content = null;
		CTBenefitsResponse benefitsResponse = null;
		long startTimeInMillies = System.currentTimeMillis();
		log.info("{} CLIENT_REQUEST:[{}] TRACE_ID:[{}]", ENDPOINT, JsonService.getJsonFromObject(benefitRequest),
				headers.getFirst(Constants.TRACE_ID));
		try {
			FeatureManagerHelper.httpHeaders = headers;
			if (Optional.ofNullable(benefitRequest).isPresent()
					&& (Optional.ofNullable(benefitRequest.getBenefitIds()).isPresent()
							|| Optional.ofNullable(benefitRequest.getBenefitCodes()).isPresent()
							|| Optional.ofNullable(benefitRequest.getAgreements()).isPresent())) {
				benefitsResponse = benefitsService.getBenefits(headers, benefitRequest);

				// Add attributes here which are not needed by FE to consume.
				benefitsResponse = JsonFilterService.filterAttributesAndGetObjectFromJson(benefitsResponse,
						CTBenefitsResponse.class, Stream.of("parentOffers").toArray(String[]::new));
				log.debug("{} ATTRIBUTE EXCLUSION FOR BENEFIT RESPONSE:{}", ENDPOINT,
						OffersUtils.sanitizeData(benefitRequest.getProductFamily()));

			} else {
				// TODO Check valid scenario
				throw ((new ServiceException(ErrorMessages.CPOP_INVALID_REQUEST_BENEFIT_ERROR_ON_GETBENEFITS,
						"Invalid Request")).addDetail(ErrorMessages.CPOP_INVALID_REQUEST_BENEFIT_ERROR_ON_GETBENEFITS,
								SOURCE + ".getResults()"));
			}
			log.info("EPOCH_GETBENEFITS_SUCCESS");
			content = new Content<>(benefitsResponse);

		} catch (ServiceException serviceException) {
			log.error(SOURCE + "getBenefits() :  ServiceException= ", serviceException);
			log.info("EPOCH_GETBENEFITS_FAILED");
			log.error("{} STATUS:FAILED EPOCH_GETBENEFITS_FAILED-[{}]", ENDPOINT, serviceException.getError());
			throw serviceException;
		} catch (Exception ex) {
			log.error(ERROR_MESSAGE, ex);
			log.error("{} STATUS:FAILED EPOCH_GETBENEFITS_FAILED-[{}]", ENDPOINT, ex.getMessage());
			throw ((new ServiceException(ErrorMessages.CPOP_BENEFIT_ERROR_ON_GETBENEFITS_10001, ex))
					.addDetail(ErrorMessages.CPOP_BENEFIT_ERROR_ON_GETBENEFITS_10002, SOURCE + ".getBenefits()"));
		}
		log.debug("End of BenefitsResourceImpl.getBenefits() method..");
		log.info("{} EPOCH_GETBENEFITS_EXECUTION_TIME-[{}]", ENDPOINT,
				(System.currentTimeMillis() - startTimeInMillies));
		return benefitsResponse;
	}
}
