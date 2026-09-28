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

import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.representation.Content;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api("Coupon Cart Validate")
@RequestMapping("/epochofferms")
@Produces({ MediaType.APPLICATION_JSON })
@RestController
public interface ValidateCartResource {

	@POST
	@RequestMapping("/v1/validateCart")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the valid coupon list and offers list Result ", notes = "Validate the cart and list of coupons and offers end the result senario . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = " Cart validated Successfully"),
			@ApiResponse(code = 404, message = "No valid coupons Found in Cart"),
			@ApiResponse(code = 500, message = "Error Occured While validating the Cart") })
	public ResponseEntity validateCart(@RequestHeader HttpHeaders headers,@NotNull @RequestBody CouponOffersRequest couponOffersRequest);

}