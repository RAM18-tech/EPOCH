package com.dtv.dcp.epoch.resource;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.representation.Content;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;



@Api("Benefits")
@RequestMapping("/epochofferms")
@Produces({ MediaType.APPLICATION_JSON })
@RestController
public interface BenefitsResource {

	/**
	 *
	 * @param headers
	 * @param uriInfo
	 * @param benefitRequest
	 * @return
	 */

	@POST
	@RequestMapping("/getBenefitDetails")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	@ApiOperation(value = "Returns Benefits details", notes = "Retrieves EPOCH Benefit details as requested", response = Content.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "EPOCH Benefit retrieved Successfully"),
			@ApiResponse(code = 404, message = "No Offers Found"),
			@ApiResponse(code = 500, message = "Error Occured While Retrieving EPOCH Benefits") })
	CTBenefitsResponse getBenefits(@RequestHeader HttpHeaders headers, @NotNull @RequestBody BenefitRequest benefitRequest);

}
