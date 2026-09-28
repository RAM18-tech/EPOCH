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

import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTCheckOfferEligibilityResponse;
import com.dtv.dcp.epoch.representation.Content;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Interface BaseOffersResource.
 *
 * Created by nk3077 on 07/30/2019.
 */

@Api("Offers")
@RequestMapping("/epochofferms")
@Produces({ MediaType.APPLICATION_JSON })
@RestController
public interface OffersResource {

	/**
	 * Retrieves EPOCH Offer details as requested.
	 *
	 * @param headers
	 *            the headers
	 * @param uriInfo
	 *            the uri info
	 * @param offerId
	 *            the offer code
	 * @param mode
	 *            the mode
	 * @return the response
	 */

	@POST
	@RequestMapping("/getOffers")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns Base Offers details", notes = "Retrieves EPOCH Offer details as requested", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "EPOCH Offers retrieved Successfully"),
			@ApiResponse(code = 404, message = "No Offers Found"),
			@ApiResponse(code = 500, message = "Error Occured While Retrieving EPOCH Offer for the offerType") })
	public ResponseEntity getOffers(@RequestHeader HttpHeaders headers, @NotNull @RequestBody OfferRequest offersRequest);
	
	@POST
	@RequestMapping("/checkOffersEligibility")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Validates the Offers", notes = "Check EPOCH Offers Eligibility as requested", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Completed EPOCH Offers eligibility Successfully"),
			@ApiResponse(code = 404, message = "No Offers Found"),
			@ApiResponse(code = 500, message = "Error Occured while checking EPOCH Offers eligibility") })
	public ResponseEntity checkOffersEligibility(@RequestHeader HttpHeaders headers, @NotNull @RequestBody OfferRequest offersRequest);	
}
