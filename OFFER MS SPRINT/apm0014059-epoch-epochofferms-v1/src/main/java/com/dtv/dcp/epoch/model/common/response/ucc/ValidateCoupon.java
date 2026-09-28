package com.dtv.dcp.epoch.model.common.response.ucc;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateCoupon implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String code;
	private String name;
	private String description;
	private String status;
	private ValidationResults validationResults;
	private List<Offer> offers;
	@JsonIgnore
	private List<String> offersForCouponsRequested;

	public List<String> getOffersForCouponsRequested() {
		return offersForCouponsRequested;
	}

	public void setOffersForCouponsRequested(List<String> offersForCouponsRequested) {
		this.offersForCouponsRequested = offersForCouponsRequested;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ValidationResults getValidationResults() {
		return validationResults;
	}

	public void setValidationResults(ValidationResults validationResults) {
		this.validationResults = validationResults;
	}

	public List<Offer> getOffers() {
		return offers;
	}

	public void setOffers(List<Offer> offers) {
		this.offers = offers;
	}

	@Override
	public String toString() {
		return "Coupon [id=" + id + ", code=" + code + ", name=" + name + ", description=" + description + ", status="
				+ status + ", validationResults=" + validationResults + ", offers=" + offers + "]";
	}

}