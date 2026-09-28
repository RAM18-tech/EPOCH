package com.dtv.dcp.epoch.resource;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dtv.dcp.epoch.model.common.request.CheckEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.CheckLocalChannelEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.response.CheckEligibilityLocalResponse;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.representation.Content;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Offers")
@RequestMapping("/epochofferms")
@Produces({ MediaType.APPLICATION_JSON })
@RestController
public interface OfferValidationResource {

	/**
	 * @author nk3077
	 */

	/**
	 * This API is to validate the bundle offer , takes the cart as request and
	 * send the only result back like bundle is valid or not , any missing items
	 * in the cart etc on base of bundle..
	 *
	 * @param headers
	 *            , information of the header - session id , BAN etc
	 * @param uriInfo
	 *            , information about the url
	 * @param offerValidationRequest
	 *            , this is actually a cart
	 * @return response - bundle offer result with meta-info
	 */

	@POST
	@RequestMapping("/validatecart")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the Bundle offer Result ", notes = "Validate the bundle offer and send the result senario . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Bundle offer validated Successfully"),
			@ApiResponse(code = 404, message = "No request cart Found"),
			@ApiResponse(code = 500, message = "Error Occured While validating the bundle offer") })

	public ResponseEntity offerValidation(@RequestHeader HttpHeaders headers,
			@NotNull @RequestBody OfferValidationRequest offerValidationRequest);
	
	
	@POST
	@RequestMapping("/checkLocalChannelEligibility")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the Eligibility for Local Channel ", notes = "Validate the Eligibilty for Zipcode for Local Channels . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = " Eligibilty for Zipcode for Local Channels validated Successfully"),
			@ApiResponse(code = 404, message = "No request Found"),
			@ApiResponse(code = 500, message = "Error Occured While validating the eligibilty for Zipcode for Local Channels") })

	public CheckEligibilityLocalResponse checkLocalChannelEligibility(@RequestHeader HttpHeaders headers, 
			@NotNull @RequestBody CheckLocalChannelEligibilityRequest checkLocalChannelEligibilityRequest);

	
	@POST
	@RequestMapping("/v1/checkEligibility")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the Eligibility for Service Pause ", notes = "Validate the Eligibilty Check for service pause . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = " Eligibilty Check for Service Pause validated Successfully"),
			@ApiResponse(code = 404, message = "No request Found"),
			@ApiResponse(code = 500, message = "Error Occured While validating the eligibilty check for Service Pause") })

	public ResponseEntity checkEligibility(@NotNull @RequestBody CheckEligibilityRequest checkEligibilityRequest);

}