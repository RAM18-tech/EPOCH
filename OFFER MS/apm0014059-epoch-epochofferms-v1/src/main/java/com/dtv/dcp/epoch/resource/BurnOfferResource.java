package com.dtv.dcp.epoch.resource;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequestWrapper;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnResponse;

import io.swagger.annotations.Api;

@Api("Burn Reward Offer")
@RequestMapping("/epochofferms")
@RestController
public interface BurnOfferResource {

    @POST
    @RequestMapping("/burn")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    OfferBurnResponse burnReward(@RequestHeader HttpHeaders headers, @NotNull OfferBurnRequestWrapper wrapper);
}
