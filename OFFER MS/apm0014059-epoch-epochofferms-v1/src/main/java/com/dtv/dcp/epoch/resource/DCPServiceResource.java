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

import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.CheckEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.CheckLocalChannelEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.CouponValidationRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.response.CheckEligibilityLocalResponse;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequestWrapper;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnResponse;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.representation.Content;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;



@Api("DCPServiceResource")
@RequestMapping("/epochofferms")
@Produces({ MediaType.APPLICATION_JSON })
@RestController
public interface DCPServiceResource {

	/**
	 *
	 * @param headers
	 * @param uriInfo
	 * @param benefitRequest
	 * @return
	 */

	@POST
	@RequestMapping("/v2/getDtvBenefitDetails")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns Benefits details", notes = "Retrieves EPOCH Benefit details as requested", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "EPOCH Benefit retrieved Successfully"),
			@ApiResponse(code = 404, message = "No Offers Found"),
			@ApiResponse(code = 500, message = "Error Occured While Retrieving EPOCH Benefits") })
	Content<CTBenefitsResponse> getBenefits(@RequestHeader HttpHeaders headers, @NotNull @RequestBody BenefitRequest benefitRequest);
	
    @POST
    @RequestMapping("/v2/dtvburn")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    Content<OfferBurnResponse> burnReward(@RequestHeader HttpHeaders headers, @NotNull OfferBurnRequestWrapper wrapper);
    
	@POST
	@RequestMapping("/v2/validateDtvCouponOffers")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the coupon Result ", notes = "Validate the coupon and send the result senario . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = " Coupon validated Successfully"),
			@ApiResponse(code = 404, message = "No request coupon Found"),
			@ApiResponse(code = 500, message = "Error Occured While validating the coupon") })
	public ResponseEntity<?> couponValidation(@RequestHeader HttpHeaders headers, 
			@NotNull @RequestBody CouponValidationRequest couponValidationRequest);
	
	@POST
	@RequestMapping("/v2/getDtvOffers")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns Base Offers details", notes = "Retrieves EPOCH Offer details as requested", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "EPOCH Offers retrieved Successfully"),
			@ApiResponse(code = 404, message = "No Offers Found"),
			@ApiResponse(code = 500, message = "Error Occured While Retrieving EPOCH Offer for the offerType") })
	public ResponseEntity<?> getOffers(@RequestHeader HttpHeaders headers, @NotNull @RequestBody OfferRequest offersRequest);
	
	@POST
	@RequestMapping("/v2/checkDtvOffersEligibility")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Validates the Offers", notes = "Check EPOCH Offers Eligibility as requested", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Completed EPOCH Offers eligibility Successfully"),
			@ApiResponse(code = 404, message = "No Offers Found"),
			@ApiResponse(code = 500, message = "Error Occured while checking EPOCH Offers eligibility") })
	public ResponseEntity<?> checkOffersEligibility(@RequestHeader HttpHeaders headers, @NotNull @RequestBody OfferRequest offersRequest);	
	
	
	@POST
	@RequestMapping("/v2/validateDtvcart")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the Bundle offer Result ", notes = "Validate the bundle offer and send the result senario . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Bundle offer validated Successfully"),
			@ApiResponse(code = 404, message = "No request cart Found"),
			@ApiResponse(code = 500, message = "Error Occured While validating the bundle offer") })

	public ResponseEntity<?> offerValidation(@RequestHeader HttpHeaders headers,
			@NotNull @RequestBody OfferValidationRequest offerValidationRequest);
	
	
	@POST
	@RequestMapping("/v2/checkDtvLocalChannelEligibility")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the Eligibility for Local Channel ", notes = "Validate the Eligibilty for Zipcode for Local Channels . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = " Eligibilty for Zipcode for Local Channels validated Successfully"),
			@ApiResponse(code = 404, message = "No request Found"),
			@ApiResponse(code = 500, message = "Error Occured While validating the eligibilty for Zipcode for Local Channels") })

	public CheckEligibilityLocalResponse checkLocalChannelEligibility(@RequestHeader HttpHeaders headers, 
			@NotNull @RequestBody CheckLocalChannelEligibilityRequest checkLocalChannelEligibilityRequest);

	
	@POST
	@RequestMapping("/v1/checkDtvEligibility")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the Eligibility for Service Pause ", notes = "Validate the Eligibilty Check for service pause . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = " Eligibilty Check for Service Pause validated Successfully"),
			@ApiResponse(code = 404, message = "No request Found"),
			@ApiResponse(code = 500, message = "Error Occured While validating the eligibilty check for Service Pause") })

	public ResponseEntity<?> checkEligibility(@NotNull @RequestBody CheckEligibilityRequest checkEligibilityRequest);
	
	/**
	 * Retrieves EPOCH Product details as requested.
	 *
	 * @param headers
	 * @param uriInfo
	 * @param productsRequest
	 * @return
	 */
	// @FeatureToggle(feature = Constants.FEATURE_TOGGLE_PRODUCTS_ENABLED)
	@POST
	@RequestMapping("/v2/getDtvProducts")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns Base Products details", notes = "Retrieves EPOCH Product details as requested", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "EPOCH Products retrieved Successfully"),
			@ApiResponse(code = 404, message = "No Products Found"),
			@ApiResponse(code = 500, message = "Error Occurred While Retrieving EPOCH Product for the offerType") })
	 ResponseEntity<?> getProducts(@RequestHeader HttpHeaders headers, @NotNull @RequestBody ProductRequest productsRequest);
	
	@POST
	@RequestMapping("/v1/validateDtvCart")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns the valid coupon list and offers list Result ", notes = "Validate the cart and list of coupons and offers end the result senario . ", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = " Cart validated Successfully"),
			@ApiResponse(code = 404, message = "No valid coupons Found in Cart"),
			@ApiResponse(code = 500, message = "Error Occured While validating the Cart") })
	public ResponseEntity<?> validateCart(@RequestHeader HttpHeaders headers,@NotNull @RequestBody CouponOffersRequest couponOffersRequest);

}