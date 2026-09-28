package com.dtv.dcp.epoch.model.catalogone.offer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author dr000y
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PriceAdjustment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String priceType;

	private String adjustmentType;

	private Price price;

	public void setPriceType(String priceType) {
		this.priceType = priceType;
	}

	public String getPriceType() {
		return this.priceType;
	}

	public void setAdjustmentType(String adjustmentType) {
		this.adjustmentType = adjustmentType;
	}

	public String getAdjustmentType() {
		return this.adjustmentType;
	}

	public void setPrice(Price price) {
		this.price = price;
	}

	public Price getPrice() {
		return this.price;
	}

	@Override
	public String toString() {
		return "PriceAdjustment [priceType=" + priceType + ", adjustmentType=" + adjustmentType + ", price=" + price
				+ "]";
	}
}
