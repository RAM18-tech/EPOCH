/**
 * 
 */
package com.dtv.dcp.epoch.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.AdditionalDetails;
import com.dtv.dcp.epoch.model.common.ExistingProductFamily;
import com.dtv.dcp.epoch.model.common.VolPauseDetails;
import com.dtv.dcp.epoch.model.common.request.CheckEligibilityRequest;
import com.dtv.dcp.epoch.model.common.response.CheckEligibiltyResponse;
import com.dtv.dcp.epoch.model.common.response.PauseEligibilityDetails;

/**
 * @author nf2008
 *
 */
@Component
public class CheckEligibilityUtils {
	
	private static final Logger log = LoggerFactory.getLogger(CheckEligibilityUtils.class);
	
	@Autowired
	RedisCacheHelper redisCacheHelper;
	
	@Value("${checkeligibilty.pauseeligiblitymonths}")
	private int pauseEligiblityMonths;
	
	@Value("${checkeligibilty.pausemonthsifnopause}")
	private String pauseMonthsIfNoPause;
	
	@Value("${checkeligibilty.maxpausemonths}")
	private int maxPauseMonthsfromConfig;
	
	@Value("${checkeligibilty.docheckforadditionalinfo}")
	private boolean checkforAdditionalInfo;
	
	@Value("#{'${checkeligibilty.volpausevalidstatus}'.split(',')}")
	private List<String> volPauseValidStatus;
	
	@Value("#{'${checkeligibilty.validcustomersegment}'.split(',')}")
	private List<String> validCustomerSegment;
	
	@Value("#{'${checkeligibilty.validcontractindicator}'.split(',')}")
	private List<String> validContractIndicator;
	
	@Value("#{'${checkeligibilty.providernotallowed}'.split(',')}")
	private List<String> providerNotAllowed;
	
	@Value("#{'${checkeligibilty.iapplatformnotallowed}'.split(',')}")
	private List<String> iapPlatformNotAllowed;


	public CheckEligibiltyResponse checkEligibiltyConditions(CheckEligibilityRequest checkEligibilityRequest) {
		log.info("Start of CheckEligibilityUtils.checkEligibiltyConditions");
		CheckEligibiltyResponse checkEligibiltyResponse = null;
		PauseEligibilityDetails pauseEligibilityDetails = null;
		Integer tenure = null;
		Integer maxPauseMonths = null;
		boolean doCheckforAdditionalInfo = false;
		List<String> providerNotAllow;
		List<String> customerSeg;
		List<String> contractInd;
		try {

			Map<String, List<String>> epochGloablConfigMap = loadConfigs();
			
			if (Objects.nonNull(checkEligibilityRequest.getCustomerContext())
					&& Optional.ofNullable(checkEligibilityRequest.getCustomerContext().getExistingProductFamily())
							.isPresent()) {
				
				customerSeg = epochGloablConfigMap.get(Constants.CUSTOMERSEGMENT) != null
						? Arrays.asList(epochGloablConfigMap.get(Constants.CUSTOMERSEGMENT).get(0).split("\\s*,\\s*"))
						: validCustomerSegment;
				contractInd = epochGloablConfigMap.get(Constants.CONTRACTINDICATOR) != null
						? Arrays.asList(epochGloablConfigMap.get(Constants.CONTRACTINDICATOR).get(0).split("\\s*,\\s*"))
						: validContractIndicator;
				log.info("Eligible CustomerSegment :{} and ContractIndicator : {}", customerSeg, contractInd);
				
				tenure = CollectionUtils.isNotEmpty(epochGloablConfigMap.get(Constants.ELIGIBILITYMONTHS)) && epochGloablConfigMap.get(Constants.ELIGIBILITYMONTHS).get(0) != null
						? Integer.parseInt(epochGloablConfigMap.get(Constants.ELIGIBILITYMONTHS).get(0))
						: pauseEligiblityMonths;
				log.debug("Tenure config value : {}", tenure);

				maxPauseMonths = CollectionUtils.isNotEmpty(epochGloablConfigMap.get(Constants.MAXPAUSEMONTHS))
						&& epochGloablConfigMap.get(Constants.MAXPAUSEMONTHS).get(0) != null
								? Integer.parseInt(epochGloablConfigMap.get(Constants.MAXPAUSEMONTHS).get(0))
								: maxPauseMonthsfromConfig;
				log.debug("maxPauseMonths value : {}", maxPauseMonths);

				doCheckforAdditionalInfo = CollectionUtils.isNotEmpty(epochGloablConfigMap.get(Constants.ADDITIONALINFOCHECK))
						&& epochGloablConfigMap.get(Constants.ADDITIONALINFOCHECK).get(0) != null
								? Boolean.parseBoolean(epochGloablConfigMap.get(Constants.ADDITIONALINFOCHECK).get(0))
								: checkforAdditionalInfo;
								
				providerNotAllow = epochGloablConfigMap.get(Constants.PROVIDERNOTALLOWED) != null
						? Arrays.asList(epochGloablConfigMap.get(Constants.PROVIDERNOTALLOWED).get(0).split("\\s*,\\s*"))
						: providerNotAllowed;

				ExistingProductFamily existingProductFamily = checkEligibilityRequest.getCustomerContext()
						.getExistingProductFamily().get(0);

				if (StringUtils.isNotEmpty(checkEligibilityRequest.getCustomerSegments())
						&& customerSeg.contains(checkEligibilityRequest.getCustomerSegments())
						&& Optional.ofNullable(checkEligibilityRequest.getContractIndicator()).isPresent()
						&& !checkEligibilityRequest.getContractIndicator().isEmpty()
						&& contractInd.contains(checkEligibilityRequest.getContractIndicator().get(0))
						&& (checkCustomerTenure(tenure, existingProductFamily.getParentSubscriptionDate())
								|| checkEligibilityRequest.getContractIndicator().get(0).equalsIgnoreCase(Constants.NONCONTRACT))
						&& StringUtils.isEmpty(existingProductFamily.getGracePeriodEndDate())
						&& !(existingProductFamily.isHasActiveInstallments() && !providerNotAllow.isEmpty()
						&& existingProductFamily.getInstallmentProvider().stream().filter(Objects::nonNull)
										.anyMatch(p -> providerNotAllow.contains(p)))
						&& !(checkForAdditionalInfo(doCheckforAdditionalInfo, existingProductFamily.getAdditionalDetails(), true))
						&& Objects.isNull(existingProductFamily.getVolPauseDetails())
						&& validateNextBillingDate(existingProductFamily)) {

					log.info("Tenure Matched , No GracePeriod .. Other Condition Passed");
					pauseEligibilityDetails = new PauseEligibilityDetails();
					pauseEligibilityDetails.setEligibleForServicePause(true);
					pauseEligibilityDetails.setPausePeriod(Constants.NOOFMONTHS);
					pauseEligibilityDetails	.setPauseIntervals(new ArrayList<>(Arrays.asList(pauseMonthsIfNoPause.split(","))));
					log.info("Pause never taken before");
				} else if (StringUtils.isEmpty(existingProductFamily.getGracePeriodEndDate())
						&& Optional.ofNullable(existingProductFamily.getVolPauseDetails()).isPresent()
						&& !(checkForAdditionalInfo(doCheckforAdditionalInfo, existingProductFamily.getAdditionalDetails(), false))
						) {

						log.info("Pause taken before");
						VolPauseDetails volPauseDetails = existingProductFamily.getVolPauseDetails().get(0);
						Integer pauseMonthTaken = checkForNoOfMonthPauseTaken(volPauseDetails.getStartDate(),
								volPauseDetails.getEndDate());
						pauseEligibilityDetails = new PauseEligibilityDetails();
						if (pauseMonthTaken >= maxPauseMonths) {
							pauseEligibilityDetails.setEligibleForServicePause(false);
							pauseEligibilityDetails.setEligibleToExtendPause(false);
							log.info("Pause exhausted");
						} else {
							pauseEligibilityDetails.setEligibleToExtendPause(true);
							pauseEligibilityDetails.setPausePeriod(Constants.NOOFMONTHS);
							pauseEligibilityDetails.setPauseIntervals(new ArrayList<>());
							Integer difference = maxPauseMonths - pauseMonthTaken;
							for (Integer i = 1; i <= difference; i++) {
								pauseEligibilityDetails.getPauseIntervals().add(i.toString());
							}
							log.info("Remaining Pause : {}", difference);

						}
					}
			}
		} catch (Exception e) {
			log.error("Exception in CheckEligibilityUtils : {}", e);
			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
					.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
		}

		if (Objects.nonNull(pauseEligibilityDetails)) {
			checkEligibiltyResponse = new CheckEligibiltyResponse();
			checkEligibiltyResponse.setPauseEligibilityDetails(pauseEligibilityDetails);
		} else {
			checkEligibiltyResponse = new CheckEligibiltyResponse();
			checkEligibiltyResponse.setPauseEligibilityDetails(new PauseEligibilityDetails());
			checkEligibiltyResponse.getPauseEligibilityDetails().setEligibleForServicePause(false);
		}

		return checkEligibiltyResponse;

	}
	
	private boolean validateNextBillingDate( ExistingProductFamily existingProductFamily) {
		if(Objects.nonNull(existingProductFamily) && StringUtils.isNotEmpty(existingProductFamily.getNextBillingDate())) {
          	//Adding plus 1 day to next billing date to block one day pause. ref US- BYODOPC-736
			LocalDate nextBillingDate = LocalDate.parse(existingProductFamily.getNextBillingDate(),DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_MM_DD_YYYY_SLASH)).plusDays(1);
			LocalDate localCurrentDate = LocalDate.parse(LocalDate.now().toString(),DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYY_MM_DD));
			if(nextBillingDate.getDayOfMonth()==localCurrentDate.getDayOfMonth()) {
				return false;
			}
		}
		return true;
	}
	
	private int checkForNoOfMonthPauseTaken(String startDate, String endDate) {
		LocalDate localStartDate = LocalDate.parse(startDate,DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_MM_DD_YYYY_SLASH));
		LocalDate localEndDate = LocalDate.parse(endDate,DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_MM_DD_YYYY_SLASH));
		//Adding plus one day as the pause will start on bill cycle date and end day before bill cycle date
		LocalDate localEndDatePlusOne = localEndDate.plusDays(1);
		return Math.toIntExact(ChronoUnit.MONTHS.between(localStartDate, localEndDatePlusOne));
	
	}
	
	
	
	private boolean checkCustomerTenure(Integer tenure, String parentSubscriptionDate) {
		int totalNumberOfMonths = 0;
		try {
			LocalDate localParentSubscriptionDate = LocalDate.parse(parentSubscriptionDate,DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_MM_DD_YYYY_SLASH));
			LocalDate localCurrentDate = LocalDate.parse(LocalDate.now().toString(),DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_YYYY_MM_DD));
			totalNumberOfMonths = Math.toIntExact(ChronoUnit.MONTHS.between(localParentSubscriptionDate, localCurrentDate));
			if(totalNumberOfMonths >= tenure) {
				return true;
			}
		} catch (Exception e) {
			log.error("Exception in checkCustomerTenure : {}",e);
		}
		return false;
	}
	
	private boolean checkForAdditionalInfo(boolean doCheckforAdditionalInfo, List<AdditionalDetails> additionalDetails, boolean iapInfoCheck) {
		iapPlatformNotAllowed = redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_IAP_PLATFORM_NOTALLOWED, Constants.OTT) != null
				? Arrays.asList(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_IAP_PLATFORM_NOTALLOWED, Constants.OTT).get(0).split("\\s*,\\s*"))
				: iapPlatformNotAllowed;
		if (doCheckforAdditionalInfo && CollectionUtils.isNotEmpty(additionalDetails)) {
			for (AdditionalDetails details : additionalDetails) {
				if ((details.getAdditionalInfoName().equalsIgnoreCase(Constants.SERVICE_SUSPENDED_INFO)
						|| details.getAdditionalInfoName().equalsIgnoreCase(Constants.SERVICE_SUSPENDED_INFO_UPPERCASE))
						&& CollectionUtils.isNotEmpty(details.getParams())
						&& (details.getParams().stream().filter(Objects::nonNull)
								.anyMatch(d -> d.getParamValue().equalsIgnoreCase(Constants.SUSPEND))
								|| details.getParams().stream().filter(Objects::nonNull)
								.anyMatch(d -> d.getParamValue().equalsIgnoreCase(Constants.PENDING_SUSPEND)))) {
					return true;
				}
				
				if (iapInfoCheck && (details.getAdditionalInfoName().equalsIgnoreCase(Constants.IAP_INFO) ||
						details.getAdditionalInfoName().equalsIgnoreCase(Constants.IAPINFO))
						&& CollectionUtils.isNotEmpty(details.getParams())
						&& details.getParams().stream().filter(Objects::nonNull)
								.anyMatch(d -> iapPlatformNotAllowed.contains(d.getParamValue().toUpperCase()))) {
					return true;
				}
			}

		}
		return false;

	}
	
	public void validateCheckEligibiltyRequest(CheckEligibilityRequest checkEligibilityRequest) {
		List<String> contractIndicator = redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT) != null
				? Arrays.asList(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT).get(0).split("\\s*,\\s*"))
				: validContractIndicator;

		List<String> validSalesChannels = redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_SALES_CHANNELS, Constants.OTT) != null
				? redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_SALES_CHANNELS, Constants.OTT)
				: Arrays.asList("opus", "directvOnline", "oemIAPFIRETV", "oemIAPROKUTV", "osprey");

		if (!Optional.ofNullable(checkEligibilityRequest.getOfferActionType()).isPresent()) {
			throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST, "OfferActionType");
		}

		if (StringUtils.isEmpty(checkEligibilityRequest.getSalesChannel())) {
			throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST, "SalesChannel");
		}else if(!validSalesChannels.contains(checkEligibilityRequest.getSalesChannel())) {
			throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST_DETAILS, "SalesChannel");
		}
		

		if (!Optional.ofNullable(checkEligibilityRequest.getOfferProductFamily()).isPresent()) {
			throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST, "OfferProductFamily");
		}

		if (StringUtils.isEmpty(checkEligibilityRequest.getCustomerSegments())) {
			throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST, "CustomerSegments");
		}
		
		//BYODOPC-4820 change made to check customerSubscriptionType if present in request skip contractIndicator
		//setting customerSubscriptionType value to contractIndicator
		if (!StringUtils.isEmpty(checkEligibilityRequest.getCustomerSubscriptionType())) {
			checkEligibilityRequest.setContractIndicator(List.of(checkEligibilityRequest.getCustomerSubscriptionType()));
		}
		
		if (!Optional.ofNullable(checkEligibilityRequest.getContractIndicator()).isPresent()) {
			throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST, "ContractIndicator");
		}

		if (Objects.isNull(checkEligibilityRequest.getCustomerContext())) {
			throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST);
		}

		if (!Optional.ofNullable(checkEligibilityRequest.getCustomerContext().getExistingProductFamily()).isPresent()) {
			throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST);
		} else {
			for (ExistingProductFamily existingProductFamily : checkEligibilityRequest.getCustomerContext()
					.getExistingProductFamily()) {

				if (existingProductFamily.isHasActiveInstallments()
						&& !Optional.ofNullable(existingProductFamily.getInstallmentProvider()).isPresent()) {
					throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST,
							"Installment Provider");
				}

				if (Optional.ofNullable(existingProductFamily.getVolPauseDetails()).isPresent()) {
					for (VolPauseDetails volPauseDetails : existingProductFamily.getVolPauseDetails()) {
						if (StringUtils.isEmpty(volPauseDetails.getStatus())) {
							throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST,
									"Pause Status");
						}else if(!volPauseValidStatus.contains(volPauseDetails.getStatus())) {
							throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST_DETAILS,
									"Pause Status");
						}
						if (StringUtils.isEmpty(volPauseDetails.getStartDate())) {
							throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST,
									"Pause startDate");
						} else if (!Util.isValidDateFormat(volPauseDetails.getStartDate(),
								Constants.DATE_FORMAT_MM_DD_YYYY_SLASH)) {
							throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST_DETAILS,
									"Pause StartDate");
						}
						if (StringUtils.isEmpty(volPauseDetails.getEndDate())) {
							throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST,
									"Pause endDate");
						} else if (!Util.isValidDateFormat(volPauseDetails.getEndDate(),
								Constants.DATE_FORMAT_MM_DD_YYYY_SLASH)) {
							throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST_DETAILS,
									"Pause EndDate");
						}
					}
				} else if ((CollectionUtils.isNotEmpty(existingProductFamily.getAdditionalDetails())
						&& checkForAdditionalInfo(true, existingProductFamily.getAdditionalDetails(), false))
						|| (StringUtils.isEmpty(existingProductFamily.getParentSubscriptionDate())
								&& checkEligibilityRequest.getContractIndicator().contains(Constants.NONCONTRACT))
						|| !contractIndicator.contains(checkEligibilityRequest.getContractIndicator().get(0))) {
					// do nothing as suspended account will not be having parentSubsriptionDate and VolPauseDetails
					// do nothing if parentSubsriptionDate is empty for non-contract customers
					// do nothing if contractIndicator from global config does not match with request
				} else if (StringUtils.isEmpty(existingProductFamily.getParentSubscriptionDate())) {
					throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST,
							"ParentSubscriptionDate");
				} else if (!Util.isValidDateFormat(existingProductFamily.getParentSubscriptionDate(),
						Constants.DATE_FORMAT_MM_DD_YYYY_SLASH)) {
					throw new ServiceException(ErrorMessages.CTLG_CHECKELIGIBILITY_INVALID_REQUEST_DETAILS,
							"Invalid ParentSubscriptionDate");
				}
			}
		}

	}

	public Map<String, List<String>> loadConfigs(){
		Map<String, List<String>> config= new HashMap<>();
		config.put(Constants.ELIGIBILITYMONTHS,  redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_ELIGIBLITY_CHECK_MONTHS, Constants.OTT));
		config.put(Constants.MAXPAUSEMONTHS,  redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MAX_PAUSE_MONTHS, Constants.OTT));
		config.put(Constants.ADDITIONALINFOCHECK, redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_ADDI_INFO_CHECK, Constants.OTT));
		config.put(Constants.CUSTOMERSEGMENT, redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CUST_SEG, Constants.OTT));
		config.put(Constants.CONTRACTINDICATOR, redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_CONTRACT_INDICATOR, Constants.OTT));
		config.put(Constants.PROVIDERNOTALLOWED, redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CHECK_ELIGIBILITY_NOT_ALLOWED_PROVIDER, Constants.OTT));
		return config;
		
	}
}
