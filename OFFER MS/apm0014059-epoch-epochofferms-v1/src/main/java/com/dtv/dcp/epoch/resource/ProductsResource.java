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

import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.representation.Content;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The Interface BaseOffersResource.
 *
 * Created by ac2201 on 08/08/2019.
 */

@Api("Products")
@RequestMapping("/epochofferms")
@Produces({ MediaType.APPLICATION_JSON })
@RestController
public interface ProductsResource {

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
	@RequestMapping("/getProducts")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns Base Products details", notes = "Retrieves EPOCH Product details as requested", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "EPOCH Products retrieved Successfully"),
			@ApiResponse(code = 404, message = "No Products Found"),
			@ApiResponse(code = 500, message = "Error Occurred While Retrieving EPOCH Product for the offerType") })
	ResponseEntity getProducts(@RequestHeader HttpHeaders headers, @NotNull @RequestBody ProductRequest productsRequest);

}