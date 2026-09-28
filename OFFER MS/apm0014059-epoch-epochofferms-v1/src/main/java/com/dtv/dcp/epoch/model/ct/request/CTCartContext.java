package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.Location;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CTCartContext implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty("salesChannel")
    private String salesChannel;
    
    @JsonProperty("validateOnlyReward")
    private Boolean validateOnlyReward;
    
    @JsonProperty("validateAvailableQuota")
    private Boolean validateAvailableQuota;
    
    @JsonProperty("location")
    private Location location;
    
    @JsonProperty("lobDetails")
    private List<CTCartItem> lobDetails;

    @JsonProperty("billingProductCodes")
    private List<String> billingProductCodes;
    private List<String> productIds;
    private List<String> offerIds;
    @JsonProperty("offerCodes")
	private List<String> offerCodes;
    
    private List<CTLineItems> lineItems;

	public List<String> getBillingProductCodes() {
		return billingProductCodes;
	}

	public void setBillingProductCodes(List<String> billingProductCodes) {
		this.billingProductCodes = billingProductCodes;
	}

	public List<String> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<String> productIds) {
		this.productIds = productIds;
	}

	public List<String> getOfferIds() {
		return offerIds;
	}

	public void setOfferIds(List<String> offerIds) {
		this.offerIds = offerIds;
	}

	public List<String> getOfferCodes() {
		return offerCodes;
	}

	public void setOfferCodes(List<String> offerCodes) {
		this.offerCodes = offerCodes;
	}	

	public String getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}

	public Boolean getValidateOnlyReward() {
		return validateOnlyReward;
	}

	public void setValidateOnlyReward(Boolean validateOnlyReward) {
		this.validateOnlyReward = validateOnlyReward;
	}

	public Boolean getValidateAvailableQuota() {
		return validateAvailableQuota;
	}

	public void setValidateAvailableQuota(Boolean validateAvailableQuota) {
		this.validateAvailableQuota = validateAvailableQuota;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public List<CTCartItem> getLobDetails() {
		return lobDetails;
	}

	public void setLobDetails(List<CTCartItem> lobDetails) {
		this.lobDetails = lobDetails;
	}

	public List<CTLineItems> getLineItems() {
		return lineItems;
	}

	public void setLineItems(List<CTLineItems> lineItems) {
		this.lineItems = lineItems;
	}

	@Override
	public String toString() {
		return "CTCartContext [" + (salesChannel != null ? "salesChannel=" + salesChannel + ", " : "")
				+ (validateOnlyReward != null ? "validateOnlyReward=" + validateOnlyReward + ", " : "")
				+ (validateAvailableQuota != null ? "validateAvailableQuota=" + validateAvailableQuota + ", " : "")
				+ (location != null ? "location=" + location + ", " : "")
				+ (lobDetails != null ? "lobDetails=" + lobDetails + ", " : "")
				+ (billingProductCodes != null ? "billingProductCodes=" + billingProductCodes + ", " : "")
				+ (productIds != null ? "productIds=" + productIds + ", " : "")
				+ (offerIds != null ? "offerIds=" + offerIds + ", " : "")
				+ (offerCodes != null ? "offerCodes=" + offerCodes + ", " : "")
				+ (lineItems != null ? "lineItems=" + lineItems : "") + "]";
	}

	
}
