package com.dtv.dcp.epoch.resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.ServiceMetaData;
import com.dtv.dcp.epoch.model.common.request.CheckEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.CheckLocalChannelEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.response.CheckEligibilityLocalResponse;
import com.dtv.dcp.epoch.model.common.response.CheckEligibiltyResponse;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.representation.Content;
import com.dtv.dcp.epoch.representation.Error;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.OfferValidationService;
import com.dtv.dcp.epoch.util.CheckEligibilityUtils;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;

/**
 * @author nk3077
 */

/**
 * This Class has the information of the end point related  to validate the offer..
 *
 */

@Controller
public class OfferValidationResourceImpl  implements OfferValidationResource{

	/** The log. */
	private static final Logger log = LoggerFactory.getLogger(OfferValidationResourceImpl.class);
	
	/** reference of service class. */
	@Autowired
    private OfferValidationService offerValidationService;
	
	 @Autowired
	 private DMALookUpService dmaLookUpService;
	 
	 @Autowired
	 private CheckEligibilityUtils checkEligiblityUtils;
	
	/**
	 * This API is to validate the bundle offer , takes the cart as request and send the only result back like bundle is
	 * valid or not , any missing items in the cart etc on base of bundle..
	 *
	 * @param headers , information of the header - session id , BAN etc 
	 * @param uriInfo , information about the url
	 * @param offerValidationRequest , this is actually a cart
	 * @return response - bundle offer result with meta-info
	 */
	
	@Override
	public ResponseEntity offerValidation(@RequestHeader HttpHeaders headers, 
			@NotNull @RequestBody OfferValidationRequest offerValidationRequest) {
		MDC.put(Constants.TRACE_ID, null != headers ? headers.getFirst(Constants.TRACE_ID):"");
		log.debug("Start of OfferValidationResourceImpl.offerValidation() ...");
		log.info("checkOffersEligibility headers {} ",headers);
		OfferValidationResponse offerValidationResponse = null;
		Content<OfferValidationResponse> finalResponse = null;
		Link link = null;

		long startTime = System.currentTimeMillis();
		try {
			FeatureManagerHelper.httpHeaders = headers;
			String sessionId = headers.getFirst(Constants.IDPCTX_SESSION_ID);
			offerValidationResponse = offerValidationService.offerValidation(offerValidationRequest, sessionId);
			finalResponse = new Content<>(offerValidationResponse);
			// Removed the below Code , @TODO
//			link = Link.fromUri(uriInfo.getAbsolutePath().toString()).rel(Constants.SELF)
//					.type(Constants.APPLICATION_JSON).build();
//			finalResponse.metaData(retrieveMetaInfo(uriInfo, startTime));

		} catch (ServiceException serviceEx) {
			Error error =new Error(serviceEx.getError());
			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceEx.getHttpCode()));
			//throw serviceEx;
		} catch (Exception ex) {
			log.error("Unknow error ocuured in OfferValidationResourceImpl.offerValidation()  method.", ex);
			ServiceException serviceException =	new ServiceException(ErrorMessages.CTLG_WIRELESS_UNHANDLED_EXCEPTION, ex);
			Error error =new Error(serviceException.getError());
			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));
		}

		log.debug("End of OfferValidationResourceImpl.offerValidation() ...");
		return ResponseEntity.ok(offerValidationResponse);
	}

	
    /**
     * REST provider to update the meta info .
     *
     * @param startTime .
     * @param uriInfo          .
     * @return ServiceMetaData.
     */
	private ServiceMetaData retrieveMetaInfo( UriInfo uriInfo, long startTime) {
		log.debug("Start ofOffer ValidationResourceImpl.retrieveMetaInfo() ...");
		ServiceMetaData metaInfo = new ServiceMetaData();
		metaInfo.title(Constants.CART_VALIDATION_URI + uriInfo.getAbsolutePath().toString());
		metaInfo.description(Constants.CART_VALIDATION);
		metaInfo.timestamp(new StringBuilder().append(LocalDateTime.now()).toString());
		metaInfo.version(Constants.V2);
		metaInfo.processingTime(System.currentTimeMillis() - startTime);
		log.debug("End of OfferValidationResourceImpl.retrieveMetaInfo() ...");
		return metaInfo;
	}


	@Override
	public CheckEligibilityLocalResponse checkLocalChannelEligibility(HttpHeaders headers, 
			@NotNull CheckLocalChannelEligibilityRequest checkLocalChannelEligibilityRequest) {
		MDC.put(Constants.TRACE_ID, null != headers ? headers.getFirst(Constants.TRACE_ID):"");
		FeatureManagerHelper.httpHeaders = headers;
		List<String> zipCode = null != checkLocalChannelEligibilityRequest.getCustomerEligibility()?checkLocalChannelEligibilityRequest.getCustomerEligibility().getZipCode() : null;
		List<String> county = null != checkLocalChannelEligibilityRequest.getCustomerEligibility()?checkLocalChannelEligibilityRequest.getCustomerEligibility().getCounty() : null;
		CheckEligibilityLocalResponse checkEligibilityLocalResponse = new CheckEligibilityLocalResponse();
		if(Objects.nonNull(zipCode) && !zipCode.isEmpty() 
    			&& Objects.nonNull(county) && !county.isEmpty()) {
    		Boolean hasLocalChannels =  dmaLookUpService.hasLocalChannels(zipCode.get(0),county.get(0));
    		Boolean isLCCTrialMarketDMA =  dmaLookUpService.isLCCTrialMarketDMA(zipCode.get(0),county.get(0));
    		if(null != hasLocalChannels) {
    			checkEligibilityLocalResponse.setHasLocalChannels(hasLocalChannels);
    		} else {
    			checkEligibilityLocalResponse.setHasLocalChannels(false);
    		}
    		if(null != isLCCTrialMarketDMA) {
    			checkEligibilityLocalResponse.setIsLCCTrialMarketDMA(isLCCTrialMarketDMA);
    		} else {
    			checkEligibilityLocalResponse.setIsLCCTrialMarketDMA(false);
    		}
    	}
		log.debug("End of checkLocalChannelEligibility ...");
		return checkEligibilityLocalResponse;
	}

	@Override
	public ResponseEntity checkEligibility(@NotNull CheckEligibilityRequest checkEligibilityRequest) {
		log.debug("Start of OfferValidationResourceImpl.checkEligibility() ...");

		Content<CheckEligibiltyResponse> content = null;
		CheckEligibiltyResponse checkEligibiltyResponse = null;
		try {
			checkEligiblityUtils.validateCheckEligibiltyRequest(checkEligibilityRequest);
			checkEligibiltyResponse = checkEligiblityUtils.checkEligibiltyConditions(checkEligibilityRequest);

			content = new Content<CheckEligibiltyResponse>(checkEligibiltyResponse);
		} catch (ServiceException serviceEx) {
			Error error =new Error(serviceEx.getError());
			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceEx.getHttpCode()));
			//throw serviceEx;
		} catch (Exception ex) {
			log.error("Unknow error ocuured in OfferValidationResourceImpl.checkEligibility()  method.", ex);
			/*throw ((new ServiceException(ErrorMessages.CTLG_WIRELESS_UNHANDLED_EXCEPTION, ex)).addDetail(
					ErrorMessages.CTLG_WIRELESS_CART_UNHANDLED_EXCEPTION_DETAILS, "check Eligibility",
					"Check Eligibility Controller"));
			*/
			ServiceException serviceException =	new ServiceException(ErrorMessages.CTLG_WIRELESS_UNHANDLED_EXCEPTION, ex);
			Error error =new Error(serviceException.getError());
			return new ResponseEntity<>(error,HttpStatus.valueOf(serviceException.getHttpCode()));

		}

		log.debug("End of OfferValidationResourceImpl.checkEligibility() ...");
		return ResponseEntity.ok(checkEligibiltyResponse);
	}
}