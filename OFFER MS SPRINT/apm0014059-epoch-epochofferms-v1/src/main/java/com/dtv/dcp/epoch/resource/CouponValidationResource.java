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

import com.dtv.dcp.epoch.model.common.request.CouponValidationRequest;
import com.dtv.dcp.epoch.model.common.response.CouponValidationResponse;
import com.dtv.dcp.epoch.representation.Content;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Validate Coupon Offers")
@RequestMapping("/epochofferms")
@Produces({ MediaType.APPLICATION_JSON })
@RestController
public interface CouponValidationResource {

	@POST
	@RequestMapping("/validateCouponOffers")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the coupon Result ", notes = "Validate the coupon and send the result senario . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = " Coupon validated Successfully"),
			@ApiResponse(code = 404, message = "No request coupon Found"),
			@ApiResponse(code = 500, message = "Error Occured While validating the coupon") })
	public ResponseEntity couponValidation(@RequestHeader HttpHeaders headers, 
			@NotNull @RequestBody CouponValidationRequest couponValidationRequest);

}