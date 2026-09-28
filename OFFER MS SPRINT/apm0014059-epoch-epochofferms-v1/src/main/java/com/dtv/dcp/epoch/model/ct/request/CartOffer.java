package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class CartOffer implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @JsonProperty("offerCode")
    private String offerCode;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    private String action;
    
	private String offerProductType;

	public String getOfferProductType() {
		return offerProductType;
	}

	public void setOfferProductType(String offerProductType) {
		this.offerProductType = offerProductType;
	}
	
	public String getAction() {
		return action;
	}



	public void setAction(String action) {
		this.action = action;
	}



	public String getOfferCode() {
		return offerCode;
	}



	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}



	public Integer getQuantity() {
		return quantity;
	}



	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}



	@Override
	public String toString() {
		return "CartOffer [" + (offerCode != null ? "offerCode=" + offerCode + ", " : "")
				+ (quantity != null ? "quantity=" + quantity + ", " : "") + "]";
	}

	
}
