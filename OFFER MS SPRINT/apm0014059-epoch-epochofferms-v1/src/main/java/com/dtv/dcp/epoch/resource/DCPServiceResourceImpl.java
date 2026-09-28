package com.dtv.dcp.epoch.resource;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.dtv.dcp.epoch.model.common.request.BenefitRequest;
import com.dtv.dcp.epoch.model.common.request.CheckEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.CheckLocalChannelEligibilityRequest;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.CouponValidationRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.response.CheckEligibilityLocalResponse;
import com.dtv.dcp.epoch.model.common.response.CheckEligibiltyResponse;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequestWrapper;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnResponse;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTCheckOfferEligibilityResponse;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.representation.Content;
import com.dtv.dcp.epoch.representation.Error;

@Controller
public class DCPServiceResourceImpl implements DCPServiceResource {

	@Autowired
    private OffersResource offersResource;
	
	
	@Autowired
    private BenefitsResource benefitsResource;
	
	
	@Autowired
    private BurnOfferResource burnOfferResource;
	
	
	@Autowired
    private CouponValidationResource couponValidationResource;
	
	
	@Autowired
    private OfferValidationResource offerValidationResource;
	
	@Autowired
    private ProductsResource productsResource;
	
	@Autowired
    private ValidateCartResource validateCartResource;
	
	
	@Override
	public Content<CTBenefitsResponse> getBenefits(HttpHeaders headers, @NotNull BenefitRequest benefitRequest) {
		CTBenefitsResponse response = benefitsResource.getBenefits(headers, benefitRequest);
		return  new Content<>(response);
	}

	@Override
	public Content<OfferBurnResponse> burnReward(HttpHeaders headers, @NotNull OfferBurnRequestWrapper wrapper) {
		OfferBurnResponse response = burnOfferResource.burnReward(headers, wrapper);
		return   new Content<>(response);
	}

	@Override
	public ResponseEntity<?> couponValidation(HttpHeaders headers,
			@NotNull CouponValidationRequest couponValidationRequest) {
		ResponseEntity<?> responseEntity = couponValidationResource.couponValidation(headers, couponValidationRequest);
		Object body = responseEntity.getBody();
		if (body instanceof CTCouponResponse) {
			Content<CTCouponResponse> content = new Content<>((CTCouponResponse) body);
			return ResponseEntity.ok(content);
		}
		return responseEntity;
	}

	@Override
	public ResponseEntity<?> getOffers(@RequestHeader HttpHeaders headers, @NotNull @RequestBody OfferRequest offersRequest) {
		ResponseEntity<?> responseEntity = offersResource.getOffers(headers, offersRequest);
		Object body = responseEntity.getBody();
		if (body instanceof CTOfferResponse) {
			Content<CTOfferResponse> content = new Content<>((CTOfferResponse)body);
			return ResponseEntity.ok(content);
		}
		if (body instanceof Error && ((Error) body).getError() != null
				&& "EPOCH_DTV_SATELLITE_UNAUTHORIZED".equals(((Error) body).getError().getErrorId())) {
			return ResponseEntity.ok(body);
		}
		return responseEntity;
	}

	@Override
	public ResponseEntity<?> checkOffersEligibility(HttpHeaders headers, @NotNull OfferRequest offersRequest) {
		ResponseEntity<?> responseEntity = offersResource.checkOffersEligibility(headers, offersRequest);
		Object body = responseEntity.getBody();
		if (body instanceof CTCheckOfferEligibilityResponse) {
			Content<CTCheckOfferEligibilityResponse> content = new Content<>((CTCheckOfferEligibilityResponse) body);
			return ResponseEntity.ok(content);
		}
		return responseEntity;
	}

	@Override
	public ResponseEntity<?> offerValidation(HttpHeaders headers, @NotNull OfferValidationRequest offerValidationRequest) {
		ResponseEntity<?> responseEntity = offerValidationResource.offerValidation(headers, offerValidationRequest);
		Object body = responseEntity.getBody();
		if (body instanceof OfferValidationResponse) {
			Content<OfferValidationResponse> content = new Content<>((OfferValidationResponse) body);
			return ResponseEntity.ok(content);
		}
		return responseEntity;
	}

	@Override
	public CheckEligibilityLocalResponse checkLocalChannelEligibility(HttpHeaders headers,
			@NotNull CheckLocalChannelEligibilityRequest checkLocalChannelEligibilityRequest) {
		CheckEligibilityLocalResponse response = (CheckEligibilityLocalResponse)offerValidationResource.checkLocalChannelEligibility(headers, checkLocalChannelEligibilityRequest);
		return   response;
	}

	@Override
	public ResponseEntity<?> checkEligibility(@NotNull CheckEligibilityRequest checkEligibilityRequest) {
		ResponseEntity<?> responseEntity = offerValidationResource.checkEligibility(checkEligibilityRequest);
		Object body = responseEntity.getBody();
		if (body instanceof CheckEligibiltyResponse) {
			Content<CheckEligibiltyResponse> content = new Content<>((CheckEligibiltyResponse) body);
			return ResponseEntity.ok(content);
		}
		return responseEntity;
	}

	@Override
	public ResponseEntity<?> getProducts(HttpHeaders headers, @NotNull ProductRequest productsRequest) {
		ResponseEntity<?> responseEntity = productsResource.getProducts(headers, productsRequest);
		Object body = responseEntity.getBody();
		if (body instanceof CTProductResponse) {
			Content<CTProductResponse> content = new Content<>((CTProductResponse) body);
			return ResponseEntity.ok(content);
		}
		return responseEntity;
	}

	@Override
	public ResponseEntity<?> validateCart(HttpHeaders headers, @NotNull CouponOffersRequest couponOffersRequest) {
		return validateCartResource.validateCart(headers, couponOffersRequest);
	}

}